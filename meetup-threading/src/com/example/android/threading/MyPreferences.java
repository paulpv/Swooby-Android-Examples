package com.example.android.threading;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.android.threading.utils.MyString;

public class MyPreferences
{
    private static final String PREFS_KEY         = MyString.getClassName(MyApplication.class);

    private static final String KEY_DEBUG_ENABLED = "prefDebugEnabled";

    /**
     * Do not use this method; use WaveApplication.isDebugEnabled() instead.
     * Only WaveApplication.isDebugEnabled() should call this.
     * @param context
     * @return
     */
    public static boolean getDebugEnable(Context context)
    {
        return getBoolean(context, KEY_DEBUG_ENABLED, false);
    }

    /**
     * Only WaveApplication.setDebugEnabled(...) should call this.
     * @param context
     * @param value
     */
    public static void saveDebugEnable(Context context, boolean value)
    {
        putBoolean(context, KEY_DEBUG_ENABLED, value);
    }

    private static SharedPreferences getPrivatePreferences(Context context)
    {
        return context.getSharedPreferences(PREFS_KEY, Activity.MODE_PRIVATE);
    }

    private static boolean getBoolean(Context context, String key, boolean defValue)
    {
        return getPrivatePreferences(context).getBoolean(key, defValue);
    }

    private static void putBoolean(Context context, String key, boolean value)
    {
        getPrivatePreferences(context).edit().putBoolean(key, value).commit();
    }

}
