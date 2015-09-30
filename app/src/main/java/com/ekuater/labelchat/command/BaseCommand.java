
package com.ekuater.labelchat.command;

import android.text.TextUtils;

import com.ekuater.labelchat.coreservice.command.client.ICommandRequest;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class BaseCommand {

    private static final String TAG = BaseCommand.class.getSimpleName();

    // Default HTTP request method
    public static final int DEFAULT_REQUEST = ConstantCode.REQUEST_POST;

    public static boolean isRunning(ICommandRequest cmdRequest) {
        return (cmdRequest != null && !cmdRequest.isFinished()
                && !cmdRequest.isCancelled());
    }

    private JSONObject mJsonParam;
    private int mRequestMethod;
    private String mUrl;

    public BaseCommand() {
        mRequestMethod = DEFAULT_REQUEST;
        initParam();
    }

    private void initParam() {
        mJsonParam = new JSONObject();
        putBaseParameters();
    }

    public void setRequestMethod(int method) {
        mRequestMethod = method;
    }

    public int getRequestMethod() {
        return mRequestMethod;
    }

    public String getUrl() {
        return mUrl;
    }

    protected void setUrl(String url) {
        mUrl = url;
    }

    public final void putParam(String name, String value) {
        try {
            mJsonParam.put(name, value);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
    }

    public final void putParam(String name, JSONArray value) {
        try {
            mJsonParam.put(name, value);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
    }

    public final void putParam(String name, int value) {
        try {
            mJsonParam.put(name, value);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
    }

    public final void putParam(String name, long value) {
        try {
            mJsonParam.put(name, value);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
    }

    public final void putParam(String name, boolean value) {
        try {
            mJsonParam.put(name, value);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
    }

    public void clearParams() {
        initParam();
    }

    protected void putBaseParameters() {
        putParam(CommandFields.Base.CLIENT_TYPE, CommandFields.Base.DEFAULT_CLIENT_TYPE);
        putParam(CommandFields.Base.INTERFACE_VERSION, CommandFields.Base.DEFAULT_INTERFACE_VERSION);
    }

    public RequestCommand toRequestCommand() {
        RequestCommand request = new RequestCommand();

        request.setUrl(getUrl());
        request.setRequestMethod(getRequestMethod());
        request.setParam(mJsonParam.toString());

        return request;
    }

    @Override
    public String toString() {
        return ("RequestMethod=" + mRequestMethod)
                + (",Url=" + mUrl)
                + (",Param=" + mJsonParam.toString());
    }

    public static class CommandResponse {

        protected final JSONObject mResponseJson;

        public CommandResponse(String response) throws JSONException {
            this(new JSONObject(response));
        }

        public CommandResponse(JSONObject response) {
            mResponseJson = response;
        }

        public boolean executedSuccess() {
            return (CommandFields.Base.STATE_SUCCESS.equals(
                    getValueString(CommandFields.Base.STATE)));
        }

        public boolean requestSuccess() {
            return executedSuccess() && (getErrorCode()
                    == CommandErrorCode.REQUEST_SUCCESS);
        }

        public String getErrorDesc() {
            return getValueString(CommandFields.Base.ERROR_DESC);
        }

        public int getErrorCode() {
            int code = CommandErrorCode.EXECUTE_FAILED;

            String codeString = getValueString(CommandFields.Base.ERROR_CODE);
            if (!TextUtils.isEmpty(codeString)) {
                try {
                    code = Integer.valueOf(codeString);
                } catch (NumberFormatException e) {
                    L.w(TAG, e);
                }
            }

            return code;
        }

        protected final String getValueString(String name) {
            return mResponseJson.optString(name, null);
        }

        protected final int getValueInt(String name) {
            return mResponseJson.optInt(name, 0);
        }

        protected final JSONObject getValueJson(String name) {
            return mResponseJson.optJSONObject(name);
        }

        protected final JSONArray getValueJsonArray(String name) {
            return mResponseJson.optJSONArray(name);
        }

        protected final boolean getValueBoolean(String name) {
            return mResponseJson.optBoolean(name, false);
        }

        @Override
        public String toString() {
            return mResponseJson.toString();
        }
    }
}
