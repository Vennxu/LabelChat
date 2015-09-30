package com.ekuater.labelchat.coreservice;

import android.os.RemoteException;

import com.ekuater.labelchat.coreservice.event.FollowUserDataChangedEvent;

/**
 * Created by Leo on 2015/3/30.
 *
 * @author LinYong
 */
/*package*/ class CoreEventHandler {

    private final ICoreServiceCallback mCallback;

    public CoreEventHandler(ICoreServiceCallback callback) {
        mCallback = callback;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(FollowUserDataChangedEvent event) {
        notifyCoreService(new CoreServiceNotifier() {
            @Override
            public void notify(ICoreServiceListener listener) throws RemoteException {
                listener.onFollowUserDataChanged();
            }
        });
    }

    private void notifyCoreService(CoreServiceNotifier notifier) {
        mCallback.notifyCoreService(notifier);
    }
}
