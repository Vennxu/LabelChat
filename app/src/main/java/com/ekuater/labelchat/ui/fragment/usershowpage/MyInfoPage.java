package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.SelectRegionByListActivity;
import com.ekuater.labelchat.ui.fragment.PersonalSetting;
import com.ekuater.labelchat.ui.fragment.SimpleEditDialog;
import com.ekuater.labelchat.ui.fragment.SingleSelectDialog;
import com.ekuater.labelchat.ui.fragment.settings.SettingsFragment;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.util.L;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/2/3.
 *
 * @author LinYong
 */
public class MyInfoPage extends BasePage {

    private static final String TAG = MyInfoPage.class.getSimpleName();
    private static final int REQUEST_CODE_SELECT_REGION = 101;

    private UserInfoAdapter mAdapter;
    private PersonalSetting mSetting;

    private String[] mConstellations;
    private int[] mConstellationValues;
    private int[] mConstellationIcons;
    private String[] mGenderArray;
    private int[] mGenderIconArray;
    private int mMaxNicknameLength;

    private UserInfoItem.NormalInfoItem mRegionItem;

    public MyInfoPage(Fragment fragment) {
        super(fragment);
        Resources res = mContext.getResources();
        mAdapter = new UserInfoAdapter(mContext);
        mSetting = new PersonalSetting(mContext);
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
        initItems();
    }

    @Override
    public ListAdapter getContentAdapter() {
        return mAdapter;
    }

    @Override
    public AdapterView.OnItemClickListener getContentItemClickListener() {
        return mAdapter;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SELECT_REGION:
                if (resultCode == Activity.RESULT_OK) {
                    final String regionInfo = data.getStringExtra("info");
                    final String[] regions = regionInfo.split("-");
                    if (mRegionItem != null) {
                        mRegionItem.setContent(getRegionString(regions));
                    }
                    mSetting.setValue(PersonalSetting.REGION_KEY, regions);
                    mAdapter.notifyDataSetChanged();
                }
                mRegionItem = null;
                break;
            default:
                break;
        }
    }

    private void initItems() {
        Resources res = mContext.getResources();
        List<UserInfoItem.InfoItem> itemList = new ArrayList<UserInfoItem.InfoItem>();
        itemList.add(new UserInfoItem.SeparatorItem());
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.label_code),
                getValueString(PersonalSetting.LABEL_CODE_KEY), false));
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.nickname),
                getValueString(PersonalSetting.NICKNAME_KEY),
                new UserInfoItem.NormalItemListener() {
                    @Override
                    public void onClick(UserInfoItem.NormalInfoItem infoItem) {
                        editNickname(infoItem);
                    }
                }));
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.gender),
                MiscUtils.getGenderString(res, (Integer) mSetting
                        .getValue(PersonalSetting.GENDER_KEY)),
                new UserInfoItem.NormalItemListener() {
                    @Override
                    public void onClick(UserInfoItem.NormalInfoItem infoItem) {
                        selectGender(infoItem);
                    }
                }));
        itemList.add(new UserInfoItem.SeparatorItem());
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.region),
                getRegionString((String[]) mSetting.getValue(
                        PersonalSetting.REGION_KEY)),
                new UserInfoItem.NormalItemListener() {
                    @Override
                    public void onClick(UserInfoItem.NormalInfoItem infoItem) {
                        selectRegion(infoItem);
                    }
                }));
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.constellation),
                MiscUtils.getConstellationString(res, (Integer) mSetting
                        .getValue(PersonalSetting.CONSTELLATION_KEY)),
                new UserInfoItem.NormalItemListener() {
                    @Override
                    public void onClick(UserInfoItem.NormalInfoItem infoItem) {
                        selectConstellation(infoItem);
                    }
                }));
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.age),
                MiscUtils.getAgeString(res, (Integer) mSetting
                        .getValue(PersonalSetting.AGE_KEY)),
                new UserInfoItem.NormalItemListener() {
                    @Override
                    public void onClick(UserInfoItem.NormalInfoItem infoItem) {
                        selectAge(infoItem);
                    }
                }));
        itemList.add(new UserInfoItem.SeparatorItem());
        itemList.add(new UserInfoItem.NormalInfoItem(
                res.getString(R.string.action_settings),
                null,
                new UserInfoItem.NormalItemListener() {
                    @Override
                    public void onClick(UserInfoItem.NormalInfoItem infoItem) {
                        UILauncher.launchSettingsUI(mContext);
                    }
                }));
        mAdapter.updateItems(itemList);
    }

    private void editNickname(UserInfoItem.NormalInfoItem nicknameItem) {
        SimpleEditDialog.UiConfig config = new SimpleEditDialog.UiConfig();
        config.title = getString(R.string.nickname);
        config.initText = nicknameItem.getContent();
        config.maxLength = mMaxNicknameLength;
        config.inputType = InputType.TYPE_CLASS_TEXT;
        config.cancelable = false;
        config.isNicknameInput = true;
        config.listener = new NicknameEditListener(nicknameItem);
        SimpleEditDialog.newInstance(config).show(getFragmentManager(),
                "editSettingItem");
    }

    private class NicknameEditListener implements SimpleEditDialog.IListener {

        private UserInfoItem.NormalInfoItem nicknameItem;

        public NicknameEditListener(UserInfoItem.NormalInfoItem nicknameItem) {
            this.nicknameItem = nicknameItem;
        }

        @Override
        public void onCancel(CharSequence text) {
            if (TextUtils.isEmpty(nicknameItem.getContent())) {
                Toast.makeText(mContext, R.string.nickname_empty,
                        Toast.LENGTH_SHORT).show();
                editNickname(nicknameItem);
            }
        }

        @Override
        public void onOK(CharSequence sequence) {
            final String content = sequence.toString();
            if (!TextUtils.isEmpty(content)) {
                nicknameItem.setContent(sequence.toString());
                mSetting.setValue(PersonalSetting.NICKNAME_KEY, content);
                mAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(mContext, R.string.nickname_empty,
                        Toast.LENGTH_SHORT).show();
                editNickname(nicknameItem);
            }
        }
    }

    private void selectGender(UserInfoItem.NormalInfoItem genderItem) {
        SingleSelectDialog.UiConfig config = new SingleSelectDialog.UiConfig();
        config.title = getString(R.string.gender);
        config.textItems = mGenderArray;
        config.iconItems = mGenderIconArray;
        config.iconInLeft = true;
        config.listener = new GenderSelectListener(genderItem);
        SingleSelectDialog.newInstance(config).show(getFragmentManager(),
                "SingleSelectDialog");
    }

    private class GenderSelectListener implements SingleSelectDialog.IListener {

        private UserInfoItem.NormalInfoItem genderItem;

        public GenderSelectListener(UserInfoItem.NormalInfoItem genderItem) {
            this.genderItem = genderItem;
        }

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

            genderItem.setContent(sequence.toString());
            mSetting.setValue(PersonalSetting.GENDER_KEY,
                    gender);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void selectRegion(UserInfoItem.NormalInfoItem regionItem) {
        mRegionItem = regionItem;
        Intent intent = new Intent();
        intent.setClass(mContext, SelectRegionByListActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SELECT_REGION);
    }

    private void selectConstellation(UserInfoItem.NormalInfoItem constellationItem) {
        SingleSelectDialog.UiConfig config = new SingleSelectDialog.UiConfig();
        config.title = getString(R.string.constellation);
        config.textItems = mConstellations;
        config.iconItems = mConstellationIcons;
        config.iconInLeft = true;
        config.height = mContext.getResources().getDimensionPixelSize(
                R.dimen.single_select_dialog_max_height);
        config.listener = new ConstellationSelectListener(constellationItem);
        SingleSelectDialog.newInstance(config).show(getFragmentManager(),
                "SingleSelectDialog");
    }

    private class ConstellationSelectListener implements SingleSelectDialog.IListener {

        private UserInfoItem.NormalInfoItem constellationItem;

        public ConstellationSelectListener(UserInfoItem.NormalInfoItem constellationItem) {
            this.constellationItem = constellationItem;
        }

        @Override
        public void onItemSelected(int position, CharSequence sequence) {
            constellationItem.setContent(MiscUtils.getConstellationString(
                    mContext.getResources(), mConstellationValues[position]));
            mSetting.setValue(PersonalSetting.CONSTELLATION_KEY,
                    mConstellationValues[position]);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void selectAge(UserInfoItem.NormalInfoItem ageItem) {
        int initAge = (Integer) mSetting.getValue(PersonalSetting.AGE_KEY);
        String initText = (initAge >= 0) ? String.valueOf(initAge) : null;
        SimpleEditDialog.UiConfig config = new SimpleEditDialog.UiConfig();
        config.title = getString(R.string.age);
        config.initText = initText;
        config.maxLength = 3;
        config.inputType = InputType.TYPE_CLASS_NUMBER;
        config.gravityCenter = true;
        config.showLeftCountHint = false;
        config.listener = new AgeEditListener(ageItem);
        SimpleEditDialog.newInstance(config).show(getFragmentManager(),
                "editSettingItem");
    }

    private class AgeEditListener implements SimpleEditDialog.IListener {

        private UserInfoItem.NormalInfoItem ageItem;

        public AgeEditListener(UserInfoItem.NormalInfoItem ageItem) {
            this.ageItem = ageItem;
        }

        @Override
        public void onCancel(CharSequence text) {
        }

        @Override
        public void onOK(CharSequence sequence) {
            final String content = sequence.toString();
            if (!TextUtils.isEmpty(content)) {
                try {
                    final int age = Integer.valueOf(content);
                    ageItem.setContent(MiscUtils.getAgeString(mContext.getResources(), age));
                    mSetting.setValue(PersonalSetting.AGE_KEY, age);
                } catch (Exception e) {
                    L.w(TAG, e);
                }
            } else {
                ageItem.setContent("");
                mSetting.setValue(PersonalSetting.AGE_KEY, -1);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private String getString(int resId) {
        return mContext.getString(resId);
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
        final TypedArray ar = mContext.getResources().obtainTypedArray(
                R.array.constellation_icon_array);
        final int length = ar.length();
        final int[] array = new int[length];

        for (int i = 0; i < length; ++i) {
            array[i] = ar.getResourceId(i, 0);
        }

        return array;
    }
}
