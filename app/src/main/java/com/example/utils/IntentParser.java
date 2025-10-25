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
        else if (command.contains("alarm")) target = "alarm";
        else if (command.contains("dark mode")) target = "darkmode";
        else if (command.contains("light mode")) target = "lightmode";
        else if (command.contains("enable voice") || command.contains("unmute")) target = "enablevoice";
        else if (command.contains("mute") || command.contains("disable voice")) target = "mutevoice";
        else if (command.contains("exit") || command.contains("close app") || command.contains("quit")) target = "exit";
        else target = "unknown";

        // 🔹 Identify action
        if (command.contains("turn on") || command.contains("enable")) action = "on";
        else if (command.contains("turn off") || command.contains("disable")) action = "off";
        else if (command.contains("set")) action = "set";
        else if (command.contains("status") || command.contains("check")) action = "status";
        else if (command.contains("level")) action = "level";
        else action = "default";

        System.out.println("🎤 Parsed Command → Target: " + target + ", Action: " + action);

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
