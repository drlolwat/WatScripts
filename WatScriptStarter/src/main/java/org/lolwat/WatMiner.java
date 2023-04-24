package org.lolwat;

import org.dreambot.api.Client;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ExperienceListener;
import org.dreambot.api.utilities.Logger;
import org.lolwat.Mouse.BezierMouse;
import org.lolwat.Tasks.Mining.VarrockEastIron;
import org.lolwat.Tasks.WatTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ScriptManifest(name = "WatMiner", description = "It is what it is", author = "lolwat", version = 2.0, category = Category.MINING, image = "")
public class WatMiner extends AbstractScript implements ExperienceListener {
    HashMap<Integer, WatTask> tasks;
    WatTask currentTask;
    boolean fatalError = false;

    @Override
    public void onStart() {
        // Enable our custom mouse
        Client.getInstance().setMouseMovementAlgorithm(new BezierMouse());

        Logger.log("WatMiner is starting, creating WatTask instances");

        // Self-explanatory, eventually we'll probably want to dynamically load everything in the Tasks/Building Tasks/Mining etc
        tasks = new HashMap<>();

        // The higher the integer, the more important the task. So if we
        // want to add Blast mining, then it would be 101 so they pick it first.
        // (if they have the available stats/quests, otherwise VarrockEastIron would go first)
        //tasks.Put(101, new BlastMiner());
        tasks.put(100, new VarrockEastIron());

        Logger.log("Added " + tasks.size() + " WatTasks");
        Logger.log("Assessing tasks to see what is available for us to perform");

        // Loop through the tasks based on their Integer priority
        tasks.entrySet().stream()
                .sorted(Map.Entry.<Integer, WatTask>comparingByKey().reversed())
                .forEach(entry -> {
                    if(currentTask == null) {
                        WatTask task = entry.getValue();
                        if (task.canPerformTask()) {
                            currentTask = task; // Begins the task if it can be done. Maybe wait for login to do this logic
                            Logger.log("Picked a task: " + currentTask.getName());
                        } else {
                            // Else we can probably add in logic here to check if we have tasks to meet the requirements
                            // and perform those if possible (though ideally any acc we pass to this script can perform
                            // most if not all of the tasks already via another builder script or something)
                        }
                    }
                });

        if(currentTask == null) {
            Logger.error("No tasks were available to perform");
            fatalError = true;
        }
    }

    @Override
    public int onLoop() {
        if(currentTask == null) {
            // We should send the player back to re-assessment here because we can flag a new task by simply
            // nullifying the old one i guess
            return -1;
        }

        if(fatalError) {
            return -1;
        }

        if(currentTask.requiresLogin() && !Client.isLoggedIn()) {
            Logger.info("Waiting for login...");
            return 1;
        }

        currentTask.execute();
        return currentTask.loopTime();
    }
}
