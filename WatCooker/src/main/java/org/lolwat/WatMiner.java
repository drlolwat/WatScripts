package org.lolwat;

import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.event.impl.ExperienceEvent;
import org.dreambot.api.script.listener.ExperienceListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/*
      TODO
        fix rune pickaxe issues lol
 */

@ScriptManifest(name = "WatMiner", description = "It is what it is", author = "lolwat",
        version = 1.6, category = Category.MINING, image = "")
public class WatMiner extends AbstractScript implements ExperienceListener {
    private enum State {
        ON_TASK,
        TRAVERSING_TO_TASK,
        BANKING,
        HOPPING,
        SELLING_TO_GE,
        BUYING_AT_GE,
        MULING
    }

    private String stateString() {
        switch(currentState) {
            case ON_TASK:
                return "On task";
            case TRAVERSING_TO_TASK: {
                return "Traversing to task";
            }
            case BANKING: {
                return "Banking";
            }
            case HOPPING: {
                return "World Hopping";
            }
            case SELLING_TO_GE: {
                return "Selling at Exchange";
            }
            case BUYING_AT_GE: {
                return "Buying " + pickTypes.get(buyingType);
            }
            case MULING: {
                return "Handing off GP";
            }
            default: {
                return "Unknown";
            }
        }
    }

    private State currentState;
    private Item currentPickaxe;
    private HashMap<Integer, String> pickTypes;
    private GameObject rock;
    private boolean gotRock = false;
    private long lastGotRock = 0;
    private int buyingType = 0;

    // tiles
    private Tile varrockEastMine = new Tile(3286, 3368);
    private Tile varrockEastAlt = new Tile(3285, 3370);
    private List<Tile> varrockEastRocks;
    private List<Tile> varrockEastAltRocks;
    private List<String> sellList;
    private List<String> mySells;
    private boolean usingAltLocation;
    private boolean canEquip = false;
    private int oreMined = 0;
    private int expGained = 0;
    private Timer timer;
    private boolean muling = false;
    private String muleTarget = "";
    private int myWorld;
    private boolean forceBank = false;

    @Override
    public void onStart() {
        currentState = State.BANKING;
        pickTypes = new HashMap<Integer, String>() {
            {
                put(41, "Rune pickaxe");
                put(31, "Adamant pickaxe");
                put(21, "Mithril pickaxe");
                put(11, "Steel pickaxe");
                put(1, "Iron pickaxe");
            }
        };

        sellList = new ArrayList<String>() {
            {
                add("Iron ore");
                add("Uncut sapphire");
                add("Uncut emerald");
                add("Uncut ruby");
                add("Uncut diamond");
            }
        };

        mySells = new ArrayList<>();

        currentPickaxe = null;
        rock = null;
        usingAltLocation = false;

        varrockEastRocks = Arrays.asList(new Tile(3286, 3369), new Tile(3285, 3368));
        varrockEastAltRocks = Arrays.asList(new Tile(3288, 3370), new Tile(3285, 3369));

        Client.getInstance().setMouseMovementAlgorithm(new BezierMouse());
        timer = new Timer();
    }

    @Override
    public int onLoop() {
        switch (currentState) {
            case MULING: {
                Tile safeTile = new Tile(3164, 3487);
                if(!Map.isTileOnMap(safeTile)) {
                    if(Walking.shouldWalk(5)) {
                        Walking.walk(safeTile);
                    }
                }

                if(!muling) {
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
                            muling = true;
                            myWorld = Worlds.getCurrentWorld();
                            int targetWorld = 0;
                            String[] sp = response.split("\\|");
                            if (sp.length > 0) {
                                muleTarget = sp[1];
                                targetWorld = Integer.parseInt(sp[2]);
                            }
                            handleHop(targetWorld);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    if(!muleTarget.isEmpty()) {
                        if(Map.isTileOnMap(safeTile) && !Map.isTileOnScreen(safeTile)) {
                            Camera.rotateToTile(safeTile);
                        }

                        if(Players.getLocal().getTile().equals(safeTile) || Trade.isOpen()) {
                            if(!Trade.isOpen()) {
                                if (Players.closest(muleTarget) != null) {
                                    Trade.tradeWithPlayer(muleTarget);
                                    return 3500;
                                }
                            } else {
                                if(Trade.isOpen()) {
                                    if(Trade.isOpen(1)) {
                                        if(Inventory.get("Coins") != null) {
                                            Trade.addItem("Coins", Inventory.get("Coins").getAmount());
                                            Sleep.sleep(500, 750);
                                        }

                                        Trade.acceptTrade(1);
                                        Sleep.sleepUntil(() -> Trade.isOpen(2), 2000);
                                    }

                                    if(Trade.isOpen(2)) {
                                        Trade.acceptTrade(2);
                                        Sleep.sleepUntil(() -> !Trade.isOpen(), 2000);
                                        muling = false;
                                        muleTarget = "";
                                        handleHop(myWorld);
                                        mySells.clear();
                                        myWorld = 0;
                                        return 1;
                                    }

                                    return 1;
                                }
                            }
                        }
                        else {
                            if(shouldWalk(safeTile)) {
                                Walking.walk(safeTile);
                            }
                        }
                    }
                }

                return 3;
            }
            case BANKING: {
                if(!Client.isLoggedIn()) {
                    Logger.log("waiting for login");
                    return 500;
                }

                AtomicBoolean isUpgrading = new AtomicBoolean(false);

                if (!Tab.INVENTORY.isOpen()) {
                    Tab.INVENTORY.open();
                }

                if (currentPickaxe == null) {
                    if (!Equipment.isSlotEmpty(EquipmentSlot.WEAPON) && pickTypes.containsValue(Equipment.getItemInSlot(EquipmentSlot.WEAPON).getName())) {
                        currentPickaxe = Equipment.getItemInSlot(EquipmentSlot.WEAPON);
                    } else {
                        for (String val : pickTypes.values()) {
                            if (Inventory.contains(val)) {
                                currentPickaxe = Inventory.get(val);
                            }
                        }
                    }
                }

                if (!Inventory.isFull() && currentPickaxe != null && !forceBank) {
                    currentState = State.TRAVERSING_TO_TASK;
                    return 1;
                }

                if (forceBank)
                    forceBank = false;

                if (NPCs.closest("Banker") != null) {
                    if (!Bank.isOpen()) {
                        Bank.open();
                        Sleep.sleepUntil(Bank::isOpen, 10000);
                        if(!Bank.isOpen())
                            return 1;

                        Bank.depositAll("Coins");
                        Sleep.sleep(100, 120);

                        if (currentPickaxe != null && Inventory.contains(currentPickaxe.getName())) {
                            Bank.depositAllExcept(currentPickaxe.getName());
                        } else {
                            Bank.depositAllItems();
                        }

                        if (Bank.contains("Iron ore") && Bank.get("Iron ore").getAmount() >= 1000) {
                            if (!Bank.getWithdrawMode().equals(BankMode.NOTE)) {
                                Bank.setWithdrawMode(BankMode.NOTE);
                            }

                            Sleep.sleep(100, 200);

                            for (String type : sellList) {
                                if (Bank.contains(type)) {
                                    Sleep.sleep(50, 100);
                                    Bank.withdrawAll(type);
                                    mySells.add(type);
                                    Logger.log("Adding " + type + " to sell list");
                                }
                            }

                            if (!Bank.getWithdrawMode().equals(BankMode.ITEM)) {
                                Bank.setWithdrawMode(BankMode.ITEM);
                            }

                            currentState = State.SELLING_TO_GE;
                            Bank.close();
                            return 1;
                        }

                        AtomicBoolean reCheck = new AtomicBoolean(true);
                        if (currentPickaxe == null) {
                            if (Bank.count("Coins") >= 30000) {
                                pickTypes.keySet().stream()
                                        .sorted(Comparator.reverseOrder())
                                        .forEach(key -> {
                                            String value = pickTypes.get(key);
                                            if(currentPickaxe == null && !isUpgrading.get()) {
                                                if(Skills.getRealLevel(Skill.MINING) >= getPickaxeLevel(value)) {
                                                    if (Bank.contains(value)) {
                                                        currentPickaxe = Bank.get(value);
                                                        Bank.depositAllItems();
                                                        Sleep.sleep(100, 150);
                                                        Bank.withdraw(currentPickaxe.getName());
                                                    } else {
                                                        isUpgrading.set(true);
                                                        buyingType = key;
                                                        Bank.depositAllItems();
                                                        Sleep.sleep(100, 130);
                                                        Bank.withdraw("Coins", 30000);
                                                    }
                                                }
                                            }
                                        });
                            } else {
                                pickTypes.keySet().stream()
                                        .sorted(Comparator.reverseOrder())
                                        .forEach(key -> {
                                            if (currentPickaxe == null) {
                                                String value = pickTypes.get(key);
                                                if (Bank.contains(value) && Skills.getRealLevel(Skill.MINING) >= key) {
                                                    currentPickaxe = Bank.get(value);
                                                    Bank.withdraw(currentPickaxe.getName());
                                                    Sleep.sleep(100, 120);
                                                }
                                            }
                                        });
                            }
                        } else {
                            pickTypes.keySet().stream()
                                    .sorted(Comparator.reverseOrder())
                                    .forEach(key -> {
                                        String value = pickTypes.get(key);
                                        int nextLevel = key + 10;
                                        if (Bank.count("Coins") >= 30000) {
                                            if (key <= 30 && !isUpgrading.get() && Skills.getRealLevel(Skill.MINING) >= nextLevel) {
                                                if (currentPickaxe.getName().equals(value)) {
                                                    if (getPickaxeLevel(currentPickaxe.getName()) < nextLevel) {
                                                        if (Bank.contains(pickTypes.get(nextLevel))) {
                                                            Bank.depositAllItems();
                                                            Sleep.sleep(100, 120);

                                                            if (Equipment.contains(value)) {
                                                                Bank.depositAllEquipment();
                                                                Sleep.sleep(100, 120);
                                                            }

                                                            currentPickaxe = Bank.get(pickTypes.get(nextLevel));
                                                            Bank.withdraw(pickTypes.get(nextLevel));
                                                            Sleep.sleep(100, 120);
                                                        } else {
                                                            isUpgrading.set(true);
                                                            buyingType = nextLevel;

                                                            Bank.depositAllItems();
                                                            Sleep.sleep(100, 200);

                                                            if (Equipment.contains(value)) {
                                                                Bank.depositAllEquipment();
                                                                Sleep.sleep(100, 120);
                                                            }

                                                            Bank.withdraw("Coins", 30000);
                                                            Sleep.sleep(100, 120);
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            if (reCheck.get() && !currentPickaxe.getName().equals(value)) {
                                                // check if this pick is better than current pick
                                                if(getPickaxeLevel(currentPickaxe.getName()) <= key) {
                                                    if (Bank.contains(value) && Skills.getRealLevel(Skill.MINING) >= key) {
                                                        Bank.depositAllItems();
                                                        Sleep.sleep(100, 120);

                                                        if (Equipment.contains(currentPickaxe.getName())) {
                                                            Bank.depositAllEquipment();
                                                            Sleep.sleep(100, 150);
                                                        }

                                                        currentPickaxe = Bank.get(value);
                                                        Bank.withdraw(value);
                                                        Sleep.sleep(100, 150);

                                                        reCheck.set(false);
                                                    }
                                                }
                                            }
                                        }
                                    });
                        }
                    }

                    if (currentPickaxe == null) {
                        if (!isUpgrading.get()) {
                            Logger.log("Pickaxe for your level was not available");
                            return 1;
                        }
                    } else {
                        Logger.log("Using a " + currentPickaxe.getName());
                    }

                    Sleep.sleep(100, 500);

                    if (Bank.contains("Coins") && Bank.get("Coins").getAmount() >= 100000 && !isUpgrading.get()) {
                        if (currentPickaxe != null)
                            Bank.depositAllExcept(currentPickaxe.getName());
                        else
                            Bank.depositAllItems();

                        Sleep.sleep(50, 100);
                        Bank.withdrawAll("Coins");
                        currentState = State.MULING;
                        Bank.close();
                        return 1;
                    }

                    Bank.close();
                    Sleep.sleep(800);

                    if (!isUpgrading.get()) {
                        usingAltLocation = false;
                        currentState = State.TRAVERSING_TO_TASK;
                    } else {
                        currentState = State.BUYING_AT_GE;
                    }

                } else {
                    if (shouldWalk(BankLocation.VARROCK_EAST.getTile())) {
                        Walking.walk(BankLocation.VARROCK_EAST.getTile());
                    }
                }
                return 3;
            }

            case BUYING_AT_GE: {
                if (NPCs.closest("Grand Exchange Clerk") != null) {
                    if (!Map.isTileOnScreen(NPCs.closest("Grand Exchange Clerk").getTile())) {
                        Camera.rotateToEntity(NPCs.closest("Grand Exchange Clerk"));
                    }
                    if (!GrandExchange.isOpen()) {
                        GrandExchange.open();
                        Sleep.sleep(1000, 2500);
                        int slot = GrandExchange.getFirstOpenSlot();
                        GrandExchange.openBuyScreen(slot);
                        Sleep.sleep(2000);
                        GrandExchange.addBuyItem(pickTypes.get(buyingType));
                        //Keyboard.type(pickTypes.get(buyingType), true);
                        Sleep.sleep(1000, 2000);
                        GrandExchange.getIncreasePriceFivePercentButton().interact();
                        Sleep.sleep(50, 100);
                        GrandExchange.getIncreasePriceFivePercentButton().interact();
                        Sleep.sleep(100, 200);
                        GrandExchange.confirm();
                        Sleep.sleepUntil(() -> GrandExchange.isReadyToCollect(slot), 10000);

                        if(GrandExchange.isReadyToCollect(slot)) {
                            GrandExchange.collect();
                        }

                        Sleep.sleep(100, 200);
                    }

                    GrandExchange.close();

                    currentPickaxe = Inventory.get(pickTypes.get(buyingType));
                    buyingType = 0;
                    forceBank = true;
                    currentState = State.BANKING;

                } else {
                    if (shouldWalk(BankLocation.GRAND_EXCHANGE.getTile())) {
                        Walking.walk(BankLocation.GRAND_EXCHANGE);
                    }
                }
                return 3;
            }

            case SELLING_TO_GE: {
                if(NPCs.closest("Grand Exchange Clerk") != null) {
                    if(!Map.isTileOnScreen(NPCs.closest("Grand Exchange Clerk").getTile())) {
                        Camera.rotateToEntity(NPCs.closest("Grand Exchange Clerk"));
                    }
                    if(!GrandExchange.isOpen()) {
                        GrandExchange.open();
                        Sleep.sleepUntil(GrandExchange::isOpen, 6000);
                        for(String mySell : mySells) {
                            if (Inventory.contains(mySell)) {
                                Inventory.get(mySell).interact();
                                Sleep.sleep(100, 300);
                                GrandExchange.getDecreasePriceFivePercentButton().interact();
                                Sleep.sleep(100, 300);
                                GrandExchange.getDecreasePriceFivePercentButton().interact();
                                Sleep.sleep(100, 300);
                                GrandExchange.confirm();
                                Sleep.sleep(2000, 5000);
                                GrandExchange.collect();
                            }
                        }
                        GrandExchange.close();

                        forceBank = true;
                        currentState = State.BANKING;

                        if(currentPickaxe != null && !Inventory.contains(currentPickaxe.getName())) {
                            currentPickaxe = null;
                        }
                    }
                }
                else {
                    if (shouldWalk(BankLocation.GRAND_EXCHANGE.getTile())) {
                        Walking.walk(BankLocation.GRAND_EXCHANGE);
                    }
                }

                return 3;
            }

            case TRAVERSING_TO_TASK:
            {
                if(Players.getLocal().getTile().equals(varrockEastMine)) {
                    lastGotRock = Instant.now().getEpochSecond() - 10;
                    currentState = State.ON_TASK;
                }
                else {
                    if (shouldWalk(varrockEastMine)) {
                        Walking.walk(varrockEastMine);
                    }
                }

                return 3;
            }

            case ON_TASK: {
                if(!Map.isTileOnScreen(varrockEastMine)) {
                    Camera.rotateToTile(varrockEastMine);
                }

                if(Inventory.isFull()) {
                    currentState = State.BANKING;
                    return 3;
                }

                List<Tile> toUse;
                if(!usingAltLocation) {
                    toUse = varrockEastRocks;

                    if(lastGotRock > 0 && (Instant.now().getEpochSecond() - lastGotRock) > 20) {
                        toUse = varrockEastAltRocks;
                        usingAltLocation = true;
                        lastGotRock = Instant.now().getEpochSecond();
                    }
                } else {
                    boolean switchBack = true;
                    for(Player pl : Players.all()) {
                        if(pl.getTile().equals(varrockEastMine)) {
                            switchBack = false;
                            break;
                        }
                    }

                    if(lastGotRock > 0 && (Instant.now().getEpochSecond() - lastGotRock) > 30) {
                        usingAltLocation = false;
                        handleHop(0);
                        return 600;
                    }

                    if(switchBack) {
                        toUse = varrockEastRocks;
                        usingAltLocation = false;
                    }
                    else
                        toUse = varrockEastAltRocks;
                }

                if(!gotRock || (lastGotRock <= 0 || (Instant.now().getEpochSecond() - lastGotRock) > 10) || (rock != null && GameObjects.getTopObjectOnTile(rock.getTile()).getModelColors() == null)) {
                    if(Players.getLocal().isMoving()) {
                        Sleep.sleep(1000, 1500);
                    }

                    for(Tile tile : toUse) {
                        GameObject obj = GameObjects.getTopObjectOnTile(tile);
                        if(obj.getModelColors() != null) {
                            rock = obj;

                            if(!obj.isOnScreen()) {
                                Camera.rotateToEntity(obj);
                            }

                            obj.interact();
                            gotRock = true;
                            break;
                        }
                    }

                    Sleep.sleepUntil(() -> (!Players.getLocal().isAnimating() && !Players.getLocal().isMoving()) || (rock != null && GameObjects.getTopObjectOnTile(rock.getTile()).getModelColors() == null), 8000);
                }

                return 1;
            }

            case HOPPING: {
                if(Client.getGameState() == GameState.HOPPING) {
                    log("still hopping");
                    return 600;
                } else {
                    if(WorldHopper.isWorldHopperOpen()) {
                        WorldHopper.closeWorldHopper();
                    }

                    Sleep.sleep(500, 1000);

                    if(!Tab.INVENTORY.isOpen()) {
                        Tab.INVENTORY.open();
                    }

                    if(!muling) {
                        if(currentPickaxe == null || (!Inventory.contains(currentPickaxe.getName()) && !Equipment.contains(currentPickaxe.getName()))) {
                            currentPickaxe = null;
                            currentState = State.BANKING;
                        }
                        else {
                            currentState = State.TRAVERSING_TO_TASK;
                        }
                    } else {
                        currentState = State.MULING;
                    }
                }
                return 1;
            }

            default:
                return -1;
        }
    }

    @Override
    public void onGained(ExperienceEvent ev) {
        gotRock = false;
        oreMined++;
        expGained += ev.getChange();
        lastGotRock = Instant.now().getEpochSecond();
    }

    @Override
    public void onPaint(Graphics g) {
        g.setColor(new Color(204, 187, 154));
        g.fillRect(7,345, 506,130);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Verdana", Font.BOLD, 20));
        g.drawString("WatMiner: " + stateString(), 43, 377);

        g.setFont(new Font("Verdana", Font.BOLD, 15));
        g.drawString("Ores: " + oreMined + " (" + timer.getHourlyRate(oreMined) + "/h)", 43, 403);
        g.drawString("Exp: " + expGained + " (" + timer.getHourlyRate(expGained) + "/h)", 43, 423);
        g.drawString("Mining level: " + Skills.getRealLevel(Skill.MINING), 43, 443);
    }

    public void handleHop(int world) {
        if (!Tab.LOGOUT.isOpen()) {
            Tab.LOGOUT.open();
            Sleep.sleep(500, 1000);
        }

        if (!WorldHopper.isWorldHopperOpen()) {
            WorldHopper.openWorldHopper();
            Sleep.sleep(300, 800);
        }

        if(world == 0) {
            WorldHopper.hopWorld(Worlds.getRandomWorld((w) -> !w.isPVP() && !w.isMembers() && !w.isDeadmanMode() && !w.isHighRisk() && w.getMinimumLevel() <= 100));
        }
        else {
            WorldHopper.hopWorld(world);
        }
        currentState = State.HOPPING;
    }

    private boolean shouldWalk(Tile tile) {
        return Walking.shouldWalk(5);
    }

    private int getPickaxeLevel(String name) {
        name = name.toLowerCase();
        switch(name) {
            case "rune pickaxe": return 41;
            case "adamant pickaxe": return 31;
            case "mithril pickaxe": return 21;
            case "steel pickaxe": return 11;
            default: return 0;
        }
    }
}
