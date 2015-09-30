package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.UserTheme;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;

/**
 * Created by Leo on 2015/2/6.
 *
 * @author LinYong
 */
public class OneselfUserAdapter extends AbsUserAdapter {

    private static final int REQUEST_CODE_UPLOAD_AVATAR = 10000;

    public OneselfUserAdapter(Fragment fragment, UserAdapterListener listener) {
        super(fragment, listener);
        Activity activity = fragment.getActivity();
        SettingHelper settingHelper = SettingHelper.getInstance(activity);
        baseUserInfo = BaseUserInfo.fromSettingHelper(settingHelper);
    }

    @Override
    protected BasePage newContentPage(PageEnum page) {
        BasePage newPage;

        switch (page) {
            case USER_INFO:
                newPage = new MyInfoPage(fragment);
                break;
            case LABEL:
                newPage = new MyLabelPage(fragment);
                break;
            case LABEL_STORY:
                newPage = new MyLabelStoryPage(fragment);
                break;
            case THROW_PHOTO:
                newPage = new MyThrowPhotosPage(fragment);
                break;
            default:
                newPage = null;
                break;
        }

        return newPage;
    }

    @Override
    public boolean showAvatarRightIcon() {
        return true;
    }

    @Override
    public int getAvatarRightIcon() {
        return R.drawable.add_label;
    }

    @Override
    public void onAvatarRightIconClick() {
        Activity activity = fragment.getActivity();
        if (activity != null) {
            UILauncher.launchAddUserLabelUI(activity, null);
        }
    }

    @Override
    public boolean showBackIcon() {
        return false;
    }

    @Override
    public void onAvatarImageClick(ImageView avatarImage) {
        avatarImage.setOnCreateContextMenuListener(avatarCtxMenuCreateListener);
        avatarImage.showContextMenu();
    }

    @Override
    public void setupOperationBar(LayoutInflater inflater, ViewGroup container) {
        container.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_UPLOAD_AVATAR:
                if (resultCode == Activity.RESULT_OK) {
                    SettingHelper settingHelper = SettingHelper.getInstance(getActivity());
                    baseUserInfo = BaseUserInfo.fromSettingHelper(settingHelper);
                    updateBaseUserInfo();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public UserTheme getUserTheme() {
        SettingHelper helper = SettingHelper.getInstance(getActivity());
        String themeName = helper.getUserTheme();
        return TextUtils.isEmpty(themeName) ? null : UserTheme.fromThemeName(themeName);
    }

    private final View.OnCreateContextMenuListener avatarCtxMenuCreateListener
            = new View.OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            Activity activity = fragment.getActivity();
            if (activity != null) {
                MenuInflater menuInflater = activity.getMenuInflater();
                menuInflater.inflate(R.menu.avatar_ctx_menu, menu);

                MenuItem showMenu = menu.findItem(R.id.menu_show_avatar);
                if (showMenu != null) {
                    showMenu.setOnMenuItemClickListener(avatarCtxMenuClickListener);
                }
                MenuItem uploadMenu = menu.findItem(R.id.menu_upload_avatar);
                if (uploadMenu != null) {
                    uploadMenu.setOnMenuItemClickListener(avatarCtxMenuClickListener);
                }
            }
            v.setOnCreateContextMenuListener(null);
        }
    };

    private final MenuItem.OnMenuItemClickListener avatarCtxMenuClickListener
            = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            boolean handled = true;
            Activity activity = fragment.getActivity();

            switch (item.getItemId()) {
                case R.id.menu_show_avatar:
                    UILauncher.launchShowFriendAvatarImage(activity,
                            baseUserInfo.avatar);
                    break;
                case R.id.menu_upload_avatar:
                    UILauncher.launchUploadAvatarUIAndSave(fragment,
                            REQUEST_CODE_UPLOAD_AVATAR);
                    break;
                default:
                    handled = false;
                    break;
            }

            return handled;
        }
    };
}
