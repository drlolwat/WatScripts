package org.lolwat.tasks.types.tutorial;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.WatAIO;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.WatTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SelectAppearanceTask implements WatTask {
    private final int APPEAR_PAR = 679;
    private final int ACCEPT = 68;

    @Override
    public String getName() {
        return "Tutorial: Selecting appearance";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute(WatAIO instance) {
        final Widget par = Widgets.getWidget(APPEAR_PAR);
        if (par != null && par.isVisible()) {
            List<WidgetChild> widgets = Widgets.getAll(x -> x.hasAction("Select"));
            List<WidgetChild> rightArrows = new ArrayList<>();
            for (int i = 1; i < widgets.size(); i += 2) {
                rightArrows.add(widgets.get(i));
            }

            Collections.shuffle(rightArrows);
            List<WidgetChild> selectedWidgets = rightArrows.subList(0, Math.min(7, rightArrows.size()));

            for (WidgetChild widget : selectedWidgets) {
                int timesToClick = Calculations.random(1, 5);
                for (int i = 0; i < timesToClick; i++) {
                    if (widget != null && widget.isVisible() && widget.interact()) {
                        Sleep.sleep(200, 400);
                    }
                }
            }

            WidgetChild acc = Widgets.getWidget(APPEAR_PAR).getChild(ACCEPT);
            if(acc != null && acc.isVisible() && acc.hasAction("Confirm")) {
                if(acc.interact()) {
                    Sleep.sleepUntil(() -> !acc.isVisible(), 6750);
                    instance.currentTask = new GuideTask();
                }
            }
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return Calculations.random(350, 450);
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

    }

    @Override
    public Skill trainsSkill() {
        return null;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public Quest completesQuest() {
        return null;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}
