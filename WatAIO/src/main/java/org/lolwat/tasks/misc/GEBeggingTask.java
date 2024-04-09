package org.lolwat.tasks.misc;

import org.dreambot.api.input.Keyboard;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.dreambot.api.wrappers.widgets.message.MessageType;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.WebUtils;
import org.lolwat.managers.types.WatTask;
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
    private boolean hasWalkedToGE = false;
    private final Area grandExchangeArea = new Area(
            new Tile[] {
                    new Tile(3161, 3499, 0),
                    new Tile(3168, 3499, 0),
                    new Tile(3174, 3494, 0),
                    new Tile(3174, 3486, 0),
                    new Tile(3169, 3481, 0),
                    new Tile(3160, 3481, 0),
                    new Tile(3155, 3486, 0),
                    new Tile(3155, 3493, 0),
                    new Tile(3163, 3492, 0),
                    new Tile(3166, 3492, 0),
                    new Tile(3167, 3491, 0),
                    new Tile(3167, 3488, 0),
                    new Tile(3163, 3488, 0),
                    new Tile(3162, 3489, 0),
                    new Tile(3162, 3492, 0),
                    new Tile(3155, 3493, 0)
            }
    );
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
        if (!hasWalkedToGE) {
            walkToGE();
        } else {
            long currentTime = System.currentTimeMillis();
            if (currentTime >= nextBegTime) {
                performBegging(); // Ensure performBegging can be called independently
                calculateNextBegTime();
            }
            if (Calculations.random(1, 100) <= 50) { // 50% chance to move after begging
                moveToNextPerimeterTileRandomly();
            }
        }
    }

    private void walkToGE() {
        if (!grandExchangeArea.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(grandExchangeArea, this));
            Sleep.sleepUntil(() -> grandExchangeArea.contains(Players.getLocal()), 5000);
            hasWalkedToGE = true;
        } else if (grandExchangeArea.contains(Players.getLocal())) {
            hasWalkedToGE = true;
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
        long delay = Calculations.random(10000, 30000); // 30 seconds to 1 minute in milliseconds
        nextBegTime = System.currentTimeMillis() + delay;
    }

    private String getBeggingMessage() {
        String response = WebUtils.getRealResponse("noname", "nomsg", "begging").replace("\"", "");
        Logger.log("Begging Message: " + response);
        return response;
    }

    private String getThankYouMessage() {
        String response = WebUtils.getRealResponse(tradeName,"Here, have something of value", "begging").replace("\"", "");
        Logger.log("Thank You Message: " + response);
        return response;
    }

    private void performBegging() {
        if(Bank.isOpen()) {
            Bank.close();
            Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);
        }

        String beggingMessage = getBeggingMessage();
        if (!beggingMessage.isEmpty()) {
            Keyboard.type(beggingMessage, true);
        } else {
            Logger.log("Failed to generate a begging message.");
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

    private boolean attemptTradeWithPlayer(Player trader, String traderName) {
        if (trader == null) {
            Logger.log("Player " + traderName + " not found.");
            return false;
        }

        // Ensure the player is on screen and interactable
        if (!trader.isOnScreen()) {
            Camera.rotateToEntity(trader);
        }

        int attempts = 0;
        while (attempts < 3) {
            // Ensure player is still valid for interaction
            if (trader != null && trader.isOnScreen()) {
                boolean initiated = trader.interact("Trade with");

                if (initiated) {
                    Sleep.sleepUntil(() -> Trade.isOpen(), 5000); // Adjust based on latency
                    if (Trade.isOpen()) {
                        Logger.log("Trade window opened with " + traderName);
                        return true;
                    } else {
                        Logger.log("Trade window did not open. Trade might be declined by " + traderName);
                    }
                } else {
                    Logger.log("Failed to interact for trade with " + traderName);
                }
            } else {
                Logger.log("Player " + traderName + " is not on screen or not interactable. Attempt: " + attempts);
                // Re-acquire the player reference in case they've moved
                trader = Players.closest(p -> p != null && p.getName().equalsIgnoreCase(traderName));
            }

            attempts++;
            Sleep.sleep(1000); // Wait a bit before retrying
        }

        Logger.log("Failed to initiate trade with " + traderName + " after " + attempts + " attempts.");
        return false;
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
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }

    @Override
    public void onMessage(Message message) {
        // Check if the message is a trade request
        if (message.getType() == MessageType.TRADE && message.getMessage().contains("wishes to trade with you.")) {
            // Extract the name of the player who sent the trade request
            String traderName = message.getUsername();

            // Log for debugging
            Logger.log("Trade request received from: " + traderName);

            // Locate the player who sent the trade request
            Player trader = Players.closest(player -> player != null && player.getName().equalsIgnoreCase(traderName));

            if (trader != null) {
                // Attempt to trade with the player
                boolean tradeAttempted = attemptTradeWithPlayer(trader, traderName);
                if (tradeAttempted) {
                    Logger.log("Trade attempt with " + traderName + " was successful.");
                } else {
                    Logger.log("Failed to initiate trade with " + traderName);
                }
            } else {
                Logger.log("Could not find player: " + traderName);
            }
        }
    }
}