package org.lolwat.misc.utils;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.magic.Spell;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.lolwat.managers.ItemManager;
import org.lolwat.managers.MobManager;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.mobs.Mob;

import java.util.HashMap;
import java.util.Map;

public class CombatUtils {
    private static boolean meetsRequirements(Mob mob, Skill skill) {
        if (mob.getLevelRequirements() != null) {
            for (Map.Entry<Skill, Integer> map : mob.getLevelRequirements().entrySet()) {
                Skill reqSkill = map.getKey();
                if (skill == Skill.MAGIC && (isMeleeSkill(reqSkill) || reqSkill == Skill.RANGED)) {
                    continue;
                }
                if (skill == Skill.RANGED && (isMeleeSkill(reqSkill) || reqSkill == Skill.MAGIC)) {
                    continue;
                }
                if (isMeleeSkill(skill) && (reqSkill == Skill.MAGIC || reqSkill == Skill.RANGED)) {
                    continue;
                }

                if (Skills.getRealLevel(reqSkill) < map.getValue()) {
                    return false;
                }
            }
        }

        if (mob.getQuestRequirements() != null) {
            for (Quest quest : mob.getQuestRequirements()) {
                if (!Quests.isFinished(quest)) {
                    return false;
                }
            }
        }

        return true;
    }
    public static Mob getBestMob(Skill skill) {
        Mob bestMob = null;
        int highestLevel = 0;

        for (Mob mob : MobManager.getInstance().getMobs()) {
            if (meetsRequirements(mob, skill)) {
                int mobLevel = mob.getLevelRequirements().getOrDefault(skill, 0);
                if (mobLevel > highestLevel) {
                    highestLevel = mobLevel;
                    bestMob = mob;
                } else if (mobLevel == highestLevel) {
                    bestMob = mob;
                }
            }
        }

        return bestMob;
    }

    public static boolean canAffordCast(Spell spell) {
        HashMap<WatItem, Integer> runes = getRunesRequired((Normal)spell, 1);
        for(Map.Entry<WatItem, Integer> kv : runes.entrySet()) {
            if(!Inventory.contains(kv.getKey().getName())) {
                return false;
            }

            if(Inventory.count(kv.getKey().getName()) < kv.getValue()) {
                return false;
            }
        }

        return true;
    }

    public static HashMap<WatItem, Integer> getRunesRequired(Normal spell, int spellCount) {
        HashMap<WatItem, Integer> ret = new HashMap<>();
        if(spellCount <= 0) {
            spellCount = Calculations.random(50, 100);
        }

        switch(spell) {
            default:
            case WIND_STRIKE: {
                ret.put(ItemManager.getInstance().getItem("Mind rune"), spellCount * 3);
                ret.put(ItemManager.getInstance().getItem("Water rune"), spellCount);
                ret.put(ItemManager.getInstance().getItem("Earth rune"), spellCount * 2);
                break;
            }

            case WATER_STRIKE: {
                ret.put(ItemManager.getInstance().getItem("Water rune"), spellCount);
                ret.put(ItemManager.getInstance().getItem("Mind rune"), spellCount);
                break;
            }

            case EARTH_STRIKE: {
                ret.put(ItemManager.getInstance().getItem("Mind rune"), spellCount);
                ret.put(ItemManager.getInstance().getItem("Earth rune"), spellCount * 2);
                break;
            }

            case FIRE_STRIKE: {
                ret.put(ItemManager.getInstance().getItem("Mind rune"), spellCount);
                ret.put(ItemManager.getInstance().getItem("Fire rune"), spellCount * 3);
                break;
            }

            case FIRE_BOLT: {
                ret.put(ItemManager.getInstance().getItem("Chaos rune"), spellCount);
                ret.put(ItemManager.getInstance().getItem("Fire rune"), spellCount * 4);
                break;
            }

            case WIND_BLAST: {
                ret.put(ItemManager.getInstance().getItem("Death rune"), spellCount);
                break;
            }

            case FIRE_BLAST: {
                ret.put(ItemManager.getInstance().getItem("Death rune"), spellCount);
                ret.put(ItemManager.getInstance().getItem("Fire rune"), spellCount * 5);
            }

            case HIGH_LEVEL_ALCHEMY: {
                ret.put(ItemManager.getInstance().getItem("Nature rune"), spellCount);
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

        if(magicLevel >= 35) {
            return Normal.FIRE_BOLT;
        }

        if(magicLevel >= 13) {
            return Normal.FIRE_STRIKE;
        }

        if(magicLevel >= 9) {
            return Normal.EARTH_STRIKE;
        }

        if(magicLevel >= 5) {
            return Normal.WATER_STRIKE;
        }

        return Normal.WIND_STRIKE;
    }

    private static boolean isMeleeSkill(Skill s) {
        return s == Skill.ATTACK || s == Skill.STRENGTH || s == Skill.DEFENCE;
    }
}
