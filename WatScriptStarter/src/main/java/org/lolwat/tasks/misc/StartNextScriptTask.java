package org.lolwat.tasks.misc;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.ScriptManager;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.types.WatTask;

public class StartNextScriptTask implements WatTask {
    @Override
    public String getName() {
        return "Starting next script";
    }

    @Override
    public void execute() {
        String scriptName = ConfigManager.getInstance().getConfigString("script_to_launch");
        String scriptParams = ConfigManager.getInstance().getConfigString("script_params_to_launch");

        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}
            ScriptManager.getScriptManager().start(scriptName, scriptParams);
        }).start();

        ScriptManager.getScriptManager().stop();
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public Skill trainsSkill() {
        return null;
    }
}
