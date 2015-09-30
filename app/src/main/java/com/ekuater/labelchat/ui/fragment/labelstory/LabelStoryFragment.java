package com.ekuater.labelchat.ui.fragment.labelstory;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LabelStoryCategory;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserPraise;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.ContentSharer;
import com.ekuater.labelchat.ui.ShareContent;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.BaseActivity;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Label on 2015/1/4.
 *
 * @author Xu wenxiang
 */

public class LabelStoryFragment extends Fragment {

    public final static String LABEL = "label";
    public final static String LABEL_STORY = "label_story";
    private final static int LABEL_STORY_INFO = 101;
    private final static int LABEL_STORY_PRAISE = 102;
    private final static int LABEL_STORY_PRAISE_EXIT = 103;
    private final static int LABEL_STORY_NULL = 105;
    private static final int MSG_ADDING_LABEL_RESULT = 106;
    private static final int MSG_ADDING_LABEL_HOLD = 107;
    public static final int DETAIL_RESULT_CODE = 108;
    public static final int SEND_LABEL_STORY = 109;

    private CustomListView mPullToRefreshListView;
    private boolean isLoading = true;
    private LabelStoryManager mLabelStoryManager;
    private LabelStoryCategory category = null;
    private int indexPager = 0;
    private LabelStoryAdapter adapter;
    private Activity mContext;
    private boolean isPullRefresh = false;
    private RelativeLayout mLabelStoryRelative;
    private ImageView mLabelStoryLoading;
    private TextView mLabelStoryNoDateTv;
    private ImageView mImageSend;
    private ArrayList<LabelStory> mLabelStories = new ArrayList<LabelStory>();
    private UserLabelManager mUserLabelManager;
    private AvatarManager mAvatarManager;
    private LabelStoryUtils mLabelStoryUtils;
    private boolean isShowAnimotion = false;
    private PopupWindow mPopupwindow;
    private String letterMsg = null;
    private String queryUserId = null;

    private Handler storyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LABEL_STORY_INFO:
                    stopAnimation();
                    updateList((LabelStory[]) msg.obj);
                    break;

                case LABEL_STORY_PRAISE_EXIT:
                    ShowToast.makeText(mContext, R.drawable.emoji_sad, mContext.
                            getResources().getString(R.string.labelstory_input_praise_failed)).show();
                    break;
                case LABEL_STORY_NULL:
                    stopAnimation();
                    mLabelStoryNoDateTv.setVisibility(View.VISIBLE);
                    mPullToRefreshListView.setVisibility(View.INVISIBLE);
                    break;
                case LabelStoryUtils.FOLLOWING_REQUEST_CODE:
                    onFollowingHandler(msg);
                    break;
                case LabelStoryUtils.PRAISE_REQUEST_CODE:
                    praiseDate(msg);
                    break;
                case LabelStoryUtils.LETTER_REQUEST_CODE:
                    onLetterHandler(msg);
                    break;
                default:
                    break;
            }
        }
    };

    private void onLetterHandler(Message message) {
        switch (message.arg1) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                int position = message.arg2;
                LabelStoryUtils.insertSystemPush(PushMessageManager.getInstance(mContext), mLabelStories.get(position).getStranger(), letterMsg);
                ShowToast.makeText(mContext, R.drawable.emoji_smile, mContext.
                        getResources().getString(R.string.send_letter_succese)).show();
                break;
            default:
                ShowToast.makeText(mContext, R.drawable.emoji_cry, mContext.
                        getResources().getString(R.string.send_letter_failed)).show();
                break;

        }
    }

    private void onFollowingHandler(Message message) {
        switch (message.arg1) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                mLabelStories.get(message.arg2).setIsFollowing("Y");
                adapter.notifyDataSetChanged();
                break;
            default:
                ShowToast.makeText(mContext, R.drawable.emoji_cry, mContext.
                        getResources().getString(R.string.request_failure)).show();
                break;

        }
    }


    private ContentSharer mContentSharer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getActivity();

        BaseActivity baseActivity = (BaseActivity) mContext;
        baseActivity.setHasContentSharer();
        mContentSharer = baseActivity.getContentSharer();

        WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
        lp.softInputMode |= WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        mLabelStoryManager = LabelStoryManager.getInstance(getActivity());
        mUserLabelManager = UserLabelManager.getInstance(getActivity());
        mAvatarManager = AvatarManager.getInstance(getActivity());
        mLabelStoryUtils = new LabelStoryUtils(LabelStoryFragment.this, mContentSharer, storyHandler);
        isShowAnimotion = true;
        paramArguments();
        adapter = new LabelStoryAdapter(mContext, mOnClickListener, LabelStoryUtils.ONE, category.getmCategoryName());
        getDate();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DETAIL_RESULT_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = data;
                    ArrayList<LabelStory> labelStoryArrayList = intent.getParcelableArrayListExtra(LabelStoryDetailViewPagerActivity.VIEW_PAGER_LIST_INFO);
                    if (labelStoryArrayList != null) {
                        mLabelStories = labelStoryArrayList;
                        int position = intent.getIntExtra(LabelStoryDetailViewPagerActivity.LABEL_STORY_POSITION, 0);
                        adapter.updateAdapterArrayList(mLabelStories,LabelStoryUtils.REFRESH_DATA);
                        mPullToRefreshListView.setSelection(position);
                    }
                }
                break;

        }
    }

    private CustomListView.OnRefreshListener mOnRefreshListener
            = new CustomListView.OnRefreshListener() {
        @Override
        public void onRefresh() {
            isPullRefresh = true;
            indexPager = 0;
            getDate();
        }
    };

    private CustomListView.OnLoadMoreListener mOnLoadListener
            = new CustomListView.OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            if (isLoading) {
                isLoading = false;
                isPullRefresh = false;
                getDate();
            }
        }
    };
    public void showStrangerDetailUI(Stranger stranger) {
        if (stranger != null && !stranger.getUserId().equals(SettingHelper.getInstance(mContext).getAccountUserId())) {
            UILauncher.launchStrangerDetailUI(mContext, stranger);
        }
    }
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.story_item_praise:
                    int position = Integer.parseInt(v.getTag().toString());
                    LabelStory labelStory = mLabelStories.get(position);
                    praiseLabelStory(position, labelStory.getLabelStoryId());
                    break;
                case R.id.descript_tx:
                    if (v.getTag() != null) {
                        showStrangerDetailUI(mLabelStories.get((Integer) v.getTag()).getStranger());
                    }
                    break;
//                case R.id.story_item_label:
//                    UILauncher.launchFragmentLabelStoryUI(getActivity(), (LabelStoryCategory) v.getTag(), null);
//                    break;
                case R.id.label_input_send:
                    UILauncher.launchFragmentSendLabelStoryUI(mContext,SEND_LABEL_STORY);
                    break;
                case R.id.operation_bar_praise:
                    int praisePosition = (Integer)v.getTag();
                    LabelStory praiseLabelStory = mLabelStories.get(praisePosition);
                    mLabelStoryUtils.praise(praiseLabelStory.getLabelStoryId(),praisePosition);
                    break;
                case R.id.descript_following:
                    int i = (Integer)v.getTag();
                    LabelStory labelStory1 = mLabelStories.get(i);
                    if ("Y".equals(labelStory1.getIsFollowing())) {

                    }else{
                        mLabelStoryUtils.following(mLabelStories.get(i).getStranger().getUserId(), i);
                    }
                    break;
                case R.id.operation_bar_letter:
                    int letterPosition = (Integer)v.getTag();
                    LabelStory letterLabelStory = mLabelStories.get(letterPosition);
                    if (letterLabelStory.getStranger() != null) {
                        PrivateLetterFragmentDialog dialog = PrivateLetterFragmentDialog.newInstance(letterLabelStory.getLabelStoryId(), letterLabelStory.getStranger().getAvatarThumb(),
                                letterLabelStory.getStranger().getUserId(), letterLabelStory.getStranger().getShowName(),
                                mAvatarManager, letterPosition, onSendEmaileClicklistener);
                        dialog.show(getFragmentManager(), "PrivateLetterFragmentDialog");
                    }
                    break;
                case R.id.operation_bar_more:
                    showPopwindow(v);
                    break;
                case R.id.share:
                    int sharePosition = (Integer)v.getTag();
                    LabelStory shareLabelStory = mLabelStories.get(sharePosition);
                    mPopupwindow.dismiss();
                    shareContent(new ShareContent(
                            getString(R.string.labelstory_item_share_gaveyout),
                            shareLabelStory.getContent(),
                            BitmapFactory.decodeResource(getResources(),
                                    R.drawable.ap_icon_large),
                            getString(R.string.config_label_story_detail_url)
                                    + shareLabelStory.getLabelStoryId(),
                            shareLabelStory.getLabelStoryId()));
                    break;
                case R.id.report:
                    mPopupwindow.dismiss();
                    MiscUtils.complainDynamic(mContext, mLabelStories.get(
                            (Integer) v.getTag()).getLabelStoryId());
                    break;
                default:
                    break;
            }
        }
    };
    private void shareContent(ShareContent content) {
        mContentSharer.setShareContent(content);
        mContentSharer.openSharePanel();
    }
    public PrivateLetterFragmentDialog.OnSendEmaileClicklistener onSendEmaileClicklistener = new PrivateLetterFragmentDialog.OnSendEmaileClicklistener() {
        @Override
        public void onSendEmaile(String labelStoryId, String userId, String message, int position) {
            letterMsg = message;
            mLabelStoryUtils.sendLetter(labelStoryId, userId, message, position);
        }
    };
    public void showPopwindow(View v){
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View popupWindow = layoutInflater.inflate(R.layout.option, null);
        TextView report = (TextView) popupWindow.findViewById(R.id.report);
        TextView share = (TextView) popupWindow.findViewById(R.id.share);
        report.setOnClickListener(mOnClickListener);
        share.setOnClickListener(mOnClickListener);
        share.setTag(v.getTag());
        report.setTag(v.getTag());
        mPopupwindow = new PopupWindow(popupWindow, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        mPopupwindow.setFocusable(true);
        mPopupwindow.setOutsideTouchable(true);
        mPopupwindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupwindow.showAsDropDown(v);
    }
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    };
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position != 0) {
                LabelStory labelStory = mLabelStories.get(position - 1);
                if (!labelStory.getLabelStoryId().equals("0")) {
                    UILauncher.launchFragmentLabelStoryDetaileActivityUI(LabelStoryFragment.this, mLabelStories, position - 1,category.getmCategoryName(), LabelStoryUtils.ONE,DETAIL_RESULT_CODE);
                }
            }
        }
    };


    public void paramArguments() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            category = bundle.getParcelable(LabelStoryUtils.CATEGORY);
            queryUserId = bundle.getString(LabelStoryUtils.LABEL_STORY_USER_ID);
        }
        final ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    public void updateList(LabelStory[] labelStories) {
        List<LabelStory> list = Arrays.asList(labelStories);
        ArrayList<LabelStory> arrayList = new ArrayList<LabelStory>();
        arrayList.addAll(list);
        if (arrayList.size() < 20) {
            mPullToRefreshListView.setCanLoadMore(false);
        } else {
            mPullToRefreshListView.setCanLoadMore(true);
        }
        if (isPullRefresh) {
            mPullToRefreshListView.onRefreshComplete();
            if (mLabelStories != null && mLabelStories.size() > 0) {
                mLabelStories.clear();
            }
            mLabelStories = arrayList;
            adapter.updateAdapterArrayList(arrayList, LabelStoryUtils.REFRESH_DATA);
        } else {
            mPullToRefreshListView.onLoadMoreComplete();
            isLoading = true;
            mLabelStories.addAll(arrayList);
            adapter.updateAdapterArrayList(arrayList, LabelStoryUtils.LOADING_DADA);
        }
    }

    public void getDate() {
        indexPager++;
        LabelStoryManager.LabelStoryQueryObserver observer
                = new LabelStoryManager.LabelStoryQueryObserver() {
            @Override
            public void onQueryResult(int result, LabelStory[] labelStories, boolean remaining, int frendsCount) {
                if (result == LabelStoryManager.QUERY_RESULT_SUCCESS) {
                    if (labelStories != null) {
                        Message msg = storyHandler.obtainMessage(LABEL_STORY_INFO, labelStories);
                        storyHandler.sendMessage(msg);
                    } else {
                        if (indexPager == 1) {
                            Message msg = storyHandler.obtainMessage(LABEL_STORY_NULL);
                            storyHandler.sendMessage(msg);
                        }
                    }
                }
            }

            @Override
            public void onPraiseQueryResult(int result, boolean remaining) {
            }
        };
        mLabelStoryManager.accessLabelStoryInfo(category.getmCategoryId(), String.valueOf(indexPager), queryUserId, observer);
    }


    private void praiseDate(Message msg) {
        if (msg.arg1 == LabelStoryManager.QUERY_RESULT_SUCCESS) {
            int position = msg.arg2;
            LabelStory labelStory = mLabelStories.get(position);
            ArrayList<UserPraise> arrayList = new ArrayList<>();
            if (labelStory.getUserPraise() != null) {
                arrayList.addAll(Arrays.asList(labelStory.getUserPraise()));
            }
            int number = 0;
            if (labelStory.getIsPraise().equals("N")) {
                number = Integer.parseInt(mLabelStories.get(position).getPraise()) + 1;
                mLabelStories.get(position).setIsPraise("Y");
                mLabelStories.get(position).setPraise(number + "");
                arrayList.add(0, new UserPraise(SettingHelper.getInstance(getActivity()).getAccountUserId(),
                        SettingHelper.getInstance(getActivity()).getAccountNickname(),
                        SettingHelper.getInstance(getActivity()).getAccountAvatarThumb(),0));
                UserPraise[] userPraises = new UserPraise[arrayList.size()];
                mLabelStories.get(position).setUserPraise(arrayList.toArray(userPraises));
            } else {
                number = Integer.parseInt(mLabelStories.get(position).getPraise()) - 1;
                mLabelStories.get(position).setIsPraise("N");
                mLabelStories.get(position).setPraise(number + "");
                if (arrayList.size() == 1) {
                    mLabelStories.get(position).setUserPraise(null);
                } else {
                    for (int i = 0; i < arrayList.size(); i++) {
                        if (arrayList.get(i).getmPraiseUserId().equals(SettingHelper.getInstance(getActivity()).getAccountUserId())) {
                            arrayList.remove(arrayList.get(i));
                            UserPraise[] userPraises = new UserPraise[arrayList.size()];
                            mLabelStories.get(position).setUserPraise(arrayList.toArray(userPraises));
                            break;
                        }
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void praiseLabelStory(int position, String labelStoryId) {
        final int mPosition = position;
        LabelStoryManager.LabelStoryQueryObserver observer = new LabelStoryManager.LabelStoryQueryObserver() {
            @Override
            public void onQueryResult(int result, LabelStory[] labelStories, boolean remaining, int frendsCount) {

            }

            @Override
            public void onPraiseQueryResult(int result, boolean remaining) {
                if (result == LabelStoryManager.QUERY_RESULT_SUCCESS) {
                    Message msg = storyHandler.obtainMessage(LABEL_STORY_PRAISE, mPosition);
                    storyHandler.sendMessage(msg);
                } else if (result == LabelStoryManager.QUERY_RESULT_EXIT_PRAISE) {
                    Message msg = storyHandler.obtainMessage(LABEL_STORY_PRAISE_EXIT);
                    storyHandler.sendMessage(msg);
                }
            }
        };
		//TODO
        mLabelStoryManager.praiseLabelStory(labelStoryId,null, observer);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_labelstory_all, container, false);
        view.findViewById(R.id.labelstory_fragment_all_driver).setVisibility(View.GONE);
        FrameLayout actionBar = (FrameLayout) view.findViewById(R.id.actionbar);
        actionBar.setVisibility(category == null ? View.GONE : View.VISIBLE);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView title = (TextView) view.findViewById(R.id.title);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        title.setText(category.getmCategoryName());
        mPullToRefreshListView = (CustomListView) view.findViewById(R.id.labelstory_fragment_all_listview);
        RelativeLayout labelStoryRelative = (RelativeLayout) view.findViewById(R.id.labelstory_fragment_all_relative);
        mImageSend = (ImageView) view.findViewById(R.id.label_input_send);
        mLabelStoryRelative = (RelativeLayout) view.findViewById(R.id.labelstory_fragment_all_linear);
        mLabelStoryLoading = (ImageView) view.findViewById(R.id.labelstory_fragment_all_loading);
        mLabelStoryNoDateTv = (TextView) view.findViewById(R.id.labelstory_fragment_all_nodate);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.labelstory_input_all_layout);
        linearLayout.setOnClickListener(mOnClickListener);
        mImageSend.setOnClickListener(mOnClickListener);
        mPullToRefreshListView.setAdapter(adapter);
        linearLayout.setVisibility(View.VISIBLE);
        mPullToRefreshListView.setOnItemClickListener(onItemClickListener);
        mPullToRefreshListView.setOnRefreshListener(mOnRefreshListener);
        mPullToRefreshListView.setOnLoadListener(mOnLoadListener);
        labelStoryRelative.setOnTouchListener(mOnTouchListener);
        if (isShowAnimotion) {
            isShowAnimotion = false;
            startAnimation();
        }
        return view;
    }


    private void startAnimation() {
        mLabelStoryLoading.setVisibility(View.VISIBLE);
        mLabelStoryRelative.setVisibility(View.GONE);
        Drawable drawable = mLabelStoryLoading.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.start();
    }

    private void stopAnimation() {
        mLabelStoryLoading.setVisibility(View.GONE);
        mLabelStoryRelative.setVisibility(View.VISIBLE);
        Drawable drawable = mLabelStoryLoading.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.stop();
    }
}

