package org.lolwat.tasks;

import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.WatAIO;

public interface WatTask {
    String getName();
    boolean canPerformTask();
    void execute(WatAIO instance);
    boolean requiresLogin();
    int loopTime();
    void onExpGained(Skill skill, int amount, WatAIO instance);
    Skill trainsSkill();
    Integer avoidAfterLevel();
    Quest completesQuest();
}
