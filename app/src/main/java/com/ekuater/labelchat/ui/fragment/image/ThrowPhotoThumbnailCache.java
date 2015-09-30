package com.ekuater.labelchat.ui.fragment.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.LruCache;

import com.ekuater.labelchat.util.L;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * @author LinYong
 */
/*package*/ class ThrowPhotoThumbnailCache {

    private static final String TAG = ThrowPhotoThumbnailCache.class.getSimpleName();

    public interface LoadCallback {
        public void onThumbnailLoaded(Bitmap thumbnail);
    }

    private interface CallbackNotifier {
        public void notify(LoadCallback callback);
    }

    private static final Executor THREAD_POOL_EXECUTOR = AsyncTask.THREAD_POOL_EXECUTOR;

    private static ThrowPhotoThumbnailCache sInstance;

    private static synchronized void initInstance() {
        if (sInstance == null) {
            sInstance = new ThrowPhotoThumbnailCache();
        }
    }

    public static ThrowPhotoThumbnailCache getInstance() {
        if (sInstance == null) {
            initInstance();
        }
        return sInstance;
    }

    private final LruCache<String, Bitmap> mCache;
    private final Handler mHandler;

    private ThrowPhotoThumbnailCache() {
        mCache = new LruCache<String, Bitmap>(50);
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void put(String path, Bitmap thumbnail) {
        if (!TextUtils.isEmpty(path) && thumbnail != null) {
            mCache.put(path, thumbnail);
        }
    }

    public void loadThumbnail(String thumbnailPath, String sourcePath, LoadCallback callback) {
        if (TextUtils.isEmpty(thumbnailPath) && TextUtils.isEmpty(sourcePath)) {
            notifyThumbnailLoaded(callback, null);
            return;
        }

        String key = TextUtils.isEmpty(thumbnailPath) ? sourcePath : thumbnailPath;
        Bitmap thumbnail = mCache.get(key);

        if (thumbnail != null) {
            notifyThumbnailLoaded(callback, thumbnail);
            return;
        }

        THREAD_POOL_EXECUTOR.execute(new ThumbnailLoader(thumbnailPath, sourcePath, callback));
    }

    private void notifyCallback(final CallbackNotifier notifier, final LoadCallback callback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                notifier.notify(callback);
            }
        });
    }

    private void notifyThumbnailLoaded(LoadCallback callback, Bitmap thumbnail) {
        notifyCallback(new LoadedNotifier(thumbnail), callback);
    }

    private class LoadedNotifier implements CallbackNotifier {

        private final Bitmap mThumbnail;

        public LoadedNotifier(Bitmap thumbnail) {
            mThumbnail = thumbnail;
        }

        @Override
        public void notify(LoadCallback callback) {
            if (callback != null) {
                callback.onThumbnailLoaded(mThumbnail);
            }
        }
    }

    private class ThumbnailLoader implements Runnable {

        private final String mThumbnailPath;
        private final String mSourcePath;
        private final LoadCallback mCallback;

        public ThumbnailLoader(String thumbnailPath, String sourcePath,
                               LoadCallback callback) {
            mThumbnailPath = thumbnailPath;
            mSourcePath = sourcePath;
            mCallback = callback;
        }

        @Override
        public void run() {
            boolean thumbnailEmpty = TextUtils.isEmpty(mThumbnailPath);
            Bitmap thumbnail = thumbnailEmpty ? null : BitmapFactory.decodeFile(mThumbnailPath);
            String key = thumbnailEmpty ? mSourcePath : mThumbnailPath;

            if (thumbnail == null) {
                try {
                    thumbnail = generateThumbnail(mSourcePath);
                } catch (IOException e) {
                    L.w(TAG, e);
                }
            }

            put(key, thumbnail);
            notifyThumbnailLoaded(mCallback, thumbnail);
        }

        private Bitmap generateThumbnail(String sourcePath) throws IOException {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(
                    new File(sourcePath)));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();

            int i = 0;
            Bitmap bitmap;

            while (true) {
                if ((options.outWidth >> i <= 400)
                        && (options.outHeight >> i <= 800)) {
                    in = new BufferedInputStream(
                            new FileInputStream(new File(sourcePath)));
                    options.inSampleSize = (int) Math.pow(2.0D, i);
                    options.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeStream(in, null, options);
                    break;
                }
                i += 1;
            }

            return bitmap;
        }
    }
}
