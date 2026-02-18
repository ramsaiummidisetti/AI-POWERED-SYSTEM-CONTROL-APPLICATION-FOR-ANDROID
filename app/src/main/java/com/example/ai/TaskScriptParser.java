package com.example.ai;

import java.util.ArrayList;
import java.util.List;

public class TaskScriptParser {

    public static List<String> parseActions(String command) {

        command = command.toLowerCase();

        List<String> actions =
                new ArrayList<>();

        if (command.contains("youtube"))
            actions.add("youtube");

        if (command.contains("scroll"))
            actions.add("scroll");

        return actions;
    }
}