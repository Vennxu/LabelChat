
package com.ekuater.labelchat.coreservice.labels;

import android.content.Context;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.command.labels.AddLabelCommand;
import com.ekuater.labelchat.command.labels.DelLabelCommand;
import com.ekuater.labelchat.command.labels.EnumLabelCommand;
import com.ekuater.labelchat.coreservice.ICoreServiceCallback;
import com.ekuater.labelchat.coreservice.command.ICommandResponseHandler;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.settings.SettingHelper;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Store current user labels
 *
 * @author LinYong
 */
public class LabelsStore {

    private final ICoreServiceCallback mCallback;
    private final List<WeakReference<ILabelsListener>> mListeners = new CopyOnWriteArrayList<WeakReference<ILabelsListener>>();
    private final Map<String, UserLabel> mLabelsMap = new HashMap<String, UserLabel>();
    private final SettingHelper mSettingHelper;

    public LabelsStore(Context context, ICoreServiceCallback callback) {
        mCallback = callback;
        mSettingHelper = SettingHelper.getInstance(context);
        getLabelsFromLocal();
    }

    public void registerListener(final ILabelsListener listener) {
        for (WeakReference<ILabelsListener> ref : mListeners) {
            if (ref.get() == listener) {
                return;
            }
        }

        mListeners.add(new WeakReference<ILabelsListener>(listener));
        unregisterListener(null);
    }

    public void unregisterListener(final ILabelsListener listener) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            if (mListeners.get(i).get() == listener) {
                mListeners.remove(i);
            }
        }
    }

    /**
     * When a new account login, synchronize its labels from server.
     */
    public void sync() {
        EnumLabelCommand command = (EnumLabelCommand) preTreatCommand(new EnumLabelCommand());
        ICommandResponseHandler handler = new ICommandResponseHandler() {

            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                switch (result) {
                    case ConstantCode.EXECUTE_RESULT_SUCCESS:
                        try {
                            EnumLabelCommand.CommandResponse commandResponse
                                    = new EnumLabelCommand.CommandResponse(response);
                            if (commandResponse.requestSuccess()) {
                                updateLabels(commandResponse.getUserLabels());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        executeCommand(command, handler);
    }

    private void updateLabels(UserLabel[] labels) {
        synchronized (mLabelsMap) {
            mLabelsMap.clear();
            if (labels != null && labels.length > 0) {
                for (UserLabel label : labels) {
                    mLabelsMap.put(label.getName(), label);
                }
            }
            saveLabelsToLocal();
        }
        notifyLabelsUpdated();
    }

    private void deleteLabels(UserLabel[] labels) {
        if (labels != null && labels.length > 0) {
            synchronized (mLabelsMap) {
                for (UserLabel label : labels) {
                    mLabelsMap.remove(label.getName());
                }
                saveLabelsToLocal();
            }
        }
    }

    private void notifyLabelsUpdated() {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            ILabelsListener listener = mListeners.get(i).get();
            if (listener != null) {
                listener.onLabelUpdated();
            } else {
                mListeners.remove(i);
            }
        }
    }

    public UserLabel[] query() {
        synchronized (mLabelsMap) {
            final Collection<UserLabel> labels = mLabelsMap.values();
            final int length = labels.size();
            return (length > 0) ? labels.toArray(new UserLabel[length]) : null;
        }
    }

    /**
     * delete a label from current account
     *
     * @param labels labels to be deleted
     */
    public void delete(final UserLabel[] labels) {
        if (labels == null || labels.length <= 0) {
            return;
        }

        DelLabelCommand command = (DelLabelCommand) preTreatCommand(new DelLabelCommand());
        command.putParamLabels(labels);
        ICommandResponseHandler handler = new ICommandResponseHandler() {

            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                int deleteResult = ConstantCode.LABEL_OPERATION_NETWORK_ERROR;

                switch (result) {
                    case ConstantCode.EXECUTE_RESULT_SUCCESS:
                        try {
                            DelLabelCommand.CommandResponse commandResponse
                                    = new DelLabelCommand.CommandResponse(response);
                            if (commandResponse.requestSuccess()) {
                                // delete labels from local.
                                deleteLabels(labels);
                                deleteResult = ConstantCode.LABEL_OPERATION_SUCCESS;
                            } else if (commandResponse.getErrorCode() == CommandErrorCode.DATA_IN_USE) {
                                deleteResult = ConstantCode.LABEL_OPERATION_LABEL_IN_USE;
                            } else {
                                deleteResult = ConstantCode.LABEL_OPERATION_SYSTEM_ERROR;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            deleteResult = ConstantCode.LABEL_OPERATION_RESPONSE_DATA_ERROR;
                        }
                        break;
                    default:
                        break;
                }
                notifyDeleteLabelsResult(deleteResult);
            }
        };
        executeCommand(command, handler);
    }

    private void notifyDeleteLabelsResult(int result) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            ILabelsListener listener = mListeners.get(i).get();
            if (listener != null) {
                listener.onLabelDeleted(result);
            } else {
                mListeners.remove(i);
            }
        }
    }

    /**
     * Add new label to current account.
     *
     * @param labels new labels to be added
     */
    public void add(BaseLabel[] labels) {
        if (labels == null || labels.length <= 0) {
            return;
        }

        AddLabelCommand command = (AddLabelCommand) preTreatCommand(new AddLabelCommand());
        command.putParamLabels(labels);
        ICommandResponseHandler handler = new ICommandResponseHandler() {

            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                int addResult = ConstantCode.LABEL_OPERATION_NETWORK_ERROR;

                switch (result) {
                    case ConstantCode.EXECUTE_RESULT_SUCCESS:
                        try {
                            AddLabelCommand.CommandResponse commandResponse = new AddLabelCommand.CommandResponse(response);
                            if (commandResponse.requestSuccess()) {
                                // need synchronize labels from server.
                                sync();
                                addResult = ConstantCode.LABEL_OPERATION_SUCCESS;
                            } else {
                                addResult = ConstantCode.LABEL_OPERATION_SYSTEM_ERROR;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            addResult = ConstantCode.LABEL_OPERATION_RESPONSE_DATA_ERROR;
                        }
                        break;
                    default:
                        break;
                }
                notifyAddLabelsResult(addResult);
            }
        };
        executeCommand(command, handler);
    }

    private void notifyAddLabelsResult(int result) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            ILabelsListener listener = mListeners.get(i).get();
            if (listener != null) {
                listener.onLabelAdded(result);
            } else {
                mListeners.remove(i);
            }
        }
    }

    /**
     * When current user account has been changed, clear all labels in local.
     */
    public void clear() {
        updateLabels(null);
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

    private void getLabelsFromLocal() {
        final UserLabel[] labels = mSettingHelper.getAccountUserLabels();
        synchronized (mLabelsMap) {
            mLabelsMap.clear();
            if (labels != null && labels.length > 0) {
                for (UserLabel label : labels) {
                    mLabelsMap.put(label.getName(), label);
                }
            }
        }
    }

    private void saveLabelsToLocal() {
        final Collection<UserLabel> labelCollection = mLabelsMap.values();
        final int length = labelCollection.size();
        final UserLabel[] labels = (length > 0)
                ? labelCollection.toArray(new UserLabel[length]) : null;
        mSettingHelper.setAccountUserLabels(labels);
    }
}
