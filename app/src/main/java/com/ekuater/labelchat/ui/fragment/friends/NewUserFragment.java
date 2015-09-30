package com.ekuater.labelchat.ui.fragment.friends;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.util.CompatUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewUserFragment extends Fragment implements Handler.Callback {

    public static final String EXTRA_AS_PART = "extra_as_part";

    private static final int HANDLER_NEW_USER = 101;

    private ContactsManager mContactsManager;
    private StrangerAdapter mStrangerAdapter;
    private ProgressBar mProgressBar;
    private ListView listView;
    private Handler mHandler;
    private boolean mIsAsPart;

    private void handleNewUser(int result, Object object) {
        if (result == ConstantCode.EXECUTE_RESULT_SUCCESS) {
            mProgressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            Stranger[] strangers = (Stranger[]) object;
            mStrangerAdapter.addStrangerList(Arrays.asList(strangers));
        }
    }

    private final AbsListView.OnItemClickListener mItemClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mContactsManager.isInGuestMode()) {
                UILauncher.launchLoginPromptUI(getFragmentManager());
            } else {
                Object object = parent.getItemAtPosition(position);
                if (object instanceof Stranger) {
                    Stranger stranger = (Stranger) object;
                    UILauncher.launchStrangerDetailUI(view.getContext(), stranger);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        final Bundle args = getArguments();

        mIsAsPart = (args != null) && args.getBoolean(EXTRA_AS_PART, false);
        mHandler = new Handler(this);
        mContactsManager = ContactsManager.getInstance(activity);
        mStrangerAdapter = new StrangerAdapter(activity);

        if (!mIsAsPart && actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_people_around, container, false);
        listView = (ListView) view.findViewById(R.id.friend_list);
        listView.setAdapter(mStrangerAdapter);
        listView.setOnItemClickListener(mItemClickListener);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);

        if (mIsAsPart) {
            // Hide title bar
            view.findViewById(R.id.title_bar).setVisibility(View.GONE);
            CompatUtils.setBackground(view, null);
            listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        } else {
            TextView title = (TextView) view.findViewById(R.id.title);
            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            title.setText(R.string.new_user);
            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
        }
        getNewUser();
        return view;
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case HANDLER_NEW_USER:
                handleNewUser(msg.arg1, msg.obj);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private void getNewUser() {
        ContactsManager.NewUserObserver observer = new ContactsManager.NewUserObserver() {
            @Override
            public void onQueryResult(int result, Stranger[] strangers, boolean remaining) {
                Message message = Message.obtain(mHandler, HANDLER_NEW_USER, result, 0, strangers);
                mHandler.sendMessage(message);
            }
        };
        mContactsManager.queryNewUser(getActivity(), observer);
    }

    private static class StrangerAdapter extends BaseAdapter {

        private final Context mContext;
        private final ContactsManager mContactsManager;
        private final LayoutInflater mInflater;
        private final AvatarManager mAvatarManager;
        private final LocationInfo mMyLocation;
        private final int mFriendColor;
        private final int mStrangerColor;
        private List<Stranger> mStrangerList = new ArrayList<>();

        public StrangerAdapter(Context context) {
            super();
            mContext = context;
            mContactsManager = ContactsManager.getInstance(mContext);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mAvatarManager = AvatarManager.getInstance(context);
            mMyLocation = AccountManager.getInstance(context).getLocation();

            Resources res = mContext.getResources();
            mFriendColor = res.getColor(R.color.friend_name_color);
            mStrangerColor = res.getColor(R.color.stranger_name_color);
        }

        public synchronized void addStrangerList(List<Stranger> list) {
            mStrangerList.addAll(list);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mStrangerList.size();
        }

        @Override
        public Stranger getItem(int position) {
            return mStrangerList.get(position);
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
            bindView(convertView, position);
            return convertView;
        }

        private View newView(ViewGroup parent) {
            return mInflater.inflate(R.layout.search_friend_item, parent, false);
        }

        private void bindView(View view, int position) {
            Stranger stranger = getItem(position);
            TextView nameView = (TextView) view.findViewById(R.id.nickname);
            ImageView sexView = (ImageView) view.findViewById(R.id.gender);
            ImageView avatarImage = (ImageView) view.findViewById(R.id.avatar_image);
            TextView distanceView = (TextView) view.findViewById(R.id.distance);
            LocationInfo strangerLocation = stranger.getLocation();

            showName(stranger, nameView);
            sexView.setImageResource(ConstantCode.getSexImageResource(stranger.getSex()));
            MiscUtils.showAvatarThumb(mAvatarManager, stranger.getAvatarThumb(), avatarImage);

            if (mMyLocation != null && strangerLocation != null) {
                distanceView.setText(MiscUtils.getDistanceString(mContext,
                        mMyLocation.getDistance(strangerLocation)));
                distanceView.setVisibility(View.VISIBLE);
            } else {
                distanceView.setVisibility(View.GONE);
            }
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
    }
}
