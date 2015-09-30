package com.ekuater.labelchat.ui.widget.emoji.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;

import com.ekuater.labelchat.R;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LinYong
 */
public class DynamicImageSpan extends DynamicDrawableSpan {

    public interface AnimationListener {
        public void onAmimationUpdate();
    }

    private static final String TAG = "DynamicImageSpan";

    private static final int MAX_CACHE_SIZE = 50;
    private static final DrawableCache sCache = new DrawableCache(MAX_CACHE_SIZE);

    private static final class AnimateCallback implements Drawable.Callback {

        @Override
        public void invalidateDrawable(Drawable who) {
            if (who instanceof DynamicDrawable) {
                ((DynamicDrawable) who).notifyAmimationUpdate();
            }
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            if (who instanceof DynamicDrawable) {
                if (sAnimateScheduleHandler != null) {
                    final long delay = when - SystemClock.uptimeMillis();
                    sAnimateScheduleHandler.postDelayed(what, delay);
                }
            }
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            if (who instanceof DynamicDrawable) {
                if (sAnimateScheduleHandler != null) {
                    sAnimateScheduleHandler.removeCallbacks(what);
                }
            }
        }
    }

    private static HandlerThread sHandlerThread;
    private static Handler sAnimateScheduleHandler;
    private static AnimateCallback mAnimateCallback;
    private static int sCount = 0;

    private static synchronized void increaseCount() {
        if (sCount == 0) {
            sHandlerThread = new HandlerThread("DynamicImageSpan_AnimateScheduleThread");
            sHandlerThread.start();
            sAnimateScheduleHandler = new Handler(sHandlerThread.getLooper());
            mAnimateCallback = new AnimateCallback();
        }
        ++sCount;
    }

    private static synchronized void decreaseCount() {
        if (sCount > 0) {
            --sCount;
        }

        if (sCount == 0) {
            sAnimateScheduleHandler = null;
            sHandlerThread.quit();
            sHandlerThread = null;
            mAnimateCallback = null;
        }
    }

    private static synchronized Drawable getCachedDrawable(int id, int size) {
        return sCache.get(id, size);
    }

    private static synchronized void putCachedDrawable(int id, int size, Drawable drawable) {
        if (drawable != null) {
            sCache.put(id, size, drawable);
        }
    }

    private static Drawable buildDrawableInternal(Resources res, int id) {
        GifDecoder gifDecoder = new GifDecoder();
        InputStream source = res.openRawResource(id);
        Drawable drawable = null;
        int frameCount;
        Bitmap[] frameBmps = null;
        int[] frameDelays = null;

        try {
            gifDecoder.read(source);
            frameCount = gifDecoder.getFrameCount();

            if (frameCount > 0) {
                frameBmps = new Bitmap[frameCount];
                frameDelays = new int[frameCount];

                for (int i = 0; i < frameCount; ++i) {
                    frameBmps[i] = gifDecoder.getFrame(i);
                    frameDelays[i] = gifDecoder.getDelay(i);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "buildDrawableInternal() failure, id=" + Integer.toHexString(id)
                    + ", Exception:" + e);
            return null;
        }

        if (frameBmps != null) {
            DynamicDrawable dynamicDrawable = new DynamicDrawable();

            for (int i = 0; i < frameCount; ++i) {
                dynamicDrawable.addFrame(new BitmapDrawable(res, frameBmps[i]), frameDelays[i]);
            }
            dynamicDrawable.setOneShot(false);

            drawable = dynamicDrawable;
        }

        return drawable;
    }

    private final List<WeakReference<AnimationListener>> mListeners
            = new ArrayList<WeakReference<AnimationListener>>();
    private Resources mResources;
    private int mResourceId;
    private int mSize;
    private DynamicDrawable.AnimationListener mAnimationListener
            = new DynamicDrawable.AnimationListener() {
        @Override
        public void onAmimationUpdate() {
            notifyAmimationUpdate();
        }
    };

    public DynamicImageSpan(Context context, int resourceId, int size) {
        super();
        increaseCount();
        mResources = context.getApplicationContext().getResources();
        mResourceId = resourceId;
        mSize = size;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        decreaseCount();
    }

    @Override
    public Drawable getDrawable() {
        Drawable drawable = getCachedDrawable(mResourceId, mSize);

        if (drawable == null) {
            drawable = buildDrawableInternal(mResources, mResourceId);
            if (drawable == null) {
                drawable = mResources.getDrawable(R.drawable.emoji_f_dynamic_default);
            }

            drawable.setBounds(0, 0, mSize, mSize);
            putCachedDrawable(mResourceId, mSize, drawable);
        }

        if (drawable instanceof DynamicDrawable) {
            if (drawable.getCallback() == null) {
                drawable.setCallback(mAnimateCallback);
            }
            ((DynamicDrawable) drawable).registerListener(mAnimationListener);
            ((DynamicDrawable) drawable).start();
        }

        return drawable;
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

    private void notifyAmimationUpdate() {
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
