package org.lolwat.tasks.types.misc;

import org.dreambot.api.input.Keyboard;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.misc.utils.WebUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.WatAIO;

import java.util.HashMap;

import static org.dreambot.api.utilities.Sleep.sleepUntil;

public class GEBeggingTask implements WatTask {

    @Override
    public String getName() {
        return "GE Begging";
    }

    private Tile[] perimeterTiles; // Tiles along the perimeter of the begging area
    private long nextBegTime = System.currentTimeMillis(); // Initialize to current time to start the process immediately
    private String tradeName = null;
    public GEBeggingTask() {
        initializePerimeterTiles();
    }

    private void initializePerimeterTiles() {
        // Populate this array with the tiles around the begging zone
        perimeterTiles = new Tile[]{
                new Tile(3161, 3494, 0),
                new Tile(3170, 3495, 0),
                new Tile(3171, 3489, 0),
                new Tile(3168, 3483, 0),
                new Tile(3160, 3489, 0),
                new Tile(3156, 3488, 0),
                new Tile(3160, 3484, 0),
                new Tile(3164, 3481, 0),
                new Tile(3161, 3486, 0),
                new Tile(3158, 3492, 0),
                new Tile(3168, 3489, 0),
                new Tile(3171, 3485, 0),
                new Tile(3163, 3497, 0)
        };
    }

    @Override
    public void execute(WatAIO instance) {
        long currentTime = System.currentTimeMillis();
        if (currentTime >= nextBegTime || nextBegTime == 0) {
            moveToNextPerimeterTileRandomly();
            performBegging();
            calculateNextBegTime();
        }
    }

    private void moveToNextPerimeterTileRandomly() {
        if (perimeterTiles.length > 0) {
            int randomIndex = Calculations.random(0, perimeterTiles.length - 1);
            Tile nextTile = perimeterTiles[randomIndex];
            Walking.walk(nextTile);
            Logger.log("Moving to new location for begging: " + nextTile.toString());
        }
    }

    private void calculateNextBegTime() {
        long delay = Calculations.random(45000, 90000); // .75 to 90 minutes in milliseconds
        nextBegTime = System.currentTimeMillis() + delay;
    }

    private String getBeggingMessage() {
        String response = WebUtils.getRealResponse("noname", "nomsg", "begging");
        Logger.log("Begging Message: " + response);
        return response;
    }

    private String getThankYouMessage() {
        String response = WebUtils.getRealResponse(tradeName,"Here, have something of value", "begging");
        Logger.log("Thank You Message: " + response);
        return response;
    }

    private void performBegging() {
        String beggingMessage = getBeggingMessage();
        if (beggingMessage != "") {
            Keyboard.type(beggingMessage, true);
        } else {
            Logger.log("Failed to generate a begging message.");
            return;
        }

        // Wait for someone to trade with us
        if (!Trade.isOpen()) {
            long startTime = System.currentTimeMillis();
            while (!Trade.isOpen() && (System.currentTimeMillis() - startTime) < 90000) { // Wait for 1.5 minutes max
                Sleep.sleep(Calculations.random(1000, 3000));
            }
        } else if (Trade.isOpen() && !Trade.isOpen(2)) {
            // Accept the first trade screen
            Trade.acceptTrade();
            //Track name of player trading with
            tradeName = Trade.getTradingWith();
        } else if (Trade.isOpen() && Trade.isOpen(2)) {
            // Accept the second trade screen
            Trade.acceptTrade();
            sleepUntil(() -> !Trade.isOpen(), 5000);

            // Confirm the trade was successful and thank the donor
            if (!Trade.isOpen()) {
                String thankYouMessage = getThankYouMessage();
                Keyboard.type(thankYouMessage, true);

                // Now, bank the GP
                if (!Bank.isOpen()) {
                    Bank.open();
                }

                if (Bank.isOpen()) {
                    Bank.depositAllExcept(item -> item != null && item.getName().contains("Coins"));
                    Bank.close();
                }
            }
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return 5000;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.HITPOINTS;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public Quest completesQuest() {
        return null;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}