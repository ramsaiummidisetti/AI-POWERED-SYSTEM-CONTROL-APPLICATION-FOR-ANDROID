package com.example.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.utils.CommandOrchestrator;

import java.util.List;

public class ScriptEngine {

    public static void execute(Context context, List<String> actions) {

        // ✅ FIX: Context-only orchestrator
        CommandOrchestrator orchestrator =
                new CommandOrchestrator(context);

        Handler handler = new Handler(Looper.getMainLooper());
        int delay = 0;

        for (String action : actions) {

            int currentDelay = delay;

            handler.postDelayed(() -> {
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
            }, currentDelay);

            delay += 1500; // delay between actions
        }
    }
}
