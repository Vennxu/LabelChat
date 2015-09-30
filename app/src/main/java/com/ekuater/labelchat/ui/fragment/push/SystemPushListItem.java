package com.ekuater.labelchat.ui.fragment.push;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;

import org.w3c.dom.Text;

/**
 * Created by Administrator on 2015/4/30.
 */
public class SystemPushListItem {

    public static final int VIEW_TYPE_COMMENT = 0;
    public static final int VIEW_TYPE_PRAISE = 1;
    public static final int VIWE_TYPE_REMAIND = 2;
    public static final int VIEW_TYPE_COUNT = 3;

    public static int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    public interface PushItem {
        public View newView(LayoutInflater inflater, ViewGroup parent);

        public void bindView(View view);

        public int getShowViewType();

        public void onClick();

        public long getTime();

        public void delete();
    }

    public abstract static class CommentAbsPushItem implements PushItem {

        private long msgId;
        private int type;
        private int state;
        private long time;
        private Context context;

        public CommentAbsPushItem(Context context, SystemPush systemPush) {
            this.context = context;
            msgId = systemPush.getId();
            type = systemPush.getType();
            time = systemPush.getTime();
            state = systemPush.getState();
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.system_comment_abs_item, parent, false);
        }

        @Override
        public void bindView(View view) {
            RelativeLayout rootView = (RelativeLayout) view.findViewById(R.id.rootView);
            CircleImageView icon = (CircleImageView) view.findViewById(R.id.system_comment_tx);
            TextView title = (TextView) view.findViewById(R.id.system_comment_nickname);
            TextView subTitle = (TextView) view.findViewById(R.id.system_comment_content);
            TextView timeView = (TextView) view.findViewById(R.id.system_comment_time);
            LinearLayout contentLinear = (LinearLayout) view.findViewById(R.id.system_comment_additional);
            ImageView imageView = (ImageView) view.findViewById(R.id.system_comment_image);
            TextView content = (TextView) view.findViewById(R.id.system_comment_image_content);
            TextView titleContent = (TextView) view.findViewById(R.id.system_comment_voice_content);
            ImageView hint = (ImageView) view.findViewById(R.id.system_new_message_hint);
            hint.setVisibility(state == SystemPush.STATE_UNPROCESSED ? View.VISIBLE : View.GONE);
            rootView.setBackgroundColor(state == SystemPush.STATE_UNPROCESSED ? getColor(R.color.white) : getColor(R.color.backgroundColor));
            getTitle(title);
            timeView.setText(getTimeString(getTime()));
            getSubTitle(subTitle);
            setContent(content);
            setTitleContent(titleContent);
            setImage(imageView);
            setIcon(icon);
        }

        @Override
        public int getShowViewType() {
            return VIEW_TYPE_COMMENT;
        }

        @Override
        public long getTime() {
            return time;
        }

        @Override
        public void delete() {
            PushMessageManager.getInstance(context).deletePushMessage(msgId);
        }

        private String getTimeString(long time) {
            return DateTimeUtils.getMessageDateString(context, time);
        }

        protected abstract void getTitle(TextView textView);

        protected abstract void getSubTitle(TextView textView);

        protected abstract void setIcon(CircleImageView circleImageView);

        protected abstract void setImage(ImageView imageView);

        protected abstract void setContent(TextView textView);

        protected abstract void setTitleContent(TextView titleContent);

        protected Context getContext() {
            return context;
        }

        protected String getString(int resId) {
            return context.getString(resId);
        }

        protected int getColor(int resId) {
            return context.getResources().getColor(resId);
        }

        protected String getString(int resId, Object... formatArgs) {
            return context.getString(resId, formatArgs);
        }

        protected int getState() {
            return state;
        }

        protected void changetState() {
            if (getState() == SystemPush.STATE_UNPROCESSED) {
                PushMessageManager.getInstance(context).updatePushMessageProcessed(msgId);
            }
        }
    }


    public abstract static class PraiseAbsPushItem implements PushItem {

        private long msgId;
        private int type;
        private int state;
        private long time;
        private Context context;

        public PraiseAbsPushItem(Context context, SystemPush systemPush) {
            this.context = context;
            msgId = systemPush.getId();
            type = systemPush.getType();
            time = systemPush.getTime();
            state = systemPush.getState();
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.system_praise_abs_item, parent, false);
        }

        @Override
        public void bindView(View view) {
            RelativeLayout rootView = (RelativeLayout) view.findViewById(R.id.rootView);
            CircleImageView icon = (CircleImageView) view.findViewById(R.id.system_praise_tx);
            TextView title = (TextView) view.findViewById(R.id.system_praise_nickname);
            TextView subTitle = (TextView) view.findViewById(R.id.system_praise_content);
            TextView timeView = (TextView) view.findViewById(R.id.system_praise_time);
            LinearLayout contentLinear = (LinearLayout) view.findViewById(R.id.system_praise_additional);
            ImageView imageView = (ImageView) view.findViewById(R.id.system_praise_image);
            TextView content = (TextView) view.findViewById(R.id.system_praise_image_content);
            TextView titleContent = (TextView) view.findViewById(R.id.system_praise_voice_content);
            ImageView hint = (ImageView) view.findViewById(R.id.system_new_message_hint);
            hint.setVisibility(state == SystemPush.STATE_UNPROCESSED ? View.VISIBLE : View.GONE);
            rootView.setBackgroundColor(state == SystemPush.STATE_UNPROCESSED ? context.getResources().
                    getColor(R.color.white) : context.getResources().getColor(R.color.backgroundColor));
            title.setText(getTitle());
            subTitle.setText(getString(R.string.praise_remind, getSubTitle()));
            timeView.setText(getTimeString(getTime()));
            setContent(content);
            setTitleContent(titleContent);
            setImage(imageView);
            setIcon(icon);
        }

        @Override
        public int getShowViewType() {
            return VIEW_TYPE_PRAISE;
        }

        @Override
        public long getTime() {
            return time;
        }

        @Override
        public void delete() {
            PushMessageManager.getInstance(context).deletePushMessage(msgId);
        }

        private String getTimeString(long time) {
            return DateTimeUtils.getMessageDateString(context, time);
        }

        protected abstract String getTitle();

        protected abstract String getSubTitle();

        protected abstract void setIcon(CircleImageView circleImageView);

        protected abstract void setImage(ImageView imageView);

        protected abstract void setContent(TextView textView);

        protected abstract void setTitleContent(TextView titleContent);

        protected String getString(int resId) {
            return context.getString(resId);
        }

        protected String getString(int resId, Object... formatArgs) {
            return context.getString(resId, formatArgs);
        }

        protected void changetState() {
            if (state == SystemPush.STATE_UNPROCESSED) {
                PushMessageManager.getInstance(context).updatePushMessageProcessed(msgId);
            }
        }

        protected Context getContext() {
            return context;
        }
    }

    public abstract static class RemaindAbsPushItem implements PushItem {

        private long msgId;
        private int type;
        private long time;
        private int state;
        private Context context;

        public RemaindAbsPushItem(Context context, SystemPush systemPush) {
            this.context = context;
            msgId = systemPush.getId();
            type = systemPush.getType();
            time = systemPush.getTime();
            state = systemPush.getState();
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.system_remaind_abs_item, parent, false);
        }

        @Override
        public void bindView(View view) {
            RelativeLayout rootView = (RelativeLayout) view.findViewById(R.id.rootView);
            final CircleImageView icon = (CircleImageView) view.findViewById(R.id.system_remaind_tx);
            TextView title = (TextView) view.findViewById(R.id.system_remaind_nickname);
            TextView subTitle = (TextView) view.findViewById(R.id.system_remaind_content);
            TextView timeView = (TextView) view.findViewById(R.id.system_remaind_time);
            TextView rightTitle = (TextView) view.findViewById(R.id.system_remaind_interest_content);
            final ImageView image = (ImageView) view.findViewById(R.id.system_remaind_image);
            ImageView hint = (ImageView) view.findViewById(R.id.system_new_message_hint);
            TextView confideContent = (TextView) view.findViewById(R.id.confide_content);
            TextView voiceContent = (TextView) view.findViewById(R.id.system_remaind_voice_content);
            hint.setVisibility(state == SystemPush.STATE_UNPROCESSED ? View.VISIBLE : View.GONE);
            rootView.setBackgroundColor(state == SystemPush.STATE_UNPROCESSED ?
                    getColor(R.color.white) : getColor(R.color.backgroundColor));
            title.setText(getTitle());
            subTitle.setText(getSubTitle());
            timeView.setText(getTimeString(getTime()));
            setIcon(icon);
            setImage(image);
            setRightTitle(rightTitle);
            setConfideContent(confideContent);
            setVoiceContent(voiceContent);
        }

        @Override
        public int getShowViewType() {
            return VIWE_TYPE_REMAIND;
        }

        @Override
        public long getTime() {
            return time;
        }

        @Override
        public void delete() {
            PushMessageManager.getInstance(context).deletePushMessage(msgId);
        }

        private String getTimeString(long time) {
            return DateTimeUtils.getMessageDateString(context, time);
        }

        protected abstract String getTitle();

        protected abstract String getSubTitle();

        protected abstract void setIcon(CircleImageView circleImageView);

        protected abstract void setImage(ImageView image);

        protected abstract void setRightTitle(TextView rightTitle);

        protected abstract void setConfideContent(TextView textView);

        protected abstract void setVoiceContent(TextView textView);

        protected String getString(int resId) {
            return context.getString(resId);
        }

        protected String getString(int resId, Object... formatArgs) {
            return context.getString(resId, formatArgs);
        }

        protected int getColor(int resId) {
            return context.getResources().getColor(resId);
        }

        protected void changetState() {
            if (state == SystemPush.STATE_UNPROCESSED) {
                PushMessageManager.getInstance(context).updatePushMessageProcessed(msgId);
            }
        }

        protected Context getContext() {
            return context;
        }
    }


}
