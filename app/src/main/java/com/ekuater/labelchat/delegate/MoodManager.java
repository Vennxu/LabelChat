package com.ekuater.labelchat.delegate;

import android.content.Context;
import android.text.TextUtils;

import com.ekuater.labelchat.command.interest.GetInterestCommand;
import com.ekuater.labelchat.command.interest.PushInteractCommand;
import com.ekuater.labelchat.command.interest.SetInterestCommand;
import com.ekuater.labelchat.command.mood.MoodCommand;
import com.ekuater.labelchat.command.recent.RecentCommand;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.InterestType;
import com.ekuater.labelchat.datastruct.MoodUser;
import com.ekuater.labelchat.datastruct.PushInteract;
import com.ekuater.labelchat.datastruct.RecentVisitor;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/3/21.
 *
 * @author FanChong
 */
public class MoodManager extends BaseManager {
    private static final String TAG = MoodManager.class.getSimpleName();
    public static final int QUERY_RESULT_SUCCESS = 0;
    public static final int QUERY_RESULT_ILLEGAL_ARGUMENTS = 1;
    public static final int QUERY_RESULT_QUERY_FAILURE = 2;
    public static final int QUERY_RESULT_RESPONSE_DATA_ERROR = 3;

    public MoodManager(Context context) {
        super(context);
    }

    public interface IListener extends BaseManager.IListener {
        public void onUserInterestUpdate();

        public void onAddInterestResult(int result);
    }

    public interface ListenerNotifier {
        public void notify(IListener listener);
    }

    private static MoodManager sInstance;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MoodManager(context.getApplicationContext());
        }
    }

    public static MoodManager getInstance(Context context) {
        if (sInstance == null) {
            initInstance(context);
        }
        return sInstance;
    }

    public interface MoodSendObserver {
        public void onQueryResult(int result);
    }

    public void moodSend(String moodContent, ArrayList<MoodUser> moodUsers, MoodSendObserver observer) {
        if (observer == null) {
            return;
        }
        MoodCommand command = new MoodCommand(getSession(), getUserId());
        command.putParamMood(moodContent);
        command.putParamArrayUserId(moodUsers);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (!(mObj instanceof MoodSendObserver)) {
                    return;
                }
                MoodSendObserver observer = (MoodSendObserver) mObj;
                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE);
                    return;
                }
                try {
                    MoodCommand.CommandResponse cmdResp = new MoodCommand.CommandResponse(response);
                    int ret_ = QUERY_RESULT_QUERY_FAILURE;
                    if (cmdResp.requestSuccess()) {
                        ret_ = QUERY_RESULT_SUCCESS;
                    }
                    observer.onQueryResult(ret_);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR);
            }
        };
        executeCommand(command,handler);
    }
}
