package com.ekuater.labelchat.ui.fragment.friends;

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
import android.widget.Button;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.LabelPraise;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.util.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Leo on 2015/1/16.
 *
 * @author LinYong
 */
public class StrangerDetailFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = StrangerDetailFragment.class.getSimpleName();

    public static final String EXTRA_STRANGER = "extra_stranger";

    private static final int MSG_LOAD_LABEL_PRAISE_RESULT = 101;
    private static final int MSG_ADD_FRIEND_REQUEST_RESULT = 102;

    private ContactsManager mContactsManager;
    private UserLabelManager mLabelManager;
    private AccountManager mAccountManager;
    private Stranger mStranger;
    private Map<String, UserLabel> mLabelIdMap;
    private List<String> mMyLabelIdList;
    private boolean mIsFriend;
    private UserDetailHelper mDetailHelper;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_LABEL_PRAISE_RESULT:
                    handleLoadLabelPraiseResult(msg.arg1, (LabelPraise[]) msg.obj);
                    break;
                case MSG_ADD_FRIEND_REQUEST_RESULT:
                    handleAddFriendRequestResult(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };

    private final UserDetailHelper.Listener mHelperListener
            = new UserDetailHelper.AbsListener() {
        @Override
        public void onLabelClick(UserLabel label) {
//            UILauncher.launchFragmentLabelStoryUI(getActivity(),
//                    label.toBaseLabel(), mStranger.getUserId());
        }

        @Override
        public void onLabelLongClick(UserLabel label) {
            if (!(mMyLabelIdList.contains(label.getId()))) {
                UILauncher.launchLabelOptionUI(getFragmentManager(),
                        label.toBaseLabel());
            }
        }
    };

    private SimpleProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        activity.setResult(Activity.RESULT_OK);

        mContactsManager = ContactsManager.getInstance(activity);
        mLabelManager = UserLabelManager.getInstance(activity);
        mAccountManager = AccountManager.getInstance(activity);
        mStranger = getArguments().getParcelable(EXTRA_STRANGER);
        mLabelIdMap = buildLabelIdMap(mStranger.getLabels());
        mMyLabelIdList = buildLabelIdList(mLabelManager.getAllLabels());
        mIsFriend = mContactsManager.getUserContactByUserId(mStranger.getUserId()) != null;
        mDetailHelper = new UserDetailHelper(activity, mHelperListener);
        mDetailHelper.setDetail(new UserDetailHelper.Detail(mStranger, mIsFriend));

        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mStranger.getShowName());
        }

        loadLabelPraise();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stranger_detail, container, false);
        Button addFriendBtn = (Button) view.findViewById(R.id.add_as_friend);
        Button chatFirstBtn = (Button) view.findViewById(R.id.chat_first);
        addFriendBtn.setOnClickListener(this);
        addFriendBtn.setVisibility(mIsFriend ? View.GONE : View.VISIBLE);
        chatFirstBtn.setOnClickListener(this);

        mDetailHelper.setParentView(view);
        mDetailHelper.bindInfo();
        mDetailHelper.bindLabels();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_as_friend:
                onAddAsFriend();
                break;
            case R.id.chat_first:
                onChatFirst();
                break;
            default:
                break;
        }
    }

    private Map<String, UserLabel> buildLabelIdMap(UserLabel[] labels) {
        Map<String, UserLabel> labelMap = new HashMap<String, UserLabel>();

        if (labels != null && labels.length > 0) {
            for (UserLabel label : labels) {
                labelMap.put(label.getId(), label);
            }
        }

        return labelMap;
    }

    private List<String> buildLabelIdList(UserLabel[] labels) {
        List<String> idList = new ArrayList<String>();

        if (labels != null && labels.length > 0) {
            for (UserLabel label : labels) {
                idList.add(label.getId());
            }
        }

        return idList;
    }

    private void loadLabelPraise() {
        mLabelManager.queryLabelPraise(mStranger.getUserId(),
                new UserLabelManager.LabelPraiseQueryObserver() {
                    @Override
                    public void onQueryResult(int result, LabelPraise[] labelPraises) {
                        Message msg = mHandler.obtainMessage(MSG_LOAD_LABEL_PRAISE_RESULT,
                                result, 0, labelPraises);
                        mHandler.sendMessage(msg);
                    }
                });
    }

    private void handleLoadLabelPraiseResult(int result, LabelPraise[] labelPraises) {
        L.v(TAG, "handleLoadLabelPraiseResult(), result=%1$d, count=%2$d", result,
                labelPraises != null ? labelPraises.length : 0);
        updateContactLabelPraise(labelPraises);
    }

    private void updateContactLabelPraise(LabelPraise[] labelPraises) {
        if (labelPraises != null && labelPraises.length > 0) {
            final String userId = mStranger.getUserId();

            for (LabelPraise labelPraise : labelPraises) {
                UserLabel userLabel = mLabelIdMap.get(labelPraise.getLabelId());
                if (userLabel != null && userId.equals(labelPraise.getUserId())) {
                    userLabel.setPraiseCount(labelPraise.getPraiseCount());
                }
            }
            mDetailHelper.setDetail(new UserDetailHelper.Detail(mStranger, mIsFriend));
            mDetailHelper.bindLabels();
        }
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

    private void onAddAsFriend() {
        if (mContactsManager.isInGuestMode()) {
            UILauncher.launchLoginPromptUI(getFragmentManager());
        } else {
            showValidateMessageDialog();
        }
    }

    private void onChatFirst() {
        if (mContactsManager.isInGuestMode()) {
            UILauncher.launchLoginPromptUI(getFragmentManager());
        } else {
            UILauncher.launchStrangerChattingUI(getActivity(), mStranger);
            finish();
        }
    }

    private void sendAddRequest(String message) {
        if (mContactsManager.isInGuestMode()) {
            UILauncher.launchLoginPromptUI(getFragmentManager());
        } else if (!mAccountManager.getLabelCode().equals(mStranger.getLabelCode())
                && !mAccountManager.getUserId().equals(mStranger.getUserId())) {
            mContactsManager.requestAddFriend(mStranger.getUserId(),
                    mStranger.getLabelCode(), message,
                    new FunctionCallListener() {
                        @Override
                        public void onCallResult(int result, int errorCode, String errorDesc) {
                            Message msg = mHandler.obtainMessage(MSG_ADD_FRIEND_REQUEST_RESULT,
                                    result, errorCode, errorCode);
                            mHandler.sendMessage(msg);
                        }
                    });
            showProgressDialog();
        } else {
            ShowToast.makeText(getActivity(), R.drawable.emoji_sad, getString(R.string.cannot_add_themselves_as_friends)).show();
        }
    }

    private void handleAddFriendRequestResult(int result) {
        Activity activity = getActivity();

        dismissProgressDialog();
        if (activity != null) {
            if (result == FunctionCallListener.RESULT_CALL_SUCCESS) {
                ShowToast.makeText(getActivity(), R.drawable.emoji_smile, getString(R.string.request_success)).show();
            } else {
                ShowToast.makeText(getActivity(), R.drawable.emoji_cry, getString(R.string.request_failure)).show();
            }
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
            mProgressDialog.show(getFragmentManager(), "ProgressDialog");
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
