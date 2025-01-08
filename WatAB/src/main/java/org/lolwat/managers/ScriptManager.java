package org.lolwat.managers;

import lombok.Getter;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.walking.pathfinding.impl.web.WebFinder;
import org.dreambot.api.methods.walking.web.node.impl.teleports.MagicTeleport;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.widgets.Menu;
import org.lolwat.misc.mouse.HumanMouse;

import java.util.HashMap;

@Getter
public class ScriptManager {
    public static void start(String profileName) {
        Logger.log("ScriptManager starting: "
                + org.dreambot.api.script.ScriptManager.getScriptManager().getCurrentScript().getManifest().name() +
                ", profile: " + profileName);

        if(MobManager.getInstance() == null) {
            Logger.log("Constructing MobManager singleton.");
            MobManager.setInstance(new MobManager());
            MobManager.getInstance().createMobs();
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

        WebFinder.getWebFinder().disableEquipmentTeleports();
        WebFinder.getWebFinder().disableEquippingTeleports();
        WebFinder.getWebFinder().disableInventoryTeleports();
        WebFinder.getWebFinder().disableTeleport(MagicTeleport.LUMBRIDGE_HOME_TELEPORT);
    }
}
