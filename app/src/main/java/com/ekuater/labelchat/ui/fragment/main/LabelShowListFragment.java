
package com.ekuater.labelchat.ui.fragment.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.util.MiscUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author LinYong
 */
public class LabelShowListFragment extends Fragment
        implements View.OnClickListener {

    // private static final String TAG =
    // LabelShowListFragment.class.getSimpleName();

    private static final String PROGRESS_DIALOG_TAG = "ProgressDialog";

    private static final int MSG_REFRESH_USER_LABEL = 101;
    private static final int MSG_SWITCH_STATE = 102;
    private static final int MSG_UPDATE_ACCOUNT_DATA = 103;

    private static final int STATE_NORMAL = 0;
    private static final int STATE_DELETE_LABEL = 1;

    private static final int REASON_DELETE_LABEL = 200;
    private static final int REASON_DELETE_LABEL_SUCCESS = 201;
    private static final int REASON_DELETE_LABEL_FAILURE = 202;

    private UserLabelManager mLabelManager;
    private AccountManager mAccountManager;
    private SettingHelper mSettingHelper;
    private AvatarManager mAvatarManager;
    private ContactsManager mContactsManager;
    private LabelAdapter mLabelAdapter;
    private MenuAdapter mMenuAdapter;
    private ListView mListView;
    private View mNoLabelView;
    private SimpleProgressDialog mProgressDialog;
    private int mState = STATE_NORMAL;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_USER_LABEL:
                    handleRefreshUserLabels();
                    break;
                case MSG_SWITCH_STATE:
                    handleSwitchState(msg.arg1, msg.arg2);
                case MSG_UPDATE_ACCOUNT_DATA:
                    handleUpdateAccountData();
                    break;
                default:
                    break;
            }
        }
    };

    private final UserLabelManager.IListener mLabelManagerListener = new UserLabelManager.AbsListener() {
        @Override
        public void onLabelUpdated() {
            refreshUserLabels();
        }

        @Override
        public void onLabelDeleted(int result) {
            refreshUserLabels();
            switchState(STATE_NORMAL, (result == ConstantCode.LABEL_OPERATION_SUCCESS)
                    ? REASON_DELETE_LABEL_SUCCESS : REASON_DELETE_LABEL_FAILURE);
        }
    };
    private final AccountManager.IListener mAccountListener = new AccountManager.AbsListener() {

        @Override
        public void onLogin(int result) {
            sendUpdateAccountData();
        }

        @Override
        public void onLogout(int result) {
            sendUpdateAccountData();
        }

        private void sendUpdateAccountData() {
            Message message = mHandler.obtainMessage(MSG_UPDATE_ACCOUNT_DATA);
            mHandler.sendMessage(message);
        }
    };
    private final ILabelListener mLabelShowListener = new ILabelListener() {
        @Override
        public void onLabelClicked(UserLabel label) {
            if (mContactsManager.isInGuestMode()) {
                UILauncher.launchLoginPromptUI(getFragmentManager());
            } else {
//                UILauncher.launchFragmentLabelStoryUI(getActivity(), label.toBaseLabel(), null);
            }
        }

        @Override
        public boolean onLabelLongClicked(UserLabel label) {
            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        setHasOptionsMenu(true);
        mLabelManager = UserLabelManager.getInstance(activity);
        mLabelManager.registerListener(mLabelManagerListener);
        mAccountManager = AccountManager.getInstance(activity);
        mAccountManager.registerListener(mAccountListener);
        mContactsManager = ContactsManager.getInstance(activity);
        mSettingHelper = SettingHelper.getInstance(activity);
        mAvatarManager = AvatarManager.getInstance(activity);
        mLabelAdapter = new LabelAdapter(activity);
        mLabelAdapter.setListener(mLabelShowListener);
        mMenuAdapter = new MenuAdapter(activity);
        refreshUserLabels();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLabelManager.unregisterListener(mLabelManagerListener);
        mAccountManager.unregisterListener(mAccountListener);
        mLabelAdapter.setListener(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_label_show_list,
                container, false);
        mListView = (ListView) view.findViewById(R.id.label_list);
        mListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mListView.setAdapter(mLabelAdapter);
        mListView.setOnItemClickListener(mLabelAdapter);
        mListView.setOnItemLongClickListener(mLabelAdapter);
        mNoLabelView = view.findViewById(R.id.no_label);
        ListView menuList = (ListView) view.findViewById(R.id.menu_list);
        menuList.setAdapter(mMenuAdapter);
        menuList.setOnItemClickListener(mMenuAdapter);
        registerForContextMenu(mListView);
        updateNoLabelVisible();

        return view;
    }

    private void handleUpdateAccountData() {
        bindUserAreaData(getView());
    }

    private void bindUserAreaData(View view) {
        if (view == null) {
            return;
        }

        ImageView avatarImage = (ImageView) view.findViewById(R.id.avatar_image);

        if (mAccountManager.isLogin()) {
            MiscUtils.showAvatarThumb(mAvatarManager, mSettingHelper.getAccountAvatarThumb(),
                    avatarImage, R.drawable.contact_single);
        } else {
            avatarImage.setImageResource(R.drawable.contact_single);
        }

        avatarImage.setOnClickListener(this);
        mMenuAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindUserAreaData(getView());
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v == mListView) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.label_show_item_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean handled = false;
        ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();

        if (menuInfo instanceof AdapterView.AdapterContextMenuInfo) {
            AdapterView.AdapterContextMenuInfo adapterMenuInfo
                    = (AdapterView.AdapterContextMenuInfo) menuInfo;
            handled = true;

            switch (item.getItemId()) {
                case R.id.delete:
                    deleteUserLabels(mLabelAdapter.getItem(adapterMenuInfo.position));
                    break;
                default:
                    handled = false;
                    break;
            }
        }

        return handled || super.onContextItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        final Activity activity = getActivity();

        switch (v.getId()) {
            case R.id.avatar_image:
                UILauncher.launchShowFriendAvatarImage(activity,
                        SettingHelper.getInstance(activity).getAccountAvatar());
                break;
            default:
                break;
        }
    }

    private void deleteUserLabels(UserLabel[] labels) {
        if (labels != null && labels.length > 0) {
            switchState(STATE_DELETE_LABEL, REASON_DELETE_LABEL);
            mLabelManager.deleteUserLabels(labels);
        }
    }

    private void deleteUserLabels(UserLabel label) {
        deleteUserLabels(new UserLabel[]{
                label
        });
    }

    private void refreshUserLabels() {
        Message message = mHandler.obtainMessage(MSG_REFRESH_USER_LABEL);
        mHandler.sendMessage(message);
    }

    private void handleRefreshUserLabels() {
        final UserLabel[] labels = mLabelManager.getAllLabels();
        List<UserLabel> list = new ArrayList<UserLabel>();

        if (labels != null && labels.length > 0) {
            Collections.addAll(list, labels);
        }
        mLabelAdapter.updateLabels(list);
        updateNoLabelVisible();
    }

    private void updateNoLabelVisible() {
        if (mNoLabelView != null) {
            mNoLabelView.setVisibility(mLabelAdapter.getCount() > 0
                    ? View.GONE : View.VISIBLE);
        }
    }

    private void showProgressDialog() {
        dismissProgressDialog();
        mProgressDialog = SimpleProgressDialog.newInstance();
        mProgressDialog.show(getFragmentManager(), PROGRESS_DIALOG_TAG);
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void switchState(int newState, int switchReason) {
        Message message = mHandler.obtainMessage(MSG_SWITCH_STATE, newState,
                switchReason);
        mHandler.sendMessage(message);
    }

    private void handleSwitchState(int newState, int switchReason) {
        if (mState != newState) {
            final Activity activity = getActivity();

            switch (mState) {
                case STATE_DELETE_LABEL:
                    switch (switchReason) {
                        case REASON_DELETE_LABEL_SUCCESS:
                            Toast.makeText(activity, R.string.delete_label_success,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case REASON_DELETE_LABEL_FAILURE:
                            Toast.makeText(activity, R.string.delete_label_failure,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }

            switch (newState) {
                case STATE_DELETE_LABEL:
                    showProgressDialog();
                    break;
                case STATE_NORMAL:
                default:
                    dismissProgressDialog();
                    break;
            }

            mState = newState;
        }
    }

    private interface ILabelListener {

        public void onLabelClicked(UserLabel label);

        public boolean onLabelLongClicked(UserLabel label);
    }

    private static class LabelAdapter extends BaseAdapter implements
            AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

        private final List<UserLabel> mLabelList;
        private final LayoutInflater mInflater;
        private WeakReference<ILabelListener> mListener;
        private final Comparator<UserLabel> mComparator = new Comparator<UserLabel>() {
            @Override
            public int compare(UserLabel lhs, UserLabel rhs) {
                long diff = rhs.getPraiseCount() - lhs.getPraiseCount();
                diff = (diff != 0) ? diff : (rhs.getTime() - lhs.getTime());
                return (diff > 0) ? 1 : (diff < 0) ? -1 : 0;
            }
        };

        public LabelAdapter(Context context) {
            super();
            mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mLabelList = new ArrayList<UserLabel>();
        }

        public synchronized void updateLabels(List<UserLabel> list) {
            mLabelList.clear();
            mLabelList.addAll(list);
            sortUserLabels(mLabelList);
            notifyDataSetChanged();
        }

        private void sortUserLabels(List<UserLabel> labelList) {
            Collections.sort(labelList, mComparator);
        }

        @Override
        public int getCount() {
            return mLabelList.size();
        }

        @Override
        public UserLabel getItem(int position) {
            return mLabelList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = newView(parent);
            }
            bindView(position, convertView);
            return convertView;
        }

        private View newView(ViewGroup parent) {
            View view = mInflater.inflate(R.layout.label_show_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.labelText = (TextView) view.findViewById(R.id.label);
            holder.praiseText = (TextView) view.findViewById(R.id.praise);
            view.setTag(holder);
            return view;
        }

        private void bindView(int position, View view) {
            UserLabel userLabel = getItem(position);
            ViewHolder holder = (ViewHolder) view.getTag();

            holder.labelText.setText(userLabel.getName());
            holder.praiseText.setText(String.valueOf(userLabel.getPraiseCount()));
        }

        public synchronized void setListener(ILabelListener listener) {
            if (listener == null) {
                mListener = null;
            } else {
                mListener = new WeakReference<ILabelListener>(listener);
            }
        }

        private synchronized ILabelListener getListener() {
            if (mListener != null) {
                return mListener.get();
            } else {
                return null;
            }
        }

        private void onItemClicked(UserLabel userLabel) {
            ILabelListener listener = getListener();
            if (listener != null) {
                listener.onLabelClicked(userLabel);
            }
        }

        private boolean onItemLongClicked(UserLabel userLabel) {
            ILabelListener listener = getListener();
            return (listener != null) && listener.onLabelLongClicked(userLabel);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            onItemClicked(getItem(position));
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            return onItemLongClicked(getItem(position));
        }

        private static class ViewHolder {

            public TextView labelText;
            public TextView praiseText;
        }
    }

    private class MenuAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

        private final Context mContext;
        private final LayoutInflater mInflater;
        private LabelShowMenuItem.Item[] mMenuItems;

        public MenuAdapter(Context context) {
            super();
            mContext = context;
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            updateMenuItem();
        }

        private void updateMenuItem() {
            final List<LabelShowMenuItem.Item> menuList = new ArrayList<LabelShowMenuItem.Item>();

            menuList.add(newAddLabelMenu());
            menuList.add(newPersonalMenu());
            menuList.add(newBindAccountMenu());
            menuList.add(newLabelStoryMenu());
            menuList.add(newThrowPhotoMenu());
            menuList.add(newFeedbackMenu());

            // Remove null menu
            Iterator<LabelShowMenuItem.Item> iterator = menuList.iterator();
            while (iterator.hasNext()) {
                if (iterator.next() == null) {
                    iterator.remove();
                }
            }
            mMenuItems = menuList.toArray(new LabelShowMenuItem.Item[menuList.size()]);
        }

        private LabelShowMenuItem.Item newAddLabelMenu() {
            return new LabelShowMenuItem.AddLabelItem(mContext);
        }

        private LabelShowMenuItem.Item newPersonalMenu() {
            boolean isLogin = mAccountManager.isLogin();
            int personalTitle = isLogin ? R.string.personal_information
                    : R.string.sign_in_register;

            return new LabelShowMenuItem.NormalItem(
                    mContext.getString(personalTitle),
                    R.drawable.ic_setting_personal,
                    new LabelShowMenuItem.NormalItemListener() {
                        @Override
                        public void onClick(LabelShowMenuItem.NormalItem item) {
                            if (mAccountManager.isLogin()) {
                                UILauncher.launchUserInfoSettingUI(mContext);
                            } else {
                                UILauncher.launchSignInGuideUI(mContext);
                            }
                        }
                    });
        }

        private LabelShowMenuItem.Item newBindAccountMenu() {
            boolean isLogin = mAccountManager.isLogin();
            int authType = mSettingHelper.getAccountLoginAuthType();
            String mobile = mSettingHelper.getAccountMobile();
            boolean bindAccountVisible = isLogin
                    && (authType == ConstantCode.AUTH_TYPE_OAUTH)
                    && TextUtils.isEmpty(mobile);

            if (bindAccountVisible) {
                return new LabelShowMenuItem.NormalItem(
                        mContext.getString(R.string.bind_account),
                        R.drawable.ic_setting_bind,
                        new LabelShowMenuItem.NormalItemListener() {
                            @Override
                            public void onClick(LabelShowMenuItem.NormalItem item) {
                                UILauncher.launchOAuthBindAccountUI(mContext);
                            }
                        });
            } else {
                return null;
            }
        }

        private LabelShowMenuItem.Item newThrowPhotoMenu() {
            return new LabelShowMenuItem.NormalItem(
                    mContext.getString(R.string.my_throw_photos),
                    R.drawable.ic_setting_throw_photo,
                    new LabelShowMenuItem.NormalItemListener() {
                        @Override
                        public void onClick(LabelShowMenuItem.NormalItem item) {
                            if (mAccountManager.isLogin()) {
                                UILauncher.launchMyThrowPhotosUI(mContext);
                            } else {
                                UILauncher.launchSignInGuideUI(mContext);
                            }
                        }
                    });
        }

        private LabelShowMenuItem.Item newLabelStoryMenu() {
            return new LabelShowMenuItem.NormalItem(
                    mContext.getString(R.string.my_story),
                    R.drawable.ic_setting_label_story,
                    new LabelShowMenuItem.NormalItemListener() {
                        @Override
                        public void onClick(LabelShowMenuItem.NormalItem item) {
                            if (mAccountManager.isLogin()) {
//                                UILauncher.launchFragmentMyLabelStoryUI(mContext,);
                            } else {
                                UILauncher.launchSignInGuideUI(mContext);
                            }
                        }
                    });
        }

        private LabelShowMenuItem.Item newFeedbackMenu() {
            return new LabelShowMenuItem.NormalItem(
                    mContext.getString(R.string.feedback),
                    R.drawable.ic_setting_feedback,
                    new LabelShowMenuItem.NormalItemListener() {
                        @Override
                        public void onClick(LabelShowMenuItem.NormalItem item) {
                            UILauncher.launchFeedbackUI(mContext);
                        }
                    });
        }

        @Override
        public void notifyDataSetChanged() {
            updateMenuItem();
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mMenuItems.length;
        }

        @Override
        public LabelShowMenuItem.Item getItem(int position) {
            return mMenuItems[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return LabelShowMenuItem.getTypeCount();
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getViewType();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final LabelShowMenuItem.Item item = getItem(position);

            if (convertView == null) {
                convertView = item.newView(mInflater, parent);
            }
            item.bindView(convertView);
            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            getItem(position).onClick();
        }
    }
}
