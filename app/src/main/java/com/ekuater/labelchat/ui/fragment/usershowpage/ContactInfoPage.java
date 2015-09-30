package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.fragment.SimpleEditDialog;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.util.MiscUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/2/4.
 *
 * @author LinYong
 */
public class ContactInfoPage extends BasePage {

    private static final int MSG_MODIFY_REMARK_RESULT = 101;
    private static final int MSG_DELETE_FRIEND_RESULT = 102;

    private UserContact mContact;
    private UserInfoAdapter mAdapter;
    private ContactsManager mContactsManager;
    private SimpleProgressHelper mProgressHelper;
    private boolean mInModifyRemark = false;
    private String mNewRemark = "";
    private UserInfoItem.NormalInfoItem mRemarkItem;
    private SimpleProgressDialog mProgressDialog;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MODIFY_REMARK_RESULT:
                    handleModifyRemarkResult((ModifyResult) msg.obj);
                    break;
                case MSG_DELETE_FRIEND_RESULT:
                    handleDeleteFriendResult(msg.obj);
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
    };

    public ContactInfoPage(Fragment fragment, UserContact contact) {
        super(fragment);
        mContact = contact;
        mAdapter = new UserInfoAdapter(mContext);
        mContactsManager = ContactsManager.getInstance(mContext);
        mProgressHelper = new SimpleProgressHelper(fragment);
        initItems();
        mContactsManager.registerListener(mContactListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContactsManager.unregisterListener(mContactListener);
    }

    @Override
    public ListAdapter getContentAdapter() {
        return mAdapter;
    }

    @Override
    public AdapterView.OnItemClickListener getContentItemClickListener() {
        return mAdapter;
    }

    private void initItems() {
        Resources res = mContext.getResources();
        List<UserInfoItem.InfoItem> itemList = new ArrayList<UserInfoItem.InfoItem>();

        itemList.add(new UserInfoItem.SeparatorItem());
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.label_code),
                mContact.getLabelCode(), false));
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.remark),
                mContact.getRemarkName(), new UserInfoItem.NormalItemListener() {
            @Override
            public void onClick(UserInfoItem.NormalInfoItem infoItem) {
                showRemarkEditDialog(infoItem);
            }
        }));
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.nickname),
                mContact.getNickname(), false));
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.gender),
                MiscUtils.getGenderString(res, mContact.getSex()), false));
        itemList.add(new UserInfoItem.SeparatorItem());
        itemList.add(new UserInfoItem.NormalInfoItem(
                res.getString(R.string.region), getRegion(), false));
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.constellation),
                UserContact.getConstellationString(res, mContact.getConstellation()), false));
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.age),
                UserContact.getAgeString(res, mContact.getAge()), false));
        itemList.add(new UserInfoItem.DeleteFriendItem(new UserInfoItem.DeleteFriendListener() {
            @Override
            public void onDelete() {
                showDeleteFriendConfirm();
            }
        }));
        mAdapter.updateItems(itemList);
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
                    Toast.makeText(mContext, R.string.delete_friend_success,
                            Toast.LENGTH_SHORT).show();
                    mFragment.getActivity().finish();
                    break;
                default:
                    Toast.makeText(mContext, R.string.delete_friend_failure,
                            Toast.LENGTH_SHORT).show();
                    break;
            }

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


    private void showRemarkEditDialog(UserInfoItem.NormalInfoItem remarkItem) {
        SimpleEditDialog.UiConfig config = new SimpleEditDialog.UiConfig();
        config.title = getString(R.string.modify_remark);
        config.initText = mContact.getRemarkName();
        config.editHint = getString(R.string.friend_remark_hint);
        config.maxLength = mContext.getResources().getInteger(
                R.integer.friend_remark_max_length);
        config.listener = new RemarkEditListener();
        SimpleEditDialog.newInstance(config).show(getFragmentManager(),
                "RemarkEditDialog");
        mRemarkItem = remarkItem;
    }

    private class RemarkEditListener implements SimpleEditDialog.IListener {

        @Override
        public void onCancel(CharSequence text) {
        }

        @Override
        public void onOK(CharSequence text) {
            modifyRemark(text.toString());
        }
    }

    private void modifyRemark(String newRemark) {
        if (!mInModifyRemark) {
            newRemark = (newRemark == null) ? "" : newRemark;
            mInModifyRemark = true;
            mProgressHelper.show();
            mNewRemark = newRemark;
            mContactsManager.modifyFriendRemark(mContact.getUserId(), newRemark);
        }
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

    private void handleModifyRemarkResult(ModifyResult result) {
        if (!mContact.getUserId().equals(result.mFriendUserId)
                || !mNewRemark.equals(result.mFriendRemark)) {
            return;
        }

        if (mInModifyRemark) {
            if (result.mResult == ConstantCode.CONTACT_OPERATION_SUCCESS) {
                Toast.makeText(mContext, R.string.modify_remark_success,
                        Toast.LENGTH_SHORT).show();
                mContact.setRemarkName(mNewRemark);
                mRemarkItem.setContent(mNewRemark);
                mAdapter.notifyDataSetChanged();
                postEvent(new PageEvent(this, PageEvent.Event.CONTACT_UPDATE, mContact));
            } else {
                Toast.makeText(mContext, R.string.modify_remark_failure,
                        Toast.LENGTH_SHORT).show();
            }
            mInModifyRemark = false;
            mProgressHelper.dismiss();
        }
    }

    private String getString(int resId) {
        return mContext.getString(resId);
    }

    private String getRegion() {
        final String province = mContact.getProvince();
        final String city = mContact.getCity();
        String region = "";

        if (!TextUtils.isEmpty(province)) {
            region += province + "  ";
        }
        if (!TextUtils.isEmpty(city)) {
            region += (city.equals(province)) ? "" : city;
        }

        return region.trim();
    }
}
