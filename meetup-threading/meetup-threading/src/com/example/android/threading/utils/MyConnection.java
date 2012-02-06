package com.example.android.threading.utils;

import java.io.IOException;
import java.io.InputStream;

public abstract class MyConnection
{
    private static int defaultConnectTimeout = 15000;

    /**
     * @return the default connecting timeout in milliseconds.
     */
    public static int getDefaultConnectTimeout()
    {
        return defaultConnectTimeout;
    }

    /**
     * @param timeout the default connecting timeout in milliseconds.
     */
    public static void setDefaultConnectTimeout(int timeout)
    {
        defaultConnectTimeout = timeout;
    }

    public static MyConnection open(MyUri uri) throws IOException
    {
        String scheme = uri.getScheme();
        if (MyUri.URI_SCHEME_HTTPS.equalsIgnoreCase(scheme))
        {
            return new MyHttpsConnectionPlatform(uri);
        }
        else if (MyUri.URI_SCHEME_HTTP.equalsIgnoreCase(scheme))
        {
            return new MyHttpConnectionPlatform(uri);
        }
        else
        {
            throw new IllegalArgumentException("uriLocator scheme must be either http, https");
        }
    }

    public abstract InputStream openInputStream() throws IOException;

    public abstract void close() throws IOException;

    /**
     * Sets the timeout value in milliseconds for establishing the connection to the resource pointed by this WtcConnection instance.
     * A SocketTimeoutException is thrown if the connection could not be established in this time.
     * Default is WtcUrlConnection.getDefaultConnectTimeout().
     * @param timeout the connecting timeout in milliseconds.
     * @throws IllegalArgumentException if the parameter timeout is less than zero.
     */
    public abstract void setConnectTimeout(int timeout) throws IllegalArgumentException;
}
