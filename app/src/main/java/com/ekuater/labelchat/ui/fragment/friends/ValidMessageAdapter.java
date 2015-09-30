package com.ekuater.labelchat.ui.fragment.friends;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.ValidateAddFriendMessage;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.util.MiscUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/3/31.
 *
 * @author LinYong
 */
public class ValidMessageAdapter extends BaseAdapter
        implements View.OnClickListener, Handler.Callback {

    private static final int MSG_VALIDATE_DONE = 101;

    public static class MessageItem {

        private long mMessageId;
        private int mState;
        private ValidateAddFriendMessage mMessage;

        public MessageItem(long msgId, int state, ValidateAddFriendMessage message) {
            mMessageId = msgId;
            mState = state;
            mMessage = message;
        }

        public long getMessageId() {
            return mMessageId;
        }

        public int getState() {
            return mState;
        }

        public void setState(int mState) {
            this.mState = mState;
        }

        public ValidateAddFriendMessage getMessage() {
            return mMessage;
        }
    }

    private final Context mContext;
    private final SimpleProgressHelper mProgressHelper;
    private final StrangerHelper mStrangerHelper;
    private final LayoutInflater mInflater;
    private final AvatarManager mAvatarManager;
    private final PushMessageManager mPushManager;
    private final ContactsManager mContactsManager;
    private final Handler mHandler;
    private List<MessageItem> mItemList = new ArrayList<>();

    public ValidMessageAdapter(Context context, SimpleProgressHelper progressHelper,
                               StrangerHelper strangerHelper) {
        super();
        mContext = context;
        mProgressHelper = progressHelper;
        mStrangerHelper = strangerHelper;
        mInflater = LayoutInflater.from(context);
        mAvatarManager = AvatarManager.getInstance(context);
        mPushManager = PushMessageManager.getInstance(context);
        mContactsManager = ContactsManager.getInstance(context);
        mHandler = new Handler(context.getMainLooper(), this);
    }

    public void updateMessageItems(List<MessageItem> list) {
        mItemList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItemList != null ? mItemList.size() : 0;
    }

    @Override
    public MessageItem getItem(int position) {
        return mItemList != null ? mItemList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getMessageId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView(parent);
        }
        bindView(convertView, position);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avatar_image:
                onAvatarClick(v);
                break;
            case R.id.btn_reject:
                onRejectClick(v);
                break;
            case R.id.btn_agree:
                onAgreeClick(v);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_VALIDATE_DONE:
                onValidateDone(msg.arg1, msg.arg2, (MessageItem) msg.obj);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private View newView(ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        View view = mInflater.inflate(R.layout.validate_add_friend_item, parent, false);
        view.setTag(holder);

        holder.titleView = (TextView) view.findViewById(R.id.title);
        holder.subTitleView = (TextView) view.findViewById(R.id.subtitle);
        holder.avatarImage = (ImageView) view.findViewById(R.id.avatar_image);
        holder.stateView = (TextView) view.findViewById(R.id.state);
        holder.btnArea = (ViewGroup) view.findViewById(R.id.btn_area);

        holder.avatarImage.setTag(holder);
        holder.avatarImage.setOnClickListener(this);

        Button rejectBtn = (Button) view.findViewById(R.id.btn_reject);
        Button agreeBtn = (Button) view.findViewById(R.id.btn_agree);
        rejectBtn.setTag(holder);
        rejectBtn.setOnClickListener(this);
        agreeBtn.setTag(holder);
        agreeBtn.setOnClickListener(this);

        return view;
    }

    private void bindView(View view, int position) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        final MessageItem item = getItem(position);
        final ValidateAddFriendMessage message = item.getMessage();
        final Stranger stranger = message.getStranger();
        final String validateMsg = message.getValidateMessage();

        holder.titleView.setText(stranger.getShowName());
        holder.subTitleView.setText(validateMsg);
        holder.subTitleView.setVisibility(TextUtils.isEmpty(validateMsg)
                ? View.GONE : View.VISIBLE);
        MiscUtils.showAvatarThumb(mAvatarManager, stranger.getAvatarThumb(),
                holder.avatarImage);
        holder.position = position;

        switch (item.getState()) {
            case ValidateAddFriendMessage.STATE_AGREED:
                holder.stateView.setText(R.string.already_agree);
                holder.stateView.setVisibility(View.VISIBLE);
                holder.btnArea.setVisibility(View.INVISIBLE);
                break;
            case ValidateAddFriendMessage.STATE_REJECTED:
                holder.stateView.setText(R.string.already_reject);
                holder.stateView.setVisibility(View.VISIBLE);
                holder.btnArea.setVisibility(View.INVISIBLE);
                break;
            default:
                holder.stateView.setVisibility(View.INVISIBLE);
                holder.btnArea.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void onAvatarClick(View v) {
        MessageItem item = getItem(((ViewHolder) v.getTag()).position);
        ValidateAddFriendMessage message = item.getMessage();
        Stranger stranger = message.getStranger();
        String userId = stranger.getUserId();

        if (mContactsManager.getUserContactByUserId(userId) != null) {
            UILauncher.launchFriendDetailUI(mContext, userId);
        } else {
            mStrangerHelper.showStranger(userId);
        }
    }

    private void onAgreeClick(View v) {
        final MessageItem item = getItem(((ViewHolder) v.getTag()).position);
        ValidateAddFriendMessage message = item.getMessage();
        Stranger stranger = message.getStranger();
        String userId = stranger.getUserId();
        String labelCode = stranger.getLabelCode();

        mContactsManager.acceptAddFriendInvitation(userId, labelCode, "",
                new FunctionCallListener() {
                    @Override
                    public void onCallResult(int result, int errorCode, String errorDesc) {
                        mHandler.obtainMessage(MSG_VALIDATE_DONE, errorCode,
                                ValidateAddFriendMessage.STATE_AGREED, item)
                                .sendToTarget();
                    }
                });
        mProgressHelper.show();
    }

    private void onRejectClick(View v) {
        final MessageItem item = getItem(((ViewHolder) v.getTag()).position);
        ValidateAddFriendMessage message = item.getMessage();
        Stranger stranger = message.getStranger();
        String userId = stranger.getUserId();
        String labelCode = stranger.getLabelCode();

        mContactsManager.rejectAddFriendInvitation(userId, labelCode, "",
                new FunctionCallListener() {
                    @Override
                    public void onCallResult(int result, int errorCode, String errorDesc) {
                        mHandler.obtainMessage(MSG_VALIDATE_DONE, errorCode,
                                ValidateAddFriendMessage.STATE_REJECTED, item)
                                .sendToTarget();
                    }
                });
        mProgressHelper.show();
    }

    private void onValidateDone(int errorCode, int newState, MessageItem item) {
        mProgressHelper.dismiss();

        switch (errorCode) {
            case CommandErrorCode.REQUEST_SUCCESS:
            case CommandErrorCode.ALREADY_VALIDATE_ADDED:
                mPushManager.updatePushMessageState(item.getMessageId(), newState);
                item.setState(newState);
                notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    private static class ViewHolder {

        public TextView titleView;
        public TextView subTitleView;
        public ImageView avatarImage;
        public TextView stateView;
        public ViewGroup btnArea;

        public int position;
    }
}
