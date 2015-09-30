package com.ekuater.labelchat.notificationcenter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.AddFriendAgreeResultMessage;
import com.ekuater.labelchat.datastruct.AddFriendRejectResultMessage;
import com.ekuater.labelchat.datastruct.BeenFollowedMessage;
import com.ekuater.labelchat.datastruct.BeenInvitedMessage;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.ConfideMessage;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.DynamicOperateMessage;
import com.ekuater.labelchat.datastruct.DynamicRemaindMessage;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.datastruct.InteractMessage;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LetterMessage;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.datastruct.PhotoNotifyMessage;
import com.ekuater.labelchat.datastruct.PushInteract;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserTagMessage;
import com.ekuater.labelchat.datastruct.ValidateAddFriendMessage;
import com.ekuater.labelchat.delegate.ChatManager;
import com.ekuater.labelchat.delegate.CoreEventBusHub;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.delegate.event.LoginEvent;
import com.ekuater.labelchat.guard.GuardConst;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.push.SystemPushUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.util.L;
import com.ekuater.labelchat.util.UriUtils;

import de.greenrobot.event.EventBus;

/**
 * Notification center service, deal with notifications, such as new chat message received,
 * new push message, etc.
 *
 * @author LinYong
 */
public class NotificationService extends Service implements INotificationMediator {

    private static final String TAG = NotificationService.class.getSimpleName();

    // Handler message id
    private static final int MSG_NEW_CHAT_MESSAGE_RECEIVED = 101;
    private static final int MSG_HANDLE_NEW_SYSTEM_PUSH_RECEIVED = 201;

    // Notification id
    private static final int NOTIFICATION_NEW_CHAT_MESSAGE = 101;
    public static final int NOTIFICATION_VALIDATE_ADD_FRIEND = 102;
    public static final int NOTIFICATION_AGREE_RESULT = 103;
    public static final int NOTIFICATION_REJECT_RESULT = 104;
    private static final int NOTIFICATION_LABEL_STORY_COMMENTS = 105;
    public static final int NOTIFICATION_PRIVATE_LETTER_NOTIFY = 106;
    private static final int NOTIFICATION_PHOTO_NOTIFY = 107;
    private static final int NOTIFICATION_BEEN_FOLLOWED = 108;
    private static final int NOTIFICATION_BEEN_INVITED = 109;
    private static final int NOTIFICATION_CONFIDE_NOTIFY = 110;
    public static final int NOTIFICATION_SYSTEM_NOTIFY = 111;

    private static class LocalBinder extends Binder implements INotificationMediator {

        private final INotificationMediator mMediator;

        public LocalBinder(INotificationMediator mediator) {
            mMediator = mediator;
        }

        @Override
        public void enterScenario(int scenario) {
            if (mMediator != null) {
                mMediator.enterScenario(scenario);
            }
        }

        @Override
        public void exitScenario(int scenario) {
            if (mMediator != null) {
                mMediator.exitScenario(scenario);
            }
        }
    }

    private NotificationManager mNotificationManager;
    private Binder mBinder;
    private Handler mHandler;
    private int mCurrentScenario;
    private ChatManager mChatManager;
    private PushMessageManager mPushMessageManager;
    private SettingHelper mSettingHelper;
    private EventBus mCoreEventBus;

    private final ChatManager.IListener mChatListener = new ChatManager.AbsListener() {
        @Override
        public void onNewChatMessageReceived(ChatMessage chatMsg) {
            sendNewChatMessageReceived(chatMsg);
        }
    };
    private final PushMessageManager.IListener mPushMessageListener
            = new PushMessageManager.AbsListener() {
        @Override
        public void onNewSystemPushReceived(SystemPush systemPush) {
            Message message = mHandler.obtainMessage(MSG_HANDLE_NEW_SYSTEM_PUSH_RECEIVED,
                    systemPush);
            mHandler.sendMessage(message);
        }
    };
    private final Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean handled = true;

            switch (msg.what) {
                case MSG_NEW_CHAT_MESSAGE_RECEIVED:
                    handleNewChatMessageReceived(msg.obj);
                    break;
                case MSG_HANDLE_NEW_SYSTEM_PUSH_RECEIVED:
                    handleNewSystemPushReceived((SystemPush) msg.obj);
                    break;
                default:
                    handled = false;
                    break;
            }

            return handled;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mBinder = new LocalBinder(this);
        mHandler = new Handler(mHandlerCallback);

        mChatManager = ChatManager.getInstance(this);
        mChatManager.registerListener(mChatListener);
        mSettingHelper = SettingHelper.getInstance(this);
        mPushMessageManager = PushMessageManager.getInstance(this);
        mPushMessageManager.registerListener(mPushMessageListener);
        mCoreEventBus = CoreEventBusHub.getDefaultEventBus();
        mCoreEventBus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mChatManager.unregisterListener(mChatListener);
        mPushMessageManager.unregisterListener(mPushMessageListener);
        mCoreEventBus.unregister(this);

        Intent intent = new Intent(GuardConst.ACTION_SERVICE_DEAD);
        intent.putExtra(GuardConst.EXTRA_SERVICE, GuardConst.SERVICE_NOTIFICATION);
        sendBroadcast(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void enterScenario(int scenario) {
        mCurrentScenario = scenario;

        switch (mCurrentScenario) {
            case SCENARIO_CHATTING_UI:
            case SCENARIO_MAIN_UI:
                cancelNewChatMessageNotification();
                break;
            case SCENARIO_NORMAL:
            default:
                break;
        }
    }

    @Override
    public void exitScenario(int scenario) {
        if (scenario == mCurrentScenario) {
            mCurrentScenario = SCENARIO_NORMAL;
        }
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(LoginEvent event) {
        if (event.getResult() == ConstantCode.ACCOUNT_OPERATION_USER_OR_PASSWORD_ERROR) {
            signInPrompt(event.getFrom());
        }
    }

    private void signInPrompt(LoginEvent.From from) {
        if (from == LoginEvent.From.FILE_UPLOADER
                && !TextUtils.isEmpty(mSettingHelper.getAccountLabelCode())
                && !TextUtils.isEmpty(mSettingHelper.getAccountPassword())) {
            return;
        }
        UILauncher.launchLoginPromptUI(this);
    }

    private void sendNewChatMessageReceived(ChatMessage chatMsg) {
        switch (chatMsg.getConversationType()) {
            case ChatMessage.CONVERSATION_LABEL_CHAT_ROOM:
            case ChatMessage.CONVERSATION_NORMAL_CHAT_ROOM:
                return;
            default:
                break;
        }

        Message message = mHandler.obtainMessage(MSG_NEW_CHAT_MESSAGE_RECEIVED, chatMsg);
        mHandler.sendMessage(message);
    }

    private void handleNewChatMessageReceived(Object object) {
        L.v(TAG, "handleNewChatMessageReceived()");

        if (object instanceof ChatMessage) {
            switch (mCurrentScenario) {
                case SCENARIO_CHATTING_UI:
                case SCENARIO_MAIN_UI:
                    break;
                case SCENARIO_NORMAL:
                default:
                    updateNewChatMessageNotification((ChatMessage) object);
                    break;
            }
        }
    }

    private void handleNewSystemPushReceived(SystemPush systemPush) {
        switch (systemPush.getType()) {
            case SystemPushType.TYPE_VALIDATE_ADD_FRIEND:
                handleValidateAddFriendPushMessage(systemPush);
                break;
            case SystemPushType.TYPE_ADD_FRIEND_AGREE_RESULT:
                handleAgreeResultNotify(systemPush);
                break;
            case SystemPushType.TYPE_ADD_FRIEND_REJECT_RESULT:
                handleRejectResultNotify(systemPush);
                break;
            case SystemPushType.TYPE_LOCAL_DYNAMIC_PRAISE_NOTIFY:
            case SystemPushType.TYPE_LOCAL_DYNAMIC_COMMENTS_NOTIFY:
                handleLabelStoryComments(systemPush);
                break;
            case SystemPushType.TYPE_PRIVATE_LETTER:
                handlePrivateLetterNotify(systemPush);
                break;
            default:
                handleSystemPushNotify(systemPush);
                break;
        }
    }

    private void handleValidateAddFriendPushMessage(SystemPush systemPush) {
        ValidateAddFriendMessage message = ValidateAddFriendMessage.build(systemPush);

        if (message != null) {
            notifyNotification(NOTIFICATION_VALIDATE_ADD_FRIEND,
                    getString(R.string.request_add_friend,
                            message.getStranger().getShowName()),
                    message.getValidateMessage(),
                    R.drawable.ic_add_friend,
                    systemPush.getTime(),
                    getValidateAddFriendUIIntent());
        }
    }

    private void handleAgreeResultNotify(SystemPush systemPush) {
        AddFriendAgreeResultMessage resultMessage = AddFriendAgreeResultMessage.build(systemPush);
        if (resultMessage != null) {
            UserContact contact = resultMessage.getContact();
            String name = contact != null ? contact.getShowName() : "";
            String message = resultMessage.getMessage() != null ? resultMessage.getMessage() : "";
            notifyNotification(NOTIFICATION_AGREE_RESULT,
                    getString(R.string.agree_add_as_friend, name),
                    message,
                    R.drawable.ic_add_friend,
                    systemPush.getTime(),
                    getValidateAgreeResultUIIntent(contact.getUserId()));
        }
    }

    private void handleRejectResultNotify(SystemPush systemPush) {
        AddFriendRejectResultMessage rejectResultMessage = AddFriendRejectResultMessage.build(systemPush);
        if (rejectResultMessage != null) {
            Stranger stranger = rejectResultMessage.getStranger();
            String name = stranger != null ? stranger.getShowName() : "";
            String message = rejectResultMessage.getMessage() != null ? rejectResultMessage.getMessage() : "";
            notifyNotification(NOTIFICATION_REJECT_RESULT,
                    getString(R.string.reject_add_as_friend, name),
                    message,
                    R.drawable.ic_add_friend,
                    systemPush.getTime(),
                    getValidateRejectResultUIIntent(systemPush.getId()));
        }
    }

    private void handlePrivateLetterNotify(SystemPush systemPush) {
        LetterMessage message = LetterMessage.build(systemPush);
        if (message != null && message.getTag() != 1) {
            Stranger stranger = message.getStranger();
            String name = "";
            if (stranger != null) {
                name = MiscUtils.getUserRemarkName(getBaseContext(), stranger.getUserId());
            }
            String nickname = name != null && name.length() > 0 ? name : stranger != null ? stranger.getNickname() : "";

            notifyNotification(NOTIFICATION_PRIVATE_LETTER_NOTIFY,
                    getString(R.string.letter_message),
                    getString(R.string.letter_receive_message, nickname),
                    R.drawable.ic_mail_receive,
                    systemPush.getTime(),
                    getPrivateLetterUIIntent(stranger.getUserId()));
        }
    }


    public void handleSystemPushNotify(SystemPush systemPush) {
        int icon = R.drawable.ic_app_team;
        String title = getString(R.string.unknown);
        String content = "";
        int type = SystemPushUtils.SYSTEM_PUSH_OTHER;
        switch (systemPush.getType()) {
            case SystemPushType.TYPE_LABEL_STORY_COMMENTS:
                DynamicOperateMessage dynamicOperateMessage = DynamicOperateMessage.build(systemPush);
                if (dynamicOperateMessage != null && dynamicOperateMessage.getMessagePlace().equals(DynamicOperateMessage.TYPE_MESSAGE_BOX)) {
                    Stranger stranger = dynamicOperateMessage.getStranger();
                    String name = "";
                    if (stranger != null) {
                        name = MiscUtils.getUserRemarkName(getBaseContext(), stranger.getUserId());
                    }
                    String nickname = name != null && name.length() > 0 ? name : stranger != null ? stranger.getNickname() : "";
                    switch (dynamicOperateMessage.getOperateType()) {
                        case DynamicOperateMessage.TYPE_OPERATE_COMMENT:
                            icon = R.drawable.ic_story_message;
                            title = getString(R.string.comment_notify);
                            content = TextUtils.isEmpty(dynamicOperateMessage.getReplyDynamicCommentContent())
                                    ? getString(R.string.someone_comment_you, nickname)
                                    : getString(R.string.someone_reply_your_comment, nickname);
                            type = SystemPushUtils.SYSTEM_PUSH_COMMENT;

                            break;
                        case DynamicOperateMessage.TYPE_OPERATE_PRAISE:
                            icon = R.drawable.ic_praise_remind;
                            title = getString(R.string.praise_notify);
                            content = getString(R.string.praise_message, nickname);
                            type = SystemPushUtils.SYSTEM_PUSH_COMMENT;
                            break;
                    }
                    notifyNotification(NOTIFICATION_SYSTEM_NOTIFY, title, content, icon,
                            systemPush.getTime(), getSystemNotifyUI(type));
                }
                break;
            case SystemPushType.TYPE_BEEN_FOLLOWED:
                BeenFollowedMessage beenFollowedMessage = BeenFollowedMessage.build(systemPush);
                if (beenFollowedMessage != null) {
                    FollowUser followUser = beenFollowedMessage.getFollowUser();
                    String name = "";
                    if (followUser != null) {
                        name = MiscUtils.getUserRemarkName(getBaseContext(), followUser.getUserId());
                    }
                    String nickname = name != null && name.length() > 0 ? name : followUser != null ? followUser.getNickname() : "";
                    content = getString(beenFollowedMessage.getFollowType() == 0 ? R.string.attention_notify_subTitle
                            : R.string.cancel_attention_notify_subTitle, nickname);
                    type = SystemPushUtils.SYSTEM_PUSH_REMIND;
                }
                notifyNotification(NOTIFICATION_SYSTEM_NOTIFY, getString(R.string.remind_notify), content, R.drawable.ic_msg_photo_saw,
                        systemPush.getTime(), getSystemNotifyUI(type));
                break;
            case SystemPushType.TYPE_PHOTO_NOTIFY:
                PhotoNotifyMessage photoNotifyMessage = PhotoNotifyMessage.build(systemPush);
                if (photoNotifyMessage != null) {
                    LiteStranger stranger = photoNotifyMessage.getNotifyUser();
                    String name = "";
                    if (stranger != null) {
                        name = MiscUtils.getUserRemarkName(getBaseContext(), stranger.getUserId());
                    }
                    String nickname = name != null && name.length() > 0 ? name : stranger != null ? stranger.getNickname() : "";
                    switch (photoNotifyMessage.getNotifyType()) {
                        case PhotoNotifyMessage.TYPE_HAS_SEEN:
                            icon = R.drawable.ic_msg_photo_saw;
                            title = getString(R.string.remind_notify);
                            content = getString(R.string.someone_saw_the_photo, nickname);
                            type = SystemPushUtils.SYSTEM_PUSH_REMIND;

                            break;
                        case PhotoNotifyMessage.TYPE_UPLOAD_MORE:
                            icon = R.drawable.ic_msg_photo_saw;
                            title = getString(R.string.remind_notify);
                            content = getString(R.string.someone_remind_upload_more, nickname);
                            type = SystemPushUtils.SYSTEM_PUSH_REMIND;

                            break;
                        case PhotoNotifyMessage.TYPE_HAS_PRAISE:
                            icon = R.drawable.ic_praise_remind;
                            title = getString(R.string.comment_notify);
                            content = getString(R.string.someone_praise_the_photo, nickname);
                            type = SystemPushUtils.SYSTEM_PUSH_PRAISE;
                            break;
                    }
                }
                notifyNotification(NOTIFICATION_SYSTEM_NOTIFY, title, content, icon,
                        systemPush.getTime(), getSystemNotifyUI(type));
                break;
            case SystemPushType.TYPE_BEEN_INVITED:
                BeenInvitedMessage beenInvitedMessage = BeenInvitedMessage.build(systemPush);
                if (beenInvitedMessage != null) {
                    LiteStranger stranger = beenInvitedMessage.getStranger();
                    String name = "";
                    if (stranger != null) {
                        name = MiscUtils.getUserRemarkName(getBaseContext(), stranger.getUserId());
                    }
                    String nickname = name != null && name.length() > 0 ? name : stranger != null ? stranger.getNickname() : "";
                    content = getString(R.string.new_invite_message, nickname);
                    type = SystemPushUtils.SYSTEM_PUSH_REMIND;

                }
                notifyNotification(NOTIFICATION_SYSTEM_NOTIFY, getString(R.string.remind_notify), content, R.drawable.ic_msg_photo_saw,
                        systemPush.getTime(), getSystemNotifyUI(type));
                break;
            case SystemPushType.TYPE_CONFIDE_COMMEND:
                ConfideMessage confideMessage = ConfideMessage.build(systemPush);
                if (confideMessage != null && confideMessage.getMessagePlace().equals(ConfideMessage.TYPE_MESSAGE_BOX)) {
                    Stranger stranger = confideMessage.getStranger();
                    String name = "";
                    if (stranger != null) {
                        name = MiscUtils.getUserRemarkName(getBaseContext(), stranger.getUserId());
                    }
                    String nickname = name != null && name.length() > 0 ? name : stranger != null ? stranger.getNickname() : "";
                    switch (confideMessage.getOperateType()) {
                        case ConfideMessage.TYPE_OPERATE_COMMENT:
                            icon = R.drawable.ic_story_message;
                            title = getString(R.string.comment_notify);
                            content = TextUtils.isEmpty(String.valueOf(confideMessage.getReplyCommentContent())) ?
                                    getString(R.string.confide_comment, nickname) :
                                    getString(R.string.confide_reply_comment, stranger != null ?
                                            getString(R.string.confide_floor, confideMessage.getReplyFloor()) : "");
                            type = SystemPushUtils.SYSTEM_PUSH_COMMENT;
                            break;
                        case ConfideMessage.TYPE_OPERATE_PRAISE:
                            icon = R.drawable.ic_praise_remind;
                            title = getString(R.string.praise_notify);
                            content = getString(R.string.confide_praise, nickname);
                            type = SystemPushUtils.SYSTEM_PUSH_PRAISE;

                            break;
                    }
                    notifyNotification(NOTIFICATION_SYSTEM_NOTIFY, title, content, icon,
                            systemPush.getTime(), getSystemNotifyUI(type));
                }
                break;
            case SystemPushType.TYPE_TAG_INTERACT:
                InteractMessage interactMessage = InteractMessage.build(systemPush);
                if (interactMessage != null) {
                    PushInteract pushInteract = interactMessage.getInteract();
                    String name = "";
                    if (pushInteract != null) {
                        name = MiscUtils.getUserRemarkName(getBaseContext(), pushInteract.getStranger().getUserId());
                    }
                    String nickname = name != null && name.length() > 0 ? name : pushInteract != null ?
                            pushInteract.getStranger() != null ? pushInteract.getStranger().getNickname() : "" : "";
                    content = getString(R.string.tag_operate, nickname,
                            pushInteract != null ? pushInteract.getInteractOperate() : "");
                    type = SystemPushUtils.SYSTEM_PUSH_REMIND;
                }
                notifyNotification(NOTIFICATION_SYSTEM_NOTIFY, getString(R.string.remind_notify), content, R.drawable.ic_msg_photo_saw,
                        systemPush.getTime(), getSystemNotifyUI(type));
                break;
            case SystemPushType.TYPE_CONFIDE_RECOMMEND:
                content = getString(R.string.confide_recommend);
                type = SystemPushUtils.SYSTEM_PUSH_REMIND;
                notifyNotification(NOTIFICATION_SYSTEM_NOTIFY, getString(R.string.remind_notify), content, R.drawable.ic_msg_photo_saw,
                        systemPush.getTime(), getSystemNotifyUI(type));
                break;
            case SystemPushType.TYPE_UPLOAD_PHOTO:
                PhotoNotifyMessage photoNotifyMessage2 = PhotoNotifyMessage.build(systemPush);

                if (photoNotifyMessage2 != null) {
                    LiteStranger stranger = photoNotifyMessage2.getNotifyUser();
                    String name = "";
                    if (stranger != null) {
                        name = MiscUtils.getUserRemarkName(getBaseContext(), stranger.getUserId());
                    }
                    String nickname = name != null && name.length() > 0 ? name : stranger != null ? stranger.getNickname() : "";
                    content = getString(R.string.new_photo, nickname);
                    type = SystemPushUtils.SYSTEM_PUSH_REMIND;
                }
                notifyNotification(NOTIFICATION_SYSTEM_NOTIFY, getString(R.string.remind_notify), content, R.drawable.ic_msg_photo_saw,
                        systemPush.getTime(), getSystemNotifyUI(type));
                break;
            case SystemPushType.TYPE_REMAIND_TAG:
                UserTagMessage userTagMessage = UserTagMessage.build(systemPush);
                if (userTagMessage != null) {
                    LiteStranger stranger = userTagMessage.getStranger();
                    String name = "";
                    if (stranger != null) {
                        name = MiscUtils.getUserRemarkName(getBaseContext(), stranger.getUserId());
                    }
                    String nickname = name != null && name.length() > 0 ? name : stranger != null ? stranger.getNickname() : "";
                    content = getString(R.string.usertag_remaind_message_name, nickname);
                    type = SystemPushUtils.SYSTEM_PUSH_REMIND;
                }
                notifyNotification(NOTIFICATION_SYSTEM_NOTIFY, getString(R.string.remind_notify), content, R.drawable.ic_msg_photo_saw,
                        systemPush.getTime(), getSystemNotifyUI(type));
                break;
            case SystemPushType.TYPE_REMAIND_INTEREST:
                InteractMessage interactMessage2 = InteractMessage.build(systemPush);
                if (interactMessage2 != null) {
                    PushInteract pushInteract = interactMessage2.getInteract();
                    if (pushInteract != null) {
                        LiteStranger stranger = pushInteract.getStranger();
                        String name = "";
                        if (stranger != null) {
                            name = MiscUtils.getUserRemarkName(getBaseContext(), stranger.getUserId());
                        }
                        String nickname = name != null && name.length() > 0 ? name : stranger != null ? stranger.getNickname() : "";
                        content = getString(R.string.interest_remaind_message_name,
                                nickname,
                                pushInteract.getInteractOperate() != null
                                        ? pushInteract.getInteractOperate()
                                        : getString(R.string.unknown));
                        type = SystemPushUtils.SYSTEM_PUSH_REMIND;
                    }
                }
                notifyNotification(NOTIFICATION_SYSTEM_NOTIFY, getString(R.string.remind_notify), content, R.drawable.ic_msg_photo_saw,
                        systemPush.getTime(), getSystemNotifyUI(type));
                break;
            case SystemPushType.TYPE_REMAIND_DYNAMIC:
                DynamicRemaindMessage dynamicRemaindMessage = DynamicRemaindMessage.build(systemPush);
                if (dynamicRemaindMessage != null) {
                    LiteStranger stranger = dynamicRemaindMessage.getStranger();
                    String name = "";
                    if (stranger != null) {
                        name = MiscUtils.getUserRemarkName(getBaseContext(), stranger.getUserId());
                    }
                    String nickname = name != null && name.length() > 0 ? name : stranger != null ? stranger.getNickname() : "";
                    switch (dynamicRemaindMessage.getLabelStory().getType()) {
                        case LabelStory.TYPE_AUDIO:
                            content = getString(R.string.voice_remaind_message_name, nickname);
                            break;
                        case LabelStory.TYPE_TXT_IMG:
                            content = getString(R.string.image_remaind_message_name, nickname);
                            break;
                        case LabelStory.TYPE_BANKNOTE:
                            content = getString(R.string.banknote_remaind_message_name, nickname);
                            break;
                    }
                    type = SystemPushUtils.SYSTEM_PUSH_REMIND;
                }
                notifyNotification(NOTIFICATION_SYSTEM_NOTIFY, getString(R.string.remind_notify), content, R.drawable.ic_msg_photo_saw,
                        systemPush.getTime(), getSystemNotifyUI(type));
                break;
            default:
                break;
        }
    }

    private void handleLabelStoryComments(SystemPush systemPush) {
        DynamicOperateMessage message = DynamicOperateMessage.build(systemPush);
        if (message != null) {
            Stranger stranger = message.getStranger();
            String nickname = (stranger != null ? stranger.getNickname() : "");
            String title;
            String content;
            int icon;
            if (DynamicOperateMessage.TYPE_OPERATE_COMMENT.equals(message.getOperateType())) {
                content = TextUtils.isEmpty(message.getReplyDynamicCommentContent())
                        ? getString(R.string.someone_comment_you, nickname)
                        : getString(R.string.someone_reply_your_comment, nickname);
                title = getString(R.string.labelstory_comment_message_title);
                icon = R.drawable.ic_story_message;

            } else {
                content = getString(R.string.dynamic_praise_title, nickname);
                title = getString(R.string.dynamic_praise_title);
                icon = R.drawable.ic_praise_remind;
            }

            notifyNotification(NOTIFICATION_LABEL_STORY_COMMENTS,
                    title,
                    content,
                    icon,
                    systemPush.getTime(),
                    getDynamicNotifyUIIntent(systemPush.getType()));
        }
    }

    private void handlePhotoNotify(SystemPush systemPush) {
        PhotoNotifyMessage message = PhotoNotifyMessage.build(systemPush);

        if (message != null) {
            LiteStranger user = message.getNotifyUser();
            String nickname = user != null ? user.getNickname() : "";
            String title;
            String content;
            int icon;
            switch (message.getNotifyType()) {
                case PhotoNotifyMessage.TYPE_HAS_SEEN:
                    title = getString(R.string.photo_notify_title);
                    content = getString(R.string.someone_saw_the_photo, nickname);
                    icon = R.drawable.ic_msg_photo_saw;
                    break;
                case PhotoNotifyMessage.TYPE_UPLOAD_MORE:
                    title = getString(R.string.photo_request_title);
                    content = getString(R.string.someone_remind_upload_more, nickname);
                    icon = R.drawable.ic_msg_photo_remind;
                    break;
                case PhotoNotifyMessage.TYPE_HAS_PRAISE:
                    title = getString(R.string.photo_praise);
                    content = getString(R.string.someone_praise_the_photo, nickname);
                    icon = R.drawable.ic_praise_remind;
                    break;
                default:
                    title = getString(R.string.unknown);
                    content = null;
                    icon = R.drawable.ic_app_team;
                    break;
            }
            notifyNotification(NOTIFICATION_PHOTO_NOTIFY,
                    title, content, icon, systemPush.getTime(),
                    getPhotoNotifyUIIntent(systemPush.getType()));
        }
    }

    private void handleConfideNotify(SystemPush systemPush) {
        ConfideMessage message = ConfideMessage.build(systemPush);
        if (message != null) {
            Stranger stranger = message.getStranger();
            String nickname = stranger != null ? stranger.getNickname() : "";
            String title;
            String content;
            int icon;
            if (ConfideMessage.TYPE_OPERATE_PRAISE.equals(message.getOperateType())) {
                title = getString(R.string.confide_praise_title);
                content = getString(R.string.confide_praise, nickname);
                icon = R.drawable.ic_confide_praise;
            } else {
                title = getString(R.string.confide_comment_title);
                content = TextUtils.isEmpty(message.getReplyCommentContent()) ?
                        getString(R.string.confide_comment, nickname) :
                        getString(R.string.confide_reply_comment, nickname);
                icon = R.drawable.ic_confide_comment;
            }
            notifyNotification(NOTIFICATION_CONFIDE_NOTIFY, title, content, icon, systemPush.getTime(),
                    getConfideNotifyUI(systemPush.getType()));
        }
    }

    private void handleBeenFollowed(SystemPush systemPush) {
        BeenFollowedMessage message = BeenFollowedMessage.build(systemPush);

        if (message != null) {
            FollowUser user = message.getFollowUser();
            String nickname = user != null ? user.getNickname() : "";

            notifyNotification(NOTIFICATION_BEEN_FOLLOWED,
                    getString(R.string.pay_attention),
                    getString(message.getFollowType() == 0 ? R.string.attention_notify_subTitle
                            : R.string.cancel_attention_notify_subTitle, nickname),
                    R.drawable.ic_follow_remind,
                    systemPush.getTime(),
                    getAttentionListUIIntent());
        }
    }

    private void handleBeenInvited(SystemPush systemPush) {
        BeenInvitedMessage message = BeenInvitedMessage.build(systemPush);

        if (message != null) {
            LiteStranger stranger = message.getStranger();
            String text = getString(R.string.new_invite_message,
                    stranger != null ? stranger.getNickname() : "");
            notifyNotification(NOTIFICATION_BEEN_INVITED,
                    getString(R.string.invitation),
                    text,
                    R.drawable.ic_invite_receive,
                    systemPush.getTime(),
                    getInviteUserUIIntent());
        }
    }

    private void updateNewChatMessageNotification(ChatMessage chatMessage) {
        L.v(TAG, "updateNewChatMessageNotification()");

        notifyNotification(NOTIFICATION_NEW_CHAT_MESSAGE,
                getString(R.string.receive_new_chat_message),
                getChatMessageContent(chatMessage),
                R.drawable.contact_single,
                chatMessage.getTime(),
                getChattingUiIntent(chatMessage));
    }

    private void cancelNewChatMessageNotification() {
        mNotificationManager.cancel(NOTIFICATION_NEW_CHAT_MESSAGE);
    }

    private String getChatMessageContent(ChatMessage chatMessage) {
        String content = getString(R.string.unknown);

        switch (chatMessage.getType()) {
            case ChatMessage.TYPE_TEXT:
                content = chatMessage.getContent();
                break;
            case ChatMessage.TYPE_VOICE:
                content = getString(R.string.voice_message);
                break;
            case ChatMessage.TYPE_IMAGE:
                content = getString(R.string.image_message);
                break;
            default:
                break;
        }

        return content;
    }

    private PendingIntent getEmptyPendingIntent() {
        return PendingIntent.getActivity(this, 0, new Intent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getValidateAddFriendUIIntent() {
        return NotificationIntent.getActivityNotificationIntent(this,
                UILauncher.getValidateAddFriendListUIIntent(this));
    }

    private PendingIntent getValidateAgreeResultUIIntent(String userId) {
        return NotificationIntent.getActivityNotificationIntent(this,
                UILauncher.getChattingUIIntent(this, userId));
    }

    private PendingIntent getValidateRejectResultUIIntent(long msgId) {
        return NotificationIntent.getActivityNotificationIntent(this, UILauncher.getRejectAddFriendUIIntent(this, msgId));
    }

    private PendingIntent getChattingUiIntent(ChatMessage chatMessage) {
        return NotificationIntent.getActivityNotificationIntent(this,
                UILauncher.getChattingUIIntent(this, chatMessage.getTargetId()));
    }

    private PendingIntent getPrivateLetterUIIntent(String flags) {
        return NotificationIntent.getActivityNotificationIntent(this,
                UILauncher.getLabelStoryLetterMsgUIIntent(this, flags));
    }

    public PendingIntent getSystemNotifyUI(int type) {
        return NotificationIntent.getActivityNotificationIntent(this,
                UILauncher.getSystemNotifyIntent(this, type));
    }

    private PendingIntent getDynamicNotifyUIIntent(int pushType) {
        return NotificationIntent.getActivityNotificationIntent(this,
                UILauncher.getDynamicNotifyUIIntent(this, pushType));
    }

    private PendingIntent getPhotoNotifyUIIntent(int pushType) {
        return NotificationIntent.getActivityNotificationIntent(this,
                UILauncher.getPhotoNotifyUIIntent(this, pushType));
    }

    private PendingIntent getAttentionListUIIntent() {
        return NotificationIntent.getActivityNotificationIntent(this,
                UILauncher.getAttentionListUIIntent(this));
    }

    private PendingIntent getInviteUserUIIntent() {
        return NotificationIntent.getActivityNotificationIntent(this,
                UILauncher.getInviteUserUIIntent(this));
    }

    private PendingIntent getConfideNotifyUI(int pushType) {
        return NotificationIntent.getActivityNotificationIntent(this,
                UILauncher.getConfideNotifyUIIntent(this, pushType));
    }

    private void notifyNotification(int id, CharSequence title, CharSequence text, int largeIcon,
                                    long when, PendingIntent intent) {
        notifyNotification(id, title, text, largeIcon, R.mipmap.ic_launcher, when, intent);
    }

    private void notifyNotification(int id, CharSequence title, CharSequence text, int largeIcon,
                                    int smallIcon, long when, PendingIntent intent) {
        notifyNotification(id, title, text, BitmapFactory.decodeResource(
                getResources(), largeIcon), smallIcon, when, intent);
    }

    private void notifyNotification(int id, CharSequence title, CharSequence text, Bitmap largeIcon,
                                    int smallIcon, long when, PendingIntent intent) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setLargeIcon(largeIcon);
        builder.setSmallIcon(smallIcon);
        builder.setWhen(when);
        builder.setContentIntent(intent);
        builder.setAutoCancel(true);

        if (mSettingHelper.getShakeSetting()) {
            builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        }
        if (mSettingHelper.getVoiceSetting()) {
            builder.setSound(UriUtils.getResourceUri(getResources(), R.raw.notify_knock));
        }
        mNotificationManager.notify(id, buildNotification(builder));
    }

    @SuppressWarnings("deprecation")
    private Notification buildNotification(Notification.Builder builder) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return builder.getNotification();
        } else {
            return builder.build();
        }
    }
}
