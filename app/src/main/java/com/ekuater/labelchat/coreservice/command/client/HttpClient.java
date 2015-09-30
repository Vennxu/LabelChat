
package com.ekuater.labelchat.coreservice.command.client;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * @author LinYong
 */
public class HttpClient extends AbstractClient {

    // private static final String TAG = "HttpClient";
    private static final String HEADER_ACCEPT = "accept";
    private static final String APPLICATION_JSON = "application/json";

    private static HttpClient sInstance;

    private static synchronized void initInstance() {
        if (sInstance == null) {
            sInstance = new HttpClient();
        }
    }

    public static HttpClient getInstance() {
        if (sInstance == null) {
            initInstance();
        }
        return sInstance;
    }

    private static final class LocalResponseHandler extends JsonHttpResponseHandler {

        private final ICommandResponse mResponse;

        public LocalResponseHandler(ICommandResponse response) {
            mResponse = response;
        }

        private int convertStatusCode(int statusCode) {
            return statusCode;
        }

        private String jsonToString(JSONObject json) {
            return (json != null) ? json.toString() : null;
        }

        private String jsonToString(JSONArray json) {
            return (json != null) ? json.toString() : null;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            mResponse.onSuccess(convertStatusCode(statusCode), jsonToString(response));
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            mResponse.onFailure(convertStatusCode(statusCode), response.toString(), new JSONException(
                    "JSONArray, unexpected response type," + response.toString()));
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                              JSONObject errorResponse) {
            mResponse.onFailure(convertStatusCode(statusCode), jsonToString(errorResponse), throwable);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                              JSONArray errorResponse) {
            mResponse.onFailure(convertStatusCode(statusCode), jsonToString(errorResponse), throwable);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString,
                              Throwable throwable) {
            mResponse.onFailure(convertStatusCode(statusCode), responseString, throwable);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, String responseString) {
            mResponse.onFailure(convertStatusCode(statusCode), responseString, new JSONException(
                    "Unexpected response type," + responseString));
        }
    }

    private final AsyncHttpClient mClient;

    private HttpClient() {
        mClient = new AsyncHttpClient();
        mClient.addHeader(HEADER_ACCEPT, APPLICATION_JSON);
    }

    @Override
    public ICommandRequest get(String url, String param, ICommandResponse response) {
        return post(url, param, response);
    }

    @Override
    public ICommandRequest post(String url, String param, ICommandResponse response) {
        LocalResponseHandler responseHandler = new LocalResponseHandler(response);
        StringEntity entity = convertParam(param);
        RequestHandle requestHandle;

        if (entity == null) {
            return null;
        }

        requestHandle = mClient.post(null, url, entity, null, responseHandler);

        return new HttpRequest(requestHandle);
    }

    private StringEntity convertParam(String param) {
        StringEntity entity = null;

        try {
            entity = new StringEntity(param, HTTP.UTF_8);
            entity.setContentType(APPLICATION_JSON);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return entity;
    }
}
