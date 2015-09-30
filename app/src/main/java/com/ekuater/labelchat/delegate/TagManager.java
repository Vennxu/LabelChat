package com.ekuater.labelchat.delegate;

import android.content.Context;

import com.ekuater.labelchat.command.tag.GetTagCommand;
import com.ekuater.labelchat.command.tag.ListTagTypeCommand;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.TagType;
import com.ekuater.labelchat.datastruct.UserTag;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/3/15.
 *
 * @author LinYong
 */
public class TagManager extends BaseManager {

    private static final String TAG = TagManager.class.getSimpleName();

    public static final int QUERY_RESULT_SUCCESS = 0;
    public static final int QUERY_RESULT_ILLEGAL_ARGUMENTS = 1;
    public static final int QUERY_RESULT_QUERY_FAILURE = 2;
    public static final int QUERY_RESULT_RESPONSE_DATA_ERROR = 3;

    public interface IListener extends BaseManager.IListener {
        public void onUserTagUpdated();

        public void onSetUserTagResult(int result);
    }

    public static class AbsListener implements IListener{

        @Override
        public void onUserTagUpdated() {

        }

        @Override
        public void onSetUserTagResult(int result) {

        }

        @Override
        public void onCoreServiceConnected() {

        }

        @Override
        public void onCoreServiceDied() {

        }
    }

    private interface ListenerNotifier {
        public void notify(IListener listener);
    }

    public interface TagTypeObserver {
        public void onQueryResult(int result, TagType[] tagTypes);
    }

    public interface UserTagObserver {
        public void onQueryResult(int result, UserTag[] userTags);
    }

    private static TagManager sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new TagManager(context.getApplicationContext());
        }
    }

    public static TagManager getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private final List<WeakReference<IListener>> mListeners
            = new ArrayList<>();
    private final ICoreServiceNotifier mNotifier = new AbstractCoreServiceNotifier() {
        @Override
        public void onUserTagUpdated() {
            notifyListeners(new ListenerNotifier() {
                @Override
                public void notify(IListener listener) {
                    listener.onUserTagUpdated();
                }
            });
        }

        @Override
        public void onSetUserTagResult(final int result) {
            notifyListeners(new ListenerNotifier() {
                @Override
                public void notify(IListener listener) {
                    listener.onSetUserTagResult(result);
                }
            });
        }
    };

    private TagManager(Context context) {
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

    public void getTagTypes(TagTypeObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        ListTagTypeCommand command = new ListTagTypeCommand();
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (!(mObj instanceof TagTypeObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                TagTypeObserver observer = (TagTypeObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    ListTagTypeCommand.CommandResponse cmdResp
                            = new ListTagTypeCommand.CommandResponse(response);
                    TagType[] tagTypes = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        tagTypes = cmdResp.getTagTypes();
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, tagTypes);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }


    public void getUserTag(String queryUserId, UserTagObserver observer) {
        if (observer == null) {
            return;
        }
        GetTagCommand command = new GetTagCommand(getSession(), getUserId());
        command.putParamQueryUserId(queryUserId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (!(mObj instanceof UserTagObserver)) {
                    return;
                }
                UserTagObserver observer = (UserTagObserver) mObj;
                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null);
                    return;
                }
                try {
                    GetTagCommand.CommandResponse cmdResp = new GetTagCommand.CommandResponse(response);
                    UserTag[] userTags = null;
                    int ret_ = QUERY_RESULT_QUERY_FAILURE;
                    if (cmdResp.requestSuccess()) {
                        userTags = cmdResp.getTags();
                        ret_ = QUERY_RESULT_SUCCESS;
                    }
                    observer.onQueryResult(ret_, userTags);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public UserTag[] getUserTags() {
        return mCoreService.tagGetUserTags();
    }

    public void setUserTags(UserTag[] tags, String[] userIds, String tagId) {
        mCoreService.tagSetUserTags(tags, userIds, tagId);
    }

    private void notifyListeners(ListenerNotifier notifier) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            IListener listener = mListeners.get(i).get();
            if (listener != null) {
                notifier.notify(listener);
            } else {
                mListeners.remove(i);
            }
        }
    }
}
