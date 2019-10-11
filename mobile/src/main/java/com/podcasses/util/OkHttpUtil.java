package com.podcasses.util;

import android.util.Log;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * Created by aleksandar.kovachev.
 */
public class OkHttpUtil {

    public static OkHttpClient.Builder getTrustedOkHttpClient() {
        try {
            try {
                final X509TrustManager[] trustAllCerts = new X509TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };

                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0]);
                builder.hostnameVerifier((hostname, session) -> true);
                return builder;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            Log.e(OkHttpUtil.class.getSimpleName(), "getTrustedOkHttpClient: ", e);
            return null;
        }
    }

}
