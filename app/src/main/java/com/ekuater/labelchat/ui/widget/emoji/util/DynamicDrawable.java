package com.ekuater.labelchat.ui.widget.emoji.util;

import android.graphics.drawable.AnimationDrawable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LinYong
 */
/*package*/ class DynamicDrawable extends AnimationDrawable {

    public interface AnimationListener {
        public void onAmimationUpdate();
    }

    private final List<WeakReference<AnimationListener>> mListeners
            = new ArrayList<WeakReference<AnimationListener>>();

    public DynamicDrawable() {
        super();
    }

    public void registerListener(AnimationListener listener) {
        synchronized (mListeners) {
            for (WeakReference<AnimationListener> ref : mListeners) {
                if (ref.get() == listener) {
                    return;
                }
            }

            mListeners.add(new WeakReference<AnimationListener>(listener));
            unregisterListener(null);
        }
    }

    public void unregisterListener(AnimationListener listener) {
        synchronized (mListeners) {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                if (mListeners.get(i).get() == listener) {
                    mListeners.remove(i);
                }
            }
        }
    }

    public void notifyAmimationUpdate() {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            AnimationListener listener = mListeners.get(i).get();
            if (listener != null) {
                listener.onAmimationUpdate();
            } else {
                mListeners.remove(i);
            }
        }
    }
}
