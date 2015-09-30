package com.ekuater.labelchat.ui.widget.emoji;

import android.text.style.DynamicDrawableSpan;
import android.util.LruCache;

/**
 * @author LinYong
 */
public class ImageSpanCache extends LruCache<String, DynamicDrawableSpan> {

    private static final int MAX_CACHE_SIZE = 100;

    private static ImageSpanCache sInstance;

    private static synchronized void initInstance() {
        if (sInstance == null) {
            sInstance = new ImageSpanCache(MAX_CACHE_SIZE);
        }
    }

    public static ImageSpanCache getInstance() {
        if (sInstance == null) {
            initInstance();
        }
        return sInstance;
    }

    private static String genKey(int id, int size) {
        return String.valueOf(id) + "," + String.valueOf(size);
    }

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    private ImageSpanCache(int maxSize) {
        super(maxSize);
    }

    public DynamicDrawableSpan get(int id, int size) {
        return get(genKey(id, size));
    }

    public void put(int id, int size, DynamicDrawableSpan imageSpan) {
        put(genKey(id, size), imageSpan);
    }
}
