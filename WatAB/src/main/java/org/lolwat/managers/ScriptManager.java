package org.lolwat.managers;

import lombok.Getter;
import org.dreambot.api.Client;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.input.CameraMode;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.walking.pathfinding.impl.web.WebFinder;
import org.dreambot.api.methods.walking.web.node.impl.teleports.MagicTeleport;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.Menu;
import org.lolwat.WatScript;
import org.lolwat.misc.mouse.HumanMouse;
import org.lolwat.tasks.misc.BreakingTask;
import org.lolwat.tasks.misc.HopperTask;

import java.time.Instant;
import java.util.HashMap;

@Getter
public class ScriptManager {
    private static int uniqueSleep;

    public static void start(String profileName) {
        Logger.log("ScriptManager starting: "
                + org.dreambot.api.script.ScriptManager.getScriptManager().getCurrentScript().getManifest().name() +
                ", profile: " + profileName);

        if(MobManager.getInstance() == null) {
            Logger.log("Constructing MobManager singleton.");
            MobManager.setInstance(new MobManager());
            MobManager.getInstance().createMobs();
        }

        if(ItemManager.getInstance() == null) {
            Logger.log("Constructing ItemManager singleton.");
            ItemManager.setInstance(new ItemManager());
        }

        if (ConfigManager.getInstance() == null) {
            Logger.log("Constructing ConfigManager singleton.");
            ConfigManager.setInstance(new ConfigManager());
            ConfigManager.getInstance().setLevelUps(new HashMap<>());
            ConfigManager.getInstance().setNetWorth(0);
            ConfigManager.getInstance().setNetWorthGeneratedAt(0);
            ConfigManager.getInstance().loadFromProfile(profileName);
        }

        if(TeleportManager.getInstance() == null) {
            Logger.log("Constructing TeleportManager singleton.");
            TeleportManager.setInstance(new TeleportManager());
        }

        if(QuestManager.getInstance() == null) {
            Logger.log("Constructing QuestManager singleton.");
            QuestManager.setInstance(new QuestManager());
        }

        try {
            Logger.log("Setting HumanMouse algorithm.");
            HumanMouse m = new HumanMouse();
            Mouse.setMouseAlgorithm(m);
        } catch(Exception e) {
            Logger.log(e);
        }

        if (TaskManager.getInstance() == null) {
            Logger.log("Constructing TaskManager singleton.");
            TaskManager.setInstance(new TaskManager());
        }

        if(ConfigManager.getInstance().getConfigBoolean("use_menu_manip") && (!Menu.isMenuManipulationActive() || !Walking.isNoClickWalkEnabled())) {
            Logger.log("Enabling menu manipulation and noclick walk");
            Menu.toggleMenuManipulation(true);
            Walking.toggleNoClickWalk(true);
        } else if(!ConfigManager.getInstance().getConfigBoolean("use_menu_manip") && (Menu.isMenuManipulationActive() || Walking.isNoClickWalkEnabled())) {
            Logger.log("Disabling menu manipulation and noclick walk");
            Menu.toggleMenuManipulation(false);
            Walking.toggleNoClickWalk(false);
        }

        uniqueSleep = Calculations.random(ConfigManager.getInstance().getConfigInt("min_sleep_time"),
                ConfigManager.getInstance().getConfigInt("max_sleep_time"));

        if(uniqueSleep < 110) {
            uniqueSleep = 110;
        }

        Logger.log("Unique sleep time: " + uniqueSleep + " give or take 100ms each loop");

        WebFinder.getWebFinder().disableEquipmentTeleports();
        WebFinder.getWebFinder().disableEquippingTeleports();
        WebFinder.getWebFinder().disableInventoryTeleports();
        WebFinder.getWebFinder().disableTeleport(MagicTeleport.LUMBRIDGE_HOME_TELEPORT);

        Walking.setMinimapTargetSize(15);
        Camera.setCameraMode(CameraMode.MOUSE_ONLY);
    }

    public static int run() {
        if (!Client.isLoggedIn()) {
            if (TaskManager.getInstance().getCurrentTask() == null || !(TaskManager.getInstance().getCurrentTask() instanceof BreakingTask)) {
                TaskManager.getInstance().setCurrentTask(null);
                Logger.log("Enabling login manager");
                WatScript.getInstance().enableLoginManager();
                return 3000;
            }
        }

        if (ConfigManager.getInstance().isFirstStart()) {
            ConfigManager.getInstance().setFirstStart(false);
        }

        if (TaskManager.getInstance().getCurrentTask() != null) {
            if (!(TaskManager.getInstance().getCurrentTask() instanceof HopperTask) && Tabs.isOpen(Tab.LOGOUT)) {
                Tabs.open(Tab.INVENTORY);
            }

            if (TaskManager.getInstance().getTaskSelectedAt() > 0 &&
                    (Instant.now().getEpochSecond() - TaskManager.getInstance().getTaskSelectedAt()) >= TaskManager.getInstance().getTaskRunTime()) {

                Logger.log("Picking a new task due to expiry");
                TaskManager.getInstance().getNewTask();
                return 1000;
            }

            if (TaskManager.getInstance().getCurrentTask().questTask() == null) {
                if (TaskManager.getInstance().getCurrentTask().trainsSkill() != null) {
                    if (Skills.getRealLevel(TaskManager.getInstance().getCurrentTask().trainsSkill()) > TaskManager.getInstance().getCurrentTask().avoidAfterLevel()) {
                        if(!TaskManager.getInstance().getCurrentTask().data().containsKey("gp_to_generate")) {
                            Logger.log("We are now avoiding this task " + TaskManager.getInstance().getCurrentTask().getName() + " due to (task) level, picking new task..");
                            TaskManager.getInstance().getSpecificSkillTask(TaskManager.getInstance().getCurrentTask().trainsSkill());
                            return 1000;
                        }
                    }
                    else {
                        if (Skills.getRealLevel(TaskManager.getInstance().getCurrentTask().trainsSkill()) >=
                                ConfigManager.getInstance().getSkillTarget(TaskManager.getInstance().getCurrentTask().trainsSkill())) {
                            if (!TaskManager.getInstance().getCurrentTask().data().containsKey("gp_to_generate")) {
                                Logger.log("We are now avoiding this task " + TaskManager.getInstance().getCurrentTask().getName() + " due to (target) level, picking new task..");
                                TaskManager.getInstance().getNewTask();
                                return 1000;
                            }
                        }
                    }
                }
            } else {
                if(Quests.isFinished(TaskManager.getInstance().getCurrentTask().questTask().completes())) {
                    Logger.log("We are now avoiding this quest, it's completed, picking new task..");
                    TaskManager.getInstance().getNewTask();
                    return 1000;
                }
            }
        } else {
            Logger.log("Task was null, finding a new one...");
            TaskManager.getInstance().getNewTask();
            return 2500;
        }

        if (!Walking.isRunEnabled() && Walking.getRunEnergy() >= Calculations.random(75, 100)) {
            Walking.toggleRun();
            Sleep.sleep(50, 120);
        }

        // double check here
        if (TaskManager.getInstance().getCurrentTask() != null) {
            if (!Client.isLoggedIn() && TaskManager.getInstance().getCurrentTask().requiresLogin()) {
                Logger.log("Waiting for login...");
                return 1000;
            }

            TaskManager.getInstance().getCurrentTask().execute();

            return TaskManager.getInstance().getCurrentTask() != null ? (TaskManager.getInstance().getCurrentTask().loopTime() > 0 ?
                    TaskManager.getInstance().getCurrentTask().loopTime() : 300) : 300 + (uniqueSleep + Calculations.random(-100, 100));
        }

        return 100;
    }
}
