package org.lolwat.tasks.shamans;

import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.prayer.Prayer;
import org.dreambot.api.methods.prayer.Prayers;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.HopperTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ShamanCombatTask implements WatTask {
    boolean reachedLocation = false;
    NPC currentTarget = null;
    private HashMap<String, Object> data = new HashMap<>();
    private Queue<GroundItem> groundItemQueue = new LinkedList<>();
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    boolean scheduled = false;
    Area monsterArea = new Area(1289, 10100, 1296, 10093);
    Area topLeft = new Area(1292, 10100, 1289, 10097);
    Area topRight = new Area(1296, 10100, 1293, 10097);
    Area bottomLeft = new Area(1289, 10096, 1292, 10093);
    Area bottomRight = new Area(1296, 10093, 1293, 10096);
    HashMap<String, Integer> sellables;

    List<String> stackables = Arrays.asList("Chaos rune",
            "Death rune",
            "Coal",
            "Iron ore",
            "Runite ore",
            "Grimy kwuarm",
            "Grimy cadantine",
            "Grimy dwarf weed",
            "Grimy lantadyme",
            "Ranarr seed",
            "Snapdragon seed",
            "Yew seed",
            "Magic seed",
            "Palm tree seed",
            "Dragonfruit tree seed",
            "Celastrus seed",
            "Redwood tree seed");

    List<String> alchables = Arrays.asList("Rune med helm",
            "Earth battlestaff",
            "Mystic earth staff",
            "Rune warhammer",
            "Rune chainbody",
            "Red d'hide vambraces",
            "Runite bar",
            "Rune 2h sword",
            "Rune battleaxe",
            "Rune sq shield",
            "Dragonstone",
            "Rune kiteshield",
            "Dragon med helm",
            "Rune spear",
            "Shield left half",
            "Dragon spear",
            "Chilli potato",
            "Skills necklace(1)");

    public ShamanCombatTask() {
        sellables = new HashMap<>();
        for (String s : stackables) {
            sellables.put(s, -1);
        }
        sellables.put("Dragon warhammer", -1);
    }

    @Override
    public String getName() {
        return "Killing Shamans";
    }

    @Override
    public void execute() {
        if (!reachedLocation) {
            for (Map.Entry<String, Integer> entry : inventoryRequired().entrySet()) {
                if (!ItemUtils.inventoryContains(entry.getKey(), entry.getValue(), false)) {
                    Logger.log("missing " + entry.getKey());
                    TaskManager.getInstance().setCurrentTask(new BankingTask(inventoryRequired(), null, 5, this, null));
                    return;
                }
            }

            for (Map.Entry<String, Integer> entry : clothesRequired().entrySet()) {
                if (!ItemUtils.equipmentContains(entry.getKey(), entry.getValue())) {
                    Logger.log("missing " + entry.getKey());
                    TaskManager.getInstance().setCurrentTask(new BankingTask(inventoryRequired(), null, 5, this, null));
                    return;
                }
            }
        }

        if (!monsterArea.contains(Players.getLocal())) {
            Area lowerLanding = new Tile(1312, 10086).getArea(3);
            if (!lowerLanding.contains(Players.getLocal())) {
                Area topEntrance = new Tile(1313, 3683, 0).getArea(2);
                if (!topEntrance.contains(Players.getLocal())) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(topEntrance, this));
                    return;
                }

                GameObject entrance = GameObjects.closest(34405);
                if (entrance != null) {
                    if (!entrance.interact()) {
                        Logger.log("failed to interact w entrance");
                        return;
                    }
                }

                Sleep.sleepUntil(() -> lowerLanding.contains(Players.getLocal()) || Dialogues.canContinue(), 2000);
            }

            GameObject gate = GameObjects.closest(34642);
            if (gate != null) {
                if (!gate.interact()) {
                    Logger.log("failed to interact w gate");
                    return;
                }

                Sleep.sleepUntil(() -> monsterArea.contains(Players.getLocal()), 20000);
            }

            return;
        }

        if (!reachedLocation) {
            Logger.log("reached location for monster");
            reachedLocation = true;
        }

        Item antidote = Inventory.get(x -> x != null && !x.isNoted() && x.hasAction("Drink") && x.getName().contains("Antidote++"));
        Item food = Inventory.get(x -> x != null && !x.isNoted() && x.hasAction("Eat") && x.getName().contains("Shark"));
        Item rangingPotion = Inventory.get(x -> x != null && !x.isNoted() && x.hasAction("Drink") && x.getName().contains("Ranging potion"));
        Item prayerPotion = Inventory.get(x -> x != null && !x.isNoted() && x.hasAction("Drink") && x.getName().contains("Prayer potion"));
        Item natureRune = Inventory.get(x -> x != null && x.getName().contains("Nature rune"));
        Item staminaPotion = Inventory.get(x -> x != null && !x.isNoted() && x.hasAction("Drink") && x.getName().contains("Stamina potion"));
        Item amethystArrow = Equipment.get(x -> x != null && x.getName().contains("Amethyst arrow"));

        if (antidote == null || food == null || rangingPotion == null || prayerPotion == null || natureRune == null || amethystArrow == null) {
            Logger.log("we are missing a vital item");
            reachedLocation = false;
            currentTarget = null;

            TaskManager.getInstance().setCurrentTask(new BankingTask(inventoryRequired(), null, 5, this, null));
            TaskManager.getInstance().getCurrentTask().execute();
            if(!Prayers.toggle(false, Prayer.PROTECT_FROM_MISSILES)) {
                Logger.log("failed to toggle protect from missiles");
            }

            return;
        }

        if(!Combat.isAutoRetaliateOn()) {
            if(!Combat.toggleAutoRetaliate(true)) {
                Logger.log("failed to toggle auto retaliate on");
                return;
            }

            Sleep.sleepUntil(Combat::isAutoRetaliateOn, 1000);
        }

        if(Combat.getHealthPercent() <= 65) {
            Item i = Inventory.get(x -> x != null && x.hasAction("Eat"));
            if (i != null && i.interact()) {
                return;
            }
        }

        if (Combat.isPoisoned() || Combat.isEnvenomed() || Combat.isDiseased()) {
            if (!antidote.interact("Drink")) {
                Logger.log("failed to drink antidote");
                return;
            }

            Sleep.sleepUntil(() -> !Combat.isPoisoned(), 5000);
        }

        if(Walking.getRunEnergy() <= 30) {
            if (!staminaPotion.interact("Drink")) {
                Logger.log("failed to drink stamina potion");
                return;
            }
        }

        if(!Walking.isRunEnabled()) {
            Walking.toggleRun();
            Sleep.sleepUntil(Walking::isRunEnabled, 5000);
        }

        if(Skills.getBoostedLevel(Skill.RANGED) == Skills.getRealLevel(Skill.RANGED)) {
            if (!rangingPotion.interact("Drink")) {
                Logger.log("failed to drink ranging potion");
                return;
            }

            Sleep.sleepUntil(() -> Skills.getBoostedLevel(Skill.RANGED) > Skills.getRealLevel(Skill.RANGED), 5000);
        }

        if(Skills.getBoostedLevel(Skill.PRAYER) <= 30) {
            if (!prayerPotion.interact("Drink")) {
                Logger.log("failed to drink prayer potion");
                return;
            }

            Sleep.sleepUntil(() -> Skills.getBoostedLevel(Skill.PRAYER) > 30, 5000);
        }

        for(Prayer p : Prayers.getActive()) {
            if(!p.equals(Prayer.PROTECT_FROM_MISSILES)) {
                if(!Prayers.toggle(false, p)) {
                    Logger.log("failed to toggle prayer off (" + p.name() + ")");
                    return;
                }
            }
        }

        if(!Prayers.toggle(true, Prayer.PROTECT_FROM_MISSILES)) {
            Logger.log("failed to toggle protect from missiles");
            return;
        }

        if(GenericUtils.tooManyPlayers(monsterArea, 1) && Combat.getHealthPercent() >= 65) {
            Logger.log("too many players in area, hopping worlds");
            TaskManager.getInstance().setCurrentTask(new HopperTask(0, this));
            TaskManager.getInstance().getCurrentTask().execute();
            return;
        }

        if(Inventory.contains("Vial")) {
            if(!Inventory.drop("Vial")) {
                Logger.log("failed to drop vial");
            }
        }

        List<NPC> attackingMe = NPCs.all(x -> x != null
                && x.exists()
                && x.isInteracting(Players.getLocal())
                && x.getName().equals("Lizardman shaman")
                && x.isInCombat());

        if(!attackingMe.isEmpty() && currentTarget == null) {
            currentTarget = attackingMe.get(0);
        }

        if(currentTarget == null) {
            Logger.log("current target NULL");
            if (!Players.getLocal().isInCombat() && !Players.getLocal().isHealthBarVisible()) {
                Logger.log("waiting to be in combat...");
                return;
            }
        }
        else {
            if(!Combat.isSpecialActive() && Combat.getSpecialPercentage() >= 50) {
                if (!Combat.toggleSpecialAttack(true)) {
                    Logger.log("failed to toggle special attack");
                    return;
                }
            }

            if(!groundItemQueue.isEmpty()) {
                processGroundItemQueue();
            }

            if(Players.getLocal().canReach(currentTarget.getTile())) {
                if(currentTarget.getTile().distance(Players.getLocal().getTile()) <= 2 && !scheduled && !Players.getLocal().isMoving()) {
                    Logger.log("running too close to npc: " + currentTarget.getName());

                    Area[] areas = {topLeft, topRight, bottomLeft, bottomRight};
                    Area targetArea = null;
                    double maxDistance = -1;

                    for (Area area : areas) {
                        if (NPCs.all(area::contains).isEmpty()) {
                            double distance = Players.getLocal().distance(area.getCenter());
                            if (distance > maxDistance) {
                                maxDistance = distance;
                                targetArea = area;
                            }
                        }
                    }

                    if (targetArea != null) {
                        Tile playerTile = Players.getLocal().getTile();
                        Tile targetTile = null;
                        for (Tile tile : targetArea.getTiles()) {
                            if (playerTile.distance(tile) > 2) {
                                targetTile = tile;
                                break;
                            }
                        }

                        if (targetTile != null) {
                            Walking.walk(targetTile);
                        } else {
                            Logger.log("couldnt run from npc, we dumb");
                        }
                    } else {
                        Logger.log("no empty quadrant to run from npc");
                    }
                }

                if(!Players.getLocal().isMoving()
                        && !Players.getLocal().isAnimating()
                        && !Players.getLocal().isInCombat()
                        && !Players.getLocal().isHealthBarVisible()) {

                    if (!currentTarget.interact("Attack")) {
                        Logger.log("failed to attack target");
                        return;
                    }
                }
            }
        }
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.RANGED;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        HashMap<String, Integer> ret = new HashMap<>();
        ret.put("Ring of wealth (", 1);
        ret.put("Shayzien helm (5)", 1);
        ret.put("Shayzien body (5)", 1);
        ret.put("Shayzien greaves (5)", 1);
        ret.put("Shayzien boots (5)", 1);
        ret.put("Shayzien gloves (5)", 1);
        ret.put("Amulet of fury", 1);
        ret.put("Ava's accumulator", 1);
        ret.put("Magic shortbow (i)", 1);
        ret.put("Amethyst arrow", 1000);
        return ret;
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        HashMap<String, Integer> ret = new HashMap<>();
        ret.put("Ranging potion(4)", 3);
        ret.put("Prayer potion(4)", 7);
        ret.put("Stamina potion(4)", 1);
        ret.put("Shark", 10);
        ret.put("Antidote++(4)", 2);
        ret.put("Skills necklace(", 1);
        ret.put("Nature rune", 500);
        ret.put("Fire rune", 500 * 5);
        return ret;
    }

    @Override
    public void onMessage(Message m) {
        Logger.log("message: " + m.getMessage());
    }

    @Override
    public void onNpcAnimation(NPC npc, int animation, int animationDelay) {
        if (npc == null) return;
        if (!monsterArea.contains(Players.getLocal())) return;
        if(scheduled) return;

        List<Integer> runFromAnimations = Arrays.asList(7152, 7158);
        if (runFromAnimations.contains(animation)) {
            currentTarget = npc;
            Logger.log("running from animation from npc: " + npc.getName());

            Area[] areas = {topLeft, topRight, bottomLeft, bottomRight};
            Area targetArea = null;
            double maxDistance = -1;

            for (Area area : areas) {
                if (NPCs.all(area::contains).isEmpty()) {
                    double distance = Players.getLocal().distance(area.getCenter());
                    if (distance > maxDistance) {
                        maxDistance = distance;
                        targetArea = area;
                    }
                }
            }

            if (targetArea != null) {
                Tile playerTile = Players.getLocal().getTile();
                scheduled = true;
                Area finalTargetArea = targetArea;
                scheduler.schedule(() -> {
                    Tile targetTile = null;
                    for (Tile tile : finalTargetArea.getTiles()) {
                        if (playerTile.distance(tile) >= 2) {
                            targetTile = tile;
                            break;
                        }
                    }
                    if (targetTile != null) {
                        Walking.walk(targetTile);

                        Item i = Inventory.get(x -> x != null && !x.getName().equals("Chilli potato") && alchables.contains(x.getName()));
                        if(i != null) {
                            Logger.log("alching item: " + i.getName());

                            if (!Tabs.isOpen(Tab.MAGIC)) {
                                Tabs.open(Tab.MAGIC);
                            }

                            if (!Magic.castSpell(Normal.HIGH_LEVEL_ALCHEMY)) {
                                Logger.log("error casting HA");
                                return;
                            }

                            if(!Inventory.interact(i)) {
                                Logger.log("failed to alch item: " + i.getName());

                                if(Magic.isSpellSelected()) {
                                    Magic.deselect();
                                }
                            }
                        }
                    } else {
                        Logger.log("couldnt run from anim, we dumb");
                    }
                    scheduled = false;
                }, 600, TimeUnit.MILLISECONDS);
            } else {
                Logger.log("wheres the empty quadrant to run from the anim!");
            }
        }
    }

    @Override
    public void onNpcSpawn(NPC npc) {
        if (npc == null) return;
        if (!monsterArea.contains(Players.getLocal())) return;

        if (npc.getID() == 6768) {
            Logger.log("spawn has spawned lol");

            Area[] areas = {topLeft, topRight, bottomLeft, bottomRight};
            Area targetArea = null;
            double maxDistance = -1;

            for (Area area : areas) {
                if (NPCs.all(area::contains).isEmpty()) {
                    double distance = Players.getLocal().distance(area.getCenter());
                    if (distance > maxDistance) {
                        maxDistance = distance;
                        targetArea = area;
                    }
                }
            }

            if (targetArea != null) {
                Tile furthestTile = null;
                maxDistance = -1;

                for (Tile tile : targetArea.getTiles()) {
                    double distance = Players.getLocal().distance(tile);
                    if (distance > maxDistance) {
                        maxDistance = distance;
                        furthestTile = tile;
                    }
                }

                if (furthestTile != null && !scheduled) {
                    scheduled = true;
                    Tile finalFurthestTile = furthestTile;
                    scheduler.schedule(() -> {
                        Walking.walk(finalFurthestTile);
                        Item i = Inventory.get(x -> x != null && !x.getName().equals("Chilli potato") && alchables.contains(x.getName()));
                        if(i != null) {
                            Logger.log("alching item: " + i.getName());

                            if (!Tabs.isOpen(Tab.MAGIC)) {
                                Tabs.open(Tab.MAGIC);
                            }

                            if (!Magic.castSpell(Normal.HIGH_LEVEL_ALCHEMY)) {
                                Logger.log("error casting HA");
                                return;
                            }

                            if(!Inventory.interact(i)) {
                                Logger.log("failed to alch item: " + i.getName());

                                if(Magic.isSpellSelected()) {
                                    Magic.deselect();
                                }
                            }
                        }
                        scheduled = false;
                    }, 3500, TimeUnit.MILLISECONDS);
                }
            } else {
                Logger.log("no empty quadrant during spawns");
            }
        }
    }

    @Override
    public void onNpcDespawn(NPC npc) {
        if (npc == null) return;
        if(currentTarget != null && currentTarget.equals(npc)) {
            Logger.log("current target despawned");
            currentTarget = null;
        }
    }

    @Override
    public void onGroundItemSpawn(GroundItem object) {
        if (object == null) return;
        if(!stackables.contains(object.getName())
                && !alchables.contains(object.getName())
                && !object.getName().equals("Dragon warhammer")) {
            return;
        }
        groundItemQueue.add(object);
    }


    @Override
    public boolean requiresMembers() {
        return true;
    }

    @Override
    public HashMap<String, Object> data() {
        return data;
    }

    private void processGroundItemQueue() {
        if (groundItemQueue.isEmpty()) return;

        GroundItem item = groundItemQueue.poll();
        if (item == null) return;

        boolean attempted = false;

        if (item.getName().equals("Dragon warhammer")) {
            if (Inventory.isFull()) {
                Logger.log("inventory is full, dropping items");
                for (Item i : Inventory.all()) {
                    if (i == null || !Inventory.isFull()) break;
                    if (!i.getName().equals("Dragon warhammer")) {
                        if (!i.interact("Drop")) {
                            Logger.log("failed to drop item: " + i.getName());
                        }
                    }
                }
            }

            if (!item.interact("Take")) {
                Logger.log("failed to take dragon warhammer");
                return;
            }

            Sleep.sleepUntil(() -> Inventory.contains("Dragon warhammer"), 5000);

            if(Inventory.contains("Dragon warhammer")) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(inventoryRequired(), null, 1, this, null));
                TaskManager.getInstance().getCurrentTask().execute();
            }

            attempted = true;
        }

        if ((Inventory.size() < 27 || (Inventory.contains("Coins") && !Inventory.isFull())) && alchables.contains(item.getName())) {
            if (!item.interact("Take")) {
                Logger.log("failed to take alchable: " + item.getName());
                Sleep.sleepUntil(() -> !item.exists(), 5000);
            }

            attempted = true;
        }

        if ((Inventory.size() < 26 || Inventory.contains(item.getName())) && stackables.contains(item.getName())) {
            if (!item.interact("Take")) {
                Logger.log("failed to take stackable: " + item.getName());
                Sleep.sleepUntil(() -> !item.exists(), 5000);
            }

            attempted = true;
        }

        if(item.exists() && attempted) {
            Logger.log("didnt pick up item, readding to queue: " + item.getName());
            groundItemQueue.add(item);
        }
    }
}
