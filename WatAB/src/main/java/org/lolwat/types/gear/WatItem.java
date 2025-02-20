package org.lolwat.types.gear;

import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.misc.utils.NumUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class WatItem {
    private final String name;
    private final String searchFor;
    @Setter
    private int priceEach;
    @Getter
    private final HashMap<Skill, Integer> levelRequirements;
    @Getter
    private final List<Quest> questRequirements;

    public WatItem(String name, String searchFor) {
        this.name = name;
        this.searchFor = searchFor;
        this.priceEach = NumUtils.getItemPrice(name);
        this.levelRequirements = new HashMap<>();
        this.questRequirements = new ArrayList<>();
    }

    public WatItem(String name) {
        this.name = name;
        this.searchFor = name;
        this.priceEach = NumUtils.getItemPrice(name);
        this.levelRequirements = new HashMap<>();
        this.questRequirements = new ArrayList<>();
    }

    public WatItem(String name, String searchFor, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements) {
        this.name = name;
        this.searchFor = searchFor;
        this.priceEach = NumUtils.getItemPrice(name);
        this.levelRequirements = levelRequirements;
        this.questRequirements = questRequirements;
    }

    public WatItem(String name, HashMap<Skill, Integer> levelRequirements) {
        this.name = name;
        this.searchFor = name;
        this.priceEach = NumUtils.getItemPrice(name);
        this.levelRequirements = levelRequirements;
        this.questRequirements = new ArrayList<>();
    }

    public WatItem(String name, List<Quest> questRequirements) {
        this.name = name;
        this.searchFor = name;
        this.priceEach = NumUtils.getItemPrice(name);
        this.levelRequirements = new HashMap<>();
        this.questRequirements = questRequirements;
    }
}
