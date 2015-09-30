package com.ekuater.labelchat.ui.fragment.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.UserTheme;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ThemeManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.personalinfo.GapViewItem;
import com.ekuater.labelchat.ui.fragment.personalinfo.NormalViewItem;
import com.ekuater.labelchat.ui.fragment.personalinfo.PersonalInfoAdapter;
import com.ekuater.labelchat.ui.fragment.personalinfo.SubItem;
import com.ekuater.labelchat.ui.fragment.personalinfo.ViewItem;
import com.ekuater.labelchat.ui.fragment.personalinfo.ViewItemContainer;
import com.ekuater.labelchat.ui.fragment.settings.UserBgSelectFragment;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/3/12.
 *
 * @author LinYong
 */
public class PersonalInfoFragment extends Fragment implements View.OnClickListener,
        View.OnLongClickListener {

    private static final int REQUEST_CODE_UPLOAD_AVATAR = 10000;

    private AccountManager mAccountManager;
    private SettingHelper mSettingHelper;
    private AvatarManager mAvatarManager;
    private ThemeManager mThemeManager;
    private UserTheme mUserTheme;
    private String mNickname;
    private PersonalInfoAdapter mInfoAdapter;
    private View mHeaderView;
    private ImageView mTopImageView;
    private ImageView mAvatarImage;
    private ListView mListView;
    private View mListHeaderView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        mAccountManager = AccountManager.getInstance(activity);
        mSettingHelper = SettingHelper.getInstance(activity);
        mAvatarManager = AvatarManager.getInstance(activity);
        mThemeManager = ThemeManager.getInstance(activity);
        mUserTheme = getUserTheme();
        mNickname = mSettingHelper.getAccountNickname();
        mInfoAdapter = new PersonalInfoAdapter(activity, setupViewItems(activity));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_personal_info, container, false);
        mHeaderView = rootView.findViewById(R.id.header);
        mTopImageView = (ImageView) rootView.findViewById(R.id.theme_top_image);
        mTopImageView.setOnLongClickListener(this);
        displayThemeImages();
        mAvatarImage = (ImageView) rootView.findViewById(R.id.avatar_image);
        MiscUtils.showAvatarThumb(mAvatarManager, mSettingHelper.getAccountAvatarThumb(),
                mAvatarImage, R.drawable.contact_single);
        mAvatarImage.setOnClickListener(this);
        mListView = (ListView) rootView.findViewById(R.id.list);
        View listHeader = inflater.inflate(R.layout.personal_info_list_header, mListView, false);
        mListHeaderView = listHeader;
        mListView.addHeaderView(listHeader);
        mListView.setAdapter(mInfoAdapter);
        mListView.setOnItemClickListener(mInfoAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                ViewHelper.setTranslationY(mHeaderView, Math.max(-getScrollY(),
                        -mHeaderView.getMeasuredHeight()));
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        UserTheme userTheme = getUserTheme();
        String oldThemeName = getThemeName(mUserTheme);
        String newThemeName = getThemeName(userTheme);
        if (newThemeName != null && !newThemeName.equals(oldThemeName)) {
            mUserTheme = userTheme;
            displayThemeImages();
        }

        String nickname = mSettingHelper.getAccountNickname();
        if (!TextUtils.isEmpty(nickname) && !nickname.equals(mNickname)) {
            mNickname = nickname;
            mInfoAdapter.updateItems(setupViewItems(getActivity()));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_UPLOAD_AVATAR:
                if (resultCode == Activity.RESULT_OK) {
                    MiscUtils.showAvatarThumb(mAvatarManager,
                            mSettingHelper.getAccountAvatarThumb(),
                            mAvatarImage, R.drawable.contact_single);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        switch (v.getId()) {
            case R.id.avatar_image:
                onCreateAvatarImageContextMenu(menu);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean handled = true;
        Activity activity = getActivity();

        switch (item.getItemId()) {
            case R.id.menu_show_avatar:
                UILauncher.launchShowFriendAvatarImage(activity,
                        mSettingHelper.getAccountAvatar());
                break;
            case R.id.menu_upload_avatar:
                UILauncher.launchUploadAvatarUIAndSave(this, REQUEST_CODE_UPLOAD_AVATAR);
                break;
            case R.id.preview_all_info:
                UILauncher.launchMyInfoUI(activity);
                break;
            default:
                handled = false;
                break;
        }

        return handled || super.onContextItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avatar_image:
                onAvatarImageClick();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        boolean handled = true;

        switch (v.getId()) {
            case R.id.theme_top_image:
                v.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        MenuInflater inflater = getActivity().getMenuInflater();
                        inflater.inflate(R.menu.background_ctx_menu, menu);
                        menu.findItem(R.id.menu_change_background).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                UILauncher.launchFragmentInNewActivity(getActivity(),
                                        UserBgSelectFragment.class, null);
                                return true;
                            }
                        });
                    }
                });
                v.showContextMenu();
                v.setOnCreateContextMenuListener(null);
                break;
            default:
                handled = false;
                break;
        }

        return handled;
    }

    private int getScrollY() {
        View child = mListView.getChildAt(0);

        if (child == null) {
            return 0;
        }

        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        int top = child.getTop();
        int headerHeight = 0;
        if (firstVisiblePosition >= 1) {
            headerHeight = mListHeaderView.getHeight();
        }

        return -top + firstVisiblePosition * child.getHeight() + headerHeight;
    }

    private UserTheme getUserTheme() {
        String themeName = mSettingHelper.getUserTheme();
        return TextUtils.isEmpty(themeName) ? null : UserTheme.fromThemeName(themeName);
    }

    private String getThemeName(UserTheme theme) {
        return theme != null ? theme.getThemeName() : null;
    }

    private void displayThemeImages() {
        if (mUserTheme != null) {
            mThemeManager.displayThemeImage(mUserTheme.getTopImg(),
                    mTopImageView, R.drawable.user_show_bg);
        } else {
            mTopImageView.setImageResource(R.drawable.user_show_bg);
        }
    }

    private List<ViewItem> setupViewItems(Context context) {
        List<ViewItem> itemList = new ArrayList<>();
        ViewItemContainer container;

        container = new ViewItemContainer();
        container.addSubItem(newSettingItem());

        itemList.add(container);
        itemList.add(newGapItem());

        itemList.add(newDynamicItem(context));
        itemList.add(newGapItem());

        container = new ViewItemContainer();
        container.addSubItem(newAlbumItem(context));
        container.addSubItem(newTagItem(context));
        container.addSubItem(newInterestItem(context));
        itemList.add(container);
        itemList.add(newGapItem());

        container = new ViewItemContainer();
        container.addSubItem(newSetItem(context));
        container.addSubItem(newFeedbackItem(context));
        itemList.add(container);
        itemList.add(newGapItem());
        return itemList;
    }

    private SubItem newSettingItem() {
        return new SubItem(R.drawable.ic_setting_personal, mNickname,
                new SubItem.Listener() {
                    @Override
                    public void onClick() {
                        if (!isLogin()) {
                            launchLoginPromptUI();
                            return;
                        }
                        UILauncher.launchUserInfoSettingUI(getActivity());
                    }
                });
    }

    private SubItem newAlbumItem(Context context) {
        return new SubItem(R.drawable.ic_setting_throw_photo,
                context.getString(R.string.album),
                new SubItem.Listener() {
                    @Override
                    public void onClick() {
                        UILauncher.launchMyAlbumUI(getActivity());
                    }
                });
    }

    private ViewItem newDynamicItem(Context context) {
        return new NormalViewItem(R.drawable.ic_setting_label_story,
                context.getString(R.string.story),
                new NormalViewItem.Listener() {
                    @Override
                    public void onClick() {
                        UILauncher.launchMyOwnMixDynamicUI(getActivity());
                    }
                });
    }

    private SubItem newInterestItem(Context context) {
        return new SubItem(R.drawable.ic_setting_interest,
                context.getString(R.string.interest),
                new SubItem.Listener() {
                    @Override
                    public void onClick() {
                        UILauncher.launchInterestUI(getActivity(),
                                mSettingHelper.getAccountUserId());
                    }
                });
    }

    private SubItem newTagItem(Context context) {
        return new SubItem(R.drawable.ic_setting_label,
                context.getString(R.string.label),
                new SubItem.Listener() {
                    @Override
                    public void onClick() {
                        UILauncher.launchShowSelectUserTagUI(getActivity());
                    }
                });
    }

    private SubItem newSetItem(Context context) {
        return new SubItem(R.drawable.ic_setting_setting,
                context.getString(R.string.register_setting),
                new SubItem.Listener() {
                    @Override
                    public void onClick() {
                        UILauncher.launchSettingsUI(getActivity());
                    }
                });
    }

    private SubItem newFeedbackItem(Context context) {
        return new SubItem(R.drawable.ic_setting_feedback,
                context.getString(R.string.feedback),
                new SubItem.Listener() {
                    @Override
                    public void onClick() {
                        UILauncher.launchFeedbackUI(getActivity());
                    }
                });
    }

    private ViewItem newGapItem() {
        return new GapViewItem();
    }

    private void onAvatarImageClick() {
        if (!isLogin()) {
            launchLoginPromptUI();
            return;
        }
        registerForContextMenu(mAvatarImage);
        mAvatarImage.showContextMenu();
        unregisterForContextMenu(mAvatarImage);
    }

    private void onCreateAvatarImageContextMenu(ContextMenu menu) {
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.avatar_ctx_menu, menu);
    }

    private boolean isLogin() {
        return mAccountManager.isLogin();
    }

    private void launchLoginPromptUI() {
        UILauncher.launchLoginPromptUI(getFragmentManager());
    }
}
