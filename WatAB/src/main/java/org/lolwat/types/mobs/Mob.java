package org.lolwat.types.mobs;

import lombok.Getter;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.types.interfaces.MobLogic;

import java.util.HashMap;
import java.util.List;

@Getter
public class Mob {
    private final String name;
    private final HashMap<String, Area> locations;
    private final HashMap<Skill, Integer> levelRequirements;
    private final List<Quest> questRequirements;
    private final boolean membersOnly;
    private final MobLogic mobLogic;

    // maybe rangedOnly or meleeOnly?
    // maybe a list of required gear, for slayer, the combat task can inherit from this class and have a list of required gear

    public Mob(String name, HashMap<String, Area> locations, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements, boolean membersOnly, MobLogic mobLogic) {
        this.name = name;
        this.locations = locations;
        this.levelRequirements = levelRequirements;
        this.questRequirements = questRequirements;
        this.membersOnly = membersOnly;
        this.mobLogic = mobLogic;
    }

    public Area getBestLocation() {
        Area bestLocation = null;
        double closestDistance = Double.MAX_VALUE;

        for (Area location : locations.values()) {
            double distance = location.distance(Players.getLocal().getTile());
            if (distance < closestDistance) {
                closestDistance = distance;
                bestLocation = location;
            }
        }

        return bestLocation;
    }
}
