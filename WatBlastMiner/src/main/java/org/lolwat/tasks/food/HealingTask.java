package org.lolwat.tasks.food;

import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.tasks.misc.BankingTask;

import java.util.HashMap;

public class HealingTask implements WatTask {
    private final WatTask post;

    public HealingTask(WatTask post) {
        this.post = post;
    }

    @Override
    public String getName() {
        return "Wining";
    }

    @Override
    public void execute() {
        if(Combat.getHealthPercent() == 100) {
            TaskManager.getInstance().setCurrentTask(post);
            return;
        }

        if(!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Jug of wine"))) {
            TaskManager.getInstance().setCurrentTask(
                    new BankingTask(null, null, 30, this, null)
            );

            return;
        }

        Item i = Inventory.get(x -> x != null && !x.isNoted() && x.getName().equals("Jug of wine"));
        if(i != null) {
            int currentHealth = Combat.getHealthPercent();
            if(!i.interact("Drink")) {
                Logger.log("Error drinking wine");
                return;
            }

            Sleep.sleepUntil(() -> Combat.getHealthPercent() > currentHealth || Combat.getHealthPercent() == 100, 5000);
        } else {
            Logger.log("Error finding wine");
        }
    }

    @Override
    public boolean canPerformTask() {
        return false;
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public Skill trainsSkill() {
        return null;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 0;
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        HashMap<String, Integer> ret = new HashMap<>();
        ret.put("Jug of wine", -28);
        return ret;
    }
}
