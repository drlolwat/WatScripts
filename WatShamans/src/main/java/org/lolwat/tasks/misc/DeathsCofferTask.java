package org.lolwat.tasks.misc;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.utilities.AccountManager;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.WatScript;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.tasks.combat.MainCombatTask;

import java.util.Arrays;
import java.util.List;

public class DeathsCofferTask implements WatTask {
    int currentItems = 0;

    List<String> responses = Arrays.asList(
            "Yes, have you got anything for me?",
            "Can I collect the items from that gravestone now?",
            "Bring my items here now; I'll pay your fee."
    );

    Area graveZone = new Area(3238, 3196, 3241, 3191);

    public DeathsCofferTask() {
        currentItems = Inventory.size();
    }

    @Override
    public String getName() {
        return "Collecting Deaths Coffer";
    }

    @Override
    public void execute() {
        NPC death = NPCs.closest("Death");
        if(death != null) {
            Widget coffer = Widgets.getWidget(669);
            if(coffer != null && coffer.isVisible()) {
                WidgetChild button = coffer.getChild(10);
                if(button != null) {
                    if(!button.interact("Take-All")) {
                        Logger.error("failed to take all");
                    }

                    Sleep.sleepUntil(() -> Inventory.size() != currentItems, 5000);

                    if(Inventory.size() == currentItems) {
                        WatScript.getInstance().sendWebhook(AccountManager.getAccountUsername() + " had no items to collect from coffer, intervene manually.", true);
                        ScriptManager.getScriptManager().stop();
                        return;
                    }

                    WidgetChild exitBasic = coffer.getChild(1);
                    if(exitBasic != null && exitBasic.isVisible()) {
                        WidgetChild x = exitBasic.getChild(11);
                        if(x != null && x.isVisible()) {
                            if(!x.interact("Close")) {
                                Logger.error("problem exiting deaths coffer");
                            }

                            GameObject exitPortal = GameObjects.closest("Portal");
                            if(exitPortal != null) {
                                if(!exitPortal.interact("Use")) {
                                    Logger.error("error exiting from deaths domain");
                                }

                                Sleep.sleepUntil(() -> GameObjects.closest("Portal") == null, 5000);
                                TaskManager.getInstance().setCurrentTask(new MainCombatTask());
                            }
                        }
                    }
                }
            }
            else {
                if (Dialogues.inDialogue()) {
                    while (Dialogues.inDialogue()) {
                        DialogueUtils.solve(responses);
                    }
                } else {
                    if(!death.interact("Talk-to")) {
                        Logger.error("failed to talk to death");
                    }
                }
            }
        } else {
            GameObject domain  = GameObjects.closest("Death's Domain");

            if(domain == null && !graveZone.contains(Players.getLocal())) {
                TaskManager.getInstance().setCurrentTask(new TraversalTask(graveZone, this));
                return;
            }

            if(domain != null) {
                if(!domain.interact("Enter")) {
                    if(domain.distance(Players.getLocal()) > 10) {
                        Area a = new Area(3238, 3198, 3244, 3196);
                        while(!a.contains(Players.getLocal())) {
                            Walking.walk(a);
                            Sleep.sleepUntil(Walking::shouldWalk, 5000);
                        }
                    }

                    Logger.error("problem entering deaths domain");
                    return;
                }

                Sleep.sleepUntil(() -> NPCs.closest("Death") != null || Dialogues.inDialogue(), Calculations.random(5000, 10000));
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
}
