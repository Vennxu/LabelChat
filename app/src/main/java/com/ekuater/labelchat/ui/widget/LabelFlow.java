package com.ekuater.labelchat.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.ekuater.labelchat.R;

import java.util.List;

/**
 * @author LinYong
 */
public class LabelFlow extends FlowLayout {

    public interface OnLabelClickListener {
        public void onLabelClick(LabelView labelView, String label, boolean isOwned);
    }

    public interface OnLabelLongClickListener {
        public void onLabelLongClick(LabelView labelView, String label, boolean isOwned);
    }

    private LayoutInflater mInflater;
    private List<String> mOwnerLabels;
    private OnLabelClickListener mLabelClickListener;
    private OnLabelLongClickListener mLabelLongClickListener;
    private final OnClickListener mLabelItemClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof LabelView) {
                if (mLabelClickListener != null) {
                    final LabelView view = (LabelView) v;
                    mLabelClickListener.onLabelClick(view,
                            view.getText().toString(), view.isOwned());
                }
            }
        }
    };
    private final OnLongClickListener mLabelItemLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (v instanceof LabelView) {
                if (mLabelLongClickListener != null) {
                    final LabelView view = (LabelView) v;
                    mLabelLongClickListener.onLabelLongClick(view,
                            view.getText().toString(), view.isOwned());
                }
            }
            return true;
        }
    };

    public LabelFlow(Context context) {
        super(context);
        initialize(context);
    }

    public LabelFlow(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initialize(context);
    }

    public LabelFlow(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        initialize(context);
    }

    public void setOnLabelClickListener(OnLabelClickListener listener) {
        mLabelClickListener = listener;
        OnClickListener clickListener = (mLabelClickListener != null)
                ? mLabelItemClickListener : null;

        for (int i = 0; i < getChildCount(); ++i) {
            final View child = getChildAt(i);
            if (child instanceof LabelView) {
                child.setOnClickListener(clickListener);
            }
        }
    }

    public void setOnLabelLongClickListener(OnLabelLongClickListener listener) {
        mLabelLongClickListener = listener;
        OnLongClickListener longClickListener = (mLabelLongClickListener != null)
                ? mLabelItemLongClickListener : null;
        for (int i = 0; i < getChildCount(); ++i) {
            final View child = getChildAt(i);
            if (child instanceof LabelView) {
                child.setOnLongClickListener(longClickListener);
            }
        }
    }

    private void initialize(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setOwnerLabels(List<String> ownerLabels) {
        mOwnerLabels = ownerLabels;

        for (int i = 0; i < getChildCount(); ++i) {
            View child = getChildAt(i);
            if (child instanceof LabelView) {
                LabelView labelView = (LabelView) child;
                updateLabelBackground(labelView);
            }
        }
    }

    public void addLabel(String labelName) {
        LabelView labelView = (LabelView) mInflater.inflate(R.layout.user_label_item, this, false);
        labelView.setText(labelName);
        updateLabelBackground(labelView);
        addView(labelView);
        if (mLabelClickListener != null) {
            labelView.setOnClickListener(mLabelItemClickListener);
        }
    }

    private void updateLabelBackground(LabelView labelView) {
        labelView.setOwned(mOwnerLabels != null
                && mOwnerLabels.contains(labelView.getText().toString()));
    }
}
