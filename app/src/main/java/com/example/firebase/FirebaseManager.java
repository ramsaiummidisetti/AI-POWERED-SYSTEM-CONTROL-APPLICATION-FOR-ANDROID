package com.example.firebase;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {

    private FirebaseFirestore db;

    public FirebaseManager() {
        db = FirebaseFirestore.getInstance();
    }

    public void sendCommand(String command, String intent, String query, String deviceId, String app,
            String timeOfDay) {

        Map<String, Object> data = new HashMap<>();
        data.put("command", command);
        data.put("intent", intent);
        data.put("query", query);
        data.put("deviceId", deviceId);
        data.put("app", app);
        data.put("timeOfDay", timeOfDay);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("user_commands")
                .add(data)
                .addOnSuccessListener(documentReference -> Log.e("FIREBASE", "Data sent"))
                .addOnFailureListener(e -> Log.e("FIREBASE", "Error", e));
       Log.e("FIREBASE_DEBUG",
        "Sending → Command: " + command +
        " | Intent: " + intent +
        " | Query: " + query +
        " | Time: " + timeOfDay);
    }

    public void storeTransition(String fromApp, String toApp) {

        String docId = fromApp + "_" + toApp;

        DocumentReference ref = db.collection("transitions").document(docId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("from", fromApp);
        updates.put("to", toApp);
        updates.put("lastUsed", System.currentTimeMillis());

        // 🔥 Atomic increment (NO race condition)
        updates.put("count", FieldValue.increment(1));

        ref.set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.e("FIREBASE", "Transition stored: " + fromApp + " → " + toApp))
                .addOnFailureListener(e -> Log.e("FIREBASE", "Error storing transition", e));
    }

    public void storePrediction(String command, String intent, String time) {


        Map<String, Object> data = new HashMap<>();
        data.put("command", command);
        data.put("intent", intent);
        data.put("time", time);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("predictions").add(data)
                .addOnSuccessListener(doc -> Log.e("FIREBASE", "Prediction stored"))
                .addOnFailureListener(e -> Log.e("FIREBASE", "Prediction error", e));
    }

   public void storePredictionApp(String app, String time) {


    String docId = app + "_" + time;   // 🔥 KEY PART

        DocumentReference ref = db.collection("predictions").document(docId);

        Map<String, Object> data = new HashMap<>();
        data.put("app", app);
        data.put("time", time);
        data.put("lastUsed", System.currentTimeMillis());
        data.put("count", FieldValue.increment(1)); // 🔥 INCREMENT

        ref.set(data, SetOptions.merge())
            .addOnSuccessListener(aVoid ->
                Log.e("FIREBASE", "Prediction updated: " + app))
            .addOnFailureListener(e ->
                Log.e("FIREBASE", "Prediction error", e));
    }
}