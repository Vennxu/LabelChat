package com.ekuater.labelchat.ui.fragment.labelstory;

import android.app.Activity;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.PickPhotoUser;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ViewHolder;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.ui.widget.ClickEventIntercept;
import com.ekuater.labelchat.ui.widget.emoji.ShowContentTextView;

import java.util.ArrayList;

/**
 * Created by wenxiang on 2015/3/19.
 */
public class LabelStoryAdapter extends BaseAdapter{

    private LayoutInflater layoutInflater;
    private Activity context;
    private View.OnClickListener mOnClickListener;
    private AvatarManager mAvatarManager;
    private ArrayList<LabelStory> mLabelStories = new ArrayList<>();
    private int mFlag;
    private String mCategoryName = null;
    private ContactsManager contactsManager;
    private Stranger mStranger;


    public LabelStoryAdapter(Activity context,View.OnClickListener onClickListener , int flag) {
        initManager(context,onClickListener,flag);
        mFlag = flag;
        mOnClickListener = onClickListener;

    }
    public LabelStoryAdapter(Activity context,View.OnClickListener onClickListener , int flag,String categroyName) {
        initManager(context,onClickListener,flag);
        mCategoryName = categroyName;
    }
    public LabelStoryAdapter(Activity context,View.OnClickListener onClickListener , int flag,Stranger stranger) {
        initManager(context,onClickListener,flag);
        mStranger = stranger;
    }

    private void initManager(Activity context,View.OnClickListener onClickListener, int flag){
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        mAvatarManager = AvatarManager.getInstance(context);
        contactsManager = ContactsManager.getInstance(context);
        mFlag = flag;
        mOnClickListener = onClickListener;
    }


    public void updateAdapterArrayList(ArrayList<LabelStory> labelStories,int flags){
            switch (flags){
                case LabelStoryUtils.REFRESH_DATA:
                    mLabelStories.clear();
                    mLabelStories.addAll(labelStories);
                    break;
                case LabelStoryUtils.LOADING_DADA:
                    mLabelStories.addAll(labelStories);
                    break;
                default:
                    break;
            }
            notifyDataSetChanged();
    }

    public void removeList(int position){
        mLabelStories.remove(position);
        notifyDataSetChanged();
    }
    public ArrayList<LabelStory> getmLabelStories(){
        return mLabelStories;
    }
    @Override
    public int getCount() {
        return mLabelStories == null ? 0 : mLabelStories.size();
    }

    @Override
    public LabelStory getItem(int position) {
        return mLabelStories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LabelStory labelStory = getItem(position);
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_story_detail_descript,
                    parent, false);
        }
        CircleImageView story_tx = (CircleImageView) ViewHolder.get(convertView, R.id.descript_tx);
        story_tx.setOnClickListener(mOnClickListener);
        story_tx.setTag(position);
        TextView story_name = (TextView) ViewHolder.get(convertView, R.id.descript_name);

        ShowContentTextView story_content = (ShowContentTextView) ViewHolder.get(convertView, R.id.descript_content);
        story_content.setAutoLinkMask(Linkify.ALL);
        ImageView story_content_image = (ImageView) ViewHolder.get(convertView, R.id.descript_image);
        LinearLayout story_label = (LinearLayout) ViewHolder.get(convertView, R.id.story_item_label);
        TextView story_label_name = (TextView) ViewHolder.get(convertView, R.id.story_item_label_name);
        TextView story_time = (TextView) ViewHolder.get(convertView, R.id.descript_time);

        ViewHolder.get(convertView, R.id.operation_bar_more).setOnClickListener(mOnClickListener);
        TextView story_following = (TextView)ViewHolder.get(convertView, R.id.descript_following);
        ImageView story_following_icon = (ImageView) ViewHolder.get(convertView,R.id.descript_following_icon);
        LinearLayout story_following_linnear = (LinearLayout) ViewHolder.get(convertView,R.id.descript_following_linear);
        ImageView story_praise = (ImageView)ViewHolder.get(convertView, R.id.operation_bar_praise);
        ImageView story_more = (ImageView) ViewHolder.get(convertView, R.id.operation_bar_more);
        TextView story_praise_num = (TextView)ViewHolder.get(convertView, R.id.operation_bar_praise_num);
        ViewHolder.get(convertView, R.id.operation_bar_comment_linear).setBackgroundResource(0);
        ClickEventIntercept delete = (ClickEventIntercept) ViewHolder.get(convertView, R.id.operation_bar_delete);
        delete.setOnClickListener(mOnClickListener);
        delete.setTag(position);
        LinearLayout story_show_user = (LinearLayout) ViewHolder.get(convertView, R.id.operation_show_user);

        ClickEventIntercept story_user_click = (ClickEventIntercept) ViewHolder.get(convertView, R.id.operation_bar_user_area);
        story_user_click.setOnClickListener(mOnClickListener);
        story_user_click.setTag(position);
        HorizontalListView story_user_list = (HorizontalListView) ViewHolder.get(convertView, R.id.operation_bar_user);
        TextView story_user_number = (TextView) ViewHolder.get(convertView, R.id.operation_bar_user_area_read_number);

//            TextView story_comment_num = (TextView)ViewHolder.get(convertView, R.id.operation_bar_comment_num);
        ImageView story_letter = (ImageView) ViewHolder.get(convertView, R.id.operation_bar_letter);
        story_letter.setOnClickListener(mOnClickListener);
        story_letter.setTag(position);
        story_praise.setOnClickListener(mOnClickListener);
        story_praise.setTag(position);
        story_following.setOnClickListener(mOnClickListener);
        story_following.setTag(position);
        story_more.setOnClickListener(mOnClickListener);
        story_more.setTag(position);
        story_label.setOnClickListener(mOnClickListener);
        story_label.setTag(labelStory.getCategory());

        story_praise_num.setText(labelStory.getPraise());
        story_time.setText(getTimeString(labelStory.getCreateDate()).trim());
        if ("Y".equals(labelStory.getIsPraise())){
            story_praise.setImageResource(R.drawable.ic_praise_pressed);
        }else{
            story_praise.setImageResource(R.drawable.ic_praise_normal);
        }
//      story_comment_num.setText(labelStory.getCommentNum());

        if (labelStory.getThumbImages() != null && labelStory.getThumbImages().length > 0) {
            story_content_image.setVisibility(View.VISIBLE);
            MiscUtils.showLabelStoryImageThumb(mAvatarManager, labelStory.getThumbImages()[0], story_content_image, R.drawable.pic_loading);
        } else {
            story_content_image.setVisibility(View.GONE);
        }
        if (mCategoryName == null) {
            if (labelStory.getCategory() != null) {
                story_label_name.setText(labelStory.getCategory().getmCategoryName());
                story_label.setVisibility(View.VISIBLE);
            } else {
                story_label.setVisibility(View.GONE);
            }
        }else{
            story_label_name.setText(mCategoryName);
            story_label.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(labelStory.getContent())) {
            story_content.setVisibility(View.GONE);
        } else {
            story_content.setVisibility(View.VISIBLE);
            story_content.setText(labelStory.getContent());
        }
        isFollowing(labelStory, story_following, story_name ,story_tx,story_show_user,story_user_list, story_user_number,story_following_icon,story_following_linnear);
        return convertView;
    }

    private String getTimeString(long time) {
        return DateTimeUtils.getDescriptionTimeFromTimestamp(context,time);
    }

    private void isFollowing(LabelStory labelStory,TextView follwing, TextView name, CircleImageView tx,
                             LinearLayout story_show_user,HorizontalListView list, TextView show_num,ImageView following_icon,
                             LinearLayout following_icon_linear){
        switch (mFlag){
            case LabelStoryUtils.FOLLOW:
                story_show_user.setVisibility(View.GONE);
                if ( labelStory.getStranger() != null){
                    MiscUtils.showAvatarThumb(mAvatarManager, labelStory.getStranger().getAvatarThumb(), tx);
                    name.setText(labelStory.getStranger().getNickname());
                }
                if (isContact(labelStory.getStranger().getUserId())) {
                    follwing.setTextColor(context.getResources().getColor(R.color.check_more));
                    follwing.setText(R.string.main_activity_tab_friends_description);
                    following_icon.setImageResource(R.drawable.friends_icon);
                    following_icon_linear.setBackgroundResource(R.drawable.friends);
                }else{
                    follwing.setTextColor(context.getResources().getColor(R.color.followed));
                    follwing.setText(R.string.labelstory_attentioned);
                    following_icon.setImageResource(R.drawable.followed_icon);
                    following_icon_linear.setBackgroundResource(R.drawable.followed);
                }

                break;
            case LabelStoryUtils.MY:
                if(mStranger != null) {
                    MiscUtils.showAvatarThumb(mAvatarManager,mStranger.getAvatarThumb(), tx);
                    name.setText(mStranger.getNickname());
                    story_show_user.setVisibility(View.GONE);
                    following_icon_linear.setVisibility(View.VISIBLE);
                    if (isContact(mStranger.getUserId())){
                        follwing.setTextColor(context.getResources().getColor(R.color.check_more));
                        following_icon.setImageResource(R.drawable.friends_icon);
                        follwing.setText(R.string.main_activity_tab_friends_description);
                        following_icon_linear.setBackgroundResource(R.drawable.friends);
                    }else{
                        if ("Y".equals(labelStory.getIsFollowing())) {
                            follwing.setTextColor(context.getResources().getColor(R.color.followed));
                            following_icon.setImageResource(R.drawable.followed_icon);
                            follwing.setText(R.string.labelstory_attentioned);
                            following_icon_linear.setBackgroundResource(R.drawable.followed);
                        } else {
                            follwing.setTextColor(context.getResources().getColor(R.color.follow));
                            following_icon.setImageResource(R.drawable.follow_icon);
                            follwing.setText(R.string.labelstory_attention);
                            following_icon_linear.setBackgroundResource(R.drawable.follow);
                        }
                    }
                }else{
                    MiscUtils.showAvatarThumb(mAvatarManager, SettingHelper.getInstance(context).getAccountAvatarThumb(), tx);
                    name.setText(SettingHelper.getInstance(context).getAccountNickname());
                    story_show_user.setVisibility(View.VISIBLE);
                    if (labelStory.getPickPhotoUser() != null) {
                        Log.d("adapter", labelStory.getPickPhotoUser().toString());
                    }
                    list.setAdapter(new ShowUserAdapter(labelStory.getPickPhotoUser()));
                    show_num.setText(labelStory.getBrowseNum());
                    following_icon_linear.setVisibility(View.GONE);
                }
                break;
            default:
                story_show_user.setVisibility(View.GONE);
                if ( labelStory.getStranger() != null){
                    MiscUtils.showAvatarThumb(mAvatarManager, labelStory.getStranger().getAvatarThumb(), tx);
                    name.setText(labelStory.getStranger().getNickname());
                }


                if (labelStory.getStranger().getUserId().equals(SettingHelper.getInstance(context).getAccountUserId())) {
                    following_icon_linear.setVisibility(View.GONE);
                }else{
                    if (isContact(labelStory.getStranger().getUserId())){
                        follwing.setTextColor(context.getResources().getColor(R.color.check_more));
                        following_icon.setImageResource(R.drawable.friends_icon);
                        follwing.setText(R.string.main_activity_tab_friends_description);
                        following_icon_linear.setBackgroundResource(R.drawable.friends);
                    }else{
                        if ("Y".equals(labelStory.getIsFollowing())) {
                            follwing.setTextColor(context.getResources().getColor(R.color.followed));
                            following_icon.setImageResource(R.drawable.followed_icon);
                            follwing.setText(R.string.labelstory_attentioned);
                            following_icon_linear.setBackgroundResource(R.drawable.followed);
                        } else {
                            follwing.setTextColor(context.getResources().getColor(R.color.follow));
                            following_icon.setImageResource(R.drawable.follow_icon);
                            follwing.setText(R.string.labelstory_attention);
                            following_icon_linear.setBackgroundResource(R.drawable.follow);
                        }
                    }
                    following_icon_linear.setVisibility(View.VISIBLE);
                }
                break;

        }


    }

    private boolean isContact(String contactId){
       UserContact[] userContacts = contactsManager.getAllUserContact();
        if (userContacts != null) {
            for (UserContact userContact : userContacts) {
                if (userContact.getUserId().equals(contactId)) {
                    return true;
                }
            }
        }
         return false;
    }
    private class ShowUserAdapter extends BaseAdapter {
        private PickPhotoUser[] pPickPhotoUser=null;
        private LayoutInflater inflater;
        public  ShowUserAdapter(PickPhotoUser[] pickPhotoUser) {
            pPickPhotoUser=pickPhotoUser;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return pPickPhotoUser == null ? 0 : pPickPhotoUser.length;
        }

        @Override
        public PickPhotoUser getItem(int position) {
            return pPickPhotoUser[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.labelstory_praise_user_image, parent, false);
            }
            ImageView imageView = (ImageView) ViewHolder.get(convertView, R.id.labelstory_praise_iamge);
            MiscUtils.showAvatarThumb(mAvatarManager, getItem(position).getPickUserAvatarThumb(), imageView);
            return convertView;
        }
    }
}
