package com.example.android.threading.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class MyHttpConnectionPlatform extends MyConnection
{
    private static final String       TAG = MyLog.TAG(MyHttpConnectionPlatform.class);

    protected final HttpURLConnection connection;

    protected MyHttpConnectionPlatform(MyUri uri) throws IOException
    {
        URL url = new URL(uri.toString());
        connection = (HttpURLConnection) url.openConnection();
        setConnectTimeout(MyConnection.getDefaultConnectTimeout());
    }

    public InputStream openInputStream() throws IOException
    {
        return connection.getInputStream();
    }

    public void close() throws IOException
    {
        // ignore; there is no URLConnection.close() in Android
    }

    public void setConnectTimeout(int timeout) throws IllegalArgumentException
    {
        MyLog.i(TAG, "+setConnectTimeout(" + timeout + ')');
        connection.setConnectTimeout(timeout);
        MyLog.i(TAG, "-setConnectTimeout(" + timeout + ')');
    }

    public void setRequestMethod(String method) throws ProtocolException
    {
        connection.setRequestMethod(method);
    }

    public void setDoOutput(boolean newValue)
    {
        connection.setDoOutput(newValue);
    }

    public void connect() throws IOException
    {
        connection.connect();
    }

    public int getResponseCode() throws IOException
    {
        return connection.getResponseCode();
    }

    public String getResponseMessage() throws IOException
    {
        return connection.getResponseMessage();
    }

    public int getContentLength()
    {
        return connection.getContentLength();
    }
}
