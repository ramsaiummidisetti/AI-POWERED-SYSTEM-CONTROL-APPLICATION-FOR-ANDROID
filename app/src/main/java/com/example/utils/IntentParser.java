package com.example.utils;

import java.util.Locale;

public class IntentParser {

    public static ParsedIntent parse(String command) {
        command = command.toLowerCase(Locale.ROOT).trim();
        String target = "unknown";
        String action = "default";

        // 🔹 Core targets
        if (command.contains("bluetooth")) target = "bluetooth";
        else if (command.contains("battery")) target = "battery";
        else if (command.contains("network")) target = "network";
        else if (command.contains("nfc")) target = "nfc";
        else if (command.contains("alarm")) target = "alarm";
        else if (command.contains("dark mode") || command.contains("dark")) target = "darkmode";
        else if (command.contains("light mode") || command.contains("light")) target = "lightmode";
        else if (command.contains("usage") || command.contains("app usage")) target = "usage";
        else if (command.contains("time") || command.contains("clock")) target = "time";
        else if (command.contains("exit") || command.contains("quit") || command.contains("close app")) target = "exit";
        else if (command.contains("voice") || command.contains("mute")) target = "voice";

        // 🔹 Actions
        if (command.contains("turn on") || command.contains("enable") || command.contains("open"))
            action = "on";
        else if (command.contains("turn off") || command.contains("disable") || command.contains("close"))
            action = "off";
        else if (command.contains("set") || command.contains("schedule"))
            action = "set";
        else if (command.contains("status") || command.contains("check"))
            action = "status";
        else if (command.contains("level"))
            action = "level";
        else if (command.contains("usage"))
            action = "usage";
        else if (command.contains("time"))
            action = "time";
        else if (command.contains("alarm")) target = "alarm";

        else
            action = "default";

        System.out.println("🎤 Parsed Command → Target: " + target + ", Action: " + action);
        return new ParsedIntent(target, action);
    }

    // Inner Data Class
    public static class ParsedIntent {
        public String target;
        public String action;
        public ParsedIntent(String target, String action) {
            this.target = target;
            this.action = action;
        }
    }
}
