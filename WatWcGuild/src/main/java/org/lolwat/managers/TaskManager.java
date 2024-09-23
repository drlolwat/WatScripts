package org.lolwat.managers;

import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.utilities.Logger;
import org.lolwat.WatScript;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.misc.BondingTask;
import org.lolwat.tasks.misc.HopperTask;
import org.lolwat.tasks.shamans.ShamanCombatTask;

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
        setCheckedHoursAt(0);
        setMinutesPlayed(0);

        tasks.add(new ShamanCombatTask());

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

        for (WatTask task : tasks) {
            if (task.trainsSkill().equals(Skill.HITPOINTS))
                continue;

            if (task.canPerformTask() && (task.requiresMembers() && GenericUtils.isMember()
                    || !task.requiresMembers() && !GenericUtils.isMember()) && Skills.getRealLevel(task.trainsSkill()) < task.avoidAfterLevel()) {
                Logger.log("TaskManager: Selected task: " + task.getName());
                setCurrentTask(task, 0);
                return;
            }

        }
    }

    public WatTask getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(WatTask task) {
        setCurrentTask(task, getTaskRunTime());
    }

    public void setCurrentTask(WatTask value, int runtime) {
        currentTask = value;

        if(runtime == 0)
            taskSelectedAt = Instant.now().getEpochSecond();

        taskRunTime = runtime > 0
                ? runtime
                : Calculations.random(1200, 1800 + (value != null && value.trainsSkill() != null
                ? (60 * Skills.getRealLevel(value.trainsSkill()))
                : 0));
    }

    private boolean preTaskSelection() {
        if(!ConfigManager.getInstance().hasLoadedProfile() ||
                (getInstance().getCheckedHoursAt() == 0 ||
                        (Instant.now().getEpochSecond() - getInstance().getCheckedHoursAt()) >= 3600)) {

            ConfigManager.getInstance().setHasLoadedProfile(true);
            setCheckedHoursAt(Instant.now().getEpochSecond());
            WatScript.getInstance().enableLoginManager();
        }

        if(!Client.isLoggedIn()) {
            Logger.log("Awaiting login...");
            return true;
        }

        if (!GenericUtils.isMember()) {
            setCurrentTask(new BondingTask(null), 0);
            Logger.log("We are making our account a member");
            return true;
        }

        if(GenericUtils.isMember() && !Worlds.getCurrent().isMembers()) {
            setCurrentTask(new HopperTask(0, (currentTask != null) ? currentTask : null), 0);
            Logger.log("We are hopping into a P2P world");
            return true;
        }

        tasksUntilBreak--;
        Collections.shuffle(tasks);
        return false;
    }

    public List<WatTask> getTasks() { return tasks; }
    public double getTaskSelectedAt() { return taskSelectedAt; }
    public int getTaskRunTime() { return taskRunTime; }

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
