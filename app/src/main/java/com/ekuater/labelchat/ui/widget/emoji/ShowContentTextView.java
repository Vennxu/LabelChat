package com.ekuater.labelchat.ui.widget.emoji;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ekuater.labelchat.R;


/**
 * Created by Administrator on 2015/1/20.
 *
 * @author FanChong
 */
public class ShowContentTextView extends EmojiTextView {
   /* private int mEmojiSize;*/

    public ShowContentTextView(Context context) {
        super(context);
        //init(null);
    }

    public ShowContentTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
       // init(attributeSet);
    }

    public ShowContentTextView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
      //  init(attributeSet);
    }

   /* private void init(AttributeSet attrs) {
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
        SpannableString pannable = new SpannableString(text);
        EmotifyHelper.emotify(getContext(), pannable, mEmojiSize);
        super.setText(pannable, type);
    }

    private int getDefaultEmojiSize() {
        return Math.round(getTextSize()) + 10;
    }

    public void setEmojiSize(int pixels) {
        mEmojiSize = pixels;
    }

*/
    @Override
    public void setFocusable(boolean focusable) {
        super.setFocusable(false);
    }
}
