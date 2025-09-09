package com.example.utils;

import org.json.JSONObject;

public class LogEvent {
    public String event;
    public long timestamp;
    public String severity;
    public String source;
    public JSONObject meta;

    public LogEvent(String event, String severity, String source, JSONObject meta) {
        this.event = event;
        this.timestamp = System.currentTimeMillis();
        this.severity = severity;
        this.source = source;
        this.meta = meta;
    }

    @Override
    public String toString() {
        return "Event=" + event +
               " | Time=" + timestamp +
               " | Severity=" + severity +
               " | Source=" + source +
               " | Meta=" + (meta != null ? meta.toString() : "{}");
    }
}
