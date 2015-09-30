package com.ekuater.labelchat.ui.fragment.main;

import android.content.res.Resources;
import android.os.AsyncTask;
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
import com.ekuater.labelchat.datastruct.Dynamic.DynamicPublicEvent;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.UIEventBusHub;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.labelstory.NewMessageHint;
import com.ekuater.labelchat.ui.fragment.mixdynamic.MixDynamicUIHelper;
import com.ekuater.labelchat.ui.widget.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Leo on 2015/3/9.
 *
 * @author Xu wenxiang
 */
public class LabelStoryPageFragment extends Fragment {

    private PushMessageManager mPushMessageManager;
    private EventBus mUIEventBus;
    private PagerAdapter adapter;
    private TextView mTipArea;

    private PushMessageManager.AbsListener mPushMessagManagerList = new PushMessageManager.AbsListener() {
        @Override
        public void onNewSystemPushReceived(SystemPush systemPush) {
            super.onNewSystemPushReceived(systemPush);
            loadNewMessage();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPushMessageManager = PushMessageManager.getInstance(getActivity());
        mPushMessageManager.registerListener(mPushMessagManagerList);
        mUIEventBus = UIEventBusHub.getDefaultEventBus();
        mUIEventBus.register(this);
        adapter = new PagerAdapter(getChildFragmentManager(), getResources());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_label_story_page, container, false);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        mTipArea = (TextView) rootView.findViewById(R.id.tip_area);
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.pager);
        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) rootView.findViewById(R.id.pager_tab);
        title.setText(R.string.main_activity_tab_dynamic_description);
        viewPager.setAdapter(adapter);
        tabStrip.setViewPager(viewPager);
        loadNewMessage();
        return rootView;
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(DynamicPublicEvent event) {
        loadNewMessage();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mPushMessageManager.unregisterListener(mPushMessagManagerList);
        mUIEventBus.unregister(this);
    }

    private void loadNewMessage() {
        new LoadDynamicMessageTask().executeOnExecutor(LoadDynamicMessageTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    private class LoadDynamicMessageTask extends AsyncTask<Void, Void, List<NewMessageHint>> {

        @Override
        protected List<NewMessageHint> doInBackground(Void... params) {
            return mPushMessageManager.getUnprocessedHintMessage();
        }

        @Override
        protected void onPostExecute(List<NewMessageHint> newMessageHints) {
            super.onPostExecute(newMessageHints);
            int messageCount = newMessageHints.size();
            if (messageCount > 0) {
                mTipArea.setVisibility(View.VISIBLE);
                mTipArea.setText(String.valueOf(messageCount));
                mTipArea.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UILauncher.launchDynamicNewMessageUI(getActivity());
                    }
                });
            } else {
                mTipArea.setVisibility(View.GONE);
            }
        }
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mPageList = new ArrayList<>();
        private final List<CharSequence> mTitleList = new ArrayList<>();

        public PagerAdapter(FragmentManager fm, Resources resources) {
            super(fm);
            initFragments(resources);
        }

        private void initFragments(Resources resources) {
            mPageList.add(MixDynamicUIHelper.newGlobalFragment());
            mPageList.add(MixDynamicUIHelper.newRelatedFragment());

            mTitleList.add(resources.getString(R.string.labelstory_all));
            mTitleList.add(resources.getString(R.string.labelstory_related));
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
