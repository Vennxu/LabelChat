package com.ekuater.labelchat.ui.fragment.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.MiscManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/2/9.
 *
 * @author LinYong
 */
public class SettingsFragment extends Fragment {

    private SettingAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        mAdapter = new SettingAdapter(activity);
        setupSettingItems();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        ImageView icon = (ImageView) rootView.findViewById(R.id.icon);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title.setText(R.string.action_settings);
        ListView settingList = (ListView) rootView.findViewById(R.id.setting_list);
        settingList.setAdapter(mAdapter);
        settingList.setOnItemClickListener(mAdapter);
        return rootView;
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void setupSettingItems() {
        List<SettingItem.Item> itemList = new ArrayList<>();
        addBindAccountItem(itemList);
        addMessageNotify(itemList);
        addMyBackgroundItem(itemList);
        addMyChatBackground(itemList);
        addExitItem(itemList);
        mAdapter.updateItems(itemList);
    }

    private void addBindAccountItem(List<SettingItem.Item> itemList) {
        Activity activity = getActivity();
        AccountManager accountManager = AccountManager.getInstance(activity);
        SettingHelper settingHelper = SettingHelper.getInstance(activity);
        boolean isLogin = accountManager.isLogin();
        int authType = settingHelper.getAccountLoginAuthType();
        String mobile = settingHelper.getAccountMobile();
        boolean bindAccountVisible = isLogin
                && (authType == ConstantCode.AUTH_TYPE_OAUTH)
                && TextUtils.isEmpty(mobile);

        if (bindAccountVisible) {
            itemList.add(new SettingItem.TextItem(getString(R.string.bind_account),
                    new SettingItem.ItemClickListener() {
                        @Override
                        public void onClick(SettingItem.Item item) {
                            UILauncher.launchOAuthBindAccountUI(getActivity());
                        }
                    }));
        }
    }

    private void addMyBackgroundItem(List<SettingItem.Item> itemList) {
        itemList.add(new SettingItem.TextItem(getString(R.string.my_background),
                new SettingItem.ItemClickListener() {
                    @Override
                    public void onClick(SettingItem.Item item) {
                        UILauncher.launchFragmentInNewActivity(getActivity(),
                                UserBgSelectFragment.class, null);
                    }
                }));
    }

    private void addMyChatBackground(List<SettingItem.Item> itemList) {
        itemList.add(new SettingItem.TextItem(getString(R.string.chat_background),
                new SettingItem.ItemClickListener() {
                    @Override
                    public void onClick(SettingItem.Item item) {
                        UILauncher.launchSelectChatBgUI(getActivity());
                    }
                }));
    }

    private void addMessageNotify(List<SettingItem.Item> itemList) {
        itemList.add(new SettingItem.TextItem(getString(R.string.message_notify),
                new SettingItem.ItemClickListener() {
                    @Override
                    public void onClick(SettingItem.Item item) {
                        UILauncher.launchMessageNotifySettingUI(getActivity());
                    }
                }));
    }

    private void addExitItem(List<SettingItem.Item> itemList) {
        itemList.add(new SettingItem.ExitItem(
                new SettingItem.ItemClickListener() {
                    @Override
                    public void onClick(SettingItem.Item item) {
                        onExitClick();
                    }
                }));
    }

    private void onExitClick() {
        ConfirmDialogFragment.UiConfig uiConfig
                = new ConfirmDialogFragment.UiConfig(
                true, getString(R.string.exit), null,
                getString(R.string.exit_app_confirm), null);
        ConfirmDialogFragment.newInstance(uiConfig,
                new ConfirmDialogFragment.IConfirmListener() {
                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onConfirm() {
                        MiscManager.getInstance(getActivity()).exitApp();
                    }
                }).show(getFragmentManager(), ConfirmDialogFragment.class.getName());
    }

    private class SettingAdapter extends BaseAdapter
            implements AdapterView.OnItemClickListener {

        private final LayoutInflater mInflater;
        private List<SettingItem.Item> mItemList;

        public SettingAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        public void updateItems(List<SettingItem.Item> list) {
            mItemList = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return (mItemList != null) ? mItemList.size() : 0;
        }

        @Override
        public SettingItem.Item getItem(int position) {
            return (mItemList != null) ? mItemList.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position).isEnabled();
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getItemType();
        }

        @Override
        public int getViewTypeCount() {
            return SettingItem.getItemTypeCount();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SettingItem.Item item = getItem(position);
            if (convertView == null) {
                convertView = item.newView(mInflater, parent);
            }
            item.bindView(convertView);
            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            Object object = parent.getAdapter().getItem(position);
            if (object != null && object instanceof SettingItem.Item) {
                ((SettingItem.Item) object).onClick();
            }
        }
    }
}
