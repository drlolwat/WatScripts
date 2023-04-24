package org.lolwat;

import org.dreambot.api.Client;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ExperienceListener;
import org.lolwat.Mouse.BezierMouse;

@ScriptManifest(name = "WatMiner", description = "It is what it is", author = "lolwat", version = 2.0, category = Category.MINING, image = "")
public class WatMiner extends AbstractScript implements ExperienceListener {
    @Override
    public void onStart() {
        Client.getInstance().setMouseMovementAlgorithm(new BezierMouse());
    }

    @Override
    public int onLoop() {
        return 1;
    }
}
