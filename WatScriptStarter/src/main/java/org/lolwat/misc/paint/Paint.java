package org.lolwat.misc.paint;

import org.dreambot.api.Client;
import org.dreambot.api.methods.world.Worlds;
import org.lolwat.WatScript;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.NumUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Paint implements PaintInfo {
    private int ping;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public Paint() {
        ping = Worlds.getCurrent().getPing();
        scheduler.scheduleAtFixedRate(this::updatePing, 0, 30, TimeUnit.SECONDS);
    }

    private void updatePing() {
        Worlds.updatePing(Worlds.getCurrent());
        ping = Worlds.getCurrent().getPing();
    }

    @Override
    public String[] getPaintInfo() {
        if(WatScript.getInstance().getElapsedTime() != null && Client.isLoggedIn()) {
            String elapsedTimeStr = WatScript.getInstance().getElapsedTime();
            long elapsedTimeMillis = parseElapsedTime(elapsedTimeStr);
            double elapsedTimeHours = elapsedTimeMillis / 3600000.0; // Convert milliseconds to hours

            double alchsPerHour = ConfigManager.getInstance().getTotalAlchs() / elapsedTimeHours;
            double profitPerHour = ConfigManager.getInstance().getTotalProfit() / elapsedTimeHours;

            return new String[]{
                    "WatMarketAlcher",
                    TaskManager.getInstance().getCurrentTask() != null ? "Task: " + TaskManager.getInstance().getCurrentTask().getName() : "",
                    "Time running: " + WatScript.getInstance().getElapsedTime(),
                    "Alchs: " + ConfigManager.getInstance().getTotalAlchs() + " (" + NumUtils.simplifyNumber(alchsPerHour) + "/h)",
                    "Profit: " + NumUtils.simplifyNumber(ConfigManager.getInstance().getTotalProfit()) + " (" + NumUtils.simplifyNumber(profitPerHour) + "/h)",
            };
        }

        return new String[]{};
    }

    private long parseElapsedTime(String elapsedTimeStr) {
        String[] parts = elapsedTimeStr.split(" ");
        long hours = Long.parseLong(parts[0].replace("h", ""));
        long minutes = Long.parseLong(parts[1].replace("m", ""));
        long seconds = Long.parseLong(parts[2].replace("s", ""));
        return (hours * 3600 + minutes * 60 + seconds) * 1000;
    }
}