package org.lolwat.tasks.exchange;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.lolwat.managers.TaskManager;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.interfaces.WatTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuyMultipleItemsTask implements WatTask {
    private final HashMap<WatItem, Integer> items;
    private final WatTask parent;
    private final List<WatItem> processed;

    public BuyMultipleItemsTask(HashMap<WatItem, Integer> items, WatTask parent) {
        this.items = items;
        this.parent = parent;
        this.processed = new ArrayList<>();
    }

    @Override
    public void execute() {
        for (Map.Entry<WatItem, Integer> i : items.entrySet()) {
            if (!processed.contains(i.getKey())) {
                Logger.log("MultipleItemsTask handing off to SingleItemTask");
                processed.add(i.getKey());
                TaskManager.getInstance().setCurrentTask(new BuySingleItemTask(
                        i.getKey().getSearchFor(),
                        i.getValue(),
                        i.getKey().getPrice(),
                        this)
                );
                return;
            }
        }

        if(processed.size() == items.size()) {
            for(Map.Entry<WatItem, Integer> i : items.entrySet()) {
                if(Inventory.count(x -> x.getName().contains(i.getKey().getName())) < i.getValue()) {
                    processed.remove(i.getKey());
                    return;
                }
            }

            Logger.log("processed all exchange items");
            TaskManager.getInstance().setCurrentTask(parent);
        }
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return parent.trainsSkill();
    }

    @Override
    public Integer avoidAfterLevel() {
        return parent.avoidAfterLevel();
    }
}
