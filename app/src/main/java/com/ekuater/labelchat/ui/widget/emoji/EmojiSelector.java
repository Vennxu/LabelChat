
package com.ekuater.labelchat.ui.widget.emoji;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.widget.emoji.pagerindicator.CirclePageIndicator;
import com.ekuater.labelchat.ui.widget.emoji.util.EmojiAdapter;
import com.ekuater.labelchat.ui.widget.emoji.util.EmojiFace;
import com.ekuater.labelchat.ui.widget.emoji.util.EmojiViewPageAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EmojiSelector extends LinearLayout implements
        OnItemClickListener {

    private static final int PORT_COLUMN = 7;
    private static final int PORT_ROW = 3;

    private static final String BACKSPACE_KEY = "backspace";
    private static final int BACKSPACE_ICON = R.drawable.emoji_ic_delete;

    private OnEmojiClickedListener mOnEmojiClickedListener;

    public void setOnEmojiClickedListener(
            OnEmojiClickedListener onEmojiClickedListener) {
        mOnEmojiClickedListener = onEmojiClickedListener;
    }

    public interface OnEmojiClickedListener {

        public void onEmojiClicked(String emoji);

        public void onBackspace();
    }

    public EmojiSelector(Context context) {
        super(context);
    }

    public EmojiSelector(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (isInEditMode()) {
            return;
        }

        final ViewPager viewPager = (ViewPager) findViewById(R.id.emoji_selector_child_pager);
        final Context context = getContext();
        final EmojiFace emojiFace = EmojiFace.getInstance(context);
        final Set<String> faceSet = emojiFace.getFaceMap().keySet();
        final int totalCount = faceSet.size();
        final String[] faces = faceSet.toArray(new String[totalCount]);
        final int pageEmojiCount = getPageEmojiCount();
        final List<GridView> gridList = new ArrayList<GridView>();
        final LayoutInflater inflater = LayoutInflater.from(context);

        if (totalCount > 0) {
            int countLeft = totalCount;
            int startIdx = 0;

            do {
                final int count = Math.min(countLeft, pageEmojiCount);
                final String[] pageFaces = new String[count];
                System.arraycopy(faces, startIdx, pageFaces, 0, count);
                gridList.add(newEmojiGridView(inflater, viewPager, emojiFace, pageFaces));
                countLeft -= count;
                startIdx += count;
            } while (countLeft > 0);
        }
        EmojiViewPageAdapter adapter = new EmojiViewPageAdapter(gridList);
        viewPager.setAdapter(adapter);
        CirclePageIndicator indicator = (CirclePageIndicator) findViewById(
                R.id.emoji_selector_indicator);
        indicator.setViewPager(viewPager);
    }

    private int getPageEmojiCount() {
        return PORT_COLUMN * PORT_ROW - 1;
    }

    private GridView newEmojiGridView(LayoutInflater inflater, ViewGroup parent,
                                      EmojiFace emojiFace, String[] faces) {
        GridView gridView = (GridView) inflater.inflate(R.layout.emoji_grid, parent, false);
        gridView.setNumColumns(PORT_COLUMN);
        gridView.setAdapter(new EmojiAdapter(inflater, emojiFace, faces,
                BACKSPACE_KEY, BACKSPACE_ICON));
        gridView.setOnTouchListener(forbiddenScroll());
        gridView.setOnItemClickListener(this);
        return gridView;
    }

    private OnTouchListener forbiddenScroll() {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        };
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        final EmojiAdapter adapter = (EmojiAdapter) parent.getAdapter();
        final String faceTag = adapter.getFaceTag(position);

        if (TextUtils.isEmpty(faceTag)) {
            return;
        }

        if (mOnEmojiClickedListener != null) {
            if (BACKSPACE_KEY.equals(faceTag)) {
                mOnEmojiClickedListener.onBackspace();
            } else {
                mOnEmojiClickedListener.onEmojiClicked(faceTag);
            }
        }
    }
}
