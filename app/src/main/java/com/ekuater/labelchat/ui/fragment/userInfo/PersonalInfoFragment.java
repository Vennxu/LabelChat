package com.ekuater.labelchat.ui.fragment.userInfo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.Interact;
import com.ekuater.labelchat.datastruct.PersonalUser;
import com.ekuater.labelchat.datastruct.PushInteract;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserInterest;
import com.ekuater.labelchat.datastruct.UserTag;
import com.ekuater.labelchat.datastruct.UserTheme;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.FollowingManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.delegate.InterestManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.delegate.ThemeManager;
import com.ekuater.labelchat.ui.ContentSharer;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.BaseActivity;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.fragment.SimpleEditDialog;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.fragment.UserShowFragment;
import com.ekuater.labelchat.ui.fragment.friends.ValidateMessageDialog;
import com.ekuater.labelchat.ui.fragment.labelstory.LabelStoryUtils;
import com.ekuater.labelchat.ui.fragment.labelstory.PrivateLetterFragmentDialog;
import com.ekuater.labelchat.ui.util.CompatUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.util.InterestUtils;
import com.ekuater.labelchat.util.TextUtil;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2015/3/16.
 *
 * @author Xu Wenxiang
 */
public class PersonalInfoFragment extends HeaderFragment implements Handler.Callback {

    private static final int MSG_QUERY_CONTACT_INFO_RESULT = 101;
    private static final int MSG_FRIEND_DELETE_RESULT = 102;
    private static final int MSG_SEND_VISIT_INVITE = 103;
    private static final int MSG_ADD_FRIEND_REQUEST_RESULT = 104;
    private static final int MSG_CANCEL_FOLLOW_USER_RESULT = 105;
    private static final int MSG_REFRESH_UI = 106;
    private static final int MSG_FOLLOW_USER_RESULT = 107;
    private static final int MSG_INTERACT_RESULT = 108;
    private static final int MSG_MODIFY_REMARK_RESULT = 109;

    private Activity mContext;
    private AvatarManager mAvatarManager;
    private ListViewUpNoRefresh mListView;
    private PersonalInfoAdapter mPersonalInfoAdapter;

    private AsyncLoadSomething mAsyncLoadSomething;

    private FollowingManager mFollowingManager;
    private ContactsManager mContactsManager;

    private UserContact mUserContact;
    private PersonalUser mPersonalUser;
    private ThemeManager mThemeManager;
    private AccountManager mAccountManager;
    private InterestManager mInterestManager;
    private UserTheme mUserTheme;
    private SimpleProgressDialog mProgressDialog;
    private SimpleProgressHelper progressHelper;
    private String queryUserId;
    private PersonalUserItem.StrangerItem strangerItem;
    private Handler mHandler = new Handler(this);

    private void handlerPushInteract(int constanCode) {
        switch (constanCode) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                ShowToast.makeText(mContext, R.drawable.emoji_smile, mContext.getResources().getString(R.string.remaind_success)).show();
                break;
            default:
                ShowToast.makeText(mContext, R.drawable.emoji_cry, mContext.getResources().getString(R.string.remaind_failure)).show();
                break;
        }
    }

    private void queryContactInfo(String queryUserId) {
        mContactsManager.queryContactInfo(queryUserId, new ContactsManager.ContactQueryObserver() {
            @Override
            public void onQueryResult(int result, UserContact contact) {
                progressHelper.show();
                Message message = mHandler.obtainMessage(MSG_QUERY_CONTACT_INFO_RESULT, contact);
                mHandler.sendMessage(message);
            }
        });
    }

    private void queryStrangerInfo(String queryUserId) {
        mContactsManager.queryUserInfo(queryUserId, new ContactsManager.UserQueryObserver() {
            @Override
            public void onQueryResult(int result, Stranger user) {
                progressHelper.show();
                UserContact userContact = new UserContact(user);
                Message message = mHandler.obtainMessage(MSG_QUERY_CONTACT_INFO_RESULT, userContact);
                mHandler.sendMessage(message);
            }
        });
    }

    private void handlerQueryContactInfoResult(UserContact contact) {
        progressHelper.dismiss();
        mPersonalInfoAdapter.getContactInfo(contact, interactListener);
        mListView.initData(mContext, mPersonalUser);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setHeaderBackgroundScrollMode(HEADER_BACKGROUND_SCROLL_PARALLAX);
        setOnHeaderScrollChangedListener(new OnHeaderScrollChangedListener() {
            @Override
            public void onHeaderScrollChanged(float progress, int height, int scroll) {
                height -= getActivity().getActionBar().getHeight();
                progress = (float) scroll / height;
                if (progress > 1f) {
                    progress = 1f;
                }
                progress = (1 - (float) Math.cos(progress * Math.PI)) * 0.5f;
                ((FadingActionBarActivity) getActivity())
                        .getFadingActionBarHelper()
                        .setActionBarAlpha((int) (255 * progress));
            }
        });

        cancelAsyncTask(mAsyncLoadSomething);
        mAsyncLoadSomething = new AsyncLoadSomething(this);
        mAsyncLoadSomething.execute();
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_QUERY_CONTACT_INFO_RESULT:
                handlerQueryContactInfoResult((UserContact) msg.obj);
                break;
            case MSG_FRIEND_DELETE_RESULT:
                handleDeleteFriendResult(msg.obj);
                break;
            case MSG_SEND_VISIT_INVITE:
                handlerSendVisitInvite(msg.arg1);
                break;
            case LabelStoryUtils.LETTER_REQUEST_CODE:
                onLetterHandler(msg);
                break;
            case MSG_ADD_FRIEND_REQUEST_RESULT:
                handleAddFriendRequestResult(msg.arg1);
                break;
            case MSG_FOLLOW_USER_RESULT:
                handlerFollowUser(msg.arg1, msg.arg2);
                break;
            case MSG_CANCEL_FOLLOW_USER_RESULT:
                handlerUnfollowUser(msg.arg1);
                break;
            case MSG_REFRESH_UI:
                strangerItem.changeUI();
                break;
            case MSG_INTERACT_RESULT:
                handlerPushInteract(msg.arg1);
                break;
            case MSG_MODIFY_REMARK_RESULT:
                handleModifyRemarkResult((ModifyResult) msg.obj);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private static class DeleteFriendResult {

        public final int result;
        public final String friendUserId;
        public final String friendLabelCode;

        public DeleteFriendResult(int result, String friendUserId, String friendLabelCode) {
            this.result = result;
            this.friendUserId = friendUserId;
            this.friendLabelCode = friendLabelCode;
        }
    }

    private ContactsManager.IListener mContactListener = new ContactsManager.AbsListener() {
        @Override
        public void onModifyFriendRemarkResult(int result, String friendUserId,
                                               final String friendRemark) {
            mHandler.obtainMessage(MSG_MODIFY_REMARK_RESULT,
                    new ModifyResult(result, friendUserId, friendRemark))
                    .sendToTarget();
        }

        @Override
        public void onDeleteFriendResult(int result, String friendUserId, String friendLabelCode) {
            Message message = mHandler.obtainMessage(MSG_FRIEND_DELETE_RESULT,
                    new DeleteFriendResult(result, friendUserId, friendLabelCode));
            mHandler.sendMessage(message);
        }
    };

    private PersonalInfo.InteractListener interactListener = new PersonalInfo.InteractListener() {
        @Override
        public void onClick(View view, Interact interact) {
            cover.setVisibility(View.VISIBLE);
            showPopupWindow(view, interact);
        }
    };

    private void handleDeleteFriendResult(Object object) {
        if (object instanceof DeleteFriendResult) {
            DeleteFriendResult result = (DeleteFriendResult) object;

            switch (result.result) {
                case ConstantCode.CONTACT_OPERATION_SUCCESS:
                    ShowToast.makeText(mContext, R.drawable.emoji_sad, mContext.getResources().getString(R.string.delete_friend_success)).show();
                    getActivity().finish();
                    break;
                default:
                    ShowToast.makeText(mContext, R.drawable.emoji_cry, mContext.getResources().getString(R.string.delete_friend_failure)).show();
                    break;
            }

            dismissProgressDialog();
        }
    }

    private void sendVisitInvite(String userId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(getActivity(), R.raw.notify_knock);
        mediaPlayer.start();
        mFollowingManager.sendInviteNotify(userId, new FunctionCallListener() {
            @Override
            public void onCallResult(int result, int errorCode, String errorDesc) {
                Message message = mHandler.obtainMessage(MSG_SEND_VISIT_INVITE, result, 0);
                mHandler.sendMessage(message);
            }
        });
    }

    private void handlerSendVisitInvite(int result) {
        String sex;
        switch (mUserContact.getSex()) {
            case 1:
                sex = mContext.getResources().getString(R.string.he);
                break;
            case 2:
                sex = mContext.getResources().getString(R.string.her);
                break;
            default:
                sex = mContext.getResources().getString(R.string.he);
                break;
        }
        if (result == FunctionCallListener.RESULT_CALL_SUCCESS) {
            ShowToast.makeText(mContext, R.drawable.emoji_smile, mContext.getResources().getString(R.string.say_hello, sex)).show();
        } else {
            ShowToast.makeText(mContext, R.drawable.emoji_cry, mContext.getResources().getString(R.string.say_hello_failure)).show();
        }
    }

    @Override
    public void onDetach() {
        cancelAsyncTask(mAsyncLoadSomething);
        super.onDetach();
    }

    private TextView nickname;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressHelper = new SimpleProgressHelper(getActivity());
        mContext = getActivity();
        parseArguments();
        mPersonalInfoAdapter = new PersonalInfoAdapter(mContext, mUserContact);
        mFollowingManager = FollowingManager.getInstance(mContext);
        mContactsManager = ContactsManager.getInstance(mContext);
        mAvatarManager = AvatarManager.getInstance(mContext);
        mThemeManager = ThemeManager.getInstance(mContext);
        mAccountManager = AccountManager.getInstance(mContext);
        mInterestManager = InterestManager.getInstance(mContext);
        mUserTheme = mUserContact.getTheme();
        BaseActivity baseActivity = (BaseActivity) mContext;
        baseActivity.setHasContentSharer();
        mContentSharer = baseActivity.getContentSharer();
        mLabelStoryUtils = new LabelStoryUtils(PersonalInfoFragment.this, mContentSharer, mHandler);
        ImageView leftIcon = (ImageView) getActivity().getActionBar().getCustomView().findViewById(R.id.left_icon);
        nickname = (TextView) getActivity().getActionBar().getCustomView().findViewById(R.id.nickname);
        ImageView rightIcon = (ImageView) getActivity().getActionBar().getCustomView().findViewById(R.id.right_icon);
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        nickname.setText(mUserContact.getShowName());

        mProgressHelper = new SimpleProgressHelper(baseActivity);
        if (mPersonalUser.getType() == PersonalUser.CONTACT) {
            rightIcon.setImageResource(R.drawable.more_option);
            rightIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onModifyRemark();
                }
            });
        } else {
            rightIcon.setVisibility(View.GONE);
        }

        mContactsManager.registerListener(mContactListener);
        queryUserId = mUserContact.getUserId();
        if (queryUserId.equals(getString(R.string.app_team_user_id))) {
            rightIcon.setVisibility(View.GONE);
        }
        if (mPersonalUser.getType() == PersonalUser.CONTACT) {
            queryContactInfo(queryUserId);
        } else {
            queryStrangerInfo(queryUserId);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContactsManager.unregisterListener(mContactListener);
    }

    private void parseArguments() {
        Bundle argument = getArguments();
        if (argument != null) {
            mPersonalUser = argument.getParcelable(UserShowFragment.EXTRA_PERSONAL);
            mUserContact = mPersonalUser.getUserContact();
        }
    }

    private SimpleProgressHelper mProgressHelper;
    private boolean mInModifyRemark = false;

    private class RemarkEditListener implements SimpleEditDialog.IListener {

        @Override
        public void onCancel(CharSequence text) {
        }

        @Override
        public void onOK(CharSequence text) {
            modifyRemark(text.toString());
        }
    }

    private static class ModifyResult {

        public final int mResult;
        public final String mFriendUserId;
        public final String mFriendRemark;

        public ModifyResult(int result, String friendUserId,
                            String friendRemark) {
            mResult = result;
            mFriendUserId = friendUserId;
            mFriendRemark = friendRemark;
        }
    }

    private void onModifyRemark() {
        SimpleEditDialog.UiConfig config = new SimpleEditDialog.UiConfig();
        config.title = getString(R.string.modify_remark);
        config.initText = mUserContact.getRemarkName();
        config.editHint = getString(R.string.friend_remark_hint);
        config.maxLength = getActivity().getResources().getInteger(
                R.integer.friend_remark_max_length);
        config.listener = new RemarkEditListener();
        SimpleEditDialog.newInstance(config).show(getFragmentManager(), "RemarkEditDialog");
    }

    private void modifyRemark(String newRemark) {
        if (!mInModifyRemark) {
            newRemark = (newRemark == null) ? "" : newRemark;
            mInModifyRemark = true;
            mProgressHelper.show();
            mContactsManager.modifyFriendRemark(mUserContact.getUserId(), newRemark);
        }
    }

    private void handleModifyRemarkResult(ModifyResult result) {
        if (!mUserContact.getUserId().equals(result.mFriendUserId)) {
            return;
        }

        if (mInModifyRemark) {
            if (result.mResult == ConstantCode.CONTACT_OPERATION_SUCCESS) {
                ShowToast.makeText(getActivity(), R.drawable.emoji_smile, getActivity().
                        getResources().getString(R.string.modify_remark_success)).show();
                mUserContact.setRemarkName(result.mFriendRemark);
                nickname.setText(mUserContact.getShowName());
            } else {
                ShowToast.makeText(getActivity(), R.drawable.emoji_cry, getActivity().
                        getResources().getString(R.string.modify_remark_failure)).show();
            }
            mInModifyRemark = false;
            mProgressHelper.dismiss();
        }
    }

    private ImageView topImage;
    private CircleImageView avatarImage;
    private TextView userTagText;
    private TextView userInterestText;

    @Override
    public View onCreateHeaderView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.contact_info_header, container, false);
        topImage = (ImageView) view.findViewById(R.id.top_background);
        avatarImage = (CircleImageView) view.findViewById(R.id.contact_avatar_image);
        String imageThumbPath = mUserContact.getAvatarThumb();
        final String imagePath = mUserContact.getAvatar();
        if (imageThumbPath != null) {
            mAvatarManager.displayAvatarThumb(imageThumbPath, avatarImage, R.drawable.temp_small_pics_1);
        } else {
            avatarImage.setBackgroundResource(R.drawable.temp_small_pics_1);
        }
        avatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imagePath != null) {
                    UILauncher.launchShowFriendAvatarImage(mContext, imagePath);
                }
            }
        });
        if (mUserTheme != null) {
            mThemeManager.displayThemeImage(mUserTheme.getTopImg(), topImage, R.drawable.user_show_bg);
        } else {
            topImage.setBackgroundResource(R.drawable.user_show_bg);
        }

        TextView regionText = (TextView) view.findViewById(R.id.region);
        TextView signatureText = (TextView) view.findViewById(R.id.signature);
        regionText.setText(TextUtil.isEmpty(mUserContact.getCity())
                ? mUserContact.getProvince() : mUserContact.getCity());
        signatureText.setText(mUserContact.getSignature());
        return view;
    }

    private LinearLayout personalOption;
    private View cover;
    private String letterMsg = null;
    private LabelStoryUtils mLabelStoryUtils;
    private ContentSharer mContentSharer;

    private PersonalItem.UserClickListener userClickListener = new PersonalItem.UserClickListener() {
        @Override
        public void letterClick() {
            sendEmail();
        }

        @Override
        public void tallClick() {
            if (mPersonalUser.getType() == PersonalUser.CONTACT) {
                UILauncher.launchChattingUI(mContext, mUserContact.getUserId());
            } else {
                UILauncher.launchStrangerChattingUI(mContext, new Stranger(mUserContact));
            }
        }

        @Override
        public void inviteClick() {
            sendVisitInvite(mUserContact.getUserId());
        }

        @Override
        public void attentionClick() {
            followUser(mUserContact.getUserId());
        }

        @Override
        public void addClick() {
            onAddAsFriend();
        }

        @Override
        public void unFollowClick() {
            unfollowUser(mUserContact.getUserId());
        }

        @Override
        public void reportClick() {
            MiscUtils.complainUser(mContext, mUserContact.getUserId());
        }

        @Override
        public void deleteFriendClick() {
            showDeleteFriendConfirm();
        }
    };

    @Override
    public View onCreateContentView(LayoutInflater inflater, final ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_personal, container, false);
        personalOption = (LinearLayout) view.findViewById(R.id.personal_option);
        mListView = (ListViewUpNoRefresh) view.findViewById(R.id.list);
        personalOption.addView(childOpertionView(inflater));
        return view;
    }

    @Override
    public View onCoverView(LayoutInflater inflater, ViewGroup container) {
        cover = inflater.inflate(R.layout.cover_view, container, false);
        userTagText = (TextView) cover.findViewById(R.id.interact_user_tag);
        cover.setVisibility(View.GONE);
        return cover;
    }

    private View childOpertionView(LayoutInflater inflater) {
        View personalView = null;
        switch (mPersonalUser.getType()) {
            case PersonalUser.CONTACT:
                PersonalUserItem.ContactItem contactUserItem = new PersonalUserItem.ContactItem(mContext, userClickListener);
                personalView = contactUserItem.newView(inflater, personalOption);
                break;
            case PersonalUser.STRANGER:
                strangerItem = new PersonalUserItem.StrangerItem(mContext, mUserContact, userClickListener);
                personalView = strangerItem.newView(inflater, personalOption);
                break;
            default:
                break;
        }
        return personalView;
    }

    private void showDeleteFriendConfirm() {
        ConfirmDialogFragment.UiConfig uiConfig
                = new ConfirmDialogFragment.UiConfig(
                getString(R.string.delete_friend_confirm),
                null, false);
        ConfirmDialogFragment.IConfirmListener confirmListener
                = new ConfirmDialogFragment.AbsConfirmListener() {
            @Override
            public void onConfirm() {
                startDeleteFriend();
            }
        };
        ConfirmDialogFragment fragment = ConfirmDialogFragment.newInstance(uiConfig,
                confirmListener);
        fragment.show(getFragmentManager(), "DeleteFriendConfirm");
    }

    private void startDeleteFriend() {
        showProgressDialog();
        mContactsManager.deleteFriend(mUserContact.getUserId(), mUserContact.getLabelCode());
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = SimpleProgressDialog.newInstance();
            mProgressDialog.show(getFragmentManager(), "ProgressDialog");
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void sendEmail() {
        PrivateLetterFragmentDialog dialog = PrivateLetterFragmentDialog.newInstance(null, mUserContact.getAvatarThumb(),
                mUserContact.getUserId(), mUserContact.getShowName(),
                mAvatarManager, 0, new PrivateLetterFragmentDialog.OnSendEmaileClicklistener() {
                    @Override
                    public void onSendEmaile(String labelStoryId, String userId, String message, int position) {
                        letterMsg = message;
                        mLabelStoryUtils.sendLetter(labelStoryId, userId, message, position);
                    }
                });
        dialog.show(getFragmentManager(), "PrivateLetterFragmentDialog");
    }

    private void onLetterHandler(Message message) {
        switch (message.arg1) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                LabelStoryUtils.insertSystemPush(PushMessageManager.getInstance(mContext), new Stranger(mUserContact), letterMsg);
                ShowToast.makeText(mContext, R.drawable.emoji_smile, mContext.getResources().getString(R.string.send_letter_succese)).show();
                break;
            default:
                ShowToast.makeText(mContext, R.drawable.emoji_cry, mContext.getResources().getString(R.string.send_letter_failed)).show();
                break;

        }
    }

    private void handleAddFriendRequestResult(int result) {
        progressHelper.dismiss();
        if (mContext != null) {
            if (result == FunctionCallListener.RESULT_CALL_SUCCESS) {
                ShowToast.makeText(mContext, R.drawable.emoji_smile, mContext.getResources().getString(R.string.add_friend_success)).show();
            } else {
                ShowToast.makeText(mContext, R.drawable.emoji_cry, mContext.getResources().getString(R.string.add_friend_failed)).show();
            }
        }
    }

    private void unfollowUser(String userId) {
        mFollowingManager.followingCancelUserInfo(userId, new FollowingManager.FollowingQueryObserver() {
            @Override
            public void onQueryResult(int result, boolean remaining) {
                Message message = mHandler.obtainMessage(MSG_CANCEL_FOLLOW_USER_RESULT, result, 0);
                mHandler.sendMessage(message);
            }
        });
    }

    private void handlerUnfollowUser(int result) {
        if (result == FollowingManager.RESULT_SUCCESS) {
            ShowToast.makeText(mContext, R.drawable.emoji_sad, mContext.getResources().getString(R.string.cancel_follow_success)).show();
            mHandler.sendEmptyMessage(MSG_REFRESH_UI);
        } else {
            ShowToast.makeText(mContext, R.drawable.emoji_cry, mContext.getResources().getString(R.string.cancel_follow_fail)).show();
        }
    }

    private void followUser(String userId) {
        mFollowingManager.followingUserCountInfo(userId, new FollowingManager.FollowingCountQueryObserver() {
            @Override
            public void onQueryResult(int result, int followingCount, boolean remaining) {
                Message message = mHandler.obtainMessage(MSG_FOLLOW_USER_RESULT, result, followingCount);
                mHandler.sendMessage(message);
            }
        });
    }


    private void handlerFollowUser(int result, int followCount) {
        String sex;
        switch (mUserContact.getSex()) {
            case 1:
                sex = mContext.getResources().getString(R.string.he);
                break;
            case 2:
                sex = mContext.getResources().getString(R.string.her);
                break;
            default:
                sex = mContext.getResources().getString(R.string.he);
                break;
        }
        if (result == mFollowingManager.RESULT_SUCCESS) {
            ShowToast.makeText(mContext, R.drawable.emoji_smile, mContext.getResources().getString(R.string.attention_success, sex, followCount)).show();
            mHandler.sendEmptyMessage(MSG_REFRESH_UI);
        } else {
            ShowToast.makeText(mContext, R.drawable.emoji_cry, mContext.getResources().getString(R.string.follow_fail)).show();
        }
    }

    private void setListView() {
        if (mListView == null) return;
        mListView.setVisibility(View.VISIBLE);
        setListViewAdapter(mListView, mPersonalInfoAdapter);
    }

    private void onAddAsFriend() {
        showValidateMessageDialog();
    }

    private void showValidateMessageDialog() {
        ValidateMessageDialog.newInstance(new ValidateMessageDialog.Listener() {
            @Override
            public void onCancel(String message) {
            }

            @Override
            public void onSubmit(String message) {
                sendAddRequest(message);
            }
        }).show(getFragmentManager(), ValidateMessageDialog.class.getSimpleName());
    }

    private void sendAddRequest(String message) {
        if (!mAccountManager.getLabelCode().equals(mUserContact.getLabelCode())
                && !mAccountManager.getUserId().equals(mUserContact.getUserId())) {
            mContactsManager.requestAddFriend(mUserContact.getUserId(),
                    mUserContact.getLabelCode(), message,
                    new FunctionCallListener() {
                        @Override
                        public void onCallResult(int result, int errorCode, String errorDesc) {
                            Message msg = mHandler.obtainMessage(MSG_ADD_FRIEND_REQUEST_RESULT,
                                    result, errorCode, errorCode);
                            mHandler.sendMessage(msg);
                        }
                    });
            progressHelper.show();
        } else {
            ShowToast.makeText(mContext, R.drawable.emoji_sad, mContext.getResources()
                    .getString(R.string.cannot_add_themselves_as_friends)).show();
        }
    }

    private PopupWindow popupWindow;
    private View view;
    private LayoutInflater layoutInflater;
    private TextView one;
    private TextView two;
    private TextView three;

    @SuppressLint("InflateParams")
    private void showPopupWindow(View v, Interact interact) {
        if (popupWindow == null) {
            layoutInflater = LayoutInflater.from(mContext);
            view = layoutInflater.inflate(R.layout.interact_more_pop, null, false);
            one = (TextView) view.findViewById(R.id.interact_one);
            two = (TextView) view.findViewById(R.id.interact_two);
            three = (TextView) view.findViewById(R.id.interact_three);
            popupWindow = PersonalUserItem.getPopupWindow(view);
            one.setOnClickListener(popupListener);
            two.setOnClickListener(popupListener);
            three.setOnClickListener(popupListener);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    cover.setVisibility(View.GONE);
                }
            });
        }
        one.setTag(interact);
        two.setTag(interact);
        three.setTag(interact);
        bindChangeView(v, interact);
    }

    private View.OnClickListener popupListener = new View.OnClickListener() {
        //TODO
        @Override
        public void onClick(View v) {
            String content = null;
            Interact interact = null;
            switch (v.getId()) {
                case R.id.interact_one:
                    interact = (Interact) v.getTag();
                    content = one.getText().toString();
                    if (interact != null && !TextUtil.isEmpty(content)) {
                        pushInteract(content, interact);
                    }
                    popupWindow.dismiss();
                    break;
                case R.id.interact_two:
                    content = two.getText().toString();
                    interact = (Interact) v.getTag();
                    if (interact != null && !TextUtil.isEmpty(content)) {
                        pushInteract(content, interact);
                    }
                    popupWindow.dismiss();
                    break;
                case R.id.interact_three:
                    content = three.getText().toString();
                    interact = (Interact) v.getTag();
                    if (interact != null && !TextUtil.isEmpty(content)) {
                        pushInteract(content, interact);
                    }
                    popupWindow.dismiss();
                    break;
                default:
                    break;
            }
        }
    };

    private void pushInteract(String content, Interact interact) {
        //TODO
        PushInteract message = getPushInteractMsg(content, interact);
        if (message != null) {
            mInterestManager.pushInteract(mPersonalUser.getUserContact().getUserId(), message, new InterestManager.PushInteractObserver() {
                @Override
                public void onQueryResult(int result) {
                    Message message = mHandler.obtainMessage(MSG_INTERACT_RESULT, result);
                    mHandler.sendMessage(message);
                }
            });
        }
    }

    private PushInteract getPushInteractMsg(String content, Interact interact) {
        PushInteract message = null;
        switch (interact.getType()) {
            case Interact.USER_TAG:
                UserTag userTag = interact.getUserTag();
                message = new PushInteract();
                message.setInteractType(String.valueOf(interact.getType()));
                message.setInteractObject(userTag.getTagName());
                message.setInteractOperate(content);
                message.setObjectType(String.valueOf(userTag.getTypeId()));
                break;
            case Interact.USER_INTEREST:
                UserInterest userInterest = interact.getUserInterest();
                message = new PushInteract();
                message.setInteractType(String.valueOf(interact.getType()));
                message.setInteractObject(userInterest.getInterestName());
                message.setInteractOperate(content);
                message.setObjectType(String.valueOf(userInterest.getInterestType()));
                break;
            default:
                break;
        }
        return message;
    }

    private void bindChangeView(View v, Interact interact) {
        switch (interact.getType()) {
            case Interact.USER_TAG:
                userTagText.setText(interact.getUserTag().getTagName());
                userTagText.setTextColor(mContext.getResources().getColor(R.color.white));
                GradientDrawable drawable = (GradientDrawable) mContext.getResources()
                        .getDrawable(R.drawable.corners_bg);
                if (drawable != null) {
                    drawable.setColor(interact.getUserTag().parseTagColor());
                    CompatUtils.setBackground(userTagText, drawable);
                }
                userTagText.setText(interact.getUserTag().getTagName());
                setPopupText(R.string.interact_me_too, R.string.interact_cattle_true_cow, R.string.interact_the_case);
                setPopuDrawable(R.drawable.emoji_f_static_1_10, R.drawable.emoji_f_static_4_200, R.drawable.emoji_f_static_1_150);
                getLocation(v, userTagText);
                break;
            case Interact.USER_INTEREST:
                userTagText.setTextColor(mContext.getResources().getColor(R.color.sport_bg));
                InterestUtils.setInterestColor(mContext, userTagText, interact.getUserInterest().getInterestType());
                userTagText.setText(interact.getUserInterest().getInterestName());
                setPopupText(R.string.interact_also_like, R.string.interact_very_fierce, R.string.interact_what_is_this);
                setPopuDrawable(R.drawable.emoji_f_static_1_10, R.drawable.emoji_f_static_4_200, R.drawable.emoji_f_static_2_130);
                getLocation(v, userTagText);
                break;
            default:
                break;
        }
    }

    private void setPopupText(int strOne, int strTwo, int strThree) {
        one.setText(getString(strOne));
        two.setText(getString(strTwo));
        three.setText(getString(strThree));
    }

    private void setPopuDrawable(int drawOne, int drawTwo, int drawThree) {
        one.setCompoundDrawables(null, null, getDrawable(drawOne), null);
        two.setCompoundDrawables(null, null, getDrawable(drawTwo), null);
        three.setCompoundDrawables(null, null, getDrawable(drawThree), null);
    }

    private Drawable getDrawable(int ids) {
        Drawable drawable = getResources().getDrawable(ids);
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                    drawable.getMinimumHeight());
        }
        return drawable;
    }

    private void getLocation(View v, TextView tx) {
        Rect frame = new Rect();
        mContext.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        DisplayMetrics dm = new DisplayMetrics();
        mContext.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int statusBarHeight = frame.top;
        int[] location = new int[2];
        v.getLocationInWindow(location);
        tx.setX(location[0]);
        tx.setY(location[1] - statusBarHeight);
        if (dm.heightPixels - location[1] > MiscUtils.dp2px(mContext, 140 + statusBarHeight)) {
            popupWindow.showAsDropDown(v, 0, MiscUtils.dp2px(mContext, 5));
        } else {
            popupWindow.showAsDropDown(v, 0, -MiscUtils.dp2px(mContext, 175));
        }
    }

    private void cancelAsyncTask(AsyncTask task) {
        if (task != null) task.cancel(false);
    }

    private static class AsyncLoadSomething extends AsyncTask<Void, Void, Void> {

        private static final String TAG = "AsyncLoadSomething";

        final WeakReference<PersonalInfoFragment> weakFragment;

        public AsyncLoadSomething(PersonalInfoFragment fragment) {
            this.weakFragment = new WeakReference<>(fragment);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            final PersonalInfoFragment audioFragment = weakFragment.get();
            if (audioFragment.mListView != null)
                audioFragment.mListView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            final PersonalInfoFragment audioFragment = weakFragment.get();
            if (audioFragment == null) {
                if (Project.DEBUG) Log.d(TAG, "Skipping.., because there is no fragment anymore.");
                return;
            }
            audioFragment.setListView();
        }
    }
}
