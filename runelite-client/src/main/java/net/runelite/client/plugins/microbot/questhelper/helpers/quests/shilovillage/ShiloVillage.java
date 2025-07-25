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
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.shilovillage;

import net.runelite.client.plugins.microbot.questhelper.bank.banktab.BankSlotIcons;
import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.questinfo.QuestHelperQuest;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.ObjectCondition;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemOnTileRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.npc.DialogRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.npc.NpcInteractingRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.player.FreeInventorySlotRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.player.SkillRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.quest.QuestRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicType;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.Operation;
import net.runelite.client.plugins.microbot.questhelper.requirements.var.VarbitRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.var.VarplayerRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.widget.WidgetTextRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ExperienceReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.UnlockReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.*;

import java.util.*;

public class ShiloVillage extends BasicQuestHelper
{
	// Required
	ItemRequirement spade, torchOrCandle, rope, bronzeWire, chisel, bones3;

	// Recommended
	ItemRequirement combatGear, food, staminas, antipoison, prayerPotions, quickTeleport, papyrus, charcoal,
		crumbleUndead;

	ItemRequirement belt, stonePlaque, tatteredScroll, crumpledScroll, zadimusCorpse, boneShard, beads, pommel,
		beadsOfTheDead, boneKey, rashCorpse;

	Requirement emptySlot3, moundNearby, inCavern1, inCavern2, inCavern3, inCavern4, shownStone, shownCorpse,
		hasReadTattered, hasReadCrumpled, revealedDoor, searchedDoor, doorOpened, nazNearby, corpseNearby;

	QuestStep talkToMosol, useBeltOnTrufitus, useSpadeOnGround, useTorchOnFissure, useRopeOnFissure,
		lookAtMound, searchFissure, useChiselOnStone, enterDeeperCave, searchForTatteredScroll, searchForCrumpledScroll,
		searchForCorpse, useCorpseOnTrufitus, useStonePlaqueOnTrufitus, readTattered, readCrumpled, buryCorpse;

	QuestStep searchRocksOnCairn, searchDolmen, useChiselOnPommel, useWireOnBeads;

	QuestStep searchPalms, searchDoors, makeKey, useKeyOnDoor, enterDoor, useBonesOnDoor, searchDolmenForFight, killNazastarool,
		pickupCorpse;

	QuestStep enterCairnAgain, useCorpseOnDolmen;

	Zone cavern1, cavern2, cavern3, cavern4;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();
		Map<Integer, QuestStep> steps = new HashMap<>();

		ConditionalStep goStartQuest = new ConditionalStep(this, talkToMosol);
		goStartQuest.addStep(belt.alsoCheckBank(questBank), useBeltOnTrufitus);
		steps.put(0, goStartQuest);

		steps.put(1, useSpadeOnGround);
		steps.put(2, useSpadeOnGround);
		steps.put(3, useSpadeOnGround);

		ConditionalStep lightUpFissure = new ConditionalStep(this, useTorchOnFissure);
		lightUpFissure.addStep(moundNearby, lookAtMound);
		steps.put(4, lightUpFissure);

		ConditionalStep goUseRope = new ConditionalStep(this, useRopeOnFissure);
		goUseRope.addStep(moundNearby, lookAtMound);
		steps.put(5, goUseRope);

		ConditionalStep goGetItems = new ConditionalStep(this, searchFissure);
		goGetItems.addStep(new Conditions(beadsOfTheDead, boneKey), useKeyOnDoor);
		goGetItems.addStep(new Conditions(beadsOfTheDead, searchedDoor), makeKey);
		goGetItems.addStep(new Conditions(beadsOfTheDead, revealedDoor), searchDoors);
		goGetItems.addStep(new Conditions(beadsOfTheDead), searchPalms);
		goGetItems.addStep(new Conditions(beads), useWireOnBeads);
		goGetItems.addStep(new Conditions(pommel), useChiselOnPommel);
		goGetItems.addStep(new Conditions(inCavern3), searchDolmen);
		// Stone-plaque may be useless?
		// Don't need to show Trufitus bone shard
		// Reading tattered unlocks access to the island cavern
		// Reading crumpled lets you use bronze wire on beads
		goGetItems.addStep(new Conditions(stonePlaque, hasReadTattered, hasReadCrumpled, boneShard), searchRocksOnCairn);
		goGetItems.addStep(new Conditions(stonePlaque, hasReadTattered, crumpledScroll, boneShard), readCrumpled);
		goGetItems.addStep(new Conditions(stonePlaque, tatteredScroll, crumpledScroll, boneShard), readTattered);
		goGetItems.addStep(new Conditions(stonePlaque, tatteredScroll, crumpledScroll, zadimusCorpse), buryCorpse);
		goGetItems.addStep(new Conditions(inCavern2, stonePlaque, tatteredScroll, crumpledScroll), searchForCorpse);
		goGetItems.addStep(new Conditions(inCavern2, stonePlaque, tatteredScroll), searchForCrumpledScroll);
		goGetItems.addStep(new Conditions(inCavern2, stonePlaque), searchForTatteredScroll);
		goGetItems.addStep(new Conditions(inCavern1, stonePlaque), enterDeeperCave);
		goGetItems.addStep(inCavern1, useChiselOnStone);
		goGetItems.addStep(moundNearby, lookAtMound);
		steps.put(6, goGetItems);
		steps.put(7, goGetItems);
		steps.put(8, goGetItems);
		steps.put(9, goGetItems);

		ConditionalStep goToDolem = new ConditionalStep(this, enterDoor);
		goToDolem.addStep(inCavern4, useBonesOnDoor);
		steps.put(10, goToDolem);
		steps.put(11, goToDolem);

		ConditionalStep goDoFight = new ConditionalStep(this, enterDoor);
		goDoFight.addStep(new Conditions(rashCorpse, inCavern3), useCorpseOnDolmen);
		goDoFight.addStep(rashCorpse, enterCairnAgain);
		goDoFight.addStep(corpseNearby, pickupCorpse);
		goDoFight.addStep(nazNearby, killNazastarool);
		goDoFight.addStep(inCavern4, searchDolmenForFight);
		steps.put(12, goDoFight);
		steps.put(13, goDoFight);
		steps.put(14, goDoFight);

		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		spade = new ItemRequirement("Spade", ItemID.SPADE).isNotConsumed();
		torchOrCandle = new ItemRequirement("Lit torch or candle", ItemID.TORCH_LIT);
		torchOrCandle.addAlternates(ItemID.LIT_CANDLE);
		torchOrCandle.setTooltip("You will NOT get this item back");
		rope = new ItemRequirement("Rope", ItemID.ROPE);
		bronzeWire = new ItemRequirement("Bronze wire", ItemID.BRONZECRAFTWIRE);
		chisel = new ItemRequirement("Chisel", ItemID.CHISEL).isNotConsumed();
		bones3 = new ItemRequirement("Bones", ItemID.BONES, 3);

		combatGear = new ItemRequirement("Combat gear", -1, -1).isNotConsumed();
		combatGear.setDisplayItemId(BankSlotIcons.getCombatGear());
		food = new ItemRequirement("Food", ItemCollections.GOOD_EATING_FOOD, -1);
		staminas = new ItemRequirement("Stamina potions", ItemCollections.STAMINA_POTIONS, -1);
		antipoison = new ItemRequirement("Antipoisons", ItemCollections.ANTIPOISONS, -1);
		prayerPotions = new ItemRequirement("Prayer potions", ItemCollections.PRAYER_POTIONS, -1);
		quickTeleport = new ItemRequirement("A quick teleport for escaping", -1, -1);
		papyrus = new ItemRequirement("Papyrus", ItemID.PAPYRUS);
		charcoal = new ItemRequirement("Charcoal", ItemID.CHARCOAL);
		crumbleUndead = new ItemRequirement("Crumble undead spell for final boss", -1, -1);

		emptySlot3 = new FreeInventorySlotRequirement(3);

		belt = new ItemRequirement("Wampum belt", ItemID.MOSOL_WAMPUM_BELT);
		stonePlaque = new ItemRequirement("Stone-plaque", ItemID.ZQPLAQUE);
		tatteredScroll = new ItemRequirement("Tattered scroll", ItemID.ZQBERVIRIUSSCROLL);
		crumpledScroll = new ItemRequirement("Crumpled scroll", ItemID.ZQRASHILIYIASCROLL);
		zadimusCorpse = new ItemRequirement("Zadimus corpse", ItemID.ZQZADIMUSBONES);
		zadimusCorpse.addAlternates(ItemID.ZQBONESHARD, ItemID.ZQBONEKEY);
		boneShard = new ItemRequirement("Bone shard", ItemID.ZQBONESHARD);
		boneShard.addAlternates(ItemID.ZQBONEKEY);
		boneKey = new ItemRequirement("Bone key", ItemID.ZQBONEKEY);

		pommel = new ItemRequirement("Sword pommel", ItemID.ZQBEVSWORD);
		beads = new ItemRequirement("Bone beads", ItemID.ZQBONEBEADS);
		beadsOfTheDead = new ItemRequirement("Beads of the dead", ItemID.ZQDEADBEADS);
		rashCorpse = new ItemRequirement("Rashiliya corpse", ItemID.ZQCORPSE);
	}

	@Override
	protected void setupZones()
	{
		cavern1 = new Zone(new WorldPoint(2870, 9330, 0), new WorldPoint(2950, 9407, 0));
		cavern2 = new Zone(new WorldPoint(2878, 9282, 0), new WorldPoint(2942, 9333, 0));
		cavern3 = new Zone(new WorldPoint(2751, 9353, 0), new WorldPoint(2770, 9397, 0));
		cavern4 = new Zone(new WorldPoint(2833, 9463, 0), new WorldPoint(2943, 9533, 0));
	}

	public void setupConditions()
	{
		moundNearby = new ObjectCondition(ObjectID.AHZARHOON_ENTRANCE, new WorldPoint(2922, 3000, 0));
		inCavern1 = new ZoneRequirement(cavern1);
		inCavern2 = new ZoneRequirement(cavern2);
		inCavern3 = new ZoneRequirement(cavern3);
		inCavern4 = new ZoneRequirement(cavern4);

		shownCorpse = new Conditions(true, LogicType.OR,
			new DialogRequirement("The ground in the centre of the village"),
			boneShard
		);

		shownStone = new Conditions(true, LogicType.OR,
			new DialogRequirement("If you have found anything else that you need " +
				"help with, please just let me know."),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "<str>Trufitus identified the plaque")
		);

		hasReadTattered = new Conditions(true, LogicType.OR,
			new WidgetTextRequirement(220, 3, "Bervirius, son of King Danthalas"),
			new VarplayerRequirement(VarPlayerID.ZOMBIEQUEEN, 9, Operation.GREATER_EQUAL)
		);

		hasReadCrumpled = new Conditions(true, LogicType.OR,
			new WidgetTextRequirement(222, 3, "Rashiliyia's rage went unchecked."),
			beadsOfTheDead
		);

		revealedDoor = new VarbitRequirement(VarbitID.ZQDOOR_MULTI, 1, Operation.GREATER_EQUAL);
		doorOpened = new VarbitRequirement(VarbitID.ZQDOOR_MULTI, 2, Operation.GREATER_EQUAL);
		searchedDoor = new Conditions(true, LogicType.OR,
			new WidgetTextRequirement(229, 1, "Examining the door,")
		);

		nazNearby = new NpcInteractingRequirement(NpcID.ZQ_MAINZOMBIE1, NpcID.ZQ_MAINZOMBIE2, NpcID.ZQ_MAINZOMBIE3);

		corpseNearby = new ItemOnTileRequirement(ItemID.ZQCORPSE);
	}

	public void setupSteps()
	{
		talkToMosol = new NpcStep(this, NpcID.MOSOL_REI, new WorldPoint(2884, 2951, 0),
			"Talk to Mosol Rei east of Shilo Village in south Karamja.");
		talkToMosol.addDialogSteps("Why do I need to run?", "Rashiliyia? Who is she?",
			"I'll go to see the Shaman.", "Yes, I'm sure and I'll take the Wampum belt to Trufitus.");
		talkToMosol.addDialogStep(1, "What can we do?");
		useBeltOnTrufitus = new NpcStep(this, NpcID.TRUFITUS, new WorldPoint(2809, 3085, 0),
			"Use the belt on to Trufitus.", belt.highlighted());
		useBeltOnTrufitus.addIcon(ItemID.MOSOL_WAMPUM_BELT);
		useBeltOnTrufitus.addDialogSteps("Mosol Rei said something about a legend?", "Why was it called Ah Za Rhoon?",
			"Tell me more.", "I am going to search for Ah Za Rhoon!",
			"Yes, I will seriously look for Ah Za Rhoon and I'd appreciate your help.", "Yes.");
		useSpadeOnGround = new ObjectStep(this, ObjectID.AHZARHOON_ENTRANCE, new WorldPoint(2922, 3000, 0),
			"Use a spade on the ground in south east Karamja.", spade.highlighted());
		useSpadeOnGround.addIcon(ItemID.SPADE);

		lookAtMound = new ObjectStep(this, ObjectID.AHZARHOON_ENTRANCE, new WorldPoint(2922, 3000, 0),
			"Look at the mound of earth in south east Karamja.");
		useTorchOnFissure = new ObjectStep(this, ObjectID.AHZARHOON_ENTRANCE_FISSURE, new WorldPoint(2922, 3000, 0),
			"Use a lit torch or candle on the fissure.", torchOrCandle.highlighted());
		useTorchOnFissure.addDialogStep("Yes");
		useTorchOnFissure.addIcon(ItemID.LIT_CANDLE);
		useTorchOnFissure.addSubSteps(lookAtMound);
		useRopeOnFissure = new ObjectStep(this, ObjectID.AHZARHOON_ENTRANCE_FISSURE, new WorldPoint(2922, 3000, 0),
			"Use a rope on the fissure.", rope.highlighted());
		useRopeOnFissure.addIcon(ItemID.ROPE);
		searchFissure = new ObjectStep(this, ObjectID.AHZARHOON_ENTRANCE_FISSURE_WITHROPE, new WorldPoint(2922, 3000, 0),
			"Right-click search the fissure.");
		searchFissure.addDialogStep("Yes, I'll give it a go!");
		useChiselOnStone = new ObjectStep(this, ObjectID.ZQSECRETSTONE, new WorldPoint(2901, 9379, 0),
			"Use a chisel on the strange looking stone to the south.", chisel.highlighted());
		useChiselOnStone.addIcon(ItemID.CHISEL);
		enterDeeperCave = new ObjectStep(this, ObjectID.SECRETRUBBLE, new WorldPoint(2888, 9373, 0),
			"Search the cave in to go deeper in the caverns.");
		enterDeeperCave.addDialogStep("Yes, I'll wriggle through.");
		searchForTatteredScroll = new ObjectStep(this, ObjectID.SECRETRUBBLEBOOK, new WorldPoint(2885, 9318, 0),
			"Search the loose rocks to the north for a tattered scroll.");
		searchForTatteredScroll.addDialogStep("Yes, I'll carefully move the rocks to see what's behind them.");
		searchForCrumpledScroll = new ObjectStep(this, ObjectID.ZQSACKS, new WorldPoint(2939, 9285, 0),
			"Search the old sacks to the east for a crumpled scroll.");
		searchForCorpse = new ObjectStep(this, ObjectID.ZQGALLOWS, new WorldPoint(2935, 9326, 0),
			"Right-click search the gallows in the north east corner for a corpse.");
		searchForCorpse.addDialogStep("Yes, I may find something else on the corpse.");

		useCorpseOnTrufitus = new NpcStep(this, NpcID.TRUFITUS, new WorldPoint(2809, 3085, 0),
			"Use Zadimus's corpse on Trufitus.", zadimusCorpse.highlighted());
		useCorpseOnTrufitus.addDialogStep("Is there any sacred ground around here?");
		useCorpseOnTrufitus.addIcon(ItemID.ZQZADIMUSBONES);

		useStonePlaqueOnTrufitus = new NpcStep(this, NpcID.TRUFITUS, new WorldPoint(2809, 3085, 0),
			"Use the stone-plaque on Trufitus.", stonePlaque.highlighted());
		useStonePlaqueOnTrufitus.addIcon(ItemID.ZQPLAQUE);

		readTattered = new DetailedQuestStep(this, "Read the tattered scroll.", tatteredScroll.highlighted());
		readTattered.addDialogSteps("Yes please.");

		readCrumpled = new DetailedQuestStep(this, "Read the crumpled scroll.", crumpledScroll.highlighted());
		readCrumpled.addDialogSteps("Yes please.");

		buryCorpse = new DetailedQuestStep(this, new WorldPoint(2795, 3089, 0),
			"Bury Zadimus's corpse in the middle of Tai Bwo Wannai.", zadimusCorpse.highlighted());
		buryCorpse.addIcon(ItemID.ZQZADIMUSBONES);

		searchRocksOnCairn = new ObjectStep(this, ObjectID.ZQROCKS, new WorldPoint(2762, 2990, 0),
			"Right-click search the rocks on Cairn Isle.");
		searchRocksOnCairn.addDialogSteps("Yes please, I can think of nothing nicer!");
		searchDolmen = new ObjectStep(this, ObjectID.ZQDOLMEN, new WorldPoint(2767, 9365, 0),
			"Right-click search the dolmen to the south.");
		useChiselOnPommel = new DetailedQuestStep(this, "Use a chisel on the pommel.", chisel.highlighted(),
			pommel.highlighted());
		useWireOnBeads = new DetailedQuestStep(this, "Use bronze wire on the beads.", bronzeWire.highlighted(),
			beads.highlighted());

		searchPalms = new ObjectStep(this, ObjectID.ZQQUEST_HIDYTREE, new WorldPoint(2916, 3093, 0),
			"Search the palm trees in the north east of Karamja. Come equipped for a boss fight.",
			List.of(beadsOfTheDead.equipped(), boneShard, chisel, bones3,
			combatGear), List.of(crumbleUndead, food));

		searchDoors = new ObjectStep(this, ObjectID.HILLSIDEDOORR_MULTI, new WorldPoint(2916, 3091, 0),
			"Right-click search the doors behind the palm trees.", combatGear);

		// 8180 opened?
		makeKey = new DetailedQuestStep(this, "Use a chisel on the bone shard.", chisel.highlighted(), boneShard.highlighted());
		useKeyOnDoor = new ObjectStep(this, ObjectID.HILLSIDEDOORR_MULTI, new WorldPoint(2916, 3091, 0),
			"Use the bone key on the doors behind the palm trees.", boneKey.highlighted());
		useKeyOnDoor.addIcon(ItemID.ZQBONEKEY);
		enterDoor = new ObjectStep(this, ObjectID.HILLSIDEDOORR_MULTI, new WorldPoint(2916, 3091, 0),
			"Enter the doors behind the palm trees.", beadsOfTheDead.equipped(), bones3,
			combatGear);
		useBonesOnDoor = new ObjectStep(this, ObjectID.THZQ_TOMBROOML1, new WorldPoint(2892, 9480, 0),
			"Equip the beads of the dead, then make your way through the gate, down the rocks, then to the south west" +
				" corner. Use bones on the door there.",
			beadsOfTheDead.equipped(), bones3.highlighted());
		useBonesOnDoor.addIcon(ItemID.BONES);
		searchDolmenForFight = new ObjectStep(this, ObjectID.ZQRASHDOLMEN, new WorldPoint(2893, 9488, 0),
			"Search the dolmen, ready to fight.");

		killNazastarool = new NpcStep(this, NpcID.ZQ_MAINZOMBIE1, new WorldPoint(2892, 9488, 0),
			"Defeat Nazastarool's 3 forms. You can safe spot them over the dolmen, and the Crumble Undead spell is very" +
				" strong against them.");
		((NpcStep) killNazastarool).addAlternateNpcs(NpcID.ZQ_MAINZOMBIE2, NpcID.ZQ_MAINZOMBIE3);
		((NpcStep)killNazastarool).addSafeSpots(new WorldPoint(2894, 9486, 0), new WorldPoint(2891, 9486, 0));
		pickupCorpse = new ItemStep(this, "Pickup Rashiliyia's corpse.", rashCorpse);
		enterCairnAgain = new ObjectStep(this, ObjectID.ZQROCKS, new WorldPoint(2762, 2990, 0),
			"Right-click search the rocks on Cairn Isle to enter the caverns again.", rashCorpse);
		enterCairnAgain.addDialogSteps("Yes please, I can think of nothing nicer!");
		useCorpseOnDolmen = new ObjectStep(this, ObjectID.ZQDOLMEN, new WorldPoint(2767, 9365, 0),
			"Use Rashiliyia's corpse on the dolmen to the south.", rashCorpse.highlighted());
		useCorpseOnDolmen.addIcon(ItemID.ZQCORPSE);
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		return Arrays.asList(spade, torchOrCandle, rope, bronzeWire, chisel, bones3);
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		return Arrays.asList(combatGear, food, staminas, antipoison, prayerPotions, quickTeleport,
			papyrus, charcoal, crumbleUndead);
	}


	@Override
	public List<String> getCombatRequirements()
	{
		return Collections.singletonList("Nazastarool 3 times (levels 68, 91, 93) (safespottable)");
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		ArrayList<Requirement> req = new ArrayList<>();
		req.add(new QuestRequirement(QuestHelperQuest.JUNGLE_POTION, QuestState.FINISHED));
		req.add(new SkillRequirement(Skill.CRAFTING, 20));
		req.add(new SkillRequirement(Skill.AGILITY, 32));
		return req;
	}

	@Override
	public QuestPointReward getQuestPointReward()
	{
		return new QuestPointReward(2);
	}

	@Override
	public List<ExperienceReward> getExperienceRewards()
	{
		return Collections.singletonList(new ExperienceReward(Skill.CRAFTING, 3875));
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return Arrays.asList(
				new UnlockReward("Access to Shilo Village"),
				new UnlockReward("Ability to mine gem rocks in Shilo Village"));
	}


	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		allSteps.add(new PanelDetails("Exploring",
			Arrays.asList(talkToMosol, useBeltOnTrufitus, useSpadeOnGround, useTorchOnFissure, useRopeOnFissure,
				searchFissure, useChiselOnStone, enterDeeperCave, searchForTatteredScroll, searchForCrumpledScroll,
				searchForCorpse, buryCorpse, readTattered, readCrumpled), spade, torchOrCandle, rope, chisel));

		allSteps.add(new PanelDetails("Free Raiysha",
			Arrays.asList(searchRocksOnCairn, searchDolmen, useChiselOnPommel, useWireOnBeads, searchPalms,
				searchDoors, makeKey, useKeyOnDoor, enterDoor, useBonesOnDoor, searchDolmenForFight, killNazastarool,
				pickupCorpse, enterCairnAgain, useCorpseOnDolmen), List.of(chisel, bronzeWire, bones3),
			List.of(combatGear, food, crumbleUndead, prayerPotions)));

		return allSteps;
	}
}
