package com.example.ai;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.accessibility.UniversalControlService;
import com.example.utils.CommandOrchestrator;

import java.util.List;

public class ScriptEngine {

    private static final int MAX_ATTEMPTS = 10;

    public static void execute(CommandOrchestrator orchestrator,
            List<String> actions) {
        Log.e("SCRIPT_ENGINE", "Executing actions: " + actions);
        UniversalControlService service = UniversalControlService.getInstance();

        if (service == null)
            return;

        Handler handler = new Handler(Looper.getMainLooper());

        executeStep(orchestrator, service, actions, 0, handler);
    }

    private static void executeStep(CommandOrchestrator orchestrator,
            UniversalControlService service,
            List<String> actions,
            int index,
            Handler handler) {

        if (index >= actions.size())
            return;

        String action = actions.get(index);

        switch (action) {

            case "youtube":

                orchestrator.handleIntent(
                        "open_youtube",
                        "open youtube");

                waitForPackage(service,
                        "com.google.android.youtube",
                        () -> executeStep(orchestrator, service, actions, index + 1, handler),
                        handler,
                        0);

                break;

            case "scroll":

            waitForPackage(service,
                "com.instagram.android",
                () -> {
                    Log.e("SCRIPT_ENGINE", "About to call performAction");
                    service.performAction("scroll down");
                    executeStep(orchestrator, service, actions, index + 1, handler);
                },
                handler,
                0
            );

            break;
            default:
                executeStep(orchestrator, service, actions, index + 1, handler);
        }
    }

    private static void waitForPackage(UniversalControlService service,
            String packageName,
            Runnable nextStep,
            Handler handler,
            int attempt) {

        if (attempt >= MAX_ATTEMPTS) {
            return; // stop safely
        }

        handler.postDelayed(() -> {

            if (packageName.equals(service.getCurrentPackage())) {
                nextStep.run();
            } else {
                waitForPackage(service, packageName, nextStep, handler, attempt + 1);
            }

        }, 400);
    }
}