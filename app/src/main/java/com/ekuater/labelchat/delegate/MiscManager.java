package com.ekuater.labelchat.delegate;

import android.content.Context;
import android.text.TextUtils;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.ComplainCommand;
import com.ekuater.labelchat.command.FeedbackCommand;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.util.TextUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Miscellaneous function of CoreService
 *
 * @author LinYong
 */
public class MiscManager extends BaseManager {

    public interface IListener extends BaseManager.IListener {
        public void onNetworkAvailableChanged(boolean networkAvailable);
    }

    public static class AbsListener implements IListener {
        @Override
        public void onCoreServiceConnected() {
        }

        @Override
        public void onCoreServiceDied() {
        }

        @Override
        public void onNetworkAvailableChanged(boolean networkAvailable) {
        }
    }

    private static MiscManager sInstance;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MiscManager(context.getApplicationContext());
        }
    }

    public static MiscManager getInstance(Context context) {
        if (sInstance == null) {
            initInstance(context);
        }
        return sInstance;
    }

    private final List<WeakReference<IListener>> mListeners = new ArrayList<>();
    private final ICoreServiceNotifier mNotifier = new AbstractCoreServiceNotifier() {
        @Override
        public void onCoreServiceConnected() {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onCoreServiceConnected();
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onCoreServiceDied() {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onCoreServiceDied();
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onNetworkAvailableChanged(boolean networkAvailable) {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onNetworkAvailableChanged(networkAvailable);
                } else {
                    mListeners.remove(i);
                }
            }
        }
    };

    private MiscManager(Context context) {
        super(context);
        mCoreService.registerNotifier(mNotifier);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mCoreService.unregisterNotifier(mNotifier);
    }

    public void registerListener(IListener listener) {
        synchronized (mListeners) {
            for (WeakReference<IListener> ref : mListeners) {
                if (ref.get() == listener) {
                    return;
                }
            }
            mListeners.add(new WeakReference<>(listener));
            unregisterListener(null);
        }
    }

    public void unregisterListener(IListener listener) {
        synchronized (mListeners) {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                if (mListeners.get(i).get() == listener) {
                    mListeners.remove(i);
                }
            }
        }
    }

    public void exitApp() {
        mCoreService.exitApp();
    }

    public boolean isNetworkAvailable() {
        return mCoreService.isNetworkAvailable();
    }

    /**
     * Request CoreService to execute the command
     *
     * @param command the command to be executed
     * @param handler command execute response handler
     */
    public void executeCommand(BaseCommand command, ICommandResponseHandler handler) {
        mCoreService.executeCommand(command, handler);
    }

    /**
     * Request CoreService to execute the command
     *
     * @param request the command request to be executed
     * @param handler command execute response handler
     */
    public void executeCommand(RequestCommand request, ICommandResponseHandler handler) {
        mCoreService.executeCommand(request, handler);
    }

    /**
     * request to call interface to server
     *
     * @param url     relative url of interface, like "/api/user/mobileVerifyCode"
     * @param param   the parameter of call, always be json string
     * @param handler interface call response handler
     */
    public void executeCommand(String url, String param, ICommandResponseHandler handler) {
        RequestCommand request = new RequestCommand();

        request.setUrl(url);
        request.setRequestMethod(ConstantCode.REQUEST_POST);
        request.setParam(param);
        executeCommand(request, handler);
    }

    /**
     * Upload suggestion to server
     *
     * @param nickname    user nickname
     * @param suggestion  suggestion message
     * @param contactInfo contact information
     * @param listener    upload result response listener
     */
    public void uploadFeedbackSuggestion(String nickname, String suggestion, String contactInfo,
                                         FunctionCallListener listener) {
        if (TextUtils.isEmpty(suggestion)) {
            if (listener != null) {
                listener.onCallResult(FunctionCallListener.RESULT_CALL_SUCCESS,
                        CommandErrorCode.REQUEST_SUCCESS, null);
            }
            return;
        }

        FeedbackCommand command = new FeedbackCommand(getSession(),
                getUserId(), getLabelCode());
        command.putParamNickname(nickname);
        command.putParamSuggestion(suggestion);
        command.putParamContactInfo(contactInfo);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    public void complainUser(String complainUserId, FunctionCallListener listener) {
        complainUser(complainUserId, null, listener);
    }

    public void complainDynamic(String dynamicId, FunctionCallListener listener) {
        complainDynamic(dynamicId, null, listener);
    }

    public void complainConfide(String confideId, FunctionCallListener listener) {
        complainConfide(confideId, null, listener);
    }

    public void complainUser(String complainUserId, String content, FunctionCallListener listener) {
        complain(CommandFields.Normal.COMPLAIN_TYPE_USER, complainUserId, content, listener);
    }

    public void complainDynamic(String dynamicId, String content, FunctionCallListener listener) {
        complain(CommandFields.Normal.COMPLAIN_TYPE_DYNAMIC, dynamicId, content, listener);
    }

    public void complainConfide(String confideId, String content, FunctionCallListener listener) {
        complain(CommandFields.Normal.COMPLAIN_TYPE_CONFIDE, confideId, content, listener);
    }

    private void complain(String complainType, String objectId, String content,
                          FunctionCallListener listener) {
        ComplainCommand command = new ComplainCommand(getSession(), getUserId());
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        command.putParamComplainType(complainType);
        command.putParamObjectId(objectId);
        if (!TextUtil.isEmpty(content)) {
            command.putParamContent(content);
        }
        executeCommand(command, handler);
    }
}
