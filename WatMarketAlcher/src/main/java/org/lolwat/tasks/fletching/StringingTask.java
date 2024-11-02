package org.lolwat.tasks.fletching;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.tasks.misc.BankingTask;

import java.util.HashMap;
import java.util.Map;

public class StringingTask implements WatTask {
    @Override
    public String getName() {
        return "Fletching (Stringing)";
    }

    @Override
    public void execute() {
        for (Map.Entry<String, Integer> entry : inventoryRequired().entrySet()) {
            if (!ItemUtils.inventoryContains(entry.getKey(), 1, false)) {
                Logger.log("Missing " + entry.getValue() + " " + entry.getKey());
                TaskManager.getInstance().setCurrentTask(new BankingTask(null, null,
                        Calculations.random(145, 155), this, null));
                return;
            }
        }

        if(Bank.isOpen()) {
            Bank.close();
            Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);
        }

        if(!Tabs.isOpen(Tab.INVENTORY)) {
            if(!Tabs.open(Tab.INVENTORY)) {
                Logger.log("Failed to open inventory tab");
                return;
            }

            Sleep.sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), 5000);
        }

        if(!Inventory.use("Bow string")) {
            Logger.log("Failed to use Bow string");
            return;
        }

        if(!Inventory.use("Yew longbow (u)")) {
            Logger.log("Failed to use Yew longbow (u)");
            return;
        }

        Sleep.sleepUntil(() -> Widgets.isVisible(270), 5000);

        Widget widget = Widgets.getWidget(270);
        if(widget != null && widget.isVisible()) {
            WidgetChild bow = widget.getChild(14);
            if(bow != null) {
                if(!bow.interact()) {
                    Logger.log("Failed to interact with bow");
                    return;
                }
            }
        }

        Sleep.sleepUntil(() -> !Inventory.contains("Yew longbow (u)") && !Players.getLocal().isAnimating(), 35000);
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
        return Skill.FLETCHING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        HashMap<String, Integer> inventory = new HashMap<>();
        inventory.put("Yew longbow (u)", 14);
        inventory.put("Bow string", 14);
        return inventory;
    }
}