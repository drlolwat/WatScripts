package org.lolwat.Tasks;

import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.WatMiner;

import java.util.*;
import java.util.List;

public interface WatTask {
    String getName();
    boolean canPerformTask();
    void execute(WatMiner instance);
    boolean requiresLogin();
    int loopTime();
    void onExpGained(Skill skill, int amount, WatMiner instance);
}
