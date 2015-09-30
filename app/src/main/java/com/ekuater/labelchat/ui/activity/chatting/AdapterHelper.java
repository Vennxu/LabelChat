package com.ekuater.labelchat.ui.activity.chatting;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.EnvConfig;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.delegate.ChatManager;
import com.ekuater.labelchat.delegate.StrangerManager;
import com.ekuater.labelchat.delegate.TmpGroupManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.fragment.friends.StrangerHelper;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.widget.emoji.EmojiTextView;

import java.io.File;

/**
 * @author LinYong
 */
/*package*/ class AdapterHelper {

    private static final long TIME_GAP = 5 * 60 * 1000; // 5 minutes

    private static final class VoicePlayListener implements RecordPlayer.IPlayListener {

        private final View mPlayAnimView;
        private final View mAnimView;

        public VoicePlayListener(View view) {
            mPlayAnimView = view.findViewById(R.id.chatting_item_voice_play_anim);
            mAnimView = view.findViewById(R.id.chatting_item_voice_anim);
        }

        private void startAnimation() {
            if (mPlayAnimView != null) {
                mPlayAnimView.setVisibility(View.GONE);
            }
            if (mAnimView != null) {
                mAnimView.setVisibility(View.VISIBLE);
            }
        }

        private void stopAnimation() {
            if (mPlayAnimView != null) {
                mPlayAnimView.setVisibility(View.VISIBLE);
            }
            if (mAnimView != null) {
                mAnimView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPlayStarted() {
            startAnimation();
        }

        @Override
        public void onPlayCompleted() {
            stopAnimation();
        }

        @Override
        public void onPlayStopped() {
            stopAnimation();
        }

        @Override
        public void onPlayError() {
            stopAnimation();
        }
    }

    private static String getTimeString(Context context, long milliseconds) {
        return DateTimeUtils.getTimeString(context, milliseconds);
    }

    private static String getVoiceTime(long time) {
        long seconds = Math.max((time / 1000), 1);
        long min = seconds / 60;
        long sec = seconds % 60;

        return ((min > 0) ? (String.valueOf(min) + "'") : "")
                + ((sec >= 0) ? (String.valueOf(sec) + "\"") : "");
    }

    private final View.OnClickListener mTextClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Do nothing
        }
    };
    private final View.OnClickListener mVoiceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // play the voice message
            Object tag = v.getTag();

            if (tag != null && tag instanceof ChatMessage) {
                ChatMessage chatMessage = (ChatMessage) tag;
                if (chatMessage.getType() == ChatMessage.TYPE_VOICE) {
                    mRecordPlayer.play(chatMessage.getContent(), new VoicePlayListener(v));
                }
            }
        }
    };
    private final View.OnClickListener mImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object tag = v.getTag();

            if (tag != null && tag instanceof ChatMessage) {
                ChatMessage chatMessage = (ChatMessage) tag;
                if (chatMessage.getType() == ChatMessage.TYPE_IMAGE) {
                    Uri uri = Uri.fromFile(new File(mChatImageDir, chatMessage.getContent()));
                    UILauncher.launchImageViewUI(v.getContext(), uri);
                }
            }
        }
    };
    private final View.OnClickListener mSendFailedClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object tag = v.getTag();

            if (tag instanceof ChatMessage) {
                ChatMessage chatMessage = (ChatMessage) tag;
                showResentConfirm(chatMessage);
            }
        }
    };

    private final class OnMenuItemClickListener implements MenuItem.OnMenuItemClickListener {

        private final ChatMessage mChatMessage;

        public OnMenuItemClickListener(View view) {
            mChatMessage = getChatMessage(view);
        }

        private ChatMessage getChatMessage(View view) {
            Object tag = view.getTag();
            ChatMessage chatMessage = null;

            if (tag != null && tag instanceof ChatMessage) {
                chatMessage = (ChatMessage) tag;
            }
            return chatMessage;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            boolean _ret = true;

            switch (item.getItemId()) {
                case R.id.chatting_item_delete_chat_message: {
                    if (mChatMessage != null) {
                        deleteChatMessage(mChatMessage.getId());
                    }
                    break;
                }
                case R.id.chatting_content_copy:
                    copyFrotv(mChatMessage);
                    break;
                default:
                    _ret = false;
                    break;
            }
            return _ret;
        }
    }

    private final FragmentActivity mActivity;
    private final RecordPlayer mRecordPlayer;
    private final ImageFactory mImageFactory;
    private final File mChatImageDir;
    private final ChatManager mChatManager;
    private ClipboardManager mClipboard;
    private final MenuInflater mMenuInflater;
    private TmpGroupManager mTmpGroupManager;
    private ChatConversation mChatConversation;
    private StrangerManager mStrangerManager;
    private StrangerHelper mStrangerHelper;
    private Point mImageItemMaxSize;

    public AdapterHelper(FragmentActivity activity, String targetId,
                         ChatConversation conversation) {
        Resources res = activity.getResources();
        mActivity = activity;
        mChatConversation = conversation;
        mRecordPlayer = new RecordPlayer(activity, targetId);
        mImageFactory = new ImageFactory(activity, targetId);
        mChatImageDir = EnvConfig.getImageChatMsgDirectory(targetId);
        mChatManager = ChatManager.getInstance(activity);
        mMenuInflater = new MenuInflater(activity);
        mTmpGroupManager = TmpGroupManager.getInstance(activity);
        mStrangerManager = StrangerManager.getInstance(activity);
        mStrangerHelper = new StrangerHelper(activity);
        mImageItemMaxSize = new Point(res.getDimensionPixelSize(R.dimen.image_msg_item_max_width),
                res.getDimensionPixelSize(R.dimen.image_msg_item_max_height));
    }

    public synchronized void onPause() {
        mRecordPlayer.stop();
    }

    public synchronized void onResume() {
    }

    private void copyFrotv(ChatMessage cm) {
        if (null == mClipboard) {
            mClipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
        }
        ClipData clip = ClipData.newPlainText("editText", cm.getContent());
        mClipboard.setPrimaryClip(clip);
    }

    public void bindView(View view, ChatMessage chatMessage, ChatMessage prevChatMessage) {
        switch (chatMessage.getType()) {
            case ChatMessage.TYPE_TEXT:
                bindTextView(view, chatMessage);
                break;
            case ChatMessage.TYPE_VOICE:
                bindVoiceView(view, chatMessage);
                break;
            case ChatMessage.TYPE_IMAGE:
                bindImageView(view, chatMessage);
                break;
            default:
                break;
        }

        // Treat message time show
        TextView timeText = (TextView) view.findViewById(R.id.chatting_item_datetime);
        long timeDiff = (prevChatMessage != null) ? chatMessage.getTime()
                - prevChatMessage.getTime() : TIME_GAP + 1;
        timeText.setText(getTimeString(view.getContext(), chatMessage.getTime()));
        timeText.setVisibility(timeDiff > TIME_GAP ? View.VISIBLE : View.GONE);

        treatMessageState(view, chatMessage);
    }

    public void unbindView(View view) {
        // remove all listener when unbind view
        unsetViewListener(view);
    }

    private void bindTextView(View view, ChatMessage chatMessage) {
        // inflate the real view by ViewStub first.
        ViewStub stub = (ViewStub) view.findViewById(R.id.chatting_item_emoji_message_block_stub);
        if (stub != null) {
            stub.inflate();
        }

        EmojiTextView contentText = (EmojiTextView) view.findViewById(
                R.id.chatting_item_emoji_message_block);
        contentText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean ret = false;
                CharSequence text = ((TextView) v).getText();
                Spannable stext = Spannable.Factory.getInstance().newSpannable(text);
                TextView widget = (TextView) v;
                int action = event.getAction();
                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    x -= widget.getTotalPaddingLeft();
                    y -= widget.getTotalPaddingTop();
                    x += widget.getScrollX();
                    y += widget.getScrollY();
                    Layout layout = widget.getLayout();
                    int line = layout.getLineForVertical(y);
                    int off = layout.getOffsetForHorizontal(line, x);
                    ClickableSpan[] link = stext.getSpans(off, off, ClickableSpan.class);
                    if (link.length != 0) {
                        if (action == MotionEvent.ACTION_UP) {
                            link[0].onClick(widget);
                        }
                        ret = true;
                    }
                }
                return ret;
            }
        });
        contentText.setText(chatMessage.getContent());
        contentText.setAutoLinkMask(Linkify.ALL);
        contentText.setMovementMethod(LinkMovementMethod.getInstance());
        setViewListener(view, chatMessage);
    }

    private void bindVoiceView(View view, ChatMessage chatMessage) {
        // inflate the real view by ViewStub first.
        ViewStub stub = (ViewStub) view.findViewById(R.id.chatting_item_voice_message_block_stub);
        if (stub != null) {
            stub.inflate();
        }

        TextView voiceTimeText = (TextView) view.findViewById(R.id.chatting_item_voice_time_text);
        voiceTimeText.setText(getVoiceTime(Long.valueOf(chatMessage.getPreview())));
        setViewListener(view, chatMessage);
    }

    private void bindImageView(View view, ChatMessage chatMessage) {
        // inflate the real view by ViewStub first.
        ViewStub stub = (ViewStub) view.findViewById(R.id.chatting_item_image_message_block_stub);
        if (stub != null) {
            stub.inflate();
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.chatting_item_image_message_block);
        Bitmap bitmap = mImageFactory.decodeThumbnail(chatMessage.getPreview());
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        if (bitmap != null) {
            Point maxSize = mImageItemMaxSize;
            float scale = Math.max(bitmap.getWidth() / (float) maxSize.x,
                    bitmap.getHeight() / (float) maxSize.y);
            if (scale > 0) {
                lp.width = (int) (bitmap.getWidth() / scale);
                lp.height = (int) (bitmap.getHeight() / scale);
            } else {
                lp.width = bitmap.getWidth();
                lp.height = bitmap.getHeight();
            }
            imageView.setLayoutParams(lp);
            imageView.setImageBitmap(bitmap);
        } else {
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            imageView.setLayoutParams(lp);
            imageView.setImageResource(R.drawable.chatting_item_image_message_image_fail);
        }
        setViewListener(view, chatMessage);
    }

    private void setViewListener(View view, ChatMessage chatMessage) {
        View contentView = view.findViewById(R.id.chatting_item_content);

        if (contentView == null) {
            return;
        }

        switch (chatMessage.getType()) {
            case ChatMessage.TYPE_TEXT: {
                contentView.setOnClickListener(mTextClickListener);
                break;
            }
            case ChatMessage.TYPE_VOICE: {
                contentView.setOnClickListener(mVoiceClickListener);
                break;
            }
            case ChatMessage.TYPE_IMAGE: {
                contentView.setOnClickListener(mImageClickListener);
                break;
            }
            default: {
                contentView.setOnClickListener(null);
                contentView.setOnLongClickListener(null);
                break;
            }
        }

        contentView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                mMenuInflater.inflate(R.menu.chatting_item_long_click_menu, menu);
                MenuItem copyText = menu.findItem(R.id.chatting_content_copy);
                MenuItem deleteText = menu.findItem(R.id.chatting_item_delete_chat_message);
                ChatMessage chatMessage = (ChatMessage) v.getTag();

                if (chatMessage.getType() == ChatMessage.TYPE_TEXT) {
                    copyText.setVisible(true);
                    copyText.setOnMenuItemClickListener(new OnMenuItemClickListener(v));
                } else {
                    copyText.setVisible(false);
                }
                deleteText.setOnMenuItemClickListener(new OnMenuItemClickListener(v));
            }
        });

        contentView.setTag(chatMessage);
        View avatarImage = view.findViewById(R.id.chatting_item_avatar);
        avatarImage.setTag(chatMessage);
        avatarImage.setOnClickListener(mAvatarClickListener);
    }

    private final View.OnClickListener mAvatarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ChatMessage chatMsg = (ChatMessage) v.getTag();

            switch (chatMsg.getDirection()) {
                case ChatMessage.DIRECTION_RECV:
                    showRecvMsgSenderDetail(chatMsg);
                    break;
                case ChatMessage.DIRECTION_SEND:
                default:
                    UILauncher.launchMyInfoUI(mActivity);
                    break;
            }
        }

        private void showRecvMsgSenderDetail(ChatMessage chatMsg) {
            switch (mChatConversation) {
                case PRIVATE:
                    UILauncher.launchFriendDetailUI(mActivity, chatMsg.getTargetId());
                    break;
                case STRANGER_TEMP: {
                    Stranger stranger = mStrangerManager.getStranger(chatMsg.getTargetId());
                    if (stranger != null) {
                        UILauncher.launchStrangerDetailUI(mActivity, stranger);
                    }
                    break;
                }
                case GROUP: {
                    TmpGroup tmpGroup = mTmpGroupManager.queryGroup(chatMsg.getTargetId());
                    if (tmpGroup != null) {
                        Stranger[] strangers = tmpGroup.getMembers();
                        String senderId = chatMsg.getSenderId();
                        Stranger stranger = null;

                        if (strangers != null && strangers.length > 0) {
                            for (Stranger tmpStranger : strangers) {
                                if (tmpStranger != null && senderId.equals(
                                        tmpStranger.getUserId())) {
                                    stranger = tmpStranger;
                                    break;
                                }
                            }
                        }

                        if (stranger != null) {
                            UILauncher.launchStrangerDetailUI(mActivity, stranger);
                        }
                    }
                    break;
                }
                case NORMAL_CHAT_ROOM:
                    mStrangerHelper.showStranger(chatMsg.getSenderId());
                    break;
                default:
                    break;
            }
        }
    };

    private void unsetViewListener(View view) {
        View contentView = view.findViewById(R.id.chatting_item_content);

        if (contentView != null) {
            contentView.setOnClickListener(null);
            contentView.setOnLongClickListener(null);
            contentView.setTag(null);
            contentView.setOnCreateContextMenuListener(null);

            View failedView = contentView.findViewById(R.id.chatting_item_send_state_send_failed);
            if (failedView != null) {
                failedView.setOnClickListener(null);
                failedView.setTag(null);
            }

            View avatarView = view.findViewById(R.id.chatting_item_avatar);
            if (avatarView != null) {
                avatarView.setTag(null);
            }
        }
    }

    private void treatMessageState(View view, ChatMessage chatMessage) {
        switch (chatMessage.getDirection()) {
            case ChatMessage.DIRECTION_RECV:
                updateMessageRead(chatMessage);
                break;
            case ChatMessage.DIRECTION_SEND:
                showMessageState(view, chatMessage);
                break;
            default:
                break;
        }
    }

    private void updateMessageRead(ChatMessage chatMessage) {
        if (chatMessage.getState() != ChatMessage.STATE_READ) {
            final int newState = ChatMessage.STATE_READ;
            mChatManager.updateChatMessageState(chatMessage.getId(), newState);
            chatMessage.setState(newState);
        }
    }

    private void showMessageState(View view, ChatMessage chatMessage) {
        final int state = chatMessage.getState();
        final boolean show = (state == ChatMessage.STATE_SENDING)
                || (state == ChatMessage.STATE_SEND_FAILED);

        if (show) {
            ViewStub stub = (ViewStub) view.findViewById(R.id.chatting_item_send_state_stub);
            if (stub != null) {
                stub.inflate();
            }

            ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.chatting_item_send_state);
            if (viewGroup != null) {
                viewGroup.setVisibility(View.VISIBLE);
                for (int i = 0; i < viewGroup.getChildCount(); ++i) {
                    viewGroup.getChildAt(i).setVisibility(View.GONE);
                }

                switch (state) {
                    case ChatMessage.STATE_SENDING:
                        viewGroup.findViewById(R.id.chatting_item_send_state_sending)
                                .setVisibility(View.VISIBLE);
                        break;
                    case ChatMessage.STATE_SEND_FAILED: {
                        View failedView = viewGroup.findViewById(
                                R.id.chatting_item_send_state_send_failed);
                        failedView.setVisibility(View.VISIBLE);
                        failedView.setTag(chatMessage);
                        failedView.setOnClickListener(mSendFailedClickListener);
                        break;
                    }
                    default:
                        viewGroup.setVisibility(View.GONE);
                        break;
                }
            }
        } else {
            ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.chatting_item_send_state);
            if (viewGroup != null) {
                viewGroup.setVisibility(View.GONE);
            }
        }
    }

    private void deleteChatMessage(long messageId) {
        mChatManager.deleteMessage(messageId);
    }

    private void resendMessage(long messageId) {
        mChatManager.resendMessage(messageId, null);
    }

    private class ResendConfirmListener extends ConfirmDialogFragment.AbsConfirmListener {

        private final ChatMessage mChatMessage;

        public ResendConfirmListener(ChatMessage chatMessage) {
            mChatMessage = chatMessage;
        }

        @Override
        public void onConfirm() {
            resendMessage(mChatMessage.getId());
        }
    }

    private void showResentConfirm(ChatMessage chatMessage) {
        ConfirmDialogFragment.UiConfig uiConfig
                = new ConfirmDialogFragment.UiConfig(
                mActivity.getString(R.string.resend_chat_message_confirm),
                null);
        ConfirmDialogFragment.IConfirmListener confirmListener
                = new ResendConfirmListener(chatMessage);
        ConfirmDialogFragment fragment = ConfirmDialogFragment.newInstance(uiConfig,
                confirmListener);
        fragment.show(mActivity.getSupportFragmentManager(), "ResendConfirm");
    }
}
