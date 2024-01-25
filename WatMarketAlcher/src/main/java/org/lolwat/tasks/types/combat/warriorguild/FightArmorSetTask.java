package org.lolwat.tasks.types.combat.warriorguild;

import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.WatAIO;
import org.lolwat.misc.utils.combat.melee.MeleeUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.misc.BankingTask;
import org.lolwat.tasks.types.misc.TraversalTask;

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
    private int tokens;
    private String latest;

    public FightArmorSetTask(Skill skillType, HashMap<String, Integer> inventory, int tokenCount, String latestObtained) {
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

        if(!latestObtained.isEmpty()) {
            inventoryReq.put(latestObtained, 1);
        }

        tokens = tokenCount;
        latest = latestObtained;
    }

    @Override
    public String getName() {
        return "Obtaining warrior guild tokens";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute(WatAIO instance) {
        if(Bank.isOpen()) {
            if(!Inventory.contains("Warrior guild token")) {
                Bank.withdrawAll("Warrior guild token");
                Sleep.sleepUntil(() -> Inventory.contains("Warrior guild token"), 10000);
            }
        }

        List<String> toRemove = new ArrayList<>();

        for (java.util.Map.Entry<String, Integer> item : inventoryReq.entrySet()) {
            if (!Equipment.contains(item.getKey())) {
                if (Inventory.contains(item.getKey())) {
                    if (Inventory.get(item.getKey()).isNoted()) {
                        continue;
                    }

                    toRemove.add(item.getKey());
                }
            } else {
                toRemove.add(item.getKey());
            }
        }

        for (String s : toRemove) {
            inventoryReq.remove(s);
        }

        if (!inventoryReq.isEmpty()) {
            Logger.log("We need to grab the rest of our melee equipment..");
            for (String s : inventoryReq.keySet()) {
                Logger.log("- " + s);
            }

            instance.currentTask = new BankingTask(inventoryReq, null, 1, this);
            return;
        }

        if (!inventoryReq.isEmpty()) {
            for (Map.Entry<String, Integer> f : inventoryReq.entrySet()) {
                if (!Inventory.contains(f.getKey())) {
                    instance.currentTask = new BankingTask(inventoryReq, null, 1, this);
                    return;
                }
            }
        }

        if (needsEat && Combat.getHealthPercent() <= 50) {
            Item i = Inventory.get(x -> x != null && x.hasAction("Eat"));
            if (i != null && i.interact()) {
                Sleep.sleep(60, 120);
            }
        }

        if (Inventory.count("Warrior guild token") >= 200) {
            instance.currentTask = new FightCyclopsTask(trainingSkill, new HashMap<String, Integer>() {
                {
                    put("Lobster", 20);
                }
            }, new HashMap<String, Integer>() {
                {
                    put("Warrior guild token", -(Inventory.count("Warrior guild token") + Bank.count("Warrior guild token")));
                    put(latest, 1);
                }
            }, latest);
            return;
        }

        if (!fightingArea.contains(Players.getLocal())) {
            instance.currentTask = new TraversalTask(fightingArea, this);
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

        if (needsCheck)
            needsCheck = false;

        for (GroundItem i : GroundItems.all()) {
            if (i.getName().contains("Black")) {
                if (!i.interact("Take")) {
                    Logger.error("Error picking up armor set...");
                }
            }

            if (i.getName().contains("token")) {
                int count = i.getAmount();
                if (!i.interact("Take")) {
                    Logger.error("Error picking up token...");
                } else {
                    tokens += count;
                }
            }
        }

        if (!Players.getLocal().isInCombat() && !Dialogues.inDialogue()) {
            if (Inventory.contains("Black full helm") && Inventory.contains("Black platebody") && Inventory.contains("Black platelegs")) {
                GameObject animator = GameObjects.closest("Magical Animator");
                if (animator != null) {
                    if (animator.interact("Animate")) {
                        Sleep.sleepUntil(() -> !Dialogues.inDialogue(), 5000);
                    }
                }
            } else {
                if(GroundItems.closest(n -> n.getName().contains("Black")) == null) {
                    needsCheck = true;
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
    public Quest completesQuest() {
        return null;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return MeleeUtils.getRequiredItems(true);
    }
}
