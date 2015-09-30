package com.ekuater.labelchat.ui.activity.chatting;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.LiteStrangerManager;
import com.ekuater.labelchat.delegate.ShortUrlImageLoadListener;
import com.ekuater.labelchat.delegate.StrangerManager;
import com.ekuater.labelchat.delegate.TmpGroupManager;
import com.ekuater.labelchat.delegate.imageloader.LoadFailType;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.util.MiscUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LinYong
 */
public class MessageListAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    private final AdapterHelper mHelper;
    private final int mDefaultAvatarIconId;
    private final String mSelfAvatarUrl;
    private String mFriendAvatarUrl;
    private List<ChatMessage> mChatMessageList = new ArrayList<ChatMessage>();
    private Bitmap mSelfAvatarBitmap;
    private Bitmap mFriendAvatarBitmap;
    private ChatConversation mChatConversation;
    private ContactsManager mContactsManager;
    private AvatarManager mAvatarManager;
    private TmpGroupManager mTmpGroupManager;
    private LiteStrangerManager mLiteStrangerManager;

    public MessageListAdapter(FragmentActivity activity, String targetId,
                              ListView listView, ChatConversation conversation) {
        super();
        mChatConversation = conversation;
        mTmpGroupManager = TmpGroupManager.getInstance(activity);
        mContactsManager = ContactsManager.getInstance(activity);
        mLiteStrangerManager = LiteStrangerManager.getInstance(activity);
        mInflater = LayoutInflater.from(activity);
        mHelper = new AdapterHelper(activity, targetId, conversation);
        mDefaultAvatarIconId = R.drawable.contact_single;
        mSelfAvatarUrl = getSelfAvatarUrl(activity);

        if (listView != null) {
            listView.setRecyclerListener(new AbsListView.RecyclerListener() {
                @Override
                public void onMovedToScrapHeap(View view) {
                    mHelper.unbindView(view);
                }
            });
        }

        mAvatarManager = AvatarManager.getInstance(activity);
        ShortUrlImageLoadListener avatarListener = new ShortUrlImageLoadListener() {
            @Override
            public void onLoadFailed(String url, LoadFailType loadFailType) {
            }

            @Override
            public void onLoadComplete(String url, Bitmap loadedImage) {
                if (url.equals(mSelfAvatarUrl)) {
                    mSelfAvatarBitmap = loadedImage;
                    notifyDataSetChanged();
                } else if (url.equals(mFriendAvatarUrl)) {
                    mFriendAvatarBitmap = loadedImage;
                    notifyDataSetChanged();
                }
            }
        };

        switch (mChatConversation) {
            case PRIVATE:
                mFriendAvatarUrl = getFriendAvatarUrl(targetId);
                mFriendAvatarBitmap = mAvatarManager.getAvatarThumbBitmap(
                        mFriendAvatarUrl, avatarListener);
                break;
            case STRANGER_TEMP:
                mFriendAvatarUrl = getStrangerAvatarUrl(activity, targetId);
                mFriendAvatarBitmap = mAvatarManager.getAvatarThumbBitmap(
                        mFriendAvatarUrl, avatarListener);
                break;
            default:
                break;
        }
        mSelfAvatarBitmap = mAvatarManager.getAvatarThumbBitmap(
                mSelfAvatarUrl, avatarListener);
    }

    public MessageListAdapter(FragmentActivity activity, String targetId,
                              ChatConversation conversation) {
        this(activity, targetId, null, conversation);
    }

    public synchronized void updateChatMessages(List<ChatMessage> list) {
        if (list != null) {
            mChatMessageList = list;
        } else {
            mChatMessageList = new ArrayList<ChatMessage>();
        }
        notifyDataSetChanged();
    }

    public void updateList(List<ChatMessage> list) {
        if (list != null) {
            mChatMessageList = list;
        } else {
            mChatMessageList = new ArrayList<ChatMessage>();
        }
    }

    public synchronized void onPause() {
        mHelper.onPause();
    }

    public synchronized void onResume() {
        mHelper.onResume();
    }

    @Override
    public int getItemViewType(int position) {
        return MessageType.getMessageType(getItem(position));
    }

    @Override
    public int getViewTypeCount() {
        return MessageType.getTypeCount();
    }

    @Override
    public int getCount() {
        return mChatMessageList.size();
    }

    @Override
    public ChatMessage getItem(int position) {
        return mChatMessageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView(position, parent);
        }
        bindView(position, convertView);
        return convertView;
    }

    private View newView(int position, ViewGroup parent) {
        ChatMessage chatMessage = getItem(position);
        int type = chatMessage.getType();
        int layout = 0;

        switch (type) {
            case MessageType.EXT_TYPE_TIP:
                layout = R.layout.chatting_list_item_tip;
                break;
            default:
                break;
        }

        if (layout <= 0) {
            switch (chatMessage.getDirection()) {
                case ChatMessage.DIRECTION_SEND:
                    layout = R.layout.chatting_list_item_send;
                    break;
                case ChatMessage.DIRECTION_RECV:
                default:
                    layout = R.layout.chatting_list_item_recv;
                    break;
            }
        }

        return mInflater.inflate(layout, parent, false);
    }

    private void bindView(int position, View view) {
        final ChatMessage chatMessage = getItem(position);
        if (bindExtTypeView(chatMessage, view)) {
            return;
        }
        final ImageView avatarImage = (ImageView) view.findViewById(R.id.chatting_item_avatar);
        Bitmap avatarBitmap;
        boolean avatarSet = false;
        View divisionHeight = view.findViewById(R.id.division_height);

        switch (chatMessage.getDirection()) {
            case ChatMessage.DIRECTION_RECV:
                switch (mChatConversation) {
                    case PRIVATE:
                        avatarBitmap = mFriendAvatarBitmap;
                        break;
                    case GROUP: {
                        String memberAvatarUrl = getGroupMemberAvatarUrl(chatMessage);
                        MiscUtils.showAvatarThumb(mAvatarManager, memberAvatarUrl,
                                avatarImage, mDefaultAvatarIconId);
                        avatarBitmap = null;
                        avatarSet = true;
                        break;
                    }
                    case STRANGER_TEMP:
                        avatarBitmap = mFriendAvatarBitmap;
                        break;
                    case NORMAL_CHAT_ROOM: {
                        String memberAvatarUrl = getLiteStrangerAvatarUrl(
                                chatMessage.getSenderId());
                        MiscUtils.showAvatarThumb(mAvatarManager, memberAvatarUrl,
                                avatarImage, mDefaultAvatarIconId);
                        avatarBitmap = null;
                        avatarSet = true;
                        break;
                    }
                    default:
                        avatarBitmap = mFriendAvatarBitmap;
                        break;
                }
                break;
            case ChatMessage.DIRECTION_SEND:
            default:
                avatarBitmap = mSelfAvatarBitmap;
                break;
        }

        if (!avatarSet) {
            if (avatarBitmap != null) {
                avatarImage.setImageBitmap(avatarBitmap);
            } else {
                avatarImage.setImageResource(mDefaultAvatarIconId);
            }
        }

        int lastPosition = position - 1;
        ChatMessage prevChatMsg = (lastPosition >= 0) ? getItem(lastPosition) : null;
        mHelper.bindView(view, chatMessage, prevChatMsg);
        divisionHeight.setVisibility(prevChatMsg != null
                && chatMessage.getSenderId()
                .equals(prevChatMsg.getSenderId())
                ? View.GONE : View.VISIBLE);
    }

    private boolean bindExtTypeView(ChatMessage chatMessage, View view) {
        int type = chatMessage.getType();
        boolean bound = true;

        switch (type) {
            case MessageType.EXT_TYPE_TIP:
                bindTipView(chatMessage, view);
                break;
            default:
                bound = false;
                break;
        }

        return bound;
    }

    private void bindTipView(ChatMessage chatMessage, View view) {
        TextView tipView = (TextView) view.findViewById(R.id.tip);
        tipView.setText(chatMessage.getContent());
    }

    private String getSelfAvatarUrl(Context context) {
        SettingHelper helper = SettingHelper.getInstance(context);
        return helper.getAccountAvatarThumb();
    }

    private String getGroupMemberAvatarUrl(ChatMessage chatMessage) {
        TmpGroup tmpGroup = mTmpGroupManager.queryGroup(chatMessage.getTargetId());
        Stranger[] strangers = tmpGroup.getMembers();
        Stranger stranger = null;

        for (Stranger tmpStranger : strangers) {
            if (tmpStranger.getUserId().equals(chatMessage.getSenderId())) {
                stranger = tmpStranger;
                break;
            }
        }
        return (stranger != null) ? stranger.getAvatarThumb() : null;
    }

    private String getFriendAvatarUrl(String friendUserId) {
        final UserContact contact = mContactsManager.getUserContactByUserId(friendUserId);
        return (contact != null) ? contact.getAvatarThumb() : null;
    }

    private String getStrangerAvatarUrl(Context context, String userId) {
        final Stranger stranger = StrangerManager.getInstance(context)
                .getStranger(userId);
        return (stranger != null) ? stranger.getAvatarThumb() : null;
    }

    private String getLiteStrangerAvatarUrl(String userId) {
        String avatarUrl = getFriendAvatarUrl(userId);

        if (TextUtils.isEmpty(avatarUrl)) {
            final LiteStranger stranger = mLiteStrangerManager.getLiteStranger(userId);
            avatarUrl = (stranger != null) ? stranger.getAvatarThumb() : null;
        }

        return avatarUrl;
    }
}
