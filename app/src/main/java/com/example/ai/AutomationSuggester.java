package com.example.ai;

import android.content.Context;
import android.widget.Toast;

import com.example.utils.VoiceHelper;

public class AutomationSuggester {

    public static void suggest(Context context, String appPackage) {

        if (appPackage == null)
            return;

        String msg = "You frequently use this app. Do you want to automate actions for it?";
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        VoiceHelper.speak(context, msg);
    }
}
