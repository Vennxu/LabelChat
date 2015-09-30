package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LabelStoryCategory;
import com.ekuater.labelchat.datastruct.PickPhotoUser;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.fragment.labelstory.HorizontalListView;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ViewHolder;
import com.ekuater.labelchat.ui.widget.ClickEventIntercept;
import com.ekuater.labelchat.ui.widget.emoji.ShowContentTextView;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Label on 2015/1/4.
 *
 * @author Xu wenxiang
 */

public class MyLabelStoryPage extends BasePage {

    private final static int LABEL_STORY_INFO = 101;
    private final static int LABEL_STORY_PRAISE = 102;
    private final static int LABEL_STORY_PRAISE_EXIT = 103;
    private final static int LABEL_STORY_NULL = 105;
    private static final int MSG_ADDING_LABEL_RESULT = 106;
    private static final int MSG_ADDING_LABEL_HOLD = 107;
    private static final int LABEL_STORY_MY_REQUEST=108;
    private static final int LABEL_STORY_MY_DELETE=109;

    private boolean isLoading = true;
    private LabelStoryManager mLabelStoryManager;
    private int indexPager = 0;
    private LabelStoryAdapter adapter;
    private boolean isPullRefresh = false;
    private ArrayList<LabelStory> mLabelStories = new ArrayList<LabelStory>();
    private UserLabelManager mUserLabelManager;
    private AvatarManager mAvatarManager;
    private SettingHelper mSettingHelper;
    private View backgroundView;
    private View viewNoDate;
    private View view;
    private View mLayout;
    private ProgressBar mProgressBar;
    private TextView mMore;
    private boolean isCanLoad = true;
    private Stranger mStranger = null;
    private boolean mNowLoading;
    private ImageView mBackgroudImageView;
    private String mQueryUserId;
    private SimpleProgressDialog mProgressDialog;
    private int mDeleteIndex;

    public MyLabelStoryPage(Fragment fragment) {
        super(fragment);
        mLabelStoryManager = LabelStoryManager.getInstance(mContext);
        mUserLabelManager = UserLabelManager.getInstance(mContext);
        mSettingHelper = SettingHelper.getInstance(mContext);
        mAvatarManager = AvatarManager.getInstance(mContext);
        adapter = new LabelStoryAdapter(mContext);
        mNowLoading = false;
        mQueryUserId=mSettingHelper.getAccountUserId();
        getDate();
    }

    public MyLabelStoryPage(Fragment fragment, Stranger stranger) {
        super(fragment);
        mStranger = stranger;
        mLabelStoryManager = LabelStoryManager.getInstance(mContext);
        mUserLabelManager = UserLabelManager.getInstance(mContext);
        mAvatarManager = AvatarManager.getInstance(mContext);
        mSettingHelper = SettingHelper.getInstance(mContext);
        adapter = new LabelStoryAdapter(mContext);
        mNowLoading = false;
        mQueryUserId=mStranger.getUserId();
        getDate();
    }

    @Override
    public void onAddToContentBackground(ViewGroup container) {
        super.onAddToContentBackground(container);
        if (backgroundView == null) {
            backgroundView = LayoutInflater.from(mContext).inflate(
                    R.layout.user_show_story_background, container, false);
            view = backgroundView.findViewById(R.id.story_line);
            viewNoDate = backgroundView.findViewById(R.id.no_story);
            mBackgroudImageView = (ImageView) viewNoDate.findViewById(
                    R.id.layout_story_nodate);
        }

        ViewGroup parent = (ViewGroup) backgroundView.getParent();
        if (parent != null) {
            parent.removeView(backgroundView);
        }
        container.addView(backgroundView);

        mBackgroudImageView.setImageResource(mQueryUserId != null
                ? R.drawable.story_null_others : R.drawable.story_null);
        viewNoDate.setVisibility(mLabelStories.size() > 0 ? View.GONE : View.VISIBLE);
        view.setVisibility(mLabelStories.size() > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean isLoading() {
        return mNowLoading;
    }

    @Override
    public ListAdapter getContentAdapter() {
        return adapter;
    }

    @Override
    public AdapterView.OnItemClickListener getContentItemClickListener() {
        return null;
    }

    private Handler storyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            postHandler(msg);
        }
    };
    private void postHandler(Message msg){
        switch (msg.what) {
            case LABEL_STORY_INFO:
                updateList((LabelStory[]) msg.obj);
                break;
            case LABEL_STORY_PRAISE:
                praiseDate(msg.obj);
                break;

            case LABEL_STORY_PRAISE_EXIT:
                Toast.makeText(mContext, R.string.labelstory_input_praise_failed,
                        Toast.LENGTH_SHORT).show();
                break;
            case LABEL_STORY_NULL:
                if (mLayout != null) {
                    mLayout.setVisibility(View.GONE);
                }
                break;
            case MSG_ADDING_LABEL_RESULT:
                onAddLabelResult(msg.arg1);
                break;
            case LABEL_STORY_MY_DELETE:
                onDeleteHandlerResult(msg.arg1,msg.arg2);
                break;
            default:
                break;
        }
    }
    private UserLabelManager.IListener mLabelListener = new UserLabelManager.AbsListener() {
        @Override
        public void onLabelAdded(int result) {
            Message msg = storyHandler.obtainMessage(MSG_ADDING_LABEL_RESULT, result, 0);
            storyHandler.sendMessage(msg);
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.story_item_praise:
                    int position = Integer.parseInt(v.getTag().toString());
                    LabelStory labelStory = mLabelStories.get(position);
                    praiseLabelStory(position, labelStory.getLabelStoryId());
                    break;
                case R.id.story_my_item_label:
                    UILauncher.launchFragmentLabelStoryUI(mContext, ((LabelStoryCategory) v.getTag()), null);
                    break;
                case R.id.story_my_item_linear_content:
                    if (v.getTag() != null) {
//                        UILauncher.launchFragmentLabelStoryDetaileActivityUI(mFragment,mLabelStories,(Integer) v.getTag(),null,LABEL_STORY_MY_REQUEST);
//                        UILauncher.launchFragmentLabelStoryDetaileUI(mContext, (LabelStory) v.getTag(), true);
                    }
                    break;
                case R.id.show_label_story_user_area:
                    UILauncher.launchFragmentPickPhotoCrowd(mContext, (PickPhotoUser[])v.getTag(),mContext.getString(R.string.pick_photo_user));
                    break;
                case R.id.story_my_item_delete:
                    if (v.getTag()!=null) {
                      mDeleteIndex=(Integer)v.getTag();
                      showConfirmDialog();
                    }
                    break;
            }
        }
    };
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
//                    UILauncher.launchFragmentLabelStoryDetaileActivityUI(mFragment,mLabelStories,position-1,null,LABEL_STORY_MY_REQUEST);
                }
            }
        }
    };

    public void updateList(LabelStory[] labelStories) {
        if (labelStories.length == 20) {
            isCanLoad = true;
            if (mLayout != null) {
                mProgressBar.setVisibility(View.GONE);
                mMore.setVisibility(View.VISIBLE);
                mMore.setText(R.string.p2refresh_head_load_more);
            }
        } else {
            if (mLayout != null) {
                mLayout.setVisibility(View.GONE);
            }
            isCanLoad = false;
        }
        List<LabelStory> list = Arrays.asList(labelStories);
        ArrayList<LabelStory> arrayList = new ArrayList<LabelStory>();
        arrayList.addAll(list);
        if (isPullRefresh) {
            if (mLabelStories != null && mLabelStories.size() > 0) {
                mLabelStories.clear();
            }
            mLabelStories = arrayList;
        } else {
            isLoading = true;
            mLabelStories.addAll(arrayList);
        }
        adapter.notifyDataSetChanged();
    }

    public void getDate() {
        indexPager++;
        mNowLoading = true;
        LabelStoryManager.LabelStoryQueryObserver observer
                = new LabelStoryManager.LabelStoryQueryObserver() {
            @Override
            public void onQueryResult(int result, LabelStory[] labelStories, boolean remaining, int frendsCount) {
                mNowLoading = false;
                postEvent(new PageEvent(MyLabelStoryPage.this, PageEvent.Event.LOAD_DONE));
                if (result == LabelStoryManager.QUERY_RESULT_SUCCESS) {
                    if (labelStories != null) {

                        Message msg = storyHandler.obtainMessage(LABEL_STORY_INFO, labelStories);
                        storyHandler.sendMessage(msg);
                    } else {
                        isCanLoad = false;
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
//        mLabelStoryManager.accessMyLabelStoryAllInfo(mQueryUserId, String.valueOf(indexPager), mStranger, observer);
    }

    private void onDeleteHandlerResult(int resultCode,int position){
        dismissProgressDialog();
        switch (resultCode){
            case LabelStoryManager.QUERY_RESULT_SUCCESS:
                mLabelStories.remove(position);
                adapter.notifyDataSetChanged();
                break;
            default:
                Toast.makeText(mContext, R.string.labelstory_delete,
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }
    private void praiseDate(Object obj) {
        int position = Integer.parseInt(obj.toString());
        LabelStory labelStory = mLabelStories.get(position);
        int number = 0;
        if (labelStory.getIsPraise().equals("N")) {
            number = Integer.parseInt(mLabelStories.get(position).getPraise()) + 1;
            mLabelStories.get(position).setIsPraise("Y");
            mLabelStories.get(position).setPraise(number + "");
        } else {
            number = Integer.parseInt(mLabelStories.get(position).getPraise()) - 1;
            mLabelStories.get(position).setIsPraise("N");
            mLabelStories.get(position).setPraise(number + "");
        }
        adapter.notifyDataSetChanged();
    }

    private void onAddLabelResult(int result) {
        boolean success = (result == ConstantCode.LABEL_OPERATION_SUCCESS);

        storyHandler.removeMessages(MSG_ADDING_LABEL_HOLD);
        Toast.makeText(mContext,
                success ? R.string.add_label_success
                        : R.string.add_label_failure,
                Toast.LENGTH_SHORT).show();
        mUserLabelManager.unregisterListener(mLabelListener);
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

    private void deleteLabelStory(String labelStoryId, final int mPosition){

        LabelStoryManager.LabelStoryDeleteQueryObserver observer=new LabelStoryManager.LabelStoryDeleteQueryObserver() {
            @Override
            public void onQueryResult(int result, boolean remaining) {
                Message message=Message.obtain(storyHandler,LABEL_STORY_MY_DELETE,result,mPosition);
                storyHandler.sendMessage(message);
            }
        };
        mLabelStoryManager.deleteLabelStory(labelStoryId, observer);
    }

    public class LabelStoryAdapter extends BaseAdapter {
        private final LayoutInflater layoutInflater;
        private Context context;
        final List<String> ownerLabels = new ArrayList<String>();
        private int maxBrowser=0;

        public LabelStoryAdapter(Context context) {
            this.context = context;
            layoutInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            final UserLabel[] userLabels = UserLabelManager.getInstance(context).getAllLabels();
            if (userLabels != null) {
                for (UserLabel label : userLabels) {
                    ownerLabels.add(label.getName());
                }
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (mLabelStories.size() >= 20 && position == mLabelStories.size()) {
                return 0;
            }
            return 1;
        }

        public View getView(int viewType, ViewGroup parent) {
            View layout;
            switch (viewType) {
                case 0:
                    if (mLayout == null) {
                        mLayout = layoutInflater.inflate(R.layout.layout_story_footer, parent, false);
                        mProgressBar = (ProgressBar) ViewHolder.get(mLayout, R.id.story_loading);
                        mMore = (TextView) ViewHolder.get(mLayout, R.id.story_more);
                        mMore.setText(R.string.p2refresh_head_load_more);
                        mMore.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                    }
                    layout = mLayout;
                    break;
                default:
                    layout = layoutInflater.inflate(R.layout.labelstory_my_listview_item, parent, false);
                    break;
            }
            return layout;
        }

        @Override
        public int getCount() {
            return mLabelStories.size() >= 20 ? mLabelStories.size() + 1 : mLabelStories.size();
        }

        @Override
        public LabelStory getItem(int position) {
            return mLabelStories.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            if (convertView == null) {
                convertView = getView(getItemViewType(position), parent);
            }
            if (mLabelStories != null && mLabelStories.size() != 0) {
                if (position == mLabelStories.size()) {
                    if (isCanLoad) {
                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                isCanLoad = false;
                                mMore.setVisibility(View.GONE);
                                mProgressBar.setVisibility(View.VISIBLE);
                                getDate();
                            }
                        });
                    }
                } else {
                    final LabelStory labelStory = getItem(position);
                    ShowContentTextView story_content = (ShowContentTextView) ViewHolder.get(convertView, R.id.story_all_item_content);
                    RelativeLayout story_content_linnear = (RelativeLayout) ViewHolder.get(convertView, R.id.story_my_item_linear_content);
                    story_content_linnear.setOnClickListener(mOnClickListener);
                    story_content_linnear.setTag(position);
                    story_content.setAutoLinkMask(Linkify.ALL);
//                    TextView story_praise_number = (TextView) ViewHolder.get(convertView, R.id.story_item_praisenumber);
//                    TextView story_comment_number = (TextView) ViewHolder.get(convertView, R.id.story_item_commentnumber);
//                    ImageView story_praise_image = (ImageView) ViewHolder.get(convertView, R.id.story_item_praise);
                    ImageView story_content_image = (ImageView) ViewHolder.get(convertView, R.id.story_my_item_image);
                    TextView story_floor = (TextView) ViewHolder.get(convertView, R.id.story_my_item_floor);
                    RelativeLayout story_label = (RelativeLayout) ViewHolder.get(convertView, R.id.story_my_item_label);
                    TextView story_label_name = (TextView) ViewHolder.get(convertView, R.id.story_my_item_label_name);
//                    ImageView story_label_praise = (ImageView) ViewHolder.get(convertView, R.id.story_item_praise);
//                    TextView story_label_praiseNum = (TextView) ViewHolder.get(convertView, R.id.story_item_praisenumber);
                    TextView story_time = (TextView) ViewHolder.get(convertView, R.id.story_my_item_time);
                    ClickEventIntercept story_user_area=(ClickEventIntercept) ViewHolder.get(convertView,R.id.show_label_story_user_area);
                    HorizontalListView story_user_list=(HorizontalListView) ViewHolder.get(convertView,R.id.label_story_user);
                    TextView story_user_number=(TextView) ViewHolder.get(convertView,R.id.show_label_story_user_area_read_number);
                    ImageView story_delete=(ImageView) ViewHolder.get(convertView,R.id.story_my_item_delete);
                    story_delete.setOnClickListener(mOnClickListener);
                    story_delete.setTag(position);
                    story_user_area.setOnClickListener(mOnClickListener);
                    story_user_area.setTag(labelStory.getPickPhotoUser());
                    if (TextUtils.isEmpty(labelStory.getBrowseNum())||labelStory.getBrowseNum().equals("0")){
                        story_user_area.setVisibility(View.GONE);
                    }else{
                        if (isMyUserId()) {
                            story_user_area.setVisibility(View.VISIBLE);
                            story_user_number.setText(labelStory.getBrowseNum());
                            story_user_list.setAdapter(new ShowUserAdapter(labelStory.getPickPhotoUser()));
                        }else{
                            story_user_area.setVisibility(View.GONE);
                        }
                    }
                    if (isMyUserId()) {
                        story_delete.setVisibility(View.VISIBLE);
                    }else{
                        story_delete.setVisibility(View.GONE);
                    }


                    story_label_name.setText(labelStory.getCategory().getmCategoryName());
                    story_floor.setText(labelStory.getFloor() + mContext.getString(R.string.labelstory_item_floor));
                    story_label.setOnClickListener(mOnClickListener);
                    story_label.setTag(labelStory.getCategory());
                    if (TextUtils.isEmpty(labelStory.getContent())) {
                        story_content.setVisibility(View.GONE);
                    } else {
                        story_content.setVisibility(View.VISIBLE);
                        story_content.setText(labelStory.getContent());
                    }
                    story_time.setText(getTimeString(labelStory.getCreateDate()));
                    if (labelStory.getImages() != null && labelStory.getImages().length > 0) {
                        story_content_image.setVisibility(View.VISIBLE);
                        MiscUtils.showLabelStoryImageThumb(mAvatarManager, labelStory.getImages()[0], story_content_image, R.drawable.pic_loading);
                    } else {
                        story_content_image.setVisibility(View.GONE);
                    }
                }
            } else {
                convertView.setVisibility(View.GONE);
            }
            return convertView;
        }

        private String getTimeString(long time) {
            return DateTimeUtils.getTimeString(mContext, time);
        }
        private boolean isMyUserId(){
            return mQueryUserId.equals(SettingHelper.getInstance(mContext).getAccountUserId())?true:false;
        }

    }
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = SimpleProgressDialog.newInstance();
            mProgressDialog.show(getFragmentManager(), "SimpleProgressDialog");
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
    private ConfirmDialogFragment.AbsConfirmListener confirmListener =new ConfirmDialogFragment.AbsConfirmListener(){
        @Override
        public void onConfirm() {
            showProgressDialog();
            deleteLabelStory(mLabelStories.get(mDeleteIndex).getLabelStoryId(),mDeleteIndex);
        }

        @Override
        public void onCancel() {
            super.onCancel();
        }

    };

    private void showConfirmDialog(){
        ConfirmDialogFragment.UiConfig uiConfig=new ConfirmDialogFragment.UiConfig(mFragment.getActivity().getString(R.string.labelstory_is_delete),null);
        ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(uiConfig,confirmListener);
        confirmDialogFragment.show(getFragmentManager(), "MyLabelStoryPage");
    }
    private class ShowUserAdapter extends BaseAdapter {
        private PickPhotoUser[] pPickPhotoUser=null;
        private LayoutInflater inflater;
        public  ShowUserAdapter(PickPhotoUser[] pickPhotoUser) {
            pPickPhotoUser=pickPhotoUser;
            inflater = LayoutInflater.from(mFragment.getActivity());
        }

        @Override
        public int getCount() {
            return pPickPhotoUser == null ? 0 : pPickPhotoUser.length;
        }

        @Override
        public PickPhotoUser getItem(int position) {
            return pPickPhotoUser[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.labelstory_praise_user_image, parent, false);
            }
            ImageView imageView = (ImageView) ViewHolder.get(convertView, R.id.labelstory_praise_iamge);
            MiscUtils.showAvatarThumb(mAvatarManager, getItem(position).getPickUserAvatarThumb(), imageView);
            return convertView;
        }
    }
}

