package com.ekuater.labelchat.coreservice.immediator;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.ekuater.labelchat.EnvConfig;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.account.UpdateRongCloudTokenCommand;
import com.ekuater.labelchat.coreservice.ICoreServiceCallback;
import com.ekuater.labelchat.coreservice.command.ICommandResponseHandler;
import com.ekuater.labelchat.coreservice.utils.TaskExecutor;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.util.ChatThumbnailFactory;
import com.ekuater.labelchat.util.FileUtils;
import com.ekuater.labelchat.util.L;
import com.ekuater.labelchat.util.UniqueFileName;

import org.json.JSONException;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.RongIMClient;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * RongCloud IM mediator
 *
 * @author LinYong
 */
public class RongIMMediator extends BaseIMMediator {

    private static final String TAG = RongIMMediator.class.getSimpleName();

    private static final String ACTION_RECEIVE_PUSH_MESSAGE
            = "labelchat.intent.action.ACTION_RECEIVE_PUSH_MESSAGE";
    private static final String FIELD_PUSH_ID = CommandFields.Normal.PUSH_ID;

    private static final String NORMAL_CHAT_ROOM_PREFIX = "LTS_";

    private static final int MSG_HANDLE_CONNECT_SUCCESS = 100;
    private static final int MSG_HANDLE_CONNECT_ERROR = 101;
    private static final int MSG_HANDLE_UPDATE_TOKEN_RESULT = 102;
    private static final int MSG_HANDLE_RECEIVE_MESSAGE = 110;
    private static final int MSG_HANDLE_DOWNLOAD_MESSAGE_DONE = 111;
    private static final int MSG_HANDLE_CHAT_MESSAGE_SEND_RESULT = 120;
    private static final int MSG_HANDLE_CONNECTION_STATUS_CHANGED = 130;

    private class ProcessHandler extends Handler {

        public ProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_CONNECT_SUCCESS:
                    handleConnectSuccess((String) msg.obj);
                    break;
                case MSG_HANDLE_CONNECT_ERROR:
                    handleConnectError((RongIMClient.ConnectCallback.ErrorCode) msg.obj);
                    break;
                case MSG_HANDLE_UPDATE_TOKEN_RESULT:
                    handleUpdateTokenResult((CommandResult) msg.obj);
                    break;
                case MSG_HANDLE_RECEIVE_MESSAGE:
                    handleReceiveMessage((RongIMClient.Message) msg.obj, msg.arg1);
                    break;
                case MSG_HANDLE_CHAT_MESSAGE_SEND_RESULT:
                    handleChatMessageSendResult((SendResult) msg.obj);
                    break;
                case MSG_HANDLE_DOWNLOAD_MESSAGE_DONE:
                    handleDownloadMessageDone((ChatMessage) msg.obj, msg.arg1);
                    break;
                case MSG_HANDLE_CONNECTION_STATUS_CHANGED:
                    handleConnectionStatusChanged(
                            (RongIMClient.ConnectionStatusListener.ConnectionStatus) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private final Handler mHandler;
    private final TaskExecutor mTaskExecutor;
    private final ChatThumbnailFactory mThumbnailFactory;
    private final AtomicBoolean mConnected;
    private final SettingHelper mSettingHelper;
    private RongIMClient mIMClient;

    private final RongIMClient.ConnectCallback mConnectCallback
            = new RongIMClient.ConnectCallback() {
        @Override
        public void onSuccess(String userId) {
            Message msg = mHandler.obtainMessage(MSG_HANDLE_CONNECT_SUCCESS, userId);
            mHandler.sendMessage(msg);
        }

        @Override
        public void onError(ErrorCode errorCode) {
            Message msg = mHandler.obtainMessage(MSG_HANDLE_CONNECT_ERROR, errorCode);
            mHandler.sendMessage(msg);
        }
    };
    private final RongIMClient.OnReceiveMessageListener mReceiveMessageListener
            = new RongIMClient.OnReceiveMessageListener() {
        @Override
        public void onReceived(RongIMClient.Message message, int left) {
            Message msg = mHandler.obtainMessage(MSG_HANDLE_RECEIVE_MESSAGE, left, 0, message);
            mHandler.sendMessage(msg);
        }
    };
    private final RongIMClient.ConnectionStatusListener mConnectionStatusListener
            = new RongIMClient.ConnectionStatusListener() {
        @Override
        public void onChanged(ConnectionStatus connectionStatus) {
            Message msg = mHandler.obtainMessage(MSG_HANDLE_CONNECTION_STATUS_CHANGED,
                    connectionStatus);
            mHandler.sendMessage(msg);
        }
    };

    public RongIMMediator(Context context, ICoreServiceCallback callback) {
        super(context, callback);
        mHandler = new ProcessHandler(mCallback.getProcessLooper());
        mTaskExecutor = TaskExecutor.getInstance();
        mThumbnailFactory = new ChatThumbnailFactory(mContext);
        mConnected = new AtomicBoolean(false);
        mSettingHelper = SettingHelper.getInstance(mContext);
    }

    @Override
    public void initialize() {
        RongIMClient.Options options = new RongIMClient.Options();
        options.setEnableDebug(false);

        RongIMClient.init(mContext);
        RongIMClient.setOptions(options);
        RongIMClient.setConnectionStatusListener(mConnectionStatusListener);

        try {
            RongIMClient.registerMessageType(TextMessage.class);
            RongIMClient.registerMessageType(VoiceMessage.class);
            RongIMClient.registerMessageType(ImageMessage.class);
            RongIMClient.registerMessageType(RongIMPushMessage.class);
        } catch (AnnotationNotFoundException e) {
            L.w(TAG, e);
        }
    }

    @Override
    public void deinitialize() {
    }

    @Override
    public void connect(String[] connectArgs) {
        final String token = connectArgs[0];
        connectInternal(token);
    }

    private void connectInternal(String token) {
        if (TextUtils.isEmpty(token)) {
            // token is empty, update it first.
            updateTokenAndReconnect();
            return;
        }

        try {
            mIMClient = RongIMClient.connect(token, mConnectCallback);
            mIMClient.setOnReceiveMessageListener(mReceiveMessageListener);
        } catch (Exception e) {
            L.w(TAG, e);
        }
    }

    @Override
    public void disconnect() {
        if (mIMClient != null) {
            mIMClient.setOnReceiveMessageListener(null);
            mIMClient.disconnect(false);
        }
        mConnected.set(false);
    }

    private void executeTask(Runnable task) {
        mTaskExecutor.execute(task);
    }

    private static class SendResult {

        public final ChatMessage chatMessage;
        public final RongIMClient.Message rongMessage;
        public final boolean success;
        public final RongIMClient.SendMessageCallback.ErrorCode errorCode;

        public SendResult(ChatMessage chatMessage, RongIMClient.Message rongMessage,
                          boolean success, RongIMClient.SendMessageCallback.ErrorCode errorCode) {
            this.chatMessage = chatMessage;
            this.rongMessage = rongMessage;
            this.success = success;
            this.errorCode = errorCode;
        }
    }

    private class ChatMessageSendCallback implements RongIMClient.SendMessageCallback {

        private ChatMessage mChatMessage;
        private RongIMClient.Message mRongMessage;

        public ChatMessageSendCallback(ChatMessage chatMessage) {
            mChatMessage = chatMessage;
        }

        public void setRongMessage(RongIMClient.Message message) {
            mRongMessage = message;
        }

        @Override
        public void onSuccess(int messageId) {
            onSendResult(true, null);
        }

        @Override
        public void onError(int messageId, ErrorCode errorCode) {
            onSendResult(false, errorCode);
        }

        @Override
        public void onProgress(int messageId, int percent) {
        }

        private void onSendResult(boolean success, ErrorCode errorCode) {
            Message msg = mHandler.obtainMessage(MSG_HANDLE_CHAT_MESSAGE_SEND_RESULT,
                    new SendResult(mChatMessage, mRongMessage, success, errorCode));
            mHandler.sendMessage(msg);
        }
    }

    private void handleChatMessageSendResult(SendResult sendResult) {
        final int resultCode = sendResult.success ? ConstantCode.SEND_RESULT_SUCCESS
                : ConstantCode.SEND_RESULT_NETWORK_ERROR;
        final ChatMessage chatMessage = sendResult.chatMessage;
        final RongIMClient.Message rongMessage = sendResult.rongMessage;

        notifyChatMessageSendResult(chatMessage, resultCode);
        if (rongMessage != null) {
            deleteMessage(rongMessage.getMessageId());
        }
    }

    @Override
    public boolean sendChatMessage(ChatMessage chatMessage) {
        if (mIMClient != null) {
            RongIMClient.MessageContent messageContent = buildMessageContent(chatMessage);

            if (messageContent != null) {
                ChatMessageSendCallback callback = new ChatMessageSendCallback(chatMessage);
                RongIMClient.ConversationType conversationType = getConversationType(chatMessage);
                RongIMClient.Message message = mIMClient.sendMessage(
                        conversationType, chatMessage.getTargetId(),
                        messageContent, callback);
                callback.setRongMessage(message);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean isConnected() {
        return (mIMClient != null) && mConnected.get();
    }

    @Override
    public void joinLabelChatRoom(String labelId) {
        mIMClient.joinChatRoom(labelId, 10,
                new ChatRoomOperationCallback(labelId) {
                    @Override
                    public void onSuccess() {
                        notifyJoinLabelChatRoomResult(chatRoomId, 0);
                    }

                    @Override
                    public void onError(ErrorCode errorCode) {
                        notifyJoinLabelChatRoomResult(chatRoomId, 1);
                    }
                });
    }

    @Override
    public void quitLabelChatRoom(String labelId) {
        mIMClient.quitChatRoom(labelId,
                new ChatRoomOperationCallback(labelId) {
                    @Override
                    public void onSuccess() {
                        notifyQuitLabelChatRoomResult(chatRoomId, 0);
                    }

                    @Override
                    public void onError(ErrorCode errorCode) {
                        notifyQuitLabelChatRoomResult(chatRoomId, 1);
                    }
                });
    }

    @Override
    public void joinNormalChatRoom(String chatRoomId) {
        if (isNormalChatRoom(chatRoomId)) {
            mIMClient.joinChatRoom(chatRoomId, 10,
                    new ChatRoomOperationCallback(chatRoomId) {
                        @Override
                        public void onSuccess() {
                            notifyJoinNormalChatRoomResult(chatRoomId, 0);
                        }

                        @Override
                        public void onError(ErrorCode errorCode) {
                            notifyJoinNormalChatRoomResult(chatRoomId, 1);
                        }
                    });
        } else {
            notifyJoinNormalChatRoomResult(chatRoomId, 2);
        }
    }

    @Override
    public void quitNormalChatRoom(String chatRoomId) {
        if (isNormalChatRoom(chatRoomId)) {
            mIMClient.quitChatRoom(chatRoomId,
                    new ChatRoomOperationCallback(chatRoomId) {
                        @Override
                        public void onSuccess() {
                            notifyQuitNormalChatRoomResult(chatRoomId, 0);
                        }

                        @Override
                        public void onError(ErrorCode errorCode) {
                            notifyQuitNormalChatRoomResult(chatRoomId, 1);
                        }
                    });
        } else {
            notifyQuitNormalChatRoomResult(chatRoomId, 2);
        }
    }

    private abstract static class ChatRoomOperationCallback
            implements RongIMClient.OperationCallback {

        protected final String chatRoomId;

        public ChatRoomOperationCallback(String chatRoomId) {
            this.chatRoomId = chatRoomId;
        }
    }

    private void handleConnectSuccess(String userId) {
        L.v(TAG, "handleConnectSuccess(), userId=" + userId);

        if (userId.equals(mCallback.getAccountUserId())) {
            mConnected.set(true);
            notifyConnectResult(ConstantCode.IM_CONNECT_SUCCESS);
        } else {
            L.v(TAG, "handleConnectSuccess(), userId not match, update token now.");
            updateTokenAndReconnect();
        }
    }

    private void handleConnectError(RongIMClient.ConnectCallback.ErrorCode errorCode) {
        L.v(TAG, "handleConnectError(), errorCode=" + errorCode.getMessage());

        mConnected.set(false);
        switch (errorCode) {
            case UNKNOWN:
            case PACKAGE_BROKEN:
            case SERVER_UNAVAILABLE:
            case TIMEOUT:
                mIMClient.reconnect(mConnectCallback);
                break;
            case TOKEN_INCORRECT:
                // token incorrect, get new token and connect again.
                updateTokenAndReconnect();
                break;
            default:
                break;
        }
    }

    private void updateTokenAndReconnect() {
        L.v(TAG, "updateTokenAndReconnect()");

        UpdateRongCloudTokenCommand command = new UpdateRongCloudTokenCommand();
        mCallback.preTreatCommand(command);
        ICommandResponseHandler handler = new ICommandResponseHandler() {
            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                CommandResult commandResult = new CommandResult(result, response, null);
                Message msg = mHandler.obtainMessage(MSG_HANDLE_UPDATE_TOKEN_RESULT,
                        commandResult);
                mHandler.sendMessage(msg);
            }
        };
        mCallback.executeCommand(command.toRequestCommand(), handler);
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

    private void handleUpdateTokenResult(CommandResult commandResult) {
        final int result = commandResult.result;
        final String response = commandResult.response;

        switch (result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                handleUpdateTokenResponse(response);
                break;
            default:
                // Execute command failed, do it again.
                updateTokenAndReconnect();
                break;
        }
    }

    private void handleUpdateTokenResponse(String response) {
        try {
            UpdateRongCloudTokenCommand.CommandResponse cmdResp
                    = new UpdateRongCloudTokenCommand.CommandResponse(response);
            if (cmdResp.requestSuccess()) {
                // update token success, reconnect and save token.
                final String token = cmdResp.getToken();
                connectInternal(token);
                mSettingHelper.setAccountRongCloudToken(token);
            }
        } catch (JSONException e) {
            L.w(TAG, e);
        }
    }

    private void handleReceiveMessage(RongIMClient.Message message, int left) {
        final RongIMClient.ConversationType conversationType = message.getConversationType();

        L.v(TAG, "handleReceiveMessage(), message=%1$s, left=%2$d, conversationType=%3$s",
                message.toString(), left, conversationType.getName());

        // Check if message supported or not, skip the message if not supported
        /*if (message.getMessageDirection() != RongIMClient.MessageDirection.RECEIVE) {
            L.v(TAG, "handleReceiveMessage(), message direction is not receive, skip it.");
            return;
        }*/

        switch (conversationType) {
            case PRIVATE:
            case GROUP:
            case SYSTEM:
            case CHATROOM:
                onReceivePrivateMessage(message);
                break;
            default:
                break;
        }
    }

    private void onReceivePrivateMessage(RongIMClient.Message message) {
        L.v(TAG, "onReceivePrivateMessage()");

        final RongIMClient.MessageContent messageContent = message.getContent();

        if (messageContent instanceof TextMessage) {
            onReceiveTextMessage(message);
        } else if (messageContent instanceof VoiceMessage) {
            onReceiveVoiceMessage(message);
        } else if (messageContent instanceof ImageMessage) {
            onReceiveImageMessage(message);
        } else if (messageContent instanceof RongIMPushMessage) {
            onReceivePushMessage(message);
        } else {
            L.v(TAG, "onReceivePrivateMessage(), unsupported message type, messageContent=%1$s",
                    messageContent);
        }
    }

    private int getMessageDirection(RongIMClient.MessageDirection messageDirection) {
        int direction;

        switch (messageDirection) {
            case RECEIVE:
                direction = ChatMessage.DIRECTION_RECV;
                break;
            case SEND:
                direction = ChatMessage.DIRECTION_SEND;
                break;
            default:
                direction = ChatMessage.DIRECTION_ILLEGAL;
                break;
        }

        return direction;
    }

    private ChatMessage createReceiveChatMessage(RongIMClient.Message message) {
        int conversationType = getConversationType(message);
        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setDirection(getMessageDirection(message.getMessageDirection()));
        chatMessage.setConversationType(conversationType);
        chatMessage.setState(ChatMessage.STATE_UNREAD);
        chatMessage.setTargetId(message.getTargetId());
        chatMessage.setSenderId(message.getSenderUserId());
        chatMessage.setTime(message.getReceivedTime());
        chatMessage.setMessageId(String.valueOf(message.getMessageId()));

        return chatMessage;
    }

    private void onReceiveTextMessage(RongIMClient.Message message) {
        ChatMessage chatMessage = createReceiveChatMessage(message);
        TextMessage textMessage = (TextMessage) message.getContent();

        chatMessage.setType(ChatMessage.TYPE_TEXT);
        chatMessage.setContent(textMessage.getContent());

        notifyNewChatMessageReceived(chatMessage);
        deleteMessage(message.getMessageId());
    }

    private void onReceiveVoiceMessage(RongIMClient.Message message) {
        ChatMessage chatMessage = createReceiveChatMessage(message);
        VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
        Uri uri = voiceMessage.getUri();
        String scheme = uri.getScheme();

        if (scheme == null || scheme.equals("file")) {
            File file = getVoiceFile(message.getTargetId());

            copyFile(new File(uri.getPath()), file);
            chatMessage.setType(ChatMessage.TYPE_VOICE);
            chatMessage.setContent(file.getName());
            chatMessage.setPreview(String.valueOf(voiceMessage.getDuration()));

            notifyNewChatMessageReceived(chatMessage);
            deleteMessage(message.getMessageId());
        } else {
            L.w(TAG, "onReceiveVoiceMessage(), illegal voice message uri=" + uri);
        }
    }

    private void onReceiveImageMessage(RongIMClient.Message message) {
        new ImageLoader(message).start();
    }

    private void handleDownloadMessageDone(ChatMessage chatMessage, int messageId) {
        notifyNewChatMessageReceived(chatMessage);
        deleteMessage(messageId);
    }

    private class ImageLoader {

        private final AtomicInteger mDownloadRetry;
        private final RongIMClient.Message mRongMessage;
        private final Runnable mDownloadRunnable = new Runnable() {
            @Override
            public void run() {
                download();
            }
        };
        private final RongIMClient.DownloadMediaCallback mDownloadCallback
                = new RongIMClient.DownloadMediaCallback() {
            @Override
            public void onProgress(int progress) {
            }

            @Override
            public void onSuccess(String localMediaPath) {
                mDownloadRetry.set(0);
                executeTask(new ImageMover(mRongMessage, localMediaPath));
            }

            @Override
            public void onError(ErrorCode errorCode) {
                // download failed, try again
                mDownloadRetry.set(mDownloadRetry.get() - 1);
                startInternal();
            }
        };

        public ImageLoader(RongIMClient.Message message) {
            mDownloadRetry = new AtomicInteger(3);
            mRongMessage = message;
        }

        public void start() {
            mDownloadRetry.set(3);
            startInternal();
        }

        private void startInternal() {
            executeTask(mDownloadRunnable);
        }

        private void download() {
            if (mDownloadRetry.get() > 0) {
                L.v(TAG, "download(), download image message, time=" + mDownloadRetry.get());

                ImageMessage imageMessage = (ImageMessage) mRongMessage.getContent();
                mIMClient.downloadMedia(RongIMClient.ConversationType.PRIVATE,
                        mRongMessage.getTargetId(), RongIMClient.MediaType.IMAGE,
                        imageMessage.getRemoteUri().toString(), mDownloadCallback);
            } else {
                L.w(TAG, "download(), download image message failed.");

                ChatMessage chatMessage = createReceiveChatMessage(mRongMessage);
                Message msg = mHandler.obtainMessage(MSG_HANDLE_DOWNLOAD_MESSAGE_DONE,
                        mRongMessage.getMessageId(), 0, chatMessage);
                mHandler.sendMessage(msg);
            }
        }
    }

    private class ImageMover implements Runnable {

        private final RongIMClient.Message mRongMessage;
        private final String mImagePath;

        public ImageMover(RongIMClient.Message message, String imagePath) {
            mRongMessage = message;
            mImagePath = imagePath;
        }

        @Override
        public void run() {
            ChatMessage chatMessage = createReceiveChatMessage(mRongMessage);
            chatMessage.setType(ChatMessage.TYPE_IMAGE);

            L.v(TAG, "ImageMover:run(), mImagePath=" + mImagePath);

            if (!TextUtils.isEmpty(mImagePath)) {
                File imageFile = new File(mImagePath);

                if (imageFile.exists()) {
                    String userId = mRongMessage.getTargetId();
                    File tempFile = getImageFile(userId);

                    copyFile(imageFile, tempFile);
                    chatMessage.setContent(tempFile.getName());
                    chatMessage.setPreview(mThumbnailFactory.generateThumbnailFile(
                            tempFile.getPath(), getThumbnailFile(userId).getPath()));
                }
            }

            Message msg = mHandler.obtainMessage(MSG_HANDLE_DOWNLOAD_MESSAGE_DONE,
                    mRongMessage.getMessageId(), 0, chatMessage);
            mHandler.sendMessage(msg);
        }
    }

    private void onReceivePushMessage(RongIMClient.Message message) {
        RongIMPushMessage pushMessage = (RongIMPushMessage) message.getContent();
        Intent intent = new Intent(ACTION_RECEIVE_PUSH_MESSAGE);
        intent.putExtra(FIELD_PUSH_ID, pushMessage.getPushId());
        mContext.sendBroadcast(intent);
    }

    private RongIMClient.ConversationType getConversationType(ChatMessage chatMessage) {
        RongIMClient.ConversationType conversationType;

        switch (chatMessage.getConversationType()) {
            case ChatMessage.CONVERSATION_GROUP:
                conversationType = RongIMClient.ConversationType.GROUP;
                break;
            case ChatMessage.CONVERSATION_LABEL_CHAT_ROOM:
            case ChatMessage.CONVERSATION_NORMAL_CHAT_ROOM:
                conversationType = RongIMClient.ConversationType.CHATROOM;
                break;
            case ChatMessage.CONVERSATION_PRIVATE:
            default:
                conversationType = RongIMClient.ConversationType.PRIVATE;
                break;
        }

        return conversationType;
    }

    private int getConversationType(RongIMClient.Message message) {
        int conversationType;

        switch (message.getConversationType()) {
            case GROUP:
                conversationType = ChatMessage.CONVERSATION_GROUP;
                break;
            case CHATROOM:
                conversationType = getChatRoomConversationType(message);
                break;
            case PRIVATE:
            default:
                conversationType = ChatMessage.CONVERSATION_PRIVATE;
                break;
        }

        return conversationType;
    }

    private int getChatRoomConversationType(RongIMClient.Message message) {
        String targetId = message.getTargetId();
        int conversationType;

        if (isNormalChatRoom(targetId)) {
            conversationType = ChatMessage.CONVERSATION_NORMAL_CHAT_ROOM;
        } else {
            conversationType = ChatMessage.CONVERSATION_LABEL_CHAT_ROOM;
        }

        return conversationType;
    }

    private RongIMClient.MessageContent buildMessageContent(ChatMessage chatMessage) {
        RongIMClient.MessageContent messageContent = null;

        switch (chatMessage.getType()) {
            case ChatMessage.TYPE_TEXT:
                messageContent = buildTextMessage(chatMessage);
                break;
            case ChatMessage.TYPE_VOICE:
                messageContent = buildVoiceMessage(chatMessage);
                break;
            case ChatMessage.TYPE_IMAGE:
                messageContent = buildImageMessage(chatMessage);
                break;
            default:
                break;
        }

        return messageContent;
    }

    private TextMessage buildTextMessage(ChatMessage chatMessage) {
        return TextMessage.obtain(chatMessage.getContent());
    }

    private VoiceMessage buildVoiceMessage(ChatMessage chatMessage) {
        final String to = chatMessage.getTargetId();
        final File file = new File(getVoiceFileDir(to), chatMessage.getContent());
        final Uri uri = Uri.fromFile(file);
        int duration;

        try {
            duration = Integer.valueOf(chatMessage.getPreview());
        } catch (Exception e) {
            duration = 1;
        }

        return VoiceMessage.obtain(uri, duration);
    }

    private ImageMessage buildImageMessage(ChatMessage chatMessage) {
        final String to = chatMessage.getTargetId();
        final File file = new File(getImageFileDir(to), chatMessage.getContent());
        final File thumbFile = new File(getThumbnailFileDir(to), chatMessage.getPreview());

        return ImageMessage.obtain(Uri.fromFile(thumbFile), Uri.fromFile(file));
    }

    private void handleConnectionStatusChanged(
            RongIMClient.ConnectionStatusListener.ConnectionStatus connectionStatus) {
        L.v(TAG, "handleConnectionStatusChanged(), connectionStatus="
                + connectionStatus.getMessage());
        // TODO
    }

    private boolean isNormalChatRoom(String chatRoomId) {
        return !TextUtils.isEmpty(chatRoomId)
                && chatRoomId.startsWith(NORMAL_CHAT_ROOM_PREFIX);
    }

    private void copyFile(File src, File dest) {
        FileUtils.copyFile(src, dest);
    }

    private File getVoiceFileDir(String userId) {
        return EnvConfig.getVoiceChatMsgDirectory(userId);
    }

    private File getVoiceFile(String userId) {
        return new File(getVoiceFileDir(userId),
                UniqueFileName.getUniqueFileName("amr"));
    }

    private File getImageFileDir(String userId) {
        return EnvConfig.getImageChatMsgDirectory(userId);
    }

    private File getThumbnailFileDir(String userId) {
        return EnvConfig.getImageChatMsgThumbnailDirectory(userId);
    }

    private File getImageFile(String userId) {
        return new File(getImageFileDir(userId), UniqueFileName.getUniqueFileName("jpg"));
    }

    private File getThumbnailFile(String userId) {
        return new File(getThumbnailFileDir(userId), UniqueFileName.getUniqueFileName("jpg"));
    }

    private void deleteMessage(int messageId) {
        try {
            mIMClient.deleteMessages(new int[]{
                    messageId,
            });
        } catch (Exception e) {
            L.w(TAG, e);
        }
    }
}
