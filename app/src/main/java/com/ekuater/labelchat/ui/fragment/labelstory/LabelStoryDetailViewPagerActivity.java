package com.ekuater.labelchat.ui.fragment.labelstory;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.ui.ContentSharer;
import com.ekuater.labelchat.ui.ShareContent;
import com.ekuater.labelchat.ui.activity.base.BaseActivity;
import com.ekuater.labelchat.ui.activity.base.TitleIconActivity;

import java.util.ArrayList;
import java.util.List;


public class LabelStoryDetailViewPagerActivity extends TitleIconActivity{

    private ViewPager mViewPager;
    private ArrayList<LabelStory> mLabelStoryList = null;
    public static final String VIEW_PAGER_LIST_INFO = "list_info";
    public final static String LABEL_STORY = "label_story";
    public final static String LABEL_STORY_SHOW= "label_story_show";
    public final static String LABEL_STORY_POSITION="label_story_position";
    private MyFragmentPageAdapter mAdapter = null;
    private RelativeLayout mBack;
    private Context mContext;
    private int mPosition;
    private int tag;
    private ImageView mShare;
    private ContentSharer mContentSharer;
    private String mCategoryName = null;
    private Stranger stranger;
    private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageSelected(int arg0) {
            // TODO Auto-generated method stub
            mPosition = arg0;

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_story_detail);
        BaseActivity baseActivity = this;
        ActionBar actionBar =getActionBar();
        if (actionBar != null){
            actionBar.hide();
        }
        baseActivity.setHasContentSharer();
        mContentSharer = baseActivity.getContentSharer();
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.softInputMode |= WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
        mContext = this;
        mViewPager = (ViewPager) findViewById(R.id.activity_detail);
        mBack = (RelativeLayout) findViewById(R.id.title_back_relative);
        mShare=(ImageView)findViewById(R.id.title_image_share);
        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareContent(new ShareContent(
                        getString(R.string.labelstory_item_share_gaveyout),
                        mLabelStoryList.get(mPosition).getContent(),
                        BitmapFactory.decodeResource(getResources(),
                        R.drawable.ap_icon_large),
                        getString(R.string.config_label_story_detail_url)
                                + mLabelStoryList.get(mPosition).getLabelStoryId(),
                        mLabelStoryList.get(mPosition).getLabelStoryId()));
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                putParam();
            }
        });
        mViewPager.setOnPageChangeListener(mPageChangeListener);
        argmentParam();
        initial();
    }
    private void shareContent(ShareContent content) {
        mContentSharer.setShareContent(content);
        mContentSharer.openSharePanel();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            putParam();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void getFragmentDate(){
       int count =  mAdapter.getCount();
       for (int i = 0;i < count; i++){
           LabelStoryDetaileFragment fragment = mAdapter.getItem(i);
           if (fragment.isChange){
               mLabelStoryList.remove(i);
               mLabelStoryList.add(i,fragment.mLabelStory);
           }

       }
    }

    private void argmentParam(){
        Intent intent =getIntent();
        Bundle bundle=intent.getExtras();
        mLabelStoryList=bundle.getParcelableArrayList(VIEW_PAGER_LIST_INFO);
        mPosition=bundle.getInt(LABEL_STORY_POSITION);
        mCategoryName = bundle.getString(LabelStoryUtils.CATEGORY_NAME);
        tag = bundle.getInt(LabelStoryUtils.TAG);
        stranger = bundle.getParcelable(LabelStoryUtils.STRANGER);
    }
    private void putParam(){
        Intent intent=new Intent();
        intent.putExtra(LABEL_STORY_POSITION,mPosition);
        if (mLabelStoryList!=null) {
            getFragmentDate();
            intent.putParcelableArrayListExtra(VIEW_PAGER_LIST_INFO,mLabelStoryList);
        }
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void initializeActionBar() {
        super.initializeActionBar();
    }

    private List<LabelStoryDetaileFragment> getDate() {
        List<LabelStoryDetaileFragment> contents = new ArrayList<>();
        for (int i = 0; i < mLabelStoryList.size(); i++) {
            LabelStory labelStory = mLabelStoryList.get(i);
            if (stranger != null){
                labelStory.setStranger(stranger);
            }
            LabelStoryDetaileFragment content = new LabelStoryDetaileFragment();
            Bundle args = new Bundle();
            args.putBoolean(LABEL_STORY_SHOW, true);
            args.putParcelable(LABEL_STORY, labelStory);
            args.putString(LabelStoryUtils.CATEGORY_NAME,mCategoryName);
            args.putInt(LabelStoryUtils.TAG,tag);
            content.setArguments(args);
            contents.add(content);
        }
        return contents;
    }

    private void initial() {
        List<LabelStoryDetaileFragment> contents = getDate();
        mAdapter=new MyFragmentPageAdapter(getSupportFragmentManager(), contents);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mPosition);
    }


    public class MyFragmentPageAdapter extends FragmentPagerAdapter {

        private final List<LabelStoryDetaileFragment> mContents;

        public MyFragmentPageAdapter(FragmentManager fm,
                                     List<LabelStoryDetaileFragment> contents) {
            super(fm);
            mContents = contents;
        }

        @Override
        public LabelStoryDetaileFragment getItem(int arg0) {
            return mContents.get(arg0);
        }

        @Override
        public int getCount() {
            return mContents.size();
        }
    }


}
