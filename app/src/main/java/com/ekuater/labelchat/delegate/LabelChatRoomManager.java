package com.ekuater.labelchat.delegate;

import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/3/3.
 *
 * @author LinYong
 */
public class LabelChatRoomManager extends BaseManager {

    public interface IListener extends BaseManager.IListener {

        public void onJoinChatRoomResult(String labelId, int result);

        public void onQuitChatRoomResult(String labelId, int result);
    }

    public static class AbsListener implements IListener {

        @Override
        public void onCoreServiceConnected() {
        }

        @Override
        public void onCoreServiceDied() {
        }

        @Override
        public void onJoinChatRoomResult(String labelId, int result) {
        }

        @Override
        public void onQuitChatRoomResult(String labelId, int result) {
        }
    }

    private interface ListenerNotifier {
        public void notify(IListener listener);
    }

    private static LabelChatRoomManager sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new LabelChatRoomManager(context.getApplicationContext());
        }
    }

    public static LabelChatRoomManager getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private final List<WeakReference<IListener>> mListeners
            = new ArrayList<WeakReference<IListener>>();
    private final ICoreServiceNotifier mNotifier = new AbstractCoreServiceNotifier() {
        @Override
        public void onJoinLabelChatRoomResult(String labelId, int result) {
            notifyListeners(new JoinChatRoomResultNotifier(labelId, result));
        }

        @Override
        public void onQuitLabelChatRoomResult(String labelId, int result) {
            notifyListeners(new QuitChatRoomResultNotifier(labelId, result));
        }
    };

    private LabelChatRoomManager(Context context) {
        super(context);
        mCoreService.registerNotifier(mNotifier);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mCoreService.unregisterNotifier(mNotifier);
    }

    public void registerListener(IListener listener) {
        synchronized (mListeners) {
            for (WeakReference<IListener> ref : mListeners) {
                if (ref.get() == listener) {
                    return;
                }
            }
            mListeners.add(new WeakReference<IListener>(listener));
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

    public void joinLabelChatRoom(String labelId) {
        mCoreService.joinLabelChatRoom(labelId);
    }

    public void quitLabelChatRoom(String labelId) {
        mCoreService.quitLabelChatRoom(labelId);
    }

    private void notifyListeners(ListenerNotifier notifier) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            IListener listener = mListeners.get(i).get();
            if (listener != null) {
                notifier.notify(listener);
            } else {
                mListeners.remove(i);
            }
        }
    }

    private static class JoinChatRoomResultNotifier implements ListenerNotifier {

        private final String labelId;
        private final int result;

        public JoinChatRoomResultNotifier(String labelId, int result) {
            this.labelId = labelId;
            this.result = result;
        }

        @Override
        public void notify(IListener listener) {
            listener.onJoinChatRoomResult(labelId, result);
        }
    }

    private static class QuitChatRoomResultNotifier implements ListenerNotifier {

        private final String labelId;
        private final int result;

        public QuitChatRoomResultNotifier(String labelId, int result) {
            this.labelId = labelId;
            this.result = result;
        }

        @Override
        public void notify(IListener listener) {
            listener.onQuitChatRoomResult(labelId, result);
        }
    }
}
