package com.example.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class UniversalControlService extends AccessibilityService {

    private static UniversalControlService instance;
    private static final String TAG = "UNIVERSAL_CTRL";

    private String currentPackage = "";

    public static UniversalControlService getInstance() {
        return instance;
    }

    public String getCurrentPackage() {
        return currentPackage;
    }

    // ==========================================================
    // SERVICE CONNECT
    // ==========================================================

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                          AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        info.flags =
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        info.notificationTimeout = 50;

        setServiceInfo(info);

        Log.e(TAG, "SERVICE CONNECTED");
    }

    // ==========================================================
    // PACKAGE TRACKING
    // ==========================================================

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event == null) return;

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            CharSequence pkg = event.getPackageName();

            if (pkg != null) {
                currentPackage = pkg.toString();
                Log.e(TAG, "ACTIVE PACKAGE: " + currentPackage);
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "Service interrupted");
    }

    // ==========================================================
    // MAIN EXECUTION ENTRY
    // ==========================================================

    public void performAction(String command) {

        if (currentPackage == null) {
            Log.e(TAG, "No active package");
            return;
        }

        // 🚫 Ignore own app
        if (currentPackage.equals(getPackageName())) {
            Log.e(TAG, "Ignoring own app");
            return;
        }

        // 🚫 Ignore system UI
        if (currentPackage.contains("systemui")) {
            Log.e(TAG, "Ignoring system UI");
            return;
        }

        // 🚫 Ignore launcher
        if (currentPackage.contains("launcher")) {
            Log.e(TAG, "Ignoring launcher");
            return;
        }

        AccessibilityNodeInfo root = getSafeRoot();

        if (root == null) {
            Log.e(TAG, "Root not ready");
            return;
        }

        command = command.toLowerCase().trim();

        Log.e(TAG, "Trying action in package: " + currentPackage);

        if (command.contains("scroll down")) {
            scroll(root, true);
        }
        else if (command.contains("scroll up")) {
            scroll(root, false);
        }
        else if (command.startsWith("click ") || command.startsWith("tap ")) {

            String keyword = command
                    .replaceFirst("click ", "")
                    .replaceFirst("tap ", "");

            clickSemantic(root, keyword);
        }
        else if (command.startsWith("type ")) {

            String text = command.replaceFirst("type ", "");

            typeText(root, text);
        }

        root.recycle();
    }// ==========================================================
    // SAFE ROOT (NO BLOCKING)
    // ==========================================================

    private AccessibilityNodeInfo getSafeRoot() {

        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root == null) {
            Log.e(TAG, "Root is null");
            return null;
        }

        return root;
    }

    // ==========================================================
    // SCROLL
    // ==========================================================

    private void scroll(AccessibilityNodeInfo root, boolean forward) {

        Log.e(TAG, "Trying scroll in package: " + currentPackage);

        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        collectNodes(root, nodes);

        for (AccessibilityNodeInfo node : nodes) {

            if (node == null) continue;

            if (node.isScrollable()) {

                boolean result = node.performAction(
                        forward ? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                                : AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                );

                Log.e(TAG, "Scroll result: " + result);

                recycleAll(nodes);
                return;
            }
        }

        recycleAll(nodes);
    }
        

    // ==========================================================
    // CLICK
    // ==========================================================

    private void clickSemantic(AccessibilityNodeInfo root, String keyword) {

        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        collectNodes(root, nodes);

        keyword = keyword.toLowerCase().trim();

        for (AccessibilityNodeInfo node : nodes) {

            if (node == null) continue;
            if (!node.isVisibleToUser()) continue;

            CharSequence text = node.getText();
            CharSequence desc = node.getContentDescription();

            boolean match = false;

            if (text != null &&
                    text.toString().toLowerCase().contains(keyword)) {
                match = true;
            }

            if (!match && desc != null &&
                    desc.toString().toLowerCase().contains(keyword)) {
                match = true;
            }

            if (match) {

                AccessibilityNodeInfo parent = node;

                while (parent != null) {

                    if (parent.isClickable()) {

                        parent.performAction(
                                AccessibilityNodeInfo.ACTION_CLICK
                        );

                        Log.e(TAG, "Clicked: " + keyword);

                        recycleAll(nodes);
                        return;
                    }

                    parent = parent.getParent();
                }
            }
        }

        recycleAll(nodes);
    }

    // ==========================================================
    // TYPE
    // ==========================================================

    private void typeText(AccessibilityNodeInfo root, String text) {

        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        collectNodes(root, nodes);

        for (AccessibilityNodeInfo node : nodes) {

            if (node == null) continue;

            if (node.isEditable()) {

                Bundle args = new Bundle();
                args.putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        text
                );node.performAction(
                        AccessibilityNodeInfo.ACTION_SET_TEXT,
                        args
                );

                Log.e(TAG, "Typed: " + text);

                recycleAll(nodes);
                return;
            }
        }

        recycleAll(nodes);
    }

    // ==========================================================
    // NODE COLLECTION
    // ==========================================================

    private void collectNodes(AccessibilityNodeInfo node,
                              List<AccessibilityNodeInfo> list) {

        if (node == null) return;

        list.add(node);

        for (int i = 0; i < node.getChildCount(); i++) {
            collectNodes(node.getChild(i), list);
        }
    }

    private void recycleAll(List<AccessibilityNodeInfo> nodes) {

        for (AccessibilityNodeInfo n : nodes) {
            if (n != null) n.recycle();
        }
    }
}