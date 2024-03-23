package org.lolwat.tasks.types.misc;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.lolwat.managers.TaskManager;
import org.lolwat.tasks.WatTask;
import org.lolwat.WatAIO;

import java.util.HashMap;

public class TanningTask implements WatTask {
    private final Area cowPastureEast = new Area(
            new Tile[] {
                    new Tile(3240, 3298, 0),
                    new Tile(3241, 3299, 0),
                    new Tile(3256, 3299, 0),
                    new Tile(3257, 3300, 0),
                    new Tile(3260, 3300, 0),
                    new Tile(3261, 3299, 0),
                    new Tile(3263, 3299, 0),
                    new Tile(3265, 3297, 0),
                    new Tile(3265, 3255, 0),
                    new Tile(3253, 3255, 0),
                    new Tile(3253, 3272, 0),
                    new Tile(3251, 3274, 0),
                    new Tile(3251, 3276, 0),
                    new Tile(3249, 3278, 0),
                    new Tile(3246, 3278, 0),
                    new Tile(3244, 3280, 0),
                    new Tile(3244, 3281, 0),
                    new Tile(3240, 3285, 0),
                    new Tile(3240, 3287, 0),
                    new Tile(3241, 3288, 0),
                    new Tile(3241, 3289, 0),
                    new Tile(3242, 3290, 0),
                    new Tile(3242, 3293, 0),
                    new Tile(3241, 3294, 0),
                    new Tile(3241, 3295, 0),
                    new Tile(3240, 3296, 0),
                    new Tile(3240, 3298, 0)
            }
    );
    private final Area cowPastureWest = new Area(
            new Tile[] {
                    new Tile(3193, 3301, 0),
                    new Tile(3195, 3303, 0),
                    new Tile(3199, 3303, 0),
                    new Tile(3200, 3302, 0),
                    new Tile(3205, 3302, 0),
                    new Tile(3206, 3303, 0),
                    new Tile(3209, 3303, 0),
                    new Tile(3210, 3302, 0),
                    new Tile(3210, 3297, 0),
                    new Tile(3211, 3296, 0),
                    new Tile(3211, 3295, 0),
                    new Tile(3213, 3293, 0),
                    new Tile(3213, 3290, 0),
                    new Tile(3212, 3289, 0),
                    new Tile(3212, 3285, 0),
                    new Tile(3211, 3284, 0),
                    new Tile(3206, 3284, 0),
                    new Tile(3205, 3283, 0),
                    new Tile(3200, 3283, 0),
                    new Tile(3199, 3282, 0),
                    new Tile(3196, 3282, 0),
                    new Tile(3195, 3283, 0),
                    new Tile(3195, 3284, 0),
                    new Tile(3193, 3286, 0),
                    new Tile(3193, 3301, 0)
            }
    );
    private final Area tannerArea = new Area(
            new Tile[] {
                    new Tile(3270, 3194, 0),
                    new Tile(3277, 3194, 0),
                    new Tile(3277, 3190, 0),
                    new Tile(3270, 3190, 0)
            }
    );
    private final Tile ellisLocation = new Tile(3275, 3193, 0);
    private final Area bankArea = new Area(
            new Tile[] {
                    new Tile(3268, 3170, 0),
                    new Tile(3268, 3164, 0),
                    new Tile(3272, 3164, 0),
                    new Tile(3272, 3170, 0),
                    new Tile(3268, 3170, 0)
            }
    );
    private long lastCowhideCollectionTime;

    @Override
    public void execute(WatAIO instance) {

        if (Inventory.isFull()) {
            if (!Bank.isOpen()) {
                Walking.walk(bankArea.getRandomTile());
                Sleep.sleepUntil(() -> bankArea.contains(Players.getLocal()), 5000);
                Bank.open();
                Sleep.sleepUntil(Bank::open, 5000);
            } else {
                handleBanking();
            }
        } else {
            collectCowhides();
        }

        if (tannerArea.contains(Players.getLocal()) && Inventory.contains("Cowhide")) {
            tradeAndTanCowhides();
        }

        //if (notEnoughMmItems("Cowhide", 240000)); {
        //    TaskManager.getInstance().setCurrentTask(new HopperTask(0, this));
        //}
    }

    private void handleBanking() {
        int cowhideCount = Inventory.count("Cowhide");
        if (!Inventory.contains("Coins") || Inventory.count("Coins") < cowhideCount * 3) {
            Bank.withdraw("Coins", cowhideCount * 3 - Inventory.count("Coins"));
            Sleep.sleepUntil(() -> Inventory.count("Coins") >= cowhideCount * 3, 5000);
        }
        Bank.close();
        Walking.walk(ellisLocation);
        Sleep.sleepUntil(() -> tannerArea.contains(Players.getLocal()), 5000);
    }

    private void collectCowhides() {
        if (!cowPastureEast.contains(Players.getLocal()) && !cowPastureWest.contains(Players.getLocal())) {
            Walking.walk(cowPastureEast.getRandomTile());
            Sleep.sleepUntil(() -> cowPastureEast.contains(Players.getLocal()) || cowPastureWest.contains(Players.getLocal()), 5000);
        } else {
            GroundItem cowhide = GroundItems.closest("Cowhide");
            if (cowhide != null) {
                int inventoryCountBefore = Inventory.count("Cowhide");
                if (cowhide.interact("Take")) {
                    Sleep.sleepUntil(() -> {
                        if (Inventory.count("Cowhide") > inventoryCountBefore || !cowhide.exists()) {
                            lastCowhideCollectionTime = System.currentTimeMillis();
                            return true;
                        }
                        return false;
                    }, 20000);
                }
            }
        }
    }

    private void tradeAndTanCowhides() {
        NPC ellis = NPCs.closest("Ellis");
        if (ellis != null && ellis.interact("Trade")) {
            Sleep.sleepUntil(() -> Widgets.getWidget(324) != null && Widgets.getWidget(324).isVisible(), 5000);
            if (Widgets.getWidget(324) != null && Widgets.getWidget(324).isVisible()) {
                Widgets.getWidget(324).getChild(137).interact("Tan All");
                Sleep.sleepUntil(() -> !Inventory.contains("Cowhide"), 15000);
            }
        }
    }

    @Override
    public String getName() {
        return "Cowhide Tanning";
    }

    @Override
    public boolean canPerformTask() {
        return true; // Could implement level checks or other conditions
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return Calculations.random(600, 1200); // Randomize to mimic human behavior
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {
        // Optional implementation for tracking experience gains
    }

    @Override
    public Skill trainsSkill() {
        return Skill.HITPOINTS;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101; // Optional implementation to avoid task after a certain level
    }

    @Override
    public Quest completesQuest() {
        return null; // This task does not complete a quest
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>(); // Define required clothes if any
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>(); // Define required inventory items if any
    }
}