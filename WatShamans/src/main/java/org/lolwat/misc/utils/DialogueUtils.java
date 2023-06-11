package org.lolwat.misc.utils;

import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.dreambot.api.utilities.Sleep.sleepUntil;
import static org.dreambot.api.utilities.Sleep.sleepWhile;

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
}
