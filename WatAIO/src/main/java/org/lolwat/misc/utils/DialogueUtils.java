package org.lolwat.misc.utils;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.util.*;

import static org.dreambot.api.utilities.Sleep.sleepUntil;
import static org.dreambot.api.utilities.Sleep.sleepWhile;

public class DialogueUtils {
    private static final Set<String> usedOptions = new HashSet<>();

    public static void continueWhilePossible() {
        while (Dialogues.canContinue() && Dialogues.inDialogue() && Dialogues.getOptions() == null) {
            Dialogues.continueDialogue();
            sleepWhile(Dialogues::isProcessing, Dialogues::canContinue, Calculations.random(100, 300), 1000);
            Sleep.sleep(500, 700);
        }
    }

    public static void solve(List<String> answers) {
        // look for 219 1
        if (Dialogues.getOptions() != null && !answers.isEmpty()) {
            final List<String> options = Arrays.asList(Dialogues.getOptions());

            for (String answer : answers) {
                if (options.contains(answer) && !usedOptions.contains(answer)) {
                    usedOptions.add(answer);
                    Dialogues.chooseOption(answer);
                    sleepUntil(Dialogues::canContinue, 2000);
                    break; // Exit the loop after selecting an option
                }
            }

            if (usedOptions.isEmpty()) {
                Logger.log("No unused options found: " + Arrays.toString(Dialogues.getOptions()));
            }
        }

        Widget dialogue = Widgets.getWidget(219);
        if(dialogue != null && dialogue.isVisible()) {
            WidgetChild dialogueOptions = dialogue.getChild(1);
            if(dialogueOptions != null && dialogueOptions.isVisible()) {
                for(WidgetChild option : dialogueOptions.getChildren()) {
                    if(option == null) {
                        continue;
                    }

                    if(answers.contains(option.getText())) {
                        Logger.log("Selecting dialogue option: " + option.getText());
                        if(!option.interact()) {
                            Logger.error("Failed to interact with dialogue option: " + option.getText());
                            return;
                        }

                        Sleep.sleepUntil(() -> (Dialogues.canContinue() || !option.isVisible()), Calculations.random(500, 1500));
                        break;
                    }
                }
            }
        }

        DialogueUtils.continueWhilePossible();
    }

    public static void talkTo(String npc) {
        talkTo(npc, null);
    }

    public static void talkTo(String npc, List<String> answers) {
        if (answers == null) answers = new ArrayList<>();

        if (TutorialUtils.needsOpenTab()) {
            TutorialUtils.handleTab();
            return;
        }

        if (Dialogues.isProcessing()) {
            return;
        }

        Sleep.sleepUntil(() -> (Dialogues.canContinue() || Dialogues.getOptions() != null), Calculations.random(500, 1500));

        if (!answers.isEmpty() && Dialogues.getOptions() != null) {
            solve(answers);
            return;
        }

        if (Dialogues.canContinue()) {
            Dialogues.continueDialogue();
        } else {
            NPC n = NPCs.closest(npc);
            if (n != null) {
                if (n.isOnScreen()) {
                    if (n.interact("Talk-to")) {
                        Sleep.sleepUntil(() -> (Dialogues.canContinue() || Dialogues.getOptions() != null), Calculations.random(500, 1500));
                    }
                } else {
                    Walking.walk(n);
                }
            }
        }

        Sleep.sleepUntil(() -> (Dialogues.canContinue() || Dialogues.getOptions() != null), Calculations.random(500, 1500));
    }

    public static void wipeOptions() {
        usedOptions.clear();
    }
}