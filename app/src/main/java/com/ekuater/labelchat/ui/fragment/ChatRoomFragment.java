package com.ekuater.labelchat.ui.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ChatRoom;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.NormalChatRoomManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.util.BmpUtils;

/**
 * Created by Administrator on 2015/4/2.
 */
public class ChatRoomFragment extends Fragment{

    private static final int REQUEST_LIST_CHAT_ROOM = 101;
    private Activity mActivity;
    private NormalChatRoomManager mNormalChatRoomManager;
    private AvatarManager mAvatarManager;
    private ChatRoomAdapter mChatRoomAdapter;
    private ChatRoom[] mChatRooms;
    private int mPosition;
    private Display display;
    private int columnWidth;


    private ProgressBar mProgressBar;
    private GridView mChatRoomGrid;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            postHandler(msg);
        }
    };

    private void postHandler(Message msg) {
        switch (msg.what) {

            case REQUEST_LIST_CHAT_ROOM:
                mProgressBar.setVisibility(View.GONE);
                if (msg.arg1 == ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    mChatRooms = (ChatRoom[]) msg.obj;
                    mChatRoomAdapter.notifyDataSetChanged();
                } else {
                    if (mActivity != null) {
                        ShowToast.makeText(mActivity, R.drawable.emoji_sad, getString(R.string.chat_room_load_failed)).show();
                    }
                }
                break;
            default:
                break;

        }
    }
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mPosition = position;
            UILauncher.launchNormalChatRoomUI(getActivity(), mChatRooms[mPosition]);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        ActionBar actionBar = mActivity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mNormalChatRoomManager = NormalChatRoomManager.getInstance(mActivity);
        mAvatarManager = AvatarManager.getInstance(mActivity);
        mChatRoomAdapter = new ChatRoomAdapter();
        display = mActivity.getWindowManager().getDefaultDisplay();
        columnWidth = (display.getWidth()- BmpUtils.dp2px(mActivity, 30))/2;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatroom, container, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        title.setText(R.string.chat_room);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });
        mChatRoomGrid = (GridView) view.findViewById(R.id.chat_room_grid);
        mProgressBar = (ProgressBar) view.findViewById(R.id.chat_room_progress);
        mChatRoomGrid.setAdapter(mChatRoomAdapter);
        mChatRoomGrid.setOnItemClickListener(onItemClickListener);
        queryAllChatRoom();
        return view;
    }

    private void queryAllChatRoom() {
        mProgressBar.setVisibility(View.VISIBLE);
        NormalChatRoomManager.ChatRoomListObserver observer = new NormalChatRoomManager.ChatRoomListObserver() {
            @Override
            public void onListResult(int result, ChatRoom[] chatRooms) {
                Message message = Message.obtain(mHandler, REQUEST_LIST_CHAT_ROOM, result, 0, chatRooms);
                mHandler.sendMessage(message);
            }
        };
        mNormalChatRoomManager.listChatRooms(observer);
    }


    private class ChatRoomAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public ChatRoomAdapter() {
            mInflater = LayoutInflater.from(mActivity);
        }

        @Override
        public int getCount() {
            return mChatRooms == null ? 0 : mChatRooms.length;
        }

        @Override
        public ChatRoom getItem(int position) {
            return mChatRooms[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.discover_fragment_grid_item, parent, false);
            }
            ImageView chatRoomImage = (ImageView) convertView.findViewById(R.id.discover_chatroom_image);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(columnWidth,columnWidth);
            layoutParams.setMargins(0,BmpUtils.dp2px(mActivity,10),0,0);
            chatRoomImage.setLayoutParams(layoutParams);
            TextView chatRoomName = (TextView) convertView.findViewById(R.id.discover_chatroom_name);
            TextView chatRommNum = (TextView) convertView.findViewById(R.id.discover_chatroom_num);
            TextView chatRoomDesc = (TextView) convertView.findViewById(R.id.discover_chatroom_descript);

            ChatRoom chatRoom = getItem(position);
            chatRoomName.setText(chatRoom.getChatRoomName());
            chatRommNum.setText(chatRoom.getOnlineCount() + "");
            chatRoomDesc.setText(chatRoom.getDescript());
            MiscUtils.showChatRoomAvatarThumb(mAvatarManager,chatRoom.getImageUrl(),chatRoomImage,R.drawable.image_load_fail);
            return convertView;
        }
    }
}
