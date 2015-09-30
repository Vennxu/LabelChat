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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LabelStoryComments;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserPraise;
import com.ekuater.labelchat.datastruct.mixdynamic.DynamicWrapper;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.fragment.mixdynamic.audioplay.AudioBindEvent;
import com.ekuater.labelchat.ui.fragment.mixdynamic.audioplay.AudioEntity;
import com.ekuater.labelchat.ui.fragment.mixdynamic.audioplay.AudioNotifyEvent;
import com.ekuater.labelchat.ui.fragment.mixdynamic.audioplay.AudioPlayEvent;
import com.ekuater.labelchat.ui.fragment.mixdynamic.audioplay.AudioState;
import com.ekuater.labelchat.ui.fragment.voice.VoiceUtiles;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.ui.widget.ClickEventInterceptLinear;
import com.ekuater.labelchat.ui.widget.FlowLayout;
import com.ekuater.labelchat.ui.widget.emoji.ShowContentTextView;

import org.json.JSONObject;

import java.util.Locale;

import de.greenrobot.event.EventBus;

/**
 * Created by Leo on 2015/4/16.
 *
 * @author LinYong
 */
public class AudioDynamicRender implements DynamicRender, View.OnClickListener {

    private final Context context;
    private final StoryDynamicListener listener;
    private final ViewHolder holder;
    private final ContactsManager contactsManager;
    private final AvatarManager avatarManager;
    private final String myUserId;
    private final boolean isLoadComment;
    private final LayoutInflater inflater;
    private final Animation playAnim;
    private final EventBus eventBus;

    private DynamicWrapper boundWrapper;
    private int boundPosition;
    private AudioEntity audioEntity;

    public AudioDynamicRender(Context context, boolean isLoadComment, EventBus eventBus,
                              StoryDynamicListener listener) {
        this.context = context;
        this.listener = listener;
        this.isLoadComment = isLoadComment;
        this.eventBus = eventBus;
        this.holder = new ViewHolder();
        this.contactsManager = ContactsManager.getInstance(context);
        this.avatarManager = AvatarManager.getInstance(context);
        this.myUserId = SettingHelper.getInstance(context).getAccountUserId();
        this.inflater = LayoutInflater.from(context);
        this.playAnim = AnimationUtils.loadAnimation(context, R.anim.voice_player_anim);
        this.playAnim.setInterpolator(new LinearInterpolator());
        this.audioEntity = null;
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup parent) {
        View rootView = inflater.inflate(R.layout.audio_mix_dynamic_item, parent, false);
        holder.rootView = rootView;
        holder.story_tx = (ImageView) rootView.findViewById(R.id.descript_tx);
        holder.audio_play = (ImageView) rootView.findViewById(R.id.play_image);
        holder.audio_load = rootView.findViewById(R.id.loading);
        holder.audio_anim = (CircleImageView) rootView.findViewById(R.id.play_anim);
        holder.story_name = (TextView) rootView.findViewById(R.id.descript_name);
        holder.story_time = (TextView) rootView.findViewById(R.id.descript_time);
        holder.story_content = (TextView) rootView.findViewById(R.id.descript_content);
        holder.story_content.setAutoLinkMask(Linkify.ALL);
        holder.story_following = (TextView) rootView.findViewById(R.id.descript_following);
        holder.story_following_icon = (ImageView) rootView.findViewById(R.id.descript_following_icon);
        holder.story_following_layout = rootView.findViewById(R.id.descript_following_linear);
        holder.duration = (TextView) rootView.findViewById(R.id.duration);
        holder.story_operation_arrow = (LinearLayout) rootView.findViewById(R.id.operation_bar_comment_linear);
        holder.story_praise = (ImageView) rootView.findViewById(R.id.operation_bar_praise);
        holder.story_praise_num = (TextView) rootView.findViewById(R.id.operation_bar_praise_num);
        holder.story_comment_num = (TextView) rootView.findViewById(R.id.operation_bar_comment_num);
        holder.story_comment_operation = (ClickEventInterceptLinear) rootView.findViewById(R.id.operation_bar_comment_parent);
        holder.story_letter_num = (TextView) rootView.findViewById(R.id.operation_bar_letter_num);
        holder.story_letter = (ImageView) rootView.findViewById(R.id.operation_bar_letter);
        holder.story_more = (ImageView) rootView.findViewById(R.id.operation_bar_more);
        holder.story_comment_list = (LinearLayout) rootView.findViewById(R.id.comment_list);
        holder.story_comment_parent = (LinearLayout) rootView.findViewById(R.id.comment_parent);
        holder.story_comment_tag = (TextView) rootView.findViewById(R.id.comment_tag);
        holder.story_comment_praise_list = (FlowLayout) rootView.findViewById(R.id.comment_praise_list);
        holder.story_comment_praise_list.setVerticalGap(20);
        holder.story_comment_praise_list.setHorizontalGap(20);
        holder.story_comment_praise = (ImageView) rootView.findViewById(R.id.comment_praise);
        holder.story_singer_name = (TextView) rootView.findViewById(R.id.singer);
        holder.story_song_name = (TextView) rootView.findViewById(R.id.song);
        return rootView;
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
        holder.story_letter.setOnClickListener(this);
        holder.story_praise.setOnClickListener(this);
        holder.story_following_layout.setOnClickListener(this);
        holder.story_more.setOnClickListener(this);
        holder.story_comment_tag.setOnClickListener(this);
        holder.story_comment_operation.setOnClickListener(this);
        if (isLoadComment) {
            holder.audio_play.setOnClickListener(this);
        }
    }

    @Override
    public void bindView(DynamicWrapper dynamicWrapper, int position) {
        boundWrapper = dynamicWrapper;
        boundPosition = position;
        bindStory(getBoundStory());
        if (eventBus != null) {
            eventBus.register(this);
        }
    }

    @Override
    public void unbindView() {
        if (eventBus != null) {
            eventBus.unregister(this);
            updatePlayState(AudioState.STOPPED);
        }
    }

    @Override
    public void onClick(View v) {
        final LabelStory story = getBoundStory();
        final int position = getBoundPosition();

        switch (v.getId()) {
            case R.id.descript_tx:
                listener.onStoryAvatarClick(story, position);
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
            case R.id.comment_tag:
                listener.onStoryComment(boundPosition, story);
                break;
            case R.id.play_image:
                if (audioEntity != null && eventBus != null) {
                    eventBus.post(new AudioPlayEvent(audioEntity));
                }
                break;
            case R.id.operation_bar_comment_parent:
                listener.onStoryComment(boundPosition, story);
                break;
            default:
                break;
        }
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(AudioNotifyEvent event) {
        AudioEntity entity = event.getEntity();
        if (audioEntity != null && audioEntity.getId().equals(entity.getId())) {
            audioEntity = entity;
            switch (event.getType()) {
                case STATE_NOTIFY:
                    updatePlayState();
                    break;
                case TIME_NOTIFY:
                    holder.duration.setText(context.getString(R.string.time, audioEntity.getTime()));
                    break;
                default:
                    break;
            }
        }
    }

    private void bindStory(LabelStory story) {
        Stranger author = story.getStranger();
        if (author != null) {
            String title = MiscUtils.getUserRemarkName(context, author.getUserId());
            MiscUtils.showAvatarThumb(avatarManager, author.getAvatarThumb(), holder.story_tx);
            holder.story_name.setText(title != null && title.length() > 0 ? title : author.getNickname());
        } else {
            holder.story_tx.setImageResource(R.drawable.contact_single);
            holder.story_name.setText("");
        }
        if (story.getImages() != null && story.getImages().length > 0) {
            avatarManager.displaySingerAvatar(story.getImages()[0], holder.audio_anim, R.drawable.ic_sound_pic_normal);
        }else{
            holder.audio_anim.setImageResource(R.drawable.sound_play_bg);
        }
        holder.story_time.setText(getTimeString(story.getCreateDate()).trim());
        bindFollowing(author, "Y".equals(story.getIsFollowing()));
        holder.duration.setText(context.getString(R.string.time, getDuration(story.getDuration())));
        holder.story_praise_num.setText(story.getPraise());
        holder.story_comment_num.setText(story.getCommentNum());
        holder.story_letter_num.setText(story.getLetterNum() + "");
        holder.story_praise_num.setVisibility("0".equals(story.getPraise()) ? View.INVISIBLE : View.VISIBLE);
        holder.story_comment_num.setVisibility("0".equals(story.getCommentNum()) ? View.INVISIBLE : View.VISIBLE);
        holder.story_letter_num.setVisibility(story.getLetterNum() == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.story_praise.setImageResource("Y".equals(story.getIsPraise())
                ? R.drawable.ic_praise_pressed : R.drawable.ic_praise_normal);
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
            holder.story_comment_praise.setImageResource("Y".equals(story.getIsPraise()) ? R.drawable.ic_praise_user_pressed : R.drawable.ic_praise_user_normal);
        } else {
            holder.story_operation_arrow.setBackgroundResource(0);
            holder.story_comment_parent.setVisibility(View.GONE);
        }

        audioEntity = new AudioEntity(story.getLabelStoryId(), story.getMedia(), story.getType());
        if (eventBus != null) {
            AudioBindEvent event = new AudioBindEvent(audioEntity.getId());
            eventBus.post(event);
            if (event.getEntity() != null) {
                audioEntity = event.getEntity();
            }
        }
        bingSong(story.getType(),story.getContent());
        updatePlayState();
    }

    private void bingSong(String type, String content){
        if(LabelStory.TYPE_ONLINEAUDIO.equals(type)){
            JSONObject jsonObject = VoiceUtiles.getContentJson(content);
            if (jsonObject != null) {
                holder.story_song_name.setText(jsonObject.optString(CommandFields.Dynamic.SONG_NAME) == null ?
                        context.getString(R.string.song_name, context.getString(R.string.music_unknown)) :
                        context.getString(R.string.song_name, jsonObject.optString(CommandFields.Dynamic.SONG_NAME)));
                holder.story_singer_name.setText(jsonObject.optString(CommandFields.Dynamic.SINGER_NAME) == null ?
                        context.getString(R.string.singer_name, context.getString(R.string.music_unknown)) :
                        context.getString(R.string.singer_name, jsonObject.optString(CommandFields.Dynamic.SINGER_NAME)));
                holder.story_content.setText(jsonObject.optString(CommandFields.Dynamic.DYNAMIC_CONTENT));
            }
        }else{
            holder.story_content.setText(content);
            holder.story_song_name.setText(context.getString(R.string.song_name, context.getString(R.string.music_unknown)));
            holder.story_singer_name.setText(context.getString(R.string.singer_name, context.getString(R.string.music_unknown)));
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

    private void loadPriseUser(UserPraise[] users) {
        for (final UserPraise userPraise : users) {
            CircleImageView view = (CircleImageView) inflater.inflate(R.layout.item_priase_image,
                    holder.story_comment_praise_list, false).findViewById(R.id.praise_user);
            if (userPraise != null) {
                MiscUtils.showAvatarThumb(avatarManager, userPraise.getmPraiseUserAvatarThumb(), view, R.drawable.contact_single);
            } else {
                view.setImageResource(R.drawable.contact_single);
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userPraise != null) {
                        listener.onStoryCommentTxClick(userPraise.getmPraiseUserId());
                    }
                }
            });
            holder.story_comment_praise_list.addView(view);
        }
    }

    private String getDuration(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        return String.format(Locale.ENGLISH, "%1$02d:%2$02d", minutes % 60, seconds % 60);
    }

    private void bindFollowing(Stranger author, boolean isFollowing) {
        String authorUserId = author != null ? author.getUserId() : null;
        boolean visible = true;

        if (TextUtils.isEmpty(authorUserId) || authorUserId.equals(myUserId)) {
            visible = false;
        } else if (isContact(authorUserId)) {
            holder.story_following.setTextColor(context.getResources().getColor(R.color.check_more));
            holder.story_following_icon.setImageResource(R.drawable.friends_icon);
            holder.story_following.setText(R.string.main_activity_tab_friends_description);
            holder.story_following_layout.setBackgroundResource(R.drawable.friends);
        } else if (isFollowing) {
            holder.story_following.setTextColor(context.getResources().getColor(R.color.followed));
            holder.story_following_icon.setImageResource(R.drawable.followed_icon);
            holder.story_following.setText(R.string.labelstory_attentioned);
            holder.story_following_layout.setBackgroundResource(R.drawable.followed);
        } else {
            holder.story_following.setTextColor(context.getResources().getColor(R.color.follow));
            holder.story_following_icon.setImageResource(R.drawable.follow_icon);
            holder.story_following.setText(R.string.labelstory_attention);
            holder.story_following_layout.setBackgroundResource(R.drawable.follow);
        }
        holder.story_following_layout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private String getTimeString(long time) {
        return DateTimeUtils.getDescriptionTimeFromTimestamp(context, time);
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

    private void updatePlayState() {
        updatePlayState(audioEntity.getState());
    }

    private void updatePlayState(AudioState state) {
        switch (state) {
            case LOADING:
                holder.audio_play.setEnabled(false);
                holder.audio_play.setBackgroundResource(R.drawable.record_stop);
                holder.audio_load.setVisibility(View.VISIBLE);
                break;
            case PLAYING:
                holder.audio_play.setEnabled(true);
                holder.audio_play.setBackgroundResource(R.drawable.record_stop);
                holder.audio_anim.startAnimation(playAnim);
                holder.audio_load.setVisibility(View.GONE);
                break;
            case STOPPED:
                holder.audio_play.setEnabled(true);
                holder.audio_play.setBackgroundResource(R.drawable.record_play);
                holder.audio_anim.clearAnimation();
                holder.audio_load.setVisibility(View.GONE);
                holder.duration.setText(context.getString(R.string.time, getDuration(getBoundStory().getDuration())));
                break;
            default:
                break;
        }
    }

    private static class ViewHolder {

        public View rootView;
        public ImageView story_tx;
        public TextView story_name;
        public TextView story_time;
        public TextView story_content;
        public TextView story_following;
        public ImageView story_following_icon;
        public View story_following_layout;
        public ImageView audio_play;
        public View audio_load;
        public CircleImageView audio_anim;
        public TextView duration;
        public ImageView story_praise;
        public TextView story_praise_num;
        public TextView story_comment_num;
        public ClickEventInterceptLinear story_comment_operation;
        public TextView story_letter_num;
        public ImageView story_letter;
        public ImageView story_more;
        public LinearLayout story_comment_parent;
        public LinearLayout story_comment_list;
        public TextView story_comment_tag;
        public FlowLayout story_comment_praise_list;
        public ImageView story_comment_praise;
        public LinearLayout story_operation_arrow;
        public TextView story_singer_name;
        public TextView story_song_name;
    }
}
