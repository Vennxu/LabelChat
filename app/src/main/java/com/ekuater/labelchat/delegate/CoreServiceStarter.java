package com.ekuater.labelchat.delegate;

import android.content.Context;

/**
 * @author LinYong
 */
public class CoreServiceStarter extends BaseManager {

    public interface OnStartListener {
        public void onStarted();
    }

    public static void start(Context context, OnStartListener listener) {
        new CoreServiceStarter(context, listener);
    }

    private OnStartListener mListener;
    private final ICoreServiceNotifier mNotifier
            = new AbstractCoreServiceNotifier() {
        @Override
        public void onCoreServiceConnected() {
            checkStart();
        }
    };

    private CoreServiceStarter(Context context, OnStartListener listener) {
        super(context);
        mListener = listener;
        mCoreService.registerNotifier(mNotifier);
        checkStart();
    }

    private synchronized void checkStart() {
        if (mCoreService.available()) {
            notifyStarted();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mCoreService.unregisterNotifier(mNotifier);
    }

    private void notifyStarted() {
        if (mListener != null) {
            mListener.onStarted();
            mListener = null;
        }
    }
}
