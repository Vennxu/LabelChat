package com.ekuater.labelchat.ui.fragment.main;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ChatManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.delegate.StrangerManager;
import com.ekuater.labelchat.delegate.TmpGroupManager;
import com.ekuater.labelchat.ui.fragment.push.SystemPushUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Leo on 2015/3/9.
 *
 * @author LinYong
 */
public class MessageLoadTask extends AsyncTask<Void, Void, List<MessageListItem.Item>> {

    public interface Listener {
        void onLoadDone(List<MessageListItem.Item> items);
    }

    private static class ItemComparator implements Comparator<MessageListItem.Item> {

        @Override
        public int compare(MessageListItem.Item lhs, MessageListItem.Item rhs) {
            long diff = lhs.getTime() - rhs.getTime();
            return -((diff > 0) ? 1 : (diff < 0) ? -1 : 0);
        }
    }

    private final Context mContext;
    private final Listener mListener;
    private final ItemComparator mItemComparator;
    private final PushMessageManager mPushMessageManager;
    private final ChatManager mChatManager;
    private final TmpGroupManager mTmpGroupManager;
    private final StrangerManager mStrangerManager;
    private final ContactsManager mContactsManager;
    private final AvatarManager mAvatarManager;

    private MessageListItem.GroupChatItemListener mGroupChatItemListener;
    private MessageListItem.PrivateChatItemListener mPrivateChatItemListener;

    public MessageLoadTask(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
        mItemComparator = new ItemComparator();
        mTmpGroupManager = TmpGroupManager.getInstance(context);
        mStrangerManager = StrangerManager.getInstance(context);
        mChatManager = ChatManager.getInstance(context);
        mPushMessageManager = PushMessageManager.getInstance(context);
        mContactsManager = ContactsManager.getInstance(context);
        mAvatarManager = AvatarManager.getInstance(context);
    }

    public void setGroupChatItemListener(MessageListItem.GroupChatItemListener listener) {
        mGroupChatItemListener = listener;
    }

    public void setPrivateChatItemListener(MessageListItem.PrivateChatItemListener listener) {
        mPrivateChatItemListener = listener;
    }

    public MessageLoadTask executeInThreadPool() {
        executeOnExecutor(THREAD_POOL_EXECUTOR, (Void) null);
        return this;
    }

    @Override
    protected void onPostExecute(List<MessageListItem.Item> items) {
        if (mListener != null) {
            mListener.onLoadDone(items);
        }
    }

    @Override
    protected List<MessageListItem.Item> doInBackground(Void... params) {
        List<MessageListItem.Item> itemList = new ArrayList<>();
        loadSystemPushMessage(itemList);
        loadGroupSystemPushMessage(itemList);
        loadChatSessions(itemList);
        Collections.sort(itemList, mItemComparator);
        return itemList;
    }

    private PushMessageManager.FliterType fliterType = new PushMessageManager.FliterType() {
        @Override
        public boolean accept(int target, SystemPush push) {
            return SystemPushUtils.getFliterType(target, push);
        }
    };

    private void loadSystemPushMessage(List<MessageListItem.Item> itemList) {

        final ArrayList<SystemPush> other = mPushMessageManager.getEveryTypeLastPushMessages(SystemPushUtils.SYSTEM_PUSH_OTHER, fliterType);
//        final SystemPush[] systemPushMessages = mPushMessageManager
//        .getEveryTypeLastPushMessage();
//
        final List<Integer> expandTypeList = new ArrayList<>();
        if (other != null) {
            for (SystemPush systemPush : other) {
                if (systemPush != null) {
                    MessageListItem.AbsSystemItem systemItem
                            = SystemMsgItem.build(mContext, systemPush);
                    if (systemItem == null) {
                        // Not support type, skip it.
                        continue;
                    }

                    if (isExpandAllPush(systemItem)) {
                        expandTypeList.add(systemPush.getType());
                    } else {
                        itemList.add(systemItem);
                    }
                }
            }
        }

        for (int type : expandTypeList) {
            SystemPush[] pushMessages = mPushMessageManager.getPushMessagesByType(type);
            if (pushMessages != null) {
                for (SystemPush systemPush : pushMessages) {
                    if (systemPush != null) {
                        MessageListItem.AbsSystemItem systemItem
                                = SystemMsgItem.build(mContext, systemPush);
                        if (systemItem == null) {
                            // Not support type, skip it.
                            continue;
                        }
                        itemList.add(systemItem);
                    }
                }
            }
        }
    }

    private boolean isExpandAllPush(MessageListItem.AbsSystemItem systemItem) {
        return systemItem.isExpandAllType();
    }

    private void loadChatSessions(List<MessageListItem.Item> itemList) {
        final ChatMessage[] chatMessages = mChatManager.getEveryTargetLastChatMessage();
        if (chatMessages != null) {
            for (ChatMessage chatMessage : chatMessages) {
                MessageListItem.Item item = newChatItem(chatMessage);
                if (item != null) {
                    itemList.add(item);
                }
            }
        }
    }

    private void loadGroupSystemPushMessage(List<MessageListItem.Item> itemList){
        final SystemPush comment = mPushMessageManager.getLastPushMessagesFliterType(SystemPushType.COMMENT, SystemPushUtils.SYSTEM_PUSH_COMMENT, fliterType);
        final SystemPush praise = mPushMessageManager.getLastPushMessagesFliterType(SystemPushType.PRAISE,SystemPushUtils.SYSTEM_PUSH_PRAISE, fliterType);
        final SystemPush remind = mPushMessageManager.getLastPushMessagesFliterType(SystemPushType.REMIND, SystemPushUtils.SYSTEM_PUSH_REMIND, fliterType);
        if (comment != null){
            MessageListItem.Item commentItem = new SystemGroupItem.CommentItem(mContext,comment);
            itemList.add(commentItem);
        }
        if (praise != null){
            MessageListItem.Item praiseItem = new SystemGroupItem.PraiseItem(mContext,praise);
            itemList.add(praiseItem);
        }
        if (remind != null){
            MessageListItem.Item remaindItem = new SystemGroupItem.RemaindItem(mContext,remind);
            itemList.add(remaindItem);
        }
    }

    private MessageListItem.Item newChatItem(ChatMessage chatMessage) {
        final MessageListItem.Item item;

        switch (chatMessage.getConversationType()) {
            case ChatMessage.CONVERSATION_PRIVATE: {
                MessageListItem.PrivateChatItem privateItem
                        = new MessageListItem.PrivateChatItem(mContext,
                        mAvatarManager, mPrivateChatItemListener);
                privateItem.setStringId(chatMessage.getTargetId());
                privateItem.setSubTitle(getChatMessageItemSubTitle(chatMessage));
                privateItem.setCurrentTime(chatMessage.getTime());
                privateItem.setNewMsgCount(mChatManager.getTargetUnreadChatMessageCount(
                        chatMessage.getTargetId()));
                loadChatMessageItem(privateItem);
                item = privateItem;
                break;
            }
            case ChatMessage.CONVERSATION_GROUP: {
                MessageListItem.GroupChatItem groupItem
                        = new MessageListItem.GroupChatItem(mContext,
                        mAvatarManager, mGroupChatItemListener);
                groupItem.setStringId(chatMessage.getTargetId());
                groupItem.setSubTitle(getChatMessageItemSubTitle(chatMessage));
                groupItem.setCurrentTime(chatMessage.getTime());
                groupItem.setNewMsgCount(mChatManager.getTargetUnreadChatMessageCount(
                        chatMessage.getTargetId()));
                loadGroupMessageItem(groupItem);
                item = groupItem;
                break;
            }
            default:
                item = null;
                break;
        }

        return item;
    }

    private String getChatMessageItemSubTitle(ChatMessage chatMessage) {
        String subTitle;

        switch (chatMessage.getType()) {
            case ChatMessage.TYPE_TEXT:
                subTitle = chatMessage.getContent();
                break;
            case ChatMessage.TYPE_VOICE:
                subTitle = mContext.getString(R.string.voice_message);
                break;
            case ChatMessage.TYPE_IMAGE:
                subTitle = mContext.getString(R.string.image_message);
                break;
            default:
                subTitle = mContext.getString(R.string.unknown);
                break;
        }

        return subTitle;
    }

    private MessageListItem.PrivateChatItem loadChatMessageItem(
            MessageListItem.PrivateChatItem item) {
        String userId = item.getStringId();
        String title = null;

        do {
            UserContact contact = mContactsManager.getUserContactByUserId(userId);
            if (contact != null) {
                title = contact.getShowName();
                item.setAvatarUrl(contact.getAvatarThumb());
                break;
            }

            Stranger stranger = mStrangerManager.getStranger(userId);
            if (stranger != null) {
                title = stranger.getShowName();
                item.setAvatarUrl(stranger.getAvatarThumb());
                break;
            }
        } while (false);

        title = TextUtils.isEmpty(title) ? mContext.getString(R.string.unknown) : title;
        item.setTitle(title);

        return item;
    }

    private MessageListItem.GroupChatItem loadGroupMessageItem(MessageListItem.GroupChatItem item) {
        final TmpGroup tmpGroup = mTmpGroupManager.queryGroup(item.getStringId());
        String title = null;

        if (tmpGroup != null) {
            title = tmpGroup.getGroupName();
            item.setAvatarUrl(tmpGroup.getGroupAvatar());
            item.setCreateTime(tmpGroup.getLocalCreateTime());
            item.setTotalDurationTime(tmpGroup.getExpireTime() - tmpGroup.getCreateTime());
        }
        title = TextUtils.isEmpty(title) ? mContext.getString(R.string.unknown) : title;
        item.setTitle(title);
        return item;
    }
}