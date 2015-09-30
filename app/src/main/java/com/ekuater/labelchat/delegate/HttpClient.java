package com.ekuater.labelchat.delegate;

import com.loopj.android.http.AsyncHttpClient;

/**
 * Created by Leo on 2015/1/5.
 *
 * @author LinYong
 */
public final class HttpClient {

    public static final String HEADER_ACCEPT = "accept";
    public static final String APPLICATION_JSON = "application/json";

    private static AsyncHttpClient sClient;

    private static synchronized void initClient() {
        if (sClient == null) {
            sClient = new AsyncHttpClient();
            sClient.setTimeout(30 * 1000);
            sClient.addHeader(HEADER_ACCEPT, APPLICATION_JSON);;
        }
    }

    public static AsyncHttpClient getHttpClient() {
        if (sClient == null) {
            initClient();
        }
        return sClient;
    }
}
