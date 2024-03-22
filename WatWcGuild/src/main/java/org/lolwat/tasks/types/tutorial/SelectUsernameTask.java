package org.lolwat.tasks.types.tutorial;

import org.dreambot.api.input.Keyboard;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.WatTask;

import java.awt.event.KeyEvent;
import java.util.HashMap;

public class SelectUsernameTask implements WatTask {

    private final int NAME_WIDGET = 558; // ??
    private final int NAME_TEXT_CHILDID = 12;
    private final int NAME_LOOKUP_CHILDID = 18; // Actions: "Look up name". If no actions, name is not available
    private final int NAME_SETNAME_CHILDID = 19; // Actions: "Set name"

    @Override
    public String getName() {
        return "Tutorial: Selecting username";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute(WatAIO instance) {
        if(Widgets.getWidget(NAME_WIDGET) != null && Widgets.getWidget(NAME_WIDGET).isVisible()) {
            WidgetChild nameText = Widgets.getWidget(NAME_WIDGET).getChild(NAME_TEXT_CHILDID);
            WidgetChild lookupButton = Widgets.getWidget(NAME_WIDGET).getChild(NAME_LOOKUP_CHILDID);

            if(nameText != null && nameText.isVisible()) {
                if(nameText.getText().equals("*")) {
                    nameText.interact();
                    Sleep.sleep(100, 200);
                    String name = GenericUtils.generateUsername();
                    Logger.log("Selected name:" + name);
                    Keyboard.type(name, false);
                    Sleep.sleep(100, 200);
                    if (lookupButton != null && lookupButton.isVisible() && lookupButton.hasAction("Look up name")) {
                        lookupButton.interact("Look up name");
                        Sleep.sleep(3000, 4000);
                    }
                }
                else {
                    if(nameText.interact()) {
                        while(!nameText.getText().equals("*")) {
                            Keyboard.typeKey(KeyEvent.VK_BACK_SPACE);
                        }
                    }
                }
            }

            WidgetChild confirmName = Widgets.getWidget(NAME_WIDGET).getChild(NAME_SETNAME_CHILDID);
            if(confirmName != null && confirmName.isVisible() && confirmName.hasAction("Set name")) {
                if(confirmName.interact("Set name")) {
                    Sleep.sleep(500, 1200);
                    TaskManager.getInstance().setCurrentTask(new SelectAppearanceTask());
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
