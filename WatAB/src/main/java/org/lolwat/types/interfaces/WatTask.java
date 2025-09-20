package org.lolwat.types.interfaces;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.lolwat.WatScript;
import org.lolwat.types.gear.WatItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface WatTask {
    void execute();
    boolean canPerformTask();
    Skill trainsSkill();
    Integer avoidAfterLevel();
    default int loopTime() { return 400; }
    default QuestTask questTask() { return null; }
    default HashMap<WatItem, Integer> loadout() { return new HashMap<>(); }
    default HashMap<WatItem, Integer> inventory() { return new HashMap<>(); }
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
    default boolean requiresLogin() { return true; }
    default void onExpGained(Skill skill, int amount, WatScript instance) { }
    default String getName() { return this.getClass().getSimpleName(); }
    default String getLocation() { return "Some location"; }
    default void onGroundItemSpawn(GroundItem item) { }
}
