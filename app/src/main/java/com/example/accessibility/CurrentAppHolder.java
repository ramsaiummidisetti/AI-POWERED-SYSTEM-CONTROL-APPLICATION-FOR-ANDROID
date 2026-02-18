package com.example.accessibility;

public class CurrentAppHolder {

    private static String currentApp = "";

    public static void setCurrentApp(String pkg) {
        currentApp = pkg;
    }

    public static String getCurrentApp() {
        return currentApp;
    }
}
