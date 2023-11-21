package org.lolwat.tasks.types.tutorial;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.hint.HintArrow;
import org.dreambot.api.methods.hint.HintArrowType;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.lolwat.WatAIO;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.TutorialUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.util.HashMap;

public class SurvivalInstructorTask implements WatTask {
    boolean hasLitFire = false;
    boolean hasCookedFish = false;

    Area area = new Area(
            new Tile[] {
                    new Tile(3096, 3103, 0),
                    new Tile(3096, 3090, 0),
                    new Tile(3104, 3090, 0),
                    new Tile(3107, 3095, 0),
                    new Tile(3106, 3099, 0),
                    new Tile(3103, 3103, 0)
            }
    );

    @Override
    public String getName() {
        return "Tutorial: Survival Instructor";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute(WatAIO instance) {
        if(!area.contains(Players.getLocal())) {
            instance.currentTask = new TraversalTask(area, this);
            return;
        }

        if (TutorialUtils.needsOpenTab()) {
            TutorialUtils.handleTab();
        }

        if(hasCookedFish) {
            instance.currentTask = new CookingInstructorTask();
            return;
        }

        if(HintArrow.exists()) {
            if(HintArrow.getType().equals(HintArrowType.NPC)) {
                if(HintArrow.getType().name().equals("Survial Expert")) {
                    DialogueUtils.talkTo("Survival Expert");
                } else {
                    NPC spot = GenericUtils.getNpcOnTile(HintArrow.getTile());
                    if(spot != null) {
                        spot.interact("Net");
                    }
                }
            } else {
                if(Inventory.contains("Raw shrimps")) {
                    if (!Inventory.contains("Logs")) {
                        if(!hasLitFire) {
                            GameObject obj = GameObjects.closest("Tree");
                            if (obj != null && obj.interact()) {
                                Sleep.sleepUntil(() -> Inventory.contains("Logs"), Calculations.random(5000, 10000));
                            }
                        }

                        if(Inventory.interact("Raw shrimps") && GameObjects.closest("Fire").interact()) {
                            Sleep.sleep(100, 150);
                        }
                    } else {
                        if(Inventory.interact("Tinderbox") && Inventory.interact("Logs")) {
                            Sleep.sleep(100, 150);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return Calculations.random(350, 450);
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {
        if(skill.getName().toLowerCase().contains("cook")) {
            hasCookedFish = true;
        } else if(skill.getName().toLowerCase().contains("fire")) {
            hasLitFire = true;
        }
    }

    @Override
    public Skill trainsSkill() {
        return null;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public Quest completesQuest() {
        return null;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }
}
