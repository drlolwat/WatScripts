package org.lolwat.types;

import lombok.Getter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;

import java.util.HashMap;
import java.util.List;

@Getter
public class Mob {
    private final String name;
    private final HashMap<String, Area> locations;
    private final HashMap<Skill, Integer> levelRequirements;
    private final List<Quest> questRequirements;
    private final boolean membersOnly;
    // maybe rangedOnly or meleeOnly?
    // maybe a list of required gear, for slayer, the combat task can inherit from this class and have a list of required gear

    public Mob(String name, HashMap<String, Area> locations, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements, boolean membersOnly) {
        this.name = name;
        this.locations = locations;
        this.levelRequirements = levelRequirements;
        this.questRequirements = questRequirements;
        this.membersOnly = membersOnly;
    }

}
