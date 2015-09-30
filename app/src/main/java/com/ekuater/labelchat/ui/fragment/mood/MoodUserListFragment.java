package com.ekuater.labelchat.ui.fragment.mood;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.datastruct.MoodUser;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserGroup;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.FollowingManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/5/9.
 */
public class MoodUserListFragment extends Activity {

    private Context context;
    private TextView title;
    private TextView rightTitle;
    private ExpandableListView mUserList;
    private ContactsManager contactsManager;
    private FollowingManager followingManager;
    private MoodUserListAdapter moodUserListAdapter;


    private String[] groupName;
    private boolean isAllGroup;
    private ArrayList<UserGroup> group;
    private ArrayList<MoodUser> moodUsers;
    private ArrayList<HashMap<String, ArrayList<MoodUser>>> arrayList = new ArrayList<>();

    private ExpandableListView.OnChildClickListener mOnchildClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

            MoodUser moodUser = moodUserListAdapter.getChild(groupPosition, childPosition);
            UserGroup userGroup = moodUserListAdapter.getGroup(groupPosition);
            int select = moodUser.getIsSelect();
            if (select == 0) {
                if (moodUsers.size() > 19) {
                    return false;
                }
                moodUsers.add(moodUser);
                moodUser.setIsSelect(MoodUser.SELECT);

            } else {
                for (int i = 0; i < moodUsers.size(); i++) {
                    if (moodUsers.get(i).getUserId().equals(moodUser.getUserId())) {
                        moodUsers.remove(i);
                        break;
                    }
                }
                moodUser.setIsSelect(MoodUser.NO_SELECT);
            }
            int allSelect = userGroup.isAllSelect();
            if (allSelect == UserGroup.ALL_SELECT) {
                userGroup.setIsAllSelect(UserGroup.NO_SELECT);
            }
            isSend();
            moodUserListAdapter.notifyDataSetChanged();
            updateText();
            return false;
        }
    };

    private MoodUserListAdapter.AllSelectClick mAllSelectClick = new MoodUserListAdapter.AllSelectClick() {
        @Override
        public void select(int groupPosition) {
            UserGroup userGroup = moodUserListAdapter.getGroup(groupPosition);
            int select = userGroup.isAllSelect();
            if (select == UserGroup.NO_SELECT) {
                int size = 20 - moodUsers.size();
                ArrayList<MoodUser> users = moodUserListAdapter.getArrayMoodUser(groupPosition, userGroup.getGroupName());
                if (moodUsers.size() > 20 || users.size() > size) {
                    return;
                }
                userGroup.setIsAllSelect(UserGroup.ALL_SELECT);
                group.get(groupPosition).setIsAllSelect(UserGroup.ALL_SELECT);
                isAllSelect(users, UserGroup.ALL_SELECT);
            } else {
                userGroup.setIsAllSelect(UserGroup.NO_SELECT);
                group.get(groupPosition).setIsAllSelect(UserGroup.NO_SELECT);
                ArrayList<MoodUser> users = moodUserListAdapter.getArrayMoodUser(groupPosition, userGroup.getGroupName());
                isAllSelect(users, UserGroup.NO_SELECT);
            }
            isSend();
            moodUserListAdapter.notifyDataSetChanged();
            updateText();
        }
    };

    private View.OnClickListener mOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.icon:
                    setParamResult();
                    break;
                case R.id.right_title:
                    setParamResult();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_mood_user_list);
        title = (TextView) findViewById(R.id.title);
        ImageView icon = (ImageView) findViewById(R.id.icon);
        rightTitle = (TextView) findViewById(R.id.right_title);
        icon.setOnClickListener(mOnclickListener);
        rightTitle.setText(getString(R.string.submit));
        rightTitle.setVisibility(View.VISIBLE);
        rightTitle.setOnClickListener(mOnclickListener);
        context = this;
        groupName = new String[]{context.getResources().getString(R.string.contact),
                context.getResources().getString(R.string.pay_attention),
                context.getResources().getString(R.string.fans)};
        group = new ArrayList<>();
        contactsManager = ContactsManager.getInstance(this);
        followingManager = FollowingManager.getInstance(this);
        mUserList = (ExpandableListView) findViewById(R.id.user_list);
        mUserList.setOnChildClickListener(mOnchildClickListener);
        argmentParam();
        initDate();
        isSend();
        moodUserListAdapter = new MoodUserListAdapter(context, group, arrayList, mAllSelectClick);
        mUserList.setAdapter(moodUserListAdapter);
        for (int i = 0; i < moodUserListAdapter.getGroupCount(); i++) {
            mUserList.expandGroup(i);
        }
    }

    private void argmentParam() {
        Intent intent = getIntent();
        if (intent != null) {
            moodUsers = intent.getParcelableArrayListExtra(MoodUtils.USERIDS);
            ArrayList<UserGroup> userGroups = intent.getParcelableArrayListExtra(MoodUtils.USER_GROUP);
            if (userGroups != null && userGroups.size() > 0) {
                isAllGroup = true;
                group.addAll(userGroups);
            }
        }
        updateText();
    }

    private void updateText() {
        title.setText(getString(R.string.mood_select_user, moodUsers.size(), 20));
    }

    private void initDate() {
        UserContact[] userContacts = contactsManager.getAllUserContact();
        FollowUser[] followingUsers = followingManager.getAllFollowingUser();
        FollowUser[] followerUsers = followingManager.getAllFollowerUser();
        addContacUsers(userContacts);
        addMoodUsers(followingUsers, groupName[1]);
        addMoodUsers(followerUsers, groupName[2]);
        ArrayList<MoodUser> moodUsers = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++) {
            boolean isContainsKey = false;
            if (arrayList.size() == 1) {
                return;
            }
            HashMap<String, ArrayList<MoodUser>> hashMap = arrayList.get(i);
            for (int j = 0; j < group.size(); j++) {
                String gpName = group.get(j).getGroupName();

                if (hashMap.containsKey(gpName)) {
                    if (i == 0) {
                        moodUsers.addAll(hashMap.get(gpName));
                    } else {
                        ArrayList<MoodUser> tmpMoodUser = hashMap.get(gpName);
                        tmpMoodUser.removeAll(moodUsers);
                        if (tmpMoodUser.size() == 0) {
                            arrayList.remove(i);
                            if (!isAllGroup) {
                                group.remove(j);
                            }
                        }
                        if (i == arrayList.size() - 1) {
                            moodUsers.clear();
                            Log.d("moodUsers", arrayList.size() + " ");
                        } else {
                            moodUsers.addAll(tmpMoodUser);
                        }
                    }
                    isContainsKey = true;
                    break;
                }
            }
            if (!isContainsKey) {
                arrayList.remove(i);
            }

        }
    }


    private void addMoodUsers(FollowUser[] followUsers, String groupName) {
        if (followUsers != null && followUsers.length > 0) {
            HashMap<String, ArrayList<MoodUser>> hashMap = new HashMap<>();
            ArrayList<MoodUser> users = new ArrayList<>();
            for (int i = 0; i < followUsers.length; i++) {
                FollowUser followUser = followUsers[i];
                if (isSelect(followUser.getUserId())) {
                    users.add(new MoodUser(followUser, MoodUser.SELECT));
                } else {
                    users.add(new MoodUser(followUser, MoodUser.NO_SELECT));
                }
            }
            hashMap.put(groupName, users);
            arrayList.add(hashMap);
            if (!isAllGroup) {
                group.add(new UserGroup(UserGroup.NO_SELECT, groupName));
            }
        }
    }

    private void addContacUsers(UserContact[] userContacts) {
        if (userContacts != null && userContacts.length > 0) {
            HashMap<String, ArrayList<MoodUser>> hashMap = new HashMap<>();
            ArrayList<MoodUser> users = new ArrayList<>();
            for (int i = 0; i < userContacts.length; i++) {
                UserContact userContact = userContacts[i];
                if (isSelect(userContact.getUserId())) {
                    users.add(new MoodUser(userContact, MoodUser.SELECT));
                } else {
                    users.add(new MoodUser(userContact, MoodUser.NO_SELECT));
                }
            }
            hashMap.put(groupName[0], users);
            arrayList.add(hashMap);
            if (!isAllGroup) {
                group.add(new UserGroup(UserGroup.NO_SELECT, groupName[0]));
            }
        }
    }

    private boolean isSelect(String userId) {
        boolean isSelect = false;
        if (moodUsers != null && moodUsers.size() > 0) {
            for (int i = 0; i < moodUsers.size(); i++) {
                if (moodUsers.get(i).getUserId().endsWith(userId)) {
                    isSelect = true;
                    break;
                }
            }
        } else {
            isSelect = false;
        }
        return isSelect;
    }

    private void isAllSelect(ArrayList<MoodUser> users, int select) {
        for (int i = 0; i < users.size(); i++) {
            MoodUser user = users.get(i);
            user.setIsSelect(select);
        }
        moodUsers.removeAll(users);
        if (select == MoodUser.SELECT) {
            moodUsers.addAll(users);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            setParamResult();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void isSend() {
        if (moodUsers.size() == 0) {
            rightTitle.setEnabled(false);
            rightTitle.setTextColor(getResources().getColor(R.color.colorLightDark));
        } else {
            rightTitle.setEnabled(true);
            rightTitle.setTextColor(Color.WHITE);
        }
    }

    public void setParamResult() {
        Intent intent = new Intent();
        intent.putExtra(MoodUtils.USERIDS, moodUsers);
        intent.putExtra(MoodUtils.USER_GROUP, moodUserListAdapter.getUserGroup());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
