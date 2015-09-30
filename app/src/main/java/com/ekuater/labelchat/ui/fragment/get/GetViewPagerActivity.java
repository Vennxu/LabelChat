package com.ekuater.labelchat.ui.fragment.get;


import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.TitleIconActivity;
import com.ekuater.labelchat.ui.util.NoCacheFragmentPagerAdapter;


public class GetViewPagerActivity extends TitleIconActivity implements View.OnClickListener {

    private CustomViewPager mViewPager;
    private List<Stranger> mStrangerList = null;
    private int mCount = 0;
    public static final String VIEW_PAGER_INFO = "info";
    public static final String VIEW_PAGER_VIEW = "view";
    private static final int MSG_HANDLE_GET_RESULT = 101;
    private ImageView mGetProgress,mMusicImage;
    private RelativeLayout mGetRelative, mGet;
    private TextView mChanges;
    private MenuItem mMenuTtem;
    private RelativeLayout mBack;
    private MyFragmentPageAdapter mAdapter = null;
    public static final String CLOSE_GET_VIEWPAGER = "closegetviewpager";
    private Context mContext;
    private ContactsManager mContactsManager;
    private boolean isMusic=true;
    private boolean isOpen=true;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private Handler getHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_HANDLE_GET_RESULT:
                    initial();
                    break;

            }
        }
    };


    private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageSelected(int arg0) {
            mCount = arg0;

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
            if (mCount == mStrangerList.size()-2) {
                if (arg0 == 2) {
                    loadGetPeople();
                }
            }
        }
    };
    float x;
    float moveX;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_viewpager);
        TextView title = (TextView) findViewById(R.id.title);
        ImageView icon = (ImageView) findViewById(R.id.icon);
        title.setText(getResources().getString(R.string.get_get));
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        isMusic = SettingHelper.getInstance(this).getAccountMusic();
        mContext = this;
        mViewPager = (CustomViewPager) findViewById(R.id.pager);
        mGetProgress = (ImageView) findViewById(R.id.image_get_loading);
        mGetRelative = (RelativeLayout) findViewById(R.id.get_viewpager_relative);
        mContactsManager = ContactsManager.getInstance(this);
        mGet = (RelativeLayout) findViewById(R.id.img_get_get);
        mChanges = (TextView) findViewById(R.id.get_viewpager_changes);
        mMusicImage = (ImageView) findViewById(R.id.get_viewpager_miusic);
        mMusicImage.setOnClickListener(this);
        mGet.setOnClickListener(this);
        mChanges.setOnClickListener(this);
        mViewPager.setOnPageChangeListener(mPageChangeListener);
        isOpenMusic();
        loadGetPeople();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CLOSE_GET_VIEWPAGER);
        getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);
    }
    private void isOpenMusic(){
        if (isMusic) {
            mMusicImage.setBackgroundResource(R.drawable.music_on);
        } else {
            mMusicImage.setBackgroundResource(R.drawable.music_off);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void initializeActionBar() {
        super.initializeActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == mGet) {
            if (mStrangerList.size() > 0 && mCount < mStrangerList.size()) {
                Stranger stranger = mStrangerList.get(mCount);
                if (stranger != null) {
                    Log.i("stranger", "-----------" + stranger.toString() + "-----------");
                    UILauncher.launchGetByLabelsUI(this, "1", stranger,isMusic);
                }
            }
        } else if (v == mChanges) {

            loadChanges();
        } else if (v == mBack) {
            finish();
        }else if (v==mMusicImage){
            if (isMusic){
                isMusic=false;
                SettingHelper.getInstance(this).setAccountMusic(isMusic);
                mMusicImage.setBackgroundResource(R.drawable.music_off);
            }else{
                isMusic=true;
                SettingHelper.getInstance(this).setAccountMusic(isMusic);
                mMusicImage.setBackgroundResource(R.drawable.music_on);
            }
        }
    }

    public void loadChanges() {
        mCount++;
        if (mCount < mStrangerList.size()) {
            mViewPager.setCurrentItem(mCount, true);
        } else {
            mCount = 0;
            loadGetPeople();
        }
    }

    public void startAnimation() {
        mGetProgress.setVisibility(View.VISIBLE);
        mGetRelative.setVisibility(View.GONE);
        mChanges.setVisibility(View.GONE);
        Drawable drawable = mGetProgress.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.start();
    }

    public void stopAnimation() {
        mGetProgress.setVisibility(View.GONE);
        mGetRelative.setVisibility(View.VISIBLE);
        mChanges.setVisibility(View.VISIBLE);
        Drawable drawable = mGetProgress.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.stop();
    }

    private List<Fragment> getDate() {
        List<Fragment> contents = new ArrayList<Fragment>();
        for (int i = 0; i < mStrangerList.size(); i++) {
            Stranger stranger = mStrangerList.get(i);
            GetFragment content = new GetFragment();
            Bundle args = new Bundle();
            args.putInt(VIEW_PAGER_VIEW, R.layout.get_fragment);
            args.putParcelable(VIEW_PAGER_INFO, stranger);
            content.setArguments(args);
            contents.add(content);
        }
        return contents;
    }

    private void initial() {
        stopAnimation();
        mCount = 0;
        List<Fragment> contents = getDate();
        mAdapter=new MyFragmentPageAdapter(getSupportFragmentManager(), contents);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mCount);
    }

    public void loadGetPeople() {
        startAnimation();
        if (mStrangerList != null) {
            mStrangerList.clear();
            mStrangerList = null;
        }
        ContactsManager.FriendsQueryObserver observer = new ContactsManager.FriendsQueryObserver() {
            @Override
            public void onQueryResult(int result, Stranger[] users, boolean remaining) {
                if (users != null && users.length > 0) {
                    mStrangerList = new ArrayList<Stranger>();
                    for (Stranger user : users) {
                        if (user != null && !user.getUserId().equals(SettingHelper.getInstance(mContext).getAccountUserId())) {
                            mStrangerList.add(user);
                        }
                    }
                    Message message = getHandler.obtainMessage(MSG_HANDLE_GET_RESULT);
                    getHandler.sendMessage(message);
                }
            }
        };
        mContactsManager.getRandUsers(10, observer);
    }

    public class MyFragmentPageAdapter extends NoCacheFragmentPagerAdapter {

        private final List<Fragment> mContents;

        public MyFragmentPageAdapter(FragmentManager fm,
                                     List<Fragment> contents) {
            super(fm);
            mContents = contents;
        }

        @Override
        public Fragment getItem(int arg0) {
            return mContents.get(arg0);
        }

        @Override
        public int getCount() {
            return mContents.size();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mCount == mStrangerList.size() - 2) {
                loadGetPeople();
        }else{
            mViewPager.setCurrentItem(mCount+1);
        }
        isMusic=SettingHelper.getInstance(this).getAccountMusic();
        isOpenMusic();
    }
}
