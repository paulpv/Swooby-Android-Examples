package com.example.android.threading.utils;

public abstract class MyUri
{
    public static final String URI_SCHEME_HTTP  = "http";
    public static final String URI_SCHEME_HTTPS = "https";

    public static String escape(String uriString)
    {
        uriString = uriString.replace(" ", "%20");
        return uriString;
    }

    public static String toString(MyUri[] uris)
    {
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        for (int i = 0; i < uris.length; i++)
        {
            if (i > 0)
            {
                sb.append(',');
            }
            sb.append('\"').append(uris[i].toString()).append('\"');
        }
        sb.append(']');
        return sb.toString();
    }

    public abstract String getScheme();

    public abstract String getHost();

    public abstract int getPort();

    public abstract String getPath();

    public abstract String getQueryParameter(String key);

    public abstract MyUri.Builder buildUpon();

    public static abstract class Builder
    {
        public abstract void scheme(String scheme);

        public abstract void authority(String authority);

        public abstract void path(String path);

        public abstract void appendQueryParameter(String key, String value);

        public abstract MyUri build();
    }
}
