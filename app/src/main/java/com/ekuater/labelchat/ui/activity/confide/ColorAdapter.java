package com.ekuater.labelchat.ui.activity.confide;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.util.ColorUtils;
import com.ekuater.labelchat.util.L;

/**
 * Created by Leo on 2015/4/8.
 *
 * @author LinYong
 */
public class ColorAdapter extends RecyclerView.Adapter<ColorHolder> {

    private static final String TAG = ColorAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;
    private final ColorHolderListener mHolderListener = new ColorHolderListener() {
        @Override
        public void onClick(ColorHolder holder) {
            onHolderClick(holder);
            L.d(TAG, "onClick(), select color=" + ColorUtils.toColorString(getSelectedColor()));
        }
    };

    private int[] mColors;
    private int mSelectedPos;

    public ColorAdapter(Context context) {
        super();
        mInflater = LayoutInflater.from(context);
    }

    public void updateColors(int[] colors) {
        mColors = colors;
        mSelectedPos = 0;
        notifyDataSetChanged();
    }

    public int getSelectedColor() {
        return mColors[mSelectedPos];
    }

    @Override
    public ColorHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.confide_color_item, parent, false);
        return new ColorHolder(view, mHolderListener);
    }

    @Override
    public void onBindViewHolder(ColorHolder holder, int position) {
        holder.bindView(mColors[position], position == mSelectedPos);
    }

    @Override
    public int getItemCount() {
        return mColors != null ? mColors.length : 0;
    }

    private void onHolderClick(ColorHolder holder) {
        int position = holder.getAdapterPosition();

        if (position != mSelectedPos) {
            int oldPos = mSelectedPos;
            mSelectedPos = position;
            notifyItemChanged(oldPos);
            notifyItemChanged(mSelectedPos);
        }
    }
}
