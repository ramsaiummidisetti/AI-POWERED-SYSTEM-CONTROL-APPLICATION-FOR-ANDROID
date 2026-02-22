package com.example.workflow;

import java.util.Map;

public class WorkflowStep {

    private String actionType;
    private Map<String, String> parameters;

    public WorkflowStep(String actionType, Map<String, String> parameters) {
        this.actionType = actionType;
        this.parameters = parameters;
    }

    public String getActionType() {
        return actionType;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}