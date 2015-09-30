package com.ekuater.labelchat.ui.fragment.userInfo;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.ekuater.labelchat.datastruct.InterestType;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserInterest;
import com.ekuater.labelchat.datastruct.UserTag;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/3/12.
 *
 * @author FanChong
 */
public class PersonalInfoAdapter extends BaseAdapter {

    private Activity mContext;
    private LayoutInflater inflater;
    private List<UserInfoItem> mContactInfoItemList;
    private UserContact mUserContact;

    public PersonalInfoAdapter(Activity context, UserContact userContact) {
        mContext = context;
        inflater = LayoutInflater.from(context);
        mUserContact = userContact;
    }

    public synchronized void getContactInfo(
            UserContact contact,
            PersonalInfo.InteractListener interactListener) {
        mUserContact = contact;
        mContactInfoItemList = new ArrayList<>();

        if (mUserContact != null) {
            final int photoCount = getPhotoCount(mUserContact);
            final int tagCount = getTagCount(mUserContact);
            final int interestCount = getInterestCount(mUserContact);
            final boolean dynamicLastItem = photoCount <= 0
                    && tagCount <= 0 && interestCount <= 0;

            mContactInfoItemList.add(new PersonalInfo.BasicInfoItem(
                    mContext, mUserContact));
            mContactInfoItemList.add(new PersonalInfo.DynamicItem(
                    mContext, mUserContact, dynamicLastItem));
            if (photoCount > 0) {
                mContactInfoItemList.add(new PersonalInfo.AlbumItem(mContext, mUserContact));
            }
            if (tagCount > 0) {
                mContactInfoItemList.add(new PersonalInfo.PersonLabelItem(mContext,
                        mUserContact, interactListener));
            }
            if (interestCount > 0) {
                mContactInfoItemList.add(new PersonalInfo.FavoriteInterestItem(mContext,
                        mUserContact, interactListener));
            }
        }
        notifyDataSetChanged();
    }

    private int getPhotoCount(@NonNull UserContact contact) {
        return contact.getMyPhotoTotal();
    }

    private int getTagCount(@NonNull UserContact contact) {
        UserTag[] tags = contact.getUserTags();
        return tags != null ? tags.length : 0;
    }

    private int getInterestCount(@NonNull UserContact contact) {
        InterestType[] types = contact.getInterestTypes();
        int count = 0;

        if (types != null) {
            for (InterestType type : types) {
                UserInterest[] interests = (type != null)
                        ? type.getUserInterests() : null;
                count += (interests != null) ? interests.length : 0;
            }
        }
        return count;
    }

    @Override
    public int getCount() {
        return mContactInfoItemList == null ? 0 : mContactInfoItemList.size();
    }

    @Override
    public UserInfoItem getItem(int position) {
        return mContactInfoItemList == null ? null : mContactInfoItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return AdapterView.ITEM_VIEW_TYPE_IGNORE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserInfoItem item = getItem(position);
        if (convertView == null) {
            convertView = item.newView(inflater, parent);
        }
        item.bindView(convertView);
        return convertView;
    }
}
