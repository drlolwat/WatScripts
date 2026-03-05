package org.lolwat.misc.paint;

import org.dreambot.api.methods.world.Worlds;
import org.lolwat.WatScript;

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
        String elapsedTimeStr = WatScript.getInstance().getElapsedTime();
        long elapsedTimeMillis = parseElapsedTime(elapsedTimeStr);
        double elapsedTimeHours = elapsedTimeMillis / 3600000.0;

        int planksCreated = WatScript.getInstance().getPlanksCreated();;
        double planksPerHour = planksCreated / elapsedTimeHours;

        int sipsTaken = WatScript.getInstance().getSipsTaken();
        double sipsPerHour = sipsTaken / elapsedTimeHours;

        return new String[] {
                "Wat WC Guild",
                "Time running: " + WatScript.getInstance().getElapsedTime(),
                "Planks created: " + planksCreated + " (" + String.format("%.2f", planksPerHour) + "/h)",
                "Sips taken: " + sipsTaken + " (" + String.format("%.2f", sipsPerHour) + "/h)",
                "Ping: " + ping + "ms"
        };
    }

    private long parseElapsedTime(String elapsedTimeStr) {
        String[] parts = elapsedTimeStr.split(" ");
        long hours = Long.parseLong(parts[0].replace("h", ""));
        long minutes = Long.parseLong(parts[1].replace("m", ""));
        long seconds = Long.parseLong(parts[2].replace("s", ""));
        return (hours * 3600 + minutes * 60 + seconds) * 1000;
    }
}