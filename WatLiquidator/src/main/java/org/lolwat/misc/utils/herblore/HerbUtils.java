package org.lolwat.misc.utils.herblore;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.lolwat.misc.utils.SkillUtils;

public class HerbUtils {
    public static String bestHerbToClean() {
        int level = Skills.getRealLevel(Skill.HERBLORE);
        if (level >= 67) {
            return "Grimy lantadyme";
        } else if (level >= 65) {
            return "Grimy cadantine";
        } else if (level >= 40) {
            return "Grimy irit leaf";
        } else if(level >= 20) {
            return "Grimy harralander";
        } else if(level >= 11) {
            return "Grimy tarromin";
        } else if(level >= 5) {
            return "Grimy marrentill";
        } else {
            return "Grimy guam leaf";
        }
    }

    public static int herbsRequiredToLevel() {
        String herb = bestHerbToClean();
        int xpNeeded = SkillUtils.getExperienceToNextLevel(Skill.HERBLORE);
        int divider = 0;

        switch(herb) {
            case "Grimy guam leaf":
                divider = 2;
                break;
            case "Grimy marrentill":
                divider = 3;
                break;
            case "Grimy tarromin":
                divider = 5;
                break;
            case "Grimy harralander":
                divider = 6;
                break;
            case "Grimy irit leaf":
                divider = 8;
                break;
            case "Grimy cadantine":
                divider = 12;
                break;
            case "Grimy lantadyme":
                divider = 13;
                break;
        }

        return xpNeeded / divider;
    }
}
