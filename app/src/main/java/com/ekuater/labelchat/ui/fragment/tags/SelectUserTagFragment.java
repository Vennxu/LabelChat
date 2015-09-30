package com.ekuater.labelchat.ui.fragment.tags;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.TagType;
import com.ekuater.labelchat.datastruct.UserTag;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.TagManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Leo on 2015/3/16.
 *
 * @author LinYong
 */
public class SelectUserTagFragment extends Fragment implements TagSelectListener,
        ViewPager.OnPageChangeListener, View.OnClickListener {

    private static final int[] TYPE_NAME_BG = {
            R.drawable.tag_type_bg1,
            R.drawable.tag_type_bg2,
            R.drawable.tag_type_bg3,
    };

    private static int getTypeNameBg(int itemIdx) {
        final int length = TYPE_NAME_BG.length;
        final int idx = itemIdx % length;
        return TYPE_NAME_BG[idx];
    }

    private static final int MSG_LOAD_TAG_TYPES = 101;

    private Handler mHandler;
    private TagManager mTagManager;
    private ContactsManager mContactsManager;
    private TagTypeAdapter mTagTypeAdapter;
    private final Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean handled = true;

            switch (msg.what) {
                case MSG_LOAD_TAG_TYPES:
                    handleLoadTagTypes((TagType[]) msg.obj);
                    break;
                default:
                    handled = false;
                    break;
            }
            return handled;
        }
    };
    private boolean mLoading;
    private ImageView mLoadingImage;
    private TextView mTypeNameView;
    private ViewPager mViewPager;
    private Button mNextButton;
    private boolean mTagChanged;
    private ArrayList<UserTag> userTags;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();

        mHandler = new Handler(mHandlerCallback);
        mTagManager = TagManager.getInstance(activity);
        mContactsManager = ContactsManager.getInstance(activity);
        mTagTypeAdapter = new TagTypeAdapter(getChildFragmentManager(), this);

        if (actionBar != null) {
            actionBar.hide();
        }
        mTagChanged = false;
        loadTagTypes();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_select_user_tag, container, false);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        ImageView icon = (ImageView) rootView.findViewById(R.id.icon);
        title.setText(R.string.select_tag);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mLoadingImage = (ImageView) rootView.findViewById(R.id.loading);
        mTypeNameView = (TextView) rootView.findViewById(R.id.type_name);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mNextButton = (Button) rootView.findViewById(R.id.next_step);
        mViewPager.setAdapter(mTagTypeAdapter);
        mViewPager.setOnPageChangeListener(this);
        onPageSelected(mViewPager.getCurrentItem());
        updateLoadingImageVisibility();
        mNextButton.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTagChanged) {
            setTags();
        }
    }

    @Override
    public void onTagSelectChanged(UserTag tag, boolean isSelected) {
        mTagChanged = true;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        TagTypeDisplayFragment fragment = mTagTypeAdapter.getItem(position);
        if (fragment != null) {
            mTypeNameView.setVisibility(View.VISIBLE);
            mTypeNameView.setBackgroundResource(getTypeNameBg(position));
            mTypeNameView.setText(fragment.getTagTypeName());
        } else {
            mTypeNameView.setVisibility(View.GONE);
        }

        int count = mTagTypeAdapter.getCount();
        if (count > 0) {
            mNextButton.setVisibility(View.VISIBLE);
            if (position == count - 1) {
                mNextButton.setText(R.string.done);
                mNextButton.setBackgroundResource(R.drawable.tag_finish);
            } else {
                mNextButton.setText(R.string.next_tag_type);
                mNextButton.setBackgroundResource(R.drawable.next_step);
            }
        } else {
            mNextButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next_step:
                onNextStepClick();
                break;
            default:
                break;
        }
    }

    private void onNextStepClick() {
        int currentItem = mViewPager.getCurrentItem();
        int itemCount = mTagTypeAdapter.getCount();
        int nextItm = currentItem + 1;
        if (currentItem == itemCount - 1) {
            finish();
        } else if (0 <= nextItm && nextItm < itemCount) {
            mViewPager.setCurrentItem(nextItm);
        }
    }

    private void loadTagTypes() {
        mTagManager.getTagTypes(new TagManager.TagTypeObserver() {
            @Override
            public void onQueryResult(int result, TagType[] tagTypes) {
                mHandler.obtainMessage(MSG_LOAD_TAG_TYPES, result, 0, tagTypes).sendToTarget();
            }
        });
        mLoading = true;
    }

    private void handleLoadTagTypes(TagType[] tagTypes) {
        mTagTypeAdapter.updateTagTypes(tagTypes);
        userTags = new ArrayList<>();
        UserTag[] userTag = mTagManager.getUserTags();
        if (userTag != null){
            Collections.addAll(userTags, userTag);
        }
        mLoading = false;
        if (getView() != null) {
            updateLoadingImageVisibility();
            onPageSelected(mViewPager.getCurrentItem());
        }
    }

    private void setTags() {
        List<UserTag> tagList = new ArrayList<>();
        String tagId = null;
        for (int i = 0; i < mTagTypeAdapter.getCount(); ++i) {
            TagTypeDisplayFragment fragment = mTagTypeAdapter.getItem(i);
            Collections.addAll(tagList, fragment.getSelectedTags());
        }
        ArrayList<UserTag> tmpUserTag = new ArrayList<>();
        tmpUserTag.addAll(tagList);
        tmpUserTag.removeAll(userTags);
        if (tagList.size() > 0){
            List<String> userIds = mContactsManager.getUserIds(getActivity());
            String[] strUserIds = null;
            tagId = String.valueOf(tmpUserTag.get(0).getTagId());
            if(userIds != null) {
                strUserIds = userIds.toArray(new String[userIds.size()]);
            }
            mTagManager.setUserTags(tagList.toArray(new UserTag[tagList.size()]),strUserIds, tagId);
        }
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void updateLoadingImageVisibility() {
        if (mLoadingImage != null) {
            mLoadingImage.setVisibility(mLoading ? View.VISIBLE : View.GONE);
        }
    }

    private static class TagTypeAdapter extends FragmentPagerAdapter {

        private final List<TagTypeDisplayFragment> mFragmentList = new ArrayList<>();
        private final TagSelectListener mTagSelectListener;

        public TagTypeAdapter(FragmentManager fm, TagSelectListener listener) {
            super(fm);
            mTagSelectListener = listener;
        }

        public void updateTagTypes(TagType[] tagTypes) {
            initFragments(tagTypes);
            notifyDataSetChanged();
        }

        private void initFragments(TagType[] tagTypes) {
            mFragmentList.clear();
            if (tagTypes != null) {
                for (TagType tagType : tagTypes) {
                    TagTypeDisplayFragment fragment = new TagTypeDisplayFragment();
                    fragment.setTagSelectListener(mTagSelectListener);
                    fragment.setTagType(tagType);
                    mFragmentList.add(fragment);
                }
            }
        }

        @Override
        public TagTypeDisplayFragment getItem(int position) {
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
