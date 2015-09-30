
package com.ekuater.labelchat.coreservice.command;

import android.content.Context;

import com.ekuater.labelchat.command.SessionCommand;
import com.ekuater.labelchat.coreservice.command.client.ICommandExecuteListener;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LinYong
 */
public class CommandProcessor {

    private static final String TAG = CommandProcessor.class.getSimpleName();

    private interface ListenerNotifier {
        public void notify(ICommandProcessListener listener);
    }

    private final CommandExecutor mCmdExecutor;
    private final List<ICommandProcessListener> mListeners = new ArrayList<ICommandProcessListener>();

    private class CmdExecuteListener implements ICommandExecuteListener {

        private final String mCmdSession;

        public CmdExecuteListener(String cmdSession) {
            mCmdSession = cmdSession;
        }

        @Override
        public void onSuccess(int statusCode, String response) {
            notifySuccess(mCmdSession, response);
            checkSessionInvalid(mCmdSession, response);
            onExecuteResult();
        }

        @Override
        public void onFailure(int statusCode, String response, Throwable throwable) {
            notifyFailure(mCmdSession, statusCode, response);
            onExecuteResult();
        }

        private void onExecuteResult() {
        }
    }

    public CommandProcessor(Context context) {
        mCmdExecutor = CommandExecutor.getInstance(context);
    }

    public void registerListener(final ICommandProcessListener listener) {
        for (ICommandProcessListener tempListener : mListeners) {
            if (tempListener == listener) {
                return;
            }
        }

        mListeners.add(listener);
        unregisterListener(null);
    }

    public void unregisterListener(final ICommandProcessListener listener) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            if (mListeners.get(i) == listener) {
                mListeners.remove(i);
            }
        }
    }

    public void executeCommand(RequestCommand command) {
        if (command == null) {
            return;
        }
        mCmdExecutor.execute(command, new CmdExecuteListener(command.getSession()));
    }

    private void checkSessionInvalid(String cmdSession, String response) {
        try {
            SessionCommand.CommandResponse cmdResp
                    = new SessionCommand.CommandResponse(response);
            if (cmdResp.isSessionInvalid()) {
                notifySessionInvalid(cmdSession, response);
            }
        } catch (JSONException e) {
            L.w(TAG, e);
        }
    }

    private void notifyListeners(ListenerNotifier notifier) {
        for (ICommandProcessListener listener : mListeners) {
            if (listener != null) {
                try {
                    notifier.notify(listener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void notifySuccess(String cmdSession, String response) {
        notifyListeners(new SuccessNotifier(cmdSession, response));
    }

    private void notifyFailure(String cmdSession, int statusCode, String response) {
        notifyListeners(new FailureNotifier(cmdSession, statusCode, response));
    }

    private void notifySessionInvalid(String cmdSession, String response) {
        notifyListeners(new SessionInvalidNotifier(cmdSession, response));
    }

    private static class SuccessNotifier implements ListenerNotifier {

        private final String cmdSession;
        private final String response;

        public SuccessNotifier(String cmdSession, String response) {
            this.cmdSession = cmdSession;
            this.response = response;
        }

        @Override
        public void notify(ICommandProcessListener listener) {
            listener.onSuccess(cmdSession, response);
        }
    }

    private static class FailureNotifier implements ListenerNotifier {

        private final String cmdSession;
        private final int statusCode;
        private final String response;

        public FailureNotifier(String cmdSession, int statusCode, String response) {
            this.cmdSession = cmdSession;
            this.statusCode = statusCode;
            this.response = response;
        }

        @Override
        public void notify(ICommandProcessListener listener) {
            listener.onFailure(cmdSession, response, statusCode);
        }
    }

    private static class SessionInvalidNotifier implements ListenerNotifier {

        private final String cmdSession;
        private final String response;

        public SessionInvalidNotifier(String cmdSession, String response) {
            this.cmdSession = cmdSession;
            this.response = response;
        }

        @Override
        public void notify(ICommandProcessListener listener) {
            listener.onSessionInvalid(cmdSession, response);
        }
    }
}
