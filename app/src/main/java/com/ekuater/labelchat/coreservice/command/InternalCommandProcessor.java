
package com.ekuater.labelchat.coreservice.command;

import android.content.Context;

import com.ekuater.labelchat.command.SessionCommand;
import com.ekuater.labelchat.coreservice.command.client.ICommandExecuteListener;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class InternalCommandProcessor {

    private static final String TAG = InternalCommandProcessor.class.getSimpleName();

    private interface ListenerNotifier {
        public void notify(ICommandCheckListener listener);
    }

    private class CmdExecuteListener implements ICommandExecuteListener {

        private final RequestCommand mCommand;
        private final ICommandResponseHandler mHandler;

        public CmdExecuteListener(RequestCommand command, ICommandResponseHandler handler) {
            mCommand = command;
            mHandler = handler;
        }

        @Override
        public void onSuccess(int statusCode, String response) {
            mHandler.onResponse(mCommand, ConstantCode.EXECUTE_RESULT_SUCCESS, response);
            checkSessionInvalid(mCommand.getSession(), response);
        }

        @Override
        public void onFailure(int statusCode, String response, Throwable throwable) {
            mHandler.onResponse(mCommand, ConstantCode.EXECUTE_RESULT_NETWORK_ERROR, response);
        }
    }

    private final CommandExecutor mCmdExecutor;
    private final ICommandCheckListener mListener;

    public InternalCommandProcessor(Context context, ICommandCheckListener listener) {
        mCmdExecutor = CommandExecutor.getInstance(context);
        mListener = listener;
    }

    public void executeCommand(RequestCommand command, ICommandResponseHandler handler) {
        mCmdExecutor.execute(command, new CmdExecuteListener(command, handler));
    }

    private void notifyListeners(ListenerNotifier notifier) {
        if (mListener != null) {
            try {
                notifier.notify(mListener);
            } catch (Exception e) {
                L.w(TAG, e);
            }
        }
    }

    private void checkSessionInvalid(String cmdSession, String response) {
        try {
            SessionCommand.CommandResponse cmdResp
                    = new SessionCommand.CommandResponse(response);
            if (cmdResp.isSessionInvalid()) {
                notifySessionInvalid(cmdSession, response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void notifySessionInvalid(String cmdSession, String response) {
        notifyListeners(new SessionInvalidNotifier(cmdSession, response));
    }

    private static class SessionInvalidNotifier implements ListenerNotifier {

        private final String cmdSession;
        private final String response;

        public SessionInvalidNotifier(String cmdSession, String response) {
            this.cmdSession = cmdSession;
            this.response = response;
        }

        @Override
        public void notify(ICommandCheckListener listener) {
            listener.onSessionInvalid(cmdSession, response);
        }
    }
}
