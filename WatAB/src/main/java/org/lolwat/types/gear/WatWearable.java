package org.lolwat.types.gear;

import lombok.Getter;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;

import java.util.HashMap;
import java.util.List;

public class WatWearable extends WatItem {
    @Getter
    public final boolean wearable = true;
    @Getter
    public final EquipmentSlot slot;

    public WatWearable(String name, String searchFor, EquipmentSlot slot) {
        super(name, searchFor);
        this.slot = slot;
    }

    public WatWearable(String name, EquipmentSlot slot) {
        super(name);
        this.slot = slot;
    }

    public WatWearable(String name, String searchFor, EquipmentSlot slot, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements) {
        super(name, searchFor, levelRequirements, questRequirements);
        this.slot = slot;
    }

    public WatWearable(String name, EquipmentSlot slot, HashMap<Skill, Integer> levelRequirements) {
        super(name, levelRequirements);
        this.slot = slot;
    }

    public WatWearable(String name, EquipmentSlot slot, List<Quest> questRequirements) {
        super(name, questRequirements);
        this.slot = slot;
    }

    public WatWearable(String name, EquipmentSlot slot, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements) {
        super(name, levelRequirements, questRequirements);
        this.slot = slot;
    }
}
