package org.lolwat.managers;

import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;
import org.lolwat.WatScript;
import org.lolwat.managers.types.WatTask;
import org.lolwat.tasks.fletching.FletchingTask;

public class TaskManager {
    // ---- RUNTIME VARIABLES ----
    private static TaskManager instance;
    private WatTask currentTask;
    private int taskRunTime;

    public TaskManager() {

    }

    public static TaskManager getInstance() {
        return instance;
    }

    public static void setInstance(TaskManager value) {
        instance = value;
    }

    public void getNewTask() {
        getNewTask(false);
    }

    public void getNewTask(boolean noQuest) {
        if (preTaskSelection()) {
            return;
        }

        setCurrentTask(new FletchingTask(true), 0);
    }

    public WatTask getCurrentTask() {
        return currentTask;
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
        if (!ConfigManager.getInstance().hasLoadedProfile()) {
            ConfigManager.getInstance().setHasLoadedProfile(true);
            WatScript.getInstance().enableLoginManager();
        }

        if (!Client.isLoggedIn()) {
            Logger.log("Awaiting login...");
            return true;
        }

        return false;
    }

    public int getTaskRunTime() {
        return taskRunTime;
    }
}
