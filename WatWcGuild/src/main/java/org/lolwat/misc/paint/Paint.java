package org.lolwat.misc.paint;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.world.Worlds;
import org.lolwat.WatScript;
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
        String elapsedTimeStr = WatScript.getInstance().getElapsedTime();
        long elapsedTimeMillis = parseElapsedTime(elapsedTimeStr);
        double elapsedTimeHours = elapsedTimeMillis / 3600000.0; // Convert milliseconds to hours

        int shamansKilled = WatScript.getInstance().getShamansKilled();;
        int dwhCollected = WatScript.getInstance().getDwhCollected();
        int totalProfit = WatScript.getInstance().getItemWorthPicked() + WatScript.getInstance().getGoldAlched();

        double shamanPerHour = shamansKilled / elapsedTimeHours;
        double dwhPerHour = dwhCollected / elapsedTimeHours;
        double goldAlched = WatScript.getInstance().getGoldAlched() / elapsedTimeHours;
        double itemsWorthPerHour = WatScript.getInstance().getItemWorthPicked() / elapsedTimeHours;
        double xpPerHour = (Skills.getExperience(Skill.RANGED) - WatScript.getInstance().getStartRangedXp()) / elapsedTimeHours;
        double deathPerHour = WatScript.getInstance().getDeaths() / elapsedTimeHours;
        double totalProfitPerHour = totalProfit / elapsedTimeHours;

        return new String[] {
                "WatShamans",
                "Time running: " + WatScript.getInstance().getElapsedTime(),
                "Shamans killed: " + shamansKilled + " (" + String.format("%.2f", shamanPerHour) + "/h)",
                "Warhammers collected: " + dwhCollected + " (" + String.format("%.2f", dwhPerHour) + "/h)",
                "Gold alched: " + NumUtils.simplifyNumber(WatScript.getInstance().getGoldAlched()) + " (" + NumUtils.simplifyNumber(goldAlched) + "/h)",
                "Item worth picked up: " + NumUtils.simplifyNumber(WatScript.getInstance().getItemWorthPicked()) + " (" + NumUtils.simplifyNumber(itemsWorthPerHour) + "/h)",
                "Total profit: " + NumUtils.simplifyNumber(totalProfit) + " (" + NumUtils.simplifyNumber(totalProfitPerHour) + "/h)",
                "Ranged level: " + Skills.getRealLevel(Skill.RANGED) + (WatScript.getInstance().getRangedLevelsGained() > 0 ? " +" + WatScript.getInstance().getRangedLevelsGained() : "") + " (" + String.format("%.2f", xpPerHour) + " xp/h)",
                "Deaths: " + WatScript.getInstance().getDeaths() + " (" + String.format("%.2f", deathPerHour) + "/h)",
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