package com.ekuater.labelchat.ui.fragment.mixdynamic;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.ConfideComment;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.Dynamic.DynamicResultEvent;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LabelStoryComments;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserPraise;
import com.ekuater.labelchat.datastruct.confide.ConfidePublishEvent;
import com.ekuater.labelchat.datastruct.mixdynamic.DynamicType;
import com.ekuater.labelchat.datastruct.mixdynamic.DynamicWrapper;
import com.ekuater.labelchat.datastruct.mixdynamic.WrapperUtils;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ConfideManager;
import com.ekuater.labelchat.delegate.FollowingManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.delegate.QueryResult;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.ContentSharer;
import com.ekuater.labelchat.ui.ShareContent;
import com.ekuater.labelchat.ui.UIEventBusHub;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.BaseActivity;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.fragment.confide.ConfideUtils;
import com.ekuater.labelchat.ui.fragment.friends.StrangerHelper;
import com.ekuater.labelchat.ui.fragment.labelstory.CustomListView;
import com.ekuater.labelchat.ui.fragment.labelstory.DynamicArguments;
import com.ekuater.labelchat.ui.fragment.labelstory.LabelStoryUtils;
import com.ekuater.labelchat.ui.fragment.labelstory.PrivateLetterFragmentDialog;
import com.ekuater.labelchat.ui.fragment.mixdynamic.audioplay.AudioPlayMediator;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.util.GeocodeSearcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Leo on 2015/4/16.
 *
 * @author LinYong
 */
public class MixDynamicAllFragment extends Fragment implements Handler.Callback,
        CustomListView.OnRefreshListener, CustomListView.OnLoadMoreListener {

    private static final int REFRESH = 0;
    private static final int LOADING = 1;

    private static final int MSG_QUERY_RESULT = 100;
    private static final int MSG_DELETE_STORY = 101;
    private static final int MSG_DELETE_CONFIDE = 102;

    private static final int INPUT_COMMENT_REQUEST = 103;
    private static final int COMMENT_RESULT_CODE = 104;
    private static final int MSG_SEARCH_ADDRESS_RESULT = 105;
    private static final int MSG_SEARCH_ADDRESS = 106;

    private enum UiState {
        CREATED,
        VIEW_CREATED,
        VIEW_DESTROYED,
        DESTROYED
    }

    private DynamicConfig mDynamicConfig;
    private ConfideManager mConfideManager;
    private LabelStoryManager mStoryManager;
    private AvatarManager mAvatarManager;
    private SettingHelper mSettingHelper;
    private StrangerHelper mStrangerHelper;
    private String mMyUserId;
    private MixDynamicAdapter mAdapter;
    private Handler mHandler;
    private ContentSharer mContentSharer;
    private LabelStoryUtils mLabelStoryUtils;
    private SimpleProgressHelper mProgressHelper;
    private EventBus mUIEventBus;
    private AudioPlayMediator mAudioPlayMediator;
    private UiState mUiState;

    private CustomListView mListView;
    private TextView mEmptyText;
    private ImageView mLoadingView;
    private int mRequestTime = 1;
    private boolean mLoading;
    private String letterMsg = null;
    private Stranger letterStranger;
    private PopupWindow mMorePopup;
    private LabelStory mMorePopupStory;
    public boolean tag = false;
    private MixComment mixComment;
    private String position;
    private String shareContent;
    private String shareContentId;
    private String shareContentUrl;
    private GeocodeSearcher geocodeSearcher;

    private final TotalDynamicListener mDynamicListener = new TotalDynamicListener() {

        @Override
        public void onConfideItemClick(Confide confide, boolean isShowSoft, int position) {
            UILauncher.launchConfideDetaileUI(MixDynamicAllFragment.this,
                    confide, ConfideUtils.CONFIDE_SHOW_CODE, position, isShowSoft);
        }

        @Override
        public boolean onConfideItemLongClick(Confide confide, int position) {
            if (mDynamicConfig.needDeleteDynamic()) {
                onDeleteConfide(confide, position);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onConfidePraise(Confide confide, int position) {
            if (!TextUtils.isEmpty(confide.getConfideId())) {
                praiseConfide(confide);
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onConfideComment(int position) {
            mListView.setSelection(position + 1);
            mixComment = new MixComment();
            mixComment.setPosition(position);
            mixComment.setGoup(true);
            UILauncher.launchInputCommentUI(MixDynamicAllFragment.this, COMMENT_RESULT_CODE);
        }

        @Override
        public void onConfideChildComment(String replyName, int position, int childPosition) {
            mixComment = new MixComment();
            mixComment.setPosition(position);
            mixComment.setChildPosition(childPosition);
            mixComment.setGoup(false);
            UILauncher.launchInputCommentUI(MixDynamicAllFragment.this, replyName, COMMENT_RESULT_CODE);
        }

        @Override
        public void onConfideCommentTxClick(String userId) {
            mStrangerHelper.showStranger(userId);
        }

        @Override
        public void onStoryClick(LabelStory story, int position) {
            mixComment = new MixComment();
            mixComment.setPosition(position);
            DynamicArguments arguments = new DynamicArguments();
            arguments.setLabelStory(story);
            arguments.setIsShowFragment(true);
            arguments.setIsShowTitle(true);
            arguments.setTag(LabelStoryUtils.MIX);
            UILauncher.launchFragmentLabelStoryDetaileUI(getActivity(), arguments);
        }

        @Override
        public boolean onStoryLongClick(LabelStory story, int position) {
            if (mDynamicConfig.needDeleteDynamic()) {
                onDeleteStory(story, position);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onStoryPraiseClick(LabelStory story, int position) {
            praiseStory(story, position);
        }

        @Override
        public void onStoryLetterClick(LabelStory story, int position) {
            onSendPrivateLetter(story, position);
        }

        @Override
        public void onStoryAvatarClick(LabelStory story, int position) {
            showStrangerDetailUI(story.getStranger());
        }

        @Override
        public void onStoryFollowingClick(LabelStory story, int position) {
            followingStoryAuthor(story, position);
        }

        @Override
        public void onStoryMoreClick(LabelStory story, int position, View v) {
            shareContent = story.getContent();
            shareContentId = story.getLabelStoryId();
            shareContentUrl = getString(R.string.config_label_story_detail_url);
            showMorePopup(v);
        }

        @Override
        public void onStoryComment(int position, LabelStory story) {
            if (mDynamicConfig.loadComments()) {
                mixComment = new MixComment();
                mixComment.setPosition(position);
                mixComment.setGoup(true);
                UILauncher.launchInputCommentUI(MixDynamicAllFragment.this, COMMENT_RESULT_CODE);
            } else {
                DynamicArguments arguments = new DynamicArguments();
                arguments.setLabelStory(story);
                arguments.setIsShowFragment(true);
                arguments.setIsShowTitle(true);
                arguments.setTag(LabelStoryUtils.MIX);
                arguments.setIsShowKeyBroad(true);
                UILauncher.launchFragmentLabelStoryDetaileUI(getActivity(), arguments);
            }
        }

        @Override
        public void onStoryChildComment(String replyName, int position, int childPosition) {
            mixComment = new MixComment();
            mixComment.setPosition(position);
            mixComment.setChildPosition(childPosition);
            mixComment.setGoup(false);
            UILauncher.launchInputCommentUI(MixDynamicAllFragment.this, replyName, COMMENT_RESULT_CODE);
        }

        @Override
        public void onStoryCommentTxClick(String userId) {
            mStrangerHelper.showStranger(userId);
        }

        @Override
        public void onStoryImageClick(String imageUrl) {
            if (imageUrl != null) {
                UILauncher.launchLabelStoryShowPhotoUI(getActivity(), imageUrl);
            }
        }

        @Override
        public void onStoryMoreImageClick(String[] imageUrl, int position) {
            if (imageUrl != null) {
                UILauncher.launchImageGalleryUI(getActivity(), imageUrl, position);
            }
        }

        @Override
        public void onConfideMoerClick(Confide confide, int position, View v) {
            shareContent = confide.getConfideContent();
            shareContentId = confide.getConfideId();
            shareContentUrl = getString(R.string.config_confide_detail_url);
            showMorePopup(v);
        }
    };
    private final PrivateLetterFragmentDialog.OnSendEmaileClicklistener mPrivateLetterListener
            = new PrivateLetterFragmentDialog.OnSendEmaileClicklistener() {
        @Override
        public void onSendEmaile(String labelStoryId, String userId, String message, int position) {
            letterMsg = message;
            mLabelStoryUtils.sendLetter(labelStoryId, userId, message, position);
        }
    };
    private final View.OnClickListener mMorePopupListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.share:
                    mMorePopup.dismiss();
                    shareContent(new ShareContent(
                            getString(R.string.labelstory_item_share_gaveyout),
                            shareContent,
                            BitmapFactory.decodeResource(getResources(),
                                    R.drawable.ap_icon_large),
                            shareContentUrl
                                    + shareContentId,
                            shareContentId));
                    break;
                case R.id.report:
                    mMorePopup.dismiss();
                    MiscUtils.complainDynamic(v.getContext(), shareContentId);
                    break;
                default:
                    break;
            }
        }
    };
    private final Handler.Callback mStoryUtilsCb = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean handled = true;

            switch (msg.what) {
                case LabelStoryUtils.FOLLOWING_REQUEST_CODE:
                    onFollowingHandler(msg);
                    break;
                case LabelStoryUtils.PRAISE_REQUEST_CODE:
                    praiseDate(msg);
                    break;
                case LabelStoryUtils.LETTER_REQUEST_CODE:
                    onLetterHandler(msg);
                    break;
                case LabelStoryUtils.COMMENT_REQUEST_CODE:
                    onCommentHandler(msg);
                    break;
                case LabelStoryUtils.CONFIDE_COMMENT_CODE:
                    handlerAddConfideComment(msg);
                    break;
                default:
                    handled = false;
                    break;
            }
            return handled;
        }
    };
    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        ActionBar actionBar = activity.getActionBar();

        BaseActivity baseActivity = (BaseActivity) activity;
        baseActivity.setHasContentSharer();
        parseArguments(activity);
        mContentSharer = baseActivity.getContentSharer();
        mConfideManager = ConfideManager.getInstance(activity);
        mStoryManager = LabelStoryManager.getInstance(activity);
        mAvatarManager = AvatarManager.getInstance(activity);
        mSettingHelper = SettingHelper.getInstance(activity);
        mStrangerHelper = new StrangerHelper(this);
        mMyUserId = SettingHelper.getInstance(activity).getAccountUserId();
        EventBus eventBus;
        if (mDynamicConfig.loadComments()) {
            eventBus = new EventBus();
            mAudioPlayMediator = new AudioPlayMediator(activity, eventBus);
        } else {
            mAudioPlayMediator = null;
            eventBus = null;
        }
        mAdapter = new MixDynamicAdapter(activity, mDynamicConfig.loadComments(),
                eventBus, mDynamicListener);
        mHandler = new Handler(this);
        mLabelStoryUtils = new LabelStoryUtils(MixDynamicAllFragment.this, mContentSharer,
                new Handler(mStoryUtilsCb));
        mProgressHelper = new SimpleProgressHelper(this);
        mUIEventBus = UIEventBusHub.getDefaultEventBus();

        if (mDynamicConfig.needShowTitle() && actionBar != null) {
            actionBar.hide();
        }

        if (mDynamicConfig.monitorSentDynamic()) {
            mUIEventBus.register(this);
        }
        if (mAudioPlayMediator != null) {
            mAudioPlayMediator.init();
        }
        mLoading = true;
        geocodeSearcher = GeocodeSearcher.getInstance(activity);
        if (mDynamicConfig.loadComments()) {
            searchAddress();
        }
        mUiState = UiState.CREATED;
        onRefresh();
    }

    private void searchAddress() {
        LocationInfo location = AccountManager.getInstance(activity).getLocation();
        if (location == null) {
            mHandler.sendEmptyMessageDelayed(MSG_SEARCH_ADDRESS, 2000);
            return;
        }

        geocodeSearcher.searchAddress(location, new GeocodeSearcher.AddressObserver() {
            @Override
            public void onSearch(boolean success, GeocodeSearcher.SearchAddress address) {
                mHandler.obtainMessage(MSG_SEARCH_ADDRESS_RESULT,
                        success ? 1 : 0, 0, address).sendToTarget();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUIEventBus.isRegistered(this)) {
            mUIEventBus.unregister(this);
        }
        if (mAudioPlayMediator != null) {
            mAudioPlayMediator.deInit();
        }
        mHandler.removeMessages(MSG_SEARCH_ADDRESS);
        mUiState = UiState.DESTROYED;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mix_dynamic, container, false);
        View titleBar = rootView.findViewById(R.id.title_bar);
        if (mDynamicConfig.needShowTitle()) {
            ImageView icon = (ImageView) rootView.findViewById(R.id.icon);
            TextView title = (TextView) rootView.findViewById(R.id.title);
            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            title.setText(mDynamicConfig.getTitle());
        } else {
            titleBar.setVisibility(View.GONE);
        }

        mListView = (CustomListView) rootView.findViewById(R.id.list);
        mEmptyText = (TextView) rootView.findViewById(R.id.empty_tip);
        mLoadingView = (ImageView) rootView.findViewById(R.id.loading);
        mListView.setOnRefreshListener(this);
        mListView.setOnLoadListener(this);
        mListView.setAdapter(mAdapter);
        mListView.setRecyclerListener(mAdapter);
        mEmptyText.setText(mDynamicConfig.getNoDataTip());
        mLoadingView.setVisibility(mLoading ? View.VISIBLE : View.GONE);
        mUiState = UiState.VIEW_CREATED;
        updateEmptyTextVisibility();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUiState = UiState.VIEW_DESTROYED;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ConfideUtils.CONFIDE_SHOW_CODE:
                resultConfideDetail(resultCode, data);
                break;

            case COMMENT_RESULT_CODE:
                mHandler.obtainMessage(INPUT_COMMENT_REQUEST, resultCode, 0, data).sendToTarget();
                break;
            default:
                break;
        }
    }

    private void updateEmptyTextVisibility() {
        if (mEmptyText != null) {
            mEmptyText.setVisibility((!mLoading && mAdapter.getCount() <= 0)
                    ? View.VISIBLE : View.GONE);
        }
    }

    private void handleSearchAddressResult(boolean success,
                                           GeocodeSearcher.SearchAddress address) {
        if (success && address != null) {
            position = TextUtils.isEmpty(address.city) ? address.province : address.city;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_QUERY_RESULT:
                handleQueryMixStory(msg.arg1, msg.arg2, (DynamicWrapper[]) msg.obj);
                break;
            case MSG_DELETE_STORY:
                handleDeleteStory(msg.arg1, msg.arg2, (LabelStory) msg.obj);
                break;
            case MSG_DELETE_CONFIDE:
                handleDeleteConfide(msg.arg1, msg.arg2, (Confide) msg.obj);
                break;
            case INPUT_COMMENT_REQUEST:
                if (msg.arg1 == Activity.RESULT_OK) {
                    Intent data = (Intent) msg.obj;
                    if (data != null) {
                        mLabelStoryUtils.showProgressDialog();
                        resultInputCommnet(msg.arg1, data);
                    }
                }
                break;
            case MSG_SEARCH_ADDRESS_RESULT:
                handleSearchAddressResult(msg.arg1 != 0,
                        (GeocodeSearcher.SearchAddress) msg.obj);
                break;
            case MSG_SEARCH_ADDRESS:
                searchAddress();
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    @Override
    public void onRefresh() {
        mRequestTime = 1;
        queryMixDynamic(REFRESH);
    }

    @Override
    public void onLoadMore() {
        queryMixDynamic(LOADING);
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(ConfidePublishEvent event) {
        Confide confide = event.getConfide();
        if (confide != null) {
            mAdapter.addNewWrapper(WrapperUtils.fromConfide(confide));
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(DynamicResultEvent event) {
        LabelStory labelStory = event.getLabelStory();
        if (labelStory != null && mixComment != null) {
            mAdapter.getItem(mixComment.getPosition()).setDynamic(labelStory);
            changeFollowState(labelStory);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void changeFollowState(LabelStory labelStory) {
        for (int i = 0; i < mAdapter.getCount(); i++) {
            DynamicWrapper dynamicWrapper = mAdapter.getItem(i);
            if (dynamicWrapper.getType() == DynamicType.TXT || dynamicWrapper.getType() == DynamicType.AUDIO || dynamicWrapper.getType() == DynamicType.BANKNOTE) {
                LabelStory story = (LabelStory) dynamicWrapper.getDynamic();
                if (story.getStranger() == null || labelStory.getStranger() == null) {
                    break;
                }
                if (story.getStranger().getUserId().equals(labelStory.getStranger().getUserId())) {
                    story.setIsFollowing(labelStory.getIsFollowing());
                    dynamicWrapper.setDynamic(story);
                }
            }

        }
    }

    private void parseArguments(Context context) {
        Bundle args = getArguments();
        DynamicScenario scenario = (args == null) ? DynamicScenario.GLOBAL
                : DynamicScenario.fromInt(args.getInt(MixDynamicArgs.ARGS_SCENARIO_TYPE, -1));

        if (scenario == null) {
            scenario = DynamicScenario.GLOBAL;
        }
        mDynamicConfig = scenario.getConfig(context, args);
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void shareContent(ShareContent content) {
        mContentSharer.setShareContent(content);
        mContentSharer.openSharePanel();
    }

    private void queryMixDynamic(final int flag) {
        mDynamicConfig.queryMixDynamic(mRequestTime, new QueryListener() {
            @Override
            public void onQueryResult(int result, DynamicWrapper[] wrappers) {
                mHandler.obtainMessage(MSG_QUERY_RESULT, result, flag, wrappers)
                        .sendToTarget();
            }
        });
    }

    private void handleQueryMixStory(int result, int flag, DynamicWrapper[] wrappers) {
        mLoading = false;

        switch (mUiState) {
            case CREATED: // View not ready, delay
                mHandler.obtainMessage(MSG_QUERY_RESULT, result, flag, wrappers)
                        .sendToTarget();
                return;
            case VIEW_CREATED:
                break;
            default: // View destroyed, skip it
                return;
        }

        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.GONE);
        }

        List<DynamicWrapper> wrapperList = new ArrayList<>();
        int length = 0;

        if (result == QueryResult.RESULT_SUCCESS) {
            if (wrappers != null) {
                Collections.addAll(wrapperList, wrappers);
                length = wrapperList.size();
            }
            if (length >= 20) {
                mListView.setCanLoadMore(true);
                ++mRequestTime;
            } else {
                mListView.setCanLoadMore(false);
            }
        }

        switch (flag) {
            case REFRESH:
                mListView.onRefreshComplete();
                if (length > 0) {
                    mAdapter.updateWrapperList(wrapperList);
                }
                break;
            case LOADING:
                mListView.onLoadMoreComplete();
                if (length > 0) {
                    mAdapter.addWrapperList(wrapperList);
                }
                break;
            default:
                break;
        }
        updateEmptyTextVisibility();
    }

    private void resultInputCommnet(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String content = data.getStringExtra(LabelStoryUtils.CONTENT);
            boolean group = data.getBooleanExtra(LabelStoryUtils.IS_GROUP, false);
            if (mixComment != null) {
                mixComment.setGoup(group);
                checkCategory(content);
            }
        }
    }

    private void checkCategory(String content) {
        DynamicWrapper dynamic = mAdapter.getItem(mixComment.getPosition());

        switch (DynamicType.toType(dynamic.getType().getType())) {
            case TXT:
            case AUDIO:
            case BANKNOTE:
                storyComment(dynamic, content);
                break;
            case CONFIDE: {
                Confide confide = (Confide) dynamic.getDynamic();
                ConfideComment comment;

                if (mixComment.isGoup()) {
                    comment = new ConfideComment();
                    comment.setComment(content + " ");
                    comment.setPosition(position);
                    comment.setConfideId(confide.getConfideId());
                } else {
                    comment = new ConfideComment(confide.getConfideComments()[
                            mixComment.getChildPosition()]);
                    comment.setReplyComment(comment.getComment() + " ");
                    comment.setComment(content);
                    comment.setReplyFloor(comment.getFloor());
                }
                mLabelStoryUtils.addConfideComment(comment);
                break;
            }
            default:
                break;
        }
    }

    private void storyComment(DynamicWrapper dynamic, String content) {
        LabelStory story = (LabelStory) dynamic.getDynamic();
        LabelStoryComments[] comments = story.getLabelStoryComments() == null
                ? null : story.getLabelStoryComments();
        List<String> userIds = mLabelStoryUtils.getUserIds(story, comments);
        LabelStoryComments comment = new LabelStoryComments();

        if (mixComment.isGoup()) {
            comment.setmLabelStoryId(story.getLabelStoryId());
            comment.setmStoryComment(content);
            comment.setmArrayUserId(userIds);
        } else {
            if (comments != null) {
                LabelStoryComments commentChild = comments[mixComment.getChildPosition()];
                Stranger stranger = new Stranger();
                stranger.setUserId(mSettingHelper.getAccountUserId());
                stranger.setNickname(mSettingHelper.getAccountNickname());
                stranger.setAvatarThumb(mSettingHelper.getAccountAvatarThumb());
                comment.setmLabelStoryId(story.getLabelStoryId());
                comment.setmReplyUserId(commentChild.getmStranger().getUserId());
                comment.setmReplyNickName(commentChild.getmStranger().getNickname());
                comment.setmParentCommentId(commentChild.getmStroyCommentId());
                comment.setmStranger(stranger);
                comment.setmStoryComment(content);
                comment.setmCreateDate(System.currentTimeMillis());
                comment.setmArrayUserId(userIds);
            }
        }
        mLabelStoryUtils.comment(comment);
    }

    private void resultConfideDetail(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Confide confide = data.getParcelableExtra(ConfideUtils.CONFIDE);
            int index = data.getIntExtra(ConfideUtils.CONFIDE_INDEX, -1);

            if (index < 0 || index >= mAdapter.getCount()) {
                return;
            }

            DynamicWrapper wrapper = mAdapter.getItem(index);
            Confide oldConfide = wrapper.getType() == DynamicType.CONFIDE
                    ? (Confide) wrapper.getDynamic() : null;

            if (confide != null && oldConfide != null
                    && confide.getConfideId().equals(oldConfide.getConfideId())) {
                wrapper.setDynamic(confide);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void praiseConfide(Confide confide) {
        mConfideManager.praiseConfide(confide.getConfideId(), null);
        if ("Y".equals(confide.getConfideIsPraise())) {
            confide.setConfideIsPraise("N");
            confide.setConfidePraiseNum(confide.getConfidePraiseNum() - 1);
        } else {
            confide.setConfideIsPraise("Y");
            confide.setConfidePraiseNum(confide.getConfidePraiseNum() + 1);
        }
    }

    private void showStrangerDetailUI(Stranger stranger) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if (stranger != null && !stranger.getUserId().equals(mMyUserId)) {
            mStrangerHelper.showStranger(stranger.getUserId());
        } else {
            UILauncher.launchMyInfoUI(activity);
        }
    }

    private void onSendPrivateLetter(LabelStory labelStory, int position) {
        if (labelStory == null || labelStory.getStranger() == null) {
            return;
        }

        letterStranger = labelStory.getStranger();
        PrivateLetterFragmentDialog dialog = PrivateLetterFragmentDialog.newInstance(
                labelStory.getLabelStoryId(),
                letterStranger.getAvatarThumb(),
                letterStranger.getUserId(),
                letterStranger.getShowName(),
                mAvatarManager,
                position,
                mPrivateLetterListener);
        dialog.show(getFragmentManager(), "PrivateLetterFragmentDialog");
    }

    private void onCommentHandler(Message message) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        mLabelStoryUtils.dismissProgressDialog();
        switch (message.arg1) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                LabelStoryComments comments = (LabelStoryComments) message.obj;
                Stranger stranger = new Stranger();
                stranger.setUserId(mSettingHelper.getAccountUserId());
                stranger.setNickname(mSettingHelper.getAccountNickname());
                stranger.setAvatarThumb(mSettingHelper.getAccountAvatarThumb());

                if (comments != null) {
                    comments.setmStranger(stranger);
                    DynamicWrapper wrapper = mAdapter.getItem(mixComment.getPosition());
                    LabelStory story = (LabelStory) wrapper.getDynamic();
                    LabelStoryComments[] labelStoryCommentse;

                    if (story.getLabelStoryComments() == null) {
                        labelStoryCommentse = new LabelStoryComments[1];
                        labelStoryCommentse[0] = comments;
                        story.setLabelStoryComments(labelStoryCommentse);
                    } else {
                        labelStoryCommentse = story.getLabelStoryComments();
                        ArrayList<LabelStoryComments> list = new ArrayList<>();
                        list.addAll(Arrays.asList(labelStoryCommentse));
                        list.add(comments);
                        int size = list.size();
                        story.setLabelStoryComments(list.toArray(new LabelStoryComments[size]));
                    }
                    int commentNum = Integer.parseInt(story.getCommentNum()) + 1;
                    story.setCommentNum(String.valueOf(commentNum));
                    mAdapter.notifyDataSetChanged();
                }
                ShowToast.makeText(activity, R.drawable.emoji_smile,
                        getString(R.string.labelstroy_input_comment_succse)).show();
                break;
            default:
                ShowToast.makeText(activity, R.drawable.emoji_cry,
                        getString(R.string.labelstroy_input_comment_faile)).show();
                break;

        }
        mixComment = null;
    }

    private void handlerAddConfideComment(Message msg) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        mLabelStoryUtils.dismissProgressDialog();
        if (msg.arg1 == ConstantCode.EXECUTE_RESULT_SUCCESS) {
            ConfideComment addComment = (ConfideComment) msg.obj;
            if (addComment != null) {
                DynamicWrapper wrapper = mAdapter.getItem(mixComment.getPosition());
                Confide confide = (Confide) wrapper.getDynamic();
                confide.setConfideCommentNum(confide.getConfideCommentNum() + 1);
                ConfideComment[] confideComments;

                if (confide.getConfideComments() == null) {
                    confideComments = new ConfideComment[1];
                    confideComments[0] = addComment;
                    confide.setConfideComments(confideComments);
                } else {
                    confideComments = confide.getConfideComments();
                    ArrayList<ConfideComment> list = new ArrayList<>();
                    list.addAll(Arrays.asList(confideComments));
                    list.add(addComment);
                    int size = list.size();
                    confide.setConfideComments(list.toArray(new ConfideComment[size]));
                }
                mAdapter.notifyDataSetChanged();
                ShowToast.makeText(activity, R.drawable.emoji_smile,
                        getString(R.string.labelstroy_input_comment_succse)).show();
            }
        } else {
            ShowToast.makeText(activity, R.drawable.emoji_cry, activity.
                    getResources().getString(R.string.labelstroy_input_comment_faile)).show();
        }
        mixComment = null;
    }

    private void onLetterHandler(Message message) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        switch (message.arg1) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                DynamicWrapper wrapper = mAdapter.getItem(message.arg2);
                LabelStory story = (LabelStory) wrapper.getDynamic();
                story.setLetterNum(story.getLetterNum() + 1);
                mAdapter.notifyDataSetChanged();
                LabelStoryUtils.insertSystemPush(PushMessageManager.getInstance(activity),
                        letterStranger, letterMsg);
                ShowToast.makeText(activity, R.drawable.emoji_smile,
                        getString(R.string.send_letter_succese)).show();
                break;
            default:
                ShowToast.makeText(activity, R.drawable.emoji_cry,
                        getString(R.string.send_letter_failed)).show();
                break;
        }
    }

    private void praiseStory(LabelStory story, int position) {
        mLabelStoryUtils.praise(story.getLabelStoryId(), position, story);
    }

    private void praiseDate(Message msg) {
        if (msg.arg1 != LabelStoryManager.QUERY_RESULT_SUCCESS) {
            return;
        }

        LabelStory labelStory = (LabelStory) msg.obj;
        ArrayList<UserPraise> arrayList = new ArrayList<>();

        if (labelStory.getUserPraise() != null) {
            arrayList.addAll(Arrays.asList(labelStory.getUserPraise()));
        }

        if (labelStory.getIsPraise().equals("N")) {
            int number = Integer.parseInt(labelStory.getPraise()) + 1;
            labelStory.setIsPraise("Y");
            labelStory.setPraise(number + "");
            arrayList.add(0, new UserPraise(mMyUserId,
                    mSettingHelper.getAccountNickname(),
                    mSettingHelper.getAccountAvatarThumb(), 0));
            UserPraise[] userPraises = new UserPraise[arrayList.size()];
            labelStory.setUserPraise(arrayList.toArray(userPraises));
        } else {
            int number = Integer.parseInt(labelStory.getPraise()) - 1;
            labelStory.setIsPraise("N");
            labelStory.setPraise(number + "");
            if (arrayList.size() == 1) {
                labelStory.setUserPraise(null);
            } else {
                for (int i = 0; i < arrayList.size(); i++) {
                    if (arrayList.get(i).getmPraiseUserId().equals(mMyUserId)) {
                        arrayList.remove(arrayList.get(i));
                        UserPraise[] userPraises = new UserPraise[arrayList.size()];
                        labelStory.setUserPraise(arrayList.toArray(userPraises));
                        break;
                    }
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void showMorePopup(View anchor) {
        if (mMorePopup == null) {
            setupMorePopup();
        }
        mMorePopup.showAsDropDown(anchor);
    }

    private void setupMorePopup() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams")
        View contentView = inflater.inflate(R.layout.option, null);
        TextView report = (TextView) contentView.findViewById(R.id.report);
        TextView share = (TextView) contentView.findViewById(R.id.share);

        report.setOnClickListener(mMorePopupListener);
        share.setOnClickListener(mMorePopupListener);
        mMorePopup = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mMorePopup.setFocusable(true);
        mMorePopup.setOutsideTouchable(true);
        mMorePopup.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
    }

    private void followingStoryAuthor(LabelStory story, int position) {
        Stranger stranger = story.getStranger();
        String userId = stranger != null ? stranger.getUserId() : null;

        if (TextUtils.isEmpty(userId) || "Y".equals(story.getIsFollowing())) {
            return;
        }
        mLabelStoryUtils.following(userId, position, story);
    }

    private void onFollowingHandler(Message msg) {
        LabelStoryUtils.FollowUserResult result = (LabelStoryUtils.FollowUserResult) msg.obj;
        LabelStory story = (LabelStory) result.extra;

        String sex = "";
        if (story.getStranger() != null) {
            switch (story.getStranger().getSex()) {
                case 1:
                    sex = getActivity().getResources().getString(R.string.he);
                    break;
                case 2:
                    sex = getActivity().getResources().getString(R.string.her);
                    break;
                default:
                    sex = getActivity().getResources().getString(R.string.he);
                    break;
            }
        }
        switch (result.result) {
            case FollowingManager.RESULT_SUCCESS:
                ShowToast.makeText(getActivity(), R.drawable.emoji_smile, getActivity().
                        getResources().getString(R.string.attention_success, sex, result.followCount)).show();
                story.setIsFollowing("Y");
                changeFollowState(story);
                mAdapter.notifyDataSetChanged();
                break;
            default: {
                Activity activity = getActivity();
                if (activity != null) {
                    ShowToast.makeText(activity, R.drawable.emoji_cry,
                            getString(R.string.follow_fail)).show();
                }
                break;
            }
        }
    }

    private void removeDynamic(String objectId, int position) {
        DynamicWrapper wrapper = mAdapter.getItem(position);
        String newObjectId = wrapper != null ? wrapper.getObjectId() : null;

        if (!TextUtils.isEmpty(objectId) && objectId.equals(newObjectId)) {
            mAdapter.removeWrapper(position);
        }
    }

    private void handleDeleteDynamic(boolean success, int position, String objectId) {
        mProgressHelper.dismiss();

        if (success) {
            removeDynamic(objectId, position);
        } else {
            Activity activity = getActivity();
            if (activity != null) {
                ShowToast.makeText(activity, R.drawable.emoji_sad,
                        getString(R.string.labelstory_delete)).show();
            }
        }
    }

    private void showDeleteDynamicConfirm(ConfirmDialogFragment.IConfirmListener listener) {
        ConfirmDialogFragment.UiConfig uiConfig = new ConfirmDialogFragment.UiConfig(
                getString(R.string.labelstory_is_delete), null);
        ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(
                uiConfig, listener);
        dialog.show(getFragmentManager(), MixDynamicAllFragment.class.getSimpleName());
    }

    private void onDeleteStory(LabelStory story, int position) {
        showDeleteDynamicConfirm(new DeleteStoryConfirmListener(story, position));
    }

    private void handleDeleteStory(int result, int position, LabelStory story) {
        String storyId = story != null ? story.getLabelStoryId() : null;
        handleDeleteDynamic(result == LabelStoryManager.QUERY_RESULT_SUCCESS, position, storyId);
    }

    private void onDeleteConfide(Confide confide, int position) {
        showDeleteDynamicConfirm(new DeleteConfideConfirmListener(confide, position));
    }

    private void handleDeleteConfide(int result, int position, Confide confide) {
        String confideId = confide != null ? confide.getConfideId() : null;
        handleDeleteDynamic(result == FunctionCallListener.RESULT_CALL_SUCCESS,
                position, confideId);
    }

    private class DeleteStoryConfirmListener extends ConfirmDialogFragment.AbsConfirmListener {

        private final LabelStory story;
        private final int position;

        public DeleteStoryConfirmListener(LabelStory story, int position) {
            this.story = story;
            this.position = position;
        }

        @Override
        public void onConfirm() {
            mProgressHelper.show();
            mStoryManager.deleteLabelStory(story.getLabelStoryId(),
                    new LabelStoryManager.LabelStoryDeleteQueryObserver() {
                        @Override
                        public void onQueryResult(int result, boolean remaining) {
                            mHandler.obtainMessage(MSG_DELETE_STORY, result, position,
                                    story).sendToTarget();
                        }
                    });
        }
    }

    private class DeleteConfideConfirmListener extends ConfirmDialogFragment.AbsConfirmListener {

        private final Confide confide;
        private final int position;

        public DeleteConfideConfirmListener(Confide confide, int position) {
            this.confide = confide;
            this.position = position;
        }

        @Override
        public void onConfirm() {
            mProgressHelper.show();
            mConfideManager.deleteConfide(confide.getConfideId(), new FunctionCallListener() {
                @Override
                public void onCallResult(int result, int errorCode, String errorDesc) {
                    mHandler.obtainMessage(MSG_DELETE_CONFIDE, result, position,
                            confide).sendToTarget();
                }
            });
        }
    }

    private class MixComment {
        private int position;
        private int childPosition;
        private boolean isGoup;

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getChildPosition() {
            return childPosition;
        }

        public void setChildPosition(int childPosition) {
            this.childPosition = childPosition;
        }

        public boolean isGoup() {
            return isGoup;
        }

        public void setGoup(boolean isGoup) {
            this.isGoup = isGoup;
        }
    }
}
