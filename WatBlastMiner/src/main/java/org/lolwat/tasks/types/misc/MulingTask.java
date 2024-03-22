package org.lolwat.tasks.types.misc;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.tasks.WatTask;
import org.lolwat.WatAIO;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class MulingTask implements WatTask {
    String name;
    boolean active;
    String target;
    long lastSentRequest = 0;
    int originalWorld;
    boolean completed = false;
    int retries = 0;
    boolean reverse;
    HashMap<String, Integer> reverseRequest;
    private final WatTask postTask;

    public MulingTask(String taskName, int currentWorld, WatTask post) {
        name = taskName;
        active = false;
        target = "";
        originalWorld = currentWorld;
        reverse = false;
        reverseRequest = new HashMap<>();
        postTask = post;
    }

    public MulingTask(String taskName, int currentWorld, HashMap<String, Integer> request, WatTask post) {
        name = taskName;
        active = false;
        target = "";
        originalWorld = currentWorld;
        reverse = true;
        reverseRequest = request;
        postTask = post;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute(WatAIO instance) {
        if (Inventory.isFull()) {
            Logger.log("Inventory is full. Depositing items except for coins.");
            if (!Bank.open(BankLocation.GRAND_EXCHANGE)) {
                Logger.log("Unable to open bank at Grand Exchange.");
                return;
            }

            Sleep.sleepUntil(Bank::isOpen, 5000);

            if (Bank.isOpen()) {
                // Deposit all items except coins
                Bank.depositAllExcept("Coins");
                Sleep.sleepUntil(() -> !Inventory.isFull(), 3000);
            } else {
                Logger.log("Failed to open bank.");
                return;
            }

            Logger.log("Items deposited. Resuming muling task.");
        }

        if(NPCs.closest("Grand Exchange Clerk") == null) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(BankLocation.GRAND_EXCHANGE.getTile(), false, this));
            return;
        }

        if(completed) {
            TaskManager.getInstance().setCurrentTask(new HopperTask(originalWorld, postTask));
            return;
        }

        if(!active) {
            if(retries > 5) {
                ConfigManager.getInstance().setMuleConnectionFailed(true);
                // put GP back
                if(!Bank.isOpen()) {
                    Bank.open();
                    Sleep.sleepUntil(Bank::isOpen, 10000);

                    if(Inventory.contains("Coins")) {
                        Bank.depositAll("Coins");
                    }

                    Sleep.sleepUntil(() -> !Inventory.contains("Coins"), 5000);
                    Bank.close();
                }

                if(!Inventory.contains("Coins")) {
                    TaskManager.getInstance().getNewTask();
                }

                return;
            }

            StringBuilder message = new StringBuilder("READY-REGULAR|" + Players.getLocal().getName());
            if(reverse) {
                message = new StringBuilder("READY-REVERSE|" + Players.getLocal().getName() + "|");
                for(Map.Entry<String, Integer> kvp : reverseRequest.entrySet()) {
                    message.append(kvp.getKey()).append(":").append(kvp.getValue());
                    message.append(";");
                }
            }

            // REVERSE MULING REQUEST GOES:
            // READY-REVERSE|Username|Coins:100000

            Logger.log("Sending mule connection");
            try (Socket socket = new Socket("localhost", 8081)) {
                byte[] messageBytes = message.toString().getBytes(StandardCharsets.UTF_8);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(messageBytes);
                outputStream.flush();

                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[1024];
                int bytesRead = inputStream.read(buffer);
                String response = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                if (response.startsWith("OK")) {
                    active = true;
                    int targetWorld = 0;
                    String[] sp = response.split("\\|");
                    if (sp.length > 0) {
                        target = sp[1];
                        targetWorld = Integer.parseInt(sp[2]);
                    }

                    if(target != null && !target.isEmpty()) {
                        Logger.log("Good to go, hopping");
                        TaskManager.getInstance().setCurrentTask(new HopperTask(targetWorld, this));
                    }
                }
            } catch (Exception ignored) {
            }
            retries++;
        }
        else {
            if(!Tabs.isOpen(Tab.INVENTORY)) {
                Tabs.open(Tab.INVENTORY);
            }

            // do we have a target
            if(!target.isEmpty()) {
                // is the trade window closed
                if (!Trade.isOpen() && (lastSentRequest == 0 || (Instant.now().getEpochSecond() - lastSentRequest) > 5)) {
                    Player p = Players.closest(target);
                    // are we near the player
                    if (p != null) {
                        Logger.log("Detected mule nearby");
                        if(!org.dreambot.api.methods.map.Map.isTileOnScreen(p.getTile()) && Camera.rotateToEntity(p)) {
                            Logger.log("Rotated to see mule");
                            Sleep.sleep(100, 500);
                        }

                        if (lastSentRequest == 0 || (Instant.now().getEpochSecond() - lastSentRequest) > 5) {
                            if(p.interact("Trade with")) {
                                lastSentRequest = Instant.now().getEpochSecond();
                                Sleep.sleepUntil(Trade::isOpen, 5000);
                            }
                        }
                    } else {
                        Logger.log("Mule was not detected nearby");
                    }
                }

                // are we on the first window
                if (Trade.isOpen(1)) {
                    if (Inventory.contains("Coins") && Inventory.get("Coins") != null && !reverse) {
                        Trade.addItem("Coins", Inventory.get("Coins").getAmount());
                    }
                    Sleep.sleep(2000, 3000);
                    Trade.acceptTrade(1);
                }

                if (Trade.isOpen(2)) {
                    Trade.acceptTrade(2);
                    completed = true;
                    Sleep.sleepUntil(() -> !Trade.isOpen(), 2000);
                }
            } else {
                Logger.log("Waiting for mule to show up");
            }
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
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
        return null;
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
