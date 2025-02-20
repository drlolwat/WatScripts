package org.lolwat.managers;

import com.google.common.collect.Lists;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;
import org.lolwat.WatScript;
import org.lolwat.managers.types.WatTask;
import org.lolwat.tasks.shamans.MainCombatTask;

import java.awt.*;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class TaskManager {
    private List<WatTask> tasks;

    // ---- RUNTIME VARIABLES ----
    private static TaskManager instance;
    private WatTask currentTask;
    private int tasksUntilBreak;
    private double taskSelectedAt;
    private int taskRunTime;
    private double checkedHoursAt;
    private int minutesPlayed;

    public TaskManager() {
        tasks = Lists.newArrayList();
        tasks.add(new MainCombatTask());

        setCheckedHoursAt(0);
        setMinutesPlayed(0);
        Logger.log(Color.green, "TaskManager: Set up " + tasks.size() + " total tasks.");
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

        setCurrentTask(new MainCombatTask(), 0);
    }

    public WatTask getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(WatTask task) {
        setCurrentTask(task, getTaskRunTime());
    }

    public void setCurrentTask(WatTask value, int runtime) {
        currentTask = value;

        if (runtime == 0)
            taskSelectedAt = Instant.now().getEpochSecond();

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

        tasksUntilBreak--;
        Collections.shuffle(tasks);
        return false;
    }

    public List<WatTask> getTasks() {
        return tasks;
    }

    public double getTaskSelectedAt() {
        return taskSelectedAt;
    }

    public int getTaskRunTime() {
        return taskRunTime;
    }

    public double getCheckedHoursAt() {
        return checkedHoursAt;
    }

    public void setCheckedHoursAt(double checkedHoursAt) {
        this.checkedHoursAt = checkedHoursAt;
    }

    public int getMinutesPlayed() {
        return minutesPlayed;
    }

    public void setMinutesPlayed(int minutesPlayed) {
        this.minutesPlayed = minutesPlayed;
    }
}
