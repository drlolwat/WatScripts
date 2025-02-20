package org.lolwat.types.interfaces;

import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.quest.book.Quest;
import org.lolwat.types.gear.WatItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface QuestTask {
    Quest completes();
    void execute(WatTask wrapper);
    boolean canPerformTask();
    int getState();
    default boolean requiresMembers() { return false; }
    default HashMap<EquipmentSlot, WatItem> clothesRequired() { return new HashMap<>(); }

    default HashMap<String, Integer> inventoryRequired() { return new HashMap<>(); }

    default List<String> inventoryTolerated() {
        return new ArrayList<>();
    }
}
