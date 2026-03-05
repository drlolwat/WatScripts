package org.lolwat.tasks.misc;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.DialogueUtils;

import java.util.List;

public class TalkToNPC implements WatTask {
    private final String name;
    private final List<String> dialogueOptions;
    private final Area area;
    private final WatTask post;
    private final String stopAfterItem;

    public TalkToNPC(Area area, String name, WatTask post, List<String> dialogueOptions, String stopAfterItem) {
        this.name = name;
        this.dialogueOptions = dialogueOptions;
        this.area = area;
        this.post = post;
        this.stopAfterItem = stopAfterItem;
    }

    @Override
    public String getName() {
        return "Talking to NPC";
    }

    @Override
    public void execute() {
        if(!stopAfterItem.isEmpty() && Inventory.contains(x -> x != null && x.getName().equals(stopAfterItem))) {
            TaskManager.getInstance().setCurrentTask(post);
            return;
        }

        NPC npc = NPCs.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals(name));
        if(!area.contains(Players.getLocal()) && npc == null) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(area, this));
            return;
        }

        if(Dialogues.inDialogue()) {
            DialogueUtils.continueWhilePossible();
            DialogueUtils.solve(dialogueOptions);
        } else {
            if(npc != null) {
                if(!npc.interact("Talk-to")) {
                    Logger.log("Failed to interact with " + name);
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
