
package com.ekuater.labelchat.ui.widget.emoji.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import com.ekuater.labelchat.R;

import java.util.LinkedHashMap;
import java.util.Map;

public class EmojiFace {

    private static EmojiFace sInstance;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new EmojiFace(context.getApplicationContext());
        }
    }

    public static EmojiFace getInstance(Context context) {
        if (null == sInstance) {
            initInstance(context);
        }
        return sInstance;
    }

    private final Resources mResources;
    private final String mPackageName;
    private final String mStartTag;
    private final String mEndTag;
    private final String mStaticFacePrefix;
    private final String mDynamicFacePrefix;
    private final Map<String, String> mEmojiMap;

    private EmojiFace(Context context) {
        mResources = context.getResources();
        mPackageName = context.getPackageName();
        mStartTag = mResources.getString(R.string.emoji_icon_parse_start_char);
        mEndTag = mResources.getString(R.string.emoji_icon_parse_end_char);
        mStaticFacePrefix = mResources.getString(R.string.emoji_icon_static_prefix);
        mDynamicFacePrefix = mResources.getString(R.string.emoji_icon_dynamic_prefix);
        mEmojiMap = setupEmojiMap(context);
    }

    private Map<String, String> setupEmojiMap(Context context) {
        final Map<String, String> map = new LinkedHashMap<String, String>();
        final Resources res = context.getResources();
        final TypedArray keyAr = res.obtainTypedArray(R.array.emoji_key_array);
        final TypedArray valueAr = res.obtainTypedArray(R.array.emoji_value_array);
        final int length = keyAr.length();

        if (length != valueAr.length()) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < length; ++i) {
            final int keyId = keyAr.getResourceId(i, 0);
            final int valueId = valueAr.getResourceId(i, 0);
            final String[] key = res.getStringArray(keyId);
            final String[] value = res.getStringArray(valueId);

            if (key.length != value.length) {
                throw new IllegalArgumentException();
            }

            for (int j = 0; j < key.length; ++j) {
                map.put(key[j], value[j]);
            }
        }

        keyAr.recycle();
        valueAr.recycle();

        return map;
    }

    public Map<String, String> getFaceMap() {
        return mEmojiMap;
    }

    public String getStaticFacePrefix() {
        return mStaticFacePrefix;
    }

    public String getDynamicFacePrefix() {
        return mDynamicFacePrefix;
    }

    public String getStartTag() {
        return mStartTag;
    }

    public String getEndTag() {
        return mEndTag;
    }

    public String getFaceTag(String face) {
        return mStartTag + face + mEndTag;
    }

    public String getFaceId(String face) {
        if (mEmojiMap.containsKey(face)) {
            return mEmojiMap.get(face);
        } else {
            return "";
        }
    }

    public String getStaticFaceResName(String face) {
        return getStaticFacePrefix() + getFaceId(face);
    }

    public int getStaticFaceId(String face) {
        return mResources.getIdentifier(getStaticFaceResName(face),
                "drawable", mPackageName);
    }
}
