package org.lolwat.tasks.prep;

import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.LiquidationTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.*;

public class PreparationTask implements WatTask {
    int currentStep = 0;
    Area shayzienArea = new Area(
            new Tile(1537, 3623, 0),
            new Tile(1540, 3617, 0),
            new Tile(1547, 3617, 0),
            new Tile(1550, 3621, 0),
            new Tile(1549, 3627, 0),
            new Tile(1544, 3629, 0),
            new Tile(1538, 3629, 0));

    List<GroundItem> itemQueue;

    public PreparationTask() {
        itemQueue = new ArrayList<>();
    }

    @Override
    public String getName() {
        return "Preparation (" + currentStep + ")";
    }

    @Override
    public void execute() {
        if(currentStep == 0) {
            for(Map.Entry<String, Integer> map : clothesRequired().entrySet()) {
                if(!ItemUtils.equipmentContains(map.getKey(), map.getValue())) {
                    TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 1, this, null));
                    return;
                }
            }

            for(Map.Entry<String, Integer> map : inventoryRequired().entrySet()) {
                if(!ItemUtils.inventoryContains(map.getKey(), map.getValue(), false)) {
                    TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 1, this, null));
                    return;
                }
            }

            currentStep = 1;
        }
        else {
            if(currentStep > 5) {
                TaskManager.getInstance().setCurrentTask(new LiquidationTask(null, 0));
            } else {
                if (!shayzienArea.contains(Players.getLocal())) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(shayzienArea, this));
                    return;
                }

                Item food = Inventory.get(x -> x != null && x.hasAction("Eat"));
                Item pot = Inventory.get(x -> x != null && !x.isNoted() && x.getName().contains("Super combat"));
                Item antidote = Inventory.get(x -> x != null && !x.isNoted() && x.hasAction("Drink") && x.getName().contains("Antidote++"));

                if (food != null) {
                    if (Combat.getHealthPercent() <= 50) {
                        if (!food.interact("Eat")) {
                            Logger.log("failed to eat food");
                        }
                    }
                }

                if (antidote != null && (Combat.isPoisoned() || Combat.isEnvenomed() || Combat.isDiseased())) {
                    if (!antidote.interact("Drink")) {
                        Logger.log("failed to drink antidote");
                        return;
                    }

                    Sleep.sleepUntil(() -> !Combat.isPoisoned(), 5000);
                }

                if (!Combat.isAutoRetaliateOn()) {
                    if (!Combat.toggleAutoRetaliate(true)) {
                        Logger.log("failed to toggle auto retaliate on");
                        return;
                    }

                    Sleep.sleepUntil(Combat::isAutoRetaliateOn, 1000);
                }

                if (pot != null) {
                    if (Skills.getBoostedLevel(Skill.STRENGTH) <= (Skills.getRealLevel(Skill.STRENGTH) + 4)) {
                        if (!pot.interact("Drink")) {
                            Logger.log("failed to drink combat potion");
                            return;
                        }

                        Sleep.sleepUntil(() -> Skills.getBoostedLevel(Skill.STRENGTH) > Skills.getRealLevel(Skill.STRENGTH), 5000);
                    }
                }

                int piecesOfStep = Inventory.count(x -> x != null && x.getName().contains("Shayzien")
                        && x.getName().contains(String.valueOf(currentStep)));

                Logger.log("current pieces of step(" + currentStep + "): " + piecesOfStep);
                if (piecesOfStep == 5) {
                    if (currentStep == 5) {
                        TaskManager.getInstance().setCurrentTask(new LiquidationTask(null, 0));
                        return;
                    } else {
                        currentStep++;
                    }
                }

                List<Item> oldPieces = Inventory.all(x -> x != null && x.getName().contains("Shayzien")
                        && x.getName().contains(String.valueOf(currentStep - 2)));

                if (!oldPieces.isEmpty()) {
                    for (Item i : oldPieces) {
                        if (i == null) continue;

                        Logger.log("dropping old piece: " + i.getName());
                        if (!Inventory.drop(i.getName())) {
                            Logger.log("failed to drop " + i.getName());
                        }
                    }
                }

                if (Players.getLocal().isInCombat() || Players.getLocal().isHealthBarVisible())
                    return;

                if (!Tabs.isOpen(Tab.INVENTORY)) {
                    Tabs.open(Tab.INVENTORY);
                    Sleep.sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), 5000);
                }

                if (!itemQueue.isEmpty()) {
                    Logger.log("detected drop of item we need");
                    for (GroundItem i : itemQueue) {
                        if (!i.interact("Take")) {
                            Logger.log("failed to pick up " + i.getName());
                        }

                        Sleep.sleepUntil(() -> Inventory.contains(i.getName()) && !i.exists(), 5000);

                        if (!Inventory.contains(i.getName()) || i.exists())
                            return;
                    }

                    itemQueue.clear();
                }

                NPC target = NPCs.closest(x -> x != null && x.getName().equals("Soldier (tier " + currentStep + ")"));
                if (target != null) {
                    if (Dialogues.inDialogue()) {
                        DialogueUtils.continueWhilePossible();
                        DialogueUtils.solve(Arrays.asList("Okay, I reckon I can take you.", "I understand. Let's fight.", "Sure, let's fight."));
                        return;
                    }

                    if (target.hasAction("Talk-to")) {
                        if (!target.interact("Talk-to")) {
                            Logger.log("failed to talk to " + target.getName());
                        }
                    } else if (target.hasAction("Attack") && !target.isInCombat()) {
                        Logger.log("found target: " + target.getName());
                        if (!target.interact("Attack")) {
                            Logger.log("failed to attack target: " + target.getName());
                        }
                    }
                }
            }
        }
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        HashMap<String, Integer> clothes = new HashMap<>();
        clothes.put("Granite hammer", 1);
        clothes.put("Rune full helm", 1);
        clothes.put("Rune chainbody", 1);
        clothes.put("Rune platelegs", 1);
        clothes.put("Rune kiteshield", 1);
        clothes.put("Ring of wealth (", 1);
        return clothes;
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        HashMap<String, Integer> inventory = new HashMap<>();
        inventory.put("Shark", 14);
        inventory.put("Super combat potion(4)", 1);
        inventory.put("Skills necklace(", 1);
        inventory.put("Antidote++(4)", 1);
        return inventory;
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.HITPOINTS;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public boolean requiresMembers() {
        return true;
    }

    @Override
    public void onGroundItemSpawn(GroundItem object) {
        if(object == null || !object.exists()) return;
        if(!shayzienArea.contains(object)) return;
        if(!object.canReach(Players.getLocal().getTile())) return;

        if(object.getName().contains("Shayzien") && object.getName().contains(String.valueOf(currentStep))) {
            itemQueue.add(object);
        }
    }
}
