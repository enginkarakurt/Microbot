/*
 * Copyright (c) 2024, Adam <Adam@sigterm.info>
 * Copyright (c) 2025, Zoinkwiz <https://github.com/Zoinkwiz>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.microbot.questhelper.bank.banktab;

import net.runelite.client.plugins.microbot.questhelper.managers.QuestContainerManager;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.bank.BankSearch;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Potion
{
    EnumComposition potionEnum;
    int itemId;
    int doses;
    int withdrawDoses;
}

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class PotionStorage
{
    static final int VIAL_IDX = 514;
    static final int COMPONENTS_PER_POTION = 5;

    private final Client client;
    private final BankSearch bankSearch;

    private Potion[] potions;

    /* represents that something has occurred which means we should update the values of potions **/
    public boolean updateCachedPotions;

    private boolean updateBankLayout;

    @Setter
    private QuestBankTabInterface questBankTabInterface;

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        if (updateCachedPotions)
        {
            log.debug("Rebuilding potions");
            updateCachedPotions = false;
            rebuildPotions();

            if (updateBankLayout)
            {
                updateBankLayout = false;
                if (questBankTabInterface.isQuestTabActive())
                {
                    bankSearch.layoutBank();
                }
            }
        }
    }

    // Use varp change event instead of a widget change listener so that we can recache the potions prior to
    // the cs2 vm running.
    @Subscribe
    public void onVarbitChanged(VarbitChanged varbitChanged)
    {
        if (VarPlayerID.POTIONSTORE_VIALS == varbitChanged.getVarpId())
        {
            updateCachedPotions = true;
            updateBankLayout = true; // trigger a bank rebuild as the qty has changed
        }
    }

    private void rebuildPotions()
    {
        var potionStorePotions = client.getEnum(EnumID.POTIONSTORE_POTIONS);
        var potionStoreUnfinishedPotions = client.getEnum(EnumID.POTIONSTORE_UNFINISHED_POTIONS);
        potions = new Potion[potionStorePotions.size() + potionStoreUnfinishedPotions.size() + 1];
        int potionsIdx = 0;
        for (EnumComposition e : new EnumComposition[]{potionStorePotions, potionStoreUnfinishedPotions})
        {
            for (int potionEnumId : e.getIntVals())
            {
                var potionEnum = client.getEnum(potionEnumId);
                client.runScript(ScriptID.POTIONSTORE_DOSES, potionEnumId);
                int doses = client.getIntStack()[0];
                client.runScript(ScriptID.POTIONSTORE_WITHDRAW_DOSES, potionEnumId);
                int withdrawDoses = client.getIntStack()[0];

                if (doses > 0 && withdrawDoses > 0)
                {
                    Potion p = new Potion();
                    p.potionEnum = potionEnum;
                    p.itemId = potionEnum.getIntValue(withdrawDoses);
                    p.doses = doses;
                    p.withdrawDoses = withdrawDoses;
                    potions[potionsIdx] = p;
                }

                ++potionsIdx;
            }
        }

        // Add vial
        Potion p = new Potion();
        p.potionEnum = null;
        p.itemId = ItemID.VIAL_EMPTY;
        p.doses = client.getVarpValue(VarPlayerID.POTIONSTORE_VIALS);
        p.withdrawDoses = 0;
        potions[potions.length - 1] = p;

        Item[] newTrackedPotions = getItems();

        Map<Integer, Integer> newPotionsAsMap = itemArrayToMap(newTrackedPotions);
        Map<Integer, Integer> oldPotionsAsMap = itemArrayToMap(QuestContainerManager.getPotionData().getItems());
        if (!oldPotionsAsMap.equals(newPotionsAsMap))
        {
            updateBankLayout = true;
        }

        QuestContainerManager.getPotionData().update(client.getTickCount(), newTrackedPotions);
    }

    public Item[] getItems()
    {
        if (potions == null)
        {
            return new Item[0];
        }

        List<Item> items = new ArrayList<>();

        for (Potion potion : potions)
        {
            if (potion == null) continue;
            var potionEnum = potion.potionEnum;
            if (potionEnum != null)
            {
                int potionItemId = potionEnum.getIntValue(potion.withdrawDoses);
                int quantity = potion.doses / potion.withdrawDoses;
                items.add(new Item(potionItemId, quantity));
            }
            else
            {
                items.add(new Item(potion.itemId, potion.doses));
            }
        }

        return items.toArray(new Item[0]);
    }

    int count(int itemId)
    {
        if (potions == null)
        {
            return 0;
        }

        for (Potion potion : potions)
        {
            if (potion != null && potion.itemId == itemId)
            {
                if (potion.withdrawDoses != 0)
                {
                    return potion.doses / potion.withdrawDoses;
                }

                return potion.doses;
            }
        }
        return 0;
    }

    int find(int itemId)
    {
        if (potions == null)
        {
            return -1;
        }

        if (itemId == ItemID.VIAL_EMPTY)
        {
            return VIAL_IDX;
        }

        int potionIdx = 0;
        for (Potion potion : potions)
        {
            ++potionIdx;
            if (potion != null && potion.itemId == itemId)
            {
                return potionIdx - 1;
            }
        }
        return -1;
    }

    public void prepareWidgets()
    {
        // if the potion store hasn't been opened yet, the client components won't have been made yet.
        // they need to exist for the click to work correctly.
        Widget potStoreContent = client.getWidget(InterfaceID.Bankmain.POTIONSTORE_ITEMS);
        if (potStoreContent.getChildren() == null)
        {
            int childIdx = 0;
            for (int i = 0; i < potions.length; ++i) // NOPMD: ForLoopCanBeForeach
            {
                for (int j = 0; j < COMPONENTS_PER_POTION; ++j)
                {
                    potStoreContent.createChild(childIdx++, WidgetType.GRAPHIC);
                }
            }
        }
    }

    private Map<Integer, Integer> itemArrayToMap(Item[] items)
    {
        if (items == null) return new HashMap<>();
        Map<Integer, Integer> quantityMap = new HashMap<>();
        for (Item item : items)
        {
            quantityMap.put(
                    item.getId(),
                    quantityMap.getOrDefault(item.getId(), 0) + item.getQuantity()
            );
        }
        return quantityMap;
    }
}
