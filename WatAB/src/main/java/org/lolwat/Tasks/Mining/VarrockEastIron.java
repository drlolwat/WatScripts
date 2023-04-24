package org.lolwat.Tasks.Mining;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.lolwat.Tasks.DynamicBankingTask;
import org.lolwat.Tasks.WatTask;
import org.lolwat.Utils.ItemUtils;
import org.lolwat.WatMiner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class VarrockEastIron implements WatTask {
    private Tile defaultSquare = new Tile(3286, 3368);
    private Tile alternateSquare = new Tile(3285, 3370);

    private List<Tile> defaultRocks;
    private List<Tile> alternateRocks;

    @Override
    public String getName() {
        return "Varrock East Iron";
    }

    public VarrockEastIron() {
        levelRequirements.put(Skill.MINING, 15);

        defaultRocks = Arrays.asList(new Tile(3286, 3369), new Tile(3285, 3368));
        alternateRocks = Arrays.asList(new Tile(3288, 3370), new Tile(3285, 3369));
    }

    @Override
    public void execute(WatMiner instance) {
        String pickaxe = ItemUtils.getBestPickaxeForLevel();
        if(!Inventory.contains(pickaxe) && !Equipment.contains(pickaxe)) {
            Logger.log("No pickaxe, best one I can use is " + pickaxe);
            HashMap<String, Integer> bankItems = new HashMap<String, Integer>() {
                {
                    put(pickaxe, 1);
                }
            };

            instance.currentTask = new DynamicBankingTask("Grabbing Pickaxe", bankItems, true, this, true);
        } else {

        }
    }

    @Override
    public int loopTime() {
        return 5;
    }

    @Override
    public boolean requiresTradeUnrestricted() {
        return true;
    }
}
