package com.ekuater.labelchat.ui.activity.chatting;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.labelchat.EnvConfig;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ChatBg;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.ChatRoom;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.ChatManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.delegate.NormalChatRoomManager;
import com.ekuater.labelchat.delegate.ShortUrlImageLoadListener;
import com.ekuater.labelchat.delegate.StrangerManager;
import com.ekuater.labelchat.delegate.ThemeManager;
import com.ekuater.labelchat.delegate.TmpGroupManager;
import com.ekuater.labelchat.delegate.imageloader.LoadFailType;
import com.ekuater.labelchat.notificationcenter.NotificationCenter;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.ui.fragment.friends.StrangerHelper;
import com.ekuater.labelchat.ui.util.CompatUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.widget.emoji.EmojiEditText;
import com.ekuater.labelchat.ui.widget.emoji.EmojiKeyboard;
import com.ekuater.labelchat.ui.widget.emoji.EmojiSelector;
import com.ekuater.labelchat.util.ACache;
import com.ekuater.labelchat.util.FileUtils;
import com.ekuater.labelchat.util.L;
import com.ekuater.labelchat.util.UniqueFileName;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChattingUI extends BackIconActivity {

    public static final String EXTRA_CONVERSATION_TYPE = "extra_conversation_type";
    public static final String EXTRA_TARGET_ID = "extra_target_id";
    public static final String EXTRA_ATTACHMENT = "extra_attachment";

    private static final String TAG = ChattingUI.class.getSimpleName();
    // When the conversation has a lot of messages and a new message is sent, the list is scrolled
    // so the user sees the just sent message. If we have to scroll the list more than 20 items,
    // then a scroll shortcut is invoked to move the list near the end before scrolling.
    private static final int MAX_ITEMS_TO_INVOKE_SCROLL_SHORTCUT = 20;

    // Any change in height in the message list view greater than this threshold will not
    // cause a smooth scroll. Instead, we jump the list directly to the desired position.
    private static final int SMOOTH_SCROLL_THRESHOLD = 200;

    private static final int REQUEST_SELECT_IMAGE = 102;

    private static final int MSG_GROUP_DISMISS_REMIND = 101;
    private static final int MSG_QUERY_MEMBERS_RESULT = 102;
    private static final int MSG_QUERY_MEMBER_COUNT_RESULT = 103;
    private static final int MSG_JOIN_CHAT_ROOM = 104;
    private static final int MSG_QUERY_CHAT_ROOM_MEMBERS = 105;

    private static final long QUERY_MEMBER_INTERVAL = 10 * 1000L;

    private final class LoadChatMessagesTask extends AsyncTask<Void, Void, Void> {

        private final List<ChatMessage> mChatMessageList;
        private int recvMsgCount = 0;
        private int sentMsgCount = 0;

        public LoadChatMessagesTask() {
            mChatMessageList = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (!TextUtils.isEmpty(mTargetId)) {
                final ChatMessage[] chatMessages
                        = mChatManager.getTargetChatMessages(mTargetId);
                if (chatMessages != null) {
                    Collections.addAll(mChatMessageList, chatMessages);
                }

                // Get sent and received chat message count;
                switch (mChatConversation) {
                    case STRANGER_TEMP:
                        recvMsgCount = mChatManager.getTargetChatMessageDirectionCount(
                                mTargetId, ChatMessage.DIRECTION_RECV);
                        sentMsgCount = mChatManager.getTargetChatMessageDirectionCount(
                                mTargetId, ChatMessage.DIRECTION_SEND);
                        if (recvMsgCount <= 0) { // Add sender tip
                            mChatMessageList.add(0, MessageType.newTipMessage(
                                    getString(R.string.temp_chat_send_tip, mTempChatMsgLimit),
                                    mMyUserId));
                        } else if (sentMsgCount <= 0) { // Add receiver tip
                            mChatMessageList.add(0, MessageType.newTipMessage(
                                    getString(R.string.temp_chat_recv_tip, mTempChatMsgLimit),
                                    mMyUserId));
                        }
                        break;
                    default:
                        recvMsgCount = 0;
                        sentMsgCount = 0;
                        break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mMsgListAdapter.updateChatMessages(mChatMessageList);
            if (mInitLoadMsg) {
                int lastItem = mMsgListAdapter.getCount() - 1;
                mInitLoadMsg = false;
                if (lastItem >= 0) {
                    mMsgListView.setSelection(lastItem);
                }
            } else if (mScrollOnSend) {
                smoothScrollToEnd(true, SMOOTH_SCROLL_THRESHOLD + 1);
                mScrollOnSend = false;
            }

            mRecvMsgCount = recvMsgCount;
            mSentMsgCount = sentMsgCount;
        }

        public void executeInThreadPool() {
            executeOnExecutor(THREAD_POOL_EXECUTOR, (Void) null);
        }
    }

    private TextView mTitleView;
    private ChatMsgListView mMsgListView;
    private ImageView mChatBgView;
    private ImageButton mFaceSwitchBtn;
    private Button mSendChatMsgBtn;
    private ImageButton mInputAttachBtn;
    private EmojiEditText mChatMsgEdit;
    private EmojiSelector mEmojiSelector;
    private ImageButton mRecordSwitchButton;
    private View mTextInputArea;
    private RecordButton mRecordButton;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private InputMethodManager mInputMethodManager;
    private boolean mIsFaceShow = false;
    private boolean mInRecordInput = false;
    private int mLastSmoothScrollPosition;
    private boolean mScrollOnSend;
    private boolean mInitLoadMsg;
    private ImageFactory mImageFactory;
    private MessageListAdapter mMsgListAdapter;
    private String mTargetId;
    private ChatConversation mChatConversation;
    private ChatManager mChatManager;
    private ContactsManager mContactsManager;
    private StrangerManager strangerManager;
    private NormalChatRoomManager mChatRoomManager;
    private long prevJoinChatRoomTime;
    private int mTempChatMsgLimit;
    private LoadChatMessagesTask mLoadTask;
    private String mGroupName;
    private ActionBar actionBar;
    private TextView mTextDown;
    private boolean isExpand = true;
    private boolean isAutoExpand = true;
    private int mCount = 0;
    private TmpGroupManager mTmpGroupManager;
    private GroupCountDown mGroupCountDown;
    private String mGroupId;
    private int mRecvMsgCount = 0;
    private int mSentMsgCount = 0;
    private String mMyUserId;
    private Handler mHandler;
    private MemberAdapter mMemberAdapter;
    private StrangerHelper mStrangerHelper;
    private long mPrevQueryMemberTime;
    private Object mAttachment;


    private View mMemberDocker;
    private boolean mMemberDockerShow;
    private float mShowThreshold;
    private GestureDetector mDockerGestureDetector;
    private GestureDetector.OnGestureListener mDockerGestureListener
            = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            hideMemberDocker();
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            if (e1 == null || e2 == null) {
                return false;
            }

            final float offsetX = e2.getX() - e1.getX();

            if (offsetX <= -mShowThreshold) {
                showMemberDocker();
            } else {
                hideMemberDocker();
            }

            return false;
        }
    };

    private TmpGroupManager.IListener mTmpGroupListener = new TmpGroupManager.AbsListener() {
        @Override
        public void onGroupDismissRemind(String groupId, long timeRemaining) {
            super.onGroupDismissRemind(groupId, timeRemaining);
            mHandler.sendEmptyMessage(MSG_GROUP_DISMISS_REMIND);
        }
    };
    private final Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean handled = true;

            switch (msg.what) {
                case MSG_GROUP_DISMISS_REMIND: {
                    mGroupCountDown.cancel();
                    TmpGroup tmpGroup = mTmpGroupManager.queryGroup(mGroupId);

                    if (tmpGroup != null) {
                        long countTime = tmpGroup.getExpireTime() - tmpGroup.getCreateTime()
                                - (System.currentTimeMillis() - tmpGroup.getLocalCreateTime());
                        mGroupCountDown = new GroupCountDown(countTime, 1000);
                        mGroupCountDown.start();
                        isExpand = true;
                        mTextDown.setVisibility(View.VISIBLE);
                        expandDownAnimation();
                        mTextDown.setTextColor(getResources().getColor(R.color.colorgroupdown));
                    }
                    break;
                }
                case MSG_QUERY_MEMBERS_RESULT:
                    handleQueryMembers((LiteStranger[]) msg.obj);
                    break;
                case MSG_QUERY_MEMBER_COUNT_RESULT:
                    handleQueryMemberCount(msg.arg2);
                    break;
                case MSG_JOIN_CHAT_ROOM:
                    handleJoinChatRoom();
                    break;
                case MSG_QUERY_CHAT_ROOM_MEMBERS:
                    queryChatRoomMember();
                    break;
                default:
                    handled = false;
                    break;
            }

            return handled;
        }
    };
    private final ChatManager.IListener mChatManagerListener = new ChatManager.AbsListener() {
        @Override
        public void onNewChatMessageReceived(ChatMessage chatMsg) {
            mScrollOnSend = true;
        }

        @Override
        public void onChatMessageDataChanged() {
            startQueryMessages();
        }
    };
    private final ContactsManager.IListener mContactsListener
            = new ContactsManager.AbsListener() {
        @Override
        public void onDeleteFriendResult(final int result, final String friendUserId,
                                         String friendLabelCode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mTargetId != null && mTargetId.equals(friendUserId)
                            && result == ConstantCode.CONTACT_OPERATION_SUCCESS) {
                        finish();
                        mChatManager.clearTargetMessage(friendUserId);
                    }
                }
            });
        }

        @Override
        public void onContactDefriendedMe(final String friendUserId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mTargetId != null && mTargetId.equals(friendUserId)) {
                        finish();
                        mChatManager.clearTargetMessage(friendUserId);
                    }
                }
            });
        }
    };
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.chatting_ui_msg_list_view:
                    mInputMethodManager.hideSoftInputFromWindow(mChatMsgEdit.getWindowToken(), 0);
                    showEmojiSelector(false);

                    if (mChatConversation == ChatConversation.NORMAL_CHAT_ROOM) {
                        return mDockerGestureDetector.onTouchEvent(event);
                    }
                    break;
                case R.id.chatting_ui_input_edit:
                    mInputMethodManager.showSoftInput(mChatMsgEdit, 0);
                    showEmojiSelector(false);
                    break;
                default:
                    break;
            }

            return false;
        }
    };
    private View.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.chatting_ui_face_switch_btn:
                    onFaceSwitchBtnClicked();
                    break;
                case R.id.chatting_ui_input_send_btn:
                    onSendChatMsgBtnClicked();
                    break;
                case R.id.chatting_ui_input_attach_btn:
                    onInputAttachBtnClicked();
                    break;
                case R.id.chatting_item_down_image:
                    onExpandDownImageClicked();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_ui);
        mScreenHeight = getScreenHeight();
        ImageView leftIcon = (ImageView) findViewById(R.id.icon);
        ImageView rightIcon = (ImageView) findViewById(R.id.right_icon);
        mTitleView = (TextView) findViewById(R.id.title);
        mHandler = new Handler(mHandlerCallback);
        mTmpGroupManager = TmpGroupManager.getInstance(this);
        mTmpGroupManager.registerListener(mTmpGroupListener);
        mContactsManager = ContactsManager.getInstance(this);
        mContactsManager.registerListener(mContactsListener);
        strangerManager = StrangerManager.getInstance(this);

        mChatRoomManager = NormalChatRoomManager.getInstance(this);
        prevJoinChatRoomTime = 0;
        mPrevQueryMemberTime = 0;
        mTempChatMsgLimit = getResources().getInteger(R.integer.temp_chat_msg_limit_count);
        mMyUserId = SettingHelper.getInstance(this).getAccountUserId();
        parseArguments();
        initResourceRefs();
        initData();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, mScreenHeight);
        mChatBgView.setLayoutParams(layoutParams);
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setupDetailIcon(rightIcon);
        mChatBgView.setMaxHeight(mScreenHeight);
        setChatBg(this);

    }

    int mScreenHeight;

    private int getScreenHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        return height;
    }

    private void setChatBg(Context context) {
        ThemeManager mThemeManager = ThemeManager.getInstance(context);
        SettingHelper settingHelper = SettingHelper.getInstance(context);
        ChatBg chatBg = settingHelper.getChatBg();

        final ACache aCache = ACache.get(this);
        final Bitmap bitmap = aCache.getAsBitmap("ChatBackground");
        if (bitmap != null) {
            mChatBgView.setImageBitmap(bitmap);
        } else {
            if (chatBg != null) {
                String imageName = chatBg.getBgImg();
                if (imageName != null) {
                    mThemeManager.getAvatarBitmap(imageName, new ShortUrlImageLoadListener() {
                        @Override
                        public void onLoadFailed(String shortUrl, LoadFailType loadFailType) {

                        }

                        @Override
                        public void onLoadComplete(String shortUrl, Bitmap loadedImage) {
                            mChatBgView.setImageBitmap(loadedImage);
                            aCache.put("ChatBackground", loadedImage);
                        }
                    });
                }
            }
        }

    }


    private void setupDetailIcon(ImageView detailIcon) {
        switch (mChatConversation) {
            case PRIVATE:
            case STRANGER_TEMP:
                detailIcon.setImageResource(R.drawable.ic_user_info);
                break;
            case GROUP:
                detailIcon.setImageResource(R.drawable.ic_group_friend);
                break;
            case NORMAL_CHAT_ROOM:
                detailIcon.setVisibility(View.GONE);
                break;
            default:
                break;
        }

        detailIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDetailMenuSelected();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMsgListAdapter.onResume();
        sendJoinChatRoomMessage();
        NotificationCenter.getInstance(this).enterChattingUIScenario();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMsgListAdapter.onPause();
        NotificationCenter.getInstance(this).exitChattingUIScenario();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deInit();
        mTmpGroupManager.unregisterListener(mTmpGroupListener);
        mContactsManager.unregisterListener(mContactsListener);

        switch (mChatConversation) {
            case NORMAL_CHAT_ROOM:
                mChatRoomManager.quitChatRoom(mTargetId);
                mChatRoomManager.sendQuitChatRoomMessage(mTargetId);
                mHandler.removeMessages(MSG_QUERY_CHAT_ROOM_MEMBERS);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                onSelectPictureResult(resultCode, data);
                break;
            default:
                break;
        }
    }

    private void onDetailMenuSelected() {
        switch (mChatConversation) {
            case PRIVATE:
                UILauncher.launchFriendDetailUI(this, mTargetId);
                break;
            case GROUP:
                UILauncher.launchGroupInformationUI(this, mTargetId, mGroupName);
                break;
            case STRANGER_TEMP:
                UILauncher.launchStrangerDetailUI(this,
                        strangerManager.getStranger(mTargetId));
                break;
            default:
                break;
        }
    }

    private void parseArguments() {
        Intent intent = getIntent();
        String targetId = intent.getStringExtra(EXTRA_TARGET_ID);
        int conversationType = intent.getIntExtra(EXTRA_CONVERSATION_TYPE, -1);

        L.v(TAG, "parseArguments(), targetId = " + targetId);
        if (TextUtils.isEmpty(targetId)) {
            L.v(TAG, "illegal friend label code, finish chatting activity now.");
            finish();
        }

        mTargetId = targetId;
        mAttachment = null;

        String title = null;

        do {
            if (conversationType == ChatMessage.CONVERSATION_NORMAL_CHAT_ROOM) {
                mChatConversation = ChatConversation.NORMAL_CHAT_ROOM;
                ChatRoom chatRoom = intent.getParcelableExtra(EXTRA_ATTACHMENT);
                title = chatRoom != null ? chatRoom.getChatRoomName() : "";
                mAttachment = chatRoom;
                break;
            }

            UserContact contact = mContactsManager.getUserContactByUserId(targetId);
            if (contact != null) {
                title = contact.getShowName();
                mChatConversation = ChatConversation.PRIVATE;
                break;
            }

            Stranger stranger = strangerManager.getStranger(targetId);
            if (stranger != null) {
                title = stranger.getShowName();
                mChatConversation = ChatConversation.STRANGER_TEMP;
                break;
            }

            TmpGroup tmpGroup = mTmpGroupManager.queryGroup(targetId);
            if (tmpGroup != null) {
                mGroupName = tmpGroup.getGroupName();
                mGroupId = tmpGroup.getGroupId();
                title = mGroupName;
                mChatConversation = ChatConversation.GROUP;

                long countTime = tmpGroup.getExpireTime() - tmpGroup.getCreateTime()
                        - (System.currentTimeMillis() - tmpGroup.getLocalCreateTime());
                if (countTime < tmpGroup.getDismissRemindTime()) {
                    mTextDown.setTextColor(getResources().getColor(R.color.colorgroupdown));
                }
                mGroupCountDown = new GroupCountDown(countTime, 1000);
                mGroupCountDown.start();
                break;
            }

            if (mChatConversation == null) {
                mChatConversation = ChatConversation.PRIVATE;
            }
            finish();
        } while (false);

        actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mTitleView.setText(title);

    }

    private class GroupCountDown extends CountDownTimer {

        public GroupCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            if (actionBar != null) {
                mTitleView.setText(mGroupName + "(" + "00:00:00" + ")");
            }
            finish();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (isAutoExpand) {
                mCount++;
                L.v(TAG, "isAutoExpand, ----------" + mCount + "-------");
                if (mCount == 9) {
                    L.v(TAG, "mCount, ----------" + mCount + "-------");
                    isAutoExpand = false;
                    isExpand = false;
                    mCount = 0;
                    mTextDown.setVisibility(View.GONE);
                    closeDownAnimation();
                }
            }
            int countTime = (int) millisUntilFinished / 1000;
            String seconds = String.valueOf((countTime) % 60).length() == 2 ? String.valueOf((countTime) % 60) : "0" + String.valueOf((countTime) % 60);
            String minutes = String.valueOf((countTime / 60) % 60).length() == 2 ? (String.valueOf((countTime / 60) % 60)) : "0" + (String.valueOf((countTime / 60) % 60));
            String hours = String.valueOf(countTime / 3600).length() == 2 ? String.valueOf((countTime / 3600)) : "0" + String.valueOf((countTime / 3600));
            mTextDown.setText(getResources().getString(R.string.surplus) + "(" + hours + ":" + minutes + ":" + seconds + ")");
        }
    }

    private void initResourceRefs() {
        mWindowLayoutParams = getWindow().getAttributes();
        mMsgListView = (ChatMsgListView) findViewById(R.id.chatting_ui_msg_list_view);
        mChatBgView = (ImageView) findViewById(R.id.chat_bg);
        mFaceSwitchBtn = (ImageButton) findViewById(R.id.chatting_ui_face_switch_btn);
        mSendChatMsgBtn = (Button) findViewById(R.id.chatting_ui_input_send_btn);
        mChatMsgEdit = (EmojiEditText) findViewById(R.id.chatting_ui_input_edit);
        mEmojiSelector = (EmojiSelector) findViewById(R.id.chatting_ui_input_emoji_layout);
        mTextInputArea = findViewById(R.id.text_input_area);
        mRecordButton = (RecordButton) findViewById(R.id.start_record);
        mRecordSwitchButton = (ImageButton) findViewById(R.id.chatting_ui_record_btn);
        mInputAttachBtn = (ImageButton) findViewById(R.id.chatting_ui_input_attach_btn);
        mTextDown = (TextView) findViewById(R.id.chatting_item_down_text);
        ImageView imageDown = (ImageView) findViewById(R.id.chatting_item_down_image);
        imageDown.setOnClickListener(mClickListener);
        RelativeLayout relativeDown = (RelativeLayout) findViewById(R.id.chatting_item_down_relative);

        mMsgListView.setOnTouchListener(mTouchListener);
        mMsgListView.setOnSizeChangedListener(new ChatMsgListView.OnSizeChangedListener() {
            @Override
            public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
                smoothScrollToEnd(false, height - oldHeight);
            }
        });

        mChatMsgEdit.setOnTouchListener(mTouchListener);
        mChatMsgEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                boolean _ret = false;

                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK: {
                        if ((mWindowLayoutParams.softInputMode
                                == WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                                || isFaceKeyboardShow()) {
                            showEmojiSelector(false);
                            _ret = true;
                        }
                        break;
                    }
                    default:
                }

                return _ret;
            }
        });
        mChatMsgEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean sendVisible = !TextUtils.isEmpty(s) && !mInRecordInput;
                updateSendChatMsgBtnVisibility(sendVisible);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mEmojiSelector.setOnEmojiClickedListener(new EmojiSelector.OnEmojiClickedListener() {
            @Override
            public void onEmojiClicked(String emoji) {
                EmojiKeyboard.input(mChatMsgEdit, emoji);
            }

            @Override
            public void onBackspace() {
                EmojiKeyboard.backspace(mChatMsgEdit);
            }
        });

        mFaceSwitchBtn.setOnClickListener(mClickListener);
        mSendChatMsgBtn.setOnClickListener(mClickListener);
        mInputAttachBtn.setOnClickListener(mClickListener);

        mRecordButton.setUserId(mTargetId);
        mRecordButton.setRecordListener(new RecordButton.IRecordListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onCanceled(int result) {
                ShowToast.makeText(ChattingUI.this, R.drawable.emoji_sad, getString(R.string.cancel_record_prompt_show)).show();
            }

            @Override
            public void onFinished(String recordFileName, long recordTime) {
                L.v(TAG, "IRecordListener::onFinished()"
                        + ",recordFileName=" + recordFileName
                        + ",recordTime" + recordTime);
                ChatMessage chatMessage = buildVoiceChatMessage(recordFileName, recordTime);
                sendChatMessageInternal(chatMessage);
            }

            @Override
            public void onFailure(int result) {
            }
        });
        mRecordSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecordSwitchBtnClicked();
            }
        });

        switch (mChatConversation) {
            case GROUP:
                relativeDown.setVisibility(View.VISIBLE);
                break;
            case NORMAL_CHAT_ROOM:
                mChatRoomManager.joinChatRoom(mTargetId);
                sendJoinChatRoomMessage();
                setupMemberDocker();
                break;
            default:
                break;
        }
    }

    private void initData() {
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mMsgListAdapter = new MessageListAdapter(this, mTargetId, mMsgListView, mChatConversation);
        mMsgListView.setAdapter(mMsgListAdapter);
        mChatManager = ChatManager.getInstance(this);
        mChatManager.registerListener(mChatManagerListener);
        mImageFactory = new ImageFactory(this, mTargetId);
        mInitLoadMsg = true;
        startQueryMessages();
    }

    private void translateMemberDocker(boolean show) {
        final int width = mMemberDocker.getWidth();
        final float fromX = mMemberDocker.getTranslationX();
        final float toX = show ? 0.0F : 1.0F * width;

        ObjectAnimator.ofFloat(mMemberDocker, "translationX", fromX, toX)
                .setDuration(200)
                .start();
    }

    private void showMemberDocker() {
        if (!mMemberDockerShow) {
            mMemberDockerShow = true;
            translateMemberDocker(true);
        }
    }

    private void hideMemberDocker() {
        if (mMemberDockerShow) {
            mMemberDockerShow = false;
            translateMemberDocker(false);
        }
    }

    private void setupMemberDocker() {
        mMemberAdapter = new MemberAdapter(this);
        mStrangerHelper = new StrangerHelper(this);
        mDockerGestureDetector = new GestureDetector(this, mDockerGestureListener);
        mMemberDocker = findViewById(R.id.member_docker);
        mMemberDocker.setVisibility(View.VISIBLE);
        mMemberDockerShow = true;
        mShowThreshold = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50,
                getResources().getDisplayMetrics());

        ListView listView = (ListView) mMemberDocker.findViewById(R.id.member_list);
        listView.setAdapter(mMemberAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if (item != null && item instanceof LiteStranger) {
                    showStranger(((LiteStranger) item).getUserId());
                }
            }
        });

        mMsgListView.setLongClickable(true);
    }

    private void showStranger(String userId) {
        if (mMyUserId.equals(userId)) {
            UILauncher.launchShowFriendAvatarImage(this,
                    SettingHelper.getInstance(this).getAccountAvatar());
        } else {
            mStrangerHelper.showStranger(userId);
        }
    }

    private void queryChatRoomMember() {
        final long currentTime = System.currentTimeMillis();

        if (mChatConversation == ChatConversation.NORMAL_CHAT_ROOM
                && (currentTime - mPrevQueryMemberTime) > QUERY_MEMBER_INTERVAL) {
            mPrevQueryMemberTime = currentTime;
            queryMembers(mTargetId);
            queryMemberCount(mTargetId);
            mHandler.removeMessages(MSG_QUERY_CHAT_ROOM_MEMBERS);
            mHandler.sendEmptyMessageDelayed(MSG_QUERY_CHAT_ROOM_MEMBERS, QUERY_MEMBER_INTERVAL);
        }
    }

    private void queryMembers(String chatRoomId) {
        mChatRoomManager.queryChatRoomMembers(chatRoomId,
                new NormalChatRoomManager.QueryMembersObserver() {
                    @Override
                    public void onQueryResult(int result, LiteStranger[] members) {
                        mHandler.sendMessage(mHandler.obtainMessage(
                                MSG_QUERY_MEMBERS_RESULT, result, 0, members));
                    }
                });
    }

    private void handleQueryMembers(LiteStranger[] members) {
        mMemberAdapter.updateMembers(members);
    }

    private void queryMemberCount(String chatRoomId) {
        mChatRoomManager.queryChatRoomMemberCount(chatRoomId,
                new NormalChatRoomManager.QueryMemberCountObserver() {
                    @Override
                    public void onQueryResult(int result, int count) {
                        mHandler.sendMessage(mHandler.obtainMessage(
                                MSG_QUERY_MEMBER_COUNT_RESULT, result, count));
                    }
                });
    }

    private void handleQueryMemberCount(int count) {
        ChatRoom chatRoom = (ChatRoom) mAttachment;

        if (chatRoom != null) {
            String title = chatRoom.getChatRoomName();

            if (count > 0) {
                title += "(" + count + ")";
            }
            mTitleView.setText(title);
        }
    }

    private void updateSendChatMsgBtnVisibility(boolean visible) {
        mSendChatMsgBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
        mInputAttachBtn.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    private synchronized void startQueryMessages() {
        if (mLoadTask != null && mLoadTask.getStatus() != AsyncTask.Status.FINISHED) {
            mLoadTask.cancel(true);
        }
        mLoadTask = new LoadChatMessagesTask();
        mLoadTask.executeInThreadPool();
    }

    private void deInit() {
        mChatManager.unregisterListener(mChatManagerListener);
    }

    private void showEmojiSelector(boolean show) {
        mEmojiSelector.setVisibility(show ? View.VISIBLE : View.GONE);
        mFaceSwitchBtn.setImageResource(show ? R.drawable.ic_input_keyboard_selector
                : R.drawable.ic_input_face_selector);
        mIsFaceShow = show;
    }

    private boolean isFaceKeyboardShow() {
        return mIsFaceShow;
    }

    @SuppressLint("NewApi")
    private void smoothScrollToEnd(boolean force, int listSizeChange) {
        int lastItemVisible = mMsgListView.getLastVisiblePosition();
        int lastItemInList = mMsgListAdapter.getCount() - 1;

        L.v(TAG, "smoothScrollToEnd()"
                + ",force=" + force
                + ",listSizeChange=" + listSizeChange
                + ",lastItemVisible=" + lastItemVisible
                + ",lastItemInList=" + lastItemInList);

        if (lastItemVisible < 0 || lastItemInList < 0) {
            return;
        }

        View lastChildVisible = mMsgListView.getChildAt(lastItemVisible
                - mMsgListView.getFirstVisiblePosition());
        int lastVisibleItemBottom = 0;
        int lastVisibleItemHeight = 0;
        if (lastChildVisible != null) {
            lastVisibleItemBottom = lastChildVisible.getBottom();
            lastVisibleItemHeight = lastChildVisible.getHeight();
        }

        int listHeight = mMsgListView.getHeight();
        boolean lastItemTooTall = lastVisibleItemHeight > listHeight;
        boolean willScroll = force ||
                ((listSizeChange != 0 || lastItemInList != mLastSmoothScrollPosition) &&
                        lastVisibleItemBottom + listSizeChange <=
                                listHeight - mMsgListView.getPaddingBottom());
        if (willScroll || (lastItemTooTall && lastItemInList == lastItemVisible)) {
            if (Math.abs(listSizeChange) > SMOOTH_SCROLL_THRESHOLD) {
                if (lastItemTooTall) {
                    mMsgListView.setSelectionFromTop(lastItemInList,
                            listHeight - lastVisibleItemHeight);
                } else {
                    mMsgListView.setSelection(lastItemInList);
                }
            } else if (lastItemInList - lastItemVisible > MAX_ITEMS_TO_INVOKE_SCROLL_SHORTCUT) {
                mMsgListView.setSelection(lastItemInList);
            } else {
                if (lastItemTooTall) {
                    mMsgListView.setSelectionFromTop(lastItemInList,
                            listHeight - lastVisibleItemHeight);
                } else {
                    mMsgListView.smoothScrollToPosition(lastItemInList);
                }
                mLastSmoothScrollPosition = lastItemInList;
            }
        }
    }

    private void onFaceSwitchBtnClicked() {
        if (isFaceKeyboardShow()) {
            mInputMethodManager.showSoftInput(mChatMsgEdit, 0);
            showEmojiSelector(false);
        } else {
            mInputMethodManager.hideSoftInputFromWindow(mChatMsgEdit.getWindowToken(), 0);
            showEmojiSelector(true);
        }
    }

    private void onExpandDownImageClicked() {
        if (isExpand) {
            isExpand = false;
            isAutoExpand = false;
            mCount = 0;
            mTextDown.setVisibility(View.GONE);
            closeDownAnimation();
        } else {
            isExpand = true;
            isAutoExpand = true;
            mCount = 0;
            mTextDown.setVisibility(View.VISIBLE);
            expandDownAnimation();
        }
    }

    private void closeDownAnimation() {
        AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation translateAnimation =
                new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f);
        translateAnimation.setDuration(1000);
        animationSet.addAnimation(translateAnimation);
        mTextDown.startAnimation(animationSet);
    }

    private void expandDownAnimation() {
        AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation translateAnimation =
                new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f);
        translateAnimation.setDuration(1000);
        animationSet.addAnimation(translateAnimation);
        mTextDown.startAnimation(animationSet);
    }

    private void onRecordSwitchBtnClicked() {
        mInRecordInput = !mInRecordInput;
        mTextInputArea.setVisibility(mInRecordInput ? View.GONE : View.VISIBLE);
        mRecordButton.setVisibility(mInRecordInput ? View.VISIBLE : View.GONE);
        updateSendChatMsgBtnVisibility(!mInRecordInput
                && !TextUtils.isEmpty(mChatMsgEdit.getText()));
        mRecordSwitchButton.setImageResource(mInRecordInput ? R.drawable.ic_keyboard_selector
                : R.drawable.ic_record_selector);
        if (mInRecordInput) {
            mInputMethodManager.hideSoftInputFromWindow(mChatMsgEdit.getWindowToken(), 0);
            showEmojiSelector(false);
        }
    }

    private void onSendChatMsgBtnClicked() {
        String chatText = mChatMsgEdit.getText().toString();
        if (chatText != null && chatText.length() > 0) {
            ChatMessage chatMessage = buildTextChatMessage(chatText);
            mChatMsgEdit.setText("");
            sendChatMessageInternal(chatMessage);
        }
    }

    private void onInputAttachBtnClicked() {
        UILauncher.launchMultiSelectImageUI(this, REQUEST_SELECT_IMAGE);
    }

    private ChatMessage buildChatMessage() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setDirection(ChatMessage.DIRECTION_SEND);
        chatMessage.setTargetId(mTargetId);
        chatMessage.setState(ChatMessage.STATE_SENDING);

        int conversation;
        switch (mChatConversation) {
            case GROUP:
                conversation = ChatMessage.CONVERSATION_GROUP;
                break;
            case NORMAL_CHAT_ROOM:
                conversation = ChatMessage.CONVERSATION_NORMAL_CHAT_ROOM;
                break;
            case PRIVATE:
            case STRANGER_TEMP:
            default:
                conversation = ChatMessage.CONVERSATION_PRIVATE;
                break;
        }
        chatMessage.setConversationType(conversation);
        return chatMessage;
    }

    private ChatMessage buildTextChatMessage(String chatText) {
        ChatMessage chatMessage = buildChatMessage();
        chatMessage.setType(ChatMessage.TYPE_TEXT);
        chatMessage.setContent(chatText);
        return chatMessage;
    }

    private ChatMessage buildVoiceChatMessage(String fileName, long time) {
        ChatMessage chatMessage = buildChatMessage();
        chatMessage.setType(ChatMessage.TYPE_VOICE);
        chatMessage.setContent(fileName);
        chatMessage.setPreview(String.valueOf(time));
        return chatMessage;
    }

    private ChatMessage buildImageChatMessage(String fileName, String previewFileName) {
        ChatMessage chatMessage = buildChatMessage();
        chatMessage.setType(ChatMessage.TYPE_IMAGE);
        chatMessage.setContent(fileName);
        chatMessage.setPreview(previewFileName);
        return chatMessage;
    }

    private void sendChatMessageInternal(ChatMessage chatMessage) {
        switch (mChatConversation) {
            case STRANGER_TEMP:
                if (mRecvMsgCount > 0 || mSentMsgCount < mTempChatMsgLimit) {
                    mChatManager.sendMessage(chatMessage, null);
                }
                break;
            case PRIVATE:
            case GROUP:
            default:
                mChatManager.sendMessage(chatMessage, null);
                break;
        }

        mScrollOnSend = true;
        sendJoinChatRoomMessage();
    }

    private void onSelectPictureResult(int resultCode, Intent data) {
        L.v(TAG, "onSelectPictureResult(),resultCode=%1$d,data=%2$s", resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String[] imagePaths = data.getStringArrayExtra("file_path");
            boolean isTemp = data.getBooleanExtra("isTemp", false);

            if (imagePaths != null && imagePaths.length > 0) {
                new PicSelectTask(imagePaths, isTemp).executeOnExecutor(
                        PicSelectTask.THREAD_POOL_EXECUTOR,
                        (String) null);
            }
        }
    }

    private void sendJoinChatRoomMessage() {
        final long currentTime = System.currentTimeMillis();

        if (mChatConversation == ChatConversation.NORMAL_CHAT_ROOM
                && (currentTime - prevJoinChatRoomTime) > (3 * 60 * 60 * 1000L)) {
            mChatRoomManager.sendJoinChatRoomMessage(mTargetId, new FunctionCallListener() {
                @Override
                public void onCallResult(int result, int errorCode, String errorDesc) {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_JOIN_CHAT_ROOM));
                }
            });
            prevJoinChatRoomTime = currentTime;
        }
    }

    private void handleJoinChatRoom() {
        queryChatRoomMember();
    }

    private final class PicSelectTask extends AsyncTask<String, String, Void> {

        private final String[] mImagePaths;
        private final boolean mIsTemp;

        public PicSelectTask(String[] imagePaths, boolean isTemp) {
            super();
            mImagePaths = imagePaths;
            mIsTemp = isTemp;
        }

        @Override
        protected Void doInBackground(String... params) {
            for (String path : mImagePaths) {
                if (TextUtils.isEmpty(path)) {
                    continue;
                }

                File src = new File(path);
                if (!src.exists() || !src.isFile()) {
                    continue;
                }

                File dest = getImageFile();
                FileUtils.copyFile(src, dest);
                publishProgress(dest.getName(), mImageFactory.generateThumbnailFile(
                        dest.getPath()));

                if (mIsTemp) {
                    if (src.delete()) {
                        L.v(TAG, "PicSelectTask.doInBackground(), delete temp file:" + path);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            sendChatMessageInternal(buildImageChatMessage(values[0], values[1]));
        }
    }

    private File getImageFile() {
        return new File(EnvConfig.getImageChatMsgDirectory(mTargetId),
                UniqueFileName.getUniqueFileName("jpg"));
    }
}
