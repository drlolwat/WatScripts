package org.lolwat.tasks.cooking;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.types.mixed.FishType;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.cooking.CookingUtils;
import org.lolwat.managers.types.WatTask;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CookingFishTask implements WatTask {
    private List<Area> areas = Arrays.asList(new Area(3236, 3409, 3240, 3413));
    private FishType fishType;
    private Area usingArea;
    private int inventoryCount;
    private int minLevel;
    private int maxLevel;
    private HashMap<String, Integer> selling;

    public CookingFishTask(FishType type, int startAtLevel, int avoidAtLevel, int maximumInventories, HashMap<String, Integer> toSell) {
        fishType = type;
        usingArea = areas.get(new Random().nextInt(areas.size()));
        inventoryCount = Calculations.random(10, maximumInventories);
        minLevel = startAtLevel;
        maxLevel = avoidAtLevel;
        selling = toSell;
    }

    @Override
    public String getName() {
        return "Cooking " + fishType.toString().toLowerCase();
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.COOKING) >= minLevel && Skills.getRealLevel(Skill.COOKING) <= maxLevel;
    }

    @Override
    public void execute() {
        for (java.util.Map.Entry<String, Integer> m : CookingUtils.getRequiredItems(fishType, false, 1).entrySet()) {
            if (!Inventory.contains(m.getKey()) || Inventory.get(m.getKey()).getAmount() < m.getValue() || Inventory.get(m.getKey()).isNoted()) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(CookingUtils.getRequiredItems(fishType, true, 1),
                        selling, inventoryCount, this));

                return;
            }
        }

        if (!Tabs.isOpen(Tab.INVENTORY)) {
            Tabs.open(Tab.INVENTORY);
        }

        if (!usingArea.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(usingArea, this));
            return;
        }

        if (GameObjects.closest(x -> x != null && x.getName().toLowerCase().contains("range")).interact()) {
            Sleep.sleepUntil(() -> Widgets.getWidget(270) != null && Widgets.getWidget(270).isVisible(), 10000);
            if (Widgets.getWidget(270).getChild(14) != null && Widgets.getWidget(270).getChild(14).interact()) { //TODO null checks on the widgets
                Sleep.sleepUntil(() -> !Inventory.contains("Raw " + fishType.toString().toLowerCase()) || Dialogues.canContinue(), 60000);
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
        return Skill.COOKING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return maxLevel;
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
