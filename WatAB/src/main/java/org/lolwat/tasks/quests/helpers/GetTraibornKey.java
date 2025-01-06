package org.lolwat.tasks.quests.helpers;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.lolwat.managers.TaskManager;
import org.lolwat.types.tasks.WatTask;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.Arrays;
import java.util.HashMap;

public class GetTraibornKey implements WatTask {
    private final WatTask wrapper;

    public GetTraibornKey(WatTask wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public String getName() {
        return "Collecting Traiborn's key";
    }

    @Override
    public void execute() {
        if(ItemUtils.inventoryContains(2399, 1, false)) {
            Logger.log("Traiborns key in inventory");
            TaskManager.getInstance().setCurrentTask(wrapper);
            return;
        }

        Area traibornArea = new Area(
                new Tile(3110, 3165, 1),
                new Tile(3110, 3160, 1),
                new Tile(3114, 3160, 1),
                new Tile(3114, 3163, 1),
                new Tile(3111, 3165, 1));

        if (!Dialogues.inDialogue() && !traibornArea.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(traibornArea, this));
            return;
        }

        if (Dialogues.inDialogue()) {
            DialogueUtils.wipeOptions();
            DialogueUtils.solve(Arrays.asList("Talk about Demon Slayer.",
                    "He told me you were looking after it for him.",
                    "Well, have you got any keys knocking around?",
                    "I'll get the bones for you."));
            return;
        }

        if (!Dialogues.inDialogue()) {
            if (!Inventory.contains(x -> x.getName().equalsIgnoreCase("bones") && !x.isNoted()) || Inventory.count("Bones") < 25) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                    {
                        put("Bones", 25);
                    }
                }, null, 1, this));
                return;
            }

            NPC traiborn = NPCs.closest(x -> x != null && x.exists() && x.getName().equalsIgnoreCase("wizard traiborn")
                    && x.canReach());

            if (traiborn != null) {
                if (!traiborn.interact("Talk-to")) {
                    Logger.log("Failed to interact with Traiborn");
                    return;
                }

                Sleep.sleepUntil(Dialogues::inDialogue, 5000);
            }
        }
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public boolean requiresLogin() {
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
}
