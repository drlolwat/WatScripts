package org.lolwat.tasks.blast;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skill;
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
import org.lolwat.WatScript;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.blastmine.BlastArea;
import org.lolwat.misc.blastmine.MineCavity;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.tasks.food.HealingTask;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.HopperTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.*;

public class BlastMiningTask implements WatTask {
    private final HashMap<String, Object> data = new HashMap<>();
    private final BlastArea usingArea;
    private int currentIteration = 1;

    public BlastMiningTask() {
        final Tile bTileA = new Tile(1504, 3863);
        final Tile bTileB = new Tile(1506, 3865);
        final Tile bTileC = new Tile(1506, 3867);
        final Tile bTileD = new Tile(1504, 3869);
        final Tile bTileE = new Tile(1507, 3867);
        final Tile bTileF = new Tile(1507, 3865);
        final Tile bCavityA = new Tile(1505, 3863);
        final Tile bCavityB = new Tile(1506, 3864);
        final Tile bCavityC = new Tile(1506, 3868);
        final Tile bCavityD = new Tile(1505, 3869);
        final Tile bCavityE = new Tile(1507, 3868);
        final Tile bCavityF = new Tile(1507, 3864);

        List<BlastArea> areas = new ArrayList<>();
        areas.add(new BlastArea(
                new Area(1500, 3865, 1503, 3862),
                new Area(1503, 3869, 1507, 3865),
                Arrays.asList(
                        new MineCavity(bTileA, bCavityA, "Excavate"),
                        new MineCavity(bTileA, bCavityA, "Load"),
                        new MineCavity(bTileA, bCavityA, "Light"),
                        new MineCavity(bTileA, bCavityA, "Take"),

                        new MineCavity(bTileB, bCavityB, "Excavate"),
                        new MineCavity(bTileB, bCavityB, "Load"),
                        new MineCavity(bTileB, bCavityB, "Light"),
                        new MineCavity(bTileB, bCavityB, "Take"),

                        // 3
                        new MineCavity(bTileE, bCavityE, "Excavate"),
                        new MineCavity(bTileE, bCavityE, "Load"),
                        new MineCavity(bTileE, bCavityE, "Light"),
                        new MineCavity(bTileE, bCavityE, "Take"),


                        new MineCavity(bTileD, bCavityD, "Excavate"),
                        new MineCavity(bTileD, bCavityD, "Load"),
                        new MineCavity(bTileD, bCavityD, "Light"),
                        new MineCavity(bTileD, bCavityD, "Take"),

                        //5
                        new MineCavity(bTileC, bCavityC, "Excavate"),
                        new MineCavity(bTileC, bCavityC, "Load"),
                        new MineCavity(bTileC, bCavityC, "Light"),
                        new MineCavity(bTileC, bCavityC, "Take"),

                        new MineCavity(bTileF, bCavityF, "Excavate"),
                        new MineCavity(bTileF, bCavityF, "Load"),
                        new MineCavity(bTileF, bCavityF, "Light"),
                        new MineCavity(bTileF, bCavityF, "Take")
                ))
        );

        Collections.shuffle(areas);
        usingArea = areas.get(0);
    }

    @Override
    public String getName() {
        return "Blast Mining";
    }

    @Override
    public void execute() {
        if(Combat.getHealthPercent() <= 30) {
            TaskManager.getInstance().setCurrentTask(new HealingTask(this));
            return;
        }

        if (!bank() && !Map.isTileOnMap(usingArea.getStartArea().getRandomTile())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(usingArea.getStartArea(), this));
            return;
        }

        if (!Tabs.isOpen(Tab.INVENTORY)) {
            Tabs.open(Tab.INVENTORY);
            Sleep.sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), 5000);
        }

        if (Bank.isOpen()) {
            Bank.close();
            Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);
            return;
        }

        if (bank())
            return;

        Item potion = Inventory.get(x -> x != null && !x.isNoted() && x.getName().contains("Stamina potion") && x.hasAction("Drink"));
        if(potion == null) {
            Logger.log("need to bank: stamina potion");
            deposit(true);
            TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 4, this, null));
            return;
        }

        int iteration = 1;
        for (MineCavity c : usingArea.getRun()) {
            if (c == null) continue;

            if (Dialogues.inDialogue()) {
                Dialogues.continueDialogue();
            }

            if (iteration < currentIteration) {
                Logger.log("skipping iteration " + iteration + " because it was already done before");
                iteration++;
                continue;
            } else {
                currentIteration = iteration;
            }

            if(Combat.getHealthPercent() <= 30) {
                TaskManager.getInstance().setCurrentTask(new HealingTask(this));
                return;
            }

            Logger.log("running iteration " + iteration);
            Item loopPotion = Inventory.get(x -> x != null && !x.isNoted() && x.getName().contains("Stamina potion") && x.hasAction("Drink"));
            if(loopPotion == null) {
                Logger.log("need to bank: stamina potion");
                deposit(true);
                TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 4, this, null));
                return;
            }
            else {
                if (Walking.getRunEnergy() <= 20 || (Walking.getRunEnergy() <= 70 && !Walking.isStaminaActive())) {
                    if (!loopPotion.interact("Drink")) {
                        Logger.log("failed to drink stamina potion");
                    }
                }
            }

            if (GenericUtils.tooManyPlayers(usingArea.getHopArea(), 1, false)) {
                deposit(true);
                TaskManager.getInstance().setCurrentTask(new HopperTask(0, this));
                return;
            }

            if (Players.getLocal().getTile().equals(c.getAdjacentTile())) {
                Logger.log("running object, next to");
                runObject(c);
            } else {
                Logger.log("running object, need to move");
                Walking.walk(c.getAdjacentTile());
                Sleep.sleepUntil(() -> Players.getLocal().getTile().equals(c.getAdjacentTile()) && !Players.getLocal().isAnimating(), 2000);
                runObject(c);
            }

            if (bank()) {
                Logger.log("banking after iteration " + iteration);
                return;
            }

            iteration++;
            if (iteration > usingArea.getRun().size()) {
                currentIteration = 1;
                break;
            }
        }
    }

    private void denote() {
        if(ItemUtils.inventoryCount("Dynamite", false) == 0) {
            if(Inventory.use("Dynamite")) {
                GameObject chest = GameObjects.closest(x -> x != null && x.getName().equals("Bank chest"));
                if(chest != null && chest.interact("Use")) {
                    Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    if(Dialogues.inDialogue()) {
                        Dialogues.clickOption("Yes");
                        Sleep.sleepUntil(() -> ItemUtils.inventoryCount("Dynamite", false) > 0, 5000);
                    }
                }
            } else {
                Logger.log("failed to use dynamite");
            }
        }
    }

    private void deposit(boolean any) {
        GameObject oreSack = GameObjects.closest(x -> x != null && x.getName().contains("Ore sack"));
        if(((any && Inventory.contains("Blasted ore")) || Inventory.count("Blasted ore") >= 8) && oreSack != null) {
            Logger.log("need to deposit blasted ore, any:" + any);
            if(!oreSack.interact("Deposit")) {
                Logger.log("failed to deposit into ore sack");
            }

            Sleep.sleepUntil(() -> !Inventory.contains("Blasted ore"), 5000);
        }

        if(!any)
            denote();
    }

    private boolean bank() {
        if(hasThreshold()) {
            Logger.log("threshold reached, banking");
            deposit(true);
            collectRawOre();
            return true;
        }

        if(!Inventory.contains("Dynamite") || !Inventory.contains("Chisel") || !Inventory.contains("Tinderbox")) {
            Logger.log("need to bank for dynamite, chisel, or tinderbox");
            deposit(true);
            TaskManager.getInstance().setCurrentTask(
                    new BankingTask(null, null, 4, this, null)
            );
            return true;
        }

        deposit(false);
        return false;
    }

    private void collectRawOre() {
        Logger.log("collecting raw ore");
        if(!Inventory.isEmpty()) {
            Logger.log("need to bank before collecting raw ore");
            if (!Bank.isOpen()) {
                ItemUtils.bank(this);
            }

            Bank.depositAllItems();
            Sleep.sleepUntil(Inventory::isEmpty, 5000);
            return;
        }

        if(Bank.isOpen()) {
            Logger.log("closing bank");
            Bank.close();
            Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);
            return;
        }

        Area operatorArea = new Tile(1498, 3864).getArea(5);
        if(!operatorArea.contains(Players.getLocal().getTile())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(operatorArea, this));
            return;
        }

        NPC operator = NPCs.closest(x -> x != null && x.getName().equals("Operator") && x.hasAction("Collect"));
        if(operator != null) {
            Logger.log("interacting with operator");
            if(!operator.interact("Collect")) {
                Logger.log("issue collecting raw ore");
            }

            Sleep.sleepUntil(() -> !Inventory.isEmpty(), 15000);
        } else {
            Logger.log("no operator found");
        }
    }

    private boolean hasThreshold() {
        int threshold = 300;
        int coal = PlayerSettings.getBitValue(10698);
        int gold = PlayerSettings.getBitValue(10699);
        int mith = PlayerSettings.getBitValue(10700);
        int adam = PlayerSettings.getBitValue(10701);
        int rune = PlayerSettings.getBitValue(10702);

        Logger.log("coal: " + coal + ", gold: " + gold + ", mith: " + mith + ", adam: " + adam + ", rune: " + rune);
        return (coal >= threshold || gold >= threshold || mith >= threshold || adam >= threshold || rune >= threshold);
    }

    private void runObject(MineCavity c) {
        List<GameObject> cavities = GameObjects.all(x -> x != null && !x.getName().equals("null") && x.getTile().equals(c.getObjTile()));
        for(GameObject cavity : cavities) {
            if (cavity != null) {
                if(c.getAction().equals("Take")) {
                    if(!Inventory.isFull()) {
                        List<GroundItem> ores = GroundItems.all(x -> x != null
                                && x.getTile().equals(Players.getLocal().getTile())
                                && x.getName().contains("Blasted ore"));

                        for(GroundItem ore : ores) {
                            if(Inventory.isFull())
                                break;

                            if (ore != null) {
                                if (!ore.interact("Take")) {
                                    Logger.log("failed to take ore " + ore.getName());
                                    continue;
                                }

                                Sleep.sleepUntil(() -> !ore.exists(), 5000);
                                if (!ore.exists()) {
                                    WatScript.oresPicked++;
                                }
                            }
                        }
                    }
                }
                else {
                    if(!Players.getLocal().getTile().equals(c.getAdjacentTile())) {
                        Walking.walk(c.getAdjacentTile());
                        Sleep.sleepUntil(() -> Players.getLocal().getTile().equals(c.getAdjacentTile()) && !Players.getLocal().isAnimating(), 2000);
                    }

                    if (cavity.hasAction(c.getAction())) {
                        if (!cavity.interact(c.getAction())) {
                            Logger.log("failed to interact with cavity: " + cavity.getName() + " with action: " + c.getAction());
                        } else {
                            Logger.log("interacted with cavity");
                            if (c.getAction().equals("Load")) {
                                WatScript.dynamitePlaced++;
                            }
                        }

                        Sleep.sleepUntil(() -> !cavity.exists()
                                        || (!Players.getLocal().isAnimating() && !cavity.hasAction(c.getAction()))
                                        || Dialogues.inDialogue(),
                                5000);

                        Sleep.sleep(Calculations.random(100, 300));
                    } else {
                        Logger.log("cavity does not have action: " + c.getAction());
                    }
                }
            } else {
                Logger.log("cavity is null");
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
        return Skill.MINING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        HashMap<String, Integer> ret = new HashMap<>();
        ret.put("Ring of wealth (", 1);

        if(usingArea != null && !Map.isTileOnMap(usingArea.getRun().get(0).getAdjacentTile())) {
            ret.put("Games necklace(", 1);
        }

        ret.put("Graceful hood", 1);
        ret.put("Graceful cape", 1);
        ret.put("Graceful top", 1);
        ret.put("Graceful legs", 1);
        ret.put("Graceful gloves", 1);
        ret.put("Graceful boots", 1);
        return ret;
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        HashMap<String, Integer> ret = new HashMap<>();
        ret.put("Dynamite", -500);
        ret.put("Chisel", 1);
        ret.put("Tinderbox", 1);
        ret.put("Stamina potion(", 1);
        return ret;
    }

    @Override
    public HashMap<String, Object> data() {
        return data;
    }

    @Override
    public void onMessage(Message m) {
        if(!m.getMessage().contains(":") && m.getMessage().endsWith("mining here.")) {
            TaskManager.getInstance().setCurrentTask(new HopperTask(0, this, usingArea));
        }
    }
}
