package org.lolwat.misc.utils;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.WatAIO;

import static org.dreambot.api.utilities.Logger.log;

public class TutorialUtils {
    private static final Tab[] tabs = new Tab[]{Tab.COMBAT, Tab.SKILLS, Tab.QUEST, Tab.INVENTORY, Tab.EQUIPMENT,
            Tab.PRAYER, Tab.MAGIC, Tab.CLAN, Tab.ACCOUNT_MANAGEMENT, Tab.FRIENDS, Tab.LOGOUT, Tab.OPTIONS, Tab.EMOTES, Tab.MUSIC};

    public static boolean needsOpenTab() {
        return PlayerSettings.getConfig(1021) != 2560 && PlayerSettings.getConfig(1021) != 2048;
    }

    public static Tab getTab() {
        int conf = PlayerSettings.getConfig(1021) & 15;
        conf -= 1;
        int conf2 = PlayerSettings.getConfig(281);
        if (conf2 == 580) {
            conf = 9;
        }
        if (conf2 == 590)
            conf = 8;
        if (conf >= 0 && conf < tabs.length) {
            return tabs[conf];
        }
        return null;
    }

    public static void handleTab() {
        if(GenericUtils.isOnTutorial()) {
            final Tab t = getTab();
            if (t == null) {
                log("Tab is null?");
                return;
            }

            if (Tabs.openWithMouse(t)) {
                Sleep.sleepUntil(() -> Tabs.isOpen(t), Calculations.random(1200, 1600));
            }
        }
    }
}
