
package com.ekuater.labelchat.ui.fragment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.PraiseStranger;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;

import java.util.ArrayList;
import java.util.List;

public class StrangerFriendGroupListViewAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private AvatarManager mAvatarManger;
    private ArrayList<PraiseStranger> mStrangerList = new ArrayList<PraiseStranger>();

    public ArrayList<PraiseStranger> getStrangerList() {
        return mStrangerList;
    }

    public StrangerFriendGroupListViewAdapter(Context context) {
        this.mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mAvatarManger = AvatarManager.getInstance(context);
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
        ShowGroupFriend showGroupFriend;
        CircleImageView circleImageView = null;
        PraiseStranger praiseStranger = getItem(position);
        Stranger stranger = praiseStranger.getStranger();
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.stranger_group, parent,
                    false);
            showGroupFriend = new ShowGroupFriend();
            showGroupFriend.strangerFriendImage = (ImageView) convertView
                    .findViewById(R.id.stringer_friend_group);
            convertView.setTag(showGroupFriend);
        } else {
            showGroupFriend = (ShowGroupFriend) convertView.getTag();
        }
        if (showGroupFriend.strangerFriendImage instanceof CircleImageView) {
            circleImageView = (CircleImageView) showGroupFriend.strangerFriendImage;
        }

        MiscUtils
                .showAvatarThumb(mAvatarManger, stranger.getAvatarThumb(),
                        showGroupFriend.strangerFriendImage);
        return convertView;
    }

    class ShowGroupFriend {
        ImageView strangerFriendImage;
    }

}
