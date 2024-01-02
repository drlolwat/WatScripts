package org.lolwat.misc.utils;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.WatAIO;
import org.lolwat.tasks.types.misc.HopperTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class GenericUtils {
    private static HashMap<String, Integer> levels = new HashMap<String, Integer>() {
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
        }
    };

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
                Logger.log("EQUIPMENTCHECKER: EQUIPPED WEARABLE");
                Sleep.sleep(100, 200);

                if(Bank.isOpen() && old != null) {
                    Sleep.sleep(200, 400);
                    Bank.depositAll(old.getName());
                    Logger.log("EQUIPMENTCHECKER: DEPOSITED OLD WEARABLE: " + old.getName());
                }

                return true;
            }
            else if(i.hasAction("Wield") && i.interact("Wield")) {
                Logger.log("EQUIPMENTCHECKER: EQUIPPED WEAPON/AMMO");
                Sleep.sleep(100, 200);

                if(Bank.isOpen() && old != null) {
                    Sleep.sleep(200, 400);
                    Bank.depositAll(old.getName());
                    Logger.log("EQUIPMENTCHECKER: DEPOSITED OLD WEP/TOOL: " + old.getName());
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
            ret.put(WatAIO.CAPE_TYPE, 1);
        }
        return ret;
    }

    public static String generateUsername() {
        String name = "";
        try {
            URL url = new URL("https://botbuddy.net/_api_/getname.php");

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
}
