package com.ekuater.labelchat.delegate;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.ekuater.labelchat.data.DataConstants;
import com.ekuater.labelchat.datastruct.ConfideMessage;
import com.ekuater.labelchat.datastruct.DynamicOperateMessage;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.ui.fragment.labelstory.NewMessageHint;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LinYong
 */
public class PushMessageManager extends BaseManager {

    public interface IListener extends BaseManager.IListener {
        public void onPushMessageDataChanged();

        public void onNewSystemPushReceived(SystemPush systemPush);
    }

    public static class AbsListener implements IListener {

        @Override
        public void onCoreServiceConnected() {
        }

        @Override
        public void onCoreServiceDied() {
        }

        @Override
        public void onPushMessageDataChanged() {
        }

        @Override
        public void onNewSystemPushReceived(SystemPush systemPush) {
        }
    }

    public interface FliterType {
        public boolean accept(int target, SystemPush push);
    }

    private static class ColumnsMap {

        public final int mId;
        public final int mType;
        public final int mState;
        public final int mContent;
        public final int mTime;
        public final int mFlags;


        private static int getColumnIndex(Cursor cursor, String columnName) {
            return cursor.getColumnIndex(columnName);
        }

        public ColumnsMap(Cursor cursor) {
            mId = getColumnIndex(cursor, DataConstants.Push._ID);
            mType = getColumnIndex(cursor, DataConstants.Push.TYPE);
            mState = getColumnIndex(cursor, DataConstants.Push.STATE);
            mContent = getColumnIndex(cursor, DataConstants.Push.CONTENT);
            mTime = getColumnIndex(cursor, DataConstants.Push.DATETIME);
            mFlags = getColumnIndex(cursor, DataConstants.Push.FLAGS);
        }
    }

    private static PushMessageManager sInstance;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PushMessageManager(context.getApplicationContext());
        }
    }

    public static PushMessageManager getInstance(Context context) {
        if (sInstance == null) {
            initInstance(context);
        }
        return sInstance;
    }

    private final Context mContext;
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
        public void onNewSystemPushReceived(SystemPush systemPush) {
            notifyNewSystemPushReceived(systemPush);
        }
    };

    private final ContentObserver mContentObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            notifyPushMessageDataChanged();
        }
    };

    private PushMessageManager(Context context) {
        super(context);
        mContext = context;
        mCoreService.registerNotifier(mNotifier);
        mContext.getContentResolver().registerContentObserver(DataConstants.Push.CONTENT_URI,
                true, mContentObserver);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mCoreService.unregisterNotifier(mNotifier);
        mContext.getContentResolver().unregisterContentObserver(mContentObserver);
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

    public void deletePushMessageByType(int type) {
        if (isInGuestMode()) {
            return;
        }
        mCoreService.deleteSystemPushByType(type);
    }

    public void deleteLikeSystemPushByType(int type, String tag) {
        if (isInGuestMode()) {
            return;
        }
        mCoreService.deleteLikeSystemPushByType(type, tag);
    }

    public void deletePushMessageByTypes(int[] types) {
        if (isInGuestMode()) {
            return;
        }
        mCoreService.deleteSystemPushByTypes(types);
    }

    public void deletePushMessageByFlag(String flag) {
        if (isInGuestMode()) {
            return;
        }
        mCoreService.deletePushMessageByFlag(flag);
    }

    public void deletePushMessage(long messageId) {
        if (isInGuestMode()) {
            return;
        }
        mCoreService.deleteSystemPush(messageId);
    }

    public SystemPush[] getEveryTypeLastPushMessage() {
        if (isInGuestMode()) {
            return null;
        }

        final String lastTime = "last_time";
        final ContentResolver cr = mContext.getContentResolver();
        final int length = DataConstants.Push.ALL_COLUMNS.length;
        final String[] projection = new String[length + 1];
        final String selection = DataConstants.Push.TYPE + ">0"
                + ") GROUP BY (" + DataConstants.Push.TYPE;
        //final String orderBy = lastTime + " DESC";
        SystemPush[] pushMessages = null;

        System.arraycopy(DataConstants.Push.ALL_COLUMNS, 0, projection, 0, length);
        projection[length] = "MAX(" + DataConstants.Push.DATETIME + ") AS " + lastTime;

        final Cursor cursor = cr.query(DataConstants.Push.CONTENT_URI, projection,
                selection, null, null/*orderBy*/);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                final SystemPush[] tmpPushMessages = new SystemPush[cursor.getCount()];
                final ColumnsMap columnsMap = new ColumnsMap(cursor);
                int idx = 0;

                cursor.moveToFirst();
                do {
                    tmpPushMessages[idx++] = buildPushMessage(cursor, columnsMap);
                } while (cursor.moveToNext());

                pushMessages = tmpPushMessages;
            }
            cursor.close();
        }
        return pushMessages;
    }

    public ArrayList<SystemPush> getEveryTypeLastPushMessages(int target, FliterType fliterType) {
        if (isInGuestMode()) {
            return null;
        }

        final String lastTime = "last_time";
        final ContentResolver cr = mContext.getContentResolver();
        final int length = DataConstants.Push.ALL_COLUMNS.length;
        final String[] projection = new String[length + 1];
        final String selection = DataConstants.Push.TYPE + ">0"
                + ") GROUP BY (" + DataConstants.Push.TYPE;
        //final String orderBy = lastTime + " DESC";
        ArrayList<SystemPush> pushMessages = null;

        System.arraycopy(DataConstants.Push.ALL_COLUMNS, 0, projection, 0, length);
        projection[length] = "MAX(" + DataConstants.Push.DATETIME + ") AS " + lastTime;

        final Cursor cursor = cr.query(DataConstants.Push.CONTENT_URI, projection,
                selection, null, null/*orderBy*/);

        if (cursor.getCount() > 0) {
            pushMessages = new ArrayList<>();
            final ColumnsMap columnsMap = new ColumnsMap(cursor);
            cursor.moveToFirst();
            do {
                SystemPush systemPush = buildPushMessage(cursor, columnsMap);
                if (fliterType.accept(target, systemPush)) {
                    pushMessages.add(systemPush);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pushMessages;
    }

    public SystemPush[] getEveryFlagLastPushMessage() {
        if (isInGuestMode()) {
            return null;
        }

        final String lastTime = "last_time";
        final ContentResolver cr = mContext.getContentResolver();
        final int length = DataConstants.Push.ALL_COLUMNS.length;
        final String[] projection = new String[length + 1];
        final String selection = DataConstants.Push.FLAGS
                + ") GROUP BY (" + DataConstants.Push.FLAGS;
        //final String orderBy = lastTime + " DESC";
        SystemPush[] pushMessages = null;

        System.arraycopy(DataConstants.Push.ALL_COLUMNS, 0, projection, 0, length);
        projection[length] = "MAX(" + DataConstants.Push.DATETIME + ") AS " + lastTime;

        final Cursor cursor = cr.query(DataConstants.Push.CONTENT_URI, projection,
                selection, null, null/*orderBy*/);

        if (cursor.getCount() > 0) {
            final SystemPush[] tmpPushMessages = new SystemPush[cursor.getCount()];
            final ColumnsMap columnsMap = new ColumnsMap(cursor);
            int idx = 0;

            cursor.moveToFirst();
            do {
                tmpPushMessages[idx++] = buildPushMessage(cursor, columnsMap);
            } while (cursor.moveToNext());

            pushMessages = tmpPushMessages;
        }
        cursor.close();

        return pushMessages;
    }

    public SystemPush[] getPushMessagesByFlags(String flags) {
        if (isInGuestMode()) {
            return null;
        }

        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Push.CONTENT_URI;
        final String[] projection = DataConstants.Push.ALL_COLUMNS;
        final String selection = DataConstants.Push.FLAGS + "=?";
        final String[] selectionArgs = new String[]{flags};
        final String orderBy = DataConstants.Push.DATETIME + " ASC";
        final Cursor cursor = cr.query(uri, projection, selection, selectionArgs, orderBy);
        SystemPush[] pushMessages = null;

        if (cursor.getCount() > 0) {
            final SystemPush[] tmpPushMessages = new SystemPush[cursor.getCount()];
            final ColumnsMap columnsMap = new ColumnsMap(cursor);
            int idx = 0;

            cursor.moveToFirst();
            do {
                tmpPushMessages[idx++] = buildPushMessage(cursor, columnsMap);
            } while (cursor.moveToNext());

            pushMessages = tmpPushMessages;
        }
        cursor.close();

        return pushMessages;
    }

    public void insertPushMessage(SystemPush systemPush) {
        ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();

        values.put(DataConstants.Push.TYPE, systemPush.getType());
        values.put(DataConstants.Push.DATETIME, systemPush.getTime());
        values.put(DataConstants.Push.STATE, systemPush.getState());
        values.put(DataConstants.Push.CONTENT, systemPush.getContent());
        values.put(DataConstants.Push.FLAGS, systemPush.getFlag());

        Uri uri = cr.insert(DataConstants.Push.CONTENT_URI, values);
        long id = ContentUris.parseId(uri);

        if (id != -1L) {
            systemPush.setId(id);
        }
    }

    public SystemPush[] getPushMessagesByType(int type) {
        if (isInGuestMode()) {
            return null;
        }

        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Push.CONTENT_URI;
        final String[] projection = DataConstants.Push.ALL_COLUMNS;
        final String selection = DataConstants.Push.TYPE + "=?";
        final String[] selectionArgs = new String[]{
                String.valueOf(type),
        };
        final String orderBy = DataConstants.Push.DATETIME + " DESC";
        final Cursor cursor = cr.query(uri, projection, selection, selectionArgs, orderBy);
        SystemPush[] pushMessages = null;

        if (cursor.getCount() > 0) {
            final SystemPush[] tmpPushMessages = new SystemPush[cursor.getCount()];
            final ColumnsMap columnsMap = new ColumnsMap(cursor);
            int idx = 0;

            cursor.moveToFirst();
            do {
                tmpPushMessages[idx++] = buildPushMessage(cursor, columnsMap);
            } while (cursor.moveToNext());

            pushMessages = tmpPushMessages;
        }
        cursor.close();

        return pushMessages;
    }

    public interface TypeFliter {
        public boolean accept();
    }

    public ArrayList<SystemPush> getPushMessagesFliterType(int[] type, int target, FliterType fliterType) {
        if (isInGuestMode()) {
            return null;
        }
        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Push.CONTENT_URI;
        final String[] projection = DataConstants.Push.ALL_COLUMNS;
        StringBuffer selectType = new StringBuffer(" in(");
        final String[] selectionArgs = new String[type.length];
        for (int i = 0; i < type.length; i++) {
            if (i == (type.length - 1)) {
                selectType.append("?)");
            } else {
                selectType.append("?,");
            }
            selectionArgs[i] = String.valueOf(type[i]);
        }
        Log.d("select", selectType.toString());
        final String selection = DataConstants.Push.TYPE + selectType.toString();
        final String orderBy = DataConstants.Push.DATETIME + " DESC";
        final Cursor cursor = cr.query(uri, projection, selection, selectionArgs, orderBy);
        ArrayList<SystemPush> pushMessages = null;

        if (cursor.getCount() > 0) {
            final ArrayList<SystemPush> tmpPushMessages = new ArrayList<>();
            final ColumnsMap columnsMap = new ColumnsMap(cursor);
            cursor.moveToFirst();
            do {
                SystemPush systemPush = buildPushMessage(cursor, columnsMap);
                if (fliterType.accept(target, systemPush)) {
                    tmpPushMessages.add(systemPush);
                }
            } while (cursor.moveToNext());
            pushMessages = tmpPushMessages;
        }
        cursor.close();

        return pushMessages;
    }

    public SystemPush getLastPushMessagesFliterType(int[] type, int target, FliterType fliterType) {
        if (isInGuestMode()) {
            return null;
        }
        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Push.CONTENT_URI;
        final String[] projection = DataConstants.Push.ALL_COLUMNS;
        StringBuffer selectType = new StringBuffer(" in(");
        final String[] selectionArgs = new String[type.length];
        for (int i = 0; i < type.length; i++) {
            if (i == (type.length - 1)) {
                selectType.append("?)");
            } else {
                selectType.append("?,");
            }
            selectionArgs[i] = String.valueOf(type[i]);
        }
        Log.d("select", selectType.toString());
        final String selection = DataConstants.Push.TYPE + selectType.toString();
        final String orderBy = DataConstants.Push.DATETIME + " DESC";
        final Cursor cursor = cr.query(uri, projection, selection, selectionArgs, orderBy);
        SystemPush pushMessages = null;
        if (cursor.getCount() > 0) {
            final ColumnsMap columnsMap = new ColumnsMap(cursor);
            cursor.moveToFirst();
            do {
                SystemPush systemPush = buildPushMessage(cursor, columnsMap);
                if (fliterType.accept(target, systemPush)) {
                    pushMessages = systemPush;
                    break;
                }
            } while (cursor.moveToNext());

        }
        cursor.close();
        return pushMessages;
    }

    public int getUnprocessedPushMessageCount(int[] type, int target, FliterType fliterType) {
        if (isInGuestMode()) {
            return 0;
        }

        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Push.CONTENT_URI;
        final String[] projection = DataConstants.Push.ALL_COLUMNS;
        StringBuffer selectType = new StringBuffer(" in(");
        final String[] selectionArgs = new String[type.length+1];
        for (int i = 0; i < type.length+1; i++) {
            if (i < type.length) {
                if (i == (type.length - 1)) {
                    selectType.append("?)");
                } else {
                    selectType.append("?,");
                }
            }
            if (i == type.length) {
                selectionArgs[i] = String.valueOf(SystemPush.STATE_UNPROCESSED);
            }else{
                selectionArgs[i] = String.valueOf(type[i]);
            }

        }
        final String selection = DataConstants.Push.TYPE + selectType.toString() + " AND "
                + DataConstants.Push.STATE + "=?";

        Log.d("select", selectType.toString());
//        final String[] selectionArgs = new String[]{
//                String.valueOf(type),
//                String.valueOf(SystemPush.STATE_UNPROCESSED),
//        };
        final Cursor cursor = cr.query(uri, projection, selection, selectionArgs, null);
        int count = 0;
        if (cursor.getCount() > 0) {
            final ArrayList<SystemPush> tmpPushMessages = new ArrayList<>();
            final ColumnsMap columnsMap = new ColumnsMap(cursor);
            cursor.moveToFirst();
            do {
                SystemPush systemPush = buildPushMessage(cursor, columnsMap);
                if (fliterType.accept(target, systemPush)) {
                    tmpPushMessages.add(systemPush);
                }
            } while (cursor.moveToNext());
            count = tmpPushMessages.size();
        }
        return count;
    }

    public int getUnprocessedPushMessageCount(int type) {
        if (isInGuestMode()) {
            return 0;
        }

        final String totalCount = "total_count";
        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Push.CONTENT_URI;
        final String[] projection = {
                "COUNT(*) AS " + totalCount,
        };
        final String selection = DataConstants.Push.TYPE + "=? AND "
                + DataConstants.Push.STATE + "=?";
        final String[] selectionArgs = new String[]{
                String.valueOf(type),
                String.valueOf(SystemPush.STATE_UNPROCESSED),
        };
        final Cursor cursor = cr.query(uri, projection, selection, selectionArgs, null);
        int count = 0;

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            count = cursor.getInt(cursor.getColumnIndex(totalCount));
        }
        cursor.close();

        return count;
    }

    public int getUnprocessedPushMessageCount(String flag) {
        if (isInGuestMode()) {
            return 0;
        }

        final String totalCount = "total_count";
        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Push.CONTENT_URI;
        final String[] projection = {
                "COUNT(*) AS " + totalCount,
        };
        final String selection = DataConstants.Push.FLAGS + "=? AND "
                + DataConstants.Push.STATE + "=?";
        final String[] selectionArgs = new String[]{
                flag,
                String.valueOf(SystemPush.STATE_UNPROCESSED),
        };
        final Cursor cursor = cr.query(uri, projection, selection, selectionArgs, null);
        int count = 0;

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            count = cursor.getInt(cursor.getColumnIndex(totalCount));
        }
        cursor.close();

        return count;
    }

    public void updatePushMessageState(long messageId, int state) {
        if (isInGuestMode()) {
            return;
        }

        if (messageId >= 0) {
            final ContentResolver cr = mContext.getContentResolver();
            final Uri uri = ContentUris.withAppendedId(DataConstants.Push.CONTENT_URI, messageId);
            final ContentValues values = new ContentValues();

            values.put(DataConstants.Push.STATE, state);
            cr.update(uri, values, null, null);
        }
    }

    public void updatePushMessageProcessed(long messageId) {
        updatePushMessageState(messageId, SystemPush.STATE_PROCESSED);
    }

    public SystemPush getPushMessage(long messageId) {
        if (isInGuestMode()) {
            return null;
        }

        SystemPush pushMessage = null;
        if (messageId >= 0) {
            final ContentResolver cr = mContext.getContentResolver();
            final Uri uri = ContentUris.withAppendedId(DataConstants.Push.CONTENT_URI, messageId);
            final String[] projection = DataConstants.Push.ALL_COLUMNS;
            final Cursor cursor = cr.query(uri, projection, null, null, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                pushMessage = buildPushMessage(cursor);
            }
            cursor.close();
        }

        return pushMessage;
    }


    private SystemPush buildPushMessage(Cursor cursor) {
        return buildPushMessage(cursor, new ColumnsMap(cursor));
    }

    private SystemPush buildPushMessage(Cursor cursor, ColumnsMap columnsMap) {
        final long id = cursor.getLong(columnsMap.mId);
        final int type = cursor.getInt(columnsMap.mType);
        final int state = cursor.getInt(columnsMap.mState);
        final String content = cursor.getString(columnsMap.mContent);
        final long time = cursor.getLong(columnsMap.mTime);
        final String flags = cursor.getString(columnsMap.mFlags);
        final SystemPush pushMessage = new SystemPush();

        pushMessage.setId(id);
        pushMessage.setType(type);
        pushMessage.setState(state);
        pushMessage.setContent(content);
        pushMessage.setTime(time);
        pushMessage.setFlag(flags);

        return pushMessage;
    }

    private void notifyPushMessageDataChanged() {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            IListener listener = mListeners.get(i).get();
            if (listener != null) {
                listener.onPushMessageDataChanged();
            } else {
                mListeners.remove(i);
            }
        }
    }

    private void notifyNewSystemPushReceived(SystemPush systemPush) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            IListener listener = mListeners.get(i).get();
            if (listener != null) {
                listener.onNewSystemPushReceived(systemPush);
            } else {
                mListeners.remove(i);
            }
        }
    }

    public List<NewMessageHint> getHintMessage() {
        final ArrayList<NewMessageHint> hintMessage = new ArrayList<>();
        getPushMessagesFliterType(SystemPushType.COMMENT, 0, new FliterType() {
            @Override
            public boolean accept(int target, SystemPush push) {
                switch (push.getType()) {
                    case SystemPushType.TYPE_LABEL_STORY_COMMENTS:
                        DynamicOperateMessage dynamicMessage = DynamicOperateMessage.build(push);
                        if(dynamicMessage != null) {
                            if (dynamicMessage.getMessagePlace().equals(DynamicOperateMessage.TYPE_MESSAGE_RED_DOT)) {
                                NewMessageHint newMessageHint = new NewMessageHint();
                                newMessageHint.setDynamicMessage(dynamicMessage);
                                newMessageHint.setAvatarImage(dynamicMessage.getStranger().getAvatarThumb());
                                newMessageHint.setType(push.getType());
                                newMessageHint.setTime(push.getTime());
                                newMessageHint.setId(push.getId());
                                newMessageHint.setState(push.getState());
                                hintMessage.add(newMessageHint);
                            }
                        }
                        break;
                    case SystemPushType.TYPE_CONFIDE_COMMEND:
                        ConfideMessage confideMessage = ConfideMessage.build(push);
                        if (confideMessage != null) {
                            if (confideMessage.getMessagePlace().equals(ConfideMessage.TYPE_MESSAGE_RED_DOT)) {
                                NewMessageHint newMessageHint = new NewMessageHint();
                                newMessageHint.setConfideMessages(confideMessage);
                                newMessageHint.setAvatarImage(confideMessage.getVirtualAvatar());
                                newMessageHint.setType(push.getType());
                                newMessageHint.setTime(push.getTime());
                                newMessageHint.setId(push.getId());
                                newMessageHint.setState(push.getState());
                                hintMessage.add(newMessageHint);
                            }
                        }
                }
                return true;
            }
        });
        return hintMessage;
    }

    public List<NewMessageHint> getUnprocessedHintMessage() {
        List<NewMessageHint> newMessageHintList = new ArrayList<>();
        List<NewMessageHint> temList = getHintMessage();
        if (temList != null && temList.size() > 0) {
            for (NewMessageHint messageHint : temList) {
                if (messageHint.getState() == SystemPush.STATE_UNPROCESSED) {
                    newMessageHintList.add(messageHint);
                }
            }
        }
        return newMessageHintList;
    }
}
