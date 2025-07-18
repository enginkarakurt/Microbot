/*
 * Copyright (c) 2021, Zoinkwiz
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
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.entertheabyss;

import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.questinfo.QuestHelperQuest;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.player.FreeInventorySlotRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.quest.QuestRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.var.VarbitRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ExperienceReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.ItemReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.UnlockReward;
import net.runelite.client.plugins.microbot.questhelper.steps.ConditionalStep;
import net.runelite.client.plugins.microbot.questhelper.steps.NpcStep;
import net.runelite.client.plugins.microbot.questhelper.steps.ObjectStep;
import net.runelite.client.plugins.microbot.questhelper.steps.QuestStep;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;

import java.util.*;

public class EnterTheAbyss extends BasicQuestHelper
{
	// Recommended
	ItemRequirement varrockTeleport, ardougneTeleport, edgevilleTeleport, passageTeleport;

	// Items during quest
	ItemRequirement scryingOrb, scryingOrbCharged;

	Requirement inWizardBasement, teleportedFromVarrock, teleportedFromArdougne, teleportedFromWizardsTower,
		teleportedFromGnome, teleportedFromDistentor, freeInventorySpace;

	QuestStep talkToMageInWildy, talkToMageInVarrock, talkToAubury, goDownInWizardsTower, talkToSedridor,
		talkToCromperty, talkToMageAfterTeleports, talkToMageToFinish;

	Zone wizardBasement;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();
		Map<Integer, QuestStep> steps = new HashMap<>();

		steps.put(0, talkToMageInWildy);
		steps.put(1, talkToMageInVarrock);

		ConditionalStep locateEssenceMine = new ConditionalStep(this, talkToAubury);
		locateEssenceMine.addStep(new Conditions(scryingOrbCharged), talkToMageAfterTeleports);
		locateEssenceMine.addStep(new Conditions(teleportedFromVarrock, teleportedFromWizardsTower), talkToCromperty);
		locateEssenceMine.addStep(new Conditions(teleportedFromVarrock, inWizardBasement), talkToSedridor);
		locateEssenceMine.addStep(teleportedFromVarrock, goDownInWizardsTower);
		steps.put(2, locateEssenceMine);

		steps.put(3, talkToMageToFinish);

		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		varrockTeleport = new ItemRequirement("Teleports to Varrock", ItemID.POH_TABLET_VARROCKTELEPORT, 2);
		ardougneTeleport = new ItemRequirement("Teleport to Ardougne", ItemID.POH_TABLET_ARDOUGNETELEPORT);
		edgevilleTeleport = new ItemRequirement("Teleport to Edgeville", ItemCollections.AMULET_OF_GLORIES);
		passageTeleport = new ItemRequirement("Teleport to Wizards' Tower", ItemCollections.NECKLACE_OF_PASSAGES);

		scryingOrb = new ItemRequirement("Scrying orb", ItemID.SCRYING_ORB_EMPTY);
		scryingOrb.setTooltip("You can get another from the Mage of Zamorak in south east Varrock");

		scryingOrbCharged = new ItemRequirement("Scrying orb", ItemID.SCRYING_ORB_FULL);
		scryingOrbCharged.setTooltip("You can get another from the Mage of Zamorak in south east Varrock");

		freeInventorySpace = new FreeInventorySlotRequirement(1);
	}

	@Override
	protected void setupZones()
	{
		wizardBasement = new Zone(new WorldPoint(3094, 9553, 0), new WorldPoint(3125, 9582, 0));
	}

	public void setupConditions()
	{
		inWizardBasement = new ZoneRequirement(wizardBasement);

		teleportedFromWizardsTower = new VarbitRequirement(2314, 1);
		teleportedFromVarrock = new VarbitRequirement(2315, 1);
		teleportedFromArdougne = new VarbitRequirement(2316, 1);
		teleportedFromDistentor = new VarbitRequirement(2317, 1);
		teleportedFromGnome = new VarbitRequirement(2318, 1);
	}

	public void setupSteps()
	{
		talkToMageInWildy = new NpcStep(this, NpcID.RCU_ZAMMY_MAGE1A, new WorldPoint(3102, 3557, 0), "Talk to the Mage" +
			" of Zamorak in the Wilderness north of Edgeville. BRING NOTHING AS YOU CAN BE KILLED BY OTHER PLAYERS HERE.");

		talkToMageInVarrock = new NpcStep(this, NpcID.RCU_ZAMMY_MAGE1_EDGEB, new WorldPoint(3259, 3383, 0),
			"Talk to the Mage of Zamorak in south east Varrock.", freeInventorySpace);
		talkToMageInVarrock.addDialogSteps("Where do you get your runes from?", "Maybe I could make it worth your while?", "Yes, but I can still help you as well.",
			"I did it so that I could then steal their secrets.", "Deal.",
			"But I'm a loyal servant of Zamorak as well!", "Okay, fine. I don't really serve Zamorak.", "Because I can still help you.");

		talkToAubury = new NpcStep(this, NpcID.AUBURY_3OP, new WorldPoint(3253, 3401, 0),
			"Teleport to the essence mine with Aubury in south east Varrock.", scryingOrb);
		talkToAubury.addDialogStep("Can you teleport me to the Rune Essence Mine?");

		goDownInWizardsTower = new ObjectStep(this, ObjectID.WIZARDS_TOWER_LADDERTOP, new WorldPoint(3104, 3162, 0),
			"Teleport to the essence mine with Sedridor in the Wizard Tower's basement.", scryingOrb);
		goDownInWizardsTower.addDialogStep("Wizard's Tower");
		talkToSedridor = new NpcStep(this, NpcID.HEAD_WIZARD_1OP, new WorldPoint(3104, 9571, 0),
			"Teleport to the essence mine with Sedridor in the Wizard Tower's basement.", scryingOrb);
		((NpcStep) talkToSedridor).addAlternateNpcs(5034);
		talkToSedridor.addDialogStep("Can you teleport me to the Rune Essence Mine?");
		talkToSedridor.addSubSteps(goDownInWizardsTower);

		talkToCromperty = new NpcStep(this, NpcID.CROMPERTY_PRE_DIARY, new WorldPoint(2684, 3323, 0),
			"Teleport to the essence mine with Wizard Cromperty in East Ardougne.", scryingOrb);
		talkToCromperty.addDialogStep("Can you teleport me to the Rune Essence Mine?");

		talkToMageAfterTeleports = new NpcStep(this, NpcID.RCU_ZAMMY_MAGE1_EDGEB, new WorldPoint(3259, 3383, 0),
			"Talk to the Mage of Zamorak in south east Varrock.", scryingOrbCharged);
		talkToMageToFinish = new NpcStep(this, NpcID.RCU_ZAMMY_MAGE1_EDGEB, new WorldPoint(3259, 3383, 0),
			"Talk to the Mage of Zamorak again.");
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		return Arrays.asList(edgevilleTeleport, varrockTeleport, passageTeleport, ardougneTeleport);
	}

	@Override
	public List<String> getNotes()
	{
		return Collections.singletonList("The start of this miniquest is in the Wilderness. Other players can " +
			"attack you there, so make sure to not bring anything there!");
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		ArrayList<Requirement> req = new ArrayList<>();
		req.add(new QuestRequirement(QuestHelperQuest.RUNE_MYSTERIES, QuestState.FINISHED));
		return req;
	}

	@Override
	public List<ExperienceReward> getExperienceRewards()
	{
		return Collections.singletonList(new ExperienceReward(Skill.RUNECRAFT, 1000));
	}

	@Override
	public List<ItemReward> getItemRewards()
	{
		return Collections.singletonList(new ItemReward("A small rune pouch", ItemID.RCU_POUCH_SMALL, 1));
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return Collections.singletonList(new UnlockReward("Ability to enter The Abyss"));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		allSteps.add(new PanelDetails("Helping the Zamorakians",
			Arrays.asList(talkToMageInWildy, talkToMageInVarrock, talkToAubury, talkToSedridor,
				talkToCromperty, talkToMageAfterTeleports, talkToMageToFinish)));

		return allSteps;
	}
}
