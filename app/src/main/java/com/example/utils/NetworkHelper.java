package com.example.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.List;

public class NetworkHelper {

    public static String getNetworkStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo active = cm.getActiveNetworkInfo();
            if (active != null && active.isConnected()) {
                return "Connected to " + active.getTypeName();
            }
        }
        return "No active network";
    }
}
