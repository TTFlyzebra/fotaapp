package com.flyzebra.fota.httpApi;

import com.flyzebra.fota.Config;
import com.flyzebra.utils.FlyLog;
import com.flyzebra.utils.PropUtil;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpService {
    private static final int DEFAULT_TIMEOUT = 15;
    private Api httpsApi;

    private static class TrustAllManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory sSLSocketFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllManager()}, new SecureRandom());
            sSLSocketFactory = sc.getSocketFactory();
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
        return sSLSocketFactory;
    }

    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    public HttpService() {
		String serverUrl = PropUtil.get(Config.PROP_API_BASE_URL, Config.API_BASE_URL);
        SSLSocketFactory sslSocketFactory = createSSLSocketFactory();
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, new TrustAllManager())
                .hostnameVerifier(new TrustAllHostnameVerifier())
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .client(httpClientBuilder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(serverUrl)
                    .build();
            httpsApi = retrofit.create(Api.class);
        } catch (Exception e) {
            FlyLog.d(e.toString());
        }
    }

    public Api getInspectionService() {
        return httpsApi;
    }
}
