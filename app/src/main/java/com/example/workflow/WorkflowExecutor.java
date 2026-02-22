package com.example.workflow;

import android.content.Context;
import com.example.utils.CommandOrchestrator;
import com.example.ai.IntentData;

public class WorkflowExecutor {

    private CommandOrchestrator orchestrator;

    public WorkflowExecutor(Context context) {
        orchestrator = new CommandOrchestrator(context, null);
    }

    public void execute(Workflow workflow) {

        for (WorkflowStep step : workflow.getSteps()) {

            orchestrator.handleIntent(
                new IntentData(step.getActionType(), step.getParameters(), 1.0)
            );
        }
    }
}