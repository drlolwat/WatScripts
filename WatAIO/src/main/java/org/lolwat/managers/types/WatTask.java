package org.lolwat.managers.types;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.lolwat.WatAIO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface WatTask {
    String getName();
    void execute();
    boolean canPerformTask();
    boolean requiresLogin();
    Skill trainsSkill();
    Integer avoidAfterLevel();
    default int loopTime() { return 400; }
    default QuestTask questTask() { return null; }
    default HashMap<String, Integer> clothesRequired() { return new HashMap<>(); }
    default HashMap<String, Integer> inventoryRequired() { return new HashMap<>(); }
    default List<String> inventoryTolerated() {
        return new ArrayList<>();
    }
    default HashMap<String, Object> data() {
        return new HashMap<>();
    }
    default void onMessage(Message m) { }
    default boolean requiresMembers() {
        return false;
    }
    default void onExpGained(Skill skill, int amount, WatAIO instance) { }
}
