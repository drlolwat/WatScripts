package org.lolwat.tasks.shamans;

import org.dreambot.api.methods.skills.Skill;
import org.lolwat.managers.types.WatTask;

public class ShamanLobbyTask implements WatTask {
    /*
        private final Area lobbyArea = new Area(1299, 10097, 1306, 10096);

    private final Area selectedArea;// = new Area(1289, 10100, 1296, 10093);

    // data
    private final Area topLeft;// = new Area(1292, 10100, 1289, 10097);
    private final Area topRight;// = new Area(1296, 10100, 1293, 10097);
    private final Area bottomLeft;// = new Area(1289, 10096, 1292, 10093);
    private final Area bottomRight;// = new Area(1296, 10093, 1293, 10096);

    // 2nd area data
    private final Area eastArea = new Area(1309, 10100, 1316, 10092); // would be set as selected area
    private final Area eastTopLeft = new Area(1309, 10100, 1312, 10096);
    private final Area eastTopRight = new Area(1313, 10100, 1316, 10096);
    private final Area eastBottomLeft = new Area(1309, 10095, 1312, 10091);
    private final Area eastBottomRight = new Area(1313, 10095, 1316, 10091);

     */
    @Override
    public String getName() {
        return "Going to Shaman lobby";
    }

    @Override
    public void execute() {

    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.RANGED;
    }
}
