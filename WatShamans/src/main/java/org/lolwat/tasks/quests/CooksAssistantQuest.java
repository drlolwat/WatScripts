package org.lolwat.tasks.quests;

import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.utilities.Logger;
import org.lolwat.managers.types.QuestTask;

import java.util.ArrayList;
import java.util.List;

public class CooksAssistantQuest implements QuestTask {
    @Override
    public void execute() {
        int state = PlayerSettings.getBitValue(29);
        switch(state) {
            default: {
                Logger.log("Unhandled value: " + state);
                break;
            }

            case 0: {
                Logger.log("Not started?");
                break;
            }

            case 1: {
                Logger.log("Started?");
                break;
            }
        }
    }

    @Override
    public Quest completes() {
        return FreeQuest.COOKS_ASSISTANT;
    }

    @Override
    public boolean canPerformTask() {
        return true; // no requirements
    }

    @Override
    public List<String> inventoryTolerated() {
        return new ArrayList<String>() {{
            add("Pot of flour");
            add("Egg");
            add("Bucket of milk");
        }};
    }
}
