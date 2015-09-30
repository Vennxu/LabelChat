package com.ekuater.labelchat.ui.fragment.friends;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
//TODO
import android.widget.Toast;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LabelPraise;
import com.ekuater.labelchat.datastruct.SystemLabel;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.fragment.SimpleEditDialog;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.util.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author LinYong
 */
public class ContactDetailFragment extends Fragment
        implements View.OnClickListener {

    private static final String TAG = ContactDetailFragment.class.getSimpleName();

    public static final String EXTRA_FRIEND_USER_ID = "friend_userId";

    private static final int MSG_MODIFY_REMARK_RESULT = 101;
    private static final int MSG_DELETE_FRIEND_RESULT = 102;
    private static final int MSG_CONTACT_DEFRIENDED_ME = 103;
    private static final int MSG_QUERY_CONTACT_INFO_RESULT = 104;
    private static final int MSG_LOAD_LABEL_PRAISE_RESULT = 105;
    private static final int MSG_RECOMMEND_LABEL_RESULT = 106;

    private static final int REQUEST_SELECT_SYSTEM_LABEL = 101;

    private ContactsManager mContactsManager;
    private UserLabelManager mLabelManager;
    private UserContact mContact;
    private Map<String, UserLabel> mLabelIdMap;
    private List<String> mMyLabelIdList;
    private SimpleProgressDialog mProgressDialog;
    private String mNewRemark = "";
    private boolean mInModifyRemark = false;
    private LabelPraise[] mLabelPraises;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MODIFY_REMARK_RESULT:
                    handleModifyRemarkResult(msg.obj);
                    break;
                case MSG_DELETE_FRIEND_RESULT:
                    handleDeleteFriendResult(msg.obj);
                    break;
                case MSG_CONTACT_DEFRIENDED_ME:
                    handleContactDefriendedMe((String) msg.obj);
                    break;
                case MSG_QUERY_CONTACT_INFO_RESULT:
                    handleQueryContactInfoResult(msg.arg1, (UserContact) msg.obj);
                    break;
                case MSG_LOAD_LABEL_PRAISE_RESULT:
                    handleLoadLabelPraiseResult(msg.arg1, (LabelPraise[]) msg.obj);
                    break;
                case MSG_RECOMMEND_LABEL_RESULT:
                    handleRecommendLabelResult(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };

    private final ContactsManager.IListener mContactListener
            = new ContactsManager.AbsListener() {
        @Override
        public void onModifyFriendRemarkResult(int result, String friendUserId,
                                               String friendRemark) {
            onModifyRemarkResult(result, friendUserId, friendRemark);
        }

        @Override
        public void onDeleteFriendResult(int result, String friendUserId, String friendLabelCode) {
            Message message = mHandler.obtainMessage(MSG_DELETE_FRIEND_RESULT,
                    new DeleteFriendResult(result, friendUserId, friendLabelCode));
            mHandler.sendMessage(message);
        }

        @Override
        public void onContactDefriendedMe(String friendUserId) {
            Message message = mHandler.obtainMessage(MSG_CONTACT_DEFRIENDED_ME, friendUserId);
            mHandler.sendMessage(message);
        }
    };
    private final UserDetailHelper.Listener mHelperListener
            = new UserDetailHelper.AbsListener() {
        @Override
        public void onLabelClick(UserLabel label) {
//            UILauncher.launchFragmentLabelStoryUI(getActivity(),
//                    label.toBaseLabel(), mContact.getUserId());
        }

        @Override
        public void onLabelLongClick(UserLabel label) {
            if (!(mMyLabelIdList.contains(label.getId()))) {
                UILauncher.launchLabelOptionUI(getFragmentManager(),
                        label.toBaseLabel());
            }
        }
    };

    private boolean mContactQueried;
    private boolean mLabelPraiseLoaded;
    private UserDetailHelper mDetailHelper;

    private SimpleProgressHelper mProgressHelper = new SimpleProgressHelper(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();

        mContactsManager = ContactsManager.getInstance(activity);
        mContactsManager.registerListener(mContactListener);
        mLabelManager = UserLabelManager.getInstance(activity);

        loadContact();
        mDetailHelper = new UserDetailHelper(activity, mHelperListener);
        if (mContact != null) {
            mDetailHelper.setDetail(new UserDetailHelper.Detail(mContact));
            mLabelIdMap = buildLabelIdMap(mContact.getLabels());
        }
        mMyLabelIdList = buildLabelIdList(mLabelManager.getAllLabels());

        setHasOptionsMenu(true);
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            if (mContact != null) {
                actionBar.setTitle(mContact.getShowName());
            } else {
                actionBar.setTitle(R.string.friend_detail);
            }
        }

        mContactQueried = false;
        mLabelPraiseLoaded = false;

        if (mContact != null) {
            queryContactInfo();
            loadLabelPraise();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContactsManager.unregisterListener(mContactListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_detail, container, false);
        view.findViewById(R.id.btn_send_message).setOnClickListener(this);
        mDetailHelper.setParentView(view);
        mDetailHelper.bindInfo();
        mDetailHelper.bindLabels();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.contact_detail_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = true;

        switch (item.getItemId()) {
            case R.id.modify_remark:
                showRemarkEditDialog();
                break;
            case R.id.delete_friend:
                showDeleteFriendConfirm();
                break;
            case R.id.recommend_label:
                onRecommendLabel();
                break;
            default:
                handled = false;
                break;
        }

        return handled || super.onOptionsItemSelected(item);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send_message:
                UILauncher.launchChattingUI(getActivity(), mContact.getUserId());
                finish();
                break;
            default:
                break;
        }
    }

    private void onRecommendLabel() {
        String[] filterLabelIds = null;

        if (mLabelIdMap != null) {
            Set<String> set = mLabelIdMap.keySet();
            filterLabelIds = set.toArray(new String[set.size()]);
        }

        UILauncher.launchSelectSystemLabelUI(ContactDetailFragment.this,
                REQUEST_SELECT_SYSTEM_LABEL, filterLabelIds,
                getString(R.string.select_label),
                getString(R.string.select), 1);
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
        mProgressHelper.show();
        mLabelManager.recommendLabel(mContact.getUserId(), labels, new FunctionCallListener() {
            @Override
            public void onCallResult(int result, int errorCode, String errorDesc) {
                mHandler.sendMessage(mHandler.obtainMessage(MSG_RECOMMEND_LABEL_RESULT,
                        result, errorCode));
            }
        });
    }

    private void handleRecommendLabelResult(int result) {
        Activity activity = getActivity();
        int resId;

        mProgressHelper.dismiss();
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

    private void loadContact() {
        final Bundle args = getArguments();
        final Activity activity = getActivity();
        UserContact contact = null;

        if (args != null) {
            contact = mContactsManager.getUserContactByUserId(
                    args.getString(EXTRA_FRIEND_USER_ID));
        }

        if (contact == null) {
            Toast.makeText(activity, R.string.empty_contact, Toast.LENGTH_SHORT).show();
            finish();
        }

        final ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null && contact != null) {
            actionBar.setTitle(contact.getShowName());
        }

        mContact = contact;
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

    private void finish() {
        getActivity().finish();
    }

    private void showRemarkEditDialog() {
        SimpleEditDialog.UiConfig config = new SimpleEditDialog.UiConfig();
        config.title = getString(R.string.modify_remark);
        config.initText = mContact.getRemarkName();
        config.editHint = getString(R.string.friend_remark_hint);
        config.maxLength = getResources().getInteger(
                R.integer.friend_remark_max_length);
        config.listener = new SimpleEditDialog.IListener() {
            @Override
            public void onCancel(CharSequence text) {
            }

            @Override
            public void onOK(CharSequence text) {
                modifyRemark(text.toString());
            }
        };
        SimpleEditDialog.newInstance(config).show(getFragmentManager(),
                "RemarkEditDialog");
    }

    private void modifyRemark(String newRemark) {
        if (!mInModifyRemark) {
            newRemark = (newRemark == null) ? "" : newRemark;
            mInModifyRemark = true;
            showProgressDialog();
            mNewRemark = newRemark;
            mContactsManager.modifyFriendRemark(mContact.getUserId(), newRemark);
        }
    }

    private void queryContactInfo() {
        mContactsManager.queryContactInfo(mContact.getUserId(),
                new ContactsManager.ContactQueryObserver() {
                    @Override
                    public void onQueryResult(int result, UserContact contact) {
                        Message msg = mHandler.obtainMessage(MSG_QUERY_CONTACT_INFO_RESULT,
                                result, 0, contact);
                        mHandler.sendMessage(msg);
                    }
                });
    }

    private void handleQueryContactInfoResult(int result, UserContact contact) {
        L.v(TAG, "handleQueryContactInfoResult(), result=%1$d, count=%2$s", result, contact);
        if (contact != null && contact.getUserId().equals(mContact.getUserId())) {
            contact.setId(mContact.getId());
            mContact = contact;
            mLabelIdMap = buildLabelIdMap(mContact.getLabels());
        }
        mContactQueried = true;
        updateContactLabelPraise();
    }

    private void loadLabelPraise() {
        mLabelManager.queryLabelPraise(mContact.getUserId(),
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
        mLabelPraiseLoaded = true;
        mLabelPraises = labelPraises;
        updateContactLabelPraise();
    }

    private void updateContactLabelPraise() {
        if (!mContactQueried || !mLabelPraiseLoaded) {
            return;
        }

        if (mLabelPraises != null && mLabelPraises.length > 0) {
            final String userId = mContact.getUserId();

            for (LabelPraise labelPraise : mLabelPraises) {
                UserLabel userLabel = mLabelIdMap.get(labelPraise.getLabelId());
                if (userLabel != null && userId.equals(labelPraise.getUserId())) {
                    userLabel.setPraiseCount(labelPraise.getPraiseCount());
                }
            }
        }

        // update list view and contact
        mDetailHelper.setDetail(new UserDetailHelper.Detail(mContact));
        mDetailHelper.bindInfo();
        mDetailHelper.bindLabels();
        mContactsManager.updateContact(mContact);
    }

    private static class ModifyResult {

        public final int mResult;
        public final String mFriendUserId;
        public final String mFriendRemark;

        public ModifyResult(int result, String friendUserId,
                            String friendRemark) {
            mResult = result;
            mFriendUserId = friendUserId;
            mFriendRemark = friendRemark;
        }
    }

    private void onModifyRemarkResult(int result, String friendUserId,
                                      String friendRemark) {
        Message message = mHandler.obtainMessage(MSG_MODIFY_REMARK_RESULT,
                new ModifyResult(result, friendUserId, friendRemark));
        mHandler.sendMessage(message);
    }

    private void handleModifyRemarkResult(Object object) {
        if (!(object instanceof ModifyResult)) {
            return;
        }

        final ModifyResult result = (ModifyResult) object;

        if (!mContact.getUserId().equals(result.mFriendUserId)
                || !mNewRemark.equals(result.mFriendRemark)) {
            return;
        }

        if (mInModifyRemark) {
            final Activity activity = getActivity();

            if (result.mResult == ConstantCode.CONTACT_OPERATION_SUCCESS) {
                Toast.makeText(activity, R.string.modify_remark_success,
                        Toast.LENGTH_SHORT).show();
                mContact.setRemarkName(mNewRemark);
                final ActionBar actionBar = getActivity().getActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(mContact.getShowName());
                }
            } else {
                Toast.makeText(activity, R.string.modify_remark_failure,
                        Toast.LENGTH_SHORT).show();
            }
            mInModifyRemark = false;
            dismissProgressDialog();
        }
    }

    private void showDeleteFriendConfirm() {
        ConfirmDialogFragment.UiConfig uiConfig
                = new ConfirmDialogFragment.UiConfig(
                getString(R.string.delete_friend_confirm),
                null, false);
        ConfirmDialogFragment.IConfirmListener confirmListener
                = new ConfirmDialogFragment.AbsConfirmListener() {
            @Override
            public void onConfirm() {
                startDeleteFriend();
            }
        };
        ConfirmDialogFragment fragment = ConfirmDialogFragment.newInstance(uiConfig,
                confirmListener);
        fragment.show(getFragmentManager(), "DeleteFriendConfirm");
    }

    private void startDeleteFriend() {
        showProgressDialog();
        mContactsManager.deleteFriend(mContact.getUserId(), mContact.getLabelCode());
    }

    private static class DeleteFriendResult {

        public final int result;
        public final String friendUserId;
        public final String friendLabelCode;

        public DeleteFriendResult(int result, String friendUserId, String friendLabelCode) {
            this.result = result;
            this.friendUserId = friendUserId;
            this.friendLabelCode = friendLabelCode;
        }
    }

    private void handleDeleteFriendResult(Object object) {
        if (object instanceof DeleteFriendResult) {
            DeleteFriendResult result = (DeleteFriendResult) object;

            switch (result.result) {
                case ConstantCode.CONTACT_OPERATION_SUCCESS:
                    Toast.makeText(getActivity(), R.string.delete_friend_success,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                    break;
                default:
                    Toast.makeText(getActivity(), R.string.delete_friend_failure,
                            Toast.LENGTH_SHORT).show();
                    break;
            }

            dismissProgressDialog();
        }
    }

    private void handleContactDefriendedMe(String friendUserId) {
        if (mContact != null && mContact.getUserId().equals(friendUserId)) {
            finish();
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
