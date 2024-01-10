package org.lolwat.tasks.types.quests;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.WatAIO;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.misc.BankingTask;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//TODO - buy raw beef from GE, cook it twice at range by hetty
//TODO - buy eye of newt from port sarim magic shop
//TODO - get rat's tail by killing any rat only after starting quest
public class WitchsPotionQuest implements WatTask{

    private final Area startLocation = new Area(2973, 3206, 2964, 3201);
    List<String> startDialogue = Arrays.asList("I'm looking for a quest.", "Yes.");

    private HashMap<String, Integer> needed = new HashMap<String,Integer>(){ {
        put("Raw beef", 1);
        put("Eye Of Newt", 1);
        put("Onion", 1);
        // put("Rat's tail", 1);
    } };

    public WitchsPotionQuest(){
    }

    @Override
    public void execute(WatAIO instance){
        //check for items
        if (!Inventory.containsAll(needed.keySet())) {
            instance.currentTask = new BankingTask(needed, new HashMap<>(), 1, this);
            return;
        }

        //check for burnt meat, buy raw beef from GE and cook it twice at range by hetty if needed

        //go to start location
        if (!startLocation.contains(Players.getLocal())) {
            instance.currentTask = new TraversalTask(startLocation, this);
            return;
        }

        if (NPCs.closest("Hetty") != null) {
            // handle the dialogue here
            if (Dialogues.inDialogue()) {
                while (Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(startDialogue);
                }
            } else {
                NPCs.closest("Hetty").interact();
            }
        }
    }

    @Override
    public String getName() {
        return "Witch's Potion";
    }

    @Override
    public boolean canPerformTask() {
        return !Quests.isFinished(FreeQuest.WITCHS_POTION) || Quests.isStarted(FreeQuest.WITCHS_POTION);
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return 650;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

    }

    @Override
    public Skill trainsSkill() {
        return null;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public Quest completesQuest() {
        return FreeQuest.WITCHS_POTION;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return GenericUtils.getSkillingGear();
    }

}