package com.ekuater.labelchat.delegate;

import android.content.Context;
import android.text.TextUtils;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.SessionCommand;
import com.ekuater.labelchat.command.UploadCommand;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.delegate.event.LoginEvent;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

/**
 * Created by Leo on 2015/3/18.
 *
 * @author LinYong
 */
public class FileUploader {

    private static final String REAL_URL_PREFIX = "http://";

    private final String mBaseUrl;
    private final AsyncHttpClient mHttpClient;

    public FileUploader(Context context) {
        mBaseUrl = parseApiBaseUrl(context);
        mHttpClient = HttpClient.getHttpClient();
    }

    public void doUpload(UploadCommand command, IUploadResponseHandler handler)
            throws FileNotFoundException {
        mHttpClient.post(getApiRealUrl(command.getUrl()), getRequestParams(command),
                new ResponseHandler(handler));
    }

    private RequestParams getRequestParams(UploadCommand command) throws FileNotFoundException {
        RequestParams params = new RequestParams();

        for (Map.Entry<String, String> entry : command.getParamMap().entrySet()) {
            params.put(entry.getKey(), entry.getValue());
        }
        List<File> fileList = command.getFileList();
        params.put(CommandFields.Normal.FILE, fileList.toArray(new File[fileList.size()]));
        return params;
    }

    private String parseApiBaseUrl(Context context) {
        String baseUrl = context.getString(R.string.config_http_api_base_url);
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private String getApiBaseUrl() {
        return mBaseUrl;
    }

    private String getApiRealUrl(String url) {
        String realUrl = url;

        if (!TextUtils.isEmpty(url) && !url.startsWith(REAL_URL_PREFIX)) {
            StringBuilder sb = new StringBuilder();
            sb.append(getApiBaseUrl());
            if (!url.startsWith("/")) {
                sb.append("/");
            }
            sb.append(url);
            realUrl = sb.toString();
        }

        return realUrl;
    }

    private static void checkSessionInvalid(JSONObject response) {
        SessionCommand.CommandResponse cmdResp = new SessionCommand.CommandResponse(response);
        if (cmdResp.isSessionInvalid()) {
            final int result = ConstantCode.ACCOUNT_OPERATION_USER_OR_PASSWORD_ERROR;
            final LoginEvent.From from = LoginEvent.From.FILE_UPLOADER;
            CoreEventBusHub.getDefaultEventBus().post(new LoginEvent(result, from));
        }
    }

    private static class ResponseHandler extends JsonHttpResponseHandler {

        private static final int STATE_INIT = 0;
        private static final int STATE_REQUEST = 1;
        private static final int STATE_RESPONSE = 2;

        private final IUploadResponseHandler mHandler;
        private int mInRequest = STATE_INIT;

        public ResponseHandler(IUploadResponseHandler handler) {
            mHandler = handler;
        }

        private String jsonToString(JSONObject json) {
            return (json != null) ? json.toString() : null;
        }

        private String jsonToString(JSONArray json) {
            return (json != null) ? json.toString() : null;
        }

        @Override
        public void onStart() {
            super.onStart();
            mInRequest = STATE_REQUEST;
        }

        @Override
        public void onRetry(int retryNo) {
            super.onRetry(retryNo);
            mInRequest = STATE_REQUEST;
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            super.onProgress(bytesWritten, totalSize);
            if (mInRequest == STATE_REQUEST) {
                mHandler.onProgress(bytesWritten, totalSize);
            }
        }

        @Override
        public void onCancel() {
            super.onCancel();
            mInRequest = STATE_INIT;
        }

        @Override
        public void onFinish() {
            super.onFinish();
            mInRequest = STATE_INIT;
        }

        @Override
        public void onPreProcessResponse(ResponseHandlerInterface instance,
                                         HttpResponse response) {
            super.onPreProcessResponse(instance, response);
            mInRequest = STATE_RESPONSE;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            mHandler.onResponse(ConstantCode.EXECUTE_RESULT_SUCCESS,
                    jsonToString(response));
            checkSessionInvalid(response);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            mHandler.onResponse(ConstantCode.EXECUTE_RESULT_NETWORK_ERROR,
                    jsonToString(response));
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                              JSONObject errorResponse) {
            mHandler.onResponse(ConstantCode.EXECUTE_RESULT_NETWORK_ERROR,
                    jsonToString(errorResponse));
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                              JSONArray errorResponse) {
            mHandler.onResponse(ConstantCode.EXECUTE_RESULT_NETWORK_ERROR,
                    jsonToString(errorResponse));
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString,
                              Throwable throwable) {
            mHandler.onResponse(ConstantCode.EXECUTE_RESULT_NETWORK_ERROR,
                    responseString);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, String responseString) {
            mHandler.onResponse(ConstantCode.EXECUTE_RESULT_NETWORK_ERROR,
                    responseString);
        }
    }
}
