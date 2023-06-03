package org.lolwat.tasks.types.firemaking;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.lolwat.tasks.types.misc.BankingTask;
import org.lolwat.utils.ItemUtils;
import org.lolwat.tasks.types.misc.TraversalTask;
import org.lolwat.tasks.WatTask;
import org.lolwat.utils.WidgetUtils;
import org.lolwat.WatAIO;
import org.lolwat.utils.types.TreeType;

import java.awt.*;
import java.util.*;
import java.util.List;

public class FiremakingTask implements WatTask {
    private Area selectedLocation;
    private final TreeType logType;
    private final int minLevel;
    private final int avoidAtLevel;
    private final HashMap<String, Integer> toSell;
    private final int inventoryLoads;
    private List<Area> areas = Arrays.asList(new Area(3225, 3430, 3265, 3428),
            new Area(3171, 3430, 3208, 3428),
            new Area(3171, 3430, 3208, 3428),
            new Area(3155, 3504, 3174, 3501),
            new Area(3171, 3430, 3208, 3428));

    public FiremakingTask(TreeType type, int firemakingLevel, int pAvoidAtLevel, int totalInventories, HashMap<String, Integer> sellList) {
        minLevel = firemakingLevel;
        selectedLocation = areas.get(new Random().nextInt(areas.size()));
        logType = type;
        avoidAtLevel = pAvoidAtLevel;
        toSell = sellList;
        inventoryLoads = Calculations.random(5, totalInventories);
    }

    @Override
    public void execute(WatAIO instance) {
        if(!Tabs.isOpen(Tab.INVENTORY)) {
            Tabs.open(Tab.INVENTORY);
        }

        for(java.util.Map.Entry<String, Integer> m : ItemUtils.getMaterialsForFiremaking(logType, false, 1).entrySet()) {
            if(!Inventory.contains(m.getKey()) || Inventory.get(m.getKey()).getAmount() < m.getValue()) {
                instance.currentTask = new BankingTask("Grabbing materials",
                        ItemUtils.getMaterialsForFiremaking(logType, true, 1),
                        true, this, true, toSell, inventoryLoads);

                return;
            }
        }

        if(!selectedLocation.contains(Players.getLocal())) {
            instance.currentTask = new TraversalTask(selectedLocation.getTile(), false, this);
            return;
        }

        if(getFireOnPlayer()) {
            instance.currentTask = new TraversalTask(selectedLocation.getTile(), false, this);
            return;
        }

        if(Inventory.contains("Tinderbox") && Inventory.get("Tinderbox").useOn(ItemUtils.getLogName(logType)) && !getFireOnPlayer()) {
            Sleep.sleepUntil(() -> Players.getLocal().isAnimating() || getFireOnPlayer() || Dialogues.canContinue(), 4500);
        }
    }

    public static boolean getFireOnPlayer() {
        GameObject fire = GameObjects.closest("Fire");
        return fire != null && fire.getTile().equals(Players.getLocal().getTile());
    }

    @Override
    public String getName() {
        return "Firemaking logs: " + logType.toString().toLowerCase();
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.FIREMAKING) >= minLevel;
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
        return Skill.FIREMAKING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return avoidAtLevel;
    }
}
