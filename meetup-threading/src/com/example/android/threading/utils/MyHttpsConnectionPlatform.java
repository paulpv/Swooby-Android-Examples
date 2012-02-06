package com.example.android.threading.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public class MyHttpsConnectionPlatform extends MyHttpConnectionPlatform
{
    private static final String           TAG = MyLog.TAG(MyHttpsConnectionPlatform.class);

    private final static HostnameVerifier defaultHostnameVerifier;
    private final static SSLSocketFactory defaultSSLSocketFactory;

    static
    {
        defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
    }

    /**
     * Idea came from:
     * http://stackoverflow.com/questions/1217141/self-signed-ssl-acceptance-android
     */
    public static void setUnsafe(boolean unsafe)
    {
        HostnameVerifier hv;
        SSLSocketFactory ssf;

        if (unsafe)
        {
            hv = new HostnameVerifier()
            {
                public boolean verify(String hostname, SSLSession session)
                {
                    return true;
                }
            };

            SSLContext context;
            try
            {
                context = SSLContext.getInstance("TLS");
                context.init(null, new X509TrustManager[]
                {
                    new X509TrustManager()
                    {
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
                        {
                        }

                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
                        {
                        }

                        public X509Certificate[] getAcceptedIssuers()
                        {
                            return new X509Certificate[0];
                        }
                    }
                }, new SecureRandom());
                ssf = context.getSocketFactory();
            }
            catch (NoSuchAlgorithmException e)
            {
                // should never happen
                e.printStackTrace();

                ssf = defaultSSLSocketFactory;
            }
            catch (KeyManagementException e)
            {
                // should never happen
                e.printStackTrace();

                ssf = defaultSSLSocketFactory;
            }
        }
        else
        {
            hv = defaultHostnameVerifier;
            ssf = defaultSSLSocketFactory;
        }

        HttpsURLConnection.setDefaultHostnameVerifier(hv);
        HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
    }

    public static boolean getUnsafe()
    {
        return HttpsURLConnection.getDefaultSSLSocketFactory() != defaultSSLSocketFactory;
    }

    public MyHttpsConnectionPlatform(MyUri uri) throws IOException
    {
        super(uri);
    }

    public InputStream openInputStream() throws IOException
    {
        return ((HttpsURLConnection) connection).getInputStream();
    }
}
