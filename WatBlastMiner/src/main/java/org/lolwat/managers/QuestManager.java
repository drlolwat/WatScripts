package org.lolwat.managers;

import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.lolwat.managers.types.QuestTask;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.quests.*;

import java.util.HashMap;
import java.util.Map;

public class QuestManager {
    private static QuestManager instance;
    private HashMap<Quest, QuestTask> questTasks;

    public QuestManager() {
        setupQuests();
    }

    public Quest getIncompleteQuest() {
        GenericUtils.shuffleHashMap(questTasks);
        for (Map.Entry<Quest, QuestTask> qt : questTasks.entrySet()) {
            if (!Quests.isFinished(qt.getKey()) && qt.getValue().canPerformTask()) {
                return qt.getKey();
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
        questTasks.put(FreeQuest.DORICS_QUEST, new DoricsQuest());
        questTasks.put(FreeQuest.IMP_CATCHER, new ImpCatcherQuest());
        questTasks.put(FreeQuest.GOBLIN_DIPLOMACY, new GoblinDiplomacyQuest());
        questTasks.put(FreeQuest.ROMEO_AND_JULIET, new RomeoAndJulietQuest());
        questTasks.put(FreeQuest.SHEEP_SHEARER, new SheepShearerQuest());
        questTasks.put(FreeQuest.WITCHS_POTION, new WitchsPotionQuest());
    }

    public static QuestManager getInstance() {
        return instance;
    }

    public static void setInstance(QuestManager inst) {
        instance = inst;
    }
}
