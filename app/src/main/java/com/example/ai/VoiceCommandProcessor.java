package com.example.ai;

public class VoiceCommandProcessor {

    public static class CommandResult {
        public String query;
        public int seconds;
    }

    public static CommandResult process(String input) {

        input = input.toLowerCase();

        CommandResult result = new CommandResult();

        int hours = 0;
        int minutes = 0;

        // 🔹 Extract hours
        if (input.contains("hour")) {
            String[] parts = input.split("hour");
            hours = extractNumber(parts[0]);
        }

        // 🔹 Extract minutes
        if (input.contains("minute")) {
            String[] parts = input.split("minute");
            minutes = extractNumber(parts[0]);
        }

        result.seconds = (hours * 3600) + (minutes * 60);

        // 🔹 Clean query
        result.query = input
                .replace("play", "")
                .replace("on youtube", "")
                .replace("from", "")
                .replaceAll("[0-9]+ hour", "")
                .replaceAll("[0-9]+ minute", "")
                .trim();

        return result;
    }

    private static int extractNumber(String text) {
        String[] words = text.trim().split(" ");
        try {
            return Integer.parseInt(words[words.length - 1]);
        } catch (Exception e) {
            return 0;
        }
    }
}