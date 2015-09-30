package com.ekuater.labelchat.ui.fragment.labelstory;

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
import com.ekuater.labelchat.datastruct.UserPraise;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.ui.fragment.friends.StrangerHelper;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;

/**
 * Created by Administrator on 2015/1/23.
 *
 * @author FanChong
 */
public class ShowPraiseCrowdFragment extends Fragment {
    public static final String PRAISE_CROWD = "praise_crowd";
    private ContactsManager mContactManager;
    private UserPraise[] mPraiseUser;
    private ListView mListView;
    private PraiseCrowdAdapter adapter;
    private StrangerHelper mStrangerHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().hide();
        parseArguments();
        mStrangerHelper = new StrangerHelper(getActivity());
        mContactManager = ContactsManager.getInstance(getActivity());
        adapter = new PraiseCrowdAdapter(getActivity(), mPraiseUser);
    }

    private void parseArguments() {
        Bundle argument = getArguments();
        if (argument != null) {
            Parcelable[] parcelables = argument.getParcelableArray(PRAISE_CROWD);
            if (parcelables != null && parcelables.length > 0) {
                mPraiseUser = new UserPraise[parcelables.length];
                for (int i = 0; i < parcelables.length; i++) {
                    mPraiseUser[i] = (UserPraise) parcelables[i];
                }
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attention_list, container, false);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView title = (TextView) view.findViewById(R.id.title);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        title.setText(R.string.labelstory_item_click_priase);
        mListView = (ListView) view.findViewById(R.id.follower_user_list);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(mOnItemClickListener);
        return view;
    }


    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            UserPraise userPraise = mPraiseUser[position];
            mStrangerHelper.showStranger(userPraise.getmPraiseUserId());
        }
    };

    public class PraiseCrowdAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        private AvatarManager mAvatarManager;
        private UserPraise[] mUser;


        public PraiseCrowdAdapter(Context context, UserPraise[] user) {
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
        public UserPraise getItem(int position) {
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
            holder.dividerLine = view.findViewById(R.id.divider_line);
            view.setTag(holder);
            return view;
        }

        private void bindView(int position, View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.dividerLine.setVisibility(View.GONE);
            MiscUtils.showAvatarThumb(mAvatarManager, mUser[position].getmPraiseUserAvatarThumb(), holder.avatarImage);
            holder.nickname.setText(mUser[position].getmPraiseUserName());
            holder.time.setText(DateTimeUtils.getTimeString(mContext, mUser[position].getmTime()));
        }

        class ViewHolder {
            View dividerLine;
            CircleImageView avatarImage;
            TextView nickname, time;
        }
    }
}
