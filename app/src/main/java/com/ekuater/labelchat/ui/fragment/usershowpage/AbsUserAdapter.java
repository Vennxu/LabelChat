package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.ekuater.labelchat.datastruct.UserTheme;
import com.ekuater.labelchat.ui.UIEventBusHub;
import com.ekuater.labelchat.ui.UILauncher;

import java.util.Map;

/**
 * Created by Leo on 2015/2/6.
 *
 * @author LinYong
 */
public abstract class AbsUserAdapter {

    protected Fragment fragment;
    protected BaseUserInfo baseUserInfo;

    private UserAdapterListener listener;
    private Map<PageEnum, BasePage> pageMap;
    private BasePage currentPage;

    public AbsUserAdapter(Fragment fragment, UserAdapterListener listener) {
        this.fragment = fragment;
        this.listener = listener;
        baseUserInfo = new BaseUserInfo();
        pageMap = new ArrayMap<PageEnum, BasePage>();
        currentPage = null;
    }

    protected abstract BasePage newContentPage(PageEnum page);

    protected void updateBaseUserInfo() {
        notifyBaseUserInfoUpdate();
    }

    protected Activity getActivity() {
        return fragment.getActivity();
    }

    protected FragmentManager getFragmentManager() {
        return fragment.getFragmentManager();
    }

    protected BasePage getPage(PageEnum pageEnum) {
        return pageMap.get(pageEnum);
    }

    public void onCreate() {
        for (PageEnum page : PageEnum.values()) {
            BasePage basePage = newContentPage(page);
            if (basePage != null) {
                pageMap.put(page, basePage);
            }
        }

        UIEventBusHub.getDefaultEventBus().register(this);

        for (BasePage basePage : pageMap.values()) {
            basePage.onCreate();
        }
    }

    public void onResume() {
        for (BasePage basePage : pageMap.values()) {
            basePage.onResume();
        }
    }

    public void onPause() {
        for (BasePage basePage : pageMap.values()) {
            basePage.onPause();
        }
    }

    public void onDestroy() {
        UIEventBusHub.getDefaultEventBus().unregister(this);

        for (BasePage basePage : pageMap.values()) {
            basePage.onDestroy();
        }
    }

    public void switchPage(PageEnum page, ListView contentList, ViewGroup background,
                           ViewGroup foreground) {
        BasePage newPage = getPage(page);
        ListAdapter adapter;
        AdapterView.OnItemClickListener itemClickListener;
        AdapterView.OnItemLongClickListener itemLongClickListener;
        boolean loading;

        background.removeAllViews();
        foreground.removeAllViews();

        if (page != null) {
            adapter = newPage.getContentAdapter();
            loading = newPage.isLoading();
            itemClickListener = newPage.getContentItemClickListener();
            itemLongClickListener = newPage.getContentItemLongClickListener();
            newPage.onAddToContentBackground(background);
            newPage.onAddToContentForeground(foreground);
        } else {
            adapter = null;
            itemClickListener = null;
            itemLongClickListener = null;
            loading = false;
        }

        contentList.setAdapter(adapter);
        contentList.setOnItemClickListener(itemClickListener);
        contentList.setOnItemLongClickListener(itemLongClickListener);
        if (loading) {
            notifyPageLoading();
        } else {
            notifyPageLoadDone();
        }

        currentPage = newPage;
    }

    public BaseUserInfo getBaseUserInfo() {
        return baseUserInfo;
    }

    public boolean wantHideTitle() {
        return true;
    }

    public boolean showAvatarRightIcon() {
        return false;
    }

    public int getAvatarRightIcon() {
        return 0;
    }

    public void onAvatarRightIconClick() {
    }

    public boolean showBackIcon() {
        return true;
    }

    public void onBackIconClick() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    public void onAvatarImageClick(ImageView avatarImage) {
        Activity activity = fragment.getActivity();
        if (activity != null) {
            UILauncher.launchShowFriendAvatarImage(activity,
                    baseUserInfo.avatar);
        }
    }

    public void setupOperationBar(LayoutInflater inflater, ViewGroup container) {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (currentPage != null) {
            currentPage.onActivityResult(requestCode, resultCode, data);
        }
    }

    public abstract UserTheme getUserTheme();

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(PageEvent event) {
        onPageEvent(event);

        if (currentPage == event.page) {
            switch (event.event) {
                case LOAD_DONE:
                    notifyPageLoadDone();
                    break;
                default:
                    break;
            }
        }
    }

    protected void onPageEvent(PageEvent event) {
    }

    private void notifyBaseUserInfoUpdate() {
        if (listener != null) {
            listener.onBaseUserInfoUpdate(baseUserInfo);
        }
    }

    private void notifyPageLoading() {
        if (listener != null) {
            listener.onPageLoading();
        }
    }

    private void notifyPageLoadDone() {
        if (listener != null) {
            listener.onPageLoadDone();
        }
    }
}
