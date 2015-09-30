package com.ekuater.labelchat.ui.fragment.labelstory;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.ConfideMessage;
import com.ekuater.labelchat.datastruct.Dynamic.DynamicPublicEvent;
import com.ekuater.labelchat.datastruct.DynamicOperateMessage;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ConfideManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.UIEventBusHub;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.fragment.friends.StrangerHelper;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.ui.widget.emoji.EmojiTextView;
import com.ekuater.labelchat.util.ColorUtils;
import com.ekuater.labelchat.util.TextUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;


/**
 * Created by Administrator on 2015/4/24.
 *
 * @author FanChong
 */
public class NewMessageFragment extends Fragment {
    private Context mContext;
    private PushMessageManager mPushMessageManager;
    private DynamicMessageHintAdapter adapter;
    private ListView mListView;
    private EventBus mUIEventBus;
    private SimpleProgressHelper simpleProgressHelper;
    private StrangerHelper strangerHelper;

    private PushMessageManager.AbsListener mPushMessageManagerListener = new PushMessageManager.AbsListener() {
        @Override
        public void onNewSystemPushReceived(SystemPush systemPush) {
            super.onNewSystemPushReceived(systemPush);
            startLoad();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().hide();
        simpleProgressHelper = new SimpleProgressHelper(getActivity());
        mContext = getActivity();
        strangerHelper = new StrangerHelper(getActivity());
        adapter = new DynamicMessageHintAdapter(mContext, strangerHelper);
        mPushMessageManager = PushMessageManager.getInstance(mContext);
        mPushMessageManager.registerListener(mPushMessageManagerListener);
        mUIEventBus = UIEventBusHub.getDefaultEventBus();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dynamic_message_hint_list, container,
                false);
        view.findViewById(R.id.icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
                if (adapter.getMessageList() != null && adapter.getMessageList().size() > 0) {
                    List<NewMessageHint> list = adapter.getMessageList();
                    List<NewMessageHint> processMessageList = getProcessMessage(list);
                    if (processMessageList != null && processMessageList.size() > 0) {
                        for (NewMessageHint messageHint : processMessageList) {
                            mPushMessageManager.deletePushMessage(messageHint.getId());
                        }
                    }

                }
            }
        });
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(R.string.relevance_hint);
        TextView rightTitle = (TextView) view.findViewById(R.id.right_title);
        rightTitle.setTextColor(getResources().getColor(R.color.white));
        rightTitle.setText(R.string.clean);
        rightTitle.setVisibility(View.VISIBLE);
        rightTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getCount() > 0) {
                    showConfirmDialog();

                }
            }
        });
        mListView = (ListView) view.findViewById(R.id.dynamic_message_hint_list);
        registerForContextMenu(mListView);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(onItemClickListener);
        startLoad();
        return view;
    }

    private List<NewMessageHint> getProcessMessage(List<NewMessageHint> list) {
        List<NewMessageHint> messageHintList = new ArrayList<>();
        for (NewMessageHint messageHint : list) {
            if (messageHint.getState() == SystemPush.STATE_PROCESSED) {
                messageHintList.add(messageHint);
            }
        }
        return messageHintList;
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            NewMessageHint newMessageHint;
            List<NewMessageHint> messageHintList = adapter.getMessageList();
            Object object = parent.getItemAtPosition(position);
            if (object instanceof NewMessageHint) {
                newMessageHint = (NewMessageHint) object;
                switch (newMessageHint.getType()) {
                    case SystemPushType.TYPE_LABEL_STORY_COMMENTS:
                        DynamicOperateMessage dynamicMessage = newMessageHint.getDynamicMessage();
                        String dynamicId = dynamicMessage.getDynamicId();
                        LabelStory labelStory = new LabelStory();
                        labelStory.setLabelStoryId(dynamicId);
                        DynamicArguments arguments = new DynamicArguments();

                        arguments.setLabelStory(labelStory);
                        arguments.setIsShowTitle(true);
                        arguments.setIsShowFragment(true);
                        switch (dynamicMessage.getOperateType()) {
                            case DynamicOperateMessage.TYPE_OPERATE_PRAISE:
                                arguments.setIsPraise(true);
                                break;
                            case DynamicOperateMessage.TYPE_OPERATE_COMMENT:
                                arguments.setIsComment(true);
                                break;
                        }
                        DynamicDetailsHelper dynamicDetailsHelper = new DynamicDetailsHelper(getActivity(), simpleProgressHelper, arguments);
                        dynamicDetailsHelper.loadDynamicDetails();
                        for (NewMessageHint messageHint : messageHintList) {
                            if (messageHint.getType() == SystemPushType.TYPE_LABEL_STORY_COMMENTS) {
                                if (messageHint.getDynamicMessage().getDynamicId().equals(dynamicId))
                                    if (messageHint.getDynamicMessage().getDynamicId().equals(dynamicId)) {
                                        mPushMessageManager.updatePushMessageProcessed(messageHint.getId());
                                        messageHint.setState(SystemPush.STATE_PROCESSED);

                                    }
                            }
                        }
                        break;
                    case SystemPushType.TYPE_CONFIDE_COMMEND:
                        Confide confide = newMessageHint.getConfideMessages().getConfide();
                        UILauncher.launchConfideDetaileUI(NewMessageFragment.this, confide, 0, 0);
                        for (NewMessageHint messageHint : messageHintList) {
                            if (messageHint.getType() == SystemPushType.TYPE_CONFIDE_COMMEND) {
                                if (messageHint.getConfideMessages().getConfideId().equals(confide.getConfideId())) {
                                    mPushMessageManager.updatePushMessageProcessed(messageHint.getId());
                                    messageHint.setState(SystemPush.STATE_PROCESSED);
                                }
                            }
                        }
                        break;
                    default:
                        return;
                }
                startLoad();
                mUIEventBus.post(new DynamicPublicEvent());
            }
        }
    };

    private void showConfirmDialog() {
        ConfirmDialogFragment.UiConfig uiConfig = new ConfirmDialogFragment.UiConfig(getActivity().getString(R.string.clean_all_comment_message), null);
        ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(uiConfig, confirmListener);
        confirmDialogFragment.show(getFragmentManager(), "LabelStoryCommentMessageFragment");
    }

    private ConfirmDialogFragment.AbsConfirmListener confirmListener = new ConfirmDialogFragment.AbsConfirmListener() {
        @Override
        public void onConfirm() {
            for (NewMessageHint messageHint : adapter.getMessageList()) {
                mPushMessageManager.deletePushMessage(messageHint.getId());
            }
            startLoad();
            mUIEventBus.post(new DynamicPublicEvent());
        }

        @Override
        public void onCancel() {
            super.onCancel();
        }

    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.delete_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean handler = false;
        ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
        if (menuInfo instanceof AdapterView.AdapterContextMenuInfo) {
            AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
            handler = true;
            switch (item.getItemId()) {
                case R.id.delete:
                    mPushMessageManager.deletePushMessage(adapter.getItem(adapterContextMenuInfo.position).getId());
                    startLoad();
                    mUIEventBus.post(new DynamicPublicEvent());
                    break;
                default:
                    handler = false;
                    break;
            }
        }
        return handler || super.onContextItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPushMessageManager.unregisterListener(mPushMessageManagerListener);
        unregisterForContextMenu(mListView);
        if (adapter.getMessageList() != null && adapter.getMessageList().size() > 0) {
            List<NewMessageHint> list = adapter.getMessageList();
            List<NewMessageHint> processMessageList = getProcessMessage(list);
            if (processMessageList != null && processMessageList.size() > 0) {
                for (NewMessageHint messageHint : processMessageList) {
                    mPushMessageManager.deletePushMessage(messageHint.getId());
                }
            }

        }
    }

    private void startLoad() {
        new LoadTask().executeOnExecutor(LoadTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    private class LoadTask extends AsyncTask<Void, Void, List<NewMessageHint>> {

        @Override
        protected List<NewMessageHint> doInBackground(Void... params) {
            return mPushMessageManager.getHintMessage();

        }

        @Override
        protected void onPostExecute(List<NewMessageHint> newMessageHints) {
            super.onPostExecute(newMessageHints);
            sortUserLabels(newMessageHints);
            adapter.updateData(newMessageHints);
        }
    }

    private final Comparator<NewMessageHint> mComparator = new Comparator<NewMessageHint>() {
        @Override
        public int compare(NewMessageHint lhs, NewMessageHint rhs) {
            long diff = rhs.getTime() - lhs.getTime();
            diff = (diff != 0) ? diff : (rhs.getTime() - lhs.getTime());
            return (diff > 0) ? 1 : (diff < 0) ? -1 : 0;
        }
    };

    private void sortUserLabels(List<NewMessageHint> messageList) {
        Collections.sort(messageList, mComparator);
    }

    private class DynamicMessageHintAdapter extends BaseAdapter {
        private Context mContext;
        private List<NewMessageHint> mMessageList;
        private LayoutInflater mInflater;
        private AvatarManager mAvatarManager;
        private StrangerHelper mStrangerHelper;


        public DynamicMessageHintAdapter(Context context, StrangerHelper strangerHelper) {
            mContext = context;
            mStrangerHelper = strangerHelper;
            mInflater = LayoutInflater.from(mContext);
            mAvatarManager = AvatarManager.getInstance(mContext);

        }

        public synchronized void updateData(List<NewMessageHint> list) {
            mMessageList = list;
            notifyDataSetChanged();
        }

        public List<NewMessageHint> getMessageList() {
            return mMessageList;
        }

        private String getTimeString(long time) {
            return DateTimeUtils.getMessageDateString(mContext, time);
        }

        @Override
        public int getCount() {
            return mMessageList == null ? 0 : mMessageList.size();
        }

        @Override
        public NewMessageHint getItem(int position) {
            return mMessageList == null ? null : mMessageList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = newView(parent);
            }
            bindView(position, convertView);
            return convertView;
        }

        private View newView(ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            View view = mInflater.inflate(R.layout.fragment_dynamic_message_hint_item, parent, false);
            holder.avatarImage = (CircleImageView) view.findViewById(R.id.avatar_image);
            holder.nickname = (TextView) view.findViewById(R.id.nickname);
            holder.time = (TextView) view.findViewById(R.id.time);
            holder.hintView = (ImageView) view.findViewById(R.id.new_message_hint);
            holder.commentContent = (EmojiTextView) view.findViewById(R.id.comment_content);
            holder.praiseImage = (ImageView) view.findViewById(R.id.praise_pic);
            holder.commentImage = (ImageView) view.findViewById(R.id.comment_pic);
            holder.confideContent = (TextView) view.findViewById(R.id.confide_content);
            holder.showVoiceArea = (LinearLayout) view.findViewById(R.id.show_voice_area);
            holder.voiceName = (TextView) view.findViewById(R.id.voice_name);
            holder.praiseType = (TextView) view.findViewById(R.id.praise_type);
            view.setTag(holder);
            return view;
        }

        private void bindView(int position, View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            NewMessageHint message = getItem(position);
            if (message.getState() == SystemPush.STATE_PROCESSED) {
                holder.hintView.setVisibility(View.GONE);
            } else {
                holder.hintView.setVisibility(View.VISIBLE);
            }
            switch (message.getType()) {
                case SystemPushType.TYPE_LABEL_STORY_COMMENTS:
                    holder.confideContent.setVisibility(View.GONE);
                    DynamicOperateMessage dynamicMessage = message.getDynamicMessage();
                    final Stranger stranger = dynamicMessage.getStranger();
                    MiscUtils.showAvatarThumb(mAvatarManager, stranger.getAvatarThumb(), holder.avatarImage);
                    String title = MiscUtils.getUserRemarkName(mContext, stranger.getUserId());
                    if (title != null && title.length() > 0) {
                        holder.nickname.setText(title);
                    } else {
                        holder.nickname.setText(stranger.getNickname());
                    }
                    holder.time.setText(getTimeString(message.getTime()));
                    holder.avatarImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mStrangerHelper.showStranger(stranger.getUserId());
                        }
                    });
                    switch (dynamicMessage.getDynamicType()) {
                        case LabelStory.TYPE_BANKNOTE:
                        case LabelStory.TYPE_TXT_IMG:
                            holder.showVoiceArea.setVisibility(View.GONE);
                            holder.confideContent.setVisibility(View.GONE);
                            holder.commentImage.setVisibility(View.VISIBLE);
                            switch (dynamicMessage.getOperateType()) {
                                case DynamicOperateMessage.TYPE_OPERATE_PRAISE:
                                    holder.praiseImage.setVisibility(View.VISIBLE);
                                    holder.praiseType.setVisibility(View.VISIBLE);
                                    holder.praiseType.setText(getString(R.string.image_praise_remind, dynamicMessage.getCreatorNickname()));
                                    holder.commentContent.setVisibility(View.GONE);
                                    break;
                                case DynamicOperateMessage.TYPE_OPERATE_COMMENT:
                                    holder.commentContent.setVisibility(View.VISIBLE);
                                    holder.praiseImage.setVisibility(View.GONE);
                                    holder.praiseType.setVisibility(View.GONE);
                                    if (TextUtils.isEmpty(dynamicMessage.getReplyDynamicCommentContent())) {
                                        holder.commentContent.setText(dynamicMessage.getDynamicCommentContent());
                                    } else {
                                        String commentContent = "@" + dynamicMessage.getReplyNickname() + getString(R.string.colon);
                                        String content = dynamicMessage.getReplyDynamicCommentContent() + commentContent + dynamicMessage.getDynamicCommentContent();
                                        int end = dynamicMessage.getReplyDynamicCommentContent().length() + commentContent.length();
                                        SpannableString ss = new SpannableString(content);
                                        ForegroundColorSpan redSpan = new ForegroundColorSpan(mContext.getResources().getColor(R.color.comment_name));
                                        ForegroundColorSpan whiteSpan = new ForegroundColorSpan(mContext.getResources().getColor(R.color.story_time));
                                        ss.setSpan(redSpan, dynamicMessage.getReplyDynamicCommentContent().length(), end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        ss.setSpan(whiteSpan, 0, dynamicMessage.getReplyDynamicCommentContent().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        holder.commentContent.setText(ss);
                                    }
                                    break;
                            }
                            if (!TextUtil.isEmpty(dynamicMessage.getDynamicImgThumb())) {
                                MiscUtils.showLabelStoryCommentAvatarThumb(mAvatarManager, dynamicMessage.
                                        getDynamicImgThumb().split(";")[0], holder.commentImage, R.drawable.pic_loading);
                            } else {
                                holder.commentImage.setBackgroundResource(R.drawable.pic_loading);
                            }
                            break;
                        case LabelStory.TYPE_AUDIO:
                            holder.commentImage.setVisibility(View.GONE);
                            holder.confideContent.setVisibility(View.GONE);
                            holder.showVoiceArea.setVisibility(View.VISIBLE);
                            switch (dynamicMessage.getOperateType()) {
                                case DynamicOperateMessage.TYPE_OPERATE_PRAISE:
                                    holder.praiseImage.setVisibility(View.VISIBLE);
                                    holder.praiseType.setVisibility(View.VISIBLE);
                                    holder.praiseType.setText(getString(R.string.voice_praise_remind, dynamicMessage.getCreatorNickname()));
                                    holder.commentContent.setVisibility(View.GONE);
                                    break;
                                case DynamicOperateMessage.TYPE_OPERATE_COMMENT:
                                    holder.commentContent.setVisibility(View.VISIBLE);
                                    holder.praiseImage.setVisibility(View.GONE);
                                    holder.praiseType.setVisibility(View.GONE);
                                    if (TextUtils.isEmpty(dynamicMessage.getReplyDynamicCommentContent())) {
                                        holder.commentContent.setText(dynamicMessage.getDynamicCommentContent());
                                    } else {
                                        String commentContent = "@" + dynamicMessage.getReplyNickname() + getString(R.string.colon);
                                        String content = dynamicMessage.getReplyDynamicCommentContent() + commentContent + dynamicMessage.getDynamicCommentContent();
                                        int end = dynamicMessage.getReplyDynamicCommentContent().length() + commentContent.length();
                                        SpannableString ss = new SpannableString(content);
                                        ForegroundColorSpan redSpan = new ForegroundColorSpan(mContext.getResources().getColor(R.color.comment_name));
                                        ForegroundColorSpan whiteSpan = new ForegroundColorSpan(mContext.getResources().getColor(R.color.story_time));
                                        ss.setSpan(redSpan, dynamicMessage.getReplyDynamicCommentContent().length(), end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        ss.setSpan(whiteSpan, 0, dynamicMessage.getReplyDynamicCommentContent().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        holder.commentContent.setText(ss);
                                    }
                                    break;
                            }
                            holder.voiceName.setText(dynamicMessage.getDynamicContent());
                            break;
                        default:
                            return;
                    }
                    break;
                case SystemPushType.TYPE_CONFIDE_COMMEND:
                    holder.showVoiceArea.setVisibility(View.GONE);
                    holder.commentImage.setVisibility(View.GONE);
                    ConfideMessage confideMessage = message.getConfideMessages();
                    Confide confide = confideMessage.getConfide();
                    mAvatarManager.displayConfideAvatar(confideMessage.getVirtualAvatar(), holder.avatarImage, R.drawable.contact_single);
                    holder.nickname.setText(getString(R.string.confide_floor, confideMessage.getFloor()));
                    holder.time.setText(getTimeString(confideMessage.getTime()));
                    switch (confideMessage.getOperateType()) {
                        case DynamicOperateMessage.TYPE_OPERATE_PRAISE:
                        case DynamicOperateMessage.TYPE_OPERATE_COMMENT:
                            holder.showVoiceArea.setVisibility(View.GONE);
                            holder.commentImage.setVisibility(View.GONE);
                            holder.praiseImage.setVisibility(View.GONE);
                            holder.praiseType.setVisibility(View.GONE);
                            holder.confideContent.setVisibility(View.VISIBLE);
                            holder.commentContent.setVisibility(View.VISIBLE);
                            if (TextUtils.isEmpty(confideMessage.getReplyCommentContent())) {
                                holder.commentContent.setText(confideMessage.getCommentContent());
                            } else {
                                String commentContent = "@" + confideMessage.getReplyFloor() + getString(R.string.labelstory_item_floor) + getString(R.string.colon);
                                String content = commentContent + confideMessage.getReplyCommentContent();
                                int end = confideMessage.getReplyCommentContent().length() + commentContent.length();
                                SpannableString ss = new SpannableString(content);

                                ForegroundColorSpan whiteSpan = new ForegroundColorSpan(mContext.getResources().getColor(R.color.story_time));
                                ss.setSpan(whiteSpan, commentContent.length(), end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                ss.setSpan(new AbsoluteSizeSpan(sp2px(mContext, 14)), commentContent.length(), end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                holder.commentContent.setText(ss);
                            }
                            if (TextUtil.isEmpty(confide.getConfideBgImg())) {
                                holder.confideContent.setBackgroundColor(confide.parseBgColor());
                            }else{
                                holder.confideContent.setBackgroundResource(ConfideManager.getInstance(mContext).getConfideBs().get(confide.getConfideBgImg()));
                            }
                            holder.confideContent.setText(confide.getConfideContent());
                            break;
                    }
                    break;
            }
        }

        public int sp2px(Context context, float spValue) {
            final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
            return (int) (spValue * fontScale + 0.5f);
        }

        class ViewHolder {
            CircleImageView avatarImage;
            TextView nickname, time, confideContent, voiceName, praiseType;
            EmojiTextView commentContent;
            ImageView praiseImage, commentImage, hintView;
            LinearLayout showVoiceArea;

        }
    }
}
