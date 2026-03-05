package org.lolwat.misc.utils;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericUtils {
    private static final Map<Player, Long> playerEntryTimes = new HashMap<>();
    private static int hopperTime = Calculations.random(8, 20);

    public static boolean tooManyPlayers(Area area, int count, boolean ignoreTime) {
        long currentTime = System.currentTimeMillis();
        int playerCount = 0;

        for (Player ply : Players.all()) {
            if (Players.getLocal().equals(ply)) continue;
            if (area.contains(ply)) {
                playerEntryTimes.putIfAbsent(ply, currentTime);

                if (ignoreTime || currentTime - playerEntryTimes.get(ply) > (hopperTime * 1000L)) {
                    playerCount++;
                }
            } else {
                playerEntryTimes.remove(ply);
            }
        }

        return playerCount >= count;
    }

    public static boolean equipItem(String item, Item old) {
        Item i = Inventory.get(x -> x != null && x.getName().contains((item)));
        if (i != null) {
            if (i.hasAction("Wear") && i.interact("Wear")) {
                Logger.log("Equipment: Equipped wearable");
                Sleep.sleep(100, 200);

                if (Bank.isOpen() && old != null) {
                    Sleep.sleep(200, 400);
                    Bank.depositAll(old.getName());
                    Logger.log("Equipment: Deposited old item: " + old.getName());
                }

                return true;
            } else if (i.hasAction("Wield") && i.interact("Wield")) {
                Logger.log("Equipment: Equipped wieldable");
                Sleep.sleep(100, 200);

                if (Bank.isOpen() && old != null) {
                    Sleep.sleep(200, 400);
                    Bank.depositAll(old.getName());
                    Logger.log("Equipment: Deposited old item: " + old.getName());
                }

                return true;
            } else if (i.hasAction("Equip") && i.interact("Equip")) {
                Logger.log("Equipment: Equipped equippable");
                Sleep.sleep(100, 200);

                if (Bank.isOpen() && old != null) {
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
        return PlayerSettings.getConfig(1780) >= 2;
    }

    public static int getMemberDays() {
        return PlayerSettings.getConfig(1780);
    }

    public static void handleSpecial() {
        List<String> weapons = new ArrayList<String>() {
            {
                add("Magic shortbow");
                add("Dragon sword");
                add("Dragon scimitar");
            }
        };

        for (String s : weapons) {
            if (Equipment.slotContains(EquipmentSlot.WEAPON, s)) {
                if (Combat.getSpecialPercentage() > 75) {
                    Combat.toggleSpecialAttack(true);
                }
            }
        }
    }

    public static boolean performEmergencyWork() {
        if(!GenericUtils.isMember()) {
            return false;
        }

        if(WorldHopper.isWorldHopperOpen()) {
            if(!WorldHopper.closeWorldHopper()) {
                Logger.log("failed to close world hopper in emergency");
                return false;
            }
        }

        if (Combat.isPoisoned() || Combat.isEnvenomed() || Combat.isDiseased()) {
            Logger.log("we are poisoned during a non combat task, handling");
            Item antidote = Inventory.get(x -> x != null && !x.isNoted() && x.hasAction("Drink") && x.getName().contains("Antidote++"));
            if(antidote != null) {
                if (!antidote.interact("Drink")) {
                    Logger.log("failed to drink antidote in emergency");
                }
            }

            Sleep.sleepUntil(() -> !Combat.isPoisoned(), 5000);
            return true;
        }

        if(!Tabs.isOpen(Tab.INVENTORY)) {
            if(!Tabs.open(Tab.INVENTORY)) {
                Logger.log("failed to open inventory in emergency");
                return false;
            }
        }

        if(Combat.getHealthPercent() <= 40) {
            Item food = Inventory.get(x -> x != null && x.hasAction("Eat"));
            if(food != null) {
                if(!food.interact("Eat")) {
                    Logger.log("failed to eat food in emergency");
                }

                return true;
            }
        }

        if(Walking.getRunEnergy() <= 30) {
            Item potion = Inventory.get(x -> x != null && x.getName().contains("Stamina potion") && x.hasAction("Drink"));
            if(potion != null) {
                if(!potion.interact("Drink")) {
                    Logger.log("failed to drink stamina potion in emergency");
                }

                return true;
            }
        }

        if(Skills.getBoostedLevel(Skill.PRAYER) <= 30) {
            Item potion = Inventory.get(x -> x != null && x.getName().contains("Prayer potion") && x.hasAction("Drink"));
            if(potion != null) {
                if(!potion.interact("Drink")) {
                    Logger.log("failed to drink prayer potion in emergency");
                }

                return true;
            }
        }

        return false;
    }

    public static void castHomeTeleport() {
        if (Magic.canCast(Normal.HOME_TELEPORT)) {
            if (!Tabs.isOpen(Tab.MAGIC)) {
                Tabs.open(Tab.MAGIC);
                Sleep.sleepUntil(() -> Tabs.isOpen(Tab.MAGIC), Calculations.random(200, 300));
            }

            if (Magic.castSpell(Normal.HOME_TELEPORT)) {
                Tile currentTile = Players.getLocal().getTile();
                Sleep.sleepUntil(() -> Players.getLocal().getTile() != currentTile && !Players.getLocal().isAnimating(), 5000);
            }

            if (!Tabs.isOpen(Tab.INVENTORY)) {
                Tabs.open(Tab.INVENTORY);
                Sleep.sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), Calculations.random(200, 300));
            }
        }
    }

    public static int[] parseBlowpipeData(String text) {
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

    public static int getHopperTime() {
        return hopperTime;
    }

    public static void setHopperTime(int hopperTime) {
        GenericUtils.hopperTime = hopperTime;
    }
}
