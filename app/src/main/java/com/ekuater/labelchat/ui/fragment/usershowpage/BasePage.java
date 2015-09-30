package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.ekuater.labelchat.ui.UIEventBusHub;

import de.greenrobot.event.EventBus;

/**
 * Created by Leo on 2015/2/3.
 *
 * @author LinYong
 */
public abstract class BasePage {

    protected Fragment mFragment;
    protected Context mContext;
    private EventBus mUIEventBus;

    public BasePage(Fragment fragment) {
        mFragment = fragment;
        mContext = fragment.getActivity();
        mUIEventBus = UIEventBusHub.getDefaultEventBus();
    }

    public void onCreate() {
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void onDestroy() {
    }

    public abstract ListAdapter getContentAdapter();

    public abstract AdapterView.OnItemClickListener getContentItemClickListener();

    public AdapterView.OnItemLongClickListener getContentItemLongClickListener() {
        return null;
    }

    public boolean isLoading() {
        return false;
    }

    public void onAddToContentBackground(ViewGroup container) {
    }

    public void onAddToContentForeground(ViewGroup container) {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    protected void postEvent(PageEvent event) {
        mUIEventBus.post(event);
    }

    protected FragmentManager getFragmentManager() {
        return mFragment.getFragmentManager();
    }

    protected void startActivityForResult(Intent intent, int requestCode) {
        mFragment.startActivityForResult(intent, requestCode);
    }
}
