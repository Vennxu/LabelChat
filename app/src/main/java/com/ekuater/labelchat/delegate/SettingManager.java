package com.ekuater.labelchat.delegate;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.content.Context;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.setting.ChatSetCommand;
import com.ekuater.labelchat.command.setting.LabelSetCommand;
import com.ekuater.labelchat.command.setting.PrivacySetCommand;
import com.ekuater.labelchat.command.setting.UserInfoSetCommand;
import com.ekuater.labelchat.util.L;

/**
 * @author LinYong
 */
public class SettingManager extends BaseManager {

    private static final String TAG = SettingManager.class.getSimpleName();

    // Query result enum
    public static final int QUERY_RESULT_SUCCESS = 0;
    public static final int QUERY_RESULT_EMPTY_ARGUMENTS = 1;
    public static final int QUERY_RESULT_ILLEGAL_ARGUMENTS = 2;
    public static final int QUERY_RESULT_QUERY_FAILURE = 3;
    public static final int QUERY_RESULT_RESPONSE_DATA_ERROR = 4;

    public interface IListener extends BaseManager.IListener {

        /**
         * notify the user labels has been updated.
         */
        public void onLabelUpdated();

        /**
         * notify the label add operation result.
         */
        public void onLabelAdded(int result);

        /**
         * notify the label delete operation result.
         */
        public void onLabelDeleted(int result);
    }

    public static class AbsListener implements IListener {

        @Override
        public void onLabelUpdated() {
        }

        @Override
        public void onLabelAdded(int result) {
        }

        @Override
        public void onLabelDeleted(int result) {
        }

        @Override
        public void onCoreServiceConnected() {
        }

        @Override
        public void onCoreServiceDied() {
        }
    }

    private static SettingManager sInstance;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SettingManager(context.getApplicationContext());
        }
    }

    public static SettingManager getInstance(Context context) {
        if (sInstance == null) {
            initInstance(context);
        }
        return sInstance;
    }

    private final AccountManager mAccountManager;

    private final MiscManager mismana;
    private final List<WeakReference<IListener>> mListeners = new ArrayList<WeakReference<IListener>>();
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

    };

    private SettingManager(Context context) {
        super(context);
        mAccountManager = AccountManager.getInstance(context);
        mCoreService.registerNotifier(mNotifier);
        mismana = MiscManager.getInstance(context);
    }

    public void registerListener(IListener listener) {
        synchronized (mListeners) {
            for (WeakReference<IListener> ref : mListeners) {
                if (ref.get() == listener) {
                    return;
                }
            }

            mListeners.add(new WeakReference<IListener>(listener));
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

    public void updatePrivacyInfo(String keyword,String setValue) {
        PrivacySetCommand command = new PrivacySetCommand(getSession());
        command.putParamPrivacySet(keyword,setValue);
        command.putParamUserId(getUserId());
        ICommandResponseHandler handler = new ICommandResponseHandler() {
            @Override
            public void onResponse(int result, String response) {
            	try {
					PrivacySetCommand.CommandResponse cmp = new PrivacySetCommand.CommandResponse(response);
					L.v("SettingManager", "updatePrivacyInfo", cmp.toString());
				} catch (JSONException e) {
                    L.w(TAG, e);
				}
            }
        };
        executeCommand(command, handler);
    }

    public void updateChatSetInfo(String keyword,String setValue) {
    	ChatSetCommand command = new ChatSetCommand(getSession());
        command.putParamChatSet(keyword,setValue);
        command.putParamUserId(getUserId());
        ICommandResponseHandler handler = new ICommandResponseHandler() {
            @Override
            public void onResponse(int result, String response) {
            	try {
            		ChatSetCommand.CommandResponse cmp = new ChatSetCommand.CommandResponse(response);
					L.v("SettingManager", "updateChatSetInfo", cmp.toString());
				} catch (JSONException e) {
                    L.w(TAG, e);
				}
            }
        };
        executeCommand(command, handler);
    }
    
    public void updateLabelSetInfo(String keyword,String setValue) {
    	LabelSetCommand command = new LabelSetCommand(getSession());
        command.putParamLabelSet(keyword,setValue);
        command.putParamUserId(getUserId());
        ICommandResponseHandler handler = new ICommandResponseHandler() {
            @Override
            public void onResponse(int result, String response) {
            	try {
            		LabelSetCommand.CommandResponse cmp = new LabelSetCommand.CommandResponse(response);
					L.v("SettingManager", "updateLabelSetInfo", cmp.toString());
				} catch (JSONException e) {
                    L.w(TAG, e);
				}
            }
        };
        executeCommand(command, handler);
    }
    
    public void updateUserInfoSet(String keyword,String setValue) {
        UserInfoSetCommand command = new UserInfoSetCommand(getSession());
        command.putParamUserInfoSet(keyword,setValue);
        command.putParamUserId(getUserId());
        ICommandResponseHandler handler = new ICommandResponseHandler() {
            @Override
            public void onResponse(int result, String response) {
            	try {
            		UserInfoSetCommand.CommandResponse cmp = new UserInfoSetCommand.CommandResponse(response);
					L.v("SettingManager", "updateUserInfoSet", cmp.toString());
				} catch (JSONException e) {
                    L.w(TAG, e);
				}
            }
        };
        executeCommand(command, handler);
    }
}
