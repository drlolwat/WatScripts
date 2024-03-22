package org.lolwat.tasks.types.prayer;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.WatAIO;
import org.lolwat.misc.types.prayer.BoneType;
import org.lolwat.misc.utils.prayer.PrayerUtils;
import org.lolwat.tasks.types.misc.BankingTask;

import java.util.HashMap;

public class BuryBonesTask implements WatTask {
    private final BoneType buryingType;
    private final int inventoryLoads;
    private boolean ready;
    private int stopAt;

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
    public void execute(WatAIO instance) {
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
    public int loopTime() {
        return 650;
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
    public Quest completesQuest() {
        return null;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return GenericUtils.getSkillingGear();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}
