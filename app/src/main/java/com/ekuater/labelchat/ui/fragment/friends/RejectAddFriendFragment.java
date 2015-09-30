package com.ekuater.labelchat.ui.fragment.friends;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.AddFriendRejectResultMessage;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.ui.widget.LabelFlow;
import com.ekuater.labelchat.ui.widget.MaxSizeScrollView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2015/1/23.
 *
 * @author FanChong
 */
public class RejectAddFriendFragment extends Fragment {

    public static final String EXTRA_MESSAGE_ID = "message_id";

    private CircleImageView mAvatarImage;
    private ImageView mGenderImage;
    private TextView mNickName;
    private TextView mDistance;
    private LabelFlow mLabelFlow;
    private MaxSizeScrollView mLabelScrollView;

    private LocationInfo mLocationInfo;
    private LocationInfo mStrangerLocationInfo;
    private AvatarManager mAvatarManger;
    private PushMessageManager mPushManager;
    private AddFriendRejectResultMessage mAddFriendRejectResultMessage;
    private long mMessageId;
    private int mMessageState;
    private Stranger mStranger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.validate_friend);
        }
        mPushManager = PushMessageManager.getInstance(getActivity());
        mAvatarManger = AvatarManager.getInstance(getActivity());
        loadMessage();
        mStranger = mAddFriendRejectResultMessage.getStranger();
        mLocationInfo = AccountManager.getInstance(getActivity()).getLocation();
        mStrangerLocationInfo = mStranger.getLocation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reject_add_friend, container, false);
        initView(view);
        MiscUtils.showAvatarThumb(AvatarManager.getInstance(getActivity()),
                mStranger.getAvatarThumb(), mAvatarImage);
        switch (mStranger.getSex()) {
            case ConstantCode.USER_SEX_FEMALE:
                mGenderImage.setImageResource(R.drawable.icon_female);
                break;
            default:
                mGenderImage.setImageResource(R.drawable.icon_male);
                break;
        }
        mNickName.setText(mStranger.getNickname());
        if (mLocationInfo != null && mStrangerLocationInfo != null) {
            mDistance.setText(MiscUtils.getDistanceString(getActivity(), mLocationInfo.getDistance(mStrangerLocationInfo)));
        }
        final List<String> ownerLabels = new ArrayList<String>();
        final UserLabel[] userLabels = UserLabelManager.getInstance(getActivity()).getAllLabels();
        if (userLabels != null) {
            for (UserLabel label : userLabels) {
                ownerLabels.add(label.getName());
            }
        }
        mLabelFlow.setOwnerLabels(ownerLabels);
        mLabelFlow.removeAllViews();
        UserLabel[] labels = mStranger.getLabels();
        if (labels != null && labels.length > 0) {
            for (UserLabel label : labels) {
                mLabelFlow.addLabel(label.getName());
            }
        }

        mLabelScrollView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                limitLabelScrollHeight();
            }
        });

        return view;
    }

    private void initView(View view) {
        mAvatarImage = (CircleImageView) view.findViewById(R.id.avatar_image);
        mGenderImage = (ImageView) view.findViewById(R.id.gender);
        mNickName = (TextView) view.findViewById(R.id.nickname);
        mDistance = (TextView) view.findViewById(R.id.distance);
        mLabelFlow = (LabelFlow) view.findViewById(R.id.user_labels);
        mLabelScrollView=(MaxSizeScrollView)view.findViewById(R.id.label_scroll);
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

    private void loadMessage() {
        Bundle args = getArguments();
        mMessageId = -1;
        if (args != null) {
            mMessageId = args.getLong(EXTRA_MESSAGE_ID, mMessageId);
        }


        SystemPush push = mPushManager.getPushMessage(mMessageId);
        if (push != null) {
            final int state = push.getState();
            mAddFriendRejectResultMessage = AddFriendRejectResultMessage.build(push);
            if (state == SystemPush.STATE_UNPROCESSED) {
                mPushManager.updatePushMessageProcessed(mMessageId);
                mMessageState = SystemPush.STATE_PROCESSED;
            } else {
                mMessageState = state;
            }
        }
    }

}
