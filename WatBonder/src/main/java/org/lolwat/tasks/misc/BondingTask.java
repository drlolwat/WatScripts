package org.lolwat.tasks.misc;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.WatScript;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.GenericUtils;

import java.util.HashMap;

public class BondingTask implements WatTask {
    @Override
    public String getName() {
        return "Bonding";
    }
    boolean bonded = false;

    public BondingTask() {

    }

    @Override
    public void execute() {
        if (!BankLocation.GRAND_EXCHANGE.getArea(5).contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(BankLocation.GRAND_EXCHANGE.getArea(5), this));
            return;
        }

        if(GenericUtils.isMember()) {
            return;
        }

        if (!Inventory.contains("Old school bond (untradeable)")) {
            TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                {
                    put("Old school bond (untradeable)", 1);
                }
            }, null, 1, this, null));
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
            Logger.log("WAIO: job done");
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
    public void onExpGained(Skill skill, int amount, WatScript instance) {

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
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
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
