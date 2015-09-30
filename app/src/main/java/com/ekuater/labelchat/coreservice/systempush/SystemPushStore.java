
package com.ekuater.labelchat.coreservice.systempush;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.ekuater.labelchat.coreservice.EventBusHub;
import com.ekuater.labelchat.coreservice.event.NewSystemPushEvent;
import com.ekuater.labelchat.data.DataConstants;
import com.ekuater.labelchat.data.DataConstants.Push;
import com.ekuater.labelchat.datastruct.ConfideMessage;
import com.ekuater.labelchat.datastruct.DynamicOperateMessage;
import com.ekuater.labelchat.datastruct.DynamicRemaindMessage;
import com.ekuater.labelchat.datastruct.InterestMessage;
import com.ekuater.labelchat.datastruct.PhotoNotifyMessage;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.util.InterestUtils;
import com.ekuater.labelchat.util.L;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * @author LinYong
 */
public final class SystemPushStore {

    private static final String TAG = SystemPushStore.class.getSimpleName();
    private static final Uri PUSH_URI = Push.CONTENT_URI;

    private interface ISystemPushListenerNotifier {

        void notify(ISystemPushListener listener);
    }

    private final Context mContext;
    private final EventBus mCoreEventBus;
    private final List<WeakReference<ISystemPushListener>> mListeners = new ArrayList<>();

    public SystemPushStore(Context context) {
        mContext = context;
        mCoreEventBus = EventBusHub.getCoreEventBus();
    }

    public void init() {
        mCoreEventBus.register(this);
    }

    public void deInit() {
        mCoreEventBus.unregister(this);
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(NewSystemPushEvent pushEvent) {
        onNewSystemPushReceived(pushEvent.getSystemPush());
    }

    public synchronized void registerListener(final ISystemPushListener listener) {
        for (WeakReference<ISystemPushListener> ref : mListeners) {
            if (ref.get() == listener) {
                return;
            }
        }

        mListeners.add(new WeakReference<>(listener));
        unregisterListener(null);
    }

    public synchronized void unregisterListener(final ISystemPushListener listener) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            if (mListeners.get(i).get() == listener) {
                mListeners.remove(i);
            }
        }
    }

    private void onNewSystemPushReceived(SystemPush systemPush) {
        if (systemPush == null) {
            return;
        }

        boolean needNotify = true;

        switch (systemPush.getType()) {
            case SystemPushType.TYPE_ADD_FRIEND:
            case SystemPushType.TYPE_FRIEND_INFO_UPDATED:
            case SystemPushType.TYPE_ADD_FRIEND_AGREE_RESULT:
            case SystemPushType.TYPE_DEFRIEND_NOTIFICATION:
                notifyNewContactPushReceived(systemPush);
                break;
            case SystemPushType.TYPE_LOGIN_ON_OTHER_CLIENT:
                notifyNewAccountPushReceived(systemPush);
                break;
            case SystemPushType.TYPE_TMP_GROUP_CREATE:
            case SystemPushType.TYPE_TMP_GROUP_DISMISS:
            case SystemPushType.TYPE_TMP_GROUP_MEMBER_QUIT:
            case SystemPushType.TYPE_TMP_GROUP_DISMISS_REMIND:
                notifyNewTmpGroupPushReceived(systemPush);
                break;
            case SystemPushType.TYPE_PHOTO_NOTIFY: {
                PhotoNotifyMessage message = PhotoNotifyMessage.build(systemPush);
                systemPush.setFlag(message.getNotifyType());
                storeSystemPush(systemPush);
                break;
            }
            case SystemPushType.TYPE_LABEL_STORY_COMMENTS: {
                DynamicOperateMessage message = DynamicOperateMessage.build(systemPush);
                systemPush.setFlag(message.getOperateType());
                storeSystemPush(systemPush);
                break;
            }
            case SystemPushType.TYPE_CONFIDE_COMMEND: {
                ConfideMessage confideMessage = ConfideMessage.build(systemPush);
                systemPush.setFlag(confideMessage.getOperateType());
                storeSystemPush(systemPush);
                break;
            }
            case SystemPushType.TYPE_REMAIND_INTEREST: {
                InterestMessage message = InterestMessage.build(systemPush);
                needNotify = false;
                if (message != null && message.getUserInterest() != null) {
                    if (InterestUtils.isInterestTypeSupported(message.getUserInterest()
                            .getInterestType())) {
                        storeSystemPush(systemPush);
                        needNotify = true;
                    }
                }
                break;
            }
            case SystemPushType.TYPE_REMAIND_DYNAMIC: {
                DynamicRemaindMessage message = DynamicRemaindMessage.build(systemPush);
                if (message != null) {
                    storeSystemPush(systemPush);
                } else {
                    needNotify = false;
                }
                break;
            }
            default:
                preProcessNewSystemPush(systemPush);
                storeSystemPush(systemPush);
                break;
        }

        if (needNotify) {
            notifyNewMessageReceived(systemPush);
        }
    }

    public synchronized void deleteSystemPushByType(int type) {
        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Push.CONTENT_URI;
        final String where = DataConstants.Push.TYPE + "=?";
        final String[] selectionArgs = {
                String.valueOf(type),
        };
        cr.delete(uri, where, selectionArgs);
    }

    public synchronized void deleteLikeSystemPushByType(int type, String tag) {
        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Push.CONTENT_URI;
        final String where = Push.CONTENT + " like" + " '%" + tag + "%'" + " and " + Push.TYPE + "=?";
        final String[] selectionArgs = {
                String.valueOf(type),
        };
        cr.delete(uri, where, selectionArgs);
    }

    public synchronized void deleteSystemPushByTypes(int[] types) {
        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Push.CONTENT_URI;
        StringBuilder sb = new StringBuilder(" in (");
        final String[] selectionArgs = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            if (i == types.length - 1) {
                sb.append("?)");
            } else {
                sb.append("?,");
            }
            selectionArgs[i] = String.valueOf(types[i]);
        }
        final String where = DataConstants.Push.TYPE + sb;
        cr.delete(uri, where, selectionArgs);
    }

    public synchronized void deleteSystemPushByFlag(String flag) {
        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Push.CONTENT_URI;
        final String where = DataConstants.Push.FLAGS + "=?";
        final String[] selectionArgs = {
                flag,
        };

        cr.delete(uri, where, selectionArgs);
    }

    public synchronized void deleteSystemPush(long messageId) {
        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = ContentUris.withAppendedId(DataConstants.Push.CONTENT_URI, messageId);
        cr.delete(uri, null, null);
    }

    public void clear() {
        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Push.CONTENT_URI;
        cr.delete(uri, null, null);
    }

    private synchronized void notifySystemPushListeners(ISystemPushListenerNotifier notifier) {
        final List<WeakReference<ISystemPushListener>> listeners = mListeners;

        for (int i = listeners.size() - 1; i >= 0; i--) {
            ISystemPushListener listener = listeners.get(i).get();
            if (listener != null) {
                try {
                    notifier.notify(listener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                listeners.remove(i);
            }
        }
    }

    private void notifyNewMessageReceived(SystemPush systemPush) {
        notifySystemPushListeners(new NewSystemPushReceivedNotifier(systemPush));
    }

    private void notifyNewAccountPushReceived(SystemPush systemPush) {
        notifySystemPushListeners(new NewAccountPushReceivedNotifier(systemPush));
    }

    private void notifyNewContactPushReceived(SystemPush systemPush) {
        notifySystemPushListeners(new NewContactPushReceivedNotifier(systemPush));
    }

    private void notifyNewTmpGroupPushReceived(SystemPush systemPush) {
        notifySystemPushListeners(new NewTmpGroupPushReceivedNotifier(systemPush));
    }

    private void preProcessNewSystemPush(SystemPush systemPush) {
        final int type = systemPush.getType();

        switch (type) {
            case SystemPushType.TYPE_CONFIRM_WEEKLY_STAR:
            case SystemPushType.TYPE_WEEKLY_STAR:
            case SystemPushType.TYPE_BUBBLE_UP:
            case SystemPushType.TYPE_TODAY_RECOMMENDED:
            case SystemPushType.TYPE_WEEKLY_HOT_LABEL:
            case SystemPushType.TYPE_REGISTER_WELCOME:
                deleteSystemPushByType(type);
                break;
            default:
                break;
        }

        switch (type) {
            case SystemPushType.TYPE_REGISTER_WELCOME: {
                SettingHelper.getInstance(mContext).setAccountNewFeedback(true);
                break;
            }
            default:
                break;
        }
    }

    private void storeSystemPush(SystemPush systemPush) {
        ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Push.TYPE, systemPush.getType());
        values.put(Push.DATETIME, systemPush.getTime());
        values.put(Push.STATE, systemPush.getState());
        values.put(Push.CONTENT, systemPush.getContent());
        values.put(Push.FLAGS, systemPush.getFlag());

        Uri uri = cr.insert(PUSH_URI, values);
        long id = ContentUris.parseId(uri);

        L.v(TAG, "storeSystemPush(), uri=%1$s, id=%2$d", uri.toString(), id);

        if (id != -1L) {
            systemPush.setId(id);
        }
    }

    private static class NewSystemPushReceivedNotifier implements ISystemPushListenerNotifier {

        private final SystemPush mSystemPush;

        public NewSystemPushReceivedNotifier(SystemPush systemPush) {
            mSystemPush = systemPush;
        }

        @Override
        public void notify(ISystemPushListener listener) {
            listener.onNewSystemPushReceived(mSystemPush);
        }
    }

    private static class NewAccountPushReceivedNotifier implements ISystemPushListenerNotifier {

        private final SystemPush mSystemPush;

        public NewAccountPushReceivedNotifier(SystemPush systemPush) {
            mSystemPush = systemPush;
        }

        @Override
        public void notify(ISystemPushListener listener) {
            listener.onNewAccountPushReceived(mSystemPush);
        }
    }

    private static class NewContactPushReceivedNotifier implements ISystemPushListenerNotifier {

        private final SystemPush mSystemPush;

        public NewContactPushReceivedNotifier(SystemPush systemPush) {
            mSystemPush = systemPush;
        }

        @Override
        public void notify(ISystemPushListener listener) {
            listener.onNewContactPushReceived(mSystemPush);
        }
    }

    private static class NewTmpGroupPushReceivedNotifier implements ISystemPushListenerNotifier {

        private final SystemPush mSystemPush;

        public NewTmpGroupPushReceivedNotifier(SystemPush systemPush) {
            mSystemPush = systemPush;
        }

        @Override
        public void notify(ISystemPushListener listener) {
            listener.onNewTmpGroupPushReceived(mSystemPush);
        }
    }
}
