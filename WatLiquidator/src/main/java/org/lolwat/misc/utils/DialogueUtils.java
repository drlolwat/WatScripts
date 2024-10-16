package org.lolwat.misc.utils;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.dreambot.api.utilities.Sleep.sleepUntil;
import static org.dreambot.api.utilities.Sleep.sleepWhile;

public class DialogueUtils {
    public static void continueWhilePossible() {
        while (Dialogues.canContinue() && Dialogues.inDialogue() && Dialogues.getOptions() == null) {
            Dialogues.continueDialogue();
            sleepWhile(Dialogues::isProcessing, Dialogues::canContinue, Calculations.random(100, 300), 1000);
            Sleep.sleep(500, 700);
        }
    }

    public static void solve(List<String> answers) {
        if (Dialogues.getOptions() != null && !answers.isEmpty()) {
            final List<String> options = Arrays.asList(Dialogues.getOptions());

            for (String answer : answers) {
                if (options.contains(answer)) {
                    Dialogues.chooseOption(answer);
                    sleepUntil(Dialogues::canContinue, 2000);
                    break; // Exit the loop after selecting an option
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
}