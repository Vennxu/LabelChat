
package com.ekuater.labelchat.coreservice.contacts;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.contact.ContactSyncCommand;
import com.ekuater.labelchat.command.contact.DeleteFriendCommand;
import com.ekuater.labelchat.command.contact.ModifyFriendRemarkCommand;
import com.ekuater.labelchat.coreservice.EventBusHub;
import com.ekuater.labelchat.coreservice.ICoreServiceCallback;
import com.ekuater.labelchat.coreservice.command.ICommandResponseHandler;
import com.ekuater.labelchat.coreservice.event.ChatUserEvent;
import com.ekuater.labelchat.coreservice.event.ChatUserGotEvent;
import com.ekuater.labelchat.data.DataConstants.Contact;
import com.ekuater.labelchat.datastruct.AddFriendMessage;
import com.ekuater.labelchat.datastruct.AddFriendAgreeResultMessage;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.DefriendNotificationMessage;
import com.ekuater.labelchat.datastruct.FriendInfoUpdateMessage;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.util.L;
import com.ekuater.labelchat.util.SystemAccount;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * @author LinYong
 */
public class ContactsStore {

    private static final String TAG = ContactsStore.class.getSimpleName();
    private static final Uri BASE_URI = Contact.CONTENT_URI;

    private interface IContactsListenerNotifier {
        public void notify(IContactsListener listener);
    }

    private static final class CommandResult {

        public final int result;
        public final String response;
        public final Object extra;

        public CommandResult(int result, String response, Object extra) {
            this.result = result;
            this.response = response;
            this.extra = extra;
        }
    }

    private static final int MSG_HANDLE_SYNC = 101;
    private static final int MSG_HANDLE_SYNC_RESULT = 102;

    private class ProcessHandler extends Handler {

        public ProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_SYNC:
                    handleSync();
                    break;
                case MSG_HANDLE_SYNC_RESULT:
                    handleSyncResult((CommandResult) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private final Context mContext;
    private final ICoreServiceCallback mCallback;
    private final List<WeakReference<IContactsListener>> mListeners = new ArrayList<WeakReference<IContactsListener>>();
    private final ProcessHandler mProcessHandler;

    private boolean mInSync = false;
    private int mSyncRetryTime;

    private final SystemAccount systemAccount;
    private final List<String> userIdCache;
    private final EventBus chatEventBus;

    public ContactsStore(Context context, ICoreServiceCallback callback) {
        mContext = context;
        mCallback = callback;
        mProcessHandler = new ProcessHandler(mCallback.getProcessLooper());
        systemAccount = SystemAccount.getInstance(context);
        userIdCache = new ArrayList<String>();
        chatEventBus = EventBusHub.getChatEventBus();
        chatEventBus.register(this, 100);
    }

    public void deInit() {
        chatEventBus.unregister(this);
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(ChatUserEvent event) {
        String userId = event.getUserId();

        if (systemAccount.getAccount(userId) != null
                || inUserIdCache(userId) || queryContact(userId) >= 0) {
            event.setSyncGotEvent(new ChatUserGotEvent(userId,
                    ChatUserGotEvent.UserType.CONTACT));
            chatEventBus.cancelEventDelivery(event);
            addToUserIdCache(userId);
        }
    }

    private void addToUserIdCache(String userId) {
        synchronized (userIdCache) {
            if (!TextUtils.isEmpty(userId) && !inUserIdCache(userId)) {
                int length = userIdCache.size();
                if (length > 50) {
                    userIdCache.remove(length - 1);
                }
                userIdCache.add(0, userId);
            }
        }
    }

    private void deleteFromUserIdCache(String userId) {
        synchronized (userIdCache) {
            Iterator<String> iterator = userIdCache.iterator();

            while (iterator.hasNext()) {
                if (iterator.next().equals(userId)) {
                    iterator.remove();
                }
            }
        }
    }

    private void clearUserIdCache() {
        synchronized (userIdCache) {
            userIdCache.clear();
        }
    }

    private boolean inUserIdCache(String userId) {
        synchronized (userIdCache) {
            for (String tmpUserId : userIdCache) {
                if (tmpUserId.equals(userId)) {
                    return true;
                }
            }
            return false;
        }
    }

    public synchronized void registerListener(final IContactsListener listener) {
        for (WeakReference<IContactsListener> ref : mListeners) {
            if (ref.get() == listener) {
                return;
            }
        }

        mListeners.add(new WeakReference<IContactsListener>(listener));
        unregisterListener(null);
    }

    public synchronized void unregisterListener(final IContactsListener listener) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            if (mListeners.get(i).get() == listener) {
                mListeners.remove(i);
            }
        }
    }

    public void sync() {
        Message message = mProcessHandler.obtainMessage(MSG_HANDLE_SYNC);
        mProcessHandler.sendMessage(message);
    }

    private void handleSync() {
        if (!mInSync) {
            mSyncRetryTime = 3;
            mInSync = true;
            syncInternal();
        }
    }

    private synchronized void syncInternal() {
        if (mSyncRetryTime > 0) {
            mSyncRetryTime--;

            ContactSyncCommand command = new ContactSyncCommand(getSession(), getUserId());
            ICommandResponseHandler handler = new ICommandResponseHandler() {
                @Override
                public void onResponse(RequestCommand command, int result, String response) {
                    Message message = mProcessHandler.obtainMessage(MSG_HANDLE_SYNC_RESULT,
                            new CommandResult(result, response, null));
                    mProcessHandler.sendMessage(message);
                }
            };
            executeCommand(command, handler);
        } else {
            mInSync = false;
        }
    }

    private void handleSyncResult(CommandResult commandResult) {
        final int result = commandResult.result;
        final String response = commandResult.response;

        switch (result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                mInSync = false;

                try {
                    ContactSyncCommand.CommandResponse cmdResp
                            = new ContactSyncCommand.CommandResponse(response);
                    if (cmdResp.requestSuccess()) {
                        clear();
                        UserContact[] contacts = cmdResp.getContacts();
                        if (contacts != null) {
                            for (UserContact contact : contacts) {
                                updateOrAddContact(contact);
                            }
                        }
                    }
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                break;
            case ConstantCode.EXECUTE_RESULT_NETWORK_ERROR:
                // sync contact again
                syncInternal();
                break;
            default:
                mInSync = false;
                break;
        }
    }

    public void clear() {
        mContext.getContentResolver().delete(BASE_URI, null, null);
        clearUserIdCache();
    }

    public void onNewPushMessage(SystemPush systemPush) {
        if (systemPush != null) {
            switch (systemPush.getType()) {
                case SystemPushType.TYPE_ADD_FRIEND:
                    handleAddContact(systemPush);
                    break;
                case SystemPushType.TYPE_FRIEND_INFO_UPDATED:
                    handleUpdateContact(systemPush);
                    break;
                case SystemPushType.TYPE_ADD_FRIEND_AGREE_RESULT:
                    handleAddFriendAgreeResult(systemPush);
                    break;
                case SystemPushType.TYPE_ADD_FRIEND_REJECT_RESULT:
                    // do nothing
                    break;
                case SystemPushType.TYPE_DEFRIEND_NOTIFICATION:
                    handleDefriendNotification(systemPush);
                    break;
                default:
                    break;
            }
        }
    }

    private synchronized void notifyContactsListeners(IContactsListenerNotifier notifier) {
        final List<WeakReference<IContactsListener>> listeners = mListeners;

        for (int i = listeners.size() - 1; i >= 0; i--) {
            IContactsListener listener = listeners.get(i).get();
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

    private class ModifyRemarkCmdHandler implements ICommandResponseHandler {

        private final String mFriendUserId;
        private final String mFriendRemark;

        public ModifyRemarkCmdHandler(String friendUserId, String friendRemark) {
            mFriendUserId = friendUserId;
            mFriendRemark = friendRemark;
        }

        @Override
        public void onResponse(RequestCommand command, int result, String response) {
            int _ret = ConstantCode.CONTACT_OPERATION_NETWORK_ERROR;

            switch (result) {
                case ConstantCode.EXECUTE_RESULT_SUCCESS:
                    _ret = parseResponse(response);
                    break;
                default:
                    break;
            }

            notifyModifyFriendRemarkResult(_ret, mFriendUserId, mFriendRemark);
        }

        private int parseResponse(String response) {
            int result = ConstantCode.CONTACT_OPERATION_RESPONSE_DATA_ERROR;

            try {
                ModifyFriendRemarkCommand.CommandResponse cmdResp
                        = new ModifyFriendRemarkCommand.CommandResponse(response);
                if (cmdResp.requestSuccess()) {
                    result = ConstantCode.CONTACT_OPERATION_SUCCESS;
                    updateUserContactRemark(mFriendUserId, mFriendRemark);
                } else {
                    result = ConstantCode.CONTACT_OPERATION_SYSTEM_ERROR;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return result;
        }
    }

    public void modifyFriendRemark(String friendUserId, String friendRemark) {
        ModifyFriendRemarkCommand command = new ModifyFriendRemarkCommand(getSession(),
                getUserId());
        command.putParamFriendUserId(friendUserId);
        command.putParamFriendRemark(friendRemark);
        ICommandResponseHandler handler = new ModifyRemarkCmdHandler(friendUserId, friendRemark);
        executeCommand(command, handler);
    }

    private void updateUserContactRemark(String userId, String remark) {
        final long id = queryContact(userId);

        if (id >= 0) {
            final ContentResolver cr = mContext.getContentResolver();
            final Uri uri = ContentUris.withAppendedId(Contact.CONTENT_URI, id);
            final ContentValues values = new ContentValues();
            values.put(Contact.REMARKS_NAME, remark);
            cr.update(uri, values, null, null);
        }
    }

    public void updateContact(UserContact contact) {
        final long _id = queryContact(contact.getUserId());

        if (_id >= 0) {
            final ContentResolver cr = mContext.getContentResolver();
            final Uri uri = ContentUris.withAppendedId(BASE_URI, _id);
            final ContentValues values = getContactValues(contact);
            cr.update(uri, values, null, null);
        }
    }

    private synchronized void notifyModifyFriendRemarkResult(int result, String friendUserId,
                                                             String friendRemark) {
        notifyContactsListeners(new ModifyFriendRemarkResultNotifier(
                result, friendUserId, friendRemark));
    }

    private void handleAddContact(SystemPush systemPush) {
        final AddFriendMessage message = AddFriendMessage.build(systemPush);

        if (message != null) {
            final UserContact contact = message.getContact();

            if (contact != null) {
                addContact(contact);
                notifyNewContactAdded(contact);
            }
        }
    }

    private void handleUpdateContact(SystemPush systemPush) {
        final FriendInfoUpdateMessage message = FriendInfoUpdateMessage.build(systemPush);

        if (message != null) {
            final UserContact contact = message.getContact();

            if (contact != null) {
                updateOrAddContact(contact);
                notifyContactUpdated(contact);
            }
        }
    }

    private void handleAddFriendAgreeResult(SystemPush systemPush) {
        final AddFriendAgreeResultMessage message
                = AddFriendAgreeResultMessage.build(systemPush);

        if (message != null) {
            final UserContact contact = message.getContact();
            // Store contact to local database.
            addContact(contact);
            notifyNewContactAdded(contact);
        }
    }

    private void handleDefriendNotification(SystemPush systemPush) {
        final DefriendNotificationMessage message
                = DefriendNotificationMessage.build(systemPush);

        if (message != null) {
            final String friendUserId = message.getUserId();

            deleteContact(friendUserId);
            // notify CoreService to delete chat history of userId
            mCallback.clearChatHistory(friendUserId);
            notifyContactDefriendedMe(friendUserId);
        }
    }

    private class DeleteFriendCmdHandler implements ICommandResponseHandler {

        private final String mFriendUserId;
        private final String mFriendLabelCode;

        public DeleteFriendCmdHandler(String userId, String labelCode) {
            mFriendUserId = userId;
            mFriendLabelCode = labelCode;
        }

        @Override
        public void onResponse(RequestCommand command, int result, String response) {
            int _ret = ConstantCode.CONTACT_OPERATION_NETWORK_ERROR;

            switch (result) {
                case ConstantCode.EXECUTE_RESULT_SUCCESS:
                    _ret = parseResponse(response);
                    break;
                default:
                    break;
            }

            notifyDeleteFriendResult(_ret, mFriendUserId, mFriendLabelCode);
        }

        private int parseResponse(String response) {
            int result = ConstantCode.CONTACT_OPERATION_RESPONSE_DATA_ERROR;

            try {
                ModifyFriendRemarkCommand.CommandResponse cmdResp
                        = new ModifyFriendRemarkCommand.CommandResponse(response);
                if (cmdResp.requestSuccess()) {
                    result = ConstantCode.CONTACT_OPERATION_SUCCESS;
                    deleteContact(mFriendUserId);
                } else {
                    result = ConstantCode.CONTACT_OPERATION_SYSTEM_ERROR;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return result;
        }
    }

    public void deleteFriend(String friendUserId, String friendLabelCode) {
        DeleteFriendCommand command = new DeleteFriendCommand(getSession(),
                getUserId(), getLabelCode());
        command.putParamFriendUserId(friendUserId);
        command.putParamFriendLabelCode(friendLabelCode);
        ICommandResponseHandler handler = new DeleteFriendCmdHandler(friendUserId, friendLabelCode);
        executeCommand(command, handler);
    }

    private synchronized void notifyDeleteFriendResult(int result, String friendUserId,
                                                       String friendLabelCode) {
        notifyContactsListeners(new DeleteFriendResultNotifier(
                result, friendUserId, friendLabelCode));
    }

    private synchronized void notifyNewContactAdded(UserContact contact) {
        notifyContactsListeners(new NewContactAddedNotifier(contact));
    }

    private synchronized void notifyContactUpdated(UserContact contact) {
        notifyContactsListeners(new ContactUpdatedNotifier(contact));
    }

    private synchronized void notifyContactDefriendedMe(String friendUserId) {
        notifyContactsListeners(new DefriendMeNotifier(friendUserId));
    }

    private ContentValues getContactValues(UserContact contact) {
        ContentValues values = new ContentValues();

        values.put(Contact.USER_ID, contact.getUserId());
        values.put(Contact.LABEL_CODE, contact.getLabelCode());
        values.put(Contact.NICKNAME, contact.getNickname());
        values.put(Contact.REMARKS_NAME, contact.getRemarkName());
        values.put(Contact.MOBILE, contact.getMobile());
        values.put(Contact.SEX, contact.getSex());
        values.put(Contact.BIRTHDAY, contact.getBirthday());
        values.put(Contact.AGE, contact.getAge());
        values.put(Contact.CONSTELLATION, contact.getConstellation());
        values.put(Contact.PROVINCE, contact.getProvince());
        values.put(Contact.CITY, contact.getCity());
        values.put(Contact.SCHOOL, contact.getSchool());
        values.put(Contact.SIGNATURE, contact.getSignature());
        values.put(Contact.AVATAR, contact.getAvatar());
        values.put(Contact.AVATAR_THUMB, contact.getAvatarThumb());
        values.put(Contact.LABELS, contact.getLabelsString());
        values.put(Contact.APPEARANCE_FACE, contact.getAppearanceFace());
        values.put(Contact.THEME, contact.getTheme() != null
                ? contact.getTheme().getThemeName() : "");

        return values;
    }

    private void addContact(UserContact contact) {
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = BASE_URI;
        ContentValues values = getContactValues(contact);

        Uri newUri = cr.insert(uri, values);
        long id = ContentUris.parseId(newUri);
        contact.setId(id);
    }

    private void updateOrAddContact(UserContact contact) {
        ContentResolver cr = mContext.getContentResolver();
        ContentValues values = getContactValues(contact);
        long _id = queryContact(contact.getUserId());

        if (_id != -1L) {
            Uri uri = ContentUris.withAppendedId(BASE_URI, _id);
            cr.update(uri, values, null, null);
        } else {
            Uri uri = BASE_URI;
            Uri newUri = cr.insert(uri, values);
            long id = ContentUris.parseId(newUri);
            contact.setId(id);
        }
    }

    private void deleteContact(String userId) {
        ContentResolver cr = mContext.getContentResolver();
        long _id = queryContact(userId);

        if (_id != -1L) {
            Uri uri = ContentUris.withAppendedId(BASE_URI, _id);
            cr.delete(uri, null, null);
        }
        deleteFromUserIdCache(userId);
    }

    private long queryContact(String userId) {
        ContentResolver cr = mContext.getContentResolver();
        String[] projection = new String[]{
                Contact._ID
        };
        String selection = String.format("%s = '%s'", Contact.USER_ID, userId);
        Uri uri = BASE_URI;
        Cursor cursor = null;
        long _id = -1L;

        try {
            cursor = cr.query(uri, projection, selection, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                _id = cursor.getLong(cursor.getColumnIndex(Contact._ID));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return _id;
    }

    private void executeCommand(BaseCommand command, ICommandResponseHandler handler) {
        mCallback.preTreatCommand(command);
        mCallback.executeCommand(command.toRequestCommand(), handler);
    }

    private String getSession() {
        return mCallback.getAccountSession();
    }

    private String getUserId() {
        return mCallback.getAccountUserId();
    }

    private String getLabelCode() {
        return mCallback.getAccountLabelCode();
    }

    private static class ModifyFriendRemarkResultNotifier implements IContactsListenerNotifier {

        private final int mResult;
        private final String mFriendUserId;
        private final String mFriendRemark;

        public ModifyFriendRemarkResultNotifier(int result, String friendUserId,
                                                String friendRemark) {
            mResult = result;
            mFriendUserId = friendUserId;
            mFriendRemark = friendRemark;
        }

        @Override
        public void notify(IContactsListener listener) {
            listener.onModifyFriendRemarkResult(mResult, mFriendUserId, mFriendRemark);
        }
    }

    private static class DeleteFriendResultNotifier implements IContactsListenerNotifier {

        private final int mResult;
        private final String mFriendUserId;
        private final String mFriendLabelCode;

        public DeleteFriendResultNotifier(int result, String friendUserId,
                                          String friendLabelCode) {
            mResult = result;
            mFriendUserId = friendUserId;
            mFriendLabelCode = friendLabelCode;
        }

        @Override
        public void notify(IContactsListener listener) {
            listener.onDeleteFriendResult(mResult, mFriendUserId, mFriendLabelCode);
        }
    }

    private static class NewContactAddedNotifier implements IContactsListenerNotifier {

        private final UserContact mContact;

        public NewContactAddedNotifier(UserContact contact) {
            mContact = contact;
        }

        @Override
        public void notify(IContactsListener listener) {
            listener.onNewContactAdded(mContact);
        }
    }

    private static class ContactUpdatedNotifier implements IContactsListenerNotifier {

        private final UserContact mContact;

        public ContactUpdatedNotifier(UserContact contact) {
            mContact = contact;
        }

        @Override
        public void notify(IContactsListener listener) {
            listener.onContactUpdated(mContact);
        }
    }

    private static class DefriendMeNotifier implements IContactsListenerNotifier {

        private final String mFriendUserId;

        public DefriendMeNotifier(String friendUserId) {
            mFriendUserId = friendUserId;
        }

        @Override
        public void notify(IContactsListener listener) {
            listener.onContactDefriendedMe(mFriendUserId);
        }
    }
}
