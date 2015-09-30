
package com.ekuater.labelchat.ui.widget.emoji.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ekuater.labelchat.R;

public class EmojiAdapter extends BaseAdapter {

    private final LayoutInflater mLayoutInflater;
    private final int mEmojiCount;
    private final String mFunctionKey;
    private final int mFunctionIcon;
    private final String[] mEmojis;
    private final EmojiFace mEmojiFace;

    public EmojiAdapter(LayoutInflater inflater, EmojiFace emojiFace, String[] faces,
                        String functionKey, int functionIcon) {
        mLayoutInflater = inflater;
        mEmojis = faces;
        mEmojiCount = faces.length;
        mFunctionKey = functionKey;
        mFunctionIcon = functionIcon;
        mEmojiFace = emojiFace;
    }

    @Override
    public int getCount() {
        return mEmojiCount + 1;
    }

    @Override
    public String getItem(int position) {
        if (position < mEmojiCount) {
            return mEmojis[position];
        } else {
            return mFunctionKey;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.emoji_cell, parent, false);
        }

        ImageView imageView = (ImageView) view;
        if (position < mEmojiCount) {
            int resId = mEmojiFace.getStaticFaceId(mEmojis[position]);
            imageView.setImageResource(resId);
        } else {
            imageView.setImageResource(mFunctionIcon);
        }
        return view;
    }

    public String getFaceTag(int position) {
        if (position < mEmojiCount) {
            return mEmojiFace.getFaceTag(mEmojis[position]);
        } else {
            return mFunctionKey;
        }
    }
}
