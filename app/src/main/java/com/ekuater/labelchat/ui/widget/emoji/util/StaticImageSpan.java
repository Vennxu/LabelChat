package com.ekuater.labelchat.ui.widget.emoji.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

/**
 * @author LinYong
 */
public class StaticImageSpan extends DynamicDrawableSpan {

    private static final int MAX_CACHE_SIZE = 50;
    private static final DrawableCache sCache = new DrawableCache(MAX_CACHE_SIZE);

    private static synchronized Drawable getCachedDrawable(int id, int size) {
        return sCache.get(id, size);
    }

    private static synchronized void putCachedDrawable(int id, int size, Drawable drawable) {
        if (drawable != null) {
            sCache.put(id, size, drawable);
        }
    }

    private static Drawable buildDrawableInternal(Resources res, int id) {
        return res.getDrawable(id);
    }

    private Resources mResources;
    private int mResourceId;
    private int mSize;

    public StaticImageSpan(Context context, int resourceId, int size) {
        super(ALIGN_BASELINE);
        mResources = context.getApplicationContext().getResources();
        mResourceId = resourceId;
        mSize = size;
    }

    @Override
    public Drawable getDrawable() {
        Drawable drawable = getCachedDrawable(mResourceId, mSize);

        if (drawable == null) {
            drawable = buildDrawableInternal(mResources, mResourceId);
            drawable.setBounds(0, 0, mSize, mSize);
            putCachedDrawable(mResourceId, mSize, drawable);
        }

        return drawable;
    }
}
