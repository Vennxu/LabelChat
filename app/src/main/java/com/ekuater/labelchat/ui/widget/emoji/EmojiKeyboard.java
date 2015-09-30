
package com.ekuater.labelchat.ui.widget.emoji;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.widget.emoji.util.EmojiFace;
import com.ekuater.labelchat.ui.widget.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EmojiKeyboard extends FrameLayout {

    private final static String EMOJI_PREFERENCE = "emoji_preferences";
    private final static String PREF_KEY_LAST_TAB = "last_tab";
    private final static String PREF_KEY_RECENT_EMOJI = "recent_remoji";
    private final static int[] ICONS = {
            R.drawable.emoji_ic_recent_light,
            R.drawable.emoji_ic_people_light
    };
    private ViewPager mPager;
    private ImageButton mBackSpace;
    private PagerSlidingTabStrip mTabs;
    private View mEmptyView;
    private GridView mRecentGridView;
    private SharedPreferences mPreference;
    private ArrayList<View> mViews;
    private EmojiPagerAdapter mPagerAdapter;
    private Handler mHandler = new Handler();
    private boolean mContinueDel;
    private EmojiFace mEmojiFace;

    private EventListener mListener;

    public void setEventListener(EventListener listener) {
        mListener = listener;
    }

    public interface EventListener {
        public void onBackspace();

        public void onEmojiSelected(String res);
    }

    public EmojiKeyboard(Context context) {
        super(context);
        init();
    }

    public EmojiKeyboard(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public EmojiKeyboard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        mEmojiFace = EmojiFace.getInstance(getContext());
        mPreference = getContext().getSharedPreferences(EMOJI_PREFERENCE,
                Context.MODE_PRIVATE);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View root = inflater.inflate(R.layout.emoji_keyboard, this);
        mTabs = (PagerSlidingTabStrip) root.findViewById(R.id.emoji_keyboard_tabs);
        mPager = (ViewPager) root.findViewById(R.id.emoji_keyboard_parent_pager);
        mBackSpace = (ImageButton) root.findViewById(R.id.emoji_keyboard_back_space);
        mViews = new ArrayList<View>();
        for (int i = 0; i < ICONS.length; i++) {
            if (i == 0) {
                EmojiGridAdapter emojiGridAdapter = new EmojiGridAdapter(
                        getContext(), new String[]{});
                View mRecentsWrap = inflater.inflate(R.layout.emoji_tab_recent, null);
                mRecentGridView = (GridView) mRecentsWrap
                        .findViewById(R.id.emoji_grid);
                mRecentGridView.setAdapter(emojiGridAdapter);
                mRecentGridView
                        .setOnItemClickListener(mRecentItemClickListener);
                mEmptyView = mRecentsWrap.findViewById(R.id.emoji_tab_recent_no_recent);
                mViews.add(mRecentsWrap);
            } else {
                EmojiSelector emojiView = (EmojiSelector) inflater.inflate(R.layout.emoji_selector, null);
                emojiView.setOnEmojiClickedListener(mOnEmojiClickedListener);
                mViews.add(emojiView);
            }
        }
        loadRecent();
        mPagerAdapter = new EmojiPagerAdapter();
        mPager.setAdapter(mPagerAdapter);
        mTabs.setOnPageChangeListener(mOnPageChangeListener);
        mTabs.setViewPager(mPager);
        mPager.setCurrentItem(mPreference.getInt(PREF_KEY_LAST_TAB, 0));
        mBackSpace.setOnClickListener(mBackSpaceClickListener);
        mBackSpace.setOnLongClickListener(mBackSpaceLongClickListener);
        mBackSpace.setOnTouchListener(mBackSpaceTouchListener);
    }

    private OnItemClickListener mRecentItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            String selected = (String) view.getTag(view.getId());
            if (mListener != null)
                mListener.onEmojiSelected(selected);
        }
    };
    private OnClickListener mBackSpaceClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onBackspace();
            }
        }
    };
    private OnLongClickListener mBackSpaceLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mListener == null) {
                return false;
            }
            mContinueDel = true;
            mHandler.post(mContinueDelRunnable);
            return false;
        }
    };

    private OnTouchListener mBackSpaceTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            if ((action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
                    && mContinueDel) {
                mContinueDel = false;
                mHandler.removeCallbacks(mContinueDelRunnable);
            }
            return false;
        }
    };

    private Runnable mContinueDelRunnable = new Runnable() {
        @Override
        public void run() {
            if (mContinueDel) {
                mListener.onBackspace();
                mHandler.postDelayed(this, 50);
            }
        }
    };

    private EmojiSelector.OnEmojiClickedListener mOnEmojiClickedListener
            = new EmojiSelector.OnEmojiClickedListener() {

        @Override
        public void onEmojiClicked(String emoji) {
            if (TextUtils.isEmpty(emoji)) {
                return;
            }
            if (mListener != null) {
                mListener.onEmojiSelected(emoji);
            }
            addToRecent(emoji);
        }

        @Override
        public void onBackspace() {
        }
    };
    private ViewPager.OnPageChangeListener mOnPageChangeListener
            = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int item) {
            mPreference.edit().putInt(PREF_KEY_LAST_TAB, item).apply();
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    private void loadRecent() {
        String recentEmoji = mPreference.getString(PREF_KEY_RECENT_EMOJI, null);
        if (!TextUtils.isEmpty(recentEmoji)) {
            mEmptyView.setVisibility(View.GONE);
            String[] recentEmojis = recentEmoji.split(",");
            Collections.reverse(Arrays.asList(recentEmojis));
            EmojiGridAdapter recentAdapter = (EmojiGridAdapter) mRecentGridView
                    .getAdapter();
            recentAdapter.setEmoji(recentEmojis);
            recentAdapter.notifyDataSetChanged();
        }
    }

    private void addToRecent(String selected) {
        String recentEmoji = mPreference.getString(PREF_KEY_RECENT_EMOJI, null);
        if (TextUtils.isEmpty(recentEmoji)) {
            recentEmoji = selected + ",";
        } else {
            String[] recs = recentEmoji.split(",");
            List<String> list = Arrays.asList(recs);
            List<String> newList = new ArrayList<String>(list);
            for (int i = newList.size() - 1; i >= 0; i--) {
                if (newList.get(i).equals(selected)) {
                    newList.remove(i);
                    break;
                }
            }
            newList.add(selected);
            if (newList.size() > 21)
                newList.remove(0);// 大于21个表情时，删除最后一个
            StringBuilder builder = new StringBuilder();
            for (String str : newList) {
                builder.append(str).append(",");
            }
            recentEmoji = builder.toString();
        }
        mPreference.edit().putString(PREF_KEY_RECENT_EMOJI, recentEmoji).apply();
        loadRecent();
    }

    public static void input(EmojiEditText editText, String emoji) {
        if (editText == null || emoji == null) {
            return;
        }

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start < 0) {
            editText.append(emoji);
        } else {
            editText.getText().replace(Math.min(start, end),
                    Math.max(start, end), emoji, 0, emoji.length());
        }
    }

    public static void backspace(EmojiEditText editText) {
        KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0,
                0, KeyEvent.KEYCODE_ENDCALL);
        editText.dispatchKeyEvent(event);
    }

    class EmojiGridAdapter extends BaseAdapter {

        private String[] mEmojis;
        private LayoutInflater mInflater;

        public EmojiGridAdapter(Context c, String[] emojis) {
            mInflater = LayoutInflater.from(c);
            mEmojis = emojis;
        }

        public void setEmoji(String[] emojis) {
            mEmojis = emojis;
        }

        public int getCount() {
            return mEmojis.length;
        }

        public Object getItem(int position) {
            return mEmojis[position];
        }

        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (convertView == null) {
                rowView = mInflater.inflate(R.layout.emoji_cell, null);
                ViewHolder viewHolder = new ViewHolder((ImageView) rowView);
                rowView.setTag(viewHolder);
            }

            ViewHolder viewHolder = (ViewHolder) rowView.getTag();
            int resId = mEmojiFace.getStaticFaceId(mEmojis[position]);
            viewHolder.imageView.setImageResource(resId);
            viewHolder.imageView.setTag(viewHolder.imageView.getId(),
                    mEmojis[position]);
            return rowView;
        }
    }

    static class ViewHolder {
        public ImageView imageView;

        public ViewHolder(ImageView imageView) {
            this.imageView = imageView;
        }
    }

    private class EmojiPagerAdapter extends PagerAdapter implements
            PagerSlidingTabStrip.IconTabProvider {

        private EmojiPagerAdapter() {
        }

        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object paramObject) {
            View localObject = mViews.get(position);
            container.removeView(localObject);
        }

        @Override
        public int getCount() {
            return mViews.size();
        }

        @Override
        public int getPageIconResId(int paramInt) {
            return ICONS[paramInt];
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View gridView = mViews.get(position);
            container.addView(gridView);
            return gridView;
        }

        @Override
        public boolean isViewFromObject(View paramView, Object paramObject) {
            return paramView == paramObject;
        }
    }
}
