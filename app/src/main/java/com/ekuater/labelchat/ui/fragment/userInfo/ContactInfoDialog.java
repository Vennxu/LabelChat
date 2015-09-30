package com.ekuater.labelchat.ui.fragment.userInfo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.ui.fragment.SimpleEditDialog;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;

/**
 * Created by Administrator on 2015/3/17.
 *
 * @author FanChong
 */
public class ContactInfoDialog extends DialogFragment {

    private static final int MSG_MODIFY_REMARK_RESULT = 101;
    private ContactsManager mContactsManager;
    private UserContact mContact;
    private RelativeLayout remark, nickname, gender, labelNumber, region, school, job, constellation, age, height;
    private TextView remarkInfo, nicknameInfo, genderInfo, labelNumberInfo, regionInfo, schoolInfo, jobInfo, constellationInfo, ageInfo, heightInfo;
    private View divider, divider2, divider3, divider4, divider5, divider6, divider7, divider8, divider9;
    private View hintView;
    private SimpleProgressHelper mProgressHelper;
    private boolean mInModifyRemark = false;
    private String mNewRemark = "";


    public static ContactInfoDialog newInstance(UserContact contact) {
        ContactInfoDialog instance = new ContactInfoDialog();
        instance.mContact = contact;
        return instance;
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

    private final ContactsManager.IListener mContactListener = new ContactsManager.AbsListener() {
        @Override
        public void onModifyFriendRemarkResult(int result, String friendUserId, String friendRemark) {
            onModifyRemarkResult(result, friendUserId, friendRemark);
        }
    };
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MODIFY_REMARK_RESULT:
                    handleModifyRemarkResult((ModifyResult) msg.obj);
                    break;
            }
        }
    };

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
                ShowToast.makeText(getActivity(), R.drawable.emoji_smile, getActivity().
                        getResources().getString(R.string.modify_remark_success)).show();
                mContact.setRemarkName(mNewRemark);
                remarkInfo.setText(mNewRemark);
            } else {
                ShowToast.makeText(getActivity(), R.drawable.emoji_cry, getActivity().
                        getResources().getString(R.string.modify_remark_failure)).show();
            }
            mInModifyRemark = false;
            mProgressHelper.dismiss();
        }
    }

    private void modifyRemark(String newRemark) {
        if (!mInModifyRemark) {
            newRemark = (newRemark == null) ? "" : newRemark;
            mInModifyRemark = true;
            mProgressHelper.show();
            mNewRemark = newRemark;
            mContactsManager.modifyFriendRemark(mContact.getUserId(), newRemark);
            hintView.setVisibility(View.GONE);
        }
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
        mContactsManager = ContactsManager.getInstance(getActivity());
        mProgressHelper = new SimpleProgressHelper(getActivity());
        mContactsManager.registerListener(mContactListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_base_info_list, container, false);
        initView(view);

        nicknameInfo.setText(mContact.getNickname());
        if (!TextUtils.isEmpty(mContact.getRemarkName())) {
            remarkInfo.setText(mContact.getRemarkName());
        } else {

            hintView.setVisibility(View.VISIBLE);
        }
        genderInfo.setText(MiscUtils.getGenderString(getResources(), mContact.getSex()));
        labelNumberInfo.setText(mContact.getLabelCode());
        if (TextUtils.isEmpty(mContact.getProvince()) && TextUtils.isEmpty(mContact.getCity())) {
            region.setVisibility(View.GONE);
            divider5.setVisibility(View.GONE);
        } else {
            regionInfo.setText(getRegion());
        }
        if (!TextUtils.isEmpty(mContact.getSchool())) {
            schoolInfo.setText(mContact.getSchool());
        } else {
            school.setVisibility(View.GONE);
            divider6.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(mContact.getJob())) {
            jobInfo.setText(mContact.getJob());
        } else {
            job.setVisibility(View.GONE);
            divider7.setVisibility(View.GONE);
        }

        if (mContact.getConstellation() > 0) {
            constellationInfo.setText(UserContact.getConstellationString(getResources(), mContact.getConstellation()));
        } else {
            constellation.setVisibility(View.GONE);
            divider8.setVisibility(View.GONE);
        }
        if (mContact.getAge() > 0) {
            ageInfo.setText(UserContact.getAgeString(getResources(), mContact.getAge()));
        } else {
            age.setVisibility(View.GONE);
            divider9.setVisibility(View.GONE);
        }
        if (mContact.getHeight() > 0) {
            heightInfo.setText(UserContact.getHeightString(getResources(), mContact.getHeight()));
        } else {
            height.setVisibility(View.GONE);
        }

        remark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleEditDialog.UiConfig config = new SimpleEditDialog.UiConfig();
                config.title = getString(R.string.modify_remark);
                config.initText = mContact.getRemarkName();
                config.editHint = getString(R.string.friend_remark_hint);
                config.maxLength = getActivity().getResources().getInteger(
                        R.integer.friend_remark_max_length);
                config.listener = new RemarkEditListener();
                SimpleEditDialog.newInstance(config).show(getFragmentManager(), "RemarkEditDialog");
            }
        });
        return view;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContactsManager.unregisterListener(mContactListener);
    }

    private void initView(View view) {
        remark = (RelativeLayout) view.findViewById(R.id.remark);
        remarkInfo = (TextView) view.findViewById(R.id.remark_info);
        divider = view.findViewById(R.id.divider);

        nickname = (RelativeLayout) view.findViewById(R.id.nickname);
        nicknameInfo = (TextView) view.findViewById(R.id.nickname_info);
        divider2 = view.findViewById(R.id.divider2);

        gender = (RelativeLayout) view.findViewById(R.id.gender);
        genderInfo = (TextView) view.findViewById(R.id.gender_info);
        divider3 = view.findViewById(R.id.divider3);

        labelNumber = (RelativeLayout) view.findViewById(R.id.label_number);
        labelNumberInfo = (TextView) view.findViewById(R.id.label_number_info);
        divider4 = view.findViewById(R.id.divider4);

        region = (RelativeLayout) view.findViewById(R.id.region);
        regionInfo = (TextView) view.findViewById(R.id.region_info);
        divider5 = view.findViewById(R.id.divider5);

        school = (RelativeLayout) view.findViewById(R.id.school);
        schoolInfo = (TextView) view.findViewById(R.id.school_info);
        divider6 = view.findViewById(R.id.divider6);

        job = (RelativeLayout) view.findViewById(R.id.job);
        jobInfo = (TextView) view.findViewById(R.id.job_info);
        divider7 = view.findViewById(R.id.divider7);


        constellation = (RelativeLayout) view.findViewById(R.id.constellation);
        constellationInfo = (TextView) view.findViewById(R.id.constellation_info);
        divider8 = view.findViewById(R.id.divider8);

        age = (RelativeLayout) view.findViewById(R.id.age);
        ageInfo = (TextView) view.findViewById(R.id.age_info);
        divider9 = view.findViewById(R.id.divider9);

        height = (RelativeLayout) view.findViewById(R.id.height);
        heightInfo = (TextView) view.findViewById(R.id.height_info);

        hintView = view.findViewById(R.id.edit_hint);

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
