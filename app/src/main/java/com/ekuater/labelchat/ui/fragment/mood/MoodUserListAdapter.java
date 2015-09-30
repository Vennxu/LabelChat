package com.ekuater.labelchat.ui.fragment.mood;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.MoodUser;
import com.ekuater.labelchat.datastruct.UserGroup;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.ui.widget.ClickEventInterceptLinear;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/5/9.
 */
public class MoodUserListAdapter extends BaseExpandableListAdapter {


    private ArrayList<UserGroup> mGroupName;
    private Context mContext;
    private LayoutInflater inflater;
    private AvatarManager avatarManager;
    private AllSelectClick allSelectClick;
    private ArrayList<HashMap<String, ArrayList<MoodUser>>> mArrayList;

    public MoodUserListAdapter(Context context, ArrayList<UserGroup> groupName, ArrayList<HashMap<String, ArrayList<MoodUser>>> arrayList,AllSelectClick allSelectClick){
        mArrayList = arrayList;
        mGroupName = groupName;
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.allSelectClick = allSelectClick;
        avatarManager = AvatarManager.getInstance(context);
    }

    public ArrayList<UserGroup> getUserGroup(){
        return mGroupName;
    }

    public ArrayList<MoodUser> getArrayMoodUser(int groupPosition, String key){
        return mArrayList.get(groupPosition).get(key);
    }

    @Override
    public int getGroupCount() {
        return mArrayList == null ? 0:mArrayList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return  mArrayList.get(groupPosition).get(getGroup(groupPosition).getGroupName()) == null ? 0 : mArrayList.get(groupPosition).get(getGroup(groupPosition).getGroupName()).size() ;
    }

    @Override
    public UserGroup getGroup(int groupPosition) {
        return mGroupName.get(groupPosition);
    }

    @Override
    public MoodUser getChild(int groupPosition, int childPosition) {
        return mArrayList.get(groupPosition).get(getGroup(groupPosition).getGroupName()).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.user_list_group, parent, false);
            holder.groupName = (TextView) convertView.findViewById(R.id.user_list_group_name);
            holder.groupImage = (ImageView) convertView.findViewById(R.id.user_list_group_select);
            holder.group = (ClickEventInterceptLinear) convertView.findViewById(R.id.user_list_group_linear);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        final int position = groupPosition;
        UserGroup userGroup = getGroup(groupPosition);
        if (userGroup != null){
            holder.groupName.setText(userGroup.getGroupName());
            holder.group.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allSelectClick.select(position);
                }
            });
            holder.groupImage.setImageResource(userGroup.isAllSelect() == UserGroup.NO_SELECT ? R.drawable.lc_btn_check_all_off:R.drawable.lc_btn_check_all_on);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.user_list_child, parent, false);
            holder.childImage = (CircleImageView) convertView.findViewById(R.id.user_list_child_image);
            holder.childName = (TextView) convertView.findViewById(R.id.user_list_child_name);
            holder.childSelect = (ImageView) convertView.findViewById(R.id.user_list_child_select);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        final MoodUser moodUser = getChild(groupPosition, childPosition);
        holder.childName.setText(moodUser.getUserName());
        MiscUtils.showAvatarThumb(avatarManager, moodUser.getAvatarThumb(), holder.childImage, R.drawable.contact_single);
        holder.childSelect.setImageResource(moodUser.getIsSelect() == 0 ? R.drawable.lc_btn_check_off:R.drawable.lc_btn_check_on);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public interface AllSelectClick{
         public void select(int groupPosition);
    }

    public class ViewHolder{
        public ImageView groupImage;
        public TextView groupName;
        public CircleImageView childImage;
        public TextView childName;
        public ImageView childSelect;
        public View childView;
        public ClickEventInterceptLinear group;
    }
}
