package com.ekuater.labelchat.ui.fragment.main;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.widget.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/3/9.
 *
 * @author LinYong
 */
public class RelationshipPageFragment extends Fragment implements View.OnClickListener {

    private PagerAdapter mPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPagerAdapter = new PagerAdapter(getChildFragmentManager(), getResources());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_relationship_page, container, false);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.pager);
        View friendSearchBtn = rootView.findViewById(R.id.friend_search);
        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) rootView.findViewById(R.id.pager_tab);

        title.setText(R.string.main_activity_tab_relationship);
        viewPager.setAdapter(mPagerAdapter);
        friendSearchBtn.setOnClickListener(this);
        tabStrip.setViewPager(viewPager);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.friend_search:
                UILauncher.launchUserDiscoveryUI(getActivity());
                break;
            default:
                break;
        }
    }

    private static class PagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mPageList = new ArrayList<>();
        private final List<CharSequence> mTitleList = new ArrayList<>();

        public PagerAdapter(FragmentManager fm, Resources resources) {
            super(fm);
            initFragments(resources);
        }

        private void initFragments(Resources resources) {
            mPageList.add(new ContactsFragment());
            mPageList.add(new FollowingUserFragment());
            mPageList.add(new FollowerUserFragment());

            mTitleList.add(resources.getString(R.string.contact));
            mTitleList.add(resources.getString(R.string.pay_attention));
            mTitleList.add(resources.getString(R.string.fans));
        }

        @Override
        public int getCount() {
            return mPageList.size();
        }

        @Override
        public Fragment getItem(int position) {
            if (0 <= position && position < mPageList.size()) {
                return mPageList.get(position);
            } else {
                return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (0 <= position && position < mTitleList.size()) {
                return mTitleList.get(position);
            } else {
                return null;
            }
        }
    }
}
