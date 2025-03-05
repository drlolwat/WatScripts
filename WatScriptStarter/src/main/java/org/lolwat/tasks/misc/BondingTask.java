package org.lolwat.tasks.misc;

import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.Menu;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.GenericUtils;

import java.util.HashMap;

public class BondingTask implements WatTask {
    @Override
    public String getName() {
        return "Bonding";
    }
    private WatTask post;

    public BondingTask(WatTask post) {
        this.post = post;
    }

    @Override
    public void execute() {
        if (!BankLocation.GRAND_EXCHANGE.getArea(5).contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(BankLocation.GRAND_EXCHANGE.getArea(5), this));
            return;
        }

        if(GenericUtils.getMemberDays() >= 2) {
            TaskManager.getInstance().setCurrentTask(post != null ? post : null);
            return;
        }

        if (Menu.isMenuManipulationActive()) {
            Logger.log("Disabling menu manipulation for bonding");
            Menu.toggleMenuManipulation(false);
        }

        if (!Inventory.contains("Old school bond (untradeable)")) {
            TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                {
                    put("Old school bond (untradeable)", 1);
                }
            }, this));
            return;
        }

        if (Bank.isOpen()) {
            Bank.close();
            return;
        }

        Sleep.sleepUntil(() -> !Bank.isOpen(), Calculations.random(500, 1000));

        if (!Inventory.interact("Old school bond (untradeable)", "Redeem")) {
            Logger.error("Error redeeming bond, please report. #1");
            return;
        }


        Sleep.sleepUntil(() -> Widgets.isVisible(861), 15000);
        Widget bondWindow = Widgets.getWidget(861);

        if (bondWindow != null && bondWindow.isVisible()) {
            Logger.log("opened bond window");
            WidgetChild singleBond = bondWindow.getChild(12);
            if (singleBond != null && singleBond.getActions() != null) {
                if (!singleBond.interact("14 days membership")) {
                    Logger.error("Error redeeming bond, please report. #2");
                    return;
                }

                Sleep.sleepUntil(() -> Widgets.isVisible(289), 15000);
                Widget redeemWindow = Widgets.getWidget(289);

                if (redeemWindow != null && redeemWindow.isVisible()) {
                    WidgetChild acceptButton = redeemWindow.getChild(8);
                    if (acceptButton != null && acceptButton.getActions() != null) {
                        if (!acceptButton.interact("Accept")) {
                            Logger.error("Error redeeming bond, please report. #3");
                            return;
                        }
                    }
                }
            }
        }

        Sleep.sleepUntil(Dialogues::canContinue, 25000);

        if(!Inventory.contains("Old school bond (untradeable)")) {
            if (!Menu.isMenuManipulationActive()) {
                Logger.log("Enabling menu manipulation, bonding complete");
                Menu.toggleMenuManipulation(true);
            }

            if(Client.isLoggedIn()) {
                if(GenericUtils.getMemberDays() == 0) {
                    if (!Tabs.isOpen(Tab.LOGOUT)) {
                        Tabs.open(Tab.LOGOUT);
                        Sleep.sleepUntil(() -> Tabs.isOpen(Tab.LOGOUT), 5000);
                    }

                    Tabs.logout();
                } else {
                    TaskManager.getInstance().setCurrentTask(new HopperTask(0, post));
                    return;
                }
            }
        }

        if(post != null) {
            TaskManager.getInstance().setCurrentTask(post);
        }
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
    public boolean canPerformTask() {
        return true;
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
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<String, Integer>() {
            {
                put("Old school bond (untradeable)", 1);
            }
        };
    }
}
