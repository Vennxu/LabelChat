
package com.ekuater.labelchat.ui.widget.emoji;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;

import com.ekuater.labelchat.R;

public class EmojiEditText extends EditText {

    private int mEmojiSize;

    public EmojiEditText(Context context) {
        super(context);
        init(null);
    }

    public EmojiEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EmojiEditText(Context context, AttributeSet attrs, int defStyle) {
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
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (isInEditMode()) {
            super.onTextChanged(text, start, lengthBefore, lengthAfter);
            return;
        }
        EmotifyHelper.emotify(getContext(), getText(), mEmojiSize);
    }

    private int getDefaultEmojiSize() {
        return Math.round(getTextSize()) + 10;
    }

    public void setEmojiSize(int pixels) {
        mEmojiSize = pixels;
    }
}
