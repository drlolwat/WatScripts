package org.lolwat.misc.utils;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.dialogues.Dialogues;
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
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.tasks.mining.MiningTask;
import org.lolwat.tasks.woodcutting.WoodcuttingTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class GenericUtils {

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

    public static String getFallbackTool(WatTask taskType) {
        if(taskType instanceof MiningTask)
            return "Bronze pickaxe";
        else if(taskType instanceof WoodcuttingTask)
            return "Bronze axe";

        return "";
    }

    public static void handlePickup(List<String> items) {
        if (ConfigManager.getInstance().getConfigBoolean("pickup_bones")) {
            items.add("Bones");
            items.add("Big bones");
        }

        GroundItem i = GroundItems.closest(x -> x != null && items.contains(x.getName()) && x.distance() <= 4 && x.canReach());

        if(!Inventory.isFull()) {
            if (items != null && i != null) {
                if (items.contains(i.getName())) {
                    if (i.interact("Take")) {
                        Logger.log("GenericUtils: Picked up item: " + i.getName());
                        Sleep.sleepUntil(() -> !i.exists(), 5000);
                    }

                    Sleep.sleepUntil(() -> !Players.getLocal().isInCombat(), Calculations.random(2000, 3000));
                    handleBury();
                }
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
        Item i = Inventory.get(item);
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

    public static boolean notedOrNull(String item) {
        if(!Inventory.contains(item)) return true;
        return Inventory.get(item).isNoted();
    }

    public static HashMap<String, Integer> getSkillingGear() {
        HashMap<String, Integer> ret = new HashMap<>();
        if(Skills.getTotalLevel() >= 75) {
            ret.put("Leather boots", 1);
            if(ConfigManager.getInstance().getConfigBoolean("use_profile_cape")) {
                ret.put(ConfigManager.getInstance().getConfigString("profile_cape_type"), 1);
            }
        }
        return ret;
    }

    public static boolean isMember() {
        if(!ConfigManager.getInstance().getConfigBoolean("profile_p2p_allowed")) {
            return false;
        }

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
        } catch (IOException e) {
            e.printStackTrace();
        }

        return name;
    }

    public static boolean canEquipTool(String toolName) {
        if(!levels.containsKey(toolName)) {
            return true;
        }

        return levels.containsKey(toolName) && Skills.getRealLevel(Skill.ATTACK) >= levels.get(toolName);
    }

    public static NPC getNpcOnTile(Tile tile) {
        return NPCs.closest(n -> n.getName().contains("Fishing") && n.getTile().equals(tile));
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

    public static void moveMouseInOrOut() {
        if(Calculations.random(4) == 1) {
            Mouse.moveOutsideScreen();
        } else {
            Mouse.move();
        }
    }

    public static void moveMouse() {
        if(Calculations.random(10) == 1) {
            moveMouseInOrOut();
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
}
