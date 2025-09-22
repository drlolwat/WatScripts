package org.lolwat.types.gear;

import lombok.Getter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;

public class WatZone {
    @Getter
    private final String name;
    @Getter
    private final Skill skill;
    @Getter
    private final int minLevel;
    @Getter
    private final Area searchArea;
    @Getter
    private final String objectName;
    @Getter
    private final boolean isNpc;
    @Getter
    private final String contextSearch;

    public WatZone(String name, Skill skill, int minLevel, Area searchArea, String objectName, boolean isNpc, String contextSearch) {
        this.name = name;
        this.skill = skill;
        this.minLevel = minLevel;
        this.searchArea = searchArea;
        this.objectName = objectName;
        this.isNpc = isNpc;
        this.contextSearch = contextSearch;
    }
}
