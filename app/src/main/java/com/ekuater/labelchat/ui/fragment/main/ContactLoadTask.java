package com.ekuater.labelchat.ui.fragment.main;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.ekuater.labelchat.datastruct.LabelStoryFeedTipMessage;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.util.CharacterParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Leo on 2015/3/9.
 *
 * @author LinYong
 */
public class ContactLoadTask extends AsyncTask<Void, Void, List<ContactsListItem.Item>> {

    public interface Listener {
        public void onLoadDone(List<ContactsListItem.Item> items);
    }

    private Context mContext;
    private Listener mListener;
    private ContactsManager mContactsManager;
    private PushMessageManager mPushMessageManager;
    private AvatarManager mAvatarManager;
    private CharacterParser mCharacterParser;
    private PinyinComparator mPinyinComparator;

    private ContactsListItem.ContactItemListener mContactItemListener;

    public ContactLoadTask(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
        mContactsManager = ContactsManager.getInstance(context);
        mPushMessageManager = PushMessageManager.getInstance(context);
        mAvatarManager = AvatarManager.getInstance(context);
        mCharacterParser = CharacterParser.getInstance();
        mPinyinComparator = new PinyinComparator();
    }

    public void setContactItemListener(ContactsListItem.ContactItemListener listener) {
        mContactItemListener = listener;
    }

    @Override
    protected void onPostExecute(List<ContactsListItem.Item> items) {
        if (mListener != null) {
            mListener.onLoadDone(items);
        }
    }

    @Override
    protected List<ContactsListItem.Item> doInBackground(Void... params) {
        final List<ContactsListItem.Item> items = new ArrayList<>();
        final UserContact[] contacts = mContactsManager.getAllUserContact();
        SystemPush[] systemPushs = mPushMessageManager.getPushMessagesByType(
                SystemPushType.TYPE_LABEL_STORY_TIP);

        setupContactTips(systemPushs, contacts);

        if (contacts != null) {
            for (UserContact contact : contacts) {
                final String userId = contact.getUserId();
                final String avatarUrl = contact.getAvatarThumb();
                final String name = contact.getShowName();

                String sortLetter;
                if (!TextUtils.isEmpty(name)) {
                    final String pinyin = mCharacterParser.getSelling(name);
                    final String firstLetter = pinyin.substring(0, 1).toUpperCase();
                    sortLetter = firstLetter.matches("[A-Z]")
                            ? firstLetter.toUpperCase() : "#";
                } else {
                    sortLetter = "#";
                }

                items.add(new ContactsListItem.ContactItem(mContext, name, sortLetter,
                        userId, avatarUrl, contact.getLabelStoryFeedTipMessage(),
                        mAvatarManager, mContactItemListener));
            }
        }

        Collections.sort(items, mPinyinComparator);

        return items;
    }

    private void setupContactTips(SystemPush[] systemPushs, UserContact[] contacts) {
        if (systemPushs == null || systemPushs.length <= 0
                || contacts == null || contacts.length <= 0) {
            return;
        }

        List<LabelStoryFeedTipMessage> tipMessages
                = LabelStoryFeedTipMessage.build(systemPushs);

        if (tipMessages == null || tipMessages.size() <= 0) {
            return;
        }

        for (UserContact contact : contacts) {
            if (contact == null) {
                continue;
            }

            String userId = contact.getUserId();
            Iterator<LabelStoryFeedTipMessage> iterator = tipMessages.iterator();

            while (iterator.hasNext()) {
                LabelStoryFeedTipMessage tipMessage = iterator.next();

                if (tipMessage == null) {
                    iterator.remove();
                    continue;
                }

                String tipUserId = tipMessage.getmFreidUserId();

                if (TextUtils.isEmpty(tipUserId)) {
                    // TODO delete this tip from database
                    iterator.remove();
                    continue;
                }

                if (!tipUserId.equals(userId)) {
                    continue;
                }

                LabelStoryFeedTipMessage tmpTipMessage
                        = contact.getLabelStoryFeedTipMessage();
                if (tmpTipMessage != null && tipUserId.equals(
                        tmpTipMessage.getmFreidUserId())) {
                    // TODO delete this tip from database
                    iterator.remove();
                    continue;
                }
                contact.setLabelStoryFeedTipMessage(tipMessage);
            }
        }
    }

    public ContactLoadTask executeInThreadPool() {
        executeOnExecutor(THREAD_POOL_EXECUTOR, (Void) null);
        return this;
    }
}
