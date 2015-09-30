package com.ekuater.labelchat.ui.fragment.labelstory;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.LabelStoryComments;
import com.ekuater.labelchat.datastruct.PersonalUser;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.friends.StrangerHelper;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.util.ViewHolder;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.ui.widget.emoji.ShowContentTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Label on 2015/2/5.
 *
 * @author Xu wenxiang
 */
class LabelStoryDetailAdapter extends BaseAdapter {

    private ArrayList<LabelStoryComments> mLabelStoryCommentses = new ArrayList<LabelStoryComments>();
    private Context mContext;
    private LayoutInflater mInflater;
    private AvatarManager mAvatarManager;
    private View mLayout;
    private ProgressBar mProgressBar;
    private TextView mMore;
    private boolean isCanload = false;
    private GetDateListener mGetDateListener;
    private StrangerHelper strangerHelper;
    private Stranger mStranger;

    public LabelStoryDetailAdapter(Context context, Fragment fragment, Stranger stranger, GetDateListener getDateListener) {
        iniManager(context, fragment);
        mStranger = stranger;
        mGetDateListener = getDateListener;
    }

    private void iniManager(Context context, Fragment fragment) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mAvatarManager = AvatarManager.getInstance(mContext);
        mInflater = LayoutInflater.from(mContext);
        mAvatarManager = AvatarManager.getInstance(mContext);
        strangerHelper = new StrangerHelper(fragment);
    }

    public void overrideArrayList(List<LabelStoryComments> labelStoryCommentses) {
        mLabelStoryCommentses.addAll(labelStoryCommentses);
        notifyDataSetChanged();
    }

    public void updateGroupComment(LabelStoryComments labelStoryCommentses) {
        mLabelStoryCommentses.add(labelStoryCommentses);
        notifyDataSetChanged();
    }


    public ArrayList<LabelStoryComments> getArray() {
        return mLabelStoryCommentses;
    }

    public void setInvisibleLayout() {
        isCanload = true;
        if (mLayout != null) {
            mLayout.setVisibility(View.GONE);
        }
    }

    public void setHideProgress() {
        isCanload = true;
        if (mLayout != null) {
            mProgressBar.setVisibility(View.GONE);
            mMore.setVisibility(View.VISIBLE);
            mMore.setText(R.string.p2refresh_head_load_more);
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (mLabelStoryCommentses.size() >= 20 && position == mLabelStoryCommentses.size()) {
            return 0;
        }
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public View getGroupTypeView(int viewType, ViewGroup parent) {
        View layout;
        switch (viewType) {
            case 0:
                if (mLayout == null) {
                    mLayout = mInflater.inflate(R.layout.layout_story_footer, parent, false);
                    mProgressBar = (ProgressBar) ViewHolder.get(mLayout, R.id.story_loading);
                    mMore = (TextView) ViewHolder.get(mLayout, R.id.story_more);
                    mMore.setText(R.string.p2refresh_head_load_more);
                    mMore.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                }
                layout = mLayout;
                break;
            default:
                layout = mInflater.inflate(R.layout.layout_comment_group, parent, false);
                break;
        }
        return layout;
    }

    public void showStrangerDetailUI(Stranger user) {
        ContactsManager contactsManager = ContactsManager.getInstance(mContext);
        if (user != null) {
            if (user.getUserId().equals(SettingHelper.getInstance(mContext).getAccountUserId())) {
                UILauncher.launchMyInfoUI(mContext);
            } else if (contactsManager.getUserContactByUserId(user.getUserId()) == null) {
                UILauncher.launchPersonalDetailUI(mContext, new PersonalUser(PersonalUser.STRANGER, new UserContact(user)));
            } else if (contactsManager.getUserContactByUserId(user.getUserId()) != null) {
                for (UserContact userContact : contactsManager.getAllUserContact()) {
                    if (userContact.getUserId().equals(user.getUserId())) {
                        UILauncher.launchPersonalDetailUI(mContext, new PersonalUser(PersonalUser.CONTACT, userContact));
                    }
                }
            } else {
                ShowToast.makeText(mContext, R.drawable.emoji_sad, mContext.getResources().getString(R.string.query_stranger_failed)).show();
            }
        }
    }

    private String getTimeString(long time) {
        return DateTimeUtils.getDescriptionTimeFromTimestamp(mContext, time);
    }

    @Override
    public int getCount() {
        return mLabelStoryCommentses.size() < 20 ? mLabelStoryCommentses.size() : mLabelStoryCommentses.size() + 1;
    }

    @Override
    public LabelStoryComments getItem(int position) {
        return mLabelStoryCommentses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = getGroupTypeView(getItemViewType(position), parent);
        }
        if (position != mLabelStoryCommentses.size()) {
            final LabelStoryComments comment = getItem(position);
            CircleImageView groupTx = (CircleImageView) convertView.findViewById(R.id.comment_tx);
            TextView groupName = (TextView) convertView.findViewById(R.id.comment_name);
            TextView groupTime = (TextView) convertView.findViewById(R.id.comment_time);
            ShowContentTextView groupContent = (ShowContentTextView) convertView.findViewById(R.id.comment_content);
            if (comment.getmStranger() != null) {
                Stranger stranger = comment.getmStranger();
                String title = MiscUtils.getUserRemarkName(mContext, stranger.getUserId());
                groupName.setText(title != null && title.length() > 0 ? title : stranger.getNickname());
                if (TextUtils.isEmpty(comment.getmReplyNickName())) {
                    groupContent.setText(comment.getmStoryComment());
                } else {
                    String content = mContext.getString(R.string.someone_reply) + comment.getmReplyNickName() + " " + comment.getmStoryComment();
                    SpannableString ss = new SpannableString(content);
                    ForegroundColorSpan redSpan = new ForegroundColorSpan(mContext.getResources().getColor(R.color.story_content));
                    ss.setSpan(redSpan, 3, 3 + comment.getmReplyNickName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    groupContent.setText(ss);
                }
                MiscUtils.showAvatarThumb(mAvatarManager, comment.getmStranger().getAvatarThumb(), groupTx, R.drawable.contact_single);
            }
            groupTime.setText(getTimeString(comment.getmCreateDate()));
            groupTx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showStrangerDetailUI(comment.getmStranger());
                }
            });
        } else {
            if (isCanload) {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isCanload = false;
                        mMore.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        mGetDateListener.getAdapterDate();
                    }
                });
            }
        }

        return convertView;
    }

    public interface GetDateListener {
        public void getAdapterDate();

        public void onPraise(String storyCommentId, int position);
    }
}
