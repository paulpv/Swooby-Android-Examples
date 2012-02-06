package com.example.android.threading.utils;

import android.os.Process;
import android.util.Log;

public class MyLogPlatform
{
    public static final int       VERBOSE     = Log.VERBOSE;
    public static final int       FATAL       = 0;          // Log.ASSERT;
    public static final int       ERROR       = Log.ERROR;
    public static final int       WARN        = Log.WARN;
    public static final int       INFO        = Log.INFO;
    public static final int       DEBUG       = Log.DEBUG;

    private static final String[] LEVEL_NAMES = new String[]
                                              {
        "F", // 0
        "?", // 1
        "T", // 2
        "D", // 3
        "I", // 4
        "W", // 5
        "E", // 6
                                              };

    public static boolean isLoggable(String tag, int level)
    {
        return Log.isLoggable(tag, level);
    }

    /**
     * You can change the default level by setting a system property: 'setprop log.tag.&lt;YOUR_LOG_TAG&gt; &lt;LEVEL&gt;'
     * Where level is either VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT, or SUPPRESS.
     * SUPPRESS will turn off all logging for your tag.
     * You can also create a local.prop file that with the following in it:
     * 'log.tag.&lt;YOUR_LOG_TAG&gt;=&lt;LEVEL&gt;' and place that in /data/local.prop.
     * 
     * @param tag
     * @param level
     */
    public static void setTagLevel(String tag, int level)
    {
        // ignore
    }

    private static final int ONESECOND = 1000;
    private static final int ONEMINUTE = 60 * ONESECOND;

    /**
     * Time, Level, PID, Tag, TID, Message, Throwable
     * 
     * @param tag
     * @param level
     * @param msg
     * @param e
     * @return
     */
    public static String format(String tag, int level, String msg, Throwable e)
    {
        StringBuffer sb = new StringBuffer();

        // We don't use net.rim.device.api.util.DateTimeUtilities.formatElapsedTime(...) because we want to output milliseconds.
        // For brevity reasons, we only need to output seconds and milliseconds
        long milliseconds = System.currentTimeMillis() % ONEMINUTE;
        sb.append(MyString.formatNumber(ONESECOND, 2));
        sb.append('.');
        sb.append(MyString.formatNumber(milliseconds % ONESECOND, 3));

        sb.append(' ').append(LEVEL_NAMES[level]);

        sb.append(" P").append(Process.myPid());

        sb.append(" T").append(Process.myTid());

        sb.append(' ').append(tag);

        sb.append(' ').append(msg);

        if (e != null)
        {
            sb.append(": throwable=").append(Log.getStackTraceString(e));
        }

        return sb.toString();
    }

    /**
     * Prints a line to LogCat.
     * On Android, "System.out.println(...)" also prints to LogCat.
     * Do *NOT* "System.out.println(...)"; it would add a [near] duplicated line to LogCat. 
     * @param tag
     * @param level
     * @param msg
     * @param e
     * @return
     */
    public static String println(String tag, int level, String msg, Throwable e)
    {
        // LogCat does its own formatting (time, level, pid, tag, message).
        // Delay most custom formatting until *after* the message is printed to LogCat.

        // LogCat does not output the Thread ID; prepend msg with it here.
        StringBuffer sb = new StringBuffer();
        sb.append('T').append(Process.myTid()).append(' ').append(msg);

        // LogCat does not output the exception; append msg with it here. 
        if (e != null)
        {
            sb.append(": throwable=").append(Log.getStackTraceString(e));
            // null the exception so that format(...) [below] doesn't append it to msg a second time.
            e = null;
        }

        msg = sb.toString();

        // print to LogCat
        Log.println(level, tag, msg);

        // Now we can format the message for use by the caller 
        msg = format(tag, level, msg, e);

        // Again, do not "System.out.println(...)"; it would only add a [near] duplicate line to LogCat.

        return msg;
    }
}
