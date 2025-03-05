package org.lolwat.managers;

import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;
import org.lolwat.WatScript;
import org.lolwat.managers.types.WatTask;
import org.lolwat.tasks.alching.HighAlchemyTask;

public class TaskManager {
    @Getter @Setter
    private static TaskManager instance;
    @Getter
    private WatTask currentTask;
    @Getter
    private int taskRunTime;

    public TaskManager() {

    }

    public void getNewTask() {
        getNewTask(false);
    }

    public void getNewTask(boolean noQuest) {
        if (preTaskSelection()) {
            return;
        }

        setCurrentTask(new HighAlchemyTask(), 0);
    }

    public void setCurrentTask(WatTask task) {
        setCurrentTask(task, getTaskRunTime());
    }

    public void setCurrentTask(WatTask value, int runtime) {
        currentTask = value;
        taskRunTime = runtime > 0
                ? runtime
                : Calculations.random(1200, 1800 + (value != null && value.trainsSkill() != null
                ? (60 * Skills.getRealLevel(value.trainsSkill()))
                : 0));
    }

    private boolean preTaskSelection() {
        if (!Client.isLoggedIn()) {
            WatScript.getInstance().enableLoginManager();
            Logger.log("Awaiting login...");
            return true;
        }

        return false;
    }
}
