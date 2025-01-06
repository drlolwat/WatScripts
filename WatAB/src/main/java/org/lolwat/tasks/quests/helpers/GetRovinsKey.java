package org.lolwat.tasks.quests.helpers;

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
import org.lolwat.tasks.misc.TraversalTask;

import java.util.Arrays;

public class GetRovinsKey implements WatTask {
    private final WatTask wrapper;

    public GetRovinsKey(WatTask wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public String getName() {
        return "Collecting Captain Rovin's key";
    }

    @Override
    public void execute() {
        if(ItemUtils.inventoryContains(2400, 1, false)) {
            Logger.log("Rovins key in inventory");
            TaskManager.getInstance().setCurrentTask(wrapper);
            return;
        }

        Area rovinArea = new Area(
                new Tile(3200, 3498, 2),
                new Tile(3200, 3496, 2),
                new Tile(3202, 3494, 2),
                new Tile(3204, 3494, 2),
                new Tile(3206, 3496, 2),
                new Tile(3206, 3498, 2),
                new Tile(3204, 3500, 2),
                new Tile(3202, 3500, 2));

        if (!Dialogues.inDialogue() && !rovinArea.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(rovinArea, this));
            return;
        }

        if (Dialogues.inDialogue()) {
            DialogueUtils.continueWhilePossible();
            DialogueUtils.solve(Arrays.asList("Can you give me your key?",
                    "Yes I know, but this is important.",
                    "There's a demon who wants to invade this city.",
                    "Yes, very.",
                    "It's not them who are going to fight the demon, it's me.",
                    "Sir Prysin said you would give me the key.",
                    "Fortune-teller Aris said I was destined to kill the demon.",
                    "Otherwise the demon will destroy the city",
                    "Sir Prysin said you would give me the key",
                    "Why did he give you one of the keys then?"));
        }

        NPC rovin = NPCs.closest(x -> x != null && x.exists() && x.getName().equalsIgnoreCase("captain rovin")
                && x.canReach());

        if (rovin != null) {
            if (!Dialogues.inDialogue()) {
                if (!rovin.interact("Talk-to")) {
                    Logger.log("Failed to interact with Captain Rovin");
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
