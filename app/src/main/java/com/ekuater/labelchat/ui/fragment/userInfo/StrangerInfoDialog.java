package com.ekuater.labelchat.ui.fragment.userInfo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.ui.util.MiscUtils;

/**
 * Created by Administrator on 2015/3/17.
 *
 * @author FanChong
 */
public class StrangerInfoDialog extends DialogFragment {
    private Stranger mStranger;
    private RelativeLayout remark, nickname, gender, labelNumber, region, school, job, constellation, age, height;
    private TextView remarkInfo, nicknameInfo, genderInfo, labelNumberInfo, regionInfo, schoolInfo, jobInfo, constellationInfo, ageInfo, heightInfo;
    private View divider, divider2, divider3, divider4, divider5, divider6, divider7, divider8, divider9;

    public static StrangerInfoDialog newInstance(Stranger stranger) {
        StrangerInfoDialog instance = new StrangerInfoDialog();
        instance.mStranger = stranger;
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_base_info_list, container, false);
        initView(view);

        nicknameInfo.setText(mStranger.getNickname());
        genderInfo.setText(MiscUtils.getGenderString(getResources(), mStranger.getSex()));
        labelNumberInfo.setText(mStranger.getLabelCode());
        if (TextUtils.isEmpty(mStranger.getProvince()) && TextUtils.isEmpty(mStranger.getCity())) {
            region.setVisibility(View.GONE);
            divider5.setVisibility(View.GONE);
        } else {
            regionInfo.setText(getRegion());
        }

        if (!TextUtils.isEmpty(mStranger.getSchool())) {
            schoolInfo.setText(mStranger.getSchool());
        } else {
            school.setVisibility(View.GONE);
            divider6.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(mStranger.getJob())) {
            jobInfo.setText(mStranger.getJob());
        } else {
            job.setVisibility(View.GONE);
            divider7.setVisibility(View.GONE);
        }

        if (mStranger.getConstellation() > 0) {
            constellationInfo.setText(UserContact.getConstellationString(getResources(), mStranger.getConstellation()));
        } else {
            constellation.setVisibility(View.GONE);
            divider8.setVisibility(View.GONE);
        }
        if (mStranger.getAge() > 0) {
            ageInfo.setText(UserContact.getAgeString(getResources(), mStranger.getAge()));
        } else {
            age.setVisibility(View.GONE);
            divider9.setVisibility(View.GONE);
        }
        if (mStranger.getHeight() > 0) {
            heightInfo.setText(UserContact.getHeightString(getResources(), mStranger.getHeight()));
        } else {
            height.setVisibility(View.GONE);
        }
        return view;
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

        remark.setVisibility(View.GONE);
        remarkInfo.setVisibility(View.GONE);
        divider.setVisibility(View.GONE);

    }

    private String getRegion() {
        final String province = mStranger.getProvince();
        final String city = mStranger.getCity();
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
