package org.lolwat.types.gear;

import lombok.Getter;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.misc.types.combat.CombatType;

import java.util.HashMap;
import java.util.List;

public class WatWearable extends WatItem {
    @Getter
    public final EquipmentSlot slot;
    @Getter
    private final CombatType combatType;
    @Getter
    private final int weight;

    public WatWearable(String name, String searchFor, EquipmentSlot slot, CombatType type, int weight) {
        super(name, searchFor);
        this.slot = slot;
        this.combatType = type;
        this.weight = weight;
    }

    public WatWearable(String name, EquipmentSlot slot, CombatType type, int weight) {
        super(name);
        this.slot = slot;
        this.combatType = type;
        this.weight = weight;
    }

    public WatWearable(String name, String searchFor, EquipmentSlot slot, CombatType type, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements, int weight) {
        super(name, searchFor, levelRequirements, questRequirements);
        this.slot = slot;
        this.combatType = type;
        this.weight = weight;
    }

    public WatWearable(String name, EquipmentSlot slot, CombatType type, HashMap<Skill, Integer> levelRequirements, int weight) {
        super(name, levelRequirements);
        this.slot = slot;
        this.combatType = type;
        this.weight = weight;
    }

    public WatWearable(String name, EquipmentSlot slot, CombatType type, List<Quest> questRequirements, int weight) {
        super(name, questRequirements);
        this.slot = slot;
        this.combatType = type;
        this.weight = weight;
    }

    public WatWearable(String name, EquipmentSlot slot, CombatType type, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements, int weight) {
        super(name, levelRequirements, questRequirements);
        this.slot = slot;
        this.combatType = type;
        this.weight = weight;
    }
}
