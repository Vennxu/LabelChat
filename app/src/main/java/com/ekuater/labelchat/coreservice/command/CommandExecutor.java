
package com.ekuater.labelchat.coreservice.command;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.coreservice.command.client.AbstractResponse;
import com.ekuater.labelchat.coreservice.command.client.ClientFactory;
import com.ekuater.labelchat.coreservice.command.client.ICommandClient;
import com.ekuater.labelchat.coreservice.command.client.ICommandExecuteListener;
import com.ekuater.labelchat.coreservice.command.client.ICommandRequest;
import com.ekuater.labelchat.coreservice.command.client.ICommandResponse;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.util.L;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LinYong
 */
@SuppressWarnings("UnusedDeclaration")
public class CommandExecutor {

    private static final String TAG = CommandExecutor.class.getSimpleName();
    private static final String DEFAULT_BASE_URL = "http://127.0.0.1";
    private static final String REAL_URL_PREFIX = "http://";

    private static final int MSG_COMMAND_EXECUTE_COMMAND = 100;
    private static final int MSG_COMMAND_EXECUTE_SUCCESS = 101;
    private static final int MSG_COMMAND_EXECUTE_FAILURE = 102;

    private static final class ProcessHandler extends Handler {

        private final CommandExecutor mExecutor;

        public ProcessHandler(Looper looper, CommandExecutor executor) {
            super(looper);
            mExecutor = executor;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_COMMAND_EXECUTE_COMMAND:
                    mExecutor.executeInternal(msg.obj);
                    break;
                case MSG_COMMAND_EXECUTE_SUCCESS:
                    AsyncCommandResponse.onSuccess(msg.obj);
                    break;
                case MSG_COMMAND_EXECUTE_FAILURE:
                    AsyncCommandResponse.onFailure(msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private static CommandExecutor sInstance;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CommandExecutor(context.getApplicationContext());
        }
    }

    public static CommandExecutor getInstance(Context context) {
        if (sInstance == null) {
            initInstance(context);
        }
        return sInstance;
    }

    private static final class CommandResponse extends AbstractResponse {

        private ICommandExecuteListener mListener;

        public CommandResponse(ICommandExecuteListener listener) {
            mListener = listener;
        }

        @Override
        public void onSuccess(int statusCode, String response) {
            mListener.onSuccess(statusCode, response);
        }

        @Override
        public void onFailure(int statusCode, String response, Throwable throwable) {
            mListener.onFailure(statusCode, response, throwable);
        }
    }

    private static final class AsyncCommandResponse extends AbstractResponse {

        private static final class ResultArgs {
            public final String url;
            public final ICommandResponse response;
            public final int statusCode;
            public final String responseString;
            public final Throwable throwable;

            public ResultArgs(String url, ICommandResponse response, int statusCode,
                              String responseString, Throwable throwable) {
                this.url = url;
                this.response = response;
                this.statusCode = statusCode;
                this.responseString = responseString;
                this.throwable = throwable;
            }

            public ResultArgs(String url, ICommandResponse response, int statusCode,
                              String responseString) {
                this(url, response, statusCode, responseString, null);
            }
        }

        public static void onSuccess(Object obj) {
            try {
                ResultArgs args = (ResultArgs) obj;
                args.response.onSuccess(args.statusCode, args.responseString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void onFailure(Object obj) {
            try {
                ResultArgs args = (ResultArgs) obj;
                args.response.onFailure(args.statusCode, args.responseString, args.throwable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private final String mUrl;
        private final ICommandResponse mResponse;
        private final CommandExecutor mExecutor;
        private final Handler mHandler;

        public AsyncCommandResponse(String url, ICommandResponse response,
                                    CommandExecutor executor) {
            mUrl = url;
            mResponse = response;
            mExecutor = executor;
            mHandler = mExecutor.mProcessHandler;
        }

        private void postOnSuccess(int statusCode, String response) {
            ResultArgs args = new ResultArgs(mUrl, mResponse, statusCode, response);
            Message msg = mHandler.obtainMessage(MSG_COMMAND_EXECUTE_SUCCESS, args);
            mHandler.sendMessage(msg);
        }

        private void postOnFailure(int statusCode, String response, Throwable throwable) {
            ResultArgs args = new ResultArgs(mUrl, mResponse, statusCode, response, throwable);
            Message msg = mHandler.obtainMessage(MSG_COMMAND_EXECUTE_FAILURE, args);
            mHandler.sendMessage(msg);
        }

        @Override
        public void onSuccess(int statusCode, String response) {
            L.v(TAG, "onSuccess()"
                    + ", url=" + mUrl
                    + ", statusCode=" + statusCode
                    + ", response=" + ((response != null) ? response : "null"));
            postOnSuccess(statusCode, response);
            onExecuteResult();
        }

        @Override
        public void onFailure(int statusCode, String response, Throwable throwable) {
            L.v(TAG, "onFailure()"
                    + ", url=" + mUrl
                    + ", statusCode=" + statusCode
                    + ", response=" + ((response != null) ? response : "null")
                    + ", throwable=" + ((throwable != null) ? throwable : "null"));
            postOnFailure(statusCode, response, throwable);
            onExecuteResult();
        }

        private void onExecuteResult() {
            mExecutor.mCommandRequestMap.remove(mUrl);
        }
    }

    private final Context mContext;
    private final HandlerThread mProcessThread;
    private final Handler mProcessHandler;
    private final ICommandClient mCmdClient;
    private String mBaseUrl;
    private final Map<String, ICommandRequest> mCommandRequestMap = new ConcurrentHashMap<>();

    private CommandExecutor(Context context) {
        mContext = context;
        mProcessThread = new HandlerThread("CommandExecutor");
        mProcessThread.start();
        mProcessHandler = new ProcessHandler(mProcessThread.getLooper(), this);
        mCmdClient = ClientFactory.getDefaultClient();
        mCommandRequestMap.clear();
        getBaseUrl();
    }

    public void execute(RequestCommand command, ICommandExecuteListener listener) {
        CommandResponse cmdResponse = new CommandResponse(listener);
        execute(command.getSession(), command.getRequestMethod(), command.getUrl(),
                command.getParam(), cmdResponse);
    }

    public ICommandRequest getCommandRequest(RequestCommand command) {
        return mCommandRequestMap.get(command.getSession());
    }

    private void execute(String commandSession, int requestMethod, String url, String param,
                         ICommandResponse response) {
        L.v(TAG, "execute()"
                + ", requestMethod=" + requestMethod
                + ", url=" + url
                + ", param=" + param);
        Message msg = mProcessHandler.obtainMessage(MSG_COMMAND_EXECUTE_COMMAND,
                new CommandExecution(commandSession, requestMethod, url, param, response));
        mProcessHandler.sendMessage(msg);
    }

    private static final class CommandExecution {

        public final String commandSession;
        public final int requestMethod;
        public final String url;
        public final String param;
        public final ICommandResponse response;

        public CommandExecution(String commandSession, int requestMethod, String url,
                                String param, ICommandResponse response) {
            this.commandSession = commandSession;
            this.requestMethod = requestMethod;
            this.url = url;
            this.param = param;
            this.response = response;
        }
    }

    private void executeInternal(Object obj) {
        CommandExecution execution = (CommandExecution) obj;
        ICommandRequest request;
        AsyncCommandResponse asyncResponse = new AsyncCommandResponse(execution.url,
                execution.response, this);
        String realUrl = parseRealUrl(execution.url);

        switch (execution.requestMethod) {
            case ConstantCode.REQUEST_GET: {
                request = mCmdClient.get(realUrl, execution.param, asyncResponse);
                break;
            }
            case ConstantCode.REQUEST_POST: {
                request = mCmdClient.post(realUrl, execution.param, asyncResponse);
                break;
            }
            case ConstantCode.REQUEST_PUT: {
                request = mCmdClient.put(realUrl, execution.param, asyncResponse);
                break;
            }
            case ConstantCode.REQUEST_DELETE: {
                request = mCmdClient.delete(realUrl, execution.param, asyncResponse);
                break;
            }
            default: {
                request = mCmdClient.post(realUrl, execution.param, asyncResponse);
                break;
            }
        }

        mCommandRequestMap.put(execution.commandSession, request);
    }

    private void getBaseUrl() {
        String temp = mContext.getResources().getString(R.string.config_http_api_base_url);

        if (TextUtils.isEmpty(temp)) {
            temp = DEFAULT_BASE_URL;
        }

        while (temp.endsWith("/")) {
            temp = temp.substring(0, temp.length() - 1);
        }

        mBaseUrl = temp;
    }

    private String parseRealUrl(String url) {
        String realUrl = url;

        if (!TextUtils.isEmpty(url) && !url.startsWith(REAL_URL_PREFIX)) {
            StringBuilder sb = new StringBuilder();
            sb.append(mBaseUrl);
            if (!url.startsWith("/")) {
                sb.append("/");
            }
            sb.append(url);
            realUrl = sb.toString();
        }

        return realUrl;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mProcessThread.quit();
    }
}
