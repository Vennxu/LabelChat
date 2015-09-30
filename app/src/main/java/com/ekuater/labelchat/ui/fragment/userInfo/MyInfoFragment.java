package com.ekuater.labelchat.ui.fragment.userInfo;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserTheme;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.ThemeManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.util.TextUtil;

import java.lang.ref.WeakReference;

public class MyInfoFragment extends HeaderFragment {
    private static final int MSG_QUERY_USER_INFO_RESULT = 101;

    private Context mContext;
    private SettingHelper mSettingHelper;
    private ContactsManager mContactsManager;
    private ThemeManager mThemeManager;
    private AvatarManager mAvatarManager;
    private UserTheme mUserTheme;
    private AsyncLoadSomething mAsyncLoadSomething;
    private PersonalInfoAdapter mPersonalInfoAdapter;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_QUERY_USER_INFO_RESULT:
                    handlerQueryUserInfoResult((Stranger) msg.obj);
                    break;
            }
        }
    };


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setHeaderBackgroundScrollMode(HEADER_BACKGROUND_SCROLL_PARALLAX);
        setOnHeaderScrollChangedListener(new OnHeaderScrollChangedListener() {
            @Override
            public void onHeaderScrollChanged(float progress, int height, int scroll) {
                height -= getActivity().getActionBar().getHeight();

                progress = (float) scroll / height;
                if (progress > 1f) progress = 1f;

                progress = (1 - (float) Math.cos(progress * Math.PI)) * 0.5f;

                ((FadingActionBarActivity) getActivity())
                        .getFadingActionBarHelper()
                        .setActionBarAlpha((int) (255 * progress));
            }
        });

        cancelAsyncTask(mAsyncLoadSomething);
        mAsyncLoadSomething = new AsyncLoadSomething(this);
        mAsyncLoadSomething.execute();

    }

    private void queryStrangerInfo(String queryUserId) {
        mContactsManager.queryUserInfo(queryUserId, new ContactsManager.UserQueryObserver() {
            @Override
            public void onQueryResult(int result, Stranger user) {

                Message message = mHandler.obtainMessage(MSG_QUERY_USER_INFO_RESULT, user);
                mHandler.sendMessage(message);

            }
        });
    }

    private void handlerQueryUserInfoResult(Stranger stranger) {
        mPersonalInfoAdapter.getContactInfo(new UserContact(stranger), null);
    }


    @Override
    public void onDetach() {
        cancelAsyncTask(mAsyncLoadSomething);
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPersonalInfoAdapter = new PersonalInfoAdapter(getActivity(), null);
        mContext = getActivity();
        mContactsManager = ContactsManager.getInstance(mContext);
        mSettingHelper = SettingHelper.getInstance(mContext);
        mThemeManager = ThemeManager.getInstance(mContext);
        mAvatarManager = AvatarManager.getInstance(mContext);
        mUserTheme = getUserTheme();
        ImageView leftIcon = (ImageView) getActivity().getActionBar().getCustomView().findViewById(R.id.left_icon);
        TextView nickname = (TextView) getActivity().getActionBar().getCustomView().findViewById(R.id.nickname);
        ImageView rightIcon = (ImageView) getActivity().getActionBar().getCustomView().findViewById(R.id.right_icon);
        rightIcon.setVisibility(View.GONE);
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        nickname.setText(mSettingHelper.getAccountNickname());
        queryStrangerInfo(mSettingHelper.getAccountUserId());
    }

    private UserTheme getUserTheme() {
        String themeName = mSettingHelper.getUserTheme();
        return TextUtils.isEmpty(themeName) ? null : UserTheme.fromThemeName(themeName);
    }

    private ImageView mTopImageView, mAvatarImage;

    @Override
    public View onCreateHeaderView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.contact_info_header, container, false);
        mTopImageView = (ImageView) view.findViewById(R.id.top_background);
        TextView regionText = (TextView) view.findViewById(R.id.region);
        TextView signatureText = (TextView) view.findViewById(R.id.signature);
        regionText.setText(TextUtil.isEmpty(mSettingHelper.getAccountCity())
                ? mSettingHelper.getAccountProvince() : mSettingHelper.getAccountCity());
        signatureText.setText(mSettingHelper.getAccountSignature());
        displayThemeImages();
        mAvatarImage = (CircleImageView) view.findViewById(R.id.contact_avatar_image);
        MiscUtils.showAvatarThumb(mAvatarManager, mSettingHelper.getAccountAvatarThumb(),
                mAvatarImage, R.drawable.contact_single);
        return view;
    }

    private void displayThemeImages() {
        if (mUserTheme != null) {
            mThemeManager.displayThemeImage(mUserTheme.getTopImg(),
                    mTopImageView, R.drawable.user_show_bg);
        } else {
            mTopImageView.setImageResource(R.drawable.user_show_bg);
        }
    }

    private ListView mListView;

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_my_info, container, false);
        mListView = (ListView) view.findViewById(R.id.list);
        return view;
    }

    @Override
    public View onCoverView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }


    private void setListView() {
        if (mListView == null) return;
        mListView.setVisibility(View.VISIBLE);
        setListViewAdapter(mListView, mPersonalInfoAdapter);
    }

    private void cancelAsyncTask(AsyncTask task) {
        if (task != null) task.cancel(false);
    }

    private static class AsyncLoadSomething extends AsyncTask<Void, Void, Void> {

        private static final String TAG = "AsyncLoadSomething";

        final WeakReference<MyInfoFragment> weakFragment;

        public AsyncLoadSomething(MyInfoFragment fragment) {
            this.weakFragment = new WeakReference<>(fragment);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            final MyInfoFragment audioFragment = weakFragment.get();
            if (audioFragment.mListView != null)
                audioFragment.mListView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            final MyInfoFragment audioFragment = weakFragment.get();
            if (audioFragment == null) {
                if (Project.DEBUG) Log.d(TAG, "Skipping.., because there is no fragment anymore.");
                return;
            }
            audioFragment.setListView();
        }
    }
}