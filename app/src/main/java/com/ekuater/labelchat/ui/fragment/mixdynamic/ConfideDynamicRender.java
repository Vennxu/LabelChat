package com.ekuater.labelchat.ui.fragment.mixdynamic;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.ConfideComment;
import com.ekuater.labelchat.datastruct.mixdynamic.DynamicWrapper;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ConfideManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.ui.widget.ClickEventInterceptLinear;
import com.ekuater.labelchat.ui.widget.FlowLayout;
import com.ekuater.labelchat.util.TextUtil;

/**
 * Created by Leo on 2015/4/16.
 *
 * @author LinYong
 */
public class ConfideDynamicRender implements DynamicRender, View.OnClickListener {

    private final Context context;
    private final ConfideDynamicListener listener;
    private final ViewHolder holder;
    private final AvatarManager avatarManager;
    private final String myUserId;
    private final String avatarThumb;

    private DynamicWrapper boundWrapper;
    private int boundPosition;
    private boolean isLoadComment;
    private LayoutInflater mInflater;

    public ConfideDynamicRender(Context context, boolean isLoadComment, ConfideDynamicListener listener) {
        this.context = context;
        this.listener = listener;
        this.isLoadComment = isLoadComment;
        this.holder = new ViewHolder();
        this.avatarManager = AvatarManager.getInstance(context);
        SettingHelper helper = SettingHelper.getInstance(context);
        this.myUserId = helper.getAccountUserId();
        this.avatarThumb = helper.getAccountAvatarThumb();
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.confide_show_item, parent, false);
        holder.rootView = view;
        holder.linearLayout = (RelativeLayout) view.findViewById(R.id.confide_show_item_parent);
        holder.tx = (CircleImageView) view.findViewById(R.id.confide_show_item_tx);
        holder.role = (TextView) view.findViewById(R.id.confide_show_item_role);
        holder.time = (TextView) view.findViewById(R.id.confide_show_item_time);
        holder.area = (TextView) view.findViewById(R.id.confide_show_item_area);
        holder.operationBar = (LinearLayout) view.findViewById(R.id.operation_bar);
        holder.praiseLinear = (LinearLayout) view.findViewById(R.id.operation_bar_comment_linear);
        holder.commentNum = (TextView) view.findViewById(R.id.operation_bar_comment_num);
        holder.praiseNum = (TextView) view.findViewById(R.id.operation_bar_praise_num);
        holder.praise = (ImageView) view.findViewById(R.id.operation_bar_praise);
        holder.commentLinear = (ClickEventInterceptLinear) view.findViewById(R.id.operation_bar_comment_parent);
        holder.comment = (ImageView) view.findViewById(R.id.operation_bar_comment);
        holder.letter = (ImageView) view.findViewById(R.id.operation_bar_letter);
        holder.letterNum = (TextView) view.findViewById(R.id.operation_bar_letter_num);
        holder.more = (ImageView) view.findViewById(R.id.operation_bar_more);
        holder.content = (TextView) view.findViewById(R.id.confide_show_item_content);
        holder.story_comment_list = (LinearLayout) view.findViewById(R.id.comment_list);
        holder.story_comment_parent = (LinearLayout) view.findViewById(R.id.comment_parent);
        holder.story_comment_tag = (TextView) view.findViewById(R.id.comment_tag);
        holder.story_comment_praise_list = (FlowLayout) view.findViewById(R.id.comment_praise_list);
        holder.story_comment_praise_relative = (RelativeLayout) view.findViewById(R.id.comment_praise_relative);
        holder.story_comment_praise_list.setVerticalGap(20);
        holder.story_comment_praise_list.setHorizontalGap(20);
        holder.praise.setImageResource(R.drawable.ic_praise_white);
        holder.more.setImageResource(R.drawable.ic_more_white);
        holder.comment.setImageResource(R.drawable.ic_translation_white);
        holder.commentNum.setTextColor(Color.WHITE);
        holder.praiseNum.setTextColor(Color.WHITE);
        holder.letterNum.setTextColor(Color.WHITE);
        holder.letter.setVisibility(View.GONE);
        holder.operationBar.setBackgroundResource(0);
        return view;
    }

    @Override
    public void bindEvents() {
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLoadComment) {
                    listener.onConfideItemClick(getBoundConfide(), false,getBoundPosition());
                }
            }
        });
        holder.rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return listener.onConfideItemLongClick(getBoundConfide(), getBoundPosition());
            }
        });
        holder.praise.setOnClickListener(this);
        holder.more.setOnClickListener(this);
        holder.commentLinear.setOnClickListener(this);
        holder.story_comment_tag.setOnClickListener(this);
    }

    @Override
    public void bindView(DynamicWrapper dynamicWrapper, int position) {
        boundWrapper = dynamicWrapper;
        boundPosition = position;
        bindConfide((Confide) boundWrapper.getDynamic());
    }

    @Override
    public void unbindView() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.operation_bar_praise:
                listener.onConfidePraise(getBoundConfide(), getBoundPosition());
                break;
            case R.id.comment_tag:
                listener.onConfideComment(boundPosition);
                break;
            case R.id.operation_bar_more:
                listener.onConfideMoerClick(getBoundConfide(), boundPosition, v);
                break;
            case R.id.operation_bar_comment_parent:
                if (!isLoadComment) {
                    listener.onConfideItemClick(getBoundConfide(), true, getBoundPosition());
                }else{
                    listener.onConfideComment(boundPosition);
                }
                break;
            default:
                break;
        }
    }

    private void bindConfide(Confide confide) {
        if (TextUtil.isEmpty(confide.getConfideBgImg())){
            holder.linearLayout.setBackgroundColor(confide.parseBgColor());
        }else{
            holder.linearLayout.setBackgroundResource(ConfideManager.getInstance(context).getConfideBs().get(confide.getConfideBgImg()));
        }
        holder.role.setText(String.format(context.getString(R.string.confide_role),
                confide.getConfideRole()));
        holder.time.setText(getDateTime(confide.getConfideCreateDate()));
        holder.commentNum.setText(confide.getConfideCommentNum()+"");
        holder.commentNum.setVisibility(confide.getConfideCommentNum() == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.praiseNum.setVisibility(confide.getConfidePraiseNum() == 0 ? View.INVISIBLE:View.VISIBLE);
        holder.praiseNum.setText(confide.getConfidePraiseNum() + "");
        holder.letterNum.setTextColor(Color.WHITE);
        holder.letterNum.setText(TextUtils.isEmpty(confide.getConfidePosition())
                ? context.getString(R.string.confide_unknown) : confide.getConfidePosition());
        holder.content.setText(confide.getConfideContent());
        holder.area.setText(TextUtils.isEmpty(confide.getConfidePosition())
                ? context.getString(R.string.confide_unknown) : confide.getConfidePosition());
        holder.praise.setImageResource(confide.getConfideIsPraise().equals("Y") ? R.drawable.ic_praise_pressed:R.drawable.ic_praise_white);
        if (myUserId.equals(confide.getConfideUserId())) {
            MiscUtils.showAvatarThumb(avatarManager, avatarThumb,
                    holder.tx, R.drawable.contact_single);
        } else {
            holder.tx.setImageResource(getUserTx(confide.getConfideSex()));
        }
        holder.story_comment_praise_relative.setVisibility(View.GONE);
        if (isLoadComment) {
            holder.story_comment_parent.setVisibility(View.VISIBLE);
            holder.story_comment_list.removeAllViews();
            if (confide.getConfideComments() != null && confide.getConfideComments().length > 0) {
                loadingComment(confide.getConfideComments());
            }
        } else {
            holder.praiseLinear.setBackgroundResource(0);
            holder.story_comment_parent.setVisibility(View.GONE);
        }
    }
    private void loadingComment(ConfideComment[] commentses){
        for (int i = 0; i < commentses.length ; ++i) {
            final int childPosition = i;
            View view = mInflater.inflate(R.layout.confide_detaile_comment_item, holder.story_comment_parent, false);
            final ConfideComment comment = commentses[childPosition];
            CircleImageView tx = (CircleImageView) com.ekuater.labelchat.ui.util.ViewHolder.get(view, R.id.confide_detaile_item_tx);
            TextView content = (TextView) com.ekuater.labelchat.ui.util.ViewHolder.get(view, R.id.confide_detaile_item_content);
            TextView reply = (TextView) com.ekuater.labelchat.ui.util.ViewHolder.get(view, R.id.confide_detaile_item_reply);
            LinearLayout reply_linnear = (LinearLayout) com.ekuater.labelchat.ui.util.ViewHolder.get(view, R.id.confide_detaile_item_reply_backgroud);
            TextView floor = (TextView) com.ekuater.labelchat.ui.util.ViewHolder.get(view, R.id.confide_detaile_item_floor);
            TextView area = (TextView) com.ekuater.labelchat.ui.util.ViewHolder.get(view, R.id.confide_detaile_item_area);
            TextView time = (TextView) com.ekuater.labelchat.ui.util.ViewHolder.get(view, R.id.confide_detaile_item_time);
            tx.setOnClickListener(this);
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

            tx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (comment.getStranger() != null) {
                        listener.onConfideCommentTxClick(comment.getStranger().getUserId());
                    }
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (comment.getFloor() != null) {
                        listener.onConfideChildComment(String.format(context.getResources().getString(R.string.confide_floor), comment.getFloor()), boundPosition, childPosition);
                    }
                }
            });
            holder.story_comment_list.addView(view);
        }
    }
    private int getUserColorText(){
        return ((Confide) boundWrapper.getDynamic()).getConfideSex().equals("2") ? R.color.confide_female : R.color.confide_male;
    }

    private int getUserBackground(){
        return ((Confide) boundWrapper.getDynamic()).getConfideSex().equals("2") ? R.drawable.confide_female : R.drawable.confide_male;
    }

    private void isShowUserTx(CircleImageView tx, ConfideComment comment){
        if (isMyUserId(SettingHelper.getInstance(context).getAccountUserId())){
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
        return confideUserId.equals(((Confide) boundWrapper.getDynamic()).getConfideUserId()) ? true : false;
    }


    private int getUserTx(String sex) {
        return "2".equals(sex) ? R.drawable.confide_female : R.drawable.confide_male;
    }

    private String getDateTime(long time) {
        return DateTimeUtils.getDescriptionTimeFromTimestamp(context, time);
    }

    private Confide getBoundConfide() {
        return (Confide) boundWrapper.getDynamic();
    }

    private int getBoundPosition() {
        return boundPosition;
    }

    private static class ViewHolder {

        public View rootView;
        public RelativeLayout linearLayout;
        public CircleImageView tx;
        public ImageView praise;
        public ImageView letter;
        public ImageView more;
        public ImageView comment;
        public LinearLayout operationBar;
        public ClickEventInterceptLinear commentLinear;
        public LinearLayout praiseLinear;
        public TextView praiseNum;
        public TextView commentNum;
        public TextView letterNum;
        public TextView time;
        public TextView role;
        public TextView area;
        public TextView content;
        public LinearLayout story_comment_parent;
        public LinearLayout story_comment_list;
        public TextView story_comment_tag;
        public FlowLayout story_comment_praise_list;
        public RelativeLayout story_comment_praise_relative;
    }
}
