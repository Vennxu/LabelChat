package com.ekuater.labelchat.ui.activity.confide;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ekuater.labelchat.R;

/**
 * Created by Leo on 2015/4/8.
 *
 * @author LinYong
 */
public class ColorHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private interface Notifier {
        public void notify(ColorHolderListener listener);
    }

    private View mColorView;
    private final ColorHolderListener mListener;

    public ColorHolder(View itemView, ColorHolderListener listener) {
        super(itemView);
        mColorView = itemView.findViewById(R.id.color);
        mListener = listener;
        itemView.setOnClickListener(this);
    }

    public void bindView(int color, boolean selected) {
        mColorView.setBackgroundColor(color);
        mColorView.setSelected(selected);
    }

    @Override
    public void onClick(View v) {
        notifyListener(new Notifier() {
            @Override
            public void notify(ColorHolderListener listener) {
                listener.onClick(ColorHolder.this);
            }
        });
    }

    private void notifyListener(Notifier notifier) {
        if (mListener != null) {
            notifier.notify(mListener);
        }
    }
}
