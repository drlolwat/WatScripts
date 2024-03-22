package org.lolwat.tasks.types.tutorial;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.hint.HintArrow;
import org.dreambot.api.methods.hint.HintArrowType;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
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
        if (!area.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(area, this));
            return;
        }

        if (TutorialUtils.needsOpenTab()) {
            TutorialUtils.handleTab();
        }

        if (hasCookedFish) {
            TaskManager.getInstance().setCurrentTask(new CookingInstructorTask());
            return;
        }

        if(Inventory.contains("Burnt shrimp")) {
            hasLitFire = false;
            hasCookedFish = false;
            NPCs.closest("Fishing spot").interact("Net");
            Sleep.sleepUntil(() -> Inventory.contains("Raw shrimps") && !Players.getLocal().isAnimating() && !Players.getLocal().isMoving(), 15000);
        }

        if (HintArrow.exists()) {
            if (HintArrow.getType().equals(HintArrowType.NPC)) {
                if (NPCs.closest("Survival Expert").getTile().equals(HintArrow.getTile())) {
                    DialogueUtils.talkTo("Survival Expert");
                } else {
                    NPC spot = GenericUtils.getNpcOnTile(HintArrow.getTile());
                    if (spot != null) {
                        spot.interact("Net");
                        Sleep.sleepUntil(() -> Inventory.contains("Raw shrimps") && !Players.getLocal().isAnimating() && !Players.getLocal().isMoving(), 15000);
                    }
                }

                return;
            } else {
                if (!Inventory.contains("Logs")) {
                    if (!hasLitFire) {
                        GameObject obj = GameObjects.closest("Tree");
                        if (obj != null && obj.interact()) {
                            Sleep.sleepUntil(() -> Inventory.contains("Logs") && !Players.getLocal().isAnimating() && !Players.getLocal().isMoving(), 15000);
                        }
                    }
                }
            }
        }

        if (Inventory.contains("Raw shrimps")) {
            if (!hasLitFire && Inventory.contains("Logs")) {
                if (Inventory.interact("Tinderbox")) {
                    if(Inventory.interact("Logs")) {
                        Tile t = Players.getLocal().getTile();
                        Sleep.sleepUntil(() -> (hasLitFire && Players.getLocal().getTile() != t && !Players.getLocal().isMoving() && !Players.getLocal().isAnimating()), 15000);
                    }
                }
            }

            if (hasLitFire) {
                Sleep.sleep(1500, 2000); // hmm
                if (Inventory.interact("Raw shrimps", "Use") && GameObjects.closest("Fire") != null && GameObjects.closest("Fire").interact()) {
                    Sleep.sleepUntil(() -> (Inventory.contains("Shrimps") && !Inventory.contains("Raw shrimps")) || !Players.getLocal().isAnimating() && !Players.getLocal().isMoving() && !Dialogues.canContinue(), 15000);
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
            Sleep.sleep(100, 150);
        } else if(skill.getName().toLowerCase().contains("fire")) {
            hasLitFire = true;
            Sleep.sleep(100, 150);
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
    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}
