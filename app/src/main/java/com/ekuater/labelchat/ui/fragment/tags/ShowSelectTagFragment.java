package com.ekuater.labelchat.ui.fragment.tags;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.UserTag;
import com.ekuater.labelchat.delegate.TagManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.util.CompatUtils;
import com.ekuater.labelchat.ui.widget.FlowLayout;

import java.util.Arrays;

/**
 * Created by Administrator on 2015/3/31.
 *
 * @author XuWenxiang
 */
public class ShowSelectTagFragment extends Fragment {

    private static final int MSG_TAG_UPDATED = 102;

    private LayoutInflater mInflater;
    private Resources mResources;
    private TagManager mTagManager;
    private Handler mHandler;
    private TagComparator mTagComparator;
    private UserTag[] mUserTags;
    private FlowLayout mShowContent;
    private View mNoTagTipView;

    private TagManager.IListener iListener = new TagManager.AbsListener() {
        @Override
        public void onUserTagUpdated() {
            notifyTagUpdated();
        }

        @Override
        public void onSetUserTagResult(int result) {
            notifyTagUpdated();
        }

        private void notifyTagUpdated() {
            mHandler.obtainMessage(MSG_TAG_UPDATED).sendToTarget();
        }
    };

    private void updateTags() {
        mUserTags = sortTags(mTagManager.getUserTags());
        mShowContent.removeAllViews();
        initDate();
    }

    private UserTag[] sortTags(UserTag[] tags) {
        if (tags != null && tags.length > 0) {
            Arrays.sort(tags, mTagComparator);
        }
        return tags;
    }

    private Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean handled = true;

            switch (msg.what) {
                case MSG_TAG_UPDATED:
                    updateTags();
                    break;
                default:
                    handled = false;
                    break;
            }
            return handled;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mInflater = LayoutInflater.from(activity);
        mResources = activity.getResources();
        mHandler = new Handler(callback);
        mTagComparator = new TagComparator();
        mTagManager = TagManager.getInstance(activity);
        mTagManager.registerListener(iListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_tag, container, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        title.setText(R.string.my_tag);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mNoTagTipView = view.findViewById(R.id.no_tag_tip);
        mShowContent = (FlowLayout) view.findViewById(R.id.show_tag_content);
        mShowContent.setHorizontalGap(20);
        mShowContent.setVerticalGap(20);
        view.findViewById(R.id.show_tag_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UILauncher.launchSelectUserTagUI(v.getContext());
            }
        });
        updateTags();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTagManager.unregisterListener(iListener);
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void initDate() {
        if (mUserTags != null && mUserTags.length > 0) {
            mNoTagTipView.setVisibility(View.GONE);
            for (UserTag userTag : mUserTags) {
                TextView userTagName = (TextView) mInflater.inflate(R.layout.interest_name,
                        mShowContent, false);
                userTagName.setText(userTag.getTagName());
                GradientDrawable drawable = (GradientDrawable) mResources
                        .getDrawable(R.drawable.corners_bg);
                drawable.setColor(userTag.parseTagColor());
                CompatUtils.setBackground(userTagName, drawable);
                mShowContent.addView(userTagName);
            }
        } else {
            mNoTagTipView.setVisibility(View.VISIBLE);
        }
    }
}
