package org.lolwat.tasks.combat.warriorguild;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.combat.melee.MeleeUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FightArmorSetTask implements WatTask {
    private Skill trainingSkill;
    private boolean needsEat;
    private HashMap<String, Integer> inventoryReq;
    private final Area fightingArea = new Area(2849, 3545, 2858, 3534);
    private boolean needsCheck = true;
    private String latest;

    public FightArmorSetTask(Skill skillType, HashMap<String, Integer> inventory, String latestObtained) {
        trainingSkill = skillType;

        if(inventory != null && !inventory.isEmpty()) {
            needsEat = true;
            inventoryReq = inventory;
        } else {
            needsEat = false;
            inventoryReq = new HashMap<>();
        }

        inventoryReq.put("Black full helm", 1);
        inventoryReq.put("Black platebody", 1);
        inventoryReq.put("Black platelegs", 1);

        latest = latestObtained;
    }

    @Override
    public String getName() {
        return "Fighting armor set";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute() {
        if (!inventoryReq.isEmpty()) {
            for (Map.Entry<String, Integer> f : inventoryReq.entrySet()) {
                if(Dialogues.inDialogue()) {
                    Sleep.sleepUntil(() -> !Dialogues.inDialogue(), 5000);
                    continue;
                }

                if (!Inventory.contains(f.getKey()) && needsCheck) {
                    NPC armorSet = NPCs.closest("Animated Black Armour");
                    GroundItem i = GroundItems.closest(x -> x.getName().contains("Black"));
                    if(f.getKey().contains("Black")) {
                        if (Players.getLocal().isInCombat() || armorSet != null || i != null || Players.getLocal().isHealthBarVisible()) {
                            continue;
                        }
                    }

                    Logger.log("We are missing " + f.getKey() + ", going to get it.");
                    TaskManager.getInstance().setCurrentTask(new BankingTask(inventoryReq, null, 1, this));
                    return;
                }
            }
        }

        if(Bank.isOpen()) {
            if(!Inventory.contains("Warrior guild token")) {
                Bank.withdrawAll("Warrior guild token");
                Sleep.sleepUntil(() -> Inventory.contains("Warrior guild token"), 10000);
            }
        }

        int foodCount = Inventory.count(x -> x != null && x.hasAction("Eat"));
        if(foodCount <= 0) {
            TaskManager.getInstance().setCurrentTask(new BankingTask(inventoryReq, null, 1, this));
            return;
        }

        if (needsEat && Combat.getHealthPercent() <= 50) {
            Item i = Inventory.get(x -> x != null && x.hasAction("Eat"));
            if (i != null && i.interact()) {
                Sleep.sleep(60, 120);
            }
        }

        if (Inventory.count("Warrior guild token") >= 200) {
            TaskManager.getInstance().setCurrentTask(new FightCyclopsTask(trainingSkill, new HashMap<String, Integer>() {
                {
                    put("Lobster", 20);
                }
            }, latest));
            return;
        }

        if (!fightingArea.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(fightingArea, this));
            return;
        }

        if (trainingSkill == Skill.ATTACK && Combat.getCombatStyle() != CombatStyle.ATTACK) {
            Tabs.open(Tab.COMBAT);
            Combat.setCombatStyle(CombatStyle.ATTACK);
        } else if (trainingSkill == Skill.DEFENCE && Combat.getCombatStyle() != CombatStyle.DEFENCE) {
            Tabs.open(Tab.COMBAT);
            Combat.setCombatStyle(CombatStyle.DEFENCE);
        } else if (trainingSkill == Skill.STRENGTH && Combat.getCombatStyle() != CombatStyle.STRENGTH) {
            Tabs.open(Tab.COMBAT);
            Combat.setCombatStyle(CombatStyle.STRENGTH);
        }

        if (!Combat.isAutoRetaliateOn()) {
            if (!Tabs.isOpen(Tab.COMBAT)) {
                Tabs.open(Tab.COMBAT);
                Sleep.sleep(120, 240);
            }

            Combat.toggleAutoRetaliate(true);
            Sleep.sleep(60, 120);
        }

        Sleep.sleep(300, 500);

        if (!Tabs.isOpen(Tab.INVENTORY)) {
            Tabs.open(Tab.INVENTORY);
            Sleep.sleep(300, 500);
        }

        if (Dialogues.canContinue()) {
            Dialogues.continueDialogue();
        }

        for (GroundItem i : GroundItems.all()) {
            if (i.getName().contains("Black")) {
                if (!i.interact("Take")) {
                    Logger.error("Error picking up armor set...");
                    return;
                }
            }

            if (i.getName().contains("token")) {
                if (!i.interact("Take")) {
                    Logger.error("Error picking up token...");
                    return;
                }
            }
        }

        if(Inventory.contains(latest) && !Equipment.contains(latest)) {
            if(!Inventory.interact(latest, "Wield")) {
                Logger.error("Error wielding new defender...");
                return;
            }

            Sleep.sleepUntil(() -> Equipment.contains(latest), Calculations.random(1500, 5000));
        }

        if (!Players.getLocal().isInCombat()) {
            if (Inventory.contains("Black full helm") && Inventory.contains("Black platebody") && Inventory.contains("Black platelegs")) {
                GameObject animator = GameObjects.closest("Magical Animator");
                if (animator != null) {
                    if (animator.interact("Animate")) {
                        needsCheck = false;
                        Sleep.sleepUntil(() -> !Dialogues.inDialogue(), 5000);
                    }
                }
            }
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return 650;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

    }

    @Override
    public Skill trainsSkill() {
        return trainingSkill;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 100;
    }

    

    @Override
    public HashMap<String, Integer> clothesRequired() {
        HashMap<String, Integer> gear = MeleeUtils.getRequiredItems(true);
        if(!latest.isEmpty()) {
            String toRemove = "";
            for(Map.Entry<String, Integer> e : gear.entrySet()) {
                if(e.getKey().contains("kiteshield")) {
                    toRemove = e.getKey();
                }
            }

            if(!toRemove.isEmpty()) {
                gear.remove(toRemove);
                gear.put(latest, 1);
            }
        }

        return gear;
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }

    @Override
    public List<String> inventoryTolerated() {
        return new ArrayList<String>() {
            {
                add("Warrior guild token");
            }
        };
    }

    @Override
    public boolean requiresMembers() {
        return true;
    }
}
