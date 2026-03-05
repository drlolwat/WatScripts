package org.lolwat.types.gear;

import lombok.Getter;
import org.dreambot.api.methods.skills.Skill;

import java.util.HashMap;

public class WatTool extends WatItem {
    @Getter
    private String name;
    @Getter
    private Skill skillUsed;
    @Getter
    private HashMap<Skill, Integer> skillsToEquip;
    @Getter
    private int weight;

    public WatTool(String name, Skill skillUsed, HashMap<Skill, Integer> levelRequirements, HashMap<Skill, Integer> skillsToEquip) {
        super(name, levelRequirements);
        this.name = name;
        this.skillUsed = skillUsed;
        this.skillsToEquip = skillsToEquip;
        this.weight = 10;
    }
}
