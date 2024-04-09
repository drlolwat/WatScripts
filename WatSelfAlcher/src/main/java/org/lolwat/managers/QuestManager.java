package org.lolwat.managers;

import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.lolwat.managers.types.QuestTask;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.quests.CooksAssistantQuest;

import java.util.HashMap;

public class QuestManager {
    private static QuestManager instance;
    private HashMap<Quest, QuestTask> questTasks;

    public QuestManager() {
        setupQuests();
    }

    public Quest getIncompleteQuest() {
        GenericUtils.shuffleHashMap(questTasks);
        for (Quest q : questTasks.keySet()) {
            if (!Quests.isFinished(q)) {
                return q;
            }
        }
        return null;
    }

    public QuestTask getQuestTask(Quest q) {
        return questTasks.get(q);
    }

    private void setupQuests() {
        questTasks = new HashMap<>();
        questTasks.put(FreeQuest.COOKS_ASSISTANT, new CooksAssistantQuest());
    }

    public static QuestManager getInstance() {
        return instance;
    }

    public static void setInstance(QuestManager inst) {
        instance = inst;
    }
}
