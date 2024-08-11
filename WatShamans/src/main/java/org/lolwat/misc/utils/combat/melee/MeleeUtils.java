package org.lolwat.misc.utils.combat.melee;

import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.PaidQuest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.lolwat.managers.ConfigManager;
import org.lolwat.misc.types.combat.DefensiveItemType;
import org.lolwat.misc.utils.GenericUtils;

import java.util.HashMap;

public class MeleeUtils {
    public static HashMap<String, Integer> getRequiredItems(boolean replaceDefender) {
        HashMap<String, Integer> ret = new HashMap<>();

        for(String s : bestGearForLevel().values()) {
            if(replaceDefender) {
                if(s.contains("defender")) {
                    s = s.replace("Dragon", "Rune"); // lolwat
                    s = s.replace("defender", "kiteshield");
                }
            }

            ret.put(s, 1);
        }

        return ret;
    }

    public static HashMap<EquipmentSlot, String> bestGearForLevel() {
        HashMap<EquipmentSlot, String> ret = new HashMap<>();

        if(Combat.getCombatLevel() < 10) {
            ret.put(EquipmentSlot.WEAPON, "Bronze sword");
            ret.put(EquipmentSlot.SHIELD, "Wooden shield");
        }
        else {
            ret.put(EquipmentSlot.HAT, defensiveItemByType(DefensiveItemType.HELMET, false));
            ret.put(EquipmentSlot.CHEST, defensiveItemByType(DefensiveItemType.CHEST, Skills.getRealLevel(Skill.DEFENCE) >= 40));
            ret.put(EquipmentSlot.LEGS, defensiveItemByType(DefensiveItemType.LEGS, ConfigManager.getInstance().getConfigBoolean("profile_use_plateskirt")));
            ret.put(EquipmentSlot.SHIELD, defensiveItemByType(DefensiveItemType.OFFHAND, false));
            ret.put(EquipmentSlot.WEAPON, bestMeleeWeapon());
            ret.put(EquipmentSlot.AMULET, GenericUtils.isMember() ? "Amulet of glory" : "Amulet of strength");
            ret.put(EquipmentSlot.FEET, defensiveItemByType(DefensiveItemType.FEET, false));
            ret.put(EquipmentSlot.HANDS, "Leather gloves");
            if (ConfigManager.getInstance().getConfigBoolean("use_profile_cape")) {
                ret.put(EquipmentSlot.CAPE, ConfigManager.getInstance().getConfigString("profile_cape_type"));
            }
        }

        return ret;
    }

    public static String defensiveItemByType(DefensiveItemType type, boolean alternative) {
        String material = bestDefEquipmentMaterial();

        if((alternative && type.equals(DefensiveItemType.CHEST)) && Quests.isFinished(FreeQuest.DRAGON_SLAYER))
            alternative = false;

        if(!alternative && GenericUtils.isMember() && type.equals(DefensiveItemType.CHEST)) {
            alternative = true;
        }

        switch(type) {
            case HELMET: {
                if(!GenericUtils.isMember()) {
                    return material + " full helm";
                }
                else {
                    if(Skills.getRealLevel(Skill.DEFENCE) >= 40) {
                        return material + " med helm";
                    }
                    else {
                        return material + " full helm";
                    }
                }
            }
            case CHEST: return material + (alternative ? " chainbody" : " platebody");
            case LEGS: return material + (ConfigManager.getInstance().getConfigBoolean("profile_use_plateskirt") ? " plateskirt" : " platelegs");
            case OFFHAND: {
                if(GenericUtils.isMember()) {
                    int defLevel = Skills.getRealLevel(Skill.DEFENCE);
                    int attLevel = Skills.getRealLevel(Skill.ATTACK);
                    int strLevel = Skills.getRealLevel(Skill.STRENGTH);

                    if((defLevel + attLevel + strLevel) >= 130) {
                        return material + " defender";
                    }

                    if(material.equals("Dragon")) {
                        return "Rune kiteshield";
                    }
                }

                return material + " kiteshield";
            }

            case FEET: {
                if(GenericUtils.isMember()) {
                    return material + " boots";
                }

                return "Leather boots";
            }
        }

        return "";
    }

    public static String bestDefEquipmentMaterial() {
        int defLevel = Skills.getRealLevel(Skill.DEFENCE);

        if (defLevel >= 40) {
            if (GenericUtils.isMember() && defLevel >= 60) {
                return "Dragon";
            }
            return "Rune";
        } else if (defLevel >= 30) {
            return "Adamant";
        } else if (defLevel >= 20) {
            return "Mithril";
        } else if (defLevel >= 10) {
            return "Iron";
        } else {
            return "Iron";
        }
    }

    public static String bestMeleeWeapon() {
        int attackLevel = Skills.getRealLevel(Skill.ATTACK);

        if(attackLevel >= 40) {
            if(GenericUtils.isMember()) {
                if(attackLevel >= 60) {
                    if(Quests.isFinished(PaidQuest.MONKEY_MADNESS))
                        return "Dragon scimitar";
                    else
                        return "Dragon sword";
                }
            }

            return "Rune scimitar";
        }
        else if(attackLevel >= 30) {
            return "Adamant scimitar";
        }
        else if(attackLevel >= 20) {
            return "Mithril scimitar";
        }
        else if(attackLevel >= 10) {
            return "Iron scimitar";
        }
        else {
            return "Iron scimitar";
        }
    }
}
