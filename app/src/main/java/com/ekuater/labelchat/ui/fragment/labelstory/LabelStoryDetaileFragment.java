package com.ekuater.labelchat.ui.fragment.labelstory;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.Dynamic.DynamicResultEvent;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LabelStoryComments;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserPraise;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.FollowingManager;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.ContentSharer;
import com.ekuater.labelchat.ui.ShareContent;
import com.ekuater.labelchat.ui.UIEventBusHub;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.BaseActivity;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.util.ViewHolder;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.ui.widget.ClickEventInterceptLinear;
import com.ekuater.labelchat.ui.widget.KeyboardStateView;
import com.ekuater.labelchat.ui.widget.emoji.EmojiEditText;
import com.ekuater.labelchat.ui.widget.emoji.EmojiKeyboard;
import com.ekuater.labelchat.ui.widget.emoji.EmojiSelector;
import com.ekuater.labelchat.ui.widget.emoji.ShowContentTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Label on 2015/1/11.
 *
 * @author Xu wenxiang
 */
public class LabelStoryDetaileFragment extends Fragment {

    private final int COMMENT_REQUEST_CODE = 1;
    private final int PRAISE_REQUEST_CODE = 2;
    private final int PRAISE_REQUEST_EXIT_CODE = 3;
    private final int COMMENT_LIST_REQUEST_CODE = 4;
    private final int COMMENT_PRAISE_REQUEST_CODE = 8;
    private final int FOLLOWING_REQUEST_CODE = 11;
    private final int LETTER_REQUEST_CODE = 12;
    private static final int MSG_KEYBOARD_STATE_CHANGED = 13;

    private NoScrollListview mCommentListView;
    private ScrollView mScrollView;
    private ViewGroup mContentContainer;
    private LinearLayout mLinearLoading, mLinearCommentLoading, mLabelNameLinear;
    private ImageView mImageLoading, mImageCommentLoading, mImageNull, mUserPraise,
            mImagePraise, mImageLetter;
    private CircleImageView mImageTx;
    private ImageView mImageFollower;
    private LinearLayout mFollowerLinear;
    private LinearLayout mPraiseLinear;
    private FrameLayout mDetailHideClick;
    private TextView mDetailName, mDetailTime, mDetailLabelName, mDetailPraiseNumber,
            mDetailReplyName, mImageFollowing, mDetailCommentNumber, mDetailLetterNumber;
    private ShowContentTextView mInputHint;
    private ClickEventInterceptLinear mCommentParent;
    private Button mSendComment;
    private HorizontalListView mPraiseUserList;
    private EmojiSelector mEmojiSelector;
    private ImageButton mFaceImageButton;
    private EmojiEditText mEmojiEditText;
    private LabelStoryManager mLabelStoryManager;
    private AvatarManager mAvatarManager;
    private PushMessageManager mPushManager;
    private ContactsManager contactsManager;
    private InputMethodManager mInputMethodManager;
    private FollowingManager mFollowingManager;
    private SettingHelper mSettingHelper;
    private SimpleProgressDialog mProgressDialog;
    private Activity mActivity;
    private int mCommentNext = 0;
    public LabelStory mLabelStory;
    private boolean isShowFragment = false;
    private LabelStoryDetailAdapter mLabelStoryDetailAdapter;
    private ContentSharer mContentSharer;
    private boolean mIsFaceShow = false;
    private boolean isGroup = true;
    private LabelStoryComments mComment;
    private boolean isShowCommentFragment = true;
    private boolean isShowNullDate = false;
    private PraiseUserAdapter mPraiseUserAdapter;
    private String letterMsg = null;
    private PopupWindow mPopupWindow;
    private String mCategoryName = null;
    private int tag;
    public boolean isChange = false;
    public boolean isShowTitle = false;
    public boolean isKeyBroad = false;
    public boolean isPraise, isComment;
    private LinearLayout mContentArea;
    private StoryContentRender mContentRender;
    private Handler mHandler;
    private EventBus mUIEventBus;
    private Handler.Callback mHandlerCb = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean handled = true;

            switch (msg.what) {
                case COMMENT_REQUEST_CODE:
                    onCommentRequestHandler(msg.arg1, msg.obj);
                    break;
                case PRAISE_REQUEST_CODE:
                    onPraiseRequestHandler();
                    break;
                case PRAISE_REQUEST_EXIT_CODE:
                    ShowToast.makeText(mActivity, R.drawable.emoji_sad,
                            mActivity.getString(R.string.labelstory_input_praise_failed)).show();
                    break;
                case COMMENT_LIST_REQUEST_CODE:
                    onCommentListRequestHandler(msg.arg1, msg.obj);
                    break;
                case COMMENT_PRAISE_REQUEST_CODE:
                    break;
                case FOLLOWING_REQUEST_CODE:
                    onFollowingHandler(msg);
                    break;
                case LETTER_REQUEST_CODE:
                    onLetterHandler(msg);
                    break;
                case MSG_KEYBOARD_STATE_CHANGED:
                    handleKeyboardStateChanged(msg.arg1);
                    break;
                default:
                    handled = false;
                    break;
            }
            return handled;
        }
    };

    private boolean isHideKeyboard = false;

    private void handleKeyboardStateChanged(int state) {
        switch (state) {
            case KeyboardStateView.KEYBOARD_STATE_HIDE:
                isHideKeyboard = true;
                clearContentEditFocus();
                break;
            case KeyboardStateView.KEYBOARD_STATE_SHOW:
                isHideKeyboard = false;
                mDetailHideClick.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private void clearContentEditFocus() {
        if (!mIsFaceShow) {
            mEmojiEditText.setVisibility(View.GONE);
            mInputHint.setVisibility(View.VISIBLE);
            mDetailHideClick.setVisibility(View.GONE);
        }
        if (mEmojiEditText.getText().length() == 0) {
            mInputHint.setText(getString(R.string.labelstroy_input_comment_hint));
        } else {
            mInputHint.setText(mEmojiEditText.getText().toString());
        }
        hideSoftInput();
    }

    private void hideSoftInput() {
        mInputMethodManager.hideSoftInputFromWindow(mEmojiEditText.getWindowToken(), 0);
    }

    private void onFollowingHandler(Message message) {
        String sex = "";
        if (mLabelStory.getStranger() != null) {
            switch (mLabelStory.getStranger().getSex()) {
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
        switch (message.arg1) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                changeFollowState();
                ShowToast.makeText(getActivity(), R.drawable.emoji_smile, getActivity().
                        getResources().getString(R.string.attention_success, sex, message.arg2)).show();
                break;
            default:
                ShowToast.makeText(mActivity, R.drawable.emoji_cry,
                        mActivity.getString(R.string.follow_fail)).show();
                break;
        }
    }

    private void changeFollowState() {
        mImageFollowing.setTextColor(mActivity.getResources().getColor(R.color.followed));
        mFollowerLinear.setBackgroundResource(R.drawable.followed);
        mImageFollower.setImageResource(R.drawable.followed_icon);
        mImageFollowing.setText(R.string.labelstory_attentioned);
        mLabelStory.setIsFollowing("Y");
    }

    private void onLetterHandler(Message message) {
        switch (message.arg1) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                int num = mLabelStory.getLetterNum() + 1;
                mLabelStory.setLetterNum(num);
                mDetailLetterNumber.setText(num + "");
                mDetailLetterNumber.setVisibility(View.VISIBLE);
                LabelStoryUtils.insertSystemPush(mPushManager, mLabelStory.getStranger(), letterMsg);
                ShowToast.makeText(mActivity, R.drawable.emoji_smile,
                        mActivity.getString(R.string.send_letter_succese)).show();
                break;
            default:
                ShowToast.makeText(mActivity, R.drawable.emoji_cry,
                        mActivity.getString(R.string.send_letter_failed)).show();
                break;
        }
    }

    private void onPraiseRequestHandler() {
        ArrayList<UserPraise> arrayList = new ArrayList<>();
        if (mLabelStory.getUserPraise() != null) {
            arrayList.addAll(Arrays.asList(mLabelStory.getUserPraise()));
        }

        if (mLabelStory.getIsPraise().equals("N")) {
            int number = Integer.parseInt(mLabelStory.getPraise()) + 1;
            mLabelStory.setIsPraise("Y");
            arrayList.add(0, new UserPraise(SettingHelper.getInstance(getActivity()).getAccountUserId(),
                    SettingHelper.getInstance(getActivity()).getAccountNickname(),
                    SettingHelper.getInstance(getActivity()).getAccountAvatarThumb(), 0));
            UserPraise[] userPraises = new UserPraise[arrayList.size()];
            mLabelStory.setUserPraise(arrayList.toArray(userPraises));
            mLabelStory.setPraise(number + "");
        } else {
            if (arrayList.size() == 1) {
                mLabelStory.setUserPraise(null);
            } else {
                for (int i = 0; i < arrayList.size(); i++) {
                    if (arrayList.get(i).getmPraiseUserId().equals(SettingHelper.getInstance(
                            getActivity()).getAccountUserId())) {
                        arrayList.remove(arrayList.get(i));
                        UserPraise[] userPraises = new UserPraise[arrayList.size()];
                        mLabelStory.setUserPraise(arrayList.toArray(userPraises));
                        break;
                    }
                }
            }
            int number = Integer.parseInt(mLabelStory.getPraise()) - 1;
            mLabelStory.setIsPraise("N");
            mLabelStory.setPraise(number + "");
        }
        isPraise();
    }

    private void startAnimation(LinearLayout linearLoading, ImageView imageLoading) {
        linearLoading.setVisibility(View.VISIBLE);
        Drawable drawable = imageLoading.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.start();
    }

    private void stopAnimation(LinearLayout linearLoading, ImageView imageLoading) {
        linearLoading.setVisibility(View.GONE);
        Drawable drawable = imageLoading.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.stop();
    }

    private void onCommentRequestHandler(int result, Object obj) {
        dismissProgressDialog();
        switch (result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS: {
                LabelStoryComments labelStoryComments = (LabelStoryComments) obj;
                Stranger stranger = new Stranger();
                stranger.setAvatarThumb(SettingHelper.getInstance(mActivity).getAccountAvatarThumb());
                stranger.setNickname(SettingHelper.getInstance(mActivity).getAccountNickname());
                stranger.setUserId(SettingHelper.getInstance(mActivity).getAccountUserId());
                labelStoryComments.setmStranger(stranger);
                mEmojiEditText.setText("");
                mInputMethodManager.hideSoftInputFromWindow(mEmojiEditText.getWindowToken(), 0);
                showEmojiSelector(false);
                int number = Integer.parseInt(mLabelStory.getCommentNum()) + 1;
                mLabelStory.setCommentNum(number + "");
                mDetailCommentNumber.setVisibility(View.VISIBLE);
                mDetailCommentNumber.setText(mLabelStory.getCommentNum() + "");
                ShowToast.makeText(mActivity, R.drawable.emoji_smile, mActivity.
                        getResources().getString(R.string.labelstroy_input_comment_succse)).show();
                mLabelStoryDetailAdapter.updateGroupComment(labelStoryComments);
                break;
            }
            default:
                if (mActivity != null) {
                    ShowToast.makeText(mActivity, R.drawable.emoji_cry, mActivity.
                            getResources().getString(R.string.labelstroy_input_comment_faile)).show();
                }
                break;
        }
    }

    private void onCommentListRequestHandler(int result, Object obj) {
        switch (result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS: {
                stopAnimation(mLinearCommentLoading, mImageCommentLoading);
                LabelStory labelStory = (LabelStory) obj;
                if (labelStory != null) {
                    LabelStoryComments[] comments = labelStory.getLabelStoryComments();
                    List<LabelStoryComments> labelStoryComments = (comments != null)
                            ? Arrays.asList(labelStory.getLabelStoryComments())
                            : new ArrayList<LabelStoryComments>();

                    mLabelStoryDetailAdapter.overrideArrayList(labelStoryComments);
                    if (labelStoryComments.size() < 20) {
                        mLabelStoryDetailAdapter.setInvisibleLayout();
                    } else {
                        mLabelStoryDetailAdapter.setHideProgress();
                    }
                    if (!isShowFragment) {
                        mLabelStory = labelStory;
                        isShowFragment = true;

                        mContentRender = StoryRenderFactory.newRender(mActivity, mLabelStory);
                        mContentRender.onCreate();
                        mContentContainer.addView(mContentRender.onCreateView(
                                LayoutInflater.from(mActivity), mContentContainer));
                        initDate();
                        showFragment();
                    }
                    isShowCommentFragment = false;
                } else {
                    isShowNullDate = true;
                }
                break;
            }
            default:
                break;
        }
        moveDown();
    }

    private void moveDown() {
        if (isComment) {
            mScrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollToBottom(mScrollView, mContentArea, 0);
                }
            });
        }

        if (isPraise) {
            mScrollView.post(new Runnable() {
                @Override
                public void run() {
                    int height = view.findViewById(R.id.desdcript_comment_null).getBottom();
                    scrollToBottom(mScrollView, mContentArea, height);
                }
            });
        }
    }

    public static void scrollToBottom(final View scroll, final View inner, final int height) {
        Handler mHandler = new Handler();

        mHandler.post(new Runnable() {
            public void run() {
                if (scroll == null || inner == null) {
                    return;
                }
                int offset = inner.getMeasuredHeight() - scroll.getHeight();
                if (offset < 0) {
                    offset = 0;
                }

                scroll.scrollTo(0, offset - height);
            }
        });
    }

    private int mCount = 0;

    private AdapterView.OnItemClickListener onItemClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position < mLabelStoryDetailAdapter.getArray().size()) {
                isGroup = false;
                LabelStoryComments group = (LabelStoryComments) parent
                        .getAdapter().getItem(position);
                if (group.getmStranger() != null) {
                    if (mComment != null) {
                        mComment = null;
                    }
                    mComment = new LabelStoryComments();
                    mComment.setmStranger(group.getmStranger());
                    mComment.setmParentCommentId(group.getmStroyCommentId());
                    mComment.setmReplyUserId(group.getmStranger().getUserId());
                    mComment.setmReplyNickName(group.getmStranger().getNickname());
                    mComment.setmLabelStoryId(mLabelStory.getLabelStoryId());

                    mEmojiEditText.setVisibility(View.VISIBLE);
                    mInputHint.setVisibility(View.GONE);
                    mEmojiEditText.requestFocus();
                    mInputMethodManager.showSoftInput(mEmojiEditText, 0);
                    showEmojiSelector(false);
                    SpannableString ss;
                    if (group.getmStranger() != null) {
                        Stranger stranger = group.getmStranger();
                        String title = MiscUtils.getUserRemarkName(mActivity, stranger.getUserId());
                        String name = title != null && title.length() > 0 ? title : stranger.getNickname();
                        String content = "@" + name + " ";
                        mCount = content.length();
                        ss = new SpannableString(content);
                        ss.setSpan(new ForegroundColorSpan(R.color.colorLabelTextLight), 0,
                                mCount - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        mCount = 0;
                        ss = null;
                    }

                    mEmojiEditText.setText(ss);
                    mEmojiEditText.setSelection(mCount);
                }
            }
        }
    };

    private void clearReply() {
        isGroup = true;
        mComment = null;
        mEmojiEditText.setHint(getString(R.string.labelstroy_input_comment_hint));
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onClickView(v);
        }
    };

    private void onClickView(View v) {
        List<String> userIds = mLabelStoryManager.getAllUserIds(mLabelStory, mLabelStoryDetailAdapter.getArray());
        switch (v.getId()) {
            case R.id.operation_bar_praise:
                praise(mLabelStory.getLabelStoryId(), userIds);
                break;
            case R.id.descript_horizontallistview:
                UILauncher.launchFragmentPraiseCrowd(getActivity(), mLabelStory.getUserPraise());
                break;
            case R.id.comment_ui_input_send_btn:
                String commentContent = mEmojiEditText.getText().toString();
                LabelStoryComments comments = new LabelStoryComments();
                if (!TextUtils.isEmpty(commentContent)) {
                    showProgressDialog();
                    if (isGroup && mComment == null) {
                        comments.setmLabelStoryId(mLabelStory.getLabelStoryId());
                        comments.setmStoryComment(commentContent);
                        comments.setmArrayUserId(userIds);
                        comment(comments);
                    } else {
                        if (commentContent.length() > mCount) {
                            Stranger stranger = new Stranger();
                            stranger.setUserId(mSettingHelper.getAccountUserId());
                            stranger.setNickname(mSettingHelper.getAccountNickname());
                            stranger.setAvatarThumb(mSettingHelper.getAccountAvatarThumb());
                            comments.setmLabelStoryId(mLabelStory.getLabelStoryId());
                            comments.setmReplyUserId(mComment.getmReplyUserId());
                            comments.setmReplyNickName(mComment.getmReplyNickName());
                            comments.setmParentCommentId(mComment.getmParentCommentId());
                            comments.setmStranger(stranger);
                            comments.setmStoryComment(commentContent.substring(mCount, commentContent.length()));
                            comments.setmCreateDate(System.currentTimeMillis());
                            comments.setmArrayUserId(userIds);
                            comment(comments);
                        } else {
                            isGroup = true;
                            ShowToast.makeText(mActivity, R.drawable.emoji_sad, mActivity.
                                    getResources().getString(R.string.labelstroy_input_comment_null)).show();
                        }
                    }
                } else {
                    isGroup = true;
                    ShowToast.makeText(mActivity, R.drawable.emoji_sad, mActivity.
                            getResources().getString(R.string.labelstroy_input_comment_null)).show();
                }
                break;
            case R.id.comment_ui_face_switch_btn:
                mInputHint.setVisibility(View.GONE);
                mEmojiEditText.setVisibility(View.VISIBLE);
                mDetailHideClick.setVisibility(View.VISIBLE);
                if (mIsFaceShow) {
                    mEmojiEditText.requestFocus();
                    showEmojiSelector(false);
                    mInputMethodManager.showSoftInput(mEmojiEditText, 0);
                } else {
                    showEmojiSelector(true);
//                    if (mEmojiEditText.getText().length() > 0) {
//                        mInputHint.setText(mEmojiEditText.getText().toString());
//                    }else {
//                        mInputHint.setText(getString(R.string.labelstroy_input_comment_hint));
//                    }
                    mInputMethodManager.hideSoftInputFromWindow(mEmojiEditText.getWindowToken(), 0);
                }
                break;
            case R.id.descript_tx:
                showStrangerDetailUI(mLabelStory.getStranger());
                break;
            case R.id.operation_bar_letter:
                if (mLabelStory.getStranger() != null) {
                    PrivateLetterFragmentDialog dialog = PrivateLetterFragmentDialog.newInstance(mLabelStory.getLabelStoryId(), mLabelStory.getStranger().getAvatarThumb(),
                            mLabelStory.getStranger().getUserId(),
                            mLabelStory.getStranger().getShowName(),
                            mAvatarManager, 0, onSendEmaileClicklistener);
                    dialog.show(getFragmentManager(), "PrivateLetterFragmentDialog");
                }
                break;
            case R.id.descript_following_linear:
                if (!TextUtils.isEmpty(mLabelStory.getStranger().getUserId())) {
                    UserContact userContact = ContactsManager.getInstance(mActivity).getUserContactByUserId(mLabelStory.getStranger().getUserId());
                    if (userContact == null) {
                        changeFollowState();
                        following(mLabelStory.getStranger().getUserId());
                    }
                }
                break;
            case R.id.operation_bar_more:
                showPopupWindow(v);
                break;
            case R.id.share:
                mPopupWindow.dismiss();
                shareContent(new ShareContent(
                        getString(R.string.labelstory_item_share_gaveyout),
                        mLabelStory.getContent(),
                        BitmapFactory.decodeResource(getResources(),
                                R.drawable.ap_icon_large),
                        getString(R.string.config_label_story_detail_url)
                                + mLabelStory.getLabelStoryId(),
                        mLabelStory.getLabelStoryId()));
                break;
            case R.id.report:
                mPopupWindow.dismiss();
                MiscUtils.complainDynamic(mActivity, mLabelStory.getLabelStoryId());
                break;
            case R.id.operation_bar_comment_parent:
                showSoftInput();
                break;
            case R.id.comment_ui_input_hint:
                showSoftInput();
                break;
            case R.id.detail_hint_click:
                showEmojiSelector(false);
                clearContentEditFocus();
                break;
        }
    }

    private void showSoftInput() {
        mDetailHideClick.setVisibility(View.VISIBLE);
        mEmojiEditText.setVisibility(View.VISIBLE);
        mInputHint.setVisibility(View.GONE);
        mEmojiEditText.requestFocus();
        showEmojiSelector(false);
        mInputMethodManager.showSoftInput(mEmojiEditText, 0);
    }

    private KeyboardStateView.OnKeyboardStateChangedListener mKeyboardStateChangedListener
            = new KeyboardStateView.OnKeyboardStateChangedListener() {
        @Override
        public void onKeyboardStateChanged(int state) {
            Message message = Message.obtain(mHandler, MSG_KEYBOARD_STATE_CHANGED, state, 0);
            mHandler.sendMessage(message);
        }
    };

    public void showPopupWindow(View v) {
        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
        @SuppressLint("InflateParams")
        View popupWindow = layoutInflater.inflate(R.layout.option, null);
        TextView report = (TextView) popupWindow.findViewById(R.id.report);
        TextView share = (TextView) popupWindow.findViewById(R.id.share);
        report.setOnClickListener(mOnClickListener);
        share.setOnClickListener(mOnClickListener);
        mPopupWindow = new PopupWindow(popupWindow, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        mPopupWindow.showAsDropDown(v);
    }

    public PrivateLetterFragmentDialog.OnSendEmaileClicklistener onSendEmaileClicklistener
            = new PrivateLetterFragmentDialog.OnSendEmaileClicklistener() {
        @Override
        public void onSendEmaile(String labelStoryId, String userId, String message, int position) {
            letterMsg = message;
            sendLetter(userId, letterMsg);
        }
    };

    public void showStrangerDetailUI(Stranger stranger) {
        if (stranger != null && !stranger.getUserId().equals(SettingHelper.getInstance(mActivity).getAccountUserId())) {
            UILauncher.launchStrangerDetailUI(mActivity, stranger);
        } else {
            UILauncher.launchMyInfoUI(mActivity);
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                mSendComment.setEnabled(false);
                mDetailReplyName.setVisibility(View.GONE);
                if (isHideKeyboard) {
                    clearContentEditFocus();
                }
                isGroup = true;
                mComment = null;

            } else {
                mSendComment.setEnabled(true);
            }
        }
    };
    private LabelStoryDetailAdapter.GetDateListener getDateListener
            = new LabelStoryDetailAdapter.GetDateListener() {
        @Override
        public void getAdapterDate() {
            loadCommentList();
        }

        @Override
        public void onPraise(String storyCommentId, int position) {
            praiseCommentsLabelStory(storyCommentId, position);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        BaseActivity baseActivity = (BaseActivity) mActivity;
        baseActivity.setHasContentSharer();
        mContentSharer = baseActivity.getContentSharer();
        mUIEventBus = UIEventBusHub.getDefaultEventBus();
        argumentParam();
        if (isShowFragment) {
            setHasOptionsMenu(false);
        } else {
            setHasOptionsMenu(true);
        }
        mHandler = new Handler(mHandlerCb);
        mLabelStoryManager = LabelStoryManager.getInstance(mActivity);
        mAvatarManager = AvatarManager.getInstance(mActivity);
        mInputMethodManager = (InputMethodManager) mActivity.getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        mSettingHelper = SettingHelper.getInstance(mActivity);
        mFollowingManager = FollowingManager.getInstance(mActivity);
        mPushManager = PushMessageManager.getInstance(mActivity);
        contactsManager = ContactsManager.getInstance(mActivity);
        mLabelStoryDetailAdapter = new LabelStoryDetailAdapter(mActivity, this, null,
                getDateListener);
        mPraiseUserAdapter = new PraiseUserAdapter();
        isChange = true;
        loadCommentList();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.labelstory_detaile_menu, menu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //TODO
        mUIEventBus.post(new DynamicResultEvent(mLabelStory));
        if (mContentRender != null) {
            mContentRender.onDestroy();
        }
    }

    private void showFragment() {
        Drawable drawable = mImageLoading.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        if (isShowFragment) {
            mLinearLoading.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
            animationDrawable.stop();
        } else {
            mScrollView.setVisibility(View.GONE);
            mLinearLoading.setVisibility(View.VISIBLE);
            animationDrawable.start();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = true;
        switch (item.getItemId()) {
            case R.id.friend_detail:
                shareContent(new ShareContent(getString(R.string.labelstory_item_share_gaveyout),
                        mLabelStory.getContent(),
                        BitmapFactory.decodeResource(getResources(),
                                R.drawable.ap_icon_large),
                        getString(R.string.config_label_story_detail_url) +
                                mLabelStory.getLabelStoryId(),
                        mLabelStory.getLabelStoryId()));
                break;
            default:
                handled = false;
                break;
        }

        return handled || super.onOptionsItemSelected(item);
    }

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_story_detaile, container, false);
        FrameLayout title_bar = (FrameLayout) view.findViewById(R.id.title_bar);
        if (isShowTitle) {
            title_bar.setVisibility(View.VISIBLE);
        }
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView ok = (TextView) view.findViewById(R.id.right_title);
        if (tag == LabelStoryUtils.STRANGERINFO && mLabelStory.getStranger() != null) {
            if (mLabelStory.getStoryTotal() > 1) {
                ok.setVisibility(View.VISIBLE);
            }
            ok.setText(getString(R.string.load_more));
            ok.setTextColor(getResources().getColor(R.color.white));
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UILauncher.launchMyLabelStoryUI(mActivity,
                            mLabelStory.getStranger().getUserId(),
                            mLabelStory.getStranger());
                    mActivity.finish();
                }
            });
        }
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        title.setText(R.string.labelstory_input_detail);
        initView(view);

        mContentContainer = (ViewGroup) view.findViewById(
                R.id.descript_content_relative);
        if (mContentRender != null) {
            mContentContainer.addView(mContentRender.onCreateView(
                    inflater, mContentContainer));
        }

        if (isShowFragment && isShowCommentFragment) {
            isShowCommentFragment = false;
            startAnimation(mLinearCommentLoading, mImageCommentLoading);
        }
        if (isShowNullDate) {
            mImageNull.setBackgroundResource(LabelStoryUtils.getCommentNull(mActivity));
        }
        initDate();
        mCommentListView.setAdapter(mLabelStoryDetailAdapter);
        mImageNull.setImageResource(LabelStoryUtils.getCommentNull(mActivity));
        if (isKeyBroad) {
            mDetailHideClick.setVisibility(View.VISIBLE);
            showSoftInput();
        } else {
            WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
            lp.softInputMode |= WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mContentRender != null) {
            mContentRender.onDestroyView();
        }
    }

    private void initView(View view) {
        view.findViewById(R.id.operation_show_user).setVisibility(View.GONE);
        mCommentListView = (NoScrollListview) view.findViewById(R.id.desdcript_comment);
        mPraiseLinear = (LinearLayout) view.findViewById(R.id.operation_praise_user);
        mScrollView = (ScrollView) view.findViewById(R.id.descript_scroll);
        mContentArea = (LinearLayout) view.findViewById(R.id.details_content_area);
        mScrollView.scrollTo(0, 0);
        mCommentListView.setFocusable(false);
        mLabelNameLinear = (LinearLayout) view.findViewById(R.id.story_item_label);
        mLinearLoading = (LinearLayout) view.findViewById(R.id.descript_loading_linear);
        mImageLoading = (ImageView) view.findViewById(R.id.descript_loading);
        mLinearCommentLoading = (LinearLayout) view.findViewById(R.id.descript_comment_loading_linear);
        mImageCommentLoading = (ImageView) view.findViewById(R.id.descript_comment_loading);
        mImageNull = (ImageView) view.findViewById(R.id.desdcript_comment_null);
        mImageNull.setVisibility(View.VISIBLE);
        mImageTx = (CircleImageView) view.findViewById(R.id.descript_tx);
        mDetailName = (TextView) view.findViewById(R.id.descript_name);
        mDetailTime = (TextView) view.findViewById(R.id.descript_time);
        mDetailReplyName = (TextView) view.findViewById(R.id.comment_text_reply);

        mLinearLoading = (LinearLayout) view.findViewById(R.id.descript_loading_linear);
        mDetailLabelName = (TextView) view.findViewById(R.id.story_item_label_name);

        view.findViewById(R.id.operation_bar_more).setOnClickListener(mOnClickListener);
        mImageFollowing = (TextView) view.findViewById(R.id.descript_following);
        mImageFollower = (ImageView) view.findViewById(R.id.descript_following_icon);
        mFollowerLinear = (LinearLayout) view.findViewById(R.id.descript_following_linear);
        mImagePraise = (ImageView) view.findViewById(R.id.operation_bar_praise);
        mImageLetter = (ImageView) view.findViewById(R.id.operation_bar_letter);
        mDetailPraiseNumber = (TextView) view.findViewById(R.id.operation_bar_praise_num);
        mDetailLetterNumber = (TextView) view.findViewById(R.id.operation_bar_letter_num);
        mDetailCommentNumber = (TextView) view.findViewById(R.id.operation_bar_comment_num);
        mCommentParent = (ClickEventInterceptLinear) view.findViewById(R.id.operation_bar_comment_parent);
        mPraiseUserList = (HorizontalListView) view.findViewById(R.id.praise_user_list);
        mUserPraise = (ImageView) view.findViewById(R.id.praise_user_image);

        mEmojiSelector = (EmojiSelector) view.findViewById(R.id.chatting_ui_input_emoji_layout);
        mFaceImageButton = (ImageButton) view.findViewById(R.id.comment_ui_face_switch_btn);
        mSendComment = (Button) view.findViewById(R.id.comment_ui_input_send_btn);
        mEmojiEditText = (EmojiEditText) view.findViewById(R.id.comment_ui_input_edit);
        mInputHint = (ShowContentTextView) view.findViewById(R.id.comment_ui_input_hint);
        mDetailHideClick = (FrameLayout) view.findViewById(R.id.detail_hint_click);
        KeyboardStateView keyboardStateView = (KeyboardStateView)
                view.findViewById(R.id.keyboard_state_view);
        keyboardStateView.setOnKeyboardStateChangedListener(mKeyboardStateChangedListener);
        onClickListener();
    }

    private boolean isContact(String contactId) {
        UserContact[] userContacts = contactsManager.getAllUserContact();
        if (userContacts != null && userContacts.length > 0) {
            for (UserContact userContact : userContacts) {
                if (userContact.getUserId().equals(contactId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void onClickListener() {
        mCommentListView.setOnItemClickListener(onItemClickListener);
        mPraiseUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UILauncher.launchFragmentPraiseCrowd(getActivity(), mLabelStory.getUserPraise());
            }
        });
        mImageLetter.setOnClickListener(mOnClickListener);
        mImagePraise.setOnClickListener(mOnClickListener);
        mLabelNameLinear.setOnClickListener(mOnClickListener);
        mImageTx.setOnClickListener(mOnClickListener);
        mEmojiEditText.addTextChangedListener(textWatcher);
        mFollowerLinear.setOnClickListener(mOnClickListener);
        mSendComment.setOnClickListener(mOnClickListener);
        mFaceImageButton.setOnClickListener(mOnClickListener);
        mCommentParent.setOnClickListener(mOnClickListener);
        mInputHint.setOnClickListener(mOnClickListener);
        mDetailHideClick.setOnClickListener(mOnClickListener);
        mEmojiEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mInputMethodManager.showSoftInput(mEmojiEditText, 0);
                showEmojiSelector(false);
                return false;
            }
        });
        mEmojiEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (mEmojiEditText.getText().toString().length() < mCount && !isGroup) {
                        mEmojiEditText.setText("");
                        clearReply();
                    }
                }
                return false;
            }
        });
        mEmojiSelector.setOnEmojiClickedListener(new EmojiSelector.OnEmojiClickedListener() {
            @Override
            public void onEmojiClicked(String emoji) {
                EmojiKeyboard.input(mEmojiEditText, emoji);
            }

            @Override
            public void onBackspace() {
                EmojiKeyboard.backspace(mEmojiEditText);
            }
        });
    }

    private void initDate() {
        if (isShowFragment && mLabelStory != null) {
            if (mLabelStory.getStranger() != null) {
                if (SettingHelper.getInstance(mActivity).getAccountUserId().equals(
                        mLabelStory.getStranger().getUserId())) {
                    mFollowerLinear.setVisibility(View.GONE);
                } else {
                    mFollowerLinear.setVisibility(View.VISIBLE);
                    isFollowing();
                }
                Stranger stranger = mLabelStory.getStranger();
                String title = MiscUtils.getUserRemarkName(mActivity, stranger.getUserId());
                mDetailName.setText(title != null && title.length() > 0 ? title : stranger.getNickname());
                MiscUtils.showAvatarThumb(mAvatarManager, mLabelStory.getStranger()
                        .getAvatarThumb(), mImageTx);
            } else {
                mFollowerLinear.setVisibility(View.GONE);
                mDetailName.setText(SettingHelper.getInstance(mActivity).getAccountNickname());
                MiscUtils.showAvatarThumb(mAvatarManager, SettingHelper.getInstance(mActivity)
                        .getAccountAvatarThumb(), mImageTx);
            }
            mDetailTime.setText(getTimeString(mLabelStory.getCreateDate()));
            initLabel();
            isPraise();

            mContentRender.bindContentData(mLabelStory);
        } else {
            showFragment();
        }
    }

    private void showEmojiSelector(boolean show) {
        mEmojiSelector.setVisibility(show ? View.VISIBLE : View.GONE);
        mFaceImageButton.setImageResource(show ? R.drawable.ic_input_keyboard_selector
                : R.drawable.ic_input_face_selector);
        mIsFaceShow = show;
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

    private void isPraise() {
        mDetailPraiseNumber.setText(mLabelStory.getPraise());
        mDetailCommentNumber.setText(mLabelStory.getCommentNum());
        mDetailLetterNumber.setText(mLabelStory.getLetterNum() + "");
        mDetailPraiseNumber.setVisibility("0".equals(mLabelStory.getPraise()) ? View.INVISIBLE : View.VISIBLE);
        mDetailCommentNumber.setVisibility("0".equals(mLabelStory.getCommentNum()) ? View.INVISIBLE : View.VISIBLE);
        mDetailLetterNumber.setVisibility(mLabelStory.getLetterNum() == 0 ? View.INVISIBLE : View.VISIBLE);
        if ("Y".equals(mLabelStory.getIsPraise())) {
            mImagePraise.setImageResource(R.drawable.ic_praise_pressed);
            mUserPraise.setImageResource(R.drawable.ic_praise_user_pressed);
        } else {
            mImagePraise.setImageResource(R.drawable.ic_praise_normal);
            mUserPraise.setImageResource(R.drawable.ic_praise_user_normal);
        }
        if (mLabelStory.getUserPraise() == null || mLabelStory.getUserPraise().length == 0) {
            mPraiseLinear.setVisibility(View.GONE);
        } else {
            mPraiseLinear.setVisibility(View.VISIBLE);
            mPraiseUserList.setAdapter(mPraiseUserAdapter);
            mPraiseUserAdapter.upDatePraiseUser(mLabelStory.getUserPraise());
        }
    }

    private void isFollowing() {
        if (tag == LabelStoryUtils.FOLLOW) {
            mImageFollowing.setTextColor(getResources().getColor(R.color.followed));
            mImageFollowing.setText(R.string.labelstory_attentioned);
            mImageFollower.setImageResource(R.drawable.followed_icon);
            mFollowerLinear.setBackgroundResource(R.drawable.followed);
        } else {
            if (mLabelStory.getIsFollowing() != null) {
                if (isContact(mLabelStory.getStranger().getUserId())) {
                    mImageFollowing.setTextColor(getResources().getColor(R.color.check_more));
                    mImageFollowing.setText(R.string.main_activity_tab_friends_description);
                    mImageFollower.setImageResource(R.drawable.friends_icon);
                    mFollowerLinear.setBackgroundResource(R.drawable.friends);
                } else {
                    if ("Y".equals(mLabelStory.getIsFollowing())) {
                        mImageFollowing.setTextColor(getResources().getColor(R.color.followed));
                        mImageFollowing.setText(R.string.labelstory_attentioned);
                        mImageFollower.setImageResource(R.drawable.followed_icon);
                        mFollowerLinear.setBackgroundResource(R.drawable.followed);
                    } else {
                        mImageFollowing.setTextColor(getResources().getColor(R.color.follow));
                        mImageFollowing.setText(R.string.labelstory_attention);
                        mImageFollower.setImageResource(R.drawable.follow_icon);
                        mFollowerLinear.setBackgroundResource(R.drawable.follow);
                    }
                }
            }
        }
    }

    public void initLabel() {
        if (!TextUtils.isEmpty(mCategoryName)) {
            mLabelNameLinear.setVisibility(View.VISIBLE);
            mDetailLabelName.setText(mCategoryName);
        } else {
            if (mLabelStory.getCategory() != null) {
                mLabelNameLinear.setVisibility(View.VISIBLE);
                mDetailLabelName.setText(mLabelStory.getCategory().getmCategoryName());
            } else {
                mLabelNameLinear.setVisibility(View.GONE);
            }
        }
    }

    private void argumentParam() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mLabelStory = bundle.getParcelable(LabelStoryFragment.LABEL_STORY);
            isShowFragment = bundle.getBoolean(LabelStoryUtils.LABEL_STORY_SHOW);
            mCategoryName = bundle.getString(LabelStoryUtils.CATEGORY_NAME);
            isShowTitle = bundle.getBoolean(LabelStoryUtils.LABEL_STORY_TITLE_SHOW);
            isPraise = bundle.getBoolean(LabelStoryUtils.IS_PRAISE);
            isComment = bundle.getBoolean(LabelStoryUtils.IS_COMMENT);
            tag = bundle.getInt(LabelStoryUtils.TAG);
            isKeyBroad = bundle.getBoolean(LabelStoryUtils.IS_KEYBROAD);
            if (isShowFragment) {
                mContentRender = StoryRenderFactory.newRender(mActivity, mLabelStory);
                mContentRender.onCreate();
            } else {
                mContentRender = null;
            }
        }
        final ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void loadCommentList() {
        mCommentNext++;
        LabelStoryManager.LabelStoryCommentListQueryObserver observer
                = new LabelStoryManager.LabelStoryCommentListQueryObserver() {
            @Override
            public void onQueryResult(int result, LabelStory labelStory, boolean remaining) {
                if (labelStory != null) {
                    Message message = Message.obtain(mHandler, COMMENT_LIST_REQUEST_CODE,
                            result, 0, labelStory);
                    mHandler.sendMessage(message);
                }
            }
        };
        if (mLabelStory != null) {
            mLabelStoryManager.commentListLabelStory(mLabelStory.getLabelStoryId(),
                    String.valueOf(mCommentNext), observer);
        }
    }

    private void shareContent(ShareContent content) {
        mContentSharer.setShareContent(content);
        mContentSharer.openSharePanel();
    }

    private void comment(LabelStoryComments comment) {
        LabelStoryManager.LabelStoryCommentQueryObserver observer
                = new LabelStoryManager.LabelStoryCommentQueryObserver() {
            @Override
            public void onQueryResult(int result, LabelStoryComments labelStoryComments,
                                      boolean remaining) {
                isGroup = true;
                Message message = Message.obtain(mHandler, COMMENT_REQUEST_CODE,
                        result, 0, labelStoryComments);
                mHandler.sendMessage(message);
            }
        };

        mLabelStoryManager.commentLabelStory(comment, observer);
    }

    private void praise(String labelStoryId, List<String> userIds) {
        LabelStoryManager.LabelStoryQueryObserver observer
                = new LabelStoryManager.LabelStoryQueryObserver() {
            @Override
            public void onQueryResult(int result, LabelStory[] labelStories,
                                      boolean remaining, int frendsCount) {
            }

            @Override
            public void onPraiseQueryResult(int result, boolean remaining) {
                if (result == LabelStoryManager.QUERY_RESULT_SUCCESS) {
                    Message msg = mHandler.obtainMessage(PRAISE_REQUEST_CODE);
                    mHandler.sendMessage(msg);
                } else if (result == LabelStoryManager.QUERY_RESULT_EXIT_PRAISE) {
                    Message msg = mHandler.obtainMessage(PRAISE_REQUEST_EXIT_CODE);
                    mHandler.sendMessage(msg);
                }
            }
        };
        mLabelStoryManager.praiseLabelStory(labelStoryId, userIds, observer);
    }

    private void praiseCommentsLabelStory(String labelStoryCommentId, final int position) {
        LabelStoryManager.LabelStoryQueryObserver observer = new LabelStoryManager.LabelStoryQueryObserver() {
            @Override
            public void onQueryResult(int result, LabelStory[] labelStories,
                                      boolean remaining, int frendsCount) {
            }

            @Override
            public void onPraiseQueryResult(int result, boolean remaining) {
                if (result == LabelStoryManager.QUERY_RESULT_SUCCESS) {
                    Message msg = mHandler.obtainMessage(COMMENT_PRAISE_REQUEST_CODE, position);
                    mHandler.sendMessage(msg);
                } else if (result == LabelStoryManager.QUERY_RESULT_EXIT_PRAISE) {
                    Message msg = mHandler.obtainMessage(PRAISE_REQUEST_EXIT_CODE);
                    mHandler.sendMessage(msg);
                }
            }
        };
        mLabelStoryManager.praiseLabelStoryComments(labelStoryCommentId, observer);
    }

    private void sendLetter(String strangerUserId, String message) {
        LabelStoryManager.LabelStoryLetterQueryObserver observer = new LabelStoryManager.LabelStoryLetterQueryObserver() {
            @Override
            public void onQueryResult(int result, boolean remaining) {
                Message message = Message.obtain(mHandler, LETTER_REQUEST_CODE, result);
                mHandler.sendMessage(message);
            }
        };
        mLabelStoryManager.letterLabelStory(mLabelStory.getLabelStoryId(), strangerUserId, message, observer);
    }

    private void following(String followUserId) {
        FollowingManager.FollowingCountQueryObserver observer = new FollowingManager.FollowingCountQueryObserver() {
            @Override
            public void onQueryResult(int result, int followCount, boolean remaining) {
                Message message = Message.obtain(mHandler, FOLLOWING_REQUEST_CODE, result, followCount);
                mHandler.sendMessage(message);
            }
        };
        mFollowingManager.followingUserCountInfo(followUserId, observer);
    }

    private class PraiseUserAdapter extends BaseAdapter {

        private UserPraise[] pUserPraises = null;
        private LayoutInflater inflater;

        public PraiseUserAdapter() {
            inflater = LayoutInflater.from(mActivity);
        }

        public void upDatePraiseUser(UserPraise[] userPraises) {
            pUserPraises = userPraises;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return pUserPraises == null ? 0 : pUserPraises.length;
        }

        @Override
        public UserPraise getItem(int position) {
            return pUserPraises[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.labelstory_praise_user_image,
                        parent, false);
            }
            ImageView imageView = (ImageView) ViewHolder.get(convertView,
                    R.id.labelstory_praise_iamge);
            MiscUtils.showAvatarThumb(mAvatarManager,
                    getItem(position).getmPraiseUserAvatarThumb(),
                    imageView);
            return convertView;
        }
    }

    private String getTimeString(long time) {
        return DateTimeUtils.getDescriptionTimeFromTimestamp(mActivity, time);
    }
}
