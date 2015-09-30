package com.ekuater.labelchat.ui.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserTheme;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ThemeManager;
import com.ekuater.labelchat.ui.fragment.usershowpage.AbsUserAdapter;
import com.ekuater.labelchat.ui.fragment.usershowpage.BaseUserInfo;
import com.ekuater.labelchat.ui.fragment.usershowpage.ContactUserAdapter;
import com.ekuater.labelchat.ui.fragment.usershowpage.OneselfUserAdapter;
import com.ekuater.labelchat.ui.fragment.usershowpage.PageEnum;
import com.ekuater.labelchat.ui.fragment.usershowpage.StrangerUserAdapter;
import com.ekuater.labelchat.ui.fragment.usershowpage.UserAdapterListener;
import com.ekuater.labelchat.ui.util.MiscUtils;

/**
 * Created by Leo on 2015/2/2.
 *
 * @author LinYong
 */
public class UserShowFragment extends Fragment
        implements View.OnClickListener {

    public static final String EXTRA_CONTACT = "extra_contact";
    public static final String EXTRA_STRANGER = "extra_stranger";
    public static final String EXTRA_PERSONAL = "extra_personal";

    private static enum UserType {
        ONESELF,
        CONTACT,
        STRANGER,
    }

    public static UserShowFragment newInstance(UserContact contact) {
        UserShowFragment instance = new UserShowFragment();
        instance.setContact(contact);
        return instance;
    }

    public static UserShowFragment newInstance(Stranger stranger) {
        UserShowFragment instance = new UserShowFragment();
        instance.setStranger(stranger);
        return instance;
    }

    private UserType mUserType;
    private Object mUserInfo;
    private AbsUserAdapter mUserAdapter;
    private AvatarManager mAvatarManager;
    private ThemeManager mThemeManager;
    private UserTheme mUserTheme;

    private View mTopTitleView;
    private View mTabView;
    private boolean mGetTopTitleHeight;
    private int mTopTitleMinTranslation;
    private ListView mContentListView;
    private View mContentListHeaderView;
    private ImageView mAvatarImage;
    private TextView mNicknameText;
    private ImageView mGenderImage;
    private ImageView mLoadingImage;
    private ViewGroup mContentBackground;
    private ViewGroup mContentForeground;
    private ViewGroup mOperationBar;
    private ViewGroup mOperationBarContainer;
    private ImageView mTopImageView;
    private ImageView mBottomImageView;

    private final UserAdapterListener mUserAdapterListener = new UserAdapterListener() {

        @Override
        public void onBaseUserInfoUpdate(BaseUserInfo baseUserInfo) {
            showAccountInfo();
        }

        @Override
        public void onPageLoading() {
            startLoadAnimation();
        }

        @Override
        public void onPageLoadDone() {
            stopLoadAnimation();
        }
    };

    public UserShowFragment() {
        super();
        mUserType = UserType.ONESELF;
        mUserInfo = null;
    }

    public void setContact(UserContact contact) {
        mUserType = UserType.CONTACT;
        mUserInfo = contact;
    }

    public void setStranger(Stranger stranger) {
        mUserType = UserType.STRANGER;
        mUserInfo = stranger;
    }

    private void parseArguments() {
        Bundle args = getArguments();

        if (args == null) {
            return;
        }

        UserContact contact = args.getParcelable(EXTRA_CONTACT);
        Stranger stranger = args.getParcelable(EXTRA_STRANGER);

        if (contact != null) {
            setContact(contact);
        } else if (stranger != null) {
            setStranger(stranger);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseArguments();
        final Activity activity = getActivity();
        mAvatarManager = AvatarManager.getInstance(activity);
        mThemeManager = ThemeManager.getInstance(activity);

        switch (mUserType) {
            case CONTACT:
                mUserAdapter = new ContactUserAdapter(this, mUserAdapterListener,
                        (UserContact) mUserInfo);
                break;
            case STRANGER:
                mUserAdapter = new StrangerUserAdapter(this, mUserAdapterListener,
                        (Stranger) mUserInfo);
                break;
            case ONESELF:
            default:
                mUserAdapter = new OneselfUserAdapter(this, mUserAdapterListener);
                break;
        }

        mUserAdapter.onCreate();
        mUserTheme = mUserAdapter.getUserTheme();

        if (mUserAdapter.wantHideTitle()) {
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_show, container, false);
        RadioGroup pageRadioGroup = (RadioGroup) rootView.findViewById(R.id.sub_page_tab);
        pageRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switchSubPage(group, checkedId);
            }
        });
        ImageView avatarRightImage = (ImageView) rootView.findViewById(R.id.avatar_right_icon);
        avatarRightImage.setOnClickListener(this);
        View backBtn = rootView.findViewById(R.id.btn_back);
        backBtn.setOnClickListener(this);
        mAvatarImage = (ImageView) rootView.findViewById(R.id.avatar_image);
        mNicknameText = (TextView) rootView.findViewById(R.id.nickname);
        mGenderImage = (ImageView) rootView.findViewById(R.id.gender);
        mLoadingImage = (ImageView) rootView.findViewById(R.id.loading);
        mContentBackground = (ViewGroup) rootView.findViewById(R.id.content_background);
        mContentForeground = (ViewGroup) rootView.findViewById(R.id.content_foreground);
        mTopImageView = (ImageView) rootView.findViewById(R.id.theme_top_image);
        mBottomImageView = (ImageView) rootView.findViewById(R.id.theme_bottom_image);
        mOperationBarContainer = (ViewGroup) rootView.findViewById(R.id.operation_bar_container);
        mOperationBar = (ViewGroup) rootView.findViewById(R.id.operation_bar);
        mTopTitleView = rootView.findViewById(R.id.top_title);
        mTabView = pageRadioGroup;
        rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (mGetTopTitleHeight) {
                    getTopTitleHeight();
                    mGetTopTitleHeight = false;
                }
            }
        });
        mGetTopTitleHeight = true;
        mContentListView = (ListView) rootView.findViewById(R.id.content_list);
        mContentListHeaderView = inflater.inflate(R.layout.user_show_list_header,
                mContentListView, false);
        mContentListView.addHeaderView(mContentListHeaderView);
        mContentListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                mTopTitleView.setTranslationY(Math.max(-getScrollY(), mTopTitleMinTranslation));
            }
        });
        setupOperationBar();
        avatarRightImage.setImageResource(mUserAdapter.getAvatarRightIcon());
        avatarRightImage.setVisibility(mUserAdapter.showAvatarRightIcon() ? View.VISIBLE : View.GONE);
        backBtn.setVisibility(mUserAdapter.showBackIcon() ? View.VISIBLE : View.GONE);
        switchSubPage(pageRadioGroup, pageRadioGroup.getCheckedRadioButtonId());
        displayThemeImages();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        showAccountInfo();
        mUserAdapter.onResume();

        UserTheme userTheme = mUserAdapter.getUserTheme();
        String oldThemeName = getThemeName(mUserTheme);
        String newThemeName = getThemeName(userTheme);
        if (newThemeName != null && !newThemeName.equals(oldThemeName)) {
            mUserTheme = userTheme;
            displayThemeImages();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mUserAdapter.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUserAdapter.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mUserAdapter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avatar_image:
                mUserAdapter.onAvatarImageClick(mAvatarImage);
                break;
            case R.id.avatar_right_icon:
                mUserAdapter.onAvatarRightIconClick();
                break;
            case R.id.btn_back:
                mUserAdapter.onBackIconClick();
                break;
            default:
                break;
        }
    }

    private String getThemeName(UserTheme theme) {
        return theme != null ? theme.getThemeName() : null;
    }

    private void displayThemeImages() {
        if (mUserTheme != null) {
            mThemeManager.displayThemeImage(mUserTheme.getTopImg(),
                    mTopImageView, R.drawable.user_show_bg);
            mThemeManager.displayThemeImage(mUserTheme.getBottomImg(),
                    mBottomImageView, R.drawable.user_show_bar_bg);
        } else {
            mTopImageView.setImageResource(R.drawable.user_show_bg);
            mBottomImageView.setImageResource(R.drawable.user_show_bar_bg);
        }
    }

    private int getScrollY() {
        View child = mContentListView.getChildAt(0);

        if (child == null) {
            return 0;
        }

        int firstVisiblePosition = mContentListView.getFirstVisiblePosition();
        int top = child.getTop();
        int headerHeight = 0;
        if (firstVisiblePosition >= 1) {
            headerHeight = mContentListHeaderView.getHeight();
        }

        return -top + firstVisiblePosition * child.getHeight() + headerHeight;
    }

    private void getTopTitleHeight() {
        int topTitleHeight = mTopTitleView.getMeasuredHeight();
        int tabHeight = mTabView.getMeasuredHeight();
        mTopTitleMinTranslation = -(topTitleHeight - tabHeight);
    }

    private void showAccountInfo() {
        BaseUserInfo baseInfo = mUserAdapter.getBaseUserInfo();
        MiscUtils.showAvatarThumb(mAvatarManager, baseInfo.avatarThumb,
                mAvatarImage, R.drawable.contact_single);
        mAvatarImage.setOnClickListener(this);
        mNicknameText.setText(baseInfo.nickname);
        mGenderImage.setImageResource(ConstantCode.getSexImageResource(
                baseInfo.gender));
    }

    private void setupOperationBar() {
        mOperationBar.removeAllViews();
        mUserAdapter.setupOperationBar(getActivity().getLayoutInflater(),
                mOperationBar);
        mOperationBarContainer.setVisibility(mOperationBar.getChildCount() > 0
                ? View.VISIBLE : View.GONE);
    }

    private void switchSubPage(RadioGroup group, int checkedId) {
        for (int i = 0; i < group.getChildCount(); ++i) {
            View child = group.getChildAt(i);

            if (child instanceof RadioButton) {
                RadioButton button = (RadioButton) child;
                int id = button.getId();

                button.setTypeface(null, (id == checkedId)
                        ? Typeface.BOLD : Typeface.NORMAL);
            }
        }

        mUserAdapter.switchPage(toPageEnum(checkedId), mContentListView,
                mContentBackground, mContentForeground);
    }

    private PageEnum toPageEnum(int checkedId) {
        PageEnum pageEnum;

        switch (checkedId) {
            case R.id.radio_personal:
                pageEnum = PageEnum.USER_INFO;
                break;
            case R.id.radio_label:
                pageEnum = PageEnum.LABEL;
                break;
            case R.id.radio_label_story:
                pageEnum = PageEnum.LABEL_STORY;
                break;
            case R.id.radio_throw_photo:
                pageEnum = PageEnum.THROW_PHOTO;
                break;
            default:
                pageEnum = PageEnum.LABEL;
                break;
        }

        return pageEnum;
    }

    private void startLoadAnimation() {
        mLoadingImage.setVisibility(View.VISIBLE);
        Drawable drawable = mLoadingImage.getDrawable();
        if (drawable instanceof AnimationDrawable) {
            AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
            if (!animationDrawable.isRunning()) {
                animationDrawable.start();
            }
        }
    }

    private void stopLoadAnimation() {
        Drawable drawable = mLoadingImage.getDrawable();
        if (drawable instanceof AnimationDrawable) {
            AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
            if (animationDrawable.isRunning()) {
                animationDrawable.stop();
            }
        }
        mLoadingImage.setVisibility(View.GONE);
    }
}
