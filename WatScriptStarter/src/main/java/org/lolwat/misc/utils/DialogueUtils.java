package org.lolwat.misc.utils;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.dreambot.api.utilities.Sleep.*;

public class DialogueUtils {
    public static void continueWhilePossible() {
        while (Dialogues.canContinue() && Dialogues.inDialogue()) {
            Dialogues.continueDialogue();
            sleepWhile(Dialogues::isProcessing, Dialogues::canContinue, 300, 1000);
            Sleep.sleep(500, 700);
        }
    }

    // Credit to the DreamBot forums for this solution
    public static void solve(List<String> answers) {
        if(Dialogues.getOptions() != null) {
            final List<String> options = Arrays.asList(Dialogues.getOptions());
            final Optional<String> optionalOption = answers.stream().filter(ans -> options.stream().anyMatch(option -> option.equals(ans))).findFirst();
            if (optionalOption.isPresent()) {
                optionalOption.ifPresent((ans) -> {
                    Dialogues.chooseOption(ans);
                    sleepUntil(Dialogues::canContinue, 5000);
                    continueWhilePossible();
                });
            } else {
                Logger.log(Arrays.toString(Dialogues.getOptions()));
            }
        }
    }

    public static void talkTo(String npc) {
        if (!Dialogues.canContinue()) {
            final NPC guide = NPCs.closest(npc);
            if (guide != null) {
                if (guide.isOnScreen()) {
                    if (guide.interact("Talk-to")) {
                        dialogueSleep();
                        Sleep.sleepUntil(Dialogues::canContinue, Calculations.random(1200, 1600));
                    }
                } else {
                    Walking.walk(guide);
                    dialogueSleep();
                }
            }
        } else {
            Dialogues.continueDialogue();
            sleep(200, 500);
        }
    }

    private static void dialogueSleep() {
        Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(300, 1200));
        Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(300, 1200));
    }
}
