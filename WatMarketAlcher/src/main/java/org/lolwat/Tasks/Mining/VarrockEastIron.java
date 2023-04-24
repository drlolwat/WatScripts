package org.lolwat.Tasks.Mining;

import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.Tasks.WatTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VarrockEastIron implements WatTask {
    String name = "Varrock East Iron";

    private Tile varrockEastMine = new Tile(3286, 3368);
    private Tile varrockEastAlt = new Tile(3285, 3370);

    private List<Tile> varrockEastRocks;
    private List<Tile> varrockEastAltRocks;

    HashMap<Skill, Integer> levelRequirements = new HashMap<Skill, Integer>() {
        {
            put(Skill.MINING, 15);
        }
    };

}
