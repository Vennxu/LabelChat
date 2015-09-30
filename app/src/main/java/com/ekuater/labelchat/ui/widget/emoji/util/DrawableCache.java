package com.ekuater.labelchat.ui.widget.emoji.util;

import android.graphics.drawable.Drawable;
import android.util.LruCache;

/**
 * @author LinYong
 */
/* package */ class DrawableCache extends LruCache<String, Drawable> {

    private static String genKey(int id, int size) {
        return String.valueOf(id) + "," + String.valueOf(size);
    }

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public DrawableCache(int maxSize) {
        super(maxSize);
    }

    public Drawable get(int id, int size) {
        return get(genKey(id, size));
    }

    public void put(int id, int size, Drawable drawable) {
        put(genKey(id, size), drawable);
    }
}
