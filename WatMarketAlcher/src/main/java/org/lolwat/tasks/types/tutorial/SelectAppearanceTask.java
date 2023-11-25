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
            boolean oddEven = Calculations.random(1, 2) == 1;
            List<WidgetChild> widgets = Widgets.getAll(x -> x.hasAction("Select"));
            Collections.shuffle(widgets);
            for(WidgetChild c : widgets) {
                if (oddEven) {
                    oddEven = false;
                    continue;
                }

                for (int i = 0; i < Calculations.random(2, 12); i++) {
                    if (c != null && c.isVisible() && c.interact()) {
                        Sleep.sleep(200, 400);
                    }
                }

                oddEven = true;
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
}
