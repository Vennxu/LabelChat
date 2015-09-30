package com.ekuater.labelchat.coreservice.tags;

import android.content.Context;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.tag.GetTagCommand;
import com.ekuater.labelchat.command.tag.SetTagCommand;
import com.ekuater.labelchat.coreservice.ICoreServiceCallback;
import com.ekuater.labelchat.coreservice.command.ICommandResponseHandler;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.datastruct.UserTag;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Leo on 2015/3/16.
 *
 * @author LinYong
 */
public class TagsStore {

    private static final String TAG = TagsStore.class.getSimpleName();

    private interface ListenerNotifier {
        public void notify(ITagsListener listener);
    }

    private final ICoreServiceCallback mCallback;
    private final SettingHelper mSettingHelper;
    private final List<WeakReference<ITagsListener>> mListeners
            = new CopyOnWriteArrayList<WeakReference<ITagsListener>>();
    private final Map<String, UserTag> mTagsMap = new HashMap<String, UserTag>();

    public TagsStore(Context context, ICoreServiceCallback callback) {
        mCallback = callback;
        mSettingHelper = SettingHelper.getInstance(context);
        getTagsFromLocal();
    }

    public void registerListener(final ITagsListener listener) {
        for (WeakReference<ITagsListener> ref : mListeners) {
            if (ref.get() == listener) {
                return;
            }
        }

        mListeners.add(new WeakReference<ITagsListener>(listener));
        unregisterListener(null);
    }

    public void unregisterListener(final ITagsListener listener) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            if (mListeners.get(i).get() == listener) {
                mListeners.remove(i);
            }
        }
    }

    public void sync() {
        GetTagCommand command = (GetTagCommand) preTreatCommand(new GetTagCommand());
        ICommandResponseHandler handler = new ICommandResponseHandler() {
            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                switch (result) {
                    case ConstantCode.EXECUTE_RESULT_SUCCESS:
                        try {
                            GetTagCommand.CommandResponse commandResponse
                                    = new GetTagCommand.CommandResponse(response);
                            if (commandResponse.requestSuccess()) {
                                updateTags(commandResponse.getTags());
                            }
                        } catch (JSONException e) {
                            L.e(TAG, e);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        executeCommand(command, handler);
    }

    public UserTag[] query() {
        synchronized (mTagsMap) {
            final Collection<UserTag> labels = mTagsMap.values();
            final int length = labels.size();
            return (length > 0) ? labels.toArray(new UserTag[length]) : null;
        }
    }

    public void setTags(final UserTag[] tags ){
        SetTagCommand command = (SetTagCommand) preTreatCommand(new SetTagCommand());
        int[] tagIds = getTagIds(tags);
        command.putParamTagIdArray(tagIds);
        ICommandResponseHandler handler = new ICommandResponseHandler() {
            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                int _ret = ConstantCode.TAG_OPERATION_NETWORK_ERROR;

                switch (result) {
                    case ConstantCode.EXECUTE_RESULT_SUCCESS:
                        try {
                            SetTagCommand.CommandResponse commandResponse
                                    = new SetTagCommand.CommandResponse(response);
                            if (commandResponse.requestSuccess()) {
                                updateTags(tags);
                                _ret = ConstantCode.TAG_OPERATION_SUCCESS;
                            } else {
                                _ret = ConstantCode.TAG_OPERATION_SYSTEM_ERROR;
                            }
                        } catch (JSONException e) {
                            L.e(TAG, e);
                            _ret = ConstantCode.TAG_OPERATION_RESPONSE_DATA_ERROR;
                        }
                        break;
                    default:
                        break;
                }
                notifySetTagResult(_ret);
            }
        };
        executeCommand(command, handler);
    }

    public void setTags(final UserTag[] tags, String[] userIds, String tagId) {
        SetTagCommand command = (SetTagCommand) preTreatCommand(new SetTagCommand());
        int[] tagIds = getTagIds(tags);
        command.putParamArrayUserId(Arrays.asList(userIds));
        command.putParamAddTagId(tagId);
        command.putParamTagIdArray(tagIds);
        ICommandResponseHandler handler = new ICommandResponseHandler() {
            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                int _ret = ConstantCode.TAG_OPERATION_NETWORK_ERROR;

                switch (result) {
                    case ConstantCode.EXECUTE_RESULT_SUCCESS:
                        try {
                            SetTagCommand.CommandResponse commandResponse
                                    = new SetTagCommand.CommandResponse(response);
                            if (commandResponse.requestSuccess()) {
                                updateTags(tags);
                                _ret = ConstantCode.TAG_OPERATION_SUCCESS;
                            } else {
                                _ret = ConstantCode.TAG_OPERATION_SYSTEM_ERROR;
                            }
                        } catch (JSONException e) {
                            L.e(TAG, e);
                            _ret = ConstantCode.TAG_OPERATION_RESPONSE_DATA_ERROR;
                        }
                        break;
                    default:
                        break;
                }
                notifySetTagResult(_ret);
            }
        };
        executeCommand(command, handler);
    }

    private int[] getTagIds(UserTag[] tags) {
        int[] tagIds;

        if (tags != null) {
            tagIds = new int[tags.length];
            for (int i = 0; i < tags.length; ++i) {
                tagIds[i] = tags[i].getTagId();
            }
        } else {
            tagIds = new int[0];
        }

        return tagIds;
    }

    public void clear() {
        updateTags(null);
    }

    private void executeCommand(RequestCommand command, ICommandResponseHandler handler) {
        mCallback.executeCommand(command, handler);
    }

    private void executeCommand(BaseCommand command, ICommandResponseHandler handler) {
        executeCommand(command.toRequestCommand(), handler);
    }

    private BaseCommand preTreatCommand(BaseCommand command) {
        return mCallback.preTreatCommand(command);
    }

    private void updateTags(UserTag[] tags) {
        synchronized (mTagsMap) {
            mTagsMap.clear();
            if (tags != null && tags.length > 0) {
                for (UserTag tag : tags) {
                    mTagsMap.put(tag.getTagName(), tag);
                }
            }
            saveTagsToLocal();
        }
        notifyTagUpdated();
    }

    private void getTagsFromLocal() {
        final UserTag[] tags = mSettingHelper.getAccountUserTags();
        synchronized (mTagsMap) {
            mTagsMap.clear();
            if (tags != null && tags.length > 0) {
                for (UserTag tag : tags) {
                    mTagsMap.put(tag.getTagName(), tag);
                }
            }
        }
    }

    private void saveTagsToLocal() {
        final Collection<UserTag> collection = mTagsMap.values();
        final int length = collection.size();
        final UserTag[] tags = (length > 0) ? collection.toArray(new UserTag[length]) : null;
        mSettingHelper.setAccountUserTags(tags);
    }

    private void notifyListeners(ListenerNotifier notifier) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            ITagsListener listener = mListeners.get(i).get();
            if (listener != null) {
                notifier.notify(listener);
            } else {
                mListeners.remove(i);
            }
        }
    }

    private void notifyTagUpdated() {
        notifyListeners(new ListenerNotifier() {
            @Override
            public void notify(ITagsListener listener) {
                listener.onTagUpdated();
            }
        });
    }

    private void notifySetTagResult(final int result) {
        notifyListeners(new ListenerNotifier() {
            @Override
            public void notify(ITagsListener listener) {
                listener.onSetTagResult(result);
            }
        });
    }
}
