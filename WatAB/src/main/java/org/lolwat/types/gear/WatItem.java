package org.lolwat.types.gear;

import lombok.Getter;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class WatItem {
    private final String name;
    private final String searchFor;
    @Getter
    private final HashMap<Skill, Integer> levelRequirements;
    @Getter
    private final List<Quest> questRequirements;
    @Getter
    private final boolean wearable = false;
    @Getter
    private final boolean weapon = false;
    @Getter
    private int price = 0;

    public WatItem(String name, String searchFor) {
        this.name = name;
        this.searchFor = searchFor;
        this.levelRequirements = new HashMap<>();
        this.questRequirements = new ArrayList<>();
        this.price = (int) (LivePrices.getHigh(this.name) * 1.2);
    }

    public WatItem(String name) {
        this.name = name;
        this.searchFor = name;
        this.levelRequirements = new HashMap<>();
        this.questRequirements = new ArrayList<>();
        this.price = (int) (LivePrices.getHigh(this.name) * 1.2);
    }

    public WatItem(String name, String searchFor, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements) {
        this.name = name;
        this.searchFor = searchFor;
        this.levelRequirements = levelRequirements;
        this.questRequirements = questRequirements;
        this.price = (int) (LivePrices.getHigh(this.name) * 1.2);
    }

    public WatItem(String name, HashMap<Skill, Integer> levelRequirements) {
        this.name = name;
        this.searchFor = name;
        this.levelRequirements = levelRequirements;
        this.questRequirements = new ArrayList<>();
        this.price = (int) (LivePrices.getHigh(this.name) * 1.2);
    }

    public WatItem(String name, List<Quest> questRequirements) {
        this.name = name;
        this.searchFor = name;
        this.levelRequirements = new HashMap<>();
        this.questRequirements = questRequirements;
        this.price = (int) (LivePrices.getHigh(this.name) * 1.2);
    }

    public WatItem(String name, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements) {
        this.name = name;
        this.searchFor = name;
        this.levelRequirements = levelRequirements;
        this.questRequirements = questRequirements;
        this.price = (int) (LivePrices.getHigh(this.name) * 1.2);
    }

    public boolean isTradeable() {
        return LivePrices.getHigh(this.name) > 0;
    }

    public void raisePrice() {
        this.price = (int) (this.price * 1.1);
    }
}
