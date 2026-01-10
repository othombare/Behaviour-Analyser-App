package com.example.behave;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private static final String PREF_NAME = "behave_prefs";
    private static final String KEY_DETECTION_ACTIVE = "detection_active";

    private SharedPreferences preferences;

    public PrefManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setDetectionActive(boolean active) {
        preferences.edit().putBoolean(KEY_DETECTION_ACTIVE, active).apply();
    }

    public boolean isDetectionActive() {
        return preferences.getBoolean(KEY_DETECTION_ACTIVE, false); // default = false
    }
}
