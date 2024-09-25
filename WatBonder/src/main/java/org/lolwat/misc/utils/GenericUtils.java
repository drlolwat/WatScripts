package org.lolwat.misc.utils;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static void handlePickup(List<String> items) {
        List<String> newItems = new ArrayList<>();
        //TODO ADD
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
            }
        } else {
            Logger.log("GenericUtils: Inventory is full, not picking up item: " + i.getName());
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
        Item i = Inventory.get(x -> x != null && x.getName().contains((item)));
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
            else if(i.hasAction("Equip") && i.interact("Equip")) {
                Logger.log("Equipment: Equipped equippable");
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

    public static int[] parseText(String text) {
        int darts = 0;
        int scales = 0;

        Pattern dartsPattern = Pattern.compile("Darts: <col=007f00>\\w+ dart x ([\\d,]+)</col>");
        Pattern scalesPattern = Pattern.compile("Scales: <col=007f00>([\\d,]+) \\(\\d+\\.\\d+%\\)</col>");

        Matcher dartsMatcher = dartsPattern.matcher(text);
        Matcher scalesMatcher = scalesPattern.matcher(text);

        if (dartsMatcher.find()) {
            String dartsCount = dartsMatcher.group(1).replace(",", "");
            darts = Integer.parseInt(dartsCount);
        }

        if (scalesMatcher.find()) {
            String scalesCount = scalesMatcher.group(1).replace(",", "");
            scales = Integer.parseInt(scalesCount);
        }

        return new int[]{darts, scales};
    }
}
