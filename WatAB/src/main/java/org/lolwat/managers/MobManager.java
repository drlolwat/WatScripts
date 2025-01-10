package org.lolwat.managers;

import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.types.mobs.Mob;
import org.lolwat.types.mobs.logic.DefaultLogic;

import java.util.HashMap;
import java.util.List;

@Getter
public class MobManager {
    @Getter @Setter
    private static MobManager instance;
    @Getter
    private List<Mob> mobs;

    public MobManager() {
        instance = this;
    }

    public Mob getBestMob(Skill s) {
        return mobs.get(0);
    }

    public void createMobs() {

    }

    private void addBasicMob(String name, HashMap<String, Area> locations, HashMap<Skill, Integer> levelRequirements) {
        mobs.add(new Mob(name, locations, levelRequirements, null, true, new DefaultLogic()));
    }

    private void addQuestedMob(String name, HashMap<String, Area> locations, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements) {
        mobs.add(new Mob(name, locations, levelRequirements, questRequirements, true, new DefaultLogic()));
    }
}
