package org.lolwat.tasks.work;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.WatScript;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.tasks.misc.BankingTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DrinkStaminaTask implements WatTask {
    Area sawmill = new Area(1624, 3501, 1626, 3498);
    WatTask post;

    public DrinkStaminaTask(WatTask post) {
        this.post = post;
    }

    @Override
    public String getName() {
        return "Drinking stamina";
    }

    @Override
    public void execute() {
        if(Walking.isStaminaActive() && Walking.getRunEnergy() >= 50) {
            TaskManager.getInstance().setCurrentTask(post);
            return;
        }

        Item i = Inventory.get(x -> x != null && !x.isNoted() && x.getName().contains("Stamina potion("));
        if(i != null) {
            if(Bank.isOpen()) {
                Bank.close();
                return;
            }

            if (!i.interact("Drink")) {
                Logger.error("failed to drink");
                return;
            }

            WatScript.getInstance().setSipsTaken(WatScript.getInstance().getSipsTaken() + 1);
            TaskManager.getInstance().setCurrentTask(post);
        } else {
            Logger.log("going to bank to get stamina");
            TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 3, this, null));
        }
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.WOODCUTTING;
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        HashMap<String, Integer> ret = new HashMap<>();
        ret.put("Stamina potion(", 1);
        return ret;
    }

    @Override
    public List<String> inventoryTolerated() {
        List<String> ret = new ArrayList<>();
        ret.add("Coins");
        return ret;
    }
}
