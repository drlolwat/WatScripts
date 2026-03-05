package org.lolwat.types.gear;

import lombok.Getter;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.misc.types.combat.CombatType;

import java.util.HashMap;
import java.util.List;

public class WatWeapon extends WatItem {
    @Getter
    private CombatType combatType;
    @Getter
    private int weight;

    public WatWeapon(String name, String searchFor, CombatType type, int weight) {
        super(name, searchFor);
        this.combatType = type;
        this.weight = weight;
    }

    public WatWeapon(String name, CombatType type, int weight) {
        super(name);
        this.combatType = type;
        this.weight = weight;
    }

    public WatWeapon(String name, String searchFor, CombatType type, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements, int weight) {
        super(name, searchFor, levelRequirements, questRequirements);
        this.combatType = type;
        this.weight = weight;
    }

    public WatWeapon(String name, CombatType type, HashMap<Skill, Integer> levelRequirements, int weight) {
        super(name, levelRequirements);
        this.combatType = type;
        this.weight = weight;
    }

    public WatWeapon(String name, CombatType type, List<Quest> questRequirements, int weight) {
        super(name, questRequirements);
        this.combatType = type;
        this.weight = weight;
    }

    public WatWeapon(String name, CombatType type, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements, int weight) {
        super(name, levelRequirements, questRequirements);
        this.combatType = type;
        this.weight = weight;
    }
}
