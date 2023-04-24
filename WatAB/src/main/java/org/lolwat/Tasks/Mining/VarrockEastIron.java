package org.lolwat.Tasks.Mining;

import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.Tasks.WatTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VarrockEastIron implements WatTask {
    String name = "Varrock East Iron";

    HashMap<Skill, Integer> levelRequirements = new HashMap<Skill, Integer>() {
        {
            put(Skill.MINING, 15);
        }
    };

}
