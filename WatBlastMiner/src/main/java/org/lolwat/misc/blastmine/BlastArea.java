package org.lolwat.misc.blastmine;

import org.dreambot.api.methods.map.Area;

import java.util.List;

public class BlastArea {
    private final Area startArea;
    private final List<MineCavity> run;
    private final Area hopArea;

    public BlastArea(Area startArea, Area hopArea, List<MineCavity> run) {
        this.startArea = startArea;
        this.run = run;
        this.hopArea = hopArea;
    }

    public Area getStartArea() {
        return startArea;
    }

    public List<MineCavity> getRun() {
        return run;
    }

    public Area getHopArea() {
        return hopArea;
    }
}
