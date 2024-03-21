package org.lolwat.tasks.types.gpfarming;

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
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.lolwat.tasks.WatTask;
import org.lolwat.WatAIO;

import java.util.HashMap;

public class TanningTask implements WatTask {
    private final Area cowPastureEast = new Area(3253, 3258, 3296, 3299);
    private final Area cowPastureWest = new Area(3191, 3273, 3229, 3298);
    private final Area bankArea = new Area(3269, 3161, 3272, 3170);
    private final Area tannerArea = new Area(3273, 3189, 3277, 3192);
    private final Tile ellisLocation = new Tile(3275, 3193, 0);

    @Override
    public void execute(WatAIO instance) {
        if (Inventory.isFull()) {
            if (!bankArea.contains(Players.getLocal())) {
                Walking.walk(bankArea.getRandomTile());
                Sleep.sleepUntil(() -> bankArea.contains(Players.getLocal()), 5000, 600);
            } else {
                if (Bank.open()) {
                    int cowhideCount = Inventory.count("Cowhide");
                    if (!Inventory.contains("Coins") || Inventory.count("Coins") < cowhideCount * 3) {
                        Bank.withdraw("Coins", cowhideCount * 3 - Inventory.count("Coins"));
                        Sleep.sleepUntil(() -> Inventory.count("Coins") >= cowhideCount * 3, 5000, 600);
                    }
                    Bank.close();
                    Walking.walk(ellisLocation);
                    Sleep.sleepUntil(() -> tannerArea.contains(Players.getLocal()), 5000, 600);
                }
            }
        } else {
            if (!cowPastureEast.contains(Players.getLocal()) && !cowPastureWest.contains(Players.getLocal())) {
                Walking.walk(cowPastureEast.getRandomTile());
                Sleep.sleepUntil(() -> cowPastureEast.contains(Players.getLocal()) || cowPastureWest.contains(Players.getLocal()), 5000, 600);
            } else {
                GroundItem cowhide = GroundItems.closest("Cowhide");
                if (cowhide != null && cowhide.interact("Take")) {
                    Sleep.sleepUntil(() -> !cowhide.exists() || Inventory.isFull(), 1000, 600);
                }
            }
        }

        if (tannerArea.contains(Players.getLocal()) && Inventory.contains("Cowhide")) {
            NPC ellis = NPCs.closest("Ellis");
            if (ellis != null && ellis.interact("Trade")) {
                Sleep.sleepUntil(() -> Widgets.getWidget(324) != null && Widgets.getWidget(324).isVisible(), 5000);
                if (Widgets.getWidget(324) != null && Widgets.getWidget(324).isVisible()) {
                    Widgets.getWidget(324).getChild(137).interact("Tan All");
                    Sleep.sleepUntil(() -> !Inventory.contains("Cowhide"), 15000, 600);
                }
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
        return Skill.CRAFTING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return null; // Optional implementation to avoid task after a certain level
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