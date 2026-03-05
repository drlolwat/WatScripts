package org.lolwat.misc.paint;

import org.lolwat.WatScript;

public class Paint implements PaintInfo {
    @Override
    public String[] getPaintInfo() {
        String elapsedTimeStr = WatScript.getInstance().getElapsedTime();
        long elapsedTimeMillis = parseElapsedTime(elapsedTimeStr);
        double elapsedTimeHours = elapsedTimeMillis / 3600000.0; // Convert milliseconds to hours

        int dynamitePlaced = WatScript.dynamitePlaced;
        int oresPicked = WatScript.oresPicked;

        double dynamitePerHour = dynamitePlaced / elapsedTimeHours;
        double oresPerHour = oresPicked / elapsedTimeHours;

        return new String[] {
                "WatBlaster",
                "Time running: " + WatScript.getInstance().getElapsedTime(),
                "Dynamite placed: " + dynamitePlaced + " (" + String.format("%.2f", dynamitePerHour) + "/h)",
                "Ore picked up: " + oresPicked + " (" + String.format("%.2f", oresPerHour) + "/h)",
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