package com.ekuater.labelchat.ui.fragment.confide;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConfideComment;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ViewHolder;
import com.ekuater.labelchat.ui.widget.CircleImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2015/4/7.
 */
public class ConfideDetaileAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private AvatarManager avatarManager;
    private SettingHelper settingHelper;
    private ArrayList<ConfideComment> mComments;
    private GetDateListener getAdapterDate;
    private boolean isCanload;
    private String userId;
    private String sex;
    private View.OnClickListener onClickListener;

    private View mLayout;
    private ProgressBar mProgressBar;
    private TextView mMore;

    public ConfideDetaileAdapter(Context context, GetDateListener getAdapterDate, String userId, String sex, View.OnClickListener onClickListener){
        this.context = context;
        mComments = new ArrayList<>();
        inflater = LayoutInflater.from(context);
        avatarManager =AvatarManager.getInstance(context);
        settingHelper = SettingHelper.getInstance(context);
        this.getAdapterDate = getAdapterDate;
        this.userId = userId;
        this.sex = sex;
        this.onClickListener = onClickListener;

    }

    public void notifyAdapterList(ConfideComment[] comments){
        List<ConfideComment> list = Arrays.asList(comments);
        if (list != null){
            if (list.size() < 20) {
                setInvisibleLayout();
            } else {
                setHideProgress();
            }
            mComments.addAll(list);
            notifyDataSetChanged();
        }
    }

    public List<ConfideComment> getConfideComment() {
        return mComments;
    }


    public void addComment(ConfideComment comment){
        mComments.add(comment);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (mComments.size() >= 20 && position == mComments.size()) {
            return 0;
        }
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;

    }

    public View getGroupTypeView(int viewType, ViewGroup parent){
        View layout;
        switch (viewType) {
            case 0:
                if (mLayout == null) {
                    mLayout =inflater.inflate(R.layout.layout_story_footer, parent, false);
                    mProgressBar = (ProgressBar) com.ekuater.labelchat.ui.util.ViewHolder.get(mLayout, R.id.story_loading);
                    mMore = (TextView) com.ekuater.labelchat.ui.util.ViewHolder.get(mLayout, R.id.story_more);
                    mMore.setText(R.string.p2refresh_head_load_more);
                    mMore.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                }
                layout = mLayout;
                break;
            default:
                layout = inflater.inflate(R.layout.confide_detaile_comment_item, parent, false);
                break;
        }
        return layout;
    }

    public void setInvisibleLayout(){
        isCanload = true;
        if (mLayout!=null) {
            mLayout.setVisibility(View.GONE);
        }
    }
    public void setHideProgress(){
        isCanload = true;
        if (mLayout!=null) {
            mProgressBar.setVisibility(View.GONE);
            mMore.setVisibility(View.VISIBLE);
            mMore.setText(R.string.p2refresh_head_load_more);
        }
    }

    @Override
    public int getCount(){
        return mComments.size() < 20 ? mComments.size() : mComments.size() + 1;
    }

    @Override
    public ConfideComment getItem(int position) {
        return mComments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView=getGroupTypeView(getItemViewType(position), parent);
        }
        if (position != mComments.size()){
            final ConfideComment comment = getItem(position);
            CircleImageView tx = (CircleImageView) ViewHolder.get(convertView, R.id.confide_detaile_item_tx);
            TextView content = (TextView) ViewHolder.get(convertView, R.id.confide_detaile_item_content);
            TextView reply = (TextView) ViewHolder.get(convertView, R.id.confide_detaile_item_reply);
            LinearLayout reply_linnear = (LinearLayout) ViewHolder.get(convertView, R.id.confide_detaile_item_reply_backgroud);
            TextView floor = (TextView) ViewHolder.get(convertView, R.id.confide_detaile_item_floor);
            TextView area = (TextView) ViewHolder.get(convertView, R.id.confide_detaile_item_area);
            TextView time = (TextView) ViewHolder.get(convertView, R.id.confide_detaile_item_time);
            tx.setOnClickListener(onClickListener);
            tx.setTag(comment.getConfideUserId());

            reply.setText(String.format(context.getString(R.string.confide_reply), comment.getReplyFloor()) + comment.getReplayComment());
            reply_linnear.setVisibility(TextUtils.isEmpty(comment.getReplayComment()) ? View.GONE : View.VISIBLE);
            floor.setText(String.format(context.getString(R.string.confide_floor), comment.getFloor()));
            area.setText(TextUtils.isEmpty(comment.getPosition()) ? context.getString(R.string.confide_unknown) : comment.getPosition());
            time.setText(getDateTime(comment.getCreateDate()));
            isShowUserTx(tx, comment);
            if(isMyUserId(comment.getConfideUserId())){
                content.setText(String.format(context.getString(R.string.confide_floorer),comment.getComment()));
                content.setTextColor(context.getResources().getColor(getUserColorText()));
            }else{
                content.setText(comment.getComment());
                content.setTextColor(context.getResources().getColor(R.color.story_content));
            }

        }else{
            if (isCanload) {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isCanload = false;
                        mMore.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        getAdapterDate.getAdapterDate();
                    }
                });
            }
        }
        return convertView;
    }

    private int getUserColorText(){
        return sex.equals("2") ? R.color.confide_female : R.color.confide_male;
    }

    private int getUserBackground(){
        return sex.equals("2") ? R.drawable.confide_female : R.drawable.confide_male;
    }

    private void isShowUserTx(CircleImageView tx, ConfideComment comment){
        if (isMyUserId(settingHelper.getAccountUserId())){
            if (comment.getStranger() == null){
                MiscUtils.showConfideAvatarThumb(avatarManager, comment.getVirtualAvatar(), tx, R.drawable.contact_single);
            }else{
                MiscUtils.showAvatarThumb(avatarManager,comment.getStranger().getAvatarThumb(), tx, R.drawable.contact_single);
            }
        }else{
            if (isMyUserId(comment.getConfideUserId())){
                tx.setImageResource(getUserBackground());
            }else{
                MiscUtils.showConfideAvatarThumb(avatarManager, comment.getVirtualAvatar(), tx, R.drawable.contact_single);
            }
        }
    }

    private boolean isMyUserId(String confideUserId){
        return confideUserId.equals(userId) ? true : false;
    }

    private String getDateTime(long time){
        return DateTimeUtils.getDescriptionTimeFromTimestamp(context, time);
    }

    public interface GetDateListener{
        public void getAdapterDate();
    }
}
