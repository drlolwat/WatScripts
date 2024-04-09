package org.lolwat.tasks.misc;

import org.dreambot.api.input.Keyboard;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.WatAIO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdvertiseTask implements WatTask {
    private final Area spamZone = new Area(
            new Tile(3154, 3478, 0),
            new Tile(3175, 3478, 0),
            new Tile(3175, 3503, 0),
            new Tile(3154, 3503, 0));
    private List<String> phrases;
    private int count = 0;
    @Override
    public String getName() {
        return "Scavenging";
    }

    public AdvertiseTask() {
        phrases = new ArrayList<String>() {
            {
                add("make gp like a pro: gg/oldschoolrs");
                add("who grinds these days? gg/oldschoolrs to bot like a legend");
                add("osrs is infested with bots. capitalize with us at gg/oldschoolrs");
                add("gg/oldschoolrs for the best botting experience");
                add("gg/oldschoolrs just look at proggys for examples");
                add("gg/oldschoolrs fully automated botting solutions");
                add("gg/oldschoolrs 150mil+ given away monthly");
                add("fund your main with gg/oldschoolrs");
                add("never grind again with gg/oldschoolrs");
            }
        };
    }

    @Override
    public void execute(WatAIO instance) {
        if(!spamZone.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(spamZone, this));
            return;
        }

        if(Dialogues.canContinue()) {
            Dialogues.continueDialogue();
            return;
        }

        if(count >= 20) {
            count = 0;
            TaskManager.getInstance().setCurrentTask(new HopperTask(0, this));
            return;
        }

        Keyboard.type(phrases.get((int) (Math.random() * phrases.size())));
        count++;
        Sleep.sleep(5000, 10000);
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return 500;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.HITPOINTS;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}
