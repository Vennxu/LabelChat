package com.ekuater.labelchat.ui.fragment.throwphoto;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.PickPhotoUser;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.ui.fragment.friends.StrangerHelper;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;


/**
 * Created by Administrator on 2015/2/7.
 *
 * @author Fan Chong
 */
public class PickPhotoUserFragment extends Fragment {

    public static final String PICK_PHOTO_CROWD = "pick_photo_crowd";
    public static final String PICK_PHOTO_TITLE = "pick_photo_title";

    private PickPhotoUser[] mPickPhotoUser;
    private PickPhotoUserdAdapter adapter;
    private ListView mListView;

    private StrangerHelper strangerHelper;
    private String mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseArgument();
        adapter = new PickPhotoUserdAdapter(getActivity(), mPickPhotoUser);
        strangerHelper = new StrangerHelper(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attention_list, container, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        title.setText(mTitle);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        mListView = (ListView) view.findViewById(R.id.follower_user_list);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                strangerHelper.showStranger(mPickPhotoUser[position].getPickUserId());
            }
        });
        return view;
    }

    private void parseArgument() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mTitle = arguments.getString(PICK_PHOTO_TITLE);
            ActionBar actionBar = getActivity().getActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
            Parcelable[] parcelables = arguments.getParcelableArray(PICK_PHOTO_CROWD);
            if (parcelables != null && parcelables.length > 0) {
                mPickPhotoUser = new PickPhotoUser[parcelables.length];
                for (int i = 0; i < parcelables.length; i++) {
                    mPickPhotoUser[i] = (PickPhotoUser) parcelables[i];
                }
            }
        }
    }

    public class PickPhotoUserdAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        private AvatarManager mAvatarManager;
        private PickPhotoUser[] mUser;


        public PickPhotoUserdAdapter(Context context, PickPhotoUser[] user) {
            mContext = context;
            mUser = user;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mAvatarManager = AvatarManager.getInstance(context);
        }

        @Override
        public int getCount() {
            return mUser.length;
        }

        @Override
        public PickPhotoUser getItem(int position) {
            return mUser[position];
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
            View view = mInflater.inflate(R.layout.recent_visitor_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.avatarImage = (CircleImageView) view.findViewById(R.id.recent_item_icon);
            holder.nickname = (TextView) view.findViewById(R.id.recent_item_title);
            holder.time = (TextView) view.findViewById(R.id.recent_item_time);
            holder.dividerLine=view.findViewById(R.id.divider_line);
            view.setTag(holder);
            return view;
        }

        private void bindView(int position, View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.dividerLine.setVisibility(View.GONE);
            MiscUtils.showAvatarThumb(mAvatarManager, mUser[position].getPickUserAvatarThumb(), holder.avatarImage);
            holder.nickname.setText(mUser[position].getPickUserName());
            holder.time.setText(DateTimeUtils.getTimeString(mContext, mUser[position].getPickPhotoDate()));
        }

        class ViewHolder {
            View dividerLine;
            CircleImageView avatarImage;
            TextView nickname, time;
        }
    }
}
