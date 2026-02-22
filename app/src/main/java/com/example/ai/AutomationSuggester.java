package com.example.ai;

import android.content.Context;
import android.content.pm.PackageManager;

public class AutomationSuggester {

    public static String generateSuggestion(Context context, String packageName) {

        if (packageName == null)
            return null;

        PackageManager pm = context.getPackageManager();

        try {
            String appName =
                    pm.getApplicationLabel(
                            pm.getApplicationInfo(packageName, 0)
                    ).toString();

            return "You frequently use " + appName +
                   ". Would you like to create a quick shortcut?";

        } catch (Exception e) {
            return null;
        }
    }
}