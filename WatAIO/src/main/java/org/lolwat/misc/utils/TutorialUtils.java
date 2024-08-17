package org.lolwat.misc.utils;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

public class TutorialUtils {
    private static final Tab[] tabs = new Tab[]{Tab.COMBAT, Tab.SKILLS, Tab.QUEST, Tab.INVENTORY, Tab.EQUIPMENT,
            Tab.PRAYER, Tab.MAGIC, Tab.CLAN, Tab.ACCOUNT_MANAGEMENT, Tab.FRIENDS, Tab.LOGOUT, Tab.OPTIONS, Tab.EMOTES, Tab.MUSIC};

    public static final int NAME_WIDGET = 558; // ??
    public static final int NAME_TEXT_CHILDID = 12;
    public static final int NAME_LOOKUP_CHILDID = 18; // Actions: "Look up name". If no actions, name is not available
    public static final int NAME_SETNAME_CHILDID = 19; // Actions: "Set name"
    public static final int APPEAR_PAR = 679; // appearance
    public static final int ACCEPT = 74;//68; // appearance

    public static boolean needsOpenTab() {
        return PlayerSettings.getConfig(1021) != 33556992 && PlayerSettings.getConfig(1021) != 33556480;
    }

    public static Tab getTab() {
        // maybe 14192, 2691, 1010 // WAS 1021
        int conf = PlayerSettings.getConfig(1021) & 15;
        conf -= 1;
        //log("Config 1021 adjusted index: " + conf); // more logging
        int tutorialstep = PlayerSettings.getConfig(281);
        //log("Config 281: " + tutorialstep); // more logging
        if (tutorialstep == 580) {
            conf = 9;
        }
        if (tutorialstep == 590)
            conf = 8;
        if (conf >= 0 && conf < tabs.length) {
            return tabs[conf];
        }
        return null;
    }

    public static void handleTab() {
        if(isOnTutorial()) {
            final Tab t = getTab();

            if(t == null) {
                Logger.log("TutorialUtils: Failed to get tab");
                return;
            }

            if (Tabs.openWithMouse(t)) {
                Sleep.sleepUntil(() -> Tabs.isOpen(t), Calculations.random(1200, 1600));
            }
        }
    }

    public static boolean isOnTutorial() {
        return (PlayerSettings.getConfig(281) != 1000);
    }

    public static int getTutorialStep() {
        return PlayerSettings.getConfig(281);
    }
}
