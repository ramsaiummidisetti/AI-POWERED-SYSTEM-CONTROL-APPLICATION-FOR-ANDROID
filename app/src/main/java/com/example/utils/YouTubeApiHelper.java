package com.example.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class YouTubeApiHelper {

    private static final String TAG = "YOUTUBE_API";

    private static final String API_KEY = "AIzaSyAvp2SXl0m1FhPBUI4gi4lEurY2qbeBJAA";

    public static String searchVideoId(String query) {

        OkHttpClient client = new OkHttpClient();

        String url = "https://www.googleapis.com/youtube/v3/search"
                + "?part=snippet"
                + "&q=" + query.replace(" ", "%20")
                + "&type=video"
                + "&maxResults=1"
                + "&key=" + API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                Log.e(TAG, "API request failed");
                return null;
            }

            String body = response.body().string();

            JSONObject json = new JSONObject(body);
            JSONArray items = json.getJSONArray("items");

            if (items.length() > 0) {
                JSONObject video = items.getJSONObject(0);
                JSONObject id = video.getJSONObject("id");

                return id.getString("videoId");
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception: ", e);
        }

        return null;
    }
}