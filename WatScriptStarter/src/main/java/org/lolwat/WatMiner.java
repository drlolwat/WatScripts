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
    public HashMap<Integer, WatTask> tasks;
    public WatTask currentTask;
    public boolean fatalError = false;

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
    }

    private void evaluate() {
        Logger.log("Assessing tasks to see what is available for us to perform");
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
            Logger.error("Unable to pick a task: does not meet any requirements");
            fatalError = true;
        }
    }

    @Override
    public int onLoop() {
        if(fatalError) {
            return 1;
        }

        if(!Client.isLoggedIn()) {
            return 1;
        }

        if(currentTask == null) {
            evaluate();
            return 1;
        }

        currentTask.execute(this);
        return currentTask.loopTime() > 0 ? currentTask.loopTime() : 5;
    }
}
