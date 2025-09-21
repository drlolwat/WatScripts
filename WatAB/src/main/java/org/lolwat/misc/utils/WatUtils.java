package org.lolwat.misc.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.ItemManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.tasks.misc.WalkingTask;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.interfaces.WatTask;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WatUtils {
    private static final HashMap<String, String> webhookUrls = new HashMap<String, String>() {{
        put("lolwat", "https://discord.com/api/webhooks/REPLACE_ME/REPLACE_ME");
        put("user1", "https://discord.com/api/webhooks/REPLACE_ME/REPLACE_ME");
    }};
    private static final HashMap<String, Integer> levels = new HashMap<String, Integer>() {
        {
            // picks
            put("Rune pickaxe", 40);
            put("Adamant pickaxe", 30);
            put("Mithril pickaxe", 20);
            put("Black pickaxe", 10);
            put("Steel pickaxe", 5);
            put("Iron pickaxe", 1);
            put("Bronze pickaxe", 1);

            //axes
            put("Rune axe", 40);
            put("Adamant axe", 30);
            put("Mithril axe", 20);
            put("Black axe", 10);
            put("Steel axe", 5);
            put("Iron axe", 1);
            put("Bronze axe", 1);

            //staff?
            put("Staff of fire", 1);

            //scims..
            put("Rune scimitar", 40);
            put("Adamant scimitar", 30);
            put("Mithril scimitar", 20);
            put("Black scimitar", 10);
            put("Steel scimitar", 5);
            put("Iron scimitar", 1);
            put("Bronze scimitar", 1);

            //bows
            put("Maple shortbow", 30);
            put("Willow shortbow", 20);
            put("Oak shortbow", 5);
            put("Shortbow", 1);

            //everything else is handled by combat utils
        }
    };
    private static long lastCallTime = 0;
    private static HashMap<String, Integer> itemPrices;
    private static double multiplier = 1.2;

    public static void sendWebhook(String message, boolean error) {
        String webhookUrl = "https://api.botbuddy.net/ws_discord.php";
        try {
            sendToDiscordApi(message, webhookUrl, error);
        } catch (Exception e) {
            Logger.log("webhook: " + e.getMessage());
        }
    }

    public static void sendToDiscordApi(String message, String webhookUrl, boolean error) throws IOException {
        if(Client.getForumUser() == null || !webhookUrls.containsKey(Client.getForumUser().getUsername().toLowerCase()))
            return;

        URL url = new URL(webhookUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        JsonObject embed = new JsonObject();
        embed.addProperty("title", "WatAB Notification");
        embed.addProperty("description", message);
        embed.addProperty("color", error ? 16711680 : 3447003);

        JsonObject payload = new JsonObject();
        payload.add("embeds", new Gson().toJsonTree(new JsonObject[]{embed}));
        payload.addProperty("webhook_url", "https://discord.com/api/webhooks/REPLACE_ME/REPLACE_ME");

        String jsonPayload = new Gson().toJson(payload);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        connection.getResponseCode();
    }

    public static String getRealResponse(String nm, String msg, String task) {
        try {
            if(Client.getForumUser() == null || !webhookUrls.containsKey(Client.getForumUser().getUsername().toLowerCase()))
                return "";

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCallTime < 15 * 60 * 1000) {
                return "";
            }

            lastCallTime = currentTime;

            String urlParameters = "nm=" + nm + "&msg=" + msg + "&task=" + task;
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            String GPT_URL = "https://api.botbuddy.net/wat.php";
            HttpURLConnection con = (HttpURLConnection) new URL(GPT_URL).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("charset", "utf-8");
            con.setRequestProperty("Content-Length", Integer.toString(postData.length));
            con.setUseCaches(false);
            con.setDoOutput(true);

            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(postData);
            }

            StringBuilder response = new StringBuilder();
            int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
            } else {
                response.append("POST request did not work. Response Code: ").append(responseCode);
                return "";
            }

            String formattedMessage = String.format(
                    "**Dreambot user**: %s\n**Replying to**: %s\n**Player sent**: %s\n**Bot sent**: %s",
                    Client.getForumUser().getUsername(),
                    nm,
                    msg,
                    response);

            sendWebhook(formattedMessage, false);
            return response.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    public static String capitalize(String str) {
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static String simplifyNumber(double number) {
        if (number >= 1000000) {
            return String.format("%.2fM", number / 1000000);
        } else if (number >= 1000) {
            return String.format("%.2fK", number / 1000);
        } else {
            return String.format("%.2f", number);
        }
    }

    public static int getItemPrice(String item) {
        if(itemPrices == null) {
            itemPrices = new HashMap<>();
        }

        if(itemPrices.containsKey(item)) {
            return itemPrices.get(item);
        }

        int randomizer = Calculations.random(5, 30);
        int num = (int) (LivePrices.getHigh(item) * multiplier);

        if(num < 100) {
            num = num * 3;
        }
        else if(num < 500) {
            num = (int) Math.ceil(num * 2.5);
        }
        else if(num < 1000) {
            num = num * 2;
        } else {
            num = (int) (num * multiplier);
        }

        itemPrices.put(item, num + randomizer);
        return itemPrices.get(item);
    }

    public static void raisePrice(String item) {
        int num;
        if(itemPrices.containsKey(item)) {
            num = itemPrices.get(item);
            itemPrices.remove(item);
        }
        else {
            num = (int) (LivePrices.getHigh(item) * multiplier);
        }

        int currentHigh = LivePrices.getHigh(item);
        if(num > (currentHigh * 20)) {
            num = currentHigh;
        }

        if(num < 100) {
            num = num * 3;
        }
        else if(num < 500) {
            num = (int) Math.ceil(num * 2.5);
        }
        else if(num < 1000) {
            num = num * 2;
        } else {
            num = (int) (num * multiplier);
        }

        itemPrices.put(item, num);
    }

    public static void handlePickup(List<String> items) {
        List<String> newItems = new ArrayList<>();

        if (ConfigManager.getInstance().getConfigBoolean("pickup_bones")) {
            newItems.add("Bones");
            newItems.add("Big bones");
        }

        newItems.addAll(items);

        GroundItem i = GroundItems.closest(x -> x != null && newItems.contains(x.getName()) && x.distance() <= 4 && x.canReach());

        if(i == null) return;

        if(!Inventory.isFull()) {
            if (newItems.contains(i.getName())) {
                if (i.interact("Take")) {
                    Logger.log("GenericUtils: Picked up item: " + i.getName());
                    Sleep.sleepUntil(() -> !i.exists(), 5000);
                }

                Sleep.sleepUntil(() -> !Players.getLocal().isInCombat(), Calculations.random(2000, 3000));
                handleBury();
            }
        } else {
            Logger.log("GenericUtils: Inventory is full, not picking up item: " + i.getName());
            handleBury();
        }
    }

    public static void handleBury() {
        if (ConfigManager.getInstance().getConfigBoolean("pickup_bones")) {
            for (Item ix : Inventory.all(x -> x != null && x.hasAction("Bury"))) {
                if (ix != null) {
                    if (Dialogues.inDialogue()) {
                        DialogueUtils.continueWhilePossible();
                        Sleep.sleepUntil(() -> !Dialogues.inDialogue(), Calculations.random(1200, 1800));
                    }

                    if (!ix.interact("Bury")) {
                        Logger.error("GenericUtils: Failed to bury item: " + ix.getName());
                    }

                    Sleep.sleepUntil(Dialogues::inDialogue, Calculations.random(1200, 1800));
                }
            }
        }
    }

    public static boolean tooManyPlayers(int distance, int count) {
        int pl = 0;
        for(Player ply : Players.all()) {
            if(Players.getLocal().equals(ply))
                continue;

            if(ply.distance(Players.getLocal()) <= distance) {
                pl++;
            }
        }

        return pl >= count;
    }

    public static boolean equipItem(String item, Item old) {
        Item i = Inventory.get(x -> x != null && !x.isNoted() && x.getName().contains(item));
        if(i != null) {
            if(i.hasAction("Wear") && i.interact("Wear")) {
                Logger.log("Equipment: Equipped wearable");
                Sleep.sleep(100, 200);

                if(Bank.isOpen() && old != null) {
                    Sleep.sleep(200, 400);
                    Bank.depositAll(old.getName());
                    Logger.log("Equipment: Deposited old item: " + old.getName());
                }

                return true;
            }
            else if(i.hasAction("Wield") && i.interact("Wield")) {
                Logger.log("Equipment: Equipped wieldable");
                Sleep.sleep(100, 200);

                if(Bank.isOpen() && old != null) {
                    Sleep.sleep(200, 400);
                    Bank.depositAll(old.getName());
                    Logger.log("Equipment: Deposited old item: " + old.getName());
                }

                return true;
            }
        } else {
            Logger.error("item to equip was null");
        }

        return false;
    }

    public static boolean isMember() {
        return PlayerSettings.getConfig(1780) > 0;
    }

    public static String generateUsername() {
        String name = "";
        try {
            URL url = new URL("https://api.botbuddy.net/getname.php");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    name = line;
                }
                reader.close();
            } else {
                System.out.println("HTTP request failed with response code: " + responseCode);
            }

            connection.disconnect();
        } catch (IOException ignored) {
        }

        return name;
    }

    public static boolean canEquipTool(String toolName) {
        if(!levels.containsKey(toolName)) {
            return true;
        }

        return levels.containsKey(toolName) && Skills.getRealLevel(Skill.ATTACK) >= levels.get(toolName);
    }

    public static <K, V> void shuffleHashMap(HashMap<K, V> hashMap) {
        // Convert HashMap entries to a List
        List<Map.Entry<K, V>> entryList = new ArrayList<>(hashMap.entrySet());

        // Shuffle the list
        Collections.shuffle(entryList);

        // Clear the original HashMap
        hashMap.clear();

        // Add the shuffled entries back to the original HashMap
        for (Map.Entry<K, V> entry : entryList) {
            hashMap.put(entry.getKey(), entry.getValue());
        }
    }

    public static void handleSpecial() {
        List<String> weapons = new ArrayList<String>() { {
            add("Magic shortbow");
            add("Dragon sword");
            add("Dragon scimitar");
        } };

        for(String s : weapons) {
            if (Equipment.slotContains(EquipmentSlot.WEAPON, s)) {
                if(Combat.getSpecialPercentage() > 75) {
                    Combat.toggleSpecialAttack(true);
                }
            }
        }
    }

    public static void castHomeTeleport() {
        if(Magic.canCast(Normal.HOME_TELEPORT)) {
            if(!Tabs.isOpen(Tab.MAGIC)) {
                Tabs.open(Tab.MAGIC);
                Sleep.sleepUntil(() -> Tabs.isOpen(Tab.MAGIC), Calculations.random(200, 300));
            }

            if(Magic.castSpell(Normal.HOME_TELEPORT)) {
                Tile currentTile = Players.getLocal().getTile();
                Sleep.sleepUntil(() -> Players.getLocal().getTile() != currentTile && !Players.getLocal().isAnimating(), 5000);
            }

            if(!Tabs.isOpen(Tab.INVENTORY)) {
                Tabs.open(Tab.INVENTORY);
                Sleep.sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), Calculations.random(200, 300));
            }
        }
    }

    public static String uppercaseFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        } else {
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
    }

    public static void setBankMode(BankMode mode) {
        if(Bank.getWithdrawMode().equals(mode)) {
            return;
        }

        Bank.setWithdrawMode(mode);
        Sleep.sleep(100, 200);
    }

    public static int equipmentCount(String name) {
        return Equipment.count(x -> x != null && x.getName().contains(name));
    }

    public static int inventoryCount(String name) {
        return Inventory.count(x -> x != null && x.getName().contains(name));
    }

    public static boolean inventoryContains(int itemId, int itemQty, boolean allowNoted) {
        return Inventory.contains(x -> x != null && (allowNoted || !x.isNoted()) && x.getID() == itemId && x.getAmount() >= itemQty && !x.getName().contains("(1)"));
    }

    public static boolean inventoryContains(String itemId, int itemQty, boolean allowNoted) {
        return Inventory.contains(x -> x != null && (allowNoted || !x.isNoted()) && x.getName().equals(itemId) && x.getAmount() >= itemQty && !x.getName().contains("(1)"));
    }

    public static boolean equipmentContains(int itemId, int itemQty) {
        return Inventory.contains(x -> x != null && x.getID() == itemId && x.getAmount() >= itemQty && !x.getName().contains("(1)"));
    }

    public static boolean equipmentSlotContains(String name, int itemQty) {
        return Inventory.contains(x -> x != null && x.getName().equals(name) && x.getAmount() >= itemQty && !x.getName().contains("(1)"));
    }

    public static boolean bankContains(int itemId, int itemQty, boolean allowNoted) {
        return Bank.contains(x -> x != null && (allowNoted || !x.isNoted()) && x.getID() == itemId && x.getAmount() >= itemQty && !x.getName().contains("(1)"));
    }

    public static void bank(WatTask task) {
        List<String> allowedObjects = new ArrayList<String>() {
            {
                add("Bank booth");
                add("Open chest");
                add("Bank chest");
            }
        };

        NPC banker = NPCs.closest("Banker");
        if (banker == null || !banker.exists()) {
            Logger.log("Banking: Looking for bank chest");
            GameObject chest = GameObjects.closest(x -> x != null
                    && allowedObjects.contains(x.getName()) && x.distance() <= 15.00
                    && !x.hasAction("Shut") && !x.hasAction("Search") && !x.hasAction("Chop down")
                    && x.canReach());

            if (chest == null) {
                Logger.log("Banking: No banker or chest found (15r), walking to nearest bank");
                TaskManager.getInstance().setCurrentTask(new WalkingTask(BankLocation.getNearest(Players.getLocal().getTile(), false).getArea(3), task));
                return;
            } else {
                Logger.log("Banking: Opening bank via " + chest.getName() + " (id: " + chest.getID() + ", distance: " + chest.distance() + ")");
                if(!chest.isOnScreen()) {
                    if(!org.dreambot.api.methods.map.Map.isTileOnMap(chest.getTile()) || chest.distance() > 15.0) {
                        Logger.log("Banking: Walking closer to chest");
                        TaskManager.getInstance().setCurrentTask(new WalkingTask(chest.getTile().getArea(2), task));
                        return;
                    }

                    Camera.rotateToTile(chest.getTile());
                    Sleep.sleepUntil(chest::isOnScreen, Calculations.random(5000, 10000));
                }

                if (!chest.interact()) {
                    Logger.error("Banking: Failed to interact with chest");
                    return;
                }
            }
        } else {
            Logger.log("Banking: Opening bank via Banker (distance: " + banker.distance() + ")");
            if (!Bank.open()) {
                return;
            }
        }

        Logger.log("Banking: Waiting for bank to be open..");
        Sleep.sleepUntil(Bank::isOpen, 15000);
    }

    public static boolean hasInventory() {
        if(TaskManager.getInstance().getCurrentTask() != null) {
            for(Map.Entry<WatItem, Integer> map  : TaskManager.getInstance().getCurrentTask().inventory().entrySet()) {
                if(!inventoryContains(map.getKey().getName(), map.getValue(), false)) {
                    Logger.log("[INVENTORY] " + TaskManager.getInstance().getCurrentTask().getName() + ": Missing " + map.getKey().getName() + " x" + map.getValue());
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean hasRequiredLoadout() {
        if(TaskManager.getInstance().getCurrentTask() != null) {
            for(Map.Entry<WatItem, Integer> map  : TaskManager.getInstance().getCurrentTask().loadout().entrySet()) {
                if(!inventoryContains(map.getKey().getName(), map.getValue(), false)) {
                    Logger.log("[LOADOUT] " + TaskManager.getInstance().getCurrentTask().getName() + ": Missing " + map.getKey().getName() + " x" + map.getValue());
                    return false;
                }
            }
        }

        return true;
    }

    public static void removeOtherItems(WatTask t) {
        for(Item i : Inventory.all()) {
            if(i == null) continue;
            WatItem wi = ItemManager.getInstance().getItem(i.getName());
            if(wi == null) {
                if(!Bank.depositAll(i)) {
                    Logger.error("no wi-error depositing " + i.getName());
                    return;
                }
                continue;
            }

            if(!t.loadout().containsKey(wi) && !t.inventory().containsKey(wi) && !t.inventoryTolerated().contains(i.getName())) {
                Logger.log("removing " + i.getName() + " from inventory");
                if(!Bank.depositAll(i)) {
                    Logger.error("error depositing " + i.getName());
                    return;
                }
            }
        }
    }
}
