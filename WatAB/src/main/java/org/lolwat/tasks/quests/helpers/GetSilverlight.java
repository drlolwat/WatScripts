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
import org.lolwat.misc.utils.WatUtils;
import org.lolwat.types.interfaces.WatTask;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.tasks.misc.QuickWithdrawTask;
import org.lolwat.tasks.misc.WalkingTask;

public class GetSilverlight implements WatTask {
    private final Area prysinArea = new Area(
            new Tile(3200, 3473, 0),
            new Tile(3200, 3471, 0),
            new Tile(3202, 3469, 0),
            new Tile(3204, 3469, 0),
            new Tile(3206, 3471, 0),
            new Tile(3206, 3474, 0),
            new Tile(3204, 3475, 0),
            new Tile(3201, 3475, 0));

    private final WatTask wrapper;

    public GetSilverlight(WatTask wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public void execute() {
        if(WatUtils.equipmentContains(2402, 1) || WatUtils.inventoryContains(2402, 1, false)) {
            TaskManager.getInstance().setCurrentTask(wrapper);
            return;
        }

        // wizard traiborns key
        if(!WatUtils.inventoryContains(2399, 1, false)) {
            TaskManager.getInstance().setCurrentTask(new QuickWithdrawTask(2399, 1,
                    new GetTraibornKey(wrapper), this));
            return;
        }

        // sir prysins key
        if (!WatUtils.inventoryContains(2401, 1, false)) {
            TaskManager.getInstance().setCurrentTask(new QuickWithdrawTask(2401, 1,
                    new GetPrysinKey(wrapper), this));
            return;
        }

        // captain rovins key
        if (!WatUtils.inventoryContains(2400, 1, false)) {
            TaskManager.getInstance().setCurrentTask(new QuickWithdrawTask(2400, 1,
                    new GetRovinsKey(wrapper), this));
            return;
        }

        Logger.log("Need to get Silverlight");

        if(!prysinArea.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new WalkingTask(prysinArea, this));
            return;
        }

        NPC sirPrysin = NPCs.closest(x -> x != null && x.exists() && x.getName().equalsIgnoreCase("sir prysin")
                && x.canReach());

        if (sirPrysin != null) {
            if (!Dialogues.inDialogue()) {
                if (!sirPrysin.interact("Talk-to")) {
                    Logger.log("Failed to interact with Sir Prysin");
                    return;
                }

                Sleep.sleepUntil(Dialogues::inDialogue, 5000);
            }
        }

        if (Dialogues.inDialogue()) {
            DialogueUtils.continueWhilePossible();
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
