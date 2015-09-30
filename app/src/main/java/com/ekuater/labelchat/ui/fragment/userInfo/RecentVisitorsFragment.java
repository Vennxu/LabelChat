package com.ekuater.labelchat.ui.fragment.userInfo;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.RecentVisitor;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.InterestManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.labelstory.LabelStoryUtils;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ViewHolder;
import com.ekuater.labelchat.ui.widget.CircleImageView;

/**
 * Created by Administrator on 2015/3/26.
 */
public class RecentVisitorsFragment extends Fragment{

    private ListView listView;
    private ImageView loading;
    private Activity activity;
    private AvatarManager mAvatarManager;
    private InterestManager mInserestManager;
    private RecentVisitor[] recentVisitors;
    private RecentVisitorAdapter mRecentVisitorAdapter;
    private String queryUserId;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            stopAnimation();
            if (msg.what == ConstantCode.EXECUTE_RESULT_SUCCESS) {
                recentVisitors = (RecentVisitor[]) msg.obj;
                if (recentVisitors != null && recentVisitors.length > 0){
                    mRecentVisitorAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        mAvatarManager = AvatarManager.getInstance(activity);
        mInserestManager = InterestManager.getInstance(activity);
        mRecentVisitorAdapter = new RecentVisitorAdapter();
        argmentParam();
    }

    private void startAnimation() {
        loading.setVisibility(View.VISIBLE);
        Drawable drawable = loading.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.start();
    }

    private void stopAnimation() {
        loading.setVisibility(View.GONE);
        Drawable drawable = loading.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.stop();
    }

    private void argmentParam(){
        Bundle bundle = getArguments();
        if (bundle != null){
            queryUserId = bundle.getString(LabelStoryUtils.LABEL_STORY_USER_ID);
        }
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null){
            actionBar.hide();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_visitors,container,false);
        listView = (ListView) view.findViewById(R.id.recent_list);
        loading = (ImageView) view.findViewById(R.id.recent_loading);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView title = (TextView) view.findViewById(R.id.title);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        title.setText(R.string.recent_visitor);
        listView.setAdapter(mRecentVisitorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Stranger stranger = new Stranger();
                RecentVisitor recentVisitor = recentVisitors[position];
                if (recentVisitor != null) {
                    stranger.setUserId(recentVisitor.getRecentUserId());
                    stranger.setAvatarThumb(recentVisitor.getRecentUserAvatarThumb());
                    stranger.setAvatar(recentVisitor.getRecentUserAvatar());
                    stranger.setLabelCode(recentVisitor.getRecentUserLabelCode());
                    stranger.setNickname(recentVisitor.getRecentUserName());
                    UILauncher.launchStrangerDetailUI(activity, stranger);
                }
            }
        });
        getRecentVisitor();
        return view;
    }

    private void getRecentVisitor(){
        startAnimation();
        InterestManager.RecentVisitorObserver observer = new InterestManager.RecentVisitorObserver() {
            @Override
            public void onQueryResult(int result, RecentVisitor[] recentVisitorss) {
                Message message = Message.obtain(mHandler,result,recentVisitorss);
                mHandler.sendMessage(message);
            }
        };
        mInserestManager.getRecentVisitor(queryUserId,observer);
    }

    private class RecentVisitorAdapter extends BaseAdapter{

        private LayoutInflater inflater;

        public RecentVisitorAdapter(){
            inflater = LayoutInflater.from(activity);
        }

        @Override
        public int getCount() {
            return recentVisitors == null ? 0 : recentVisitors.length;
        }

        @Override
        public RecentVisitor getItem(int position) {
            return recentVisitors[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null){
                convertView = inflater.inflate(R.layout.recent_visitor_item,parent,false);
            }
            CircleImageView imageView = (CircleImageView) ViewHolder.get(convertView,R.id.recent_item_icon);
            TextView textName = (TextView) ViewHolder.get(convertView,R.id.recent_item_title);
            TextView textTime = (TextView) ViewHolder.get(convertView,R.id.recent_item_time);

            MiscUtils.showAvatarThumb(mAvatarManager,getItem(position).getRecentUserAvatarThumb(),imageView,R.drawable.contact_single);
            textName.setText(getItem(position).getRecentUserName());
            textTime.setText(getTimeString(getItem(position).getRecentDate()));
            return convertView;
        }

        private String getTimeString(long time) {
            return DateTimeUtils.getTimeString(activity,time);
        }
    }

}
