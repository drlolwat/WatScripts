package org.lolwat.managers.types;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.lolwat.WatScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface WatTask {
    String getName();

    void execute();

    boolean canPerformTask();

    boolean requiresLogin();

    default Skill trainsSkill() {
        return Skill.HITPOINTS;
    }

    default int loopTime() {
        return 400;
    }

    default HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }

    default HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }

    default List<String> inventoryTolerated() {
        return new ArrayList<>();
    }

    default HashMap<String, Object> data() {
        return new HashMap<>();
    }

    default void onMessage(Message m) {
    }

    default void onExpGained(Skill skill, int amount, WatScript instance) {
    }

    default void onNpcAnimation(NPC npc, int animation, int animationDelay) {
    }

    default void onNpcSpawn(NPC npc) {
    }

    default void onNpcDespawn(NPC npc) {
    }

    default void onGroundItemSpawn(GroundItem object) {
    }
}
