package org.lolwat.managers.types;

public class WatConfig {
    private static int toolFailures = 0;

    public static int getToolFailures() {
        return toolFailures;
    }

    public static void incrementToolFailures() {
        toolFailures++;
    }

    public static void resetToolFailures() {
        toolFailures = 0;
    }
}
