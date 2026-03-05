package org.lolwat.misc.utils.combat.ranged;

import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.lolwat.managers.ConfigManager;
import org.lolwat.misc.types.combat.DefensiveItemType;
import org.lolwat.misc.utils.GenericUtils;

import java.util.HashMap;

public class RangedUtils {
    public static HashMap<String, Integer> getRequiredItems() {
        HashMap<String, Integer> ret = new HashMap<>();

        for(String s : bestGearForLevel().values()) {
            ret.put(s, 1);
        }

        return ret;
    }

    public static HashMap<EquipmentSlot, String> bestGearForLevel() {
        HashMap<EquipmentSlot, String> ret = new HashMap<>();

        ret.put(EquipmentSlot.HAT, defensiveItemByType(DefensiveItemType.HELMET, false));
        ret.put(EquipmentSlot.CHEST, defensiveItemByType(DefensiveItemType.CHEST, Skills.getRealLevel(Skill.RANGED) >= 40));
        ret.put(EquipmentSlot.LEGS, defensiveItemByType(DefensiveItemType.LEGS, false));
        ret.put(EquipmentSlot.WEAPON, bestRangedWeapon());
        ret.put(EquipmentSlot.AMULET, "Amulet of power");
        ret.put(EquipmentSlot.FEET, "Leather boots");
        ret.put(EquipmentSlot.HANDS, defensiveItemByType(DefensiveItemType.GLOVES, false));

        if(ConfigManager.getInstance().getConfigBoolean("use_profile_cape")) {
            ret.put(EquipmentSlot.CAPE, ConfigManager.getInstance().getConfigString("profile_cape_type"));
        }

        return ret;
    }

    public static String defensiveItemByType(DefensiveItemType type, boolean dhide) {
        int rngLevel = Skills.getRealLevel(Skill.RANGED);
        int defLevel = Skills.getRealLevel(Skill.DEFENCE);

        if((dhide && defLevel >= 40 && Quests.isFinished(FreeQuest.DRAGON_SLAYER)) && type == DefensiveItemType.CHEST) {
            dhide = false;
        }

        if(dhide && GenericUtils.isMember()) {
            if(rngLevel >= 50 && defLevel >= 40) {
                dhide = false;
            }
        }

        String mat = bestArmorMaterial(rngLevel);
        switch(type) {
            case HELMET: {
                if(rngLevel >= 20) { return "Coif"; } else { return "Leather cowl"; }
            }
            case CHEST: {
                return dhide ? bestArmorMaterial(20) + " body" : mat + " body";
            }
            case LEGS: {
                if(rngLevel >= 40) {
                    return mat + " chaps";
                }
                else if(rngLevel >= 20) {
                    return "Studded chaps";
                }
                return "Leather chaps";
            }
            case GLOVES: {
                if(rngLevel >= 40) {
                    if (GenericUtils.isMember()) {
                        if (rngLevel >= 70) {
                            return "Black d'hide vambraces";
                        } else if (rngLevel >= 60) {
                            return "Red d'hide vambraces";
                        } else if (rngLevel >= 50) {
                            return "Blue d'hide vambraces";
                        }
                    }

                    return "Green d'hide vambraces";
                }
                else {
                    return "Leather vambraces";
                }
            }
            case FEET: {
                return "Leather boots";
            }
        }

        return type + "";
    }

    public static String bestArmorMaterial(int rngLevel) {
        int defLevel = Skills.getRealLevel(Skill.DEFENCE);
        if(rngLevel >= 40) {
            if(GenericUtils.isMember()) {
                if (rngLevel >= 70) {
                    return "Black d'hide";
                } else if (rngLevel >= 60) {
                    return "Red d'hide";
                } else if (rngLevel >= 50) {
                    return "Blue d'hide";
                }
            }

            return "Green d'hide";
        }
        else if(rngLevel >= 20) {
            if(defLevel >= 20) {
                return "Studded";
            }
            else if(defLevel >= 10) {
                return "Hardleather";
            }
            return "Leather";
        }
        else {
            if(defLevel >= 10) {
                return "Hardleather";
            }
            else {
                return "Leather";
            }
        }
    }

    public static String bestRangedWeapon() {
        int rngLevel = Skills.getRealLevel(Skill.RANGED);
        if(rngLevel >= 50 && GenericUtils.isMember()) {
            return "Magic shortbow";
        }
        else if(rngLevel >= 40 && GenericUtils.isMember()) {
            return "Yew shortbow";
        }
        else if(rngLevel >= 30) {
            return "Maple shortbow";
        }
        else if(rngLevel >= 20) {
            return "Willow shortbow";
        }
        else if(rngLevel >= 5) {
            return "Oak shortbow";
        }
        else {
            return "Shortbow";
        }
    }

    public static String bestArrow() {
        int rngLevel = Skills.getRealLevel(Skill.RANGED);
        if(rngLevel >= 50 && GenericUtils.isMember()) {
            return "Adamant arrow";
        }
        else if(rngLevel >= 20) {
            return "Mithril arrow";
        }
        else if(rngLevel >= 5) {
            return "Steel arrow";
        }
        else {
            return "Iron arrow";
        }
    }
}
