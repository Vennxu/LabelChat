
package com.ekuater.labelchat.ui.fragment.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;

import java.util.ArrayList;
import java.util.List;

public class GroupInformationGridViewAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private AvatarManager mAvatarManager;
    private LocationInfo mLocationIngfo;
    private List<Stranger> mStrangerList;
    private Stranger[] mStrangers;
    private ContactsManager mContactsManager;
    private final int mFriendColor;
    private final int mStrangerColor;


    public GroupInformationGridViewAdapter(Context context, List<Stranger> strangers) {
        this.mContext = context;
        this.mStrangerList = strangers;
        this.mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mAvatarManager = AvatarManager.getInstance(context);
        mContactsManager = ContactsManager.getInstance(context);
        mLocationIngfo = AccountManager.getInstance(context).getLocation();
        Resources res = mContext.getResources();
        mFriendColor = res.getColor(R.color.friend_name_color);
        mStrangerColor = res.getColor(R.color.stranger_name_color);

    }


    @Override
    public int getCount() {
        return (mStrangerList == null) ? 0 : mStrangerList.size();
    }

    @Override
    public Stranger getItem(int position) {

        return (mStrangerList==null)?null:mStrangerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CircleImageView circleImageView = null;
        Stranger stranger = getItem(position);
        LocationInfo strangerLocationInfo = stranger.getLocation();
        ShowGroupList showGroupList;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.fragment_stranger_multi_friend_item, parent,
                    false);
            showGroupList = new ShowGroupList();
            showGroupList.groupFriendImage = (ImageView) convertView
                    .findViewById(R.id.stranger_friend_image);
            showGroupList.groupFriendDistance = (TextView) convertView
                    .findViewById(R.id.stranger_friend_distance);
            showGroupList.groupFriendNickname = (TextView) convertView
                    .findViewById(R.id.stranger_friend_nickname);

            convertView.setTag(showGroupList);
        } else {
            showGroupList = (ShowGroupList) convertView.getTag();
        }
        if (showGroupList.groupFriendImage instanceof CircleImageView) {
            circleImageView = (CircleImageView) showGroupList.groupFriendImage;
        }
        int color;
        switch (stranger.getSex()) {
            case ConstantCode.USER_SEX_FEMALE:
                color = mContext.getResources().getColor(R.color.pink);
                break;
            default:
                color = mContext.getResources().getColor(R.color.blue);
                break;
        }
        circleImageView.setBorderColor(color);
        MiscUtils
                .showAvatarThumb(mAvatarManager, stranger.getAvatarThumb(),
                        showGroupList.groupFriendImage);

        if (mAvatarManager != null && strangerLocationInfo != null) {
            showGroupList.groupFriendDistance
                    .setText(MiscUtils.getDistanceString(mContext,
                            mLocationIngfo.getDistance(strangerLocationInfo)));
        } else {
            showGroupList.groupFriendDistance.setBackgroundColor(mContext.getResources()
                    .getColor(
                            R.color.transparent));
            showGroupList.groupFriendDistance.setText("");
        }
        showName(stranger,showGroupList.groupFriendNickname);
//        String nickName=stranger.getNickname();
//        String nickCode=stranger.getLabelCode();
//        if(nickName.length()==0||nickName!=null){
//            showGroupList.groupFriendNickname.setText(nickName);
//        }else{
//            showGroupList.groupFriendNickname.setText(stranger.getLabelCode());
//        }
        showGroupList.groupFriendDistance.setVisibility(View.GONE);


        return convertView;
    }
    private void showName(Stranger stranger, TextView textView) {
        final UserContact contact = mContactsManager
                .getUserContactByUserId(stranger.getUserId());
        final String name = (contact != null) ? contact.getShowName()
                : stranger.getShowName();
        final int color = (contact != null) ? mFriendColor : mStrangerColor;

        textView.setTextColor(color);
        textView.setText(name);

    }
    class ShowGroupList {
        ImageView groupFriendImage;
        TextView groupFriendDistance;
        TextView groupFriendNickname;
    }

}
