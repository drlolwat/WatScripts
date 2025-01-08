package org.lolwat.managers;

import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.types.Mob;

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

    public void createMobs() {

    }

    private void addMob(String name, HashMap<String, Area> locations, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements, boolean membersOnly) {
        mobs.add(new Mob(name, locations, levelRequirements, questRequirements, membersOnly));
    }
}
