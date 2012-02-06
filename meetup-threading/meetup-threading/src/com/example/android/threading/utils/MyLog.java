package com.example.android.threading.utils;

import java.util.Vector;

public class MyLog
{
    /**
     * NOTE: android.util.Log.isLoggable(tag, level) throws IllegalArgumentException if the tag.length() > 23  
     */
    public static String TAG(Object o)
    {
        return TAG((o == null) ? null : o.getClass());
    }

    /**
     * NOTE: android.util.Log.isLoggable(tag, level) throws IllegalArgumentException if the tag.length() > 23  
     */
    public static String TAG(Class c)
    {
        String TAG = MyString.getShortClassName(c);

        final int ANDROID_LOGCAT_TAG_LENGTH_LIMIT = 23;

        if (TAG.length() > ANDROID_LOGCAT_TAG_LENGTH_LIMIT)
        {
            TAG = "..." + TAG.substring(TAG.length() - (ANDROID_LOGCAT_TAG_LENGTH_LIMIT - 3), TAG.length());
        }
        return TAG;
    }

    private static boolean isEnabled = true;

    public static void isEnabled(boolean enable)
    {
        isEnabled = enable;
    }

    public static boolean isEnabled()
    {
        return isEnabled;
    }

    // TODO:(pv) Set logging level?

    private static final Vector logListeners = new Vector();

    public static void addListener(IMyLogListener listener)
    {
        synchronized (logListeners)
        {
            logListeners.addElement(listener);
        }
    }

    public static void removeListener(IMyLogListener listener)
    {
        synchronized (logListeners)
        {
            logListeners.removeElement(listener);
        }
    }

    public static void clearListeners()
    {
        synchronized (logListeners)
        {
            logListeners.removeAllElements();
        }
    }

    protected static void println(String tag, int level, String msg, Throwable e)
    {
        if (isEnabled)// && WtcLogPlatform.isLoggable(tag, level))
        {
            String preformatted = MyLogPlatform.println(tag, level, msg, e);
            if (logListeners.size() > 0)
            {
                synchronized (logListeners)
                {
                    for (int i = 0; i < logListeners.size(); i++)
                    {
                        if (preformatted != null)
                        {
                            ((IMyLogListener) logListeners.elementAt(i)).println(preformatted);
                        }
                        else
                        {
                            ((IMyLogListener) logListeners.elementAt(i)).println(tag, level, msg, e);
                        }
                    }
                }
            }
        }
    }

    public static void v(String tag, String msg)
    {
        v(tag, msg, null);
    }

    public static void verbose(String tag, Throwable e)
    {
        v(tag, "Throwable", e);
    }

    public static void v(String tag, String msg, Throwable e)
    {
        println(tag, MyLogPlatform.VERBOSE, msg, e);
    }

    public static void d(String tag, String msg)
    {
        d(tag, msg, null);
    }

    public static void debug(String tag, Throwable e)
    {
        d(tag, "Throwable", e);
    }

    public static void d(String tag, String msg, Throwable e)
    {
        println(tag, MyLogPlatform.DEBUG, msg, e);
    }

    public static void i(String tag, String msg)
    {
        i(tag, msg, null);
    }

    public static void info(String tag, Throwable e)
    {
        i(tag, "Throwable", e);
    }

    public static void i(String tag, String msg, Throwable e)
    {
        println(tag, MyLogPlatform.INFO, msg, e);
    }

    public static void w(String tag, String msg)
    {
        w(tag, msg, null);
    }

    public static void warn(String tag, Throwable e)
    {
        w(tag, "Throwable", e);
    }

    public static void w(String tag, String msg, Throwable e)
    {
        println(tag, MyLogPlatform.WARN, msg, e);
    }

    public static void e(String tag, String msg)
    {
        e(tag, msg, null);
    }

    public static void error(String tag, Throwable e)
    {
        e(tag, "Throwable", e);
    }

    public static void e(String tag, String msg, Throwable e)
    {
        println(tag, MyLogPlatform.ERROR, msg, e);
    }

    public static void f(String tag, String msg)
    {
        f(tag, msg, null);
    }

    public static void fatal(String tag, Throwable e)
    {
        f(tag, "Throwable", e);
    }

    public static void f(String tag, String msg, Throwable e)
    {
        println(tag, MyLogPlatform.FATAL, msg, e);
    }
}
