package org.lolwat.tasks.gathering;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.ItemManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.ZoneManager;
import org.lolwat.misc.utils.WatUtils;
import org.lolwat.tasks.banking.WithdrawItemsTask;
import org.lolwat.tasks.gathering.gearing.GatheringGearTask;
import org.lolwat.tasks.misc.WalkingTask;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.gear.WatTool;
import org.lolwat.types.gear.WatZone;
import org.lolwat.types.interfaces.WatTask;

import java.util.HashMap;

public class GatheringTask implements WatTask {
    private final Skill training;

    public GatheringTask(Skill s) {
        this.training = s;
    }
    @Override
    public void execute() {
        boolean hasTool = false;

        for(Item i : Equipment.all()) {
            if(i == null) continue;
            if(ItemManager.getInstance().isValidTool(i.getName(), training)) {
                hasTool = true;
                break;
            }
        }

        if(!hasTool) {
            for(Item i : Inventory.all()) {
                if(i == null) continue;
                if(ItemManager.getInstance().isValidTool(i.getName(), training)) {
                    hasTool = true;
                    break;
                }
            }
        }

        if(!hasTool) {
            Logger.log("need to fetch tool for gathering (" + training + ")");
            TaskManager.getInstance().setCurrentTask(new GatheringGearTask(training, this));
            return;
        }

        WatTool bestTool = ItemManager.getInstance().getBestTool(this.training);
        if(Inventory.isFull()) {
            if(training.equals(Skill.MINING)) {
                for(Item it : Inventory.all()) {
                    if (it == null) continue;
                    if (!ItemManager.getInstance().isValidTool(it.getName(), Skill.MINING)) {
                        int count = Inventory.size();
                        if (!Inventory.drop(it.getName())) {
                            Logger.log("Failed to drop ore");
                            continue;
                        }

                        Sleep.sleepUntil(() -> Inventory.size() < count, 2000);
                    }
                }
            } else {
                TaskManager.getInstance().setCurrentTask(new WithdrawItemsTask(
                        new HashMap<WatItem, Integer>() {
                            {
                                put(bestTool, 1);
                            }
                        }, this
                ));
                return;
            }
        }

        if(!Equipment.contains(x -> x != null && x.getName().contains(bestTool.getName()))) {
            if(!Inventory.contains(x -> x.getName().contains(bestTool.getName()))) {
                Logger.log("we need to get the tool for the job: " + bestTool.getName());
                TaskManager.getInstance().setCurrentTask(new WithdrawItemsTask(
                        new HashMap<WatItem, Integer>() {
                            {
                                put(bestTool, 1);
                            }
                        }, this
                ));
            } else {
                if(WatUtils.canEquipTool(bestTool)) {
                    if(!WatUtils.equipItem(bestTool.getName())) {
                        Logger.error("problem equipping " + bestTool.getName());
                        return;
                    }
                }
            }
        }

        WatZone bestZone = ZoneManager.getInstance().getBestZone(training);
        if(!bestZone.getSearchArea().contains(Players.getLocal())) {
            Logger.log("We are walking to the best " + training + " area for " + bestZone.getObjectName());
            TaskManager.getInstance().setCurrentTask(new WalkingTask(bestZone.getSearchArea(), this));
            return;
        }

        if(!bestZone.isNpc()) {
            GameObject bestObject = GameObjects.closest(x -> x.getName().contains(bestZone.getObjectName()) && x.canReach());
            if (bestObject != null) {
                if (!bestObject.interact(bestZone.getContextSearch())) {
                    Logger.error("problem interacting with object " + bestObject.getName());
                    return;
                }

                int inventoryCount = Inventory.size();
                Sleep.sleepUntil(() -> Dialogues.canContinue() || Inventory.isFull() ||
                        (training == Skill.MINING && (Inventory.size() > inventoryCount || bestObject.getModelColors() == null)) ||
                        (training == Skill.WOODCUTTING && !bestObject.exists()), 15000);
            }
        } else {
            // handle npcs (fishing)
        }
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return training;
    }

    @Override
    public Integer avoidAfterLevel() {
        return ConfigManager.getInstance().getSkillTarget(training);
    }
}
