package com.example.android.threading;

import android.app.Application;

import com.example.android.threading.utils.MyLog;

public class MyApplication extends Application
{
    private static final String TAG            = MyLog.TAG(MyApplication.class);

    private Boolean             isDebugEnabled = null;

    public boolean isDebugEnabled()
    {
        if (isDebugEnabled == null)
        {
            setDebugEnabled(MyPreferences.getDebugEnable(this));
        }
        return isDebugEnabled.booleanValue();
    }

    public void setDebugEnabled(boolean enabled)
    {
        isDebugEnabled = (enabled) ? Boolean.TRUE : Boolean.FALSE;
        MyPreferences.saveDebugEnable(this, enabled);
        MyLog.isEnabled(enabled);
    }
}
