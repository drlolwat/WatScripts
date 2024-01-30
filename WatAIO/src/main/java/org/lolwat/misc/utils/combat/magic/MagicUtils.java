package org.lolwat.misc.utils.combat.magic;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.magic.Spell;
import org.dreambot.api.methods.magic.cost.Rune;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.lolwat.WatAIO;
import org.lolwat.misc.types.combat.DefensiveItemType;

import java.util.HashMap;
import java.util.Map;

public class MagicUtils {
    public static HashMap<EquipmentSlot, String> bestGearForLevel() {
        HashMap<EquipmentSlot, String> ret = new HashMap<>();

        ret.put(EquipmentSlot.HAT, "Wizard hat");
        ret.put(EquipmentSlot.CHEST,"Blue wizard robe");
        ret.put(EquipmentSlot.LEGS, "Zamorak monk bottom");
        ret.put(EquipmentSlot.WEAPON, "Staff of air");
        ret.put(EquipmentSlot.AMULET, "Amulet of magic");
        ret.put(EquipmentSlot.FEET, "Leather boots");
        ret.put(EquipmentSlot.HANDS, "Leather gloves");
        if(WatAIO.WEAR_CAPES) {
            ret.put(EquipmentSlot.CAPE, WatAIO.CAPE_TYPE);
        }
        return ret;
    }

    public static HashMap<String, Integer> getRequiredItems() {
        HashMap<String, Integer> ret = new HashMap<>();

        for(String s : bestGearForLevel().values()) {
            ret.put(s, 1);
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

    public static HashMap<String, Integer> getRunesRequired(Normal spell, int spellCount) {
        HashMap<String, Integer> ret = new HashMap<>();

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

    public static Spell getBestSpellForLevel() {
        int magicLevel = Skills.getRealLevel(Skill.MAGIC);

        if(magicLevel >= 41) {
            return Normal.WIND_BLAST;
        }
        else if(magicLevel >= 35) {
            return Normal.FIRE_BOLT;
        }
        else if(magicLevel >= 13) {
            return Normal.FIRE_STRIKE;
        }
        else if(magicLevel >= 9) {
            return Normal.EARTH_STRIKE;
        }
        else if(magicLevel >= 5) {
            return Normal.WATER_STRIKE;
        }
        else {
            return Normal.WIND_STRIKE;
        }
    }
}
