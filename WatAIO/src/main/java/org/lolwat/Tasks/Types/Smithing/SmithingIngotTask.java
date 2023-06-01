package org.lolwat.Tasks.Types.Smithing;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.widget.helpers.Smithing;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.Enums.IngotType;
import org.lolwat.Tasks.Types.Misc.BankingTask;
import org.lolwat.Tasks.Types.Misc.TraversalTask;
import org.lolwat.Tasks.WatTask;
import org.lolwat.Utils.ItemUtils;
import org.lolwat.Utils.WidgetUtils;
import org.lolwat.WatAIO;

import java.util.*;

public class SmithingIngotTask implements WatTask {
    private List<Tile> furnaceLocations = Arrays.asList(new Tile(3109, 3499));
    private final HashMap<Skill, Integer> levelRequirements = new HashMap<>();
    private Tile selectedLocation;
    private IngotType smithingType;
    private int avoidAtLevel;
    private HashMap<String, Integer> toSell;

    public SmithingIngotTask(IngotType type, int smithingLevel, int pAvoidAtLevel, HashMap<String, Integer> sellList) {
        setRequirements(new HashMap<Skill, Integer>() {{
            put(Skill.SMITHING, smithingLevel);
        }}, new ArrayList<>());

        selectedLocation = furnaceLocations.get(new Random().nextInt(furnaceLocations.size()));
        smithingType = type;
        avoidAtLevel = pAvoidAtLevel;
        toSell = sellList;
    }

    @Override
    public void execute(WatAIO instance) {
        for(java.util.Map.Entry<String, Integer> m : ItemUtils.getMaterialsForBar(smithingType, false).entrySet()) {
            // do we have enough to create at least 1 bar of this type?
            if(!Inventory.contains(m.getKey()) || Inventory.get(m.getKey()).getAmount() < m.getValue()) {
                instance.currentTask = new BankingTask("Grabbing ore", ItemUtils.getMaterialsForBar(smithingType, true), true, this, false, toSell);
                return;
            }
        }

        if(!Map.isTileOnScreen(selectedLocation)) {
            if(Map.isTileOnMap(selectedLocation) && Map.exactDistance(selectedLocation) <= 6) {
                Camera.rotateToTile(selectedLocation);
                return;
            }

            instance.currentTask = new TraversalTask(selectedLocation, true, this);
            return;
        }

        if(GameObjects.closest("Furnace") != null && !Players.getLocal().isAnimating()) {
            if(GameObjects.closest("Furnace").interact()) {
                Sleep.sleepUntil(() -> Widgets.getWidget(270) != null && Widgets.getWidget(270).isVisible(), 10000);

                if(Widgets.getWidget(270) != null && Widgets.getWidget(270).isVisible()) {
                    Widgets.getWidget(270).getChild(WidgetUtils.getIngotWidgetId(smithingType)).interact();
                }
            }
        }
    }

    public void setRequirements(HashMap<Skill, Integer> skills, List<Quest> quests) {
        levelRequirements.putAll(skills);
    }

    @Override
    public String getName() {
        return "Smithing";
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
        return 2000;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

    }

    @Override
    public Skill trainsSkill() {
        return Skill.SMITHING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return avoidAtLevel;
    }
}
