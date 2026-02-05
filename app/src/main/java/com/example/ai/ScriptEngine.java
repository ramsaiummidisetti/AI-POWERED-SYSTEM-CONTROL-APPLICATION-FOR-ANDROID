package com.example.ai;

import android.content.Context;

import com.example.utils.CommandOrchestrator;

import java.util.List;

public class ScriptEngine {

    public static void execute(Context context, List<String> actions) {

        // ⭐ Always use Context-safe orchestrator
        CommandOrchestrator orchestrator =
                new CommandOrchestrator(context);

        for (String action : actions) {

            switch (action) {
                case "youtube":
                    orchestrator.handleIntent(0);
                    break;

                case "chrome":
                    orchestrator.handleIntent(3);
                    break;

                case "wifi":
                    orchestrator.handleIntent(1);
                    break;

                case "settings":
                    orchestrator.handleIntent(6);
                    break;
            }

            // ⭐ Android 13+ SAFETY DELAY (VERY IMPORTANT)
            try {
                Thread.sleep(600);
            } catch (InterruptedException ignored) {}
        }
    }
}
