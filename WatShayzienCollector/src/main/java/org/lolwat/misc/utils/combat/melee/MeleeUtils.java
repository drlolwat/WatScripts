package org.lolwat.misc.utils.combat.melee;

import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.lolwat.misc.types.combat.DefensiveItemType;

import java.util.HashMap;

public class MeleeUtils {
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
        ret.put(EquipmentSlot.CHEST, defensiveItemByType(DefensiveItemType.CHEST, Skills.getRealLevel(Skill.DEFENCE) >= 40));
        ret.put(EquipmentSlot.LEGS, defensiveItemByType(DefensiveItemType.LEGS, false));
        ret.put(EquipmentSlot.SHIELD, defensiveItemByType(DefensiveItemType.OFFHAND, false));
        ret.put(EquipmentSlot.WEAPON, bestMeleeWeapon());
        ret.put(EquipmentSlot.AMULET, "Amulet of strength");
        ret.put(EquipmentSlot.FEET, "Leather boots");
        ret.put(EquipmentSlot.HANDS, "Leather gloves");
        ret.put(EquipmentSlot.CAPE, "Black cape");

        return ret;
    }

    public static String defensiveItemByType(DefensiveItemType type, boolean chainbody) {
        String material = bestDefEquipmentMaterial();
        switch(type) {
            case HELMET: return material + " full helm";
            case CHEST: return material + (chainbody ? " chainbody" : " platebody");
            case LEGS: return material + " platelegs";
            case OFFHAND: return material + " kiteshield";
        }
        return "";
    }

    public static String bestDefEquipmentMaterial() {
        int defLevel = Skills.getRealLevel(Skill.DEFENCE);

        if(defLevel >= 40) {
            return "Rune";
        }
        else if(defLevel >= 30) {
            return "Adamant";
        }
        else if(defLevel >= 20) {
            return "Mithril";
        }
        else if(defLevel >= 10) {
            return "Black";
        }
        else if(defLevel >= 5) {
            return "Iron";
        }
        else {
            return "Iron";
        }
    }

    public static String bestMeleeWeapon() {
        int attackLevel = Skills.getRealLevel(Skill.ATTACK);

        if(attackLevel >= 40) {
            return "Rune scimitar";
        }
        else if(attackLevel >= 30) {
            return "Adamant scimitar";
        }
        else if(attackLevel >= 20) {
            return "Mithril scimitar";
        }
        else if(attackLevel >= 10) {
            return "Black scimitar";
        }
        else if(attackLevel >= 5) {
            return "Steel scimitar";
        }
        else {
            return "Iron scimitar";
        }
    }
}
