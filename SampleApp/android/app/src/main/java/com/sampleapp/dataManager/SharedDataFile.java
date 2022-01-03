package com.sampleapp.dataManager;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedDataFile {
    private SharedPreferences mSharedPref;
    private Context mContext;

    public SharedDataFile(Context context) {
        this.mContext = context;
        this.mSharedPref = context.getSharedPreferences(mContext.getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
    }

    public boolean contains(String key) {
        return mSharedPref.contains(key);
    }

    public int get(String key, int defaultValue) {
        String value = getInternal(key);
        if (value.isEmpty()) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    public String get(String key, String defaultValue) {
        String value = getInternal(key);
        if (value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    private String getInternal(String key) {
        String str = mSharedPref.getString(key, "");
        if (str.isEmpty()) {
            return "";
        }
        return str;
    }

    public void put(String key, int value) {
        putInternal(key, Integer.toString(value));
    }

    public void put(String key, String value) {
        putInternal(key, value);
    }

    private void putInternal(String key, String value) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        if (value == null) {
            editor.remove(key);
        } else {
            editor.putString(key, value);
        }
        editor.apply();
    }
}
