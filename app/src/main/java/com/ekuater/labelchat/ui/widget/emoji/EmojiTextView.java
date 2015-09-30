
package com.ekuater.labelchat.ui.widget.emoji;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ekuater.labelchat.R;

/**
 * @author way
 */
public class EmojiTextView extends TextView {

    private int mEmojiSize;

    public EmojiTextView(Context context) {
        super(context);
        init(null);
    }

    public EmojiTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EmojiTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }

        mEmojiSize = getDefaultEmojiSize();

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs,
                    R.styleable.Emojicon);
            mEmojiSize = (int) a.getDimension(R.styleable.Emojicon_emojiconSize, mEmojiSize);
            a.recycle();
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (isInEditMode()) {
            super.setText(text, type);
            return;
        }

        if (text == null) {
            text = "";
        }
        SpannableString spannable = new SpannableString(text);
        EmotifyHelper.emotify(getContext(), spannable, mEmojiSize);
        super.setText(spannable, type);
    }

    private int getDefaultEmojiSize() {
        return Math.round(getTextSize()) + 10;
    }

    public void setEmojiSize(int pixels) {
        mEmojiSize = pixels;
    }
}
