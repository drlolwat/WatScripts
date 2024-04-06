package org.lolwat.tasks;

import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.lolwat.WatAIO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    HashMap<String, Integer> clothesRequired();
    HashMap<String, Integer> inventoryRequired();
    default List<String> inventoryTolerated() {
        return new ArrayList<>();
    }
    default HashMap<String, Object> data() {
        return new HashMap<>();
    }
    default void onMessage(Message m) {

    }
    default boolean requiresMembers() {
        return false;
    }
}
