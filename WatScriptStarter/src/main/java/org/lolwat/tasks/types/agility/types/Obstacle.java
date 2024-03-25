package org.lolwat.tasks.types.agility.types;

import org.dreambot.api.methods.map.Area;

public class Obstacle {
    private final String name;
    private final String action;
    private final Area backupArea;

    public Obstacle(String name, String action) {
        this.name = name;
        this.action = action;
        this.backupArea = null;
    }

    public Obstacle(String name, String action, Area backup) {
        this.name = name;
        this.action = action;
        this.backupArea = backup;
    }

    public String getName() {
        return name;
    }

    public String getAction() {
        return action;
    }

    public Area getBackupArea() {
        return backupArea;
    }
}
