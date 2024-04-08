package org.lolwat.tasks.types.runecrafting;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.runecrafting.RunecraftingUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.misc.BankingTask;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.util.HashMap;
import java.util.List;

public class RunecraftingTask implements WatTask {
    final int minimumLevel;
    final int avoidAfterLevel;
    final String runeType; //TODO write class for Runes

    public RunecraftingTask(String runeType, int minimumLevel, int avoidAfterLevel) {
        this.runeType = runeType;
        this.minimumLevel = minimumLevel;
        this.avoidAfterLevel = avoidAfterLevel;
    }

    @Override
    public void execute(WatAIO instance) {
        Area entrance = RunecraftingUtils.getRunecraftingAreaByRune(runeType);
        String tiara = GenericUtils.uppercaseFirst(runeType) + " tiara";

        GameObject altar = GameObjects.closest(x -> x != null && x.canReach() && x.getName().equals("Altar"));
        if (altar != null && altar.exists() && altar.getID() != 14860) { // 14860 was fucking me up in Varrock east
            if (Inventory.contains(x -> x != null && x.getName().contains("essence")) && !altar.interact("Craft-rune")) {
                Logger.log("Failed to craft rune");
            } else {
                Sleep.sleepUntil(() -> !Inventory.contains(x -> x != null && x.getName().contains("essence")), Calculations.random(3000, 6000));
            }

            if(!Inventory.contains(x -> x != null && x.getName().contains("essence"))) {
                GameObject portal = GameObjects.closest(x -> x != null && x.exists() && x.canReach() && x.getName().contains("Portal"));
                if(portal != null) {
                    if(!portal.interact("Use")) {
                        Logger.log("Failed to use portal (safe to ignore)");
                    } else {
                        Sleep.sleepUntil(() -> GameObjects.closest(x -> x != null && x.getName().contains("Mysterious ruins")) != null, Calculations.random(3000, 6000));
                    }
                }
            }
        } else {
            if (!Inventory.contains(x -> x != null && x.getName().toLowerCase().contains("essence") && !x.isNoted())
                    || !Equipment.contains(x -> x != null && x.getName().contains(tiara))) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(null, new HashMap<String, Integer>() {{
                    put(GenericUtils.uppercaseFirst(runeType) + " rune", -Calculations.random(3000, 5000));
                }}, Calculations.random(30, 40), this));
                return;
            }

            GameObject ruins = GameObjects.closest(x -> x != null && x.canReach() && x.getName().contains("Mysterious ruins"));

            if ((altar == null || !altar.canReach()) && !entrance.contains(Players.getLocal()) && !entrance.contains(Walking.getDestination())) {
                TaskManager.getInstance().setCurrentTask(new TraversalTask(entrance, this));
                return;
            }

            if (entrance.contains(Players.getLocal()) || entrance.contains(Walking.getDestination())) {
                if (ruins != null && !ruins.interact("Enter")) {
                    Logger.log("Failed to enter ruins (safe to ignore)");
                } else {
                    Sleep.sleepUntil(() -> (ruins == null || !ruins.canReach()) && (altar != null && altar.canReach()), Calculations.random(3000, 6000));
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Crafting " + runeType + " runes";
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.RUNECRAFTING) >= minimumLevel;
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
        return Skill.RUNECRAFTING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return avoidAfterLevel;
    }

    @Override
    public Quest completesQuest() {
        return null;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<String, Integer>() {{
            put(GenericUtils.uppercaseFirst(runeType) + " tiara", 1);
        }};
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<String, Integer>() {{
            put("Pure essence", 28);
        }};
    }

    @Override
    public List<String> inventoryTolerated() {
        return WatTask.super.inventoryTolerated();
    }

    @Override
    public boolean requiresMembers() {
        return WatTask.super.requiresMembers();
    }
}
