package com.ekuater.labelchat.ui.fragment.friends;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemRecommendFriendMessage;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.fragment.UserShowFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/1/27.
 *
 * @author FanChong
 */
public class RecommendFriendMainFragment extends Fragment {

    public static final String EXTRA_MESSAGE_ID = "message_id";

    private PushMessageManager mPushManager;
    private SystemRecommendFriendMessage mSystemRecommendFriendMessage;
    private long mMessageId;
    private Stranger[] mStranger;
    private ViewPager mViewPager;
    private ShowRecommendFriendAdapter mShowRecommendFriendAdapter;
    private ImageView[] mDotImage;
    private ImageView mDot1, mDot2, mDot3;

    private void loadMessage() {
        Bundle arguments = getArguments();
        mMessageId = -1;
        if (arguments != null) {
            mMessageId = arguments.getLong(EXTRA_MESSAGE_ID);
        }
        SystemPush systemPush = mPushManager.getPushMessage(mMessageId);
        {
            if (systemPush != null) {
                mSystemRecommendFriendMessage = SystemRecommendFriendMessage.build(systemPush);
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mPushManager = PushMessageManager.getInstance(getActivity());
        loadMessage();
        mStranger = mSystemRecommendFriendMessage.getRecommendFriends();
        mShowRecommendFriendAdapter = new ShowRecommendFriendAdapter(getActivity(), mStranger);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommend_firend_show, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mDot1 = (ImageView) view.findViewById(R.id.dot1);
        mDot2 = (ImageView) view.findViewById(R.id.dot2);
        mDot3 = (ImageView) view.findViewById(R.id.dot3);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setAdapter(mShowRecommendFriendAdapter);
        mDotImage = new ImageView[]{mDot1, mDot2, mDot3};
        mDotImage[0].setBackgroundResource(R.drawable.dot_select);
        mViewPager.setOnPageChangeListener(mPageChangeListener);

    }

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            for (int i = 0; i < mDotImage.length; i++) {
                mDotImage[position]
                        .setBackgroundResource(R.drawable.dot_select);
                if (position != i) {
                    mDotImage[i]
                            .setBackgroundResource(R.drawable.dot_normal);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };


    public static class ShowRecommendFriendAdapter extends FragmentPagerAdapter {
        public final List<Fragment> mFragmentList = new ArrayList<Fragment>();

        public ShowRecommendFriendAdapter(FragmentActivity activity, Stranger[] strangers) {
            super(activity.getSupportFragmentManager());
            for (Stranger stranger : strangers) {
                mFragmentList.add(UserShowFragment.newInstance(stranger));
            }

        }

        @Override
        public Fragment getItem(int position) {
            if (0 <= position && position < mFragmentList.size()) {
                return mFragmentList.get(position);
            } else {
                return null;
            }
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }
    }
}
