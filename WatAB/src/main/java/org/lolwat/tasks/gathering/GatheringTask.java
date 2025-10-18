package org.lolwat.tasks.gathering;

import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
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
    private long lastUpgradeCheck = 0;
    private static final long UPGRADE_CHECK_INTERVAL = 60 * 10; //TODO add to config

    public GatheringTask(Skill s) {
        this.training = s;
    }

    @Override
    public String getName() {
        return "Training " + training.toString().toLowerCase();
    }

    @Override
    public String getLocation() {
        return ZoneManager.getInstance().getBestZone(training).getName();
    }

    @Override
    public void execute() {
        WatTool bestTool = ItemManager.getInstance().getBestTool(training);
        WatZone bestZone = ZoneManager.getInstance().getBestZone(training);

        if(bestZone.isGatheringItemRequired()) {
            boolean hasTool = false;
            String toolOwned = "";

            for (Item i : Equipment.all()) {
                if (i == null) continue;
                if (ItemManager.getInstance().isValidTool(i.getName(), training)) {
                    hasTool = true;
                    toolOwned = i.getName();
                    break;
                }
            }

            if (!hasTool) {
                for (Item i : Inventory.all()) {
                    if (i == null) continue;
                    if (ItemManager.getInstance().isValidTool(i.getName(), training)) {
                        hasTool = true;
                        toolOwned = i.getName();
                        break;
                    }
                }
            }

            if (!hasTool) {
                Logger.log("need to fetch tool for gathering (" + training + ")");
                TaskManager.getInstance().setCurrentTask(new GatheringGearTask(training, this));
                return;
            }

            if (Bank.isCached()) {
                long now = System.currentTimeMillis();
                if (now - lastUpgradeCheck > UPGRADE_CHECK_INTERVAL) {
                    lastUpgradeCheck = now;
                    if (!toolOwned.equals(bestTool.getName())) {
                        Logger.log("we need to upgrade our gathering item");
                        TaskManager.getInstance().setCurrentTask(new GatheringGearTask(training, this));
                        return;
                    }
                }
            }
        }

        if (Inventory.isFull()) {
            if (training.equals(Skill.MINING) || training.equals(Skill.WOODCUTTING)) {
                for (Item it : Inventory.all()) {
                    if (it == null) continue;
                    WatItem wi = ItemManager.getInstance().getItem(it.getName());
                    if(wi != null && wi.getPrice() >= 5000) continue;
                    if (!ItemManager.getInstance().isValidTool(it.getName(), training)) {
                        int count = Inventory.size();
                        if (!Inventory.drop(it.getName())) {
                            Logger.log("Failed to drop during " + training.toString().toLowerCase() + " task");
                            continue;
                        }

                        Sleep.sleepUntil(() -> Inventory.size() < count, 2000);
                    }
                }
            } else {
                TaskManager.getInstance().setCurrentTask(new WithdrawItemsTask(
                        new HashMap<WatItem, Integer>() {
                            {
                                if(bestZone.isGatheringItemRequired()) {
                                    put(bestTool, 1);
                                }
                            }
                        }, this
                ));
                return;
            }
        }

        if(bestZone.isGatheringItemRequired()) {
            if (!Equipment.contains(x -> x != null && x.getName().contains(bestTool.getName()))) {
                if (!Inventory.contains(x -> x.getName().contains(bestTool.getName()))) {
                    Logger.log("we need to get the tool for the job: " + bestTool.getName());
                    TaskManager.getInstance().setCurrentTask(new WithdrawItemsTask(
                            new HashMap<WatItem, Integer>() {
                                {
                                    put(bestTool, 1);
                                }
                            }, this
                    ));
                } else {
                    if (WatUtils.canEquipTool(bestTool)) {
                        if (!WatUtils.equipItem(bestTool.getName())) {
                            Logger.error("problem equipping " + bestTool.getName());
                            return;
                        }
                    }
                }
            }
        }

        if(bestZone.isFoodRequired()) {
            Item i = Inventory.get(x -> x != null && x.hasAction("Eat"));
            if(i != null) {
                if(Combat.getHealthPercent() <= 50) {
                    if (!i.interact("Eat")) {
                        Logger.log("Issue eating food during gathering task");
                    }
                }
            } else {
                Logger.log("we need to get food for this gathering zone");
                TaskManager.getInstance().setCurrentTask(new WithdrawItemsTask(
                        new HashMap<WatItem, Integer>() {
                            {
                                put(ItemManager.getInstance().getItem("Tuna"), 12);
                            }
                        }, this
                ));
                return;
            }
        }

        if (!bestZone.getSearchArea().contains(Players.getLocal())) {
            Logger.log("We are walking to the best " + training + " area for " + bestZone.getObjectName());
            TaskManager.getInstance().setCurrentTask(new WalkingTask(bestZone.getSearchArea(), this));
            return;
        }

        if (!bestZone.isNpc()) {
            GameObject bestObject = GameObjects.closest(x -> x.getName().equals(bestZone.getObjectName()) && x.canReach());
            if (bestObject != null) {
                if (!bestObject.interact(bestZone.getContextSearch())) {
                    Logger.error("problem interacting with object " + bestObject.getName());
                    return;
                }

                int inventoryCount = Inventory.size();
                Sleep.sleepUntil(() -> Dialogues.canContinue() || Inventory.isFull() ||
                                (training.equals(Skill.MINING) && (Inventory.size() > inventoryCount || bestObject.getModelColors() == null)) ||
                                (training.equals(Skill.WOODCUTTING) && !bestObject.exists()),
                        (training.equals(Skill.MINING) ? 15000 : 60000));
            }
        } else {
            NPC bestNpc = NPCs.closest(x -> x != null && x.exists() && x.getName().equals(bestZone.getObjectName()) && x.canReach() && bestZone.getSearchArea().contains(x));
            if(bestNpc != null) {
                if(!bestNpc.interact(bestZone.getContextSearch())) {
                    Logger.error("problem interacting with " + bestNpc.getName() + ": " + bestZone.getContextSearch());
                    return;
                }

                int inventoryCount = Inventory.size();
                if (training.equals(Skill.THIEVING)) {
                    Sleep.sleepUntil(() -> Dialogues.canContinue() || Inventory.isFull() || Inventory.size() != inventoryCount ||
                            (!Players.getLocal().isAnimating() && !Players.getLocal().isHealthBarVisible() && !Players.getLocal().isMoving()), 5000);
                } else {
                    Sleep.sleepUntil(() -> Dialogues.canContinue() || Inventory.isFull() || Inventory.size() != inventoryCount,10000);
                }
            }
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
