package org.lolwat.tasks.quests;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.TaskManager;
import org.lolwat.types.interfaces.QuestTask;
import org.lolwat.types.interfaces.WatTask;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.WalkingTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoricsQuest implements QuestTask {
    private final Area startLocation = new Area(2950, 3449, 2953, 3452);
    private final List<String> startDialogue = Arrays.asList("I wanted to use your anvils.", "Yes.", "Certainly, I'll be right back!");

    @Override
    public Quest completes() {
        return FreeQuest.DORICS_QUEST;
    }

    @Override
    public void execute(WatTask wrapper) {
        List<Integer> validStates = Arrays.asList(0, 1);
        if (validStates.contains(getState())) {
            for(Map.Entry<String, Integer> entry : inventoryRequired().entrySet()) {
                List<Item> i = Inventory.all(x -> x != null && x.getName().equalsIgnoreCase(entry.getKey()) && !x.isNoted());
                if(i.isEmpty() ||
                        Inventory.count(x -> x != null && x.getName().equalsIgnoreCase(entry.getKey()) && !x.isNoted()) < entry.getValue()) {

                    TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 1, wrapper));
                    return;
                }
            }

            NPC doric = NPCs.closest(x -> x != null && x.exists() && x.getName().equals("Doric") && x.canReach());
            if (!startLocation.contains(Players.getLocal()) && doric == null) {
                TaskManager.getInstance().setCurrentTask(new WalkingTask(startLocation, wrapper));
                return;
            }

            if (Dialogues.inDialogue()) {
                DialogueUtils.continueWhilePossible();
                DialogueUtils.solve(startDialogue);
            } else {
                if (doric != null) {
                    if (!doric.interact("Talk-to")) {
                        Logger.log("Failed to interact with Doric");
                    }

                    Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                }
            }
        } else {
            Logger.log("Unhandled state: " + getState());
        }
    }

    @Override
    public boolean canPerformTask() {
        return !Quests.isFinished(completes());
    }

    @Override
    public int getState() {
        return PlayerSettings.getBitValue(31);
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<String, Integer>() {{
            put("Clay", 6);
            put("Copper ore", 4);
            put("Iron ore", 2);
        }};
    }
}
