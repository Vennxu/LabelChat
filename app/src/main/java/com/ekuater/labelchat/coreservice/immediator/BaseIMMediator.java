package com.ekuater.labelchat.coreservice.immediator;

import android.content.Context;

import com.ekuater.labelchat.coreservice.ICoreServiceCallback;
import com.ekuater.labelchat.datastruct.ChatMessage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Base IM mediator
 *
 * @author LinYong
 */
public abstract class BaseIMMediator {

    private interface IIMListenerNotifier {
        public void notify(IIMListener listener);
    }

    protected final Context mContext;
    protected final ICoreServiceCallback mCallback;
    private final List<WeakReference<IIMListener>> mListeners
            = new ArrayList<WeakReference<IIMListener>>();

    public BaseIMMediator(Context context, ICoreServiceCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    public synchronized final void registerListener(final IIMListener listener) {
        for (WeakReference<IIMListener> ref : mListeners) {
            if (ref.get() == listener) {
                return;
            }
        }

        mListeners.add(new WeakReference<IIMListener>(listener));
        unregisterListener(null);
    }

    public synchronized final void unregisterListener(final IIMListener listener) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            if (mListeners.get(i).get() == listener) {
                mListeners.remove(i);
            }
        }
    }

    private synchronized void notifyIIMListeners(IIMListenerNotifier notifier) {
        final List<WeakReference<IIMListener>> listeners = mListeners;

        for (int i = listeners.size() - 1; i >= 0; i--) {
            IIMListener listener = listeners.get(i).get();
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

    protected void notifyConnectResult(int result) {
        notifyIIMListeners(new ConnectResultNotifier(result));
    }

    protected void notifyChatMessageSendResult(ChatMessage chatMessage, int result) {
        notifyIIMListeners(new ChatMessageSendResultNotifier(chatMessage, result));
    }

    protected void notifyNewChatMessageReceived(ChatMessage chatMessage) {
        notifyIIMListeners(new NewChatMessageReceivedNotifier(chatMessage));
    }

    protected void notifyJoinLabelChatRoomResult(String labelId, int result) {
        notifyIIMListeners(new JoinLabelChatRoomResultNotifier(labelId, result));
    }

    protected void notifyQuitLabelChatRoomResult(String labelId, int result) {
        notifyIIMListeners(new QuitLabelChatRoomResultNotifier(labelId, result));
    }

    protected void notifyJoinNormalChatRoomResult(String chatRoomId, int result) {
        notifyIIMListeners(new JoinNormalChatRoomResultNotifier(chatRoomId, result));
    }

    protected void notifyQuitNormalChatRoomResult(String chatRoomId, int result) {
        notifyIIMListeners(new QuitNormalChatRoomResultNotifier(chatRoomId, result));
    }

    public abstract void initialize();

    public abstract void deinitialize();

    public abstract void connect(String[] connectArgs);

    public abstract void disconnect();

    public abstract boolean sendChatMessage(ChatMessage chatMessage);

    public abstract boolean isConnected();

    public abstract void joinLabelChatRoom(String labelId);

    public abstract void quitLabelChatRoom(String labelId);

    public abstract void joinNormalChatRoom(String chatRoomId);

    public abstract void quitNormalChatRoom(String chatRoomId);

    private static class ConnectResultNotifier implements IIMListenerNotifier {

        private final int mResult;

        public ConnectResultNotifier(int result) {
            mResult = result;
        }

        @Override
        public void notify(IIMListener listener) {
            listener.onConnectResult(mResult);
        }
    }

    private static class ChatMessageSendResultNotifier implements IIMListenerNotifier {

        private final ChatMessage mChatMessage;
        private final int mResult;

        public ChatMessageSendResultNotifier(ChatMessage chatMessage, int result) {
            mChatMessage = chatMessage;
            mResult = result;
        }

        @Override
        public void notify(IIMListener listener) {
            listener.onChatMessageSendResult(mChatMessage, mResult);
        }
    }

    private static class NewChatMessageReceivedNotifier implements IIMListenerNotifier {

        private final ChatMessage mChatMessage;

        public NewChatMessageReceivedNotifier(ChatMessage chatMessage) {
            mChatMessage = chatMessage;
        }

        @Override
        public void notify(IIMListener listener) {
            listener.onNewChatMessageReceived(mChatMessage);
        }
    }

    private static class JoinLabelChatRoomResultNotifier implements IIMListenerNotifier {

        private final String labelId;
        private final int result;

        public JoinLabelChatRoomResultNotifier(String labelId, int result) {
            this.labelId = labelId;
            this.result = result;
        }

        @Override
        public void notify(IIMListener listener) {
            listener.onJoinLabelChatRoomResult(labelId, result);
        }
    }

    private static class QuitLabelChatRoomResultNotifier implements IIMListenerNotifier {

        private final String labelId;
        private final int result;

        public QuitLabelChatRoomResultNotifier(String labelId, int result) {
            this.labelId = labelId;
            this.result = result;
        }

        @Override
        public void notify(IIMListener listener) {
            listener.onQuitLabelChatRoomResult(labelId, result);
        }
    }

    private static class JoinNormalChatRoomResultNotifier implements IIMListenerNotifier {

        private final String chatRoomId;
        private final int result;

        public JoinNormalChatRoomResultNotifier(String chatRoomId, int result) {
            this.chatRoomId = chatRoomId;
            this.result = result;
        }

        @Override
        public void notify(IIMListener listener) {
            listener.onJoinNormalChatRoomResult(chatRoomId, result);
        }
    }

    private static class QuitNormalChatRoomResultNotifier implements IIMListenerNotifier {

        private final String chatRoomId;
        private final int result;

        public QuitNormalChatRoomResultNotifier(String chatRoomId, int result) {
            this.chatRoomId = chatRoomId;
            this.result = result;
        }

        @Override
        public void notify(IIMListener listener) {
            listener.onQuitNormalChatRoomResult(chatRoomId, result);
        }
    }
}
