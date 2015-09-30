package com.ekuater.labelchat.ui.activity.chatting;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * @author LinYong
 */
public class ChatMsgListView extends ListView {

    private OnSizeChangedListener mOnSizeChangedListener;

    public ChatMsgListView(Context context) {
        super(context);
    }

    public ChatMsgListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatMsgListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mOnSizeChangedListener != null) {
            mOnSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
        }
    }

    public void setOnSizeChangedListener(OnSizeChangedListener l) {
        mOnSizeChangedListener = l;
    }

    public interface OnSizeChangedListener {
        void onSizeChanged(int width, int height, int oldWidth, int oldHeight);
    }
}
