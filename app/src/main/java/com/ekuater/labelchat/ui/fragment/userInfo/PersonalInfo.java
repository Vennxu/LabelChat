package com.ekuater.labelchat.ui.fragment.userInfo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.AlbumPhoto;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.Interact;
import com.ekuater.labelchat.datastruct.InterestType;
import com.ekuater.labelchat.datastruct.InterestTypeProperty;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserInterest;
import com.ekuater.labelchat.datastruct.UserTag;
import com.ekuater.labelchat.delegate.AlbumManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.labelstory.DynamicArguments;
import com.ekuater.labelchat.ui.fragment.labelstory.LabelStoryUtils;
import com.ekuater.labelchat.ui.fragment.voice.VoiceUtiles;
import com.ekuater.labelchat.ui.util.CompatUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.ui.widget.FlowLayout;
import com.ekuater.labelchat.util.InterestUtils;

import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by Administrator on 2015/3/13.
 *
 * @author Xu wenxiang
 */
public class PersonalInfo {

    public static class AlbumItem implements UserInfoItem {

        private Context mContext;
        private int photoTotal = 0;
        private UserContact mUserContact;
        private AlbumPhoto[] mAlbumPhotos;
        private AlbumManager mAlbumManager;

        public AlbumItem(Context context, UserContact userContact) {
            mContext = context;
            if (userContact != null) {
                photoTotal = userContact.getMyPhotoTotal();
                mUserContact = userContact;
                mAlbumPhotos = userContact.getAlbumPhotos();
            }
            mAlbumManager = AlbumManager.getInstance(mContext);
        }

        @Override
        public View newView(LayoutInflater layoutInflater, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            View view = layoutInflater.inflate(R.layout.user_info_album, parent, false);
            holder.albumView = (FrameLayout) view.findViewById(R.id.album_view);
            holder.albumHint = (TextView) view.findViewById(R.id.album_hint);
            holder.albumDriver = view.findViewById(R.id.album_driver);
            holder.albumTitle = (TextView) view.findViewById(R.id.album_title);
            holder.albumLinear = (LinearLayout) view.findViewById(R.id.album_linear);
            holder.photoViews = new ImageView[3];
            holder.photoViews[0] = (AlbumImageView) view.findViewById(R.id.first_pic);
            holder.photoViews[1] = (AlbumImageView) view.findViewById(R.id.second_pic);
            holder.photoViews[2] = (AlbumImageView) view.findViewById(R.id.third_pic);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder.photoViews != null) {
                final int viewCount = holder.photoViews.length;
                final int photoCount = mAlbumPhotos != null ? mAlbumPhotos.length : 0;
                if (photoCount < 3) {
                    holder.albumTitle.setVisibility(View.GONE);
                } else {
                    holder.albumTitle.setVisibility(View.VISIBLE);
                    holder.albumTitle.setText(mContext.getString(R.string.album_title, photoTotal));
                }
                for (int i = 0; i < viewCount; ++i) {
                    if (i < photoCount) {
                        holder.photoViews[i].setVisibility(View.VISIBLE);
                        mAlbumManager.displayPhotoThumb(mAlbumPhotos[i].getPhotoThumb(),
                                holder.photoViews[i], R.drawable.pic_loading);
                    } else {
                        holder.photoViews[i].setBackgroundResource(R.drawable.null_pic);
                    }
                }
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAlbumPhotos != null) {
                        UILauncher.launchAlbumGalleryUI(mContext, new LiteStranger(mUserContact));
                    }
                }
            });
        }

        private static class ViewHolder {
            ImageView[] photoViews;
            FrameLayout albumView;
            TextView albumHint;
            View albumDriver;
            TextView albumTitle;
            LinearLayout albumLinear;
        }
    }

    public static class DynamicItem implements UserInfoItem {

        private Activity mActivity;
        private LabelStory[] mLabelStory = null;
        private UserContact mUserContact;
        private boolean lastItem;

        public DynamicItem(Activity activity, UserContact userContact, boolean lastItem) {
            mActivity = activity;
            if (userContact != null) {
                mUserContact = userContact;
                mLabelStory = userContact.getLabelStories();
            }
            this.lastItem = lastItem;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            View view = inflater.inflate(R.layout.user_info_dynamic, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.dynamicParent = (ViewGroup) view.findViewById(R.id.dynamic_parent);
            holder.dynamic_num = (TextView) view.findViewById(R.id.dynamic_title);
            holder.dynamicNull = (ImageView) view.findViewById(R.id.dynamic_null);
            holder.dynamic_more = (TextView) view.findViewById(R.id.dynamic_check_more);

            holder.contentParent = (ViewGroup) view.findViewById(R.id.dynamic_content_parent);
            holder.content = newDynamicContent();
            holder.contentView = holder.content.newView(inflater, holder.contentParent);
            holder.contentParent.addView(holder.contentView);
            holder.itemGap = view.findViewById(R.id.item_gap);
            view.setTag(holder);
            return view;
        }

        private DynamicContent newDynamicContent() {
            if (mLabelStory != null && mLabelStory[0] != null) {
                DynamicContent content;

                switch (mLabelStory[0].getType()) {
                    case LabelStory.TYPE_ONLINEAUDIO:
                    case LabelStory.TYPE_AUDIO:
                        content = new AudioContent(mActivity);
                        break;
                    case LabelStory.TYPE_BANKNOTE:
                        content = new BanknoteContent(mActivity);
                        break;
                    case LabelStory.TYPE_TXT_IMG:
                    default:
                        content = new TxtContent(mActivity);
                        break;
                }
                return content;
            } else {
                return new TxtContent(mActivity);
            }
        }

        @Override
        public void bindView(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (mLabelStory != null && mLabelStory[0] != null) {
                final LabelStory story = mLabelStory[0];
                holder.dynamicParent.setVisibility(View.VISIBLE);
                holder.dynamicNull.setVisibility(View.GONE);
                holder.dynamic_num.setText(mActivity.getResources().getString(
                        R.string.dynamic_title, story.getStoryTotal()));
                holder.dynamic_num.setVisibility(story.getStoryTotal() == 0
                        ? View.GONE : View.VISIBLE);
                if (story.getStoryTotal() > 1) {
                    holder.dynamic_more.setVisibility(View.VISIBLE);
                    holder.dynamic_more.setText(String.format(
                            mActivity.getResources().getString(R.string.check_more),
                            story.getStoryTotal()));
                } else {
                    holder.dynamic_more.setVisibility(View.GONE);
                }
                holder.dynamic_more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Stranger stranger = new Stranger(mUserContact.getUserId(),
                                mUserContact.getNickname(), mUserContact.getAvatarThumb(),
                                mUserContact.getAvatar(), mUserContact.getSex());
                        UILauncher.launchMyLabelStoryUI(mActivity,
                                mUserContact.getUserId(), stranger);
                    }
                });

                holder.content.bindView(holder.contentView, story);
                holder.contentView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Stranger stranger = new Stranger(mUserContact.getUserId(),
                                mUserContact.getNickname(), mUserContact.getAvatarThumb(),
                                mUserContact.getAvatar(), mUserContact.getSex());
                        story.setStranger(stranger);
                        DynamicArguments arguments = new DynamicArguments();
                        arguments.setLabelStory(story);
                        arguments.setIsShowFragment(true);
                        arguments.setIsShowTitle(true);
                        arguments.setTag(LabelStoryUtils.STRANGERINFO);
                        UILauncher.launchFragmentLabelStoryDetaileUI(mActivity, arguments);
                    }
                });
            } else {
                holder.dynamic_more.setVisibility(View.GONE);
                holder.dynamicParent.setVisibility(View.GONE);
                holder.dynamicNull.setVisibility(View.VISIBLE);
            }
            holder.itemGap.setVisibility(lastItem ? View.GONE : View.VISIBLE);
        }

        private static class ViewHolder {

            ViewGroup dynamicParent;
            TextView dynamic_more;
            ImageView dynamicNull;
            TextView dynamic_num;

            ViewGroup contentParent;
            View contentView;
            DynamicContent content;

            View itemGap;
        }
    }

    private interface DynamicContent {

        View newView(LayoutInflater inflater, ViewGroup parent);

        void bindView(View contentView, LabelStory story);
    }

    private static void clearHorizontalMargins(View view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp != null && lp instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLp = (ViewGroup.MarginLayoutParams) lp;
            marginLp.leftMargin = 0;
            marginLp.rightMargin = 0;
            view.setLayoutParams(marginLp);
        }
    }

    private static class TxtContent implements DynamicContent {

        private AvatarManager avatarManager;

        public TxtContent(Context context) {
            avatarManager = AvatarManager.getInstance(context);
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            View contentView = inflater.inflate(R.layout.txt_story_content, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.dynamicText = (TextView) contentView.findViewById(R.id.descript_content);
            holder.dynamicImage = (ImageView) contentView.findViewById(R.id.descript_image);
            contentView.setTag(holder);
            clearHorizontalMargins(contentView);
            return contentView;
        }

        @Override
        public void bindView(View contentView, LabelStory story) {
            ViewHolder holder = (ViewHolder) contentView.getTag();

            String content = story.getContent();
            if (!TextUtils.isEmpty(content)) {
                holder.dynamicText.setVisibility(View.VISIBLE);
                holder.dynamicText.setText(content);
            } else {
                holder.dynamicText.setVisibility(View.GONE);
            }

            String imageThumb = getImageThumb(story);
            if (!TextUtils.isEmpty(imageThumb)) {
                holder.dynamicImage.setVisibility(View.VISIBLE);
                holder.dynamicImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                MiscUtils.showLabelStoryImageThumb(avatarManager,
                        imageThumb, holder.dynamicImage,
                        R.drawable.label_null_others);
            } else {
                holder.dynamicImage.setVisibility(View.GONE);
            }
        }

        private String getImageThumb(LabelStory story) {
            String[] thumbs = story.getThumbImages();

            if (thumbs != null && thumbs.length > 0) {
                return thumbs[0];
            } else {
                return null;
            }
        }

        private static class ViewHolder {

            TextView dynamicText;
            ImageView dynamicImage;
        }
    }

    private static class AudioContent implements DynamicContent {

        private Context context;

        public AudioContent(Context context) {
            this.context = context;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            View contentView = inflater.inflate(R.layout.audio_story_content, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.titleText = (TextView) contentView.findViewById(R.id.descript_content);
            holder.durationText = (TextView) contentView.findViewById(R.id.duration);
            holder.loadView =  contentView.findViewById(R.id.loading);
            holder.singName = (TextView) contentView.findViewById(R.id.singer);
            holder.songName = (TextView) contentView.findViewById(R.id.song);
            holder.mAnimView = (CircleImageView) contentView.findViewById(R.id.play_anim);
            contentView.setTag(holder);
            clearHorizontalMargins(contentView);
            return contentView;
        }

        @Override
        public void bindView(View contentView, LabelStory story) {
            ViewHolder holder = (ViewHolder) contentView.getTag();
            holder.loadView.setVisibility(View.GONE);
            holder.durationText.setText(getDuration(story.getDuration()));
            bingSong(holder, story);
        }

        private String getDuration(long duration) {
            long seconds = duration / 1000;
            long minutes = seconds / 60;
            return String.format(Locale.ENGLISH, "%1$02d:%2$02d", minutes % 60, seconds % 60);
        }

        private void bingSong(ViewHolder holder, LabelStory story){
            if(LabelStory.TYPE_ONLINEAUDIO.equals(story.getType())){
                if(story.getThumbImages() != null) {
                    AvatarManager.getInstance(context).displaySingerAvatar(story.getThumbImages()[0], holder.mAnimView, R.drawable.ic_sound_pic_normal);
                }
                JSONObject jsonObject = VoiceUtiles.getContentJson(story.getContent());
                if (jsonObject != null) {
                    holder.songName.setText(jsonObject.optString(CommandFields.Dynamic.SONG_NAME) == null ?
                            context.getString(R.string.song_name, context.getString(R.string.music_unknown)) :
                            context.getString(R.string.song_name, jsonObject.optString(CommandFields.Dynamic.SONG_NAME)));
                    holder.singName.setText(jsonObject.optString(CommandFields.Dynamic.SINGER_NAME) == null ?
                            context.getString(R.string.singer_name, context.getString(R.string.music_unknown)) :
                            context.getString(R.string.singer_name, jsonObject.optString(CommandFields.Dynamic.SINGER_NAME)));
                    holder.titleText.setText(jsonObject.optString(CommandFields.Dynamic.DYNAMIC_CONTENT));
                }
            }else{
                holder.titleText.setText(story.getContent());
                holder.mAnimView.setImageResource(R.drawable.ic_sound_play);
                holder.songName.setText(context.getString(R.string.song_name, context.getString(R.string.music_unknown)));
                holder.singName.setText(context.getString(R.string.singer_name, context.getString(R.string.music_unknown)));
            }
        }

        private static class ViewHolder {

            TextView titleText;
            TextView durationText;
            View loadView;
            TextView singName;
            TextView songName;
            CircleImageView mAnimView;
        }
    }

    private static class BanknoteContent implements DynamicContent {

        private AvatarManager avatarManager;

        public BanknoteContent(Context context) {
            avatarManager = AvatarManager.getInstance(context);
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            View contentView = inflater.inflate(R.layout.banknote_story_content, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.banknoteText = (TextView) contentView.findViewById(R.id.banknote_content);
            holder.banknoteImage = (ImageView) contentView.findViewById(R.id.banknote_image);
            contentView.setTag(holder);
            clearHorizontalMargins(contentView);
            return contentView;
        }

        @Override
        public void bindView(View contentView, LabelStory story) {
            ViewHolder holder = (ViewHolder) contentView.getTag();
            String content = story.getContent();
            if (TextUtils.isEmpty(content)) {
                holder.banknoteText.setVisibility(View.GONE);
            } else {
                holder.banknoteText.setVisibility(View.VISIBLE);
                holder.banknoteText.setText(content);
            }

            String[] images = story.getThumbImages();
            if (images != null && images.length > 0) {
                String image = images[0];
                holder.banknoteImage.setVisibility(View.VISIBLE);
                MiscUtils.showLabelStoryImage(avatarManager, image,
                        holder.banknoteImage, R.drawable.pic_loading);
            } else {
                holder.banknoteImage.setVisibility(View.GONE);
            }
        }

        private static class ViewHolder {

            private TextView banknoteText;
            private ImageView banknoteImage;
        }
    }

    public interface InteractListener {
        void onClick(View view, Interact interact);
    }

    public static class FavoriteInterestItem implements UserInfoItem {

        private Context mContext;
        private InterestType[] mInterestTypes;
        private InteractListener interactListener;

        public FavoriteInterestItem(Context context, UserContact userContact,
                                    InteractListener interactListener) {
            mContext = context;
            if (userContact != null) {
                mInterestTypes = userContact.getInterestTypes();
            }
            this.interactListener = interactListener;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            LinearLayout rootView = new LinearLayout(mContext);
            rootView.setOrientation(LinearLayout.VERTICAL);
            holder.interest = rootView;
            rootView.setTag(holder);
            return rootView;
        }

        @Override
        public void bindView(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();

            holder.interest.removeAllViews();
            if (mInterestTypes == null || mInterestTypes.length <= 0) {
                return;
            }

            LayoutInflater inflater = LayoutInflater.from(mContext);

            for (InterestType type : mInterestTypes) {
                UserInterest[] interests = (type != null) ? type.getUserInterests() : null;
                if (interests == null || interests.length <= 0) {
                    continue;
                }
                addInterestTypeView(type, inflater, holder.interest);
            }
            inflater.inflate(R.layout.interest_top_gap, holder.interest);
        }

        private void addInterestTypeView(InterestType interestType, LayoutInflater inflater,
                                         ViewGroup parent) {
            UserInterest[] interests = interestType != null
                    ? interestType.getUserInterests() : null;
            if (interests == null || interests.length <= 0) {
                return;
            }

            InterestTypeProperty property = InterestUtils.getTypeProperty(
                    interestType.getTypeId());
            View rootView = inflater.inflate(R.layout.user_info_favourite_interest, parent, false);
            ImageView typeIconView = (ImageView) rootView.findViewById(R.id.interest_icon);
            ViewGroup interestParent = (ViewGroup) rootView.findViewById(R.id.interest_content);

            typeIconView.setImageResource(property.getTypeIconResId());
            for (final UserInterest interest : interests) {
                if (interest == null) {
                    continue;
                }

                TextView interestView = (TextView) inflater.inflate(R.layout.interest_name,
                        interestParent, false);
                InterestUtils.setInterestColor(mContext, interestView, property);
                interestView.setText(interest.getInterestName());
                interestView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (interactListener != null) {
                            interactListener.onClick(v, new Interact(interest));
                        }
                    }
                });
                interestParent.addView(interestView);
            }
            parent.addView(rootView);
        }

        private static class ViewHolder {

            ViewGroup interest;
        }
    }

    public static class PersonLabelItem implements UserInfoItem {

        private UserTag[] mUserTag;
        private Context mContext;
        private InteractListener interactListener;

        public PersonLabelItem(Context context, UserContact userContact, InteractListener interactListener) {
            mContext = context;
            if (userContact != null) {
                mUserTag = userContact.getUserTags();
            }
            this.interactListener = interactListener;
        }

        @Override
        public View newView(LayoutInflater layoutInflater, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            holder.tag = new LinearLayout(mContext);
            holder.tag.setOrientation(LinearLayout.VERTICAL);
            holder.tagView = (LinearLayout) layoutInflater.inflate(R.layout.user_info_person_label, parent, false);
            holder.label = (FlowLayout) holder.tagView.findViewById(R.id.labels);
            holder.tag.setTag(holder);
            return holder.tag;
        }

        @Override
        public void bindView(View view) {
            LayoutInflater mInflater = LayoutInflater.from(mContext);
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.label.removeAllViews();
            holder.tag.removeAllViews();
            if (mUserTag != null && mUserTag.length > 0) {
                for (final UserTag userTag : mUserTag) {
                    holder.tagName = (TextView) mInflater.inflate(R.layout.label_name, holder.label, false);
                    holder.tagName.setText(userTag.getTagName());
                    GradientDrawable drawable = (GradientDrawable) mContext.getResources()
                            .getDrawable(R.drawable.corners_bg);
                    if (drawable != null) {
                        drawable.setColor(userTag.parseTagColor());
                        CompatUtils.setBackground(holder.tagName, drawable);
                    } else {
                        holder.tagName.setBackgroundColor(userTag.parseTagColor());
                    }
                    holder.tagName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (interactListener != null) {
                                interactListener.onClick(v, new Interact(userTag));
                            }
                        }
                    });
                    holder.label.addView(holder.tagName);
                }
                holder.tag.addView(holder.tagView);
            }
        }

        private static class ViewHolder {

            LinearLayout tag;
            LinearLayout tagView;
            FlowLayout label;
            TextView tagName;
        }
    }

    public static class BasicInfoItem implements UserInfoItem {

        private Context context;
        private UserContact contact;

        public BasicInfoItem(Context context, UserContact contact) {
            this.context = context;
            this.contact = contact;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            View rootView = inflater.inflate(R.layout.user_info_basic_info, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.infoContainer = (ViewGroup) rootView.findViewById(R.id.info_container);
            rootView.setTag(holder);
            return rootView;
        }

        @Override
        public void bindView(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            LayoutInflater inflater = LayoutInflater.from(context);
            Resources res = context.getResources();

            holder.infoContainer.removeAllViews();
            addNewInfoItem(contact.getNickname(),
                    ConstantCode.getSexImageResource(contact.getSex()),
                    inflater, holder.infoContainer);
            addNewInfoItem(context.getString(R.string.label_code) + contact.getLabelCode(),
                    inflater, holder.infoContainer);
            if (contact.getAge() > 0) {
                addNewInfoItem(MiscUtils.getAgeString(res, contact.getAge()),
                        inflater, holder.infoContainer);
            }
            if (contact.getHeight() > 0) {
                addNewInfoItem(MiscUtils.getHeightString(res, contact.getHeight()),
                        inflater, holder.infoContainer);
            }
            if (contact.getConstellation() > 0) {
                addNewInfoItem(MiscUtils.getConstellationString(res, contact.getConstellation()),
                        inflater, holder.infoContainer);
            }
            if (!TextUtils.isEmpty(contact.getSchool())) {
                addNewInfoItem(contact.getSchool(), inflater, holder.infoContainer);
            }
            if (!TextUtils.isEmpty(contact.getJob())) {
                addNewInfoItem(contact.getJob(), inflater, holder.infoContainer);
            }
        }

        private void addNewInfoItem(String content,
                                    int icon,
                                    LayoutInflater inflater,
                                    ViewGroup parent) {
            TextView textView = (TextView) inflater.inflate(R.layout.basic_info_item,
                    parent, false);
            textView.setText(content);
            if (icon > 0) {
                Drawable drawable = context.getResources().getDrawable(icon);
                textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            }
            parent.addView(textView);
        }

        private void addNewInfoItem(String content,
                                    LayoutInflater inflater,
                                    ViewGroup parent) {
            addNewInfoItem(content, 0, inflater, parent);
        }

        private static class ViewHolder {
            ViewGroup infoContainer;
        }
    }
}
