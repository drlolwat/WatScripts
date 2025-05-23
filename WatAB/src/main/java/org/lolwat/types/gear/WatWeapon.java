package org.lolwat.types.gear;

import lombok.Getter;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;

import java.util.HashMap;
import java.util.List;

public class WatWeapon extends WatItem {
    @Getter
    public final boolean weapon = true;

    public WatWeapon(String name, String searchFor) {
        super(name, searchFor);
    }

    public WatWeapon(String name) {
        super(name);
    }

    public WatWeapon(String name, String searchFor, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements) {
        super(name, searchFor, levelRequirements, questRequirements);
    }

    public WatWeapon(String name, HashMap<Skill, Integer> levelRequirements) {
        super(name, levelRequirements);
    }

    public WatWeapon(String name, List<Quest> questRequirements) {
        super(name, questRequirements);
    }

    public WatWeapon(String name, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements) {
        super(name, levelRequirements, questRequirements);
    }
}
