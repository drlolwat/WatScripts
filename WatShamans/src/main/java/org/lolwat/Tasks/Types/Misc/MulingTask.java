package org.lolwat.Tasks.Types.Misc;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.Tasks.WatTask;
import org.lolwat.WatAIO;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class MulingTask implements WatTask {
    String name;
    boolean active;
    String target;
    long lastSentRequest = 0;
    int originalWorld;
    boolean completed = false;
    int retries = 0;

    public MulingTask(String taskName, int currentWorld) {
        name = taskName;
        active = false;
        target = "";
        originalWorld = currentWorld;
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
        if(NPCs.closest("Grand Exchange Clerk") == null) {
            instance.currentTask = new TraversalTask(BankLocation.GRAND_EXCHANGE.getTile(), false, this);
            return;
        }

        if(completed) {
            instance.currentTask = new HopperTask(originalWorld, null);
            return;
        }

        if(!active) {
            if(retries > 5) {
                instance.MULE_DEAD = true;
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
                    instance.currentTask = null;
                }

                return;
            }

            Logger.log("Sending mule connection");
            try (Socket socket = new Socket("localhost", 8081)) {
                // Send a message to the server
                String message = "READY|" + Players.getLocal().getName();
                byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(messageBytes);
                outputStream.flush();

                // Receive the response from the server
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

                    Logger.log("Good to go, hopping");
                    instance.currentTask = new HopperTask(targetWorld, this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            retries++;
        }
        else {
            if(WorldHopper.isWorldHopperOpen()) {
                WorldHopper.closeWorldHopper();
            }

            if(!Tabs.isOpen(Tab.INVENTORY)) {
                Tabs.open(Tab.INVENTORY);
            }

            // do we have a target
            if(!target.isEmpty()) {
                // is the trade window closed
                if (!Trade.isOpen() && (lastSentRequest == 0 || (Instant.now().getEpochSecond() - lastSentRequest) > 5)) {
                    // are we near the player
                    if (Players.closest(target) != null) {
                        if (lastSentRequest == 0 || (Instant.now().getEpochSecond() - lastSentRequest) > 5) {
                            Trade.tradeWithPlayer(target);
                            lastSentRequest = Instant.now().getEpochSecond();
                            Sleep.sleepUntil(Trade::isOpen, 5000);
                        }
                    }
                }

                // are we on the first window
                if (Trade.isOpen(1)) {
                    if (Inventory.contains("Coins") && Inventory.get("Coins") != null) {
                        //instance.goldMuled += Inventory.get("Coins").getAmount();
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
        return null;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }
}
