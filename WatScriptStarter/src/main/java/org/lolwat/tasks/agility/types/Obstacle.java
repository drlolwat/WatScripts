package org.lolwat.tasks.agility.types;

import org.dreambot.api.methods.map.Area;

public class Obstacle {
    private final String name;
    private final String action;
    private final Area backupArea;
    private final int id;

    public Obstacle(String name, String action) {
        this.name = name;
        this.action = action;
        this.backupArea = null;
        this.id = 0;
    }

    public Obstacle(String name, String action, Area backup) {
        this.name = name;
        this.action = action;
        this.backupArea = backup;
        this.id = 0;
    }

    public Obstacle(int id, String action) {
        this.name = "";
        this.action = action;
        this.backupArea = null;
        this.id = id;
    }

    public Obstacle(int id, String action, Area backup) {
        this.name = "";
        this.action = action;
        this.backupArea = backup;
        this.id = id;
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

    public int getId() {
        return id;
    }
}
