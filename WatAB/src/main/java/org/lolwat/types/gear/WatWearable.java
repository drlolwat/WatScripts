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

    public WatWearable(String name, String searchFor, EquipmentSlot slot, CombatType type) {
        super(name, searchFor);
        this.slot = slot;
        this.combatType = type;
    }

    public WatWearable(String name, EquipmentSlot slot, CombatType type) {
        super(name);
        this.slot = slot;
        this.combatType = type;
    }

    public WatWearable(String name, String searchFor, EquipmentSlot slot, CombatType type, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements) {
        super(name, searchFor, levelRequirements, questRequirements);
        this.slot = slot;
        this.combatType = type;
    }

    public WatWearable(String name, EquipmentSlot slot, CombatType type, HashMap<Skill, Integer> levelRequirements) {
        super(name, levelRequirements);
        this.slot = slot;
        this.combatType = type;
    }

    public WatWearable(String name, EquipmentSlot slot, CombatType type, List<Quest> questRequirements) {
        super(name, questRequirements);
        this.slot = slot;
        this.combatType = type;
    }

    public WatWearable(String name, EquipmentSlot slot, CombatType type, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements) {
        super(name, levelRequirements, questRequirements);
        this.slot = slot;
        this.combatType = type;
    }
}
