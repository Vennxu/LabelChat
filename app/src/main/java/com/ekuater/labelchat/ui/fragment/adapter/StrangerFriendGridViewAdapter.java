
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
import com.ekuater.labelchat.datastruct.PraiseStranger;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;

import java.util.ArrayList;
import java.util.List;

public class StrangerFriendGridViewAdapter extends BaseAdapter {
    private Context mContext;
    private ContactsManager mContactsManager;
    private LayoutInflater mInflater;
    private AvatarManager mAvatarManger;
    private LocationInfo mLocationInfo;
    private ArrayList<PraiseStranger> mStrangerList = new ArrayList<PraiseStranger>();
    private boolean mShowDistance;

    private final int mFriendColor;
    private final int mStrangerColor;

    public ArrayList<PraiseStranger> getStrangerList() {
        return mStrangerList;
    }

    public StrangerFriendGridViewAdapter(Context context, boolean isShowDistance) {
        this.mContext = context;
        mStrangerList = new ArrayList<PraiseStranger>();
        mShowDistance = isShowDistance;
        mContactsManager = ContactsManager.getInstance(context);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mAvatarManger = AvatarManager.getInstance(context);
        mLocationInfo = AccountManager.getInstance(context).getLocation();

        Resources res = mContext.getResources();
        mFriendColor = res.getColor(R.color.friend_name_color);
        mStrangerColor = res.getColor(R.color.stranger_name_color);
    }

    @Override
    public int getCount() {
        return (mStrangerList == null) ? 0 : mStrangerList.size();
    }

    @Override
    public PraiseStranger getItem(int position) {
        return (mStrangerList == null) ? null : mStrangerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public synchronized void updataStrangerList(ArrayList<PraiseStranger> list) {
        mStrangerList = list;
        notifyDataSetChanged();
    }

    public synchronized void addStrangerList(List<PraiseStranger> list) {
        mStrangerList.addAll(list);
        notifyDataSetChanged();
    }

    public synchronized void remove(int position) {
        mStrangerList.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ShowFriend showFriend;
        CircleImageView circleImageView = null;
        PraiseStranger praiseStranger = getItem(position);
        Stranger stranger = praiseStranger.getStranger();
        LocationInfo strangerLocationInfo = stranger.getLocation();
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.fragment_stranger_friend_item, parent,
                    false);
            showFriend = new ShowFriend();
            showFriend.strangerFriendImage = (ImageView) convertView
                    .findViewById(R.id.stranger_friend_image);
            showFriend.strangerFriendDistance = (TextView) convertView
                    .findViewById(R.id.stranger_friend_distance);
            showFriend.strangerFriendNickname = (TextView) convertView
                    .findViewById(R.id.stranger_friend_nickname);
            showFriend.strangerPraiseQuantity = (TextView) convertView.findViewById(R.id.stranger_friend_praise_quantity);
            showFriend.labelLevel = (ImageView) convertView.findViewById(R.id.ic_praise);
            convertView.setTag(showFriend);
        } else {
            showFriend = (ShowFriend) convertView.getTag();
        }
        if (mShowDistance) {
            showFriend.strangerFriendDistance.setVisibility(View.VISIBLE);
        } else {
            showFriend.strangerFriendDistance.setVisibility(View.GONE);
        }
        if (showFriend.strangerFriendImage instanceof CircleImageView) {
            circleImageView = (CircleImageView) showFriend.strangerFriendImage;
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
                .showAvatarThumb(mAvatarManger, stranger.getAvatarThumb(),
                        showFriend.strangerFriendImage);

        if (mLocationInfo != null && strangerLocationInfo != null) {
            showFriend.strangerFriendDistance
                    .setText(MiscUtils.getDistanceString(mContext,
                            mLocationInfo.getDistance(strangerLocationInfo)));
        } else {
            showFriend.strangerFriendDistance.setBackgroundColor(mContext.getResources()
                    .getColor(
                            R.color.transparent));
            showFriend.strangerFriendDistance.setText("");
        }

        showName(stranger, showFriend.strangerFriendNickname);
        showFriend.strangerPraiseQuantity.setText("" + praiseStranger.getPraiseCount());
        showFriend.labelLevel.getDrawable().setLevel(praiseStranger.getPraiseCount());

        return convertView;
    }

    private void showName(Stranger stranger, TextView textView) {
        final UserContact contact = mContactsManager
                .getUserContactByUserId(stranger.getUserId());
        final String name = (contact != null) ? contact.getShowName()
                : stranger.getShowName();
        final int color = (contact != null) ? mFriendColor : mStrangerColor;

        textView.setText(name);
        textView.setTextColor(color);
    }

    class ShowFriend {
        ImageView strangerFriendImage;
        TextView strangerFriendDistance;
        TextView strangerFriendNickname;
        TextView strangerPraiseQuantity;
        ImageView labelLevel;
    }

}
