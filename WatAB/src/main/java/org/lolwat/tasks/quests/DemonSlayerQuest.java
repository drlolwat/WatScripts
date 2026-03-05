package org.lolwat.tasks.quests;

import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
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
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.ItemManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.WatUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.QuickWithdrawTask;
import org.lolwat.tasks.misc.WalkingTask;
import org.lolwat.tasks.quests.helpers.GetSilverlight;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.interfaces.QuestTask;
import org.lolwat.types.interfaces.WatTask;

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
        switch (getState()) {
            case 0: {
                if (Dialogues.inDialogue()) {
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

                if (!Dialogues.inDialogue() && !Inventory.contains("Coins")) {
                    TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<WatItem, Integer>() {
                        {
                            put(ItemManager.getInstance().getItem("Coins"), 1);
                        }
                    }, null, 1, wrapper));
                    return;
                }

                if (!Dialogues.inDialogue() && (aris == null || !start.contains(Players.getLocal()))) {
                    TaskManager.getInstance().setCurrentTask(new WalkingTask(start, wrapper));
                    return;
                }

                if (aris != null) {
                    if (!Dialogues.inDialogue()) {
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
                    TaskManager.getInstance().setCurrentTask(new WalkingTask(prysinArea, wrapper));
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
                if (!WatUtils.inventoryContains(2402, 1, false) && !WatUtils.equipmentContains(2402, 1)) {
                    TaskManager.getInstance().setCurrentTask(new QuickWithdrawTask(2402, 1,
                            new GetSilverlight(wrapper), wrapper));
                    return;
                } else {
                    if (!Equipment.contains("Silverlight")) {
                        Inventory.interact("Silverlight", "Wield");
                        Sleep.sleepUntil(() -> Equipment.contains("Silverlight"), 5000);
                        return;
                    }

                    if (Dialogues.inDialogue()) {
                        DialogueUtils.continueWhilePossible();
                        DialogueUtils.solve(getFullIncantation());
                        return;
                    }

                    if (Inventory.isEmpty() || !Inventory.contains(x -> x != null && x.hasAction("Eat"))) {
                        Logger.error("MeleeCombatTask(Q) is missing food");
                        TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<WatItem, Integer>() {
                            {
                                put(ItemManager.getInstance().getItem("Lobster"), 12);
                            }
                        }, null, 1, wrapper));
                        return;
                    }

                    if (Combat.getHealthPercent() <= 50) {
                        Item i = Inventory.get(x -> x != null && x.hasAction("Eat"));
                        if (i != null && i.interact()) {
                            Sleep.sleep(60, 120);
                        }
                    }

                    if (Players.getLocal().isInCombat()) {
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

                    if (!Dialogues.inDialogue() && delrith == null && !circle.contains(Players.getLocal())) {
                        TaskManager.getInstance().setCurrentTask(new WalkingTask(circle, wrapper));
                        return;
                    }

                    if (delrith != null) {
                        if (!Players.getLocal().isInCombat()) {
                            if (!delrith.interact("Attack")) {
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
    public HashMap<WatItem, Integer> clothesRequired() {
        return null; //TODO
        //return MeleeUtils.getRequiredItems(false, getState() == 2 && PlayerSettings.getBitValue(2568) == 2);
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
