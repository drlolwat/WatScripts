package org.lolwat.tasks.types.crafting;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.misc.types.crafting.CraftingType;
import org.lolwat.misc.utils.StringUtils;
import org.lolwat.misc.utils.crafting.CraftingUtils;
import org.lolwat.tasks.types.misc.BankingTask;
import org.lolwat.tasks.types.misc.TraversalTask;
import org.lolwat.tasks.WatTask;
import org.lolwat.WatAIO;

import java.util.*;

public class JewelryTask implements WatTask {
    private final List<Tile> furnaceLocations = Collections.singletonList(new Tile(3107, 3499));
    private final HashMap<Skill, Integer> levelRequirements = new HashMap<>();
    private final Tile selectedLocation;
    private final CraftingType craftingType;
    private final int avoidAtLevel;
    private final HashMap<String, Integer> toSell;
    private final int totalLoads;

    public JewelryTask(CraftingType type, int craftingLevel, int pAvoidAtLevel, HashMap<String, Integer> sellList) {
        setRequirements(new HashMap<Skill, Integer>() {{
            put(Skill.CRAFTING, craftingLevel);
        }}, new ArrayList<>());

        selectedLocation = furnaceLocations.get(new Random().nextInt(furnaceLocations.size()));
        craftingType = type;
        avoidAtLevel = pAvoidAtLevel;
        toSell = sellList;
        totalLoads = Calculations.random(10, 15);
    }

    @Override
    public void execute(WatAIO instance) {
        for(java.util.Map.Entry<String, Integer> m : CraftingUtils.getMaterialsForJewelry(craftingType, false, 1).entrySet()) {
            if(!Inventory.contains(m.getKey()) || Inventory.get(m.getKey()).getAmount() < m.getValue()) {
                instance.currentTask = new BankingTask("Grabbing materials",
                        CraftingUtils.getMaterialsForJewelry(craftingType, true, 1),
                        true, this, true, toSell, totalLoads);

                return;
            }
        }

        String extra = StringUtils.capitalize(craftingType.toString().toLowerCase()) + " mould";
        if(!Inventory.contains(extra)) {
            instance.currentTask = new BankingTask("Grabbing mould", new HashMap<String, Integer>() { { put(extra, 1); }}, false, this, true, null, 1);
            return;
        }

        if (!Map.isTileOnScreen(selectedLocation)) {
            if (Map.isTileOnMap(selectedLocation) && Map.exactDistance(selectedLocation) <= 6) {
                Camera.rotateToTile(selectedLocation);
                return;
            }

            instance.currentTask = new TraversalTask(selectedLocation, false, this);
            return;
        }

        if (GameObjects.closest("Furnace") != null && !Players.getLocal().isAnimating()) {
            if (GameObjects.closest("Furnace").interact()) {
                Sleep.sleepUntil(() -> Widgets.getWidget(CraftingUtils.getJewelryParentId(craftingType)) != null && Widgets.getWidget(CraftingUtils.getJewelryParentId(craftingType)).isVisible(), 10000);
                if(Widgets.getWidget(CraftingUtils.getJewelryParentId(craftingType)).getChild(CraftingUtils.getJewelryChildId(craftingType)).interact()) {
                    Sleep.sleep(500, 900);
                    Mouse.moveOutsideScreen();
                    Sleep.sleepUntil(() -> !Inventory.contains("Gold bar") || Dialogues.canContinue(), 45000);
                }
            }
        }
    }

    public void setRequirements(HashMap<Skill, Integer> skills, List<Quest> quests) {
        levelRequirements.putAll(skills);
    }

    @Override
    public String getName() {
        return "Making jewelry";
    }

    @Override
    public boolean canPerformTask() {
        for (java.util.Map.Entry<Skill, Integer> map : levelRequirements.entrySet()) {
            if (map.getValue() > Skills.getRealLevel(map.getKey())) {
                return false;
            }
        }

        return true;
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
        return Skill.CRAFTING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return avoidAtLevel;
    }

    @Override
    public Quest completesQuest() {
        return null;
    }
}
