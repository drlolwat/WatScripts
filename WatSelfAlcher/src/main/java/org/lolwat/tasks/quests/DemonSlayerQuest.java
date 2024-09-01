package org.lolwat.tasks.quests;

import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.QuestTask;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.combat.melee.MeleeUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DemonSlayerQuest implements QuestTask {
    private final Area prysinArea = new Area(
            new Tile(3200, 3473, 0),
            new Tile(3200, 3471, 0),
            new Tile(3202, 3469, 0),
            new Tile(3204, 3469, 0),
            new Tile(3206, 3471, 0),
            new Tile(3206, 3474, 0),
            new Tile(3204, 3475, 0),
            new Tile(3201, 3475, 0));

    @Override
    public Quest completes() {
        return FreeQuest.DEMON_SLAYER;
    }

    @Override
    public void execute(WatTask wrapper) {
        switch(getState()) {
            case 0: {
                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(Arrays.asList("Yes.", "So how did Wally kill Delrith?",
                            "What is the magical incantation?",
                            "Where can I find Silverlight?",
                            "Okay, thanks. I'll do my best to stop the demon.",
                            "How am I meant to fight a demon who can destroy cities?",
                            "Wally doesn't sound like a very heroic name."));
                }

                Area start = new Area(
                        new Tile(3201, 3425, 0),
                        new Tile(3201, 3423, 0),
                        new Tile(3202, 3422, 0),
                        new Tile(3204, 3422, 0),
                        new Tile(3206, 3423, 0),
                        new Tile(3205, 3426, 0),
                        new Tile(3204, 3427, 0),
                        new Tile(3201, 3426, 0));

                NPC aris = NPCs.closest(x -> x != null && x.exists() && x.getName().equalsIgnoreCase("aris")
                        && x.canReach());

                if(!Dialogues.inDialogue() && !Inventory.contains("Coins")) {
                    TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() { {
                        put("Coins", 1);
                    } }, null, 1, wrapper));
                    return;
                }

                if (!Dialogues.inDialogue() && (aris == null || !start.contains(Players.getLocal()))) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(start, wrapper));
                    return;
                }

                if(aris != null) {
                    if(!Dialogues.inDialogue()) {
                        if (!aris.interact("Talk-to")) {
                            Logger.log("Failed to interact with Aris");
                            return;
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }

                break;
            }

            case 1: {
                if (!prysinArea.contains(Players.getLocal())) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(prysinArea, wrapper));
                    return;
                }

                NPC sirPrysin = NPCs.closest(x -> x != null && x.exists() && x.getName().equalsIgnoreCase("sir prysin")
                        && x.canReach());

                if (sirPrysin != null) {
                    if (!Dialogues.inDialogue()) {
                        if (!sirPrysin.interact("Talk-to")) {
                            Logger.log("Failed to interact with Sir Prysin");
                            return;
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }

                if (Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(Arrays.asList("Aris said I should come and talk to you.",
                            "I need to find Silverlight.",
                            "He's back and unfortunately I've got to deal with him.",
                            "So give me the keys!",
                            "Can you give me your key?"));
                }

                break;
            }

            case 2: {
                if(!Inventory.contains("Silverlight") && !Equipment.contains("Silverlight")) {
                    // mage dudes key
                    if (!Inventory.contains(2399)) {
                        Logger.log("Need to get the mage dudes key");

                        Area traibornArea = new Area(
                                new Tile(3110, 3165, 1),
                                new Tile(3110, 3160, 1),
                                new Tile(3114, 3160, 1),
                                new Tile(3114, 3163, 1),
                                new Tile(3111, 3165, 1));

                        if (!Inventory.contains(x -> x.getName().equalsIgnoreCase("bones") && !x.isNoted()) || Inventory.count("Bones") < 25) {
                            TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                                {
                                    put("Bones", 25);
                                }
                            }, null, 1, wrapper));
                            return;
                        }

                        if (!Dialogues.inDialogue() && !traibornArea.contains(Players.getLocal())) {
                            TaskManager.getInstance().setCurrentTask(new TraversalTask(traibornArea, wrapper));
                            return;
                        }

                        if (Dialogues.inDialogue()) {
                            DialogueUtils.wipeOptions();
                            DialogueUtils.continueWhilePossible();
                            DialogueUtils.solve(Arrays.asList("Talk about Demon Slayer.",
                                    "He told me you were looking after it for him.",
                                    "Well, have you got any keys knocking around?",
                                    "I'll get the bones for you."));
                        }

                        if (!Dialogues.inDialogue()) {
                            NPC traiborn = NPCs.closest(x -> x != null && x.exists() && x.getName().equalsIgnoreCase("wizard traiborn")
                                    && x.canReach());

                            if (traiborn != null) {
                                if (!traiborn.interact("Talk-to")) {
                                    Logger.log("Failed to interact with Traiborn");
                                    return;
                                }

                                Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                                return;
                            }
                        }

                        return;
                    }

                    // sir prysins key
                    if (!Inventory.contains(2401)) {
                        Logger.log("Need to get Sir Prysin's key");
                        int drainStatus = PlayerSettings.getBitValue(2568); // 0=not touched, 1=key in sewer, 2=key taken
                        if (drainStatus == 0) {
                            if (!Inventory.contains("Bucket of water")) {
                                TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                                    {
                                        put("Bucket of water", 1);
                                    }
                                }, null, 1, wrapper));
                                return;
                            }

                            Area drainArea = new Area(3225, 3497, 3227, 3491);
                            if (!drainArea.contains(Players.getLocal())) {
                                TaskManager.getInstance().setCurrentTask(new TraversalTask(drainArea, wrapper));
                                return;
                            }

                            GameObject object = GameObjects.closest(x -> x != null && x.exists()
                                    && x.getName().equalsIgnoreCase("drain"));

                            if (object != null) {
                                if (!Inventory.use("Bucket of water") || !object.interact()) {
                                    Logger.log("Failed to interact with drain");
                                    return;
                                }

                                Sleep.sleepUntil(() -> PlayerSettings.getBitValue(2568) == 1, 5000);
                            }
                        } else {
                            Area skeletons = new Area(
                                    new Tile(3222, 9897, 0),
                                    new Tile(3223, 9896, 0),
                                    new Tile(3227, 9896, 0),
                                    new Tile(3228, 9897, 0),
                                    new Tile(3226, 9900, 0));

                            if (!skeletons.contains(Players.getLocal())) {
                                TaskManager.getInstance().setCurrentTask(new TraversalTask(skeletons, wrapper));
                                return;
                            }

                            GameObject key = GameObjects.closest(x -> x != null && x.exists() && x.getName().equalsIgnoreCase("Rusty key"));
                            if (key != null) {
                                if (!key.interact("Take")) {
                                    Logger.log("Failed to take key");
                                    return;
                                }

                                Sleep.sleepUntil(() -> Inventory.contains("Rusty key"), 5000);
                            }
                        }

                        return;
                    }

                    if (!Inventory.contains(2400)) { // rovins key
                        Logger.log("Need to get Rovin's key");

                        Area rovinArea = new Area(
                                new Tile(3200, 3498, 2),
                                new Tile(3200, 3496, 2),
                                new Tile(3202, 3494, 2),
                                new Tile(3204, 3494, 2),
                                new Tile(3206, 3496, 2),
                                new Tile(3206, 3498, 2),
                                new Tile(3204, 3500, 2),
                                new Tile(3202, 3500, 2));

                        if (!Dialogues.inDialogue() && !rovinArea.contains(Players.getLocal())) {
                            TaskManager.getInstance().setCurrentTask(new TraversalTask(rovinArea, wrapper));
                            return;
                        }

                        if (Dialogues.inDialogue()) {
                            DialogueUtils.continueWhilePossible();
                            DialogueUtils.solve(Arrays.asList("Can you give me your key?",
                                    "Yes I know, but this is important.",
                                    "There's a demon who wants to invade this city.",
                                    "Yes, very.",
                                    "It's not them who are going to fight the demon, it's me.",
                                    "Sir Prysin said you would give me the key.",
                                    "Fortune-teller Aris said I was destined to kill the demon.",
                                    "Otherwise the demon will destroy the city",
                                    "Sir Prysin said you would give me the key",
                                    "Why did he give you one of the keys then?"));
                        }

                        NPC rovin = NPCs.closest(x -> x != null && x.exists() && x.getName().equalsIgnoreCase("captain rovin")
                                && x.canReach());

                        if (rovin != null) {
                            if (!Dialogues.inDialogue()) {
                                if (!rovin.interact("Talk-to")) {
                                    Logger.log("Failed to interact with Captain Rovin");
                                    return;
                                }

                                Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                                return;
                            }
                        }

                        return;
                    }

                    Logger.log("Need to get Silverlight");

                    if(!prysinArea.contains(Players.getLocal())) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(prysinArea, wrapper));
                        return;
                    }

                    NPC sirPrysin = NPCs.closest(x -> x != null && x.exists() && x.getName().equalsIgnoreCase("sir prysin")
                            && x.canReach());

                    if (sirPrysin != null) {
                        if (!Dialogues.inDialogue()) {
                            if (!sirPrysin.interact("Talk-to")) {
                                Logger.log("Failed to interact with Sir Prysin");
                                return;
                            }

                            Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                        }
                    }

                    if (Dialogues.inDialogue()) {
                        DialogueUtils.continueWhilePossible();
                    }

                } else {
                    if(!Equipment.contains("Silverlight")) {
                        Inventory.interact("Silverlight", "Wield");
                        Sleep.sleepUntil(() -> Equipment.contains("Silverlight"), 5000);
                        return;
                    }

                    if(Dialogues.inDialogue()) {
                        DialogueUtils.continueWhilePossible();
                        DialogueUtils.solve(getFullIncantation());
                        return;
                    }

                    if(Inventory.isEmpty() || !Inventory.contains(x -> x != null && x.hasAction("Eat"))) {
                        Logger.error("MeleeCombatTask(Q) is missing food");
                        TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                            {
                                put("Lobster", 12);
                            }
                        }, null, 1, wrapper));
                        return;
                    }

                    if(Combat.getHealthPercent() <= 50) {
                        Item i = Inventory.get(x -> x != null && x.hasAction("Eat"));
                        if (i != null && i.interact()) {
                            Sleep.sleep(60, 120);
                        }
                    }

                    if(Players.getLocal().isInCombat()) {
                        return;
                    }

                    if (!Combat.isAutoRetaliateOn()) {
                        if (!Tabs.isOpen(Tab.COMBAT)) {
                            Tabs.open(Tab.COMBAT);
                            Sleep.sleep(120, 240);
                        }

                        Combat.toggleAutoRetaliate(true);
                        Sleep.sleep(60, 120);
                    }

                    if (!Tabs.isOpen(Tab.INVENTORY)) {
                        Tabs.open(Tab.INVENTORY);
                        Sleep.sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), 5000);
                    }

                    Area circle = new Area(
                            new Tile(3233, 3373, 0),
                            new Tile(3229, 3373, 0),
                            new Tile(3229, 3366, 0),
                            new Tile(3233, 3366, 0));

                    NPC delrith = NPCs.closest(x -> x != null && x.exists() && x.getName().equalsIgnoreCase("delrith"));

                    if(!Dialogues.inDialogue() && delrith == null && !circle.contains(Players.getLocal())) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(circle, wrapper));
                        return;
                    }

                    if(delrith != null) {
                        if(!Players.getLocal().isInCombat()) {
                            if(!delrith.interact("Attack")) {
                                Logger.log("Failed to attack Delrith");
                                return;
                            }

                            Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 3000);
                        }
                    }

                    Logger.log("My incantation: " + getFullIncantation());
                    Sleep.sleepUntil(() -> !Players.getLocal().isInCombat() && Dialogues.inDialogue(), 10000);
                }

                break;
            }

            default: {
                Logger.log("Unhandled state: " + getState());
                break;
            }
        }
    }

    @Override
    public boolean canPerformTask() {
        return !Quests.isFinished(completes())
                && Skills.getRealLevel(Skill.ATTACK) >= 10
                && Skills.getRealLevel(Skill.DEFENCE) >= 10
                && Skills.getRealLevel(Skill.STRENGTH) >= 10
                && Combat.getCombatLevel() >= 30;
    }

    @Override
    public int getState() {
        return PlayerSettings.getBitValue(FreeQuest.DEMON_SLAYER.getVarBitID()); //2561
    }

    @Override
    public List<String> inventoryTolerated() {
        return Arrays.asList("Bones", "Silverlight key", "Silverlight", "Rusty key");
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return MeleeUtils.getRequiredItems(false, getState() == 2 && PlayerSettings.getBitValue(2568) == 2);
    }

    private List<String> getFullIncantation() {
        return Arrays.asList(
                getIncantationWord(PlayerSettings.getBitValue(2562)),
                getIncantationWord(PlayerSettings.getBitValue(2563)),
                getIncantationWord(PlayerSettings.getBitValue(2564)),
                getIncantationWord(PlayerSettings.getBitValue(2565)),
                getIncantationWord(PlayerSettings.getBitValue(2566))
        );
    }

    private String getIncantationWord(int n) {
        switch(n) {
            case 0: {
                return "Carlem";
            }

            case 1: {
                return "Aber";
            }

            case 2: {
                return "Camerinthum";
            }

            case 3: {
                return "Purchai";
            }

            case 4: {
                return "Gabindo";
            }

            default: {
                return "Unknown";
            }
        }
    }
}
