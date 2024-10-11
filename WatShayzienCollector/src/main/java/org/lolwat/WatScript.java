package org.lolwat;

import org.dreambot.api.Client;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.input.CameraMode;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.walking.pathfinding.impl.web.WebFinder;
import org.dreambot.api.methods.walking.web.node.impl.teleports.MagicTeleport;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.event.impl.ExperienceEvent;
import org.dreambot.api.script.listener.AnimationListener;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.script.listener.ExperienceListener;
import org.dreambot.api.script.listener.SpawnListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.widgets.Menu;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.TeleportManager;
import org.lolwat.misc.mouse.HumanMouse;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.misc.HopperTask;

import java.awt.*;

@ScriptManifest(name = "WatScript1", description = "WatScript1", author = "lolwat", version = 0.1, category = Category.MISC)
public class WatScript extends AbstractScript implements ExperienceListener, ChatListener, AnimationListener, SpawnListener {
    private static WatScript instance;

    public static WatScript getInstance() {
        return instance;
    }

    @Override
    public void onStart(String... params) {
        if (params.length > 0) {
            doStart(params[0]);
        } else {
            doStart("default");
        }
    }

    @Override
    public void onStart() {
        doStart("default");
    }

    private void doStart(String profile) {
        if (instance == null) {
            Logger.log(Color.green, "WatShamans starting: assigning instance");
            instance = this;
        }

        if (ConfigManager.getInstance() == null) {
            Logger.log("Constructing ConfigManager singleton.");
            ConfigManager.setInstance(new ConfigManager());
            ConfigManager.getInstance().setNetWorth(0);
            ConfigManager.getInstance().setNetWorthGeneratedAt(0);
            ConfigManager.getInstance().loadFromProfile(profile);
        }

        if (TeleportManager.getInstance() == null) {
            Logger.log("Constructing TeleportManager singleton.");
            TeleportManager.setInstance(new TeleportManager());
        }

        Walking.setMinimapTargetSize(15);
        Camera.setCameraMode(CameraMode.MOUSE_ONLY);
        HumanMouse m = new HumanMouse();
        Mouse.setMouseAlgorithm(m);

        if (TaskManager.getInstance() == null) {
            Logger.log("Constructing TaskManager singleton.");
            TaskManager.setInstance(new TaskManager());
        }

        getRandomManager().disableSolver(RandomEvent.DISMISS);
        if (!Menu.isMenuManipulationActive()) {
            Logger.log("Enabling menu manipulation and noclick walk");
            Menu.toggleMenuManipulation(true);
            Walking.toggleNoClickWalk(true);
        }

        WebFinder.getWebFinder().disableEquipmentTeleports();
        WebFinder.getWebFinder().disableEquippingTeleports();
        WebFinder.getWebFinder().disableInventoryTeleports();
        WebFinder.getWebFinder().disableTeleport(MagicTeleport.LUMBRIDGE_HOME_TELEPORT);
    }

    @Override
    public int onLoop() {
        if (!Client.isLoggedIn()) {
            if (TaskManager.getInstance().getCurrentTask() == null) {
                TaskManager.getInstance().setCurrentTask(null);
                Logger.log("Enabling login manager");
                enableLoginManager();
                return 3000;
            }
        }

        if (ConfigManager.getInstance().isFirstStart()) {
            Sleep.sleep(5000);
            ConfigManager.getInstance().setFirstStart(false);
        }

        if (TaskManager.getInstance().getCurrentTask() != null) {
            // things to do if the task is active
            if (!(TaskManager.getInstance().getCurrentTask() instanceof HopperTask) && Tabs.isOpen(Tab.LOGOUT)) {
                Tabs.open(Tab.INVENTORY);
            }
        } else {
            Logger.log("Task was null, finding a new one...");
            TaskManager.getInstance().getNewTask();
            return 1500;
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
                    TaskManager.getInstance().getCurrentTask().loopTime() : 300) : 300;
        }

        return 100;
    }

    @Override
    public void onGained(ExperienceEvent ev) {
        if (TaskManager.getInstance().getCurrentTask() != null) {
            TaskManager.getInstance().getCurrentTask().onExpGained(ev.getSkill(), ev.getChange(), this);
        }
    }

    @Override
    public void onMessage(Message m) {
        if (TaskManager.getInstance().getCurrentTask() != null) {
            if (m.getMessage().startsWith("Darts")) {
                int[] data = GenericUtils.parseText(m.getMessage());
                TaskManager.getInstance().getCurrentTask().data().put("darts", data[0]);
                TaskManager.getInstance().getCurrentTask().data().put("scales", data[1]);
                Logger.log("Darts: " + data[0] + ", Scales: " + data[1] + " sent to task");
            }

            TaskManager.getInstance().getCurrentTask().onMessage(m);
        }

        if (m.getMessage().equals("Oh dear, you are dead!")) {
            Logger.log("DEATH DETECTED: STOPPING SCRIPT");
            ScriptManager.getScriptManager().stop();
        }
    }

    @Override
    public void onNpcAnimation(NPC npc, int animation, int animationDelay) {
        if (TaskManager.getInstance().getCurrentTask() != null) {
            TaskManager.getInstance().getCurrentTask().onNpcAnimation(npc, animation, animationDelay);
        }
    }

    @Override
    public void onNpcSpawn(NPC npc) {
        if (TaskManager.getInstance().getCurrentTask() != null) {
            TaskManager.getInstance().getCurrentTask().onNpcSpawn(npc);
        }
    }

    @Override
    public void onNpcDespawn(NPC npc) {
        if (TaskManager.getInstance().getCurrentTask() != null) {
            TaskManager.getInstance().getCurrentTask().onNpcDespawn(npc);
        }
    }

    @Override
    public void onGroundItemSpawn(GroundItem object) {
        if (TaskManager.getInstance().getCurrentTask() != null) {
            TaskManager.getInstance().getCurrentTask().onGroundItemSpawn(object);
        }
    }

    public void disableLoginManager() {
        getRandomManager().disableSolver(RandomEvent.LOGIN);
    }

    public void enableLoginManager() {
        getRandomManager().enableSolver(RandomEvent.LOGIN);
    }
}
