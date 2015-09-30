package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.ui.util.MiscUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/2/4.
 *
 * @author LinYong
 */
public class StrangerInfoPage extends BasePage {

    private Stranger mStranger;
    private UserInfoAdapter mAdapter;

    public StrangerInfoPage(Fragment fragment, Stranger stranger) {
        super(fragment);
        mStranger = stranger;
        mAdapter = new UserInfoAdapter(mContext);
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

    private boolean isStrangerFriend(Stranger stranger) {
        return ContactsManager.getInstance(mContext)
                .getUserContactByUserId(stranger.getUserId()) != null;
    }

    private void initItems() {
        Resources res = mContext.getResources();
        List<UserInfoItem.InfoItem> itemList = new ArrayList<UserInfoItem.InfoItem>();
        boolean isFriend = isStrangerFriend(mStranger);

        itemList.add(new UserInfoItem.SeparatorItem());
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.label_code),
                mStranger.getLabelCode(), false));
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.nickname),
                mStranger.getNickname(), false));
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.gender),
                MiscUtils.getGenderString(res, mStranger.getSex()), false));
        itemList.add(new UserInfoItem.SeparatorItem());
        itemList.add(new UserInfoItem.NormalInfoItem(
                res.getString(R.string.region), getRegion(), false));
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.constellation),
                UserContact.getConstellationString(res, mStranger.getConstellation()), false));
        itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.age),
                UserContact.getAgeString(res, mStranger.getAge()), false));
        if (!isFriend) {
            LocationInfo myLocation = AccountManager.getInstance(mContext).getLocation();
            LocationInfo location = mStranger.getLocation();

            if (myLocation != null && location != null) {
                itemList.add(new UserInfoItem.NormalInfoItem(res.getString(R.string.distance),
                        MiscUtils.getDistanceString(mContext, myLocation.getDistance(location)),
                        false));
            }
        }
        mAdapter.updateItems(itemList);
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
