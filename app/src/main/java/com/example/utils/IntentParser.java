package com.example.utils;

import java.util.Locale;

public class IntentParser {

    public static ParsedIntent parse(String command) {
        command = command.toLowerCase(Locale.ROOT);
        String target = null;
        String action = null;

        // 🔹 Identify target
        if (command.contains("bluetooth")) target = "bluetooth";
        else if (command.contains("battery")) target = "battery";
        else if (command.contains("network")) target = "network";
        else if (command.contains("nfc")) target = "nfc";

        // 🔹 Identify action
        if (command.contains("turn on") || command.contains("enable")) action = "on";
        else if (command.contains("turn off") || command.contains("disable")) action = "off";
        else if (command.contains("status") || command.contains("check")) action = "status";
        else if (command.contains("level")) action = "level";

        if (target == null)
            return new ParsedIntent("unknown", "unknown");
        if (action == null)
            action = "status";

        return new ParsedIntent(target, action);
    }

    // Inner data class
    public static class ParsedIntent {
        public String target;
        public String action;
        public ParsedIntent(String target, String action) {
            this.target = target;
            this.action = action;
        }
    }
}
