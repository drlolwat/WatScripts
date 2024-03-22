package org.lolwat.tasks.types.misc;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.AccountManager;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.WatAIO;

import java.util.HashMap;

public class BondingTask implements WatTask {
    private final WatTask postTask;

    @Override
    public String getName() {
        return "Bonding";
    }

    public BondingTask(WatTask post) {
        postTask = post;
    }

    @Override
    public void execute(WatAIO instance) {
        boolean requiresHop = false;
        if(!BankLocation.GRAND_EXCHANGE.getArea(5).contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(BankLocation.GRAND_EXCHANGE.getArea(5), this));
            return;
        }

        if(!Inventory.contains("Old school bond (untradeable)")) {
            TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                {
                    put("Old school bond (untradeable)", 1);
                }
            }, new HashMap<>(), 1, this));
            return;
        }

        if(Bank.isOpen()) {
            Bank.close();
            return;
        }

        Sleep.sleepUntil(() -> !Bank.isOpen(), Calculations.random(500, 1000));

        if(!Inventory.interact("Old school bond (untradeable)", "Redeem")) {
            Logger.error("Error redeeming bond, please report. #3");
            return;
        }

        Sleep.sleepUntil(() -> Widgets.isVisible(861), Calculations.random(500, 1000));

        if(Widgets.isVisible((861))) {
            WidgetChild c = Widgets.getWidget(861).getChild(12);
            if(c != null && c.getActions() != null) {
                if(!c.interact()) {
                    Logger.error("Error redeeming bond, please report. #1");
                    return;
                }

                Sleep.sleepUntil(() -> Widgets.isVisible(289), Calculations.random(500, 1000));
                if(Widgets.isVisible(289)) {
                    WidgetChild a = Widgets.getWidget(289).getChild(8);
                    if(a != null && a.getActions() != null) {
                        if(!a.interact()) {
                            Logger.error("Error redeeming bond, please report. #2");
                            return;
                        }

                        Sleep.sleep(5000, Calculations.random(10000, 15000));
                        requiresHop = true;
                    }
                }
            }
        }

        if(requiresHop) {
            TaskManager.getInstance().setCurrentTask(new LogoutTask(false, false, new HopperTask(0, postTask)));
            return;
        }

        Logger.error("For some reason, BondingTask did not require a hop.");
        TaskManager.getInstance().setCurrentTask(postTask);
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return 500;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return null;
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
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}
