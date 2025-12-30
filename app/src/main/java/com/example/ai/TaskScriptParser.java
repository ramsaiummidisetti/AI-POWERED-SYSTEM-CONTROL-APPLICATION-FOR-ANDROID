package com.example.ai;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;


public class TaskScriptParser {

    public static List<String> parseActions(String command) {

        command = command.toLowerCase();
        List<String> actions = new ArrayList<>();

        if (command.contains("wifi"))
            actions.add("wifi");
        if (command.contains("bluetooth"))
            actions.add("bluetooth");
        if (command.contains("open youtube"))
            actions.add("youtube");
        if (command.contains("chrome")) actions.add("chrome");
        if (command.contains("open settings"))
            actions.add("settings");
        Log.e("SCRIPT_PARSE", "Parsing command: " + command);

        return actions;
    }
}
