package com.ekuater.labelchat.ui.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.ui.activity.SelectRegionByListActivity;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.util.L;
import com.ekuater.labelchat.util.TextUtil;

public class SettingUserInfoFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = SettingUserInfoFragment.class.getSimpleName();

    private static final int REQUEST_CODE_SELECT_REGION = 101;

    private TextView mGenderText;
    private TextView mLabelCodeText;
    private TextView mConstellationText;
    private TextView mRegionText;
    private TextView mNicknameText;
    private TextView mAgeText;
    private TextView nSchoolText;
    private TextView mSignatureText;
    private TextView mJobText;
    private TextView mHeightText;
    private ImageView mTipPenView;
    private View mFocusView;
    private int mMaxSignatureCount;

    private String[] mConstellations;
    private int[] mConstellationValues;
    private int[] mConstellationIcons;
    private String[] mGenderArray;
    private int[] mGenderIconArray;
    private PersonalSetting mSetting;

    private int mMaxNicknameLength;
    private int mMaxSchoolLength;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Resources res = getResources();
        mConstellations = res.getStringArray(R.array.constellation_array);
        mGenderArray = new String[]{
                res.getString(R.string.male),
                res.getString(R.string.female),
        };
        mConstellationValues = res.getIntArray(R.array.constellation_value_array);
        mConstellationIcons = getConstellationIcons();
        mGenderIconArray = new int[]{
                R.drawable.icon_male,
                R.drawable.icon_female,
        };
        mMaxNicknameLength = res.getInteger(R.integer.nickname_max_length);
        mMaxSchoolLength = res.getInteger(R.integer.school_max_length);
        mMaxSignatureCount = res.getInteger(R.integer.signature_max_length);
        mSetting = new PersonalSetting(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_personal_info,
                container, false);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView title = (TextView) view.findViewById(R.id.title);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title.setText(R.string.personal_information);

        ScrollView scrollView = (ScrollView) view.findViewById(R.id.scroll_view);
        mGenderText = (TextView) view.findViewById(R.id.gender);
        mLabelCodeText = (TextView) view.findViewById(R.id.label_code);
        mRegionText = (TextView) view.findViewById(R.id.region);
        mConstellationText = (TextView) view.findViewById(R.id.constellation);
        mNicknameText = (TextView) view.findViewById(R.id.nickname);
        mAgeText = (TextView) view.findViewById(R.id.age);
        nSchoolText = (TextView) view.findViewById(R.id.school);
        mSignatureText = (TextView) view.findViewById(R.id.signature);
        mFocusView = view.findViewById(R.id.focus_view);
        mTipPenView = (ImageView) view.findViewById(R.id.edit_tip);
        mJobText = (TextView) view.findViewById(R.id.job);
        mHeightText = (TextView) view.findViewById(R.id.height);

        view.findViewById(R.id.set_signature).setOnClickListener(this);
        view.findViewById(R.id.set_signature).setOnClickListener(this);
        view.findViewById(R.id.set_gender).setOnClickListener(this);
        view.findViewById(R.id.set_region).setOnClickListener(this);
        view.findViewById(R.id.set_constellation).setOnClickListener(this);
        view.findViewById(R.id.set_nickname).setOnClickListener(this);
        view.findViewById(R.id.set_age).setOnClickListener(this);
        view.findViewById(R.id.set_school).setOnClickListener(this);
        view.findViewById(R.id.set_job).setOnClickListener(this);
        view.findViewById(R.id.set_height).setOnClickListener(this);

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mFocusView.requestFocus();
                }
                return false;
            }
        });
        initSettingData();
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSetting.setValue(PersonalSetting.SIGNATURE_KEY,
                mSignatureText.getText().toString());
    }

    @Override
    public void onClick(View v) {
        mFocusView.requestFocus();
        switch (v.getId()) {
            case R.id.set_signature:
                editSignature();
                break;
            case R.id.set_gender:
                selectGender();
                break;
            case R.id.set_constellation:
                selectConstellation();
                break;
            case R.id.set_region:
                selectRegion();
                break;
            case R.id.set_nickname:
                editNickname();
                break;
            case R.id.set_age:
                selectAge();
                break;
            case R.id.set_school:
                editSchool();
                break;
            case R.id.set_job:
                editJob();
                break;
            case R.id.set_height:
                selectHeight();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SELECT_REGION:
                if (resultCode == Activity.RESULT_OK) {
                    final String regionInfo = data.getStringExtra("info");
                    final String[] regions = regionInfo.split("-");
                    mRegionText.setText(getRegionString(regions));
                    mSetting.setValue(PersonalSetting.REGION_KEY, regions);
                }
                break;
            default:
                break;
        }
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void selectRegion() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), SelectRegionByListActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SELECT_REGION);
    }

    private void selectGender() {
        SingleSelectDialog.UiConfig config = new SingleSelectDialog.UiConfig();
        config.title = getString(R.string.gender);
        config.textItems = mGenderArray;
        config.iconItems = mGenderIconArray;
        config.iconInLeft = true;
        config.listener = new SingleSelectDialog.IListener() {
            @Override
            public void onItemSelected(int position, CharSequence sequence) {
                int gender;

                switch (position) {
                    case 0:
                        gender = ConstantCode.USER_SEX_MALE;
                        break;
                    case 1:
                        gender = ConstantCode.USER_SEX_FEMALE;
                        break;
                    default:
                        gender = ConstantCode.USER_SEX_UNKNOWN;
                        break;
                }

                mGenderText.setText(sequence);
                mSetting.setValue(PersonalSetting.GENDER_KEY,
                        gender);
            }
        };
        SingleSelectDialog.newInstance(config).show(getFragmentManager(),
                "SingleSelectDialog");
    }

    private void selectConstellation() {
        SingleSelectDialog.UiConfig config = new SingleSelectDialog.UiConfig();
        config.title = getString(R.string.constellation);
        config.textItems = mConstellations;
        config.iconItems = mConstellationIcons;
        config.iconInLeft = true;
        config.height = getActivity().getResources().getDimensionPixelSize(
                R.dimen.single_select_dialog_max_height);
        config.listener = new SingleSelectDialog.IListener() {
            @Override
            public void onItemSelected(int position, CharSequence sequence) {
                mConstellationText.setText(MiscUtils.getConstellationString(getResources(),
                        mConstellationValues[position]));
                mSetting.setValue(PersonalSetting.CONSTELLATION_KEY,
                        mConstellationValues[position]);
            }
        };
        SingleSelectDialog.newInstance(config).show(getFragmentManager(),
                "SingleSelectDialog");
    }

    private void selectAge() {
        int initAge = (Integer) mSetting.getValue(PersonalSetting.AGE_KEY);
        String initText = (initAge >= 0) ? String.valueOf(initAge) : null;
        SimpleEditDialog.UiConfig config = new SimpleEditDialog.UiConfig();
        config.title = getString(R.string.age);
        config.initText = initText;
        config.maxLength = 3;
        config.inputType = InputType.TYPE_CLASS_NUMBER;
        config.gravityCenter = true;
        config.showLeftCountHint = false;
        config.listener = new SimpleEditDialog.IListener() {
            @Override
            public void onCancel(CharSequence text) {
            }

            @Override
            public void onOK(CharSequence sequence) {
                final String content = sequence.toString();
                if (!TextUtils.isEmpty(content)) {
                    try {
                        final int age = Integer.valueOf(content);
                        mAgeText.setText(MiscUtils.getAgeString(getResources(), age));
                        mSetting.setValue(PersonalSetting.AGE_KEY, age);
                    } catch (Exception e) {
                        L.w(TAG, e);
                    }
                } else {
                    mAgeText.setText("");
                    mSetting.setValue(PersonalSetting.AGE_KEY, -1);
                }
            }
        };
        SimpleEditDialog.newInstance(config).show(getFragmentManager(),
                "editSettingItem");
    }

    private void selectHeight() {
        int initHeight = (Integer) mSetting.getValue(PersonalSetting.HEIGHT_KEY);
        String initText = (initHeight >= 0) ? String.valueOf(initHeight) : null;
        SimpleEditDialog.UiConfig config = new SimpleEditDialog.UiConfig();
        config.title = getString(R.string.height);
        config.initText = initText;
        config.maxLength = 3;
        config.inputType = InputType.TYPE_CLASS_NUMBER;
        config.gravityCenter = true;
        config.showLeftCountHint = false;
        config.listener = new SimpleEditDialog.IListener() {
            @Override
            public void onCancel(CharSequence text) {
            }

            @Override
            public void onOK(CharSequence sequence) {
                final String content = sequence.toString();
                if (!TextUtils.isEmpty(content)) {
                    try {
                        final int height = Integer.valueOf(content);
                        mHeightText.setText(MiscUtils.getHeightString(getResources(), height));
                        mSetting.setValue(PersonalSetting.HEIGHT_KEY, height);
                    } catch (Exception e) {
                        L.w(TAG, e);
                    }
                } else {
                    mHeightText.setText("");
                    mSetting.setValue(PersonalSetting.HEIGHT_KEY, -1);
                }
            }
        };
        SimpleEditDialog.newInstance(config).show(getFragmentManager(),
                "editSettingItem");
    }

    private void editSchool() {
        SimpleEditDialog.UiConfig config = new SimpleEditDialog.UiConfig();
        config.title = getString(R.string.school);
        config.initText = nSchoolText.getText().toString();
        config.maxLength = mMaxSchoolLength;
        config.inputType = InputType.TYPE_CLASS_TEXT;
        config.gravityCenter = true;
        config.listener = new SimpleEditDialog.IListener() {
            @Override
            public void onCancel(CharSequence text) {
            }

            @Override
            public void onOK(CharSequence sequence) {
                String content = sequence.toString();
                nSchoolText.setText(sequence);
                mSetting.setValue(PersonalSetting.SCHOOL_KEY, content);
            }
        };
        SimpleEditDialog.newInstance(config).show(getFragmentManager(),
                "editSettingItem");
    }

    private void editNickname() {
        SimpleEditDialog.UiConfig config = new SimpleEditDialog.UiConfig();
        config.title = getString(R.string.nickname);
        config.initText = mNicknameText.getText().toString();
        config.maxLength = mMaxNicknameLength;
        config.inputType = InputType.TYPE_CLASS_TEXT;
        config.gravityCenter = true;
        config.cancelable = false;
        config.isNicknameInput = true;
        config.listener = new SimpleEditDialog.IListener() {
            @Override
            public void onCancel(CharSequence text) {
                if (TextUtils.isEmpty(mNicknameText.getText().toString())) {
                    ShowToast.makeText(getActivity(), R.drawable.emoji_smile, getString(R.string.nickname_empty)).show();
                    editNickname();
                }
            }

            @Override
            public void onOK(CharSequence sequence) {
                final String content = sequence.toString();
                if (!TextUtils.isEmpty(content)) {
                    mNicknameText.setText(sequence);
                    mSetting.setValue(PersonalSetting.NICKNAME_KEY, content);
                } else {
                    ShowToast.makeText(getActivity(), R.drawable.emoji_smile, getString(R.string.nickname_empty)).show();
                    editNickname();
                }
            }
        };
        SimpleEditDialog.newInstance(config).show(getFragmentManager(),
                "editSettingItem");
    }

    private void editSignature() {
        SimpleEditDialog.UiConfig config = new SimpleEditDialog.UiConfig();
        config.title = getString(R.string.signature);
        config.initText = mSignatureText.getText().toString();
        config.maxLength = mMaxSignatureCount;
        config.inputType = InputType.TYPE_CLASS_TEXT;
        config.cancelable = false;
        config.isNicknameInput = true;
        config.isChangerEditHeight = true;
        config.listener = new SimpleEditDialog.IListener() {
            @Override
            public void onCancel(CharSequence text) {
            }

            @Override
            public void onOK(CharSequence text) {
                final String content = text.toString();
                if (!TextUtil.isEmpty(content)) {
                    mSignatureText.setText(content);
                    mSetting.setValue(PersonalSetting.SIGNATURE_KEY, content);
                    mTipPenView.setVisibility(View.GONE);
                } else {
                    mSignatureText.setText("");
                    mTipPenView.setVisibility(View.VISIBLE);
                    mSetting.setValue(PersonalSetting.SIGNATURE_KEY, "");
                }
            }
        };
        SimpleEditDialog.newInstance(config).show(getFragmentManager(), "editSettingItem");
    }

    private void editJob() {
        SimpleEditDialog.UiConfig config = new SimpleEditDialog.UiConfig();
        config.title = getString(R.string.job);
        config.initText = mJobText.getText().toString();
        config.maxLength = mMaxNicknameLength;
        config.inputType = InputType.TYPE_CLASS_TEXT;
        config.cancelable = false;
        config.gravityCenter = true;
        config.isNicknameInput = true;
        config.listener = new SimpleEditDialog.IListener() {
            @Override
            public void onCancel(CharSequence text) {
            }

            @Override
            public void onOK(CharSequence text) {
                final String content = text.toString();
                if (!TextUtil.isEmpty(content)) {
                    mJobText.setText(content);
                    mSetting.setValue(PersonalSetting.JOB_KEY, content);
                } else {
                    mJobText.setText("");
                    mSetting.setValue(PersonalSetting.JOB_KEY, "");
                }
            }
        };
        SimpleEditDialog.newInstance(config).show(getFragmentManager(), "editSettingItem");
    }

    private void initSettingData() {
        final Activity activity = getActivity();
        final Resources res = activity.getResources();
        final String nickname = getValueString(PersonalSetting.NICKNAME_KEY);

        mSignatureText.setText(getValueString(PersonalSetting.SIGNATURE_KEY));
        mGenderText.setText(MiscUtils.getGenderString(res,
                (Integer) mSetting.getValue(PersonalSetting.GENDER_KEY)));
        mConstellationText.setText(MiscUtils.getConstellationString(res,
                (Integer) mSetting.getValue(PersonalSetting.CONSTELLATION_KEY)));
        mLabelCodeText.setText(getValueString(PersonalSetting.LABEL_CODE_KEY));
        mRegionText.setText(getRegionString((String[]) mSetting.getValue(
                PersonalSetting.REGION_KEY)));
        mNicknameText.setText(nickname);
        mAgeText.setText(MiscUtils.getAgeString(res,
                (Integer) mSetting.getValue(PersonalSetting.AGE_KEY)));
        nSchoolText.setText(getValueString(PersonalSetting.SCHOOL_KEY));
        mHeightText.setText(MiscUtils.getHeightString(res,
                (Integer)mSetting.getValue(PersonalSetting.HEIGHT_KEY)));
        mJobText.setText(getValueString(PersonalSetting.JOB_KEY));

        // If nickname is empty, set it first.
        if (TextUtils.isEmpty(nickname)) {
            editNickname();
        }
        mTipPenView.setVisibility(TextUtil.isEmpty(mSignatureText.getText().toString())
                ? View.VISIBLE : View.GONE);
    }

    private String getRegionString(String[] regions) {
        String region;

        if (regions.length >= 2) {
            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(regions[0])
                    && !TextUtils.isEmpty(regions[1])) {
                sb.append(regions[0]);
                if (!regions[0].equals(regions[1])) {
                    sb.append("  ");
                    sb.append(regions[1]);
                }
            } else if (!TextUtils.isEmpty(regions[0])) {
                sb.append(regions[0]);
            } else if (!TextUtils.isEmpty(regions[1])) {
                sb.append(regions[1]);
            }

            region = sb.toString();
        } else {
            region = null;
        }

        return region;
    }

    private String getValueString(String key) {
        final Object object = mSetting.getValue(key);
        return (object != null) ? object.toString() : "";
    }

    private int[] getConstellationIcons() {
        final TypedArray ar = getResources().obtainTypedArray(
                R.array.constellation_icon_array);
        final int length = ar.length();
        final int[] array = new int[length];

        for (int i = 0; i < length; ++i) {
            array[i] = ar.getResourceId(i, 0);
        }
        ar.recycle();

        return array;
    }
}
