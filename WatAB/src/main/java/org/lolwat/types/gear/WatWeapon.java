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

    public WatWeapon(String name, String searchFor, CombatType type) {
        super(name, searchFor);
        this.combatType = type;
    }

    public WatWeapon(String name, CombatType type) {
        super(name);
        this.combatType = type;
    }

    public WatWeapon(String name, String searchFor, CombatType type, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements) {
        super(name, searchFor, levelRequirements, questRequirements);
        this.combatType = type;
    }

    public WatWeapon(String name, CombatType type, HashMap<Skill, Integer> levelRequirements) {
        super(name, levelRequirements);
        this.combatType = type;
    }

    public WatWeapon(String name, CombatType type, List<Quest> questRequirements) {
        super(name, questRequirements);
        this.combatType = type;
    }

    public WatWeapon(String name, CombatType type, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements) {
        super(name, levelRequirements, questRequirements);
        this.combatType = type;
    }
}
