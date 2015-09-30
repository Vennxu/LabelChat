package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemLabel;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.datastruct.UserTheme;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.fragment.friends.ValidateMessageDialog;

/**
 * Created by Leo on 2015/2/6.
 *
 * @author LinYong
 */
public class StrangerUserAdapter extends AbsUserAdapter {

    private static final int MSG_ADD_FRIEND_REQUEST_RESULT = 101;
    private static final int MSG_RECOMMEND_LABEL_RESULT = 102;

    private static final int REQUEST_SELECT_SYSTEM_LABEL = 10001;

    private Stranger stranger;
    private boolean isFriend;
    private UserLabelManager labelManager;
    private SimpleProgressHelper progressHelper;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_FRIEND_REQUEST_RESULT:
                    handleAddFriendRequestResult(msg.arg1);
                    break;
                case MSG_RECOMMEND_LABEL_RESULT:
                    handleRecommendLabelResult(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };

    public StrangerUserAdapter(Fragment fragment, UserAdapterListener listener,
                               Stranger stranger) {
        super(fragment, listener);
        Activity activity = fragment.getActivity();
        this.stranger = stranger;
        isFriend = isStrangerFriend(stranger);
        labelManager = UserLabelManager.getInstance(activity);
        progressHelper = new SimpleProgressHelper(fragment);
        baseUserInfo = BaseUserInfo.fromStranger(stranger);
    }

    @Override
    protected BasePage newContentPage(PageEnum page) {
        BasePage newPage;

        switch (page) {
            case USER_INFO:
                newPage = new StrangerInfoPage(fragment, stranger);
                break;
            case LABEL:
                newPage = new StrangerLabelPage(fragment, stranger);
                break;
            case LABEL_STORY:
                newPage = new MyLabelStoryPage(fragment, stranger);
                break;
            case THROW_PHOTO:
                newPage = new UserThrowPhotosPage(fragment, stranger.getUserId());
                break;
            default:
                newPage = null;
                break;
        }

        return newPage;
    }

    @Override
    public boolean showAvatarRightIcon() {
        return true;
    }

    @Override
    public int getAvatarRightIcon() {
        return R.drawable.ic_recommend_label_to_user;
    }

    @Override
    public void onAvatarRightIconClick() {
        onRecommendLabel();
    }

    @Override
    public void setupOperationBar(LayoutInflater inflater, ViewGroup container) {
        View bar = inflater.inflate(R.layout.user_show_stranger_bar, container, false);
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.chat_first:
                        UILauncher.launchStrangerChattingUI(getActivity(), stranger);
                        break;
                    case R.id.add_as_friend:
                        onAddAsFriend();
                        break;
                    default:
                        break;
                }
            }
        };
        TextView chatBtn = (TextView) bar.findViewById(R.id.chat_first);
        chatBtn.setOnClickListener(clickListener);
        if (isFriend) {
            chatBtn.setText(R.string.chat);
        }
        View addFriendBtn = bar.findViewById(R.id.add_as_friend);
        View right = bar.findViewById(R.id.right);
        View center = bar.findViewById(R.id.center);
        right.setVisibility(isFriend ? View.GONE : View.VISIBLE);
        center.setVisibility(isFriend ? View.GONE : View.VISIBLE);
        addFriendBtn.setVisibility(isFriend ? View.GONE : View.VISIBLE);
        addFriendBtn.setOnClickListener(clickListener);
        container.addView(bar);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SELECT_SYSTEM_LABEL:
                handleSelectSystemLabel(resultCode, data);
                break;
            default:
                break;
        }
    }

    @Override
    public UserTheme getUserTheme() {
        return stranger.getTheme();
    }

    private void onAddAsFriend() {
        showValidateMessageDialog();
    }

    private void showValidateMessageDialog() {
        ValidateMessageDialog.newInstance(new ValidateMessageDialog.Listener() {
            @Override
            public void onCancel(String message) {
            }

            @Override
            public void onSubmit(String message) {
                sendAddRequest(message);
            }
        }).show(getFragmentManager(), ValidateMessageDialog.class.getSimpleName());
    }

    private void sendAddRequest(String message) {
        Activity activity = getActivity();
        AccountManager accountManager = AccountManager.getInstance(activity);
        ContactsManager contactsManager = ContactsManager.getInstance(activity);

        if (!accountManager.getLabelCode().equals(stranger.getLabelCode())
                && !accountManager.getUserId().equals(stranger.getUserId())) {
            contactsManager.requestAddFriend(stranger.getUserId(),
                    stranger.getLabelCode(), message,
                    new FunctionCallListener() {
                        @Override
                        public void onCallResult(int result, int errorCode, String errorDesc) {
                            Message msg = mHandler.obtainMessage(MSG_ADD_FRIEND_REQUEST_RESULT,
                                    result, errorCode, errorCode);
                            mHandler.sendMessage(msg);
                        }
                    });
            progressHelper.show();
        } else {
            Toast.makeText(getActivity(), R.string.cannot_add_themselves_as_friends,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void handleAddFriendRequestResult(int result) {
        Activity activity = getActivity();

        progressHelper.dismiss();
        if (activity != null) {
            Toast.makeText(activity, result == FunctionCallListener.RESULT_CALL_SUCCESS
                            ? R.string.request_success : R.string.request_failure,
                    Toast.LENGTH_SHORT).show();
            onBackIconClick();
        }
    }

    private boolean isStrangerFriend(Stranger stranger) {
        return ContactsManager.getInstance(getActivity())
                .getUserContactByUserId(stranger.getUserId()) != null;
    }

    private void onRecommendLabel() {
        UserLabel[] labels = stranger.getLabels();
        String[] filterLabelIds = new String[labels.length];

        for (int i = 0; i < labels.length; ++i) {
            filterLabelIds[i] = labels[i].getId();
        }
        UILauncher.launchSelectSystemLabelUI(fragment,
                REQUEST_SELECT_SYSTEM_LABEL, filterLabelIds,
                fragment.getString(R.string.recommend_label_for_someone,
                        fragment.getString(stranger.getSex() == ConstantCode.USER_SEX_FEMALE
                                ? R.string.her : R.string.he)),
                fragment.getString(R.string.select), 1);
    }

    private void handleSelectSystemLabel(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            Parcelable[] parcelables = data.getParcelableArrayExtra("selected_labels");
            if (parcelables != null && parcelables.length > 0) {
                SystemLabel label = (SystemLabel) parcelables[0];
                recommendLabel(new BaseLabel[]{label.toBaseLabel()});
            }
        }
    }

    private void recommendLabel(BaseLabel[] labels) {
        progressHelper.show();
        String strangerUserId = stranger.getUserId();
        FunctionCallListener callListener = new FunctionCallListener() {
            @Override
            public void onCallResult(int result, int errorCode, String errorDesc) {
                mHandler.sendMessage(mHandler.obtainMessage(MSG_RECOMMEND_LABEL_RESULT,
                        result, errorCode));
            }
        };

        if (isFriend) {
            labelManager.recommendLabel(strangerUserId, labels, callListener);
        } else {
            labelManager.recommendStrangerLabel(strangerUserId, labels, callListener);
        }
    }

    private void handleRecommendLabelResult(int result) {
        Activity activity = getActivity();
        int resId;

        progressHelper.dismiss();
        switch (result) {
            case FunctionCallListener.RESULT_CALL_SUCCESS:
                resId = R.string.recommend_label_success;
                break;
            default:
                resId = R.string.recommend_label_failed;
                break;
        }

        if (activity != null) {
            Toast.makeText(activity, resId, Toast.LENGTH_SHORT).show();
        }
    }
}
