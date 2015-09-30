package com.ekuater.labelchat.ui.fragment.main;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.datastruct.LabelStoryFeedTipMessage;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.ui.util.MiscUtils;

/**
 * @author LinYong
 */
/*package*/ class ContactsListItem {

    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_SEARCH = 1;
    private static final int VIEW_TYPE_FUNCTION = 2;
    private static final int VIEW_TYPE_FOLLOWING = 3;
    private static final int VIEW_TYPE_FOLLOWER = 4;
    private static final int VIEW_TYPE_COUNT = 5;

    public static int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    public interface Item {

        public View newView(LayoutInflater inflater, ViewGroup parent);

        public void bindView(View view);

        public int getViewType();

        public String getSortLetters();

        public void onClick();
    }

    public interface ContactItemListener {
        public void onClick(String labelCode);
    }

    public static class ContactItem implements Item {

        private String mTitle;
        private String mSortLetter;
        private String mLabelCode;
        private String mAvatarUrl;
        private LabelStoryFeedTipMessage mLabelStoryFeedTipMessage;
        private AvatarManager mAvatarManager;
        private ContactItemListener mListener;
        private Context mContext;

        public ContactItem(Context context, String title, String sortLetter, String labelCode,
                           String avatarUrl, LabelStoryFeedTipMessage labelStoryFeedTipMessage,
                           AvatarManager avatarManager, ContactItemListener listener) {
            mTitle = title;
            mSortLetter = sortLetter;
            mLabelCode = labelCode;
            mAvatarUrl = avatarUrl;
            mLabelStoryFeedTipMessage = labelStoryFeedTipMessage;
            mAvatarManager = avatarManager;
            mListener = listener;
            mContext = context;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.contact_normal_item, parent, false);
        }

        @Override
        public void bindView(View view) {
            TextView titleView = (TextView) view.findViewById(R.id.title);
            ImageView iconView = (ImageView) view.findViewById(R.id.icon);
            TextView userName = (TextView) view.findViewById(R.id.frends_user_name);
            TextView labelName = (TextView) view.findViewById(R.id.frends_label_name);
            LinearLayout frendslayout = (LinearLayout) view.findViewById(R.id.frends_layout);

            titleView.setText(mTitle);
            showIcon(iconView);
            if (mLabelStoryFeedTipMessage != null) {
                frendslayout.setVisibility(View.VISIBLE);
                showAddLabel(labelName);
            } else {
                frendslayout.setVisibility(View.GONE);
            }
        }

        @Override
        public int getViewType() {
            return VIEW_TYPE_NORMAL;
        }

        @Override
        public String getSortLetters() {
            return mSortLetter;
        }

        @Override
        public void onClick() {
            if (mListener != null) {
                mListener.onClick(mLabelCode);
            }
        }

        private void showAddLabel(TextView labelName) {
            Log.d("getmFeedType", mLabelStoryFeedTipMessage.toString());
            if (mLabelStoryFeedTipMessage.getmFeedType().equals(LabelStoryFeedTipMessage.DYNAMIC)) {
                String add = String.format((mContext.getString(R.string.contact_label_name_send)), mLabelStoryFeedTipMessage.getmLabelName());
                SpannableString ss = new SpannableString(add);
                ss.setSpan(new ForegroundColorSpan(R.color.contact_label_name), add.indexOf("[") + 1, add.indexOf("]"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                labelName.setText(ss);
            } else if ((mLabelStoryFeedTipMessage.getmFeedType().equals(LabelStoryFeedTipMessage.ADD_LABEL))) {
                String add = String.format((mContext.getString(R.string.contact_label_name_add)), mLabelStoryFeedTipMessage.getmLabelName());
                SpannableString ss = new SpannableString(add);
                ss.setSpan(new ForegroundColorSpan(R.color.contact_label_name), add.indexOf("[") + 1, add.indexOf("]"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                labelName.setText(ss);
            }

        }

        private void showIcon(ImageView imageView) {
            final int defaultIconId = R.drawable.contact_single;

            if (!TextUtils.isEmpty(mAvatarUrl)) {
                MiscUtils.showAvatarThumb(mAvatarManager, mAvatarUrl,
                        imageView, defaultIconId);
            } else {
                imageView.setImageResource(defaultIconId);
            }
        }
    }

    public interface FunctionItemListener {
        public void onClick(String function);
    }

    public static class FunctionItem implements Item {

        private String mTitle;
        private int mIconId;
        private String mFunction;
        private FunctionItemListener mListener;

        public FunctionItem(String title, int icon, String function,
                            FunctionItemListener listener) {
            mTitle = title;
            mIconId = icon;
            mFunction = function;
            mListener = listener;
        }

        @Override
        public String getSortLetters() {
            return "?";
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.contact_function_item, parent, false);
        }

        @Override
        public void bindView(View view) {
            TextView titleView = (TextView) view.findViewById(R.id.title);
            ImageView iconView = (ImageView) view.findViewById(R.id.icon);

            titleView.setText(mTitle);
            iconView.setImageResource(mIconId);
        }

        @Override
        public int getViewType() {
            return VIEW_TYPE_FUNCTION;
        }

        @Override
        public void onClick() {
            if (mListener != null) {
                mListener.onClick(mFunction);
            }
        }
    }

    public interface SearchItemListener {
        public void onSearch();
    }

    public static class SearchItem implements Item {

        private SearchItemListener mListener;

        public SearchItem(SearchItemListener listener) {
            mListener = listener;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.contact_search_item, parent, false);
        }

        @Override
        public void bindView(View view) {
        }

        @Override
        public int getViewType() {
            return VIEW_TYPE_SEARCH;
        }

        @Override
        public String getSortLetters() {
            return "?";
        }

        @Override
        public void onClick() {
            if (mListener != null) {
                mListener.onSearch();
            }
        }
    }

    public interface FollowingItemListener {
        public void onClick(FollowUser followUser);
    }

    public static class FollowingItem implements Item {

        private FollowUser mFollowUser;
        private String mSortLetter;
        private boolean mFollowEachOther;
        private AvatarManager mAvatarManager;
        private FollowingItemListener mListener;

        public FollowingItem(FollowUser followUser, String sortLetter,
                             boolean followEachOther, AvatarManager avatarManager,
                             FollowingItemListener listener) {
            mFollowUser = followUser;
            mSortLetter = sortLetter;
            mFollowEachOther = followEachOther;
            mAvatarManager = avatarManager;
            mListener = listener;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            View view = inflater.inflate(R.layout.following_normal_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.iconView = (ImageView) view.findViewById(R.id.icon);
            holder.titleView = (TextView) view.findViewById(R.id.title);
            holder.extraView = (TextView) view.findViewById(R.id.extra);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            MiscUtils.showAvatarThumb(mAvatarManager, mFollowUser.getAvatarThumb(),
                    holder.iconView);
            holder.titleView.setText(mFollowUser.getNickname());
            holder.extraView.setText(mFollowEachOther ? R.string.follow_each_other
                    : R.string.has_follow);
        }

        @Override
        public int getViewType() {
            return VIEW_TYPE_FOLLOWING;
        }

        @Override
        public String getSortLetters() {
            return mSortLetter;
        }

        @Override
        public void onClick() {
            if (mListener != null) {
                mListener.onClick(mFollowUser);
            }
        }

        private static class ViewHolder {

            public ImageView iconView;
            public TextView titleView;
            public TextView extraView;
        }
    }

    public interface FollowerItemListener {
        public void onClick(FollowUser followUser);

        public void onAddFollowing(FollowUser followUser);
    }

    public static class FollowerItem implements Item {

        private static View.OnClickListener sButtonClickListener
                = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FollowerItem item = (FollowerItem) v.getTag();
                item.onButtonClick();
            }
        };

        private FollowUser mFollowUser;
        private String mSortLetter;
        private boolean mFollowEachOther;
        private AvatarManager mAvatarManager;
        private FollowerItemListener mListener;

        public FollowerItem(FollowUser followUser, String sortLetter,
                            boolean followEachOther, AvatarManager avatarManager,
                            FollowerItemListener listener) {
            mFollowUser = followUser;
            mSortLetter = sortLetter;
            mFollowEachOther = followEachOther;
            mAvatarManager = avatarManager;
            mListener = listener;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            View view = inflater.inflate(R.layout.follower_normal_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.iconView = (ImageView) view.findViewById(R.id.icon);
            holder.titleView = (TextView) view.findViewById(R.id.title);
            holder.extraView = (TextView) view.findViewById(R.id.extra);
            holder.buttonView = view.findViewById(R.id.button);
            holder.buttonView.setOnClickListener(sButtonClickListener);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            MiscUtils.showAvatarThumb(mAvatarManager, mFollowUser.getAvatarThumb(),
                    holder.iconView);
            holder.titleView.setText(mFollowUser.getNickname());
            holder.buttonView.setTag(this);
            if (mFollowEachOther) {
                holder.extraView.setVisibility(View.VISIBLE);
                holder.buttonView.setVisibility(View.GONE);
            } else {
                holder.extraView.setVisibility(View.GONE);
                holder.buttonView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getViewType() {
            return VIEW_TYPE_FOLLOWER;
        }

        @Override
        public String getSortLetters() {
            return mSortLetter;
        }

        @Override
        public void onClick() {
            if (mListener != null) {
                mListener.onClick(mFollowUser);
            }
        }

        private void onButtonClick() {
            if (mListener != null) {
                mListener.onAddFollowing(mFollowUser);
            }
        }

        private static class ViewHolder {

            public ImageView iconView;
            public TextView titleView;
            public TextView extraView;
            public View buttonView;
        }
    }
}
