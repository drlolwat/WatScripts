package org.lolwat.managers.types;

import org.dreambot.api.methods.quest.book.Quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface QuestTask {
    Quest completes();
    void execute();
    boolean canPerformTask();
    default boolean requiresMembers() { return false; }
    default HashMap<String, Integer> clothesRequired() { return new HashMap<>(); };
    default HashMap<String, Integer> inventoryRequired() { return new HashMap<>(); };
    default List<String> inventoryTolerated() {
        return new ArrayList<>();
    }
}
