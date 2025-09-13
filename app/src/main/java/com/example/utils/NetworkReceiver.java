package com.example.utils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.utils.LogEvent;
import com.example.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo active = cm != null ? cm.getActiveNetworkInfo() : null;
        boolean connected = active != null && active.isConnected();

        // ✅ Wrap into JSONObject instead of plain String
        JSONObject meta = new JSONObject();
        try {
            meta.put("status", "Connected: " + connected);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LogUtils.writeLogAsync(context,
                new LogEvent("network_update", "info", "system", meta));
    }
}
