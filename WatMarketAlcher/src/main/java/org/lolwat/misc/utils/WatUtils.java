package org.lolwat.misc.utils;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.magic.Spell;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.util.HashMap;
import java.util.Map;

public class WatUtils {
    public static String getBestLogType() {
        int fletchingLevel = Skills.getRealLevel(Skill.FLETCHING);
        if(fletchingLevel >= 65) {
            return "Yew logs";
        }
        else if(fletchingLevel >= 50) {
            return "Maple logs";
        }
        else if(fletchingLevel >= 35) {
            return "Willow logs";
        }
        else if(fletchingLevel >= 20) {
            return "Oak logs";
        }
        else {
            return "Logs";
        }
    }

    public static void handleWidgetFletching() {
        int fletchingLevel = Skills.getRealLevel(Skill.FLETCHING);
        Widget widget = Widgets.getWidget(270);
        if (widget != null && widget.isVisible()) {
            int childId = getChildId(fletchingLevel);
            WidgetChild child = widget.getChild(childId);
            if (child != null && child.isVisible()) {
                if (!child.interact()) {
                    Logger.log("Failed to interact with widget");
                    return;
                }

                Sleep.sleepUntil(() -> !Dialogues.inDialogue() && Players.getLocal().isAnimating(), 5000);
                Sleep.sleepUntil(() -> !Inventory.contains(getBestLogType()) || Dialogues.inDialogue(), 80000);
            }
            else {
                Logger.log("Child not visible");
            }
        } else {
            Logger.log("Widget not visible");
        }
    }

    public static int getChildId(int fletchingLevel) {
        int childId;
        if(fletchingLevel >= 70) {
            childId = 16;
        }
        else if(fletchingLevel >= 65) {
            childId = 15;
        }
        else if(fletchingLevel >= 55) {
            childId = 16;
        }
        else if(fletchingLevel >= 50) {
            childId = 15;
        }
        else if(fletchingLevel >= 40) {
            childId = 16;
        }
        else if(fletchingLevel >= 35) {
            childId = 15;
        }
        else if(fletchingLevel >= 25) {
            childId = 16;
        }
        else if(fletchingLevel >= 20) {
            childId = 15;
        }
        else if (fletchingLevel >= 10) {
            childId = 16;
        }
        else if (fletchingLevel >= 5) {
            childId = 15;
        }
        else {
            childId = 14;
        }
        return childId;
    }

    public static HashMap<String, Integer> getRunesRequired(Normal spell, int spellCount) {
        HashMap<String, Integer> ret = new HashMap<>();
        if(spellCount <= 0) {
            spellCount = Calculations.random(50, 100);
        }

        switch(spell) {
            default:
            case WIND_STRIKE: {
                ret.put("Mind rune", spellCount * 3);
                ret.put("Water rune", spellCount);
                ret.put("Earth rune", spellCount * 2);
                break;
            }

            case WATER_STRIKE: {
                ret.put("Water rune", spellCount);
                ret.put("Mind rune", spellCount);
                break;
            }

            case EARTH_STRIKE: {
                ret.put("Mind rune", spellCount);
                ret.put("Earth rune", spellCount * 2);
                break;
            }

            case FIRE_STRIKE: {
                ret.put("Mind rune", spellCount);
                ret.put("Fire rune", spellCount * 3);
                break;
            }

            case FIRE_BOLT: {
                ret.put("Chaos rune", spellCount);
                ret.put("Fire rune", spellCount * 4);
                break;
            }

            case WIND_BLAST: {
                ret.put("Death rune", spellCount);
                break;
            }

            case FIRE_BLAST: {
                ret.put("Death rune", spellCount);
                ret.put("Fire rune", spellCount * 5);
            }

            case HIGH_LEVEL_ALCHEMY: {
                ret.put("Nature rune", spellCount);
                break;
            }
        }

        return ret;
    }

    public static boolean canAffordCast(Spell spell) {
        HashMap<String, Integer> runes = getRunesRequired((Normal)spell, 1);
        for(Map.Entry<String, Integer> kv : runes.entrySet()) {
            if(!Inventory.contains(kv.getKey())) {
                return false;
            }

            if(Inventory.count(kv.getKey()) < kv.getValue()) {
                return false;
            }
        }

        return true;
    }
}
