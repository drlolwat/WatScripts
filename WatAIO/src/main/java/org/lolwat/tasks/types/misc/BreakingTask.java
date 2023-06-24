package org.lolwat.tasks.types.misc;

import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.lolwat.WatAIO;
import org.lolwat.tasks.WatTask;

public class BreakingTask implements WatTask {
    @Override
    public String getName() {
        return "Taking a break";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute(WatAIO instance) {
        if(Client.isLoggedIn()) {
            Area closest = BankLocation.getNearest().getArea(5);
            if(!closest.contains(Players.getLocal())) {
                instance.currentTask = new TraversalTask(closest, this);
                return;
            }

            if(!Tabs.isOpen(Tab.LOGOUT)) {
                Tabs.open(Tab.LOGOUT);
                Tabs.logout();
            }
        }
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public int loopTime() {
        return 600;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

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
}
