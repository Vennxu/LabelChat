package com.ekuater.labelchat.ui.fragment;

import android.content.Context;

import com.ekuater.labelchat.delegate.SettingManager;
import com.ekuater.labelchat.settings.SettingHelper;

/**
 * @author LinYong
 */
public class PersonalSetting {

    public static final String GENDER_KEY = "sex";
    public static final String CONSTELLATION_KEY = "constellation";
    public static final String LABEL_CODE_KEY = "set_user_accountno";
    public static final String REGION_KEY = "address";
    public static final String NICKNAME_KEY = "nickName";
    public static final String AGE_KEY = "age";
    public static final String SCHOOL_KEY = "school";
    public static final String SIGNATURE_KEY = "signature";
    public static final String APPEARANCE_FACE_KEY = "face";
    public static final String HEIGHT_KEY = "height";
    public static final String JOB_KEY = "job";

    private final SettingHelper mHelper;
    private final SettingManager mManager;

    public PersonalSetting(Context context) {
        mHelper = SettingHelper.getInstance(context);
        mManager = SettingManager.getInstance(context);
    }

    public Object getValue(String key) {
        Object object = null;

        if (GENDER_KEY.equals(key)) {
            object = mHelper.getAccountSex();
        } else if (CONSTELLATION_KEY.equals(key)) {
            object = mHelper.getAccountConstellation();
        } else if (LABEL_CODE_KEY.equals(key)) {
            object = mHelper.getAccountLabelCode();
        } else if (REGION_KEY.equals(key)) {
            object = new String[]{
                    mHelper.getAccountProvince(),
                    mHelper.getAccountCity()
            };
        } else if (NICKNAME_KEY.equals(key)) {
            object = mHelper.getAccountNickname();
        } else if (AGE_KEY.equals(key)) {
            object = mHelper.getAccountAge();
        } else if (SCHOOL_KEY.equals(key)) {
            object = mHelper.getAccountSchool();
        } else if (SIGNATURE_KEY.equals(key)) {
            object = mHelper.getAccountSignature();
        } else if (APPEARANCE_FACE_KEY.equals(key)) {
            object = mHelper.getAccountAppearanceFace();
        } else if (HEIGHT_KEY.equals(key)) {
            object = mHelper.getAccountHeight();
        } else if (JOB_KEY.equals(key)) {
            object = mHelper.getAccountJob();
        }

        return object;
    }

    public void setValue(String key, Object value) {
        String valueString = null;

        if (GENDER_KEY.equals(key)) {
            if (value instanceof Integer) {
                mHelper.setAccountSex((Integer) value);
            }
        } else if (CONSTELLATION_KEY.equals(key)) {
            if (value instanceof Integer) {
                mHelper.setAccountConstellation((Integer) value);
            }
        } else if (REGION_KEY.equals(key)) {
            if (value instanceof String[]) {
                final String[] regions = (String[]) value;
                if (regions.length >= 2) {
                    mHelper.setAccountProvince(regions[0]);
                    mHelper.setAccountCity(regions[1]);
                    valueString = regions[0] + "-" + regions[1];
                }
            }
        } else if (NICKNAME_KEY.equals(key)) {
            if (value instanceof String) {
                mHelper.setAccountNickname((String) value);
            }
        } else if (AGE_KEY.equals(key)) {
            if (value instanceof Integer) {
                mHelper.setAccountAge((Integer) value);
            }
        } else if (SCHOOL_KEY.equals(key)) {
            if (value instanceof String) {
                mHelper.setAccountSchool((String) value);
            }
        } else if (SIGNATURE_KEY.equals(key)) {
            if (value instanceof String) {
                mHelper.setAccountSignature((String) value);
            }
        } else if (APPEARANCE_FACE_KEY.equals(key)) {
            if (value instanceof String) {
                mHelper.setAccountAppearanceFace((String) value);
            }
        } else if (HEIGHT_KEY.equals(key)) {
            if (value instanceof Integer) {
                mHelper.setAccountHeight((Integer) value);
            }
        } else if (JOB_KEY.equals(key)) {
            mHelper.setAccountJob((String) value);
        } else {
            return;
        }

        if (valueString == null) {
            valueString = value.toString();
        }

        mManager.updateUserInfoSet(key, valueString);
    }
}
