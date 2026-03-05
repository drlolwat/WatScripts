package org.lolwat.tasks.misc;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.Shop;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.OutfitUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BuyGracefulOutfitTask implements WatTask {
    private final Area grace = new Area(
            new Tile(3046, 4961, 1),
            new Tile(3050, 4961, 1),
            new Tile(3053, 4965, 1),
            new Tile(3047, 4967, 1));

    private final WatTask post;
    private final List<String> toBuy;

    public BuyGracefulOutfitTask(WatTask post, List<String> toBuy) {
        this.post = post;
        this.toBuy = toBuy;
    }

    @Override
    public String getName() {
        return "Buying Graceful Outfit";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute() {
        if(!Inventory.contains("Mark of grace")) {
            TaskManager.getInstance().setCurrentTask(post);
            return;
        }

        if(!grace.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(grace, this));
            return;
        }

        NPC grace = NPCs.closest("Grace");
        if(grace != null) {
            if(grace.interact("Trade")) {
                Sleep.sleepUntil(() -> Shop.isOpen(), 5000);
            }

            if(!Shop.isOpen())
                return;

            for(String i : toBuy) {
                if(Inventory.contains(i))
                    continue;

                if(!Shop.contains(i)) {
                    Shop.close();
                    Logger.error("Shop did not contain " + i);
                    return;
                }

                if(!Shop.purchase(i, 1)) {
                    Logger.error("Failed to purchase " + i);
                    return;
                }

                Sleep.sleepUntil(() -> Inventory.contains(i), 5000);
            }

            if(!Shop.close()) {
                Logger.error("Failed to close shop");
            }
        }

        for(String i : toBuy) {
            if(!Inventory.contains(i)) {
                Logger.error("Inventory did not contain " + i + " after shopping");
                return;
            }
        }

        TaskManager.getInstance().setCurrentTask(post);
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return 400;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

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
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }

    @Override
    public List<String> inventoryTolerated() {
        return new ArrayList<String>() { {
            addAll(OutfitUtils.gracefulItems.keySet());
            add("Monkfish");
            add("Mark of grace");
        } };
    }
}
