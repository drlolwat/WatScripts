package org.lolwat.tasks.prayer;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.types.prayer.BoneType;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.prayer.PrayerUtils;
import org.lolwat.tasks.misc.BankingTask;

import java.util.HashMap;

public class BuryBonesTask implements WatTask {
    private final BoneType buryingType;
    private final int inventoryLoads;
    private boolean ready;
    private final int stopAt;

    @Override
    public String getName() {
        return "Burying ";
    }

    public BuryBonesTask(BoneType type, int maxLevel, int maxInventoryLoads) {
        stopAt = maxLevel;
        buryingType = type;
        inventoryLoads = Calculations.random(maxInventoryLoads);
        ready = true;
    }

    @Override
    public void execute() {
        String name = PrayerUtils.getBonesFromType(buryingType);
        if(name != null) {
            if(!Inventory.contains(name)) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() { { put(name, 28); }}, null, inventoryLoads, this));
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
    public void onExpGained(Skill skill, int amount, WatAIO instance) {
        ready = true;
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.PRAYER) <= stopAt;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.PRAYER;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return GenericUtils.getSkillingGear();
    }

}
