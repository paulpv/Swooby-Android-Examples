package com.example.android.threading.utils;

import android.net.Uri;

public class MyUriPlatform extends MyUri
{
    public static final MyUriPlatform EMPTY = new MyUriPlatform(Uri.EMPTY);

    public static MyUriPlatform parse(String uriString)
    {
        return new MyUriPlatform(Uri.parse(MyUri.escape(uriString)));
    }

    // TODO:(pv) Somehow move this to WtcUri?
    public static boolean isNullOrEmpty(MyUri[] uris)
    {
        if (uris == null || uris.length == 0)
        {
            return true;
        }
        for (int i = 0; i < uris.length; i++)
        {
            MyUri uri = uris[i];
            if (uri == null || uri == MyUriPlatform.EMPTY)
            {
                return true;
            }
        }
        return false;
    }

    private final Uri uri;

    private MyUriPlatform(Uri uri)
    {
        this.uri = uri;
    }

    public String toString()
    {
        return uri.toString();
    }

    public String getScheme()
    {
        return uri.getScheme();
    }

    public String getHost()
    {
        return uri.getHost();
    }

    public int getPort()
    {
        return uri.getPort();
    }

    public String getPath()
    {
        return uri.getPath();
    }

    public String getQueryParameter(String key)
    {
        return uri.getQueryParameter(key);
    }

    public MyUri.Builder buildUpon()
    {
        return new Builder(uri.buildUpon());
    }

    public static class Builder extends MyUri.Builder
    {
        private final android.net.Uri.Builder builder;

        public Builder()
        {
            this(new android.net.Uri.Builder());
        }

        private Builder(android.net.Uri.Builder builder)
        {
            this.builder = builder;
        }

        public void scheme(String scheme)
        {
            builder.scheme(scheme);
        }

        public void authority(String authority)
        {
            builder.authority(authority);
        }

        public void path(String path)
        {
            builder.path(path);
        }

        public void appendQueryParameter(String key, String value)
        {
            builder.appendQueryParameter(key, value);
        }

        public MyUriPlatform build()
        {
            return new MyUriPlatform(builder.build());
        }
    }
}
