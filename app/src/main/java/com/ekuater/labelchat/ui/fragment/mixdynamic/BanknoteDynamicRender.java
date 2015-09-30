package com.ekuater.labelchat.ui.fragment.mixdynamic;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LabelStoryComments;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserPraise;
import com.ekuater.labelchat.datastruct.mixdynamic.DynamicWrapper;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.fragment.labelstory.HorizontalListView;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.ui.widget.ClickEventIntercept;
import com.ekuater.labelchat.ui.widget.ClickEventInterceptLinear;
import com.ekuater.labelchat.ui.widget.FlowLayout;
import com.ekuater.labelchat.ui.widget.emoji.ShowContentTextView;

/**
 * Created by Leo on 2015/5/16.
 *
 * @author LinYong
 */
public class BanknoteDynamicRender implements DynamicRender, View.OnClickListener {

    private final Context context;
    private final StoryDynamicListener listener;
    private final ViewHolder holder;
    private final ContactsManager contactsManager;
    private final AvatarManager avatarManager;
    private boolean isLoadComment;

    private DynamicWrapper boundWrapper;
    private int boundPosition;
    private LayoutInflater inflater;

    public BanknoteDynamicRender(Context context, boolean isLoadComment, StoryDynamicListener listener) {
        this.context = context;
        this.listener = listener;
        this.isLoadComment = isLoadComment;
        this.holder = new ViewHolder();
        this.contactsManager = ContactsManager.getInstance(context);
        this.avatarManager = AvatarManager.getInstance(context);
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup parent) {
        this.inflater = inflater;
        View convertView = inflater.inflate(R.layout.banknote_mix_dynamic_item, parent, false);
        holder.rootView = convertView;
        holder.story_tx = (CircleImageView) convertView.findViewById(R.id.descript_tx);
        holder.story_name = (TextView) convertView.findViewById(R.id.descript_name);
        holder.story_content = (TextView) convertView.findViewById(R.id.banknote_content);
        holder.story_content.setAutoLinkMask(Linkify.ALL);
        holder.story_content_image = (ImageView) convertView.findViewById(R.id.banknote_image);
        holder.story_label = (LinearLayout) convertView.findViewById(R.id.story_item_label);
        holder.story_label_name = (TextView) convertView.findViewById(R.id.story_item_label_name);
        holder.story_time = (TextView) convertView.findViewById(R.id.descript_time);
        holder.story_following = (TextView) convertView.findViewById(R.id.descript_following);
        holder.story_following_icon = (ImageView) convertView.findViewById(R.id.descript_following_icon);
        holder.story_following_layout = (LinearLayout) convertView.findViewById(R.id.descript_following_linear);
        holder.story_praise = (ImageView) convertView.findViewById(R.id.operation_bar_praise);
        holder.story_more = (ImageView) convertView.findViewById(R.id.operation_bar_more);
        holder.story_praise_num = (TextView) convertView.findViewById(R.id.operation_bar_praise_num);
        holder.story_comment_num = (TextView) convertView.findViewById(R.id.operation_bar_comment_num);
        holder.story_comment_operation = (ClickEventInterceptLinear) convertView.findViewById(R.id.operation_bar_comment_parent);
        holder.story_letter_num = (TextView) convertView.findViewById(R.id.operation_bar_letter_num);
        holder.story_operation_arrow = (LinearLayout) convertView.findViewById(R.id.operation_bar_comment_linear);
        holder.delete = (ClickEventIntercept) convertView.findViewById(R.id.operation_bar_delete);
        holder.story_show_user = (LinearLayout) convertView.findViewById(R.id.operation_show_user);
        holder.story_user_click = (ClickEventIntercept) convertView.findViewById(R.id.operation_bar_user_area);
        holder.story_user_list = (HorizontalListView) convertView.findViewById(R.id.operation_bar_user);
        holder.story_user_number = (TextView) convertView.findViewById(R.id.operation_bar_user_area_read_number);
        holder.story_letter = (ImageView) convertView.findViewById(R.id.operation_bar_letter);
        holder.story_comment_list = (LinearLayout) convertView.findViewById(R.id.comment_list);
        holder.story_comment_parent = (LinearLayout) convertView.findViewById(R.id.comment_parent);
        holder.story_comment_tag = (TextView) convertView.findViewById(R.id.comment_tag);
        holder.story_comment_praise_list = (FlowLayout) convertView.findViewById(R.id.comment_praise_list);
        holder.story_comment_praise_list.setVerticalGap(20);
        holder.story_comment_praise_list.setHorizontalGap(20);
        holder.story_comment_praise = (ImageView) convertView.findViewById(R.id.comment_praise);
        return convertView;
    }

    @Override
    public void bindEvents() {
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLoadComment) {
                    listener.onStoryClick(getBoundStory(), getBoundPosition());
                }
            }
        });
        holder.rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return listener.onStoryLongClick(getBoundStory(), getBoundPosition());
            }
        });
        holder.story_tx.setOnClickListener(this);
        holder.delete.setOnClickListener(this);
        holder.story_user_click.setOnClickListener(this);
        holder.story_letter.setOnClickListener(this);
        holder.story_praise.setOnClickListener(this);
        holder.story_following_layout.setOnClickListener(this);
        holder.story_more.setOnClickListener(this);
        holder.story_label.setOnClickListener(this);
        holder.story_comment_tag.setOnClickListener(this);
        holder.story_comment_operation.setOnClickListener(this);
        if (isLoadComment) {
            holder.story_content_image.setOnClickListener(this);
        }
    }

    @Override
    public void bindView(DynamicWrapper dynamicWrapper, int position) {
        boundWrapper = dynamicWrapper;
        boundPosition = position;
        bindStory((LabelStory) dynamicWrapper.getDynamic());
    }

    @Override
    public void unbindView() {
    }

    @Override
    public void onClick(View v) {
        final LabelStory story = getBoundStory();
        final int position = getBoundPosition();

        switch (v.getId()) {
            case R.id.descript_tx:
                listener.onStoryAvatarClick(story, position);
                break;
            case R.id.operation_bar_delete:
                break;
            case R.id.operation_bar_user_area:
                break;
            case R.id.operation_bar_letter:
                listener.onStoryLetterClick(story, position);
                break;
            case R.id.operation_bar_praise:
                listener.onStoryPraiseClick(story, position);
                break;
            case R.id.descript_following_linear:
                listener.onStoryFollowingClick(story, position);
                break;
            case R.id.operation_bar_more:
                listener.onStoryMoreClick(story, position, v);
                break;
            case R.id.story_item_label:
                break;
            case R.id.comment_tag:
                listener.onStoryComment(boundPosition, story);
                break;
            case R.id.banknote_image:
                listener.onStoryImageClick(story.getImages()[0]);
                break;
            case R.id.operation_bar_comment_parent:
                listener.onStoryComment(boundPosition, story);
                break;
            default:
                break;
        }
    }

    private void bindStory(LabelStory story) {
        holder.story_praise_num.setText(story.getPraise());
        holder.story_comment_num.setText(story.getCommentNum());
        holder.story_letter_num.setText(story.getLetterNum() + "");
        holder.story_praise_num.setVisibility("0".equals(story.getPraise()) ? View.INVISIBLE : View.VISIBLE);
        holder.story_comment_num.setVisibility("0".equals(story.getCommentNum()) ? View.INVISIBLE : View.VISIBLE);
        holder.story_letter_num.setVisibility(story.getLetterNum() == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.story_time.setText(getTimeString(story.getCreateDate()).trim());
        if ("Y".equals(story.getIsPraise())) {
            holder.story_praise.setImageResource(R.drawable.ic_praise_pressed);
        } else {
            holder.story_praise.setImageResource(R.drawable.ic_praise_normal);
        }

        if (story.getThumbImages() != null && story.getThumbImages().length > 0) {
            holder.story_content_image.setVisibility(View.VISIBLE);
            MiscUtils.showLabelStoryImage(avatarManager,
                    story.getThumbImages()[0], holder.story_content_image,
                    R.drawable.pic_loading);
        } else {
            holder.story_content_image.setVisibility(View.GONE);
        }

        if (story.getCategory() != null) {
            holder.story_label_name.setText(story.getCategory().getmCategoryName());
            holder.story_label.setVisibility(View.VISIBLE);
        } else {
            holder.story_label.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(story.getContent())) {
            holder.story_content.setVisibility(View.GONE);
        } else {
            holder.story_content.setVisibility(View.VISIBLE);
            holder.story_content.setText(story.getContent());
        }
        isFollowing(story, holder.story_following, holder.story_name, holder.story_tx,
                holder.story_show_user, holder.story_following_icon, holder.story_following_layout);
        if (isLoadComment) {
            holder.story_operation_arrow.setBackgroundResource(R.drawable.message_arrow_bg);
            holder.story_comment_parent.setVisibility(View.VISIBLE);
            holder.story_comment_list.removeAllViews();
            holder.story_comment_praise_list.removeAllViews();
            if (story.getLabelStoryComments() != null && story.getLabelStoryComments().length > 0) {
                loadingComment(story.getLabelStoryComments());
            }
            if (story.getUserPraise() != null && story.getUserPraise().length > 0) {
                loadPriseUser(story.getUserPraise());
            }
            holder.story_comment_praise.setImageResource("Y".equals(story.getIsPraise())
                    ? R.drawable.ic_praise_user_pressed : R.drawable.ic_praise_user_normal);
        } else {
            holder.story_operation_arrow.setBackgroundResource(0);
            holder.story_comment_parent.setVisibility(View.GONE);
        }
    }

    private void loadingComment(LabelStoryComments[] commentses) {
        for (int i = 0; i < commentses.length; ++i) {
            final int childPosition = i;
            View view = inflater.inflate(R.layout.layout_comment_group, holder.story_comment_parent, false);
            final LabelStoryComments comment = commentses[childPosition];
            CircleImageView groupTx = (CircleImageView) view.findViewById(R.id.comment_tx);
            TextView groupName = (TextView) view.findViewById(R.id.comment_name);
            TextView groupTime = (TextView) view.findViewById(R.id.comment_time);
            ShowContentTextView groupContent = (ShowContentTextView) view.findViewById(R.id.comment_content);
            if (comment.getmStranger() != null) {
                Stranger stranger = comment.getmStranger();
                String title = MiscUtils.getUserRemarkName(context, stranger.getUserId());
                groupName.setText(title != null && title.length() > 0 ? title : stranger.getNickname());
                if (TextUtils.isEmpty(comment.getmReplyNickName())) {
                    groupContent.setText(comment.getmStoryComment());
                } else {
                    String content = context.getString(R.string.someone_reply) + comment.getmReplyNickName() + " " + comment.getmStoryComment();
                    SpannableString ss = new SpannableString(content);
                    ForegroundColorSpan redSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.story_content));
                    ss.setSpan(redSpan, 3, 3 + comment.getmReplyNickName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    groupContent.setText(ss);
                }
                MiscUtils.showAvatarThumb(avatarManager, comment.getmStranger().getAvatarThumb(), groupTx, R.drawable.contact_single);
            }
            groupTime.setText(getTimeString(comment.getmCreateDate()));
            groupTx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (comment.getmStranger() != null) {
                        listener.onStoryCommentTxClick(comment.getmStranger().getUserId());
                    }
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (comment.getmStranger() != null) {
                        Stranger stranger = comment.getmStranger();
                        String title = MiscUtils.getUserRemarkName(context, stranger.getUserId());
                        listener.onStoryChildComment(title != null && title.length() > 0 ? title : stranger.getNickname(), boundPosition, childPosition);
                    }
                }
            });
            holder.story_comment_list.addView(view);
        }
    }

    private void loadPriseUser(UserPraise[] userPraises) {
        for (final UserPraise praise : userPraises) {
            CircleImageView view = (CircleImageView) inflater.inflate(R.layout.item_priase_image,
                    holder.story_comment_praise_list, false).findViewById(R.id.praise_user);
            if (praise != null) {
                MiscUtils.showAvatarThumb(avatarManager, praise.getmPraiseUserAvatarThumb(),
                        view, R.drawable.contact_single);
            } else {
                view.setImageResource(R.drawable.contact_single);
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (praise != null) {
                        listener.onStoryCommentTxClick(praise.getmPraiseUserId());
                    }
                }
            });
            holder.story_comment_praise_list.addView(view);
        }
    }

    private String getTimeString(long time) {
        return DateTimeUtils.getDescriptionTimeFromTimestamp(context, time);
    }

    private void isFollowing(LabelStory story, TextView following, TextView name, ImageView tx,
                             View story_show_user, ImageView following_icon,
                             View following_icon_layout) {
        Stranger stranger = story.getStranger();
        String strangerUserId = stranger != null ? stranger.getUserId() : null;

        story_show_user.setVisibility(View.GONE);
        if (stranger != null) {
            String title = MiscUtils.getUserRemarkName(context, stranger.getUserId());
            MiscUtils.showAvatarThumb(avatarManager, stranger.getAvatarThumb(), tx);
            name.setText(title != null && title.length() > 0 ? title : stranger.getNickname());
        } else {
            tx.setImageResource(R.drawable.contact_single);
            name.setText("");
        }

        if (TextUtils.isEmpty(strangerUserId) || strangerUserId.equals(
                SettingHelper.getInstance(context).getAccountUserId())) {
            following_icon_layout.setVisibility(View.GONE);
        } else {
            if (isContact(strangerUserId)) {
                following.setTextColor(context.getResources().getColor(R.color.check_more));
                following_icon.setImageResource(R.drawable.friends_icon);
                following.setText(R.string.main_activity_tab_friends_description);
                following_icon_layout.setBackgroundResource(R.drawable.friends);
            } else {
                if ("Y".equals(story.getIsFollowing())) {
                    following.setTextColor(context.getResources().getColor(R.color.followed));
                    following_icon.setImageResource(R.drawable.followed_icon);
                    following.setText(R.string.labelstory_attentioned);
                    following_icon_layout.setBackgroundResource(R.drawable.followed);
                } else {
                    following.setTextColor(context.getResources().getColor(R.color.follow));
                    following_icon.setImageResource(R.drawable.follow_icon);
                    following.setText(R.string.labelstory_attention);
                    following_icon_layout.setBackgroundResource(R.drawable.follow);
                }
            }
            following_icon_layout.setVisibility(View.VISIBLE);
        }
    }

    private boolean isContact(String userId) {
        return contactsManager.getUserContactByUserId(userId) != null;
    }

    private LabelStory getBoundStory() {
        return (LabelStory) boundWrapper.getDynamic();
    }

    private int getBoundPosition() {
        return boundPosition;
    }

    private static class ViewHolder {

        public View rootView;
        public CircleImageView story_tx;
        public TextView story_name;
        public TextView story_content;
        public ImageView story_content_image;
        public LinearLayout story_label;
        public TextView story_label_name;
        public TextView story_time;
        public TextView story_following;
        public ImageView story_following_icon;
        public LinearLayout story_following_layout;
        public ImageView story_praise;
        public ImageView story_more;
        public TextView story_praise_num;
        public TextView story_comment_num;
        public ClickEventInterceptLinear story_comment_operation;
        public TextView story_letter_num;
        public ClickEventIntercept delete;
        public LinearLayout story_show_user;
        public ClickEventIntercept story_user_click;
        public HorizontalListView story_user_list;
        public TextView story_user_number;
        public ImageView story_letter;
        public LinearLayout story_comment_parent;
        public LinearLayout story_comment_list;
        public TextView story_comment_tag;
        public FlowLayout story_comment_praise_list;
        public ImageView story_comment_praise;
        public LinearLayout story_operation_arrow;
    }
}
