package org.lolwat.managers;

import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.types.mobs.Mob;
import org.lolwat.types.mobs.logic.DefaultLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class MobManager {
    @Getter @Setter
    private static MobManager instance;
    @Getter
    private List<Mob> mobs;

    public MobManager() {
        mobs = new ArrayList<>();
        instance = this;
    }

    public void createMobs() {
        addBasicMob("Chicken",
                new HashMap<String, Area>() {
                    {
                        put("Lumbridge North Chicken Test", new Area(3172, 3300, 3183, 3290));
                    }
                },

                new HashMap<Skill, Integer>() {
                    {
                        put(Skill.ATTACK, 1);
                        put(Skill.DEFENCE, 1);
                        put(Skill.STRENGTH, 1);
                        put(Skill.RANGED, 1);
                        put(Skill.MAGIC, 1);
                    }
                });

        addBasicMob("Cow",
                new HashMap<String, Area>() {
                    {
                        put("Lumbridge North Cow Test", new Area(3194, 3291, 3210, 3285));
                    }
                },

                new HashMap<Skill, Integer>() {
                    {
                        put(Skill.ATTACK, 1);
                        put(Skill.DEFENCE, 1);
                        put(Skill.STRENGTH, 1);
                        put(Skill.RANGED, 1);
                        put(Skill.MAGIC, 1);
                    }
                });
    }

    private void addBasicMob(String name, HashMap<String, Area> locations, HashMap<Skill, Integer> levelRequirements) {
        mobs.add(new Mob(name, locations, levelRequirements, null, true, new DefaultLogic()));
    }

    private void addQuestedMob(String name, HashMap<String, Area> locations, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements) {
        mobs.add(new Mob(name, locations, levelRequirements, questRequirements, true, new DefaultLogic()));
    }
}
