package com.podcasses.util;

import android.content.Context;
import android.util.Log;

import com.podcasses.R;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * Created by aleksandar.kovachev.
 */
public class OkHttpUtil {

    public static OkHttpClient.Builder getTrustedOkHttpClient(Context context) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);

            InputStream is = context.getResources().openRawResource(R.raw.podcasses);
            BufferedInputStream bis = new BufferedInputStream(is);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            while (bis.available() > 0) {
                Certificate cert = certificateFactory.generateCertificate(bis);
                keyStore.setCertificateEntry("podcasses.com", cert);
            }

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, null);

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0]);
        } catch (Exception e) {
            Log.e(OkHttpUtil.class.getSimpleName(), "getTrustedOkHttpClient: ", e);
            return null;
        }
    }

}
