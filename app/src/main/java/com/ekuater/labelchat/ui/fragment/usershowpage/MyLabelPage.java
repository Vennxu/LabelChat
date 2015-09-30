package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2015/2/3.
 *
 * @author FanChong
 */
public class MyLabelPage extends BasePage {

    private static final int MSG_REFRESH_USER_LABEL = 101;
    private static final int MSG_DELETE_USER_LABEL = 102;

    private UserLabelManager mLabelManager;
    private LabelAdapter mLabelAdapter;
    private SimpleProgressHelper mProgressHelper;
    private UserLabel[] mUserLabels;
    private View mBackgroundView;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_USER_LABEL:
                    handlerRefreshUserLabel();
                    break;
                case MSG_DELETE_USER_LABEL:
                    handlerDeleteUserLabel(msg.arg1);
                default:
                    break;
            }
        }
    };

    private UserLabel mCurrentOperateLabel;
    private final AdapterView.OnItemLongClickListener mItemLongClickListener
            = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Object item = parent.getAdapter().getItem(position);
            if (item != null && item instanceof UserLabel) {
                if (mBackgroundView != null && mItemCtxMenuCreateListener != null) {
                    mCurrentOperateLabel = (UserLabel) item;
                    mBackgroundView.showContextMenu();
                    return true;
                }
            }
            return true;
        }
    };

    private class ItemCtxMenuCreateListener implements View.OnCreateContextMenuListener {

        private MenuItem.OnMenuItemClickListener mMenuClickListener;

        public ItemCtxMenuCreateListener() {
            mMenuClickListener = new ItemCtxMenuClickListener();
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            MenuInflater menuInflater = mFragment.getActivity().getMenuInflater();
            menuInflater.inflate(R.menu.delete_menu, menu);

            for (int i = 0; i < menu.size(); ++i) {
                MenuItem menuItem = menu.getItem(i);
                menuItem.setOnMenuItemClickListener(mMenuClickListener);
            }
        }
    }

    private class ItemCtxMenuClickListener implements MenuItem.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            boolean handled = true;
            switch (item.getItemId()) {
                case R.id.delete:
                    if (mCurrentOperateLabel != null) {
                        mLabelManager.deleteUserLabels(new UserLabel[]{mCurrentOperateLabel});
                        mProgressHelper.show();
                    }
                    break;
                default:
                    handled = false;
                    break;
            }
            mCurrentOperateLabel = null;
            return handled;
        }
    }

    private ItemCtxMenuCreateListener mItemCtxMenuCreateListener;

    public MyLabelPage(Fragment fragment) {
        super(fragment);
        mLabelManager = UserLabelManager.getInstance(mContext);
        mLabelAdapter = new LabelAdapter(mContext);
        mProgressHelper = new SimpleProgressHelper(fragment);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLabelManager.registerListener(mLabelManagerListener);
        refreshUserLabel();
        mLabelManager.forceRefreshLabels();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLabelManager.unregisterListener(mLabelManagerListener);
    }

    private UserLabelManager.IListener mLabelManagerListener
            = new UserLabelManager.AbsListener() {
        @Override
        public void onLabelUpdated() {
            refreshUserLabel();
        }

        @Override
        public void onLabelDeleted(int result) {
            refreshUserLabel();
            deleteUserLabel(result);
        }
    };

    @Override
    public ListAdapter getContentAdapter() {
        return mLabelAdapter;
    }

    @Override
    public AdapterView.OnItemClickListener getContentItemClickListener() {
        return mLabelAdapter;
    }

    @Override
    public AdapterView.OnItemLongClickListener getContentItemLongClickListener() {
        return mItemLongClickListener;
    }

    @Override
    public void onAddToContentBackground(ViewGroup container) {
        if (mBackgroundView == null) {
            mBackgroundView = LayoutInflater.from(mContext).inflate(
                    R.layout.no_label_layout, container, false);
            mItemCtxMenuCreateListener = new ItemCtxMenuCreateListener();
            mBackgroundView.setOnCreateContextMenuListener(new ItemCtxMenuCreateListener());
        }
        ViewGroup parent = (ViewGroup) mBackgroundView.getParent();
        if (parent != null) {
            parent.removeView(mBackgroundView);
        }
        container.addView(mBackgroundView);
        refreshUserLabel();
        mBackgroundView.setVisibility(mUserLabels != null
                ? View.GONE : View.VISIBLE);
    }

    private void refreshUserLabel() {
        Message message = mHandler.obtainMessage(MSG_REFRESH_USER_LABEL);
        mHandler.sendMessage(message);
    }

    private void handlerRefreshUserLabel() {
        List<UserLabel> userLabelList = new ArrayList<UserLabel>();
        mUserLabels = mLabelManager.getAllLabels();
        if (mUserLabels != null && mUserLabels.length > 0) {
            Collections.addAll(userLabelList, mUserLabels);
        }
        mLabelAdapter.updateLabel(userLabelList);
        if (mBackgroundView != null) {
            mBackgroundView.setVisibility(mUserLabels != null
                    ? View.GONE : View.VISIBLE);
        }
    }

    private void deleteUserLabel(int result) {
        Message message = mHandler.obtainMessage(MSG_DELETE_USER_LABEL, result, 0);
        mHandler.sendMessage(message);
    }

    private void handlerDeleteUserLabel(int result) {
        mProgressHelper.dismiss();
        switch (result) {
            case ConstantCode.LABEL_OPERATION_SUCCESS:
                Toast.makeText(mContext, R.string.delete_label_success,
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(mContext, R.string.delete_label_failure,
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
