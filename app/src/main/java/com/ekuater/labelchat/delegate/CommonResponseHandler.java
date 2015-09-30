package com.ekuater.labelchat.delegate;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

/**
 * Created by Leo on 2015/1/26.
 *
 * @author LinYong
 */
/*package*/ class CommonResponseHandler implements ICommandResponseHandler {

    private static final String TAG = CommonResponseHandler.class.getSimpleName();

    private final FunctionCallListener listener;

    public CommonResponseHandler(FunctionCallListener listener) {
        this.listener = listener;
    }

    @Override
    public void onResponse(int result, String response) {
        if (listener == null) {
            // no available listener, so do not need to care about the
            // response result.
            return;
        }

        int callResult = FunctionCallListener.RESULT_UNKNOWN_ERROR;
        int errorCode = CommandErrorCode.EXECUTE_FAILED;
        String errorDesc = null;

        switch (result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS: {
                try {
                    BaseCommand.CommandResponse cmdResp
                            = new BaseCommand.CommandResponse(response);
                    if (cmdResp.requestSuccess()) {
                        callResult = FunctionCallListener.RESULT_CALL_SUCCESS;
                    } else {
                        callResult = FunctionCallListener.RESULT_CALL_FAILED;
                    }
                    errorCode = cmdResp.getErrorCode();
                    errorDesc = cmdResp.getErrorDesc();
                } catch (JSONException e) {
                    L.w(TAG, e);
                    callResult = FunctionCallListener.RESULT_RESPONSE_ERROR;
                }
                break;
            }
            case ConstantCode.EXECUTE_RESULT_NETWORK_ERROR:
                callResult = FunctionCallListener.RESULT_NETWORK_ERROR;
                break;
            default:
                break;
        }
        listener.onCallResult(callResult, errorCode, errorDesc);
    }
}
