package org.lolwat.tasks.herblore;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.PaidQuest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.WatAIO;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.herblore.HerbUtils;
import org.lolwat.tasks.misc.BankingTask;

import java.util.HashMap;
import java.util.List;

public class CleanHerbTask implements WatTask {
    int maxLevel;
    int sell;

    public CleanHerbTask() {
        maxLevel = ConfigManager.getInstance().getSkillTarget(Skill.HERBLORE);
    }

    @Override
    public String getName() {
        return "Cleaning Herbs";
    }

    @Override
    public boolean canPerformTask() {
        return Quests.isFinished(PaidQuest.DRUIDIC_RITUAL);
    }

    @Override
    public void execute() {
        String bestHerb = HerbUtils.bestHerbToClean();
        String toSell = GenericUtils.uppercaseFirst(bestHerb.replace("Grimy", "").trim());

        if(Skills.getRealLevel(Skill.HERBLORE) > 10) {
            sell = HerbUtils.herbsRequiredToLevel()-Calculations.random(1, 30);
        } else {
            sell = Calculations.random(3, 10) * 28;
        }

        if(sell <= 0) {
            sell = 28;
        }

        Logger.log("Cleaning herb: " + bestHerb + ", selling " + toSell + " at " + sell + "qty");

        if(!Inventory.contains(bestHerb) || Inventory.get(x -> x != null && x.getName().equalsIgnoreCase(bestHerb)).isNoted()) {
            int finalSell = sell;
            TaskManager.getInstance().setCurrentTask(new BankingTask(null, new HashMap<String, Integer>()
            {
                {
                    put(toSell, -finalSell);
                }
            }, 1, this));

            return;
        }

        if(Bank.isOpen()) {
            Bank.close();
            Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);
        }

        Item herb = Inventory.get(x -> x != null && x.getName().equalsIgnoreCase(bestHerb));
        if(herb != null && herb.interact("Clean")) {
            Sleep.sleepUntil(() -> Dialogues.inDialogue() || !Inventory.contains(bestHerb), 35000);
        }
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public int loopTime() {
        return 400;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

    }

    @Override
    public Skill trainsSkill() {
        return Skill.HERBLORE;
    }

    @Override
    public Integer avoidAfterLevel() {
        return maxLevel;
    }

    

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<String, Integer>() {
            {
                put(HerbUtils.bestHerbToClean(), -HerbUtils.herbsRequiredToLevel());
            }
        };
    }

    @Override
    public List<String> inventoryTolerated() {
        return WatTask.super.inventoryTolerated();
    }

    @Override
    public boolean requiresMembers() {
        return true;
    }
}
