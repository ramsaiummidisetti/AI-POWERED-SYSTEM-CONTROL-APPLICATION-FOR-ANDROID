package com.example.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class UniversalControlService extends AccessibilityService {

    private static UniversalControlService instance;
    private static final String TAG = "ACCESS_TEST";

    // ==============================
    // SERVICE LIFECYCLE
    // ==============================

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();

        instance = this;

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes =
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.notificationTimeout = 100;

        setServiceInfo(info);

        Log.e(TAG, "UniversalControlService Connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e(TAG, "Accessibility Event Received | Type = " + event.getEventType());
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "Accessibility Service Interrupted");
    }

    // ==============================
    // STEP-8 : SEMANTIC UI CONTROL
    // ==============================

    public void performAction(String command) {

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        command = command.toLowerCase();

        if (command.contains("search")) {
            clickSemantic(root, "search");
        }
        else if (command.contains("scroll down")) {
            root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        }
        else if (command.contains("scroll up")) {
            root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
        }
        else if (command.contains("go back")) {
            performGlobalAction(GLOBAL_ACTION_BACK);
        }
        else if (command.contains("notifications")) {
            performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
        }
    }

    // ==============================
    // CORE STEP-8 LOGIC
    // ==============================

    private void clickSemantic(AccessibilityNodeInfo root, String keyword) {

        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        collectNodes(root, nodes);

        for (AccessibilityNodeInfo node : nodes) {

            if (!node.isClickable()) continue;

            CharSequence text = node.getText();
            CharSequence desc = node.getContentDescription();

            if ((text != null && text.toString().toLowerCase().contains(keyword)) ||
                (desc != null && desc.toString().toLowerCase().contains(keyword))) {

                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.e(TAG, "Clicked semantic UI element: " + keyword);
                return;
            }
        }

        Log.e(TAG, "No matching UI element found for: " + keyword);
    }

    private void collectNodes(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> list) {
        if (node == null) return;

        list.add(node);

        for (int i = 0; i < node.getChildCount(); i++) {
            collectNodes(node.getChild(i), list);
        }
    }

    // ==============================
    // INSTANCE ACCESS
    // ==============================

    public static UniversalControlService getInstance() {
        return instance;
    }
}
