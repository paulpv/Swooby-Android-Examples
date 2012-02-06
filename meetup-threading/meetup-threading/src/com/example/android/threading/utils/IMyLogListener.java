package com.example.android.threading.utils;

public interface IMyLogListener
{
    public void println(String msg);

    public void println(String tag, int level, String msg, Throwable e);
}
