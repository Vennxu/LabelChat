package com.ekuater.labelchat.ui.fragment.friends;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.datastruct.ValidateAddFriendMessage;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.LabelFlow;
import com.ekuater.labelchat.ui.widget.MaxSizeScrollView;
import com.ekuater.labelchat.util.L;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LinYong
 */
public class ValidateAddFriendFragment extends Fragment
        implements View.OnClickListener {

    private static final String TAG = ValidateAddFriendFragment.class.getSimpleName();

    public static final String EXTRA_MESSAGE_ID = "message_id";

    private static final int UI_STATE_NORMAL = 0;
    private static final int UI_STATE_AGREED = 1;
    private static final int UI_STATE_REJECTED = 2;
    private static final int UI_STATE_REJECTING = 3;

    private ValidateAddFriendMessage mValidateMessage;
    private PushMessageManager mPushManager;
    private long mMessageId;
    private int mMessageState;
    private int mUiState = -1;

    private TextView mValidatedArea;
    private View mValidateArea;
    private View mRejectArea;
    private EditText mRejectMessageEdit;
    private MaxSizeScrollView mLabelScrollView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.validate_friend);
        }

        mPushManager = PushMessageManager.getInstance(activity);
        loadMessage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_friend_validate_add_friend,
                container, false);

        if (mValidateMessage == null) {
            return view;
        }

        final Activity activity = getActivity();
        final String validateMsg = mValidateMessage.getValidateMessage();
        final Stranger stranger = mValidateMessage.getStranger();
        final List<String> ownerLabels = new ArrayList<>();
        final UserLabel[] userLabels = UserLabelManager.getInstance(getActivity()).getAllLabels();
        if (userLabels != null) {
            for (UserLabel label : userLabels) {
                ownerLabels.add(label.getName());
            }
        }

        TextView nameText = (TextView) view.findViewById(R.id.nickname);
        nameText.setText(stranger.getShowName());
        ImageView sexImage = (ImageView) view.findViewById(R.id.gender);
        sexImage.setImageResource(ConstantCode.getSexImageResource(stranger.getSex()));
        TextView messageText = (TextView) view.findViewById(R.id.validate_message);
        messageText.setText(validateMsg);
        view.findViewById(R.id.btn_reject).setOnClickListener(this);
        view.findViewById(R.id.btn_agree).setOnClickListener(this);
        view.findViewById(R.id.btn_reject_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_reject_ok).setOnClickListener(this);
        ImageView avatarImage = (ImageView) view.findViewById(R.id.avatar_image);
        MiscUtils.showAvatarThumb(AvatarManager.getInstance(getActivity()),
                stranger.getAvatarThumb(), avatarImage);
        TextView distanceView = (TextView) view.findViewById(R.id.distance);
        LocationInfo strangerLocation = stranger.getLocation();
        LocationInfo myLocation = AccountManager.getInstance(activity).getLocation();

        if (myLocation != null && strangerLocation != null) {
            distanceView.setText(MiscUtils.getDistanceString(activity,
                    myLocation.getDistance(strangerLocation)));
            distanceView.setVisibility(View.VISIBLE);
        } else {
            distanceView.setVisibility(View.GONE);
        }

        LabelFlow labelFlow = (LabelFlow) view.findViewById(R.id.user_labels);
        labelFlow.setOwnerLabels(ownerLabels);
        labelFlow.removeAllViews();
        UserLabel[] labels = stranger.getLabels();
        if (labels != null && labels.length > 0) {
            for (UserLabel label : labels) {
                labelFlow.addLabel(label.getName());
            }
        }

        mValidatedArea = (TextView) view.findViewById(R.id.validated_message);
        mRejectArea = view.findViewById(R.id.reject_area);
        mValidateArea = view.findViewById(R.id.validate_area);
        mRejectMessageEdit = (EditText) view.findViewById(R.id.reject_message);

        int uiState;
        switch (mMessageState) {
            case ValidateAddFriendMessage.STATE_AGREED:
                uiState = UI_STATE_AGREED;
                break;
            case ValidateAddFriendMessage.STATE_REJECTED:
                uiState = UI_STATE_REJECTED;
                break;
            default:
                uiState = UI_STATE_NORMAL;
                break;
        }
        switchUiState(uiState);

        mLabelScrollView = (MaxSizeScrollView) view.findViewById(R.id.label_scroll);
        mLabelScrollView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                limitLabelScrollHeight();
            }
        });

        return view;
    }

    private void limitLabelScrollHeight() {
        View view = getView();
        if (view != null) {
            int height = mLabelScrollView.getMeasuredHeight();
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)
                    mLabelScrollView.getLayoutParams();
            mLabelScrollView.setMaxHeight(height);
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.weight = 0;
            view.requestLayout();
        }
    }

    @Override
    public void onClick(View v) {
        final Stranger stranger = mValidateMessage.getStranger();
        final String userId = stranger.getUserId();
        final String labelCode = stranger.getLabelCode();
        final ContactsManager contactsManager = ContactsManager.getInstance(getActivity());

        switch (v.getId()) {
            case R.id.btn_reject:
                switchUiState(UI_STATE_REJECTING);
                break;
            case R.id.btn_agree:
                contactsManager.acceptAddFriendInvitation(userId, labelCode, "",
                        new ValidCallListener(mPushManager, mMessageId,
                                ValidateAddFriendMessage.STATE_AGREED));
                finish();
                break;
            case R.id.btn_reject_ok: {
                String rejectMessage = mRejectMessageEdit.getText().toString();
                contactsManager.rejectAddFriendInvitation(userId, labelCode,
                        rejectMessage, new ValidCallListener(mPushManager, mMessageId,
                                ValidateAddFriendMessage.STATE_REJECTED));
                finish();
                break;
            }
            case R.id.btn_reject_cancel:
                switchUiState(UI_STATE_NORMAL);
                break;
            default:
                break;
        }
    }

    private void switchUiState(int newState) {
        if (newState == mUiState) {
            return;
        }

        switch (newState) {
            case UI_STATE_NORMAL:
                mValidatedArea.setVisibility(View.GONE);
                mRejectArea.setVisibility(View.GONE);
                mValidateArea.setVisibility(View.VISIBLE);
                break;
            case UI_STATE_AGREED:
                mValidatedArea.setVisibility(View.VISIBLE);
                mRejectArea.setVisibility(View.GONE);
                mValidateArea.setVisibility(View.GONE);
                mValidatedArea.setText(R.string.already_agree);
                break;
            case UI_STATE_REJECTED:
                mValidatedArea.setVisibility(View.VISIBLE);
                mRejectArea.setVisibility(View.GONE);
                mValidateArea.setVisibility(View.GONE);
                mValidatedArea.setText(R.string.already_reject);
                break;
            case UI_STATE_REJECTING:
                mValidatedArea.setVisibility(View.GONE);
                mRejectArea.setVisibility(View.VISIBLE);
                mValidateArea.setVisibility(View.GONE);
                mRejectMessageEdit.requestFocus();
                break;
            default:
                break;
        }
        mUiState = newState;
    }

    private void loadMessage() {
        Bundle args = getArguments();
        mMessageId = -1;

        L.v(TAG, "loadMessage(), args=" + args);

        if (args != null) {
            mMessageId = args.getLong(EXTRA_MESSAGE_ID, mMessageId);
        }

        L.v(TAG, "loadMessage(), mMessageId=" + mMessageId);

        SystemPush push = mPushManager.getPushMessage(mMessageId);
        if (push != null) {
            final int state = push.getState();
            mValidateMessage = ValidateAddFriendMessage.build(push);
            if (state == SystemPush.STATE_UNPROCESSED) {
                mPushManager.updatePushMessageProcessed(mMessageId);
                mMessageState = SystemPush.STATE_PROCESSED;
            } else {
                mMessageState = state;
            }
        }

        if (mValidateMessage == null) {
            finish();
            L.w(TAG, "loadMessage(), empty message, just finish.");
        }
    }

    private void finish() {
        getActivity().finish();
    }

    private static class ValidCallListener implements FunctionCallListener {

        private final PushMessageManager mPushManager;
        private final long mMessageId;
        private final int mNewState;

        public ValidCallListener(PushMessageManager pushManager, long messageId, int newState) {
            mPushManager = pushManager;
            mMessageId = messageId;
            mNewState = newState;
        }

        @Override
        public void onCallResult(int result, int errorCode, String errorDesc) {
            switch (errorCode) {
                case CommandErrorCode.REQUEST_SUCCESS:
                case CommandErrorCode.ALREADY_VALIDATE_ADDED:
                    mPushManager.updatePushMessageState(mMessageId,
                            mNewState);
                    break;
                default:
                    break;
            }
        }
    }
}
