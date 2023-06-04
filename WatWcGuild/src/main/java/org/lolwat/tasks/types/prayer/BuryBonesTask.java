package org.lolwat.tasks.types.prayer;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.tasks.WatTask;
import org.lolwat.WatAIO;
import org.lolwat.tasks.types.misc.BankingTask;
import org.lolwat.misc.types.prayer.BoneType;
import org.lolwat.misc.utils.prayer.PrayerUtils;

import java.util.HashMap;

public class BuryBonesTask implements WatTask {
    private final BoneType buryingType;
    private final int inventoryLoads;
    private boolean ready;

    @Override
    public String getName() {
        return "Burying ";
    }

    public BuryBonesTask(BoneType type, int maxInventoryLoads) {
        buryingType = type;
        inventoryLoads = Calculations.random(maxInventoryLoads);
        ready = true;
    }

    @Override
    public void execute(WatAIO instance) {
        String name = PrayerUtils.getBonesFromType(buryingType);
        if(name != null) {
            if(!Inventory.contains(name)) {
                instance.currentTask = new BankingTask("Grabbing bones", new HashMap<String, Integer>() { { put(name, 28); }}, true, this, true, new HashMap<>(), inventoryLoads);
                return;
            }

            Item item = Inventory.get(name);
            if(item != null && item.interact("Bury")) {
                Sleep.sleepUntil(() -> ready || Dialogues.canContinue(), 3000);
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
        ready = true;
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.PRAYER;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }
}
