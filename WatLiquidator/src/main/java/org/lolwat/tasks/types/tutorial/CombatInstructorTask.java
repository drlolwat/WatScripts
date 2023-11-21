package org.lolwat.tasks.types.tutorial;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.WatAIO;
import org.lolwat.tasks.WatTask;

import java.util.HashMap;

public class CombatInstructorTask implements WatTask {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean canPerformTask() {
        return false;
    }

    @Override
    public void execute(WatAIO instance) {
        // traversal to 3104, 9506
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return Calculations.random(350, 450);
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

    }

    @Override
    public Skill trainsSkill() {
        return null;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public Quest completesQuest() {
        return null;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }
}
