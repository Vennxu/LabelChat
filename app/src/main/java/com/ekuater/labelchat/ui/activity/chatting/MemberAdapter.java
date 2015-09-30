package com.ekuater.labelchat.ui.activity.chatting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.ui.util.MiscUtils;

/**
 * Created by Leo on 2015/3/7.
 *
 * @author LinYong
 */
public class MemberAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private AvatarManager mAvatarManager;
    private LiteStranger[] mMembers;

    public MemberAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mAvatarManager = AvatarManager.getInstance(context);
        mMembers = null;
    }

    public void updateMembers(LiteStranger[] members) {
        mMembers = members;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMembers != null ? mMembers.length : 0;
    }

    @Override
    public LiteStranger getItem(int position) {
        return mMembers != null ? mMembers[position] : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView(parent);
        }
        bindView(position, convertView);
        return convertView;
    }

    private View newView(ViewGroup parent) {
        View view = mInflater.inflate(R.layout.member_item, parent, false);
        ViewHolder holder = new ViewHolder();

        holder.avatarImage = (ImageView) view.findViewById(R.id.avatar_image);
        view.setTag(holder);
        return view;
    }

    private void bindView(int position, View view) {
        LiteStranger stranger = getItem(position);
        ViewHolder holder = (ViewHolder) view.getTag();

        MiscUtils.showAvatarThumb(mAvatarManager, stranger.getAvatarThumb(),
                holder.avatarImage);
    }

    private static class ViewHolder {

        public ImageView avatarImage;
    }
}
