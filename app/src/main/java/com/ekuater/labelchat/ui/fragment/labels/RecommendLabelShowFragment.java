package com.ekuater.labelchat.ui.fragment.labels;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LabelRecommendMessage;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.StrangerRecommendLabelMessage;
import com.ekuater.labelchat.datastruct.SystemLabel;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.util.MiscUtils;

/**
 * Created by Leo on 2015/1/27.
 *
 * @author LinYong
 */
public class RecommendLabelShowFragment extends Fragment
        implements View.OnClickListener {

    public static final String EXTRA_MESSAGE_ID = "message_id";

    private static final int MSG_ON_LABEL_ADD_RESULT = 101;

    private static enum PresenterType {
        CONTACT,
        STRANGER,
    }

    private static class RecommendDetail {

        public PresenterType type;
        public SystemLabel[] labels;
        public Object presenter;
    }

    private PushMessageManager mPushManager;
    private UserLabelManager mLabelManager;
    private ContactsManager mContactsManager;
    private AvatarManager mAvatarManager;
    private long mMessageId;
    private RecommendDetail mRecommendDetail;
    private int mMessageState = -1;
    private boolean mLabelAdding;
    private SimpleProgressDialog mProgressDialog;

    private TextView mLabelView;
    private TextView mRecommendView;
    private View mValidateArea;
    private TextView mProcessedText;
    private ImageView mAvatarView;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ON_LABEL_ADD_RESULT:
                    handleLabelAddResult(msg.arg1 == ConstantCode
                            .LABEL_OPERATION_SUCCESS);
                    break;
                default:
                    break;
            }
        }
    };

    private final UserLabelManager.AbsListener mLabelListener
            = new UserLabelManager.AbsListener() {
        @Override
        public void onLabelAdded(int result) {
            mHandler.sendMessage(mHandler.obtainMessage(
                    MSG_ON_LABEL_ADD_RESULT, result, 0));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.recommend_label_for_me);
        }

        mPushManager = PushMessageManager.getInstance(activity);
        mLabelManager = UserLabelManager.getInstance(activity);
        mContactsManager = ContactsManager.getInstance(activity);
        mAvatarManager = AvatarManager.getInstance(activity);
        loadMessage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recommend_label_show,
                container, false);
        mValidateArea = rootView.findViewById(R.id.validate_area);
        mProcessedText = (TextView) rootView.findViewById(R.id.processed_message);
        mLabelView = (TextView) rootView.findViewById(R.id.label);
        mRecommendView = (TextView) rootView.findViewById(R.id.recommend_label_text);
        mAvatarView = (ImageView) rootView.findViewById(R.id.avatar_image);
        mAvatarView.setOnClickListener(this);
        rootView.findViewById(R.id.btn_reject).setOnClickListener(this);
        rootView.findViewById(R.id.btn_agree).setOnClickListener(this);
        bindLabelView();
        updateUIStateMode();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLabelAdding) {
            mLabelManager.unregisterListener(mLabelListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reject:
                onReject();
                break;
            case R.id.btn_agree:
                onAccept();
                break;
            case R.id.avatar_image:
                onAvatarImageClick();
                break;
            default:
                break;
        }
    }

    private void bindLabelView() {
        if (mRecommendDetail == null) {
            return;
        }

        SystemLabel[] systemLabels = mRecommendDetail.labels;
        if (systemLabels != null && systemLabels.length > 0) {
            SystemLabel addLabel = systemLabels[0];
            if (addLabel != null) {
                mLabelView.setText(addLabel.getName());
            }
        }

        mRecommendView.setText(getString(R.string.someone_label_above_for_me,
                getPresenterName()));
        MiscUtils.showAvatarThumb(mAvatarManager, getPresenterAvatarThumb(),
                mAvatarView);
    }

    private String getPresenterName() {
        String name;

        switch (mRecommendDetail.type) {
            case CONTACT: {
                UserContact contact = (UserContact) mRecommendDetail.presenter;
                name = contact != null ? contact.getShowName() : "";
                break;
            }
            case STRANGER: {
                Stranger stranger = (Stranger) mRecommendDetail.presenter;
                name = stranger != null ? stranger.getShowName() : "";
                break;
            }
            default:
                name = "";
                break;
        }

        return name;
    }

    private String getPresenterAvatarThumb() {
        String avatarThumb;

        switch (mRecommendDetail.type) {
            case CONTACT: {
                UserContact contact = (UserContact) mRecommendDetail.presenter;
                avatarThumb = contact != null ? contact.getAvatarThumb() : "";
                break;
            }
            case STRANGER: {
                Stranger stranger = (Stranger) mRecommendDetail.presenter;
                avatarThumb = stranger != null ? stranger.getAvatarThumb() : "";
                break;
            }
            default:
                avatarThumb = "";
                break;
        }

        return avatarThumb;
    }

    private void onAvatarImageClick() {
        switch (mRecommendDetail.type) {
            case CONTACT: {
                UserContact contact = (UserContact) mRecommendDetail.presenter;
                if (contact != null) {
                    UILauncher.launchFriendDetailUI(getActivity(), contact.getUserId());
                }
                break;
            }
            case STRANGER: {
                Stranger stranger = (Stranger) mRecommendDetail.presenter;
                if (stranger != null) {
                    UILauncher.launchStrangerDetailUI(getActivity(), stranger);
                }
                break;
            }
            default:
                break;
        }
    }

    private void onReject() {
        updateMessageState(LabelRecommendMessage.STATE_REJECTED);
        updateUIStateMode();
    }

    private void onAccept() {
        SystemLabel[] systemLabels = mRecommendDetail.labels;
        if (systemLabels != null && systemLabels.length > 0) {
            SystemLabel addLabel = systemLabels[0];
            if (addLabel != null) {
                mLabelAdding = true;
                mLabelManager.registerListener(mLabelListener);
                mLabelManager.addUserLabels(new BaseLabel[]{addLabel.toBaseLabel()});
                showProgressDialog();
            }
        }
    }

    private void handleLabelAddResult(boolean success) {
        dismissProgressDialog();
        mLabelAdding = false;
        mLabelManager.unregisterListener(mLabelListener);

        if (success) {
            updateMessageState(LabelRecommendMessage.STATE_ACCEPTED);
            updateUIStateMode();
        }
    }

    private void updateMessageState(int state) {
        mMessageState = state;
        mPushManager.updatePushMessageState(mMessageId, mMessageState);
    }

    private void updateUIStateMode() {
        switch (mMessageState) {
            case LabelRecommendMessage.STATE_ACCEPTED:
                mProcessedText.setVisibility(View.VISIBLE);
                mValidateArea.setVisibility(View.GONE);
                mProcessedText.setText(R.string.already_agree);
                break;
            case LabelRecommendMessage.STATE_REJECTED:
                mProcessedText.setVisibility(View.VISIBLE);
                mValidateArea.setVisibility(View.GONE);
                mProcessedText.setText(R.string.already_reject);
                break;
            default:
                mProcessedText.setVisibility(View.GONE);
                mValidateArea.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void loadMessage() {
        Bundle args = getArguments();
        mMessageId = args.getLong(EXTRA_MESSAGE_ID, -1);
        SystemPush systemPush = mPushManager.getPushMessage(mMessageId);

        mRecommendDetail = null;

        if (systemPush != null) {
            int state = systemPush.getState();
            LabelRecommendMessage recommendMessage
                    = LabelRecommendMessage.build(systemPush);
            StrangerRecommendLabelMessage strangerRecommendMessage
                    = StrangerRecommendLabelMessage.build(systemPush);

            if (recommendMessage != null) {
                mRecommendDetail = new RecommendDetail();
                mRecommendDetail.type = PresenterType.CONTACT;
                mRecommendDetail.labels = recommendMessage.getRecommendLabels();
                mRecommendDetail.presenter = mContactsManager.getUserContactByUserId(
                        recommendMessage.getFriendUserId());
            } else if (strangerRecommendMessage != null) {
                mRecommendDetail = new RecommendDetail();
                mRecommendDetail.type = PresenterType.STRANGER;
                mRecommendDetail.labels = strangerRecommendMessage.getRecommendLabels();
                mRecommendDetail.presenter = strangerRecommendMessage.getStranger();
            }

            if (state == SystemPush.STATE_UNPROCESSED) {
                mPushManager.updatePushMessageProcessed(mMessageId);
                mMessageState = SystemPush.STATE_PROCESSED;
            } else {
                mMessageState = state;
            }
        }

        if (mRecommendDetail == null) {
            finish();
        }
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = SimpleProgressDialog.newInstance();
            mProgressDialog.show(getFragmentManager(), "SimpleProgressDialog");
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
