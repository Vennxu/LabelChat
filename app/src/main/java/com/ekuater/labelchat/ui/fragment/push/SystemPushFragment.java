package com.ekuater.labelchat.ui.fragment.push;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.fragment.friends.StrangerHelper;
import com.ekuater.labelchat.util.TextUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2015/4/30.
 *
 * @author Xu WenXinag
 */
public class SystemPushFragment extends Fragment {

    private ListView mListView;
    private Activity mActivity;
    private int showType;
    private String tag;
    private boolean isOldMessage = false;
    private String photoTag;
    private SystemPushAdapter mAdapter;
    private PushMessageManager mPushManager;
    private AvatarManager mAvatarManager;
    private StrangerHelper mStrangerHelper;
    private SimpleProgressHelper mSimpleProgressHelper;

    private final PushMessageManager.AbsListener mPushMessageManagerListener
            = new PushMessageManager.AbsListener() {
        @Override
        public void onPushMessageDataChanged() {
            startLoad();
        }

        @Override
        public void onNewSystemPushReceived(SystemPush systemPush) {
            startLoad();
        }
    };

    PushMessageManager.FliterType filterType = new PushMessageManager.FliterType() {
        @Override
        public boolean accept(int target, SystemPush push) {
            return (showType == SystemPushUtils.getNowType(push));
        }
    };

    public final class LoadPushItemTask extends AsyncTask<Void, Void, ArrayList<SystemPushListItem.PushItem>> {

        @Override
        protected ArrayList<SystemPushListItem.PushItem> doInBackground(Void... params) {
            final ArrayList<SystemPush> systemPushs = mPushManager.getPushMessagesFliterType(
                    SystemPushUtils.getFliterType(showType), showType, filterType);
            final ArrayList<SystemPushListItem.PushItem> list = new ArrayList<>();

            if (systemPushs != null) {
                for (SystemPush systemPush : systemPushs) {
                    if (systemPush != null) {
                        if (TextUtil.isEmpty(systemPush.getFlag())) {
                            isOldMessage = true;
                        }

                        SystemPushListItem.PushItem imageCommentItem = getPushItem(systemPush);
                        if (imageCommentItem != null) {
                            list.add(imageCommentItem);
                        } else {
                            // Not supported SystemPush, delete it.
                            mPushManager.deletePushMessage(systemPush.getId());
                        }
                    }
                }
            }
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<SystemPushListItem.PushItem> items) {
            super.onPostExecute(items);
            sortUserLabels(items);
            mAdapter.updateAdapter(items);
        }
    }

    private final Comparator<SystemPushListItem.PushItem> mComparator = new Comparator<SystemPushListItem.PushItem>() {
        @Override
        public int compare(SystemPushListItem.PushItem lhs, SystemPushListItem.PushItem rhs) {
            long diff = rhs.getTime() - lhs.getTime();
            diff = (diff != 0) ? diff : (rhs.getTime() - lhs.getTime());
            return (diff > 0) ? 1 : (diff < 0) ? -1 : 0;
        }
    };

    private void sortUserLabels(List<SystemPushListItem.PushItem> items) {
        Collections.sort(items, mComparator);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        parseArgument();
        mAdapter = new SystemPushAdapter(mActivity);
        mPushManager = PushMessageManager.getInstance(mActivity);
        mAvatarManager = AvatarManager.getInstance(mActivity);
        mStrangerHelper = new StrangerHelper(this);
        mSimpleProgressHelper = new SimpleProgressHelper(this);
        mPushManager.registerListener(mPushMessageManagerListener);
    }

    private void parseArgument() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            showType = bundle.getInt(SystemPushUtils.SYSTEM_PUSH_TYPE);
        }
        ActionBar actionBar = mActivity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.icon:
                    mActivity.finish();
                    break;
                case R.id.right_title:
                    if (mAdapter.getCount() > 0) {
                        showConfirmDialog();
                    }
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_system_push_list, container, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView rightTitle = (TextView) view.findViewById(R.id.right_title);
        rightTitle.setVisibility(View.VISIBLE);
        rightTitle.setText(getString(R.string.clean));
        rightTitle.setTextColor(Color.WHITE);
        icon.setOnClickListener(onClickListener);
        rightTitle.setOnClickListener(onClickListener);
        title.setText(getTitle());
        mListView = (ListView) view.findViewById(R.id.system_push_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Object object = parent.getItemAtPosition(position);
                if (object instanceof SystemPushListItem.PushItem) {
                    final SystemPushListItem.PushItem item = (SystemPushListItem.PushItem) object;
                    item.onClick();
                }
            }
        });
        registerForContextMenu(mListView);
        startLoad();
        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v == mListView) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.message_list_item_context_menu, menu);
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
                    deleteListItem(mAdapter.getItem(adapterMenuInfo.position));
                    break;
                default:
                    handled = false;
                    break;
            }
        }

        return handled || super.onContextItemSelected(item);
    }

    private void deleteListItem(SystemPushListItem.PushItem item) {
        item.delete();
    }

    private SystemPushListItem.PushItem getPushItem(SystemPush systemPush) {
        SystemPushListItem.PushItem pushItem;

        switch (showType) {
            case SystemPushUtils.SYSTEM_PUSH_COMMENT:
                pushItem = CommentPushItem.build(mActivity, mSimpleProgressHelper,
                        mStrangerHelper, mAvatarManager, systemPush);
                break;
            case SystemPushUtils.SYSTEM_PUSH_PRAISE:
                pushItem = PraisePushItem.build(mActivity, mSimpleProgressHelper,
                        mStrangerHelper, mAvatarManager, systemPush);
                break;
            case SystemPushUtils.SYSTEM_PUSH_REMIND:
                pushItem = RemaindPushItem.build(mActivity, mSimpleProgressHelper,
                        mStrangerHelper, mAvatarManager, systemPush);
                break;
            default:
                pushItem = null;
                break;
        }
        return pushItem;
    }

    private String getTitle() {
        String title;
        switch (showType) {
            case SystemPushUtils.SYSTEM_PUSH_COMMENT:
                title = getString(R.string.labelstory_item_comment);
                tag = "comment";
                break;
            case SystemPushUtils.SYSTEM_PUSH_PRAISE:
                title = getString(R.string.labelstory_item_click_priase);
                tag = "praise";
                photoTag = "3";
                break;
            case SystemPushUtils.SYSTEM_PUSH_REMIND:
                title = getString(R.string.photo_notify_title);
                photoTag = "1";
                break;
            default:
                title = null;
                break;
        }
        return title;
    }

    private String getTitlePrompt() {
        String title;
        switch (showType) {
            case SystemPushUtils.SYSTEM_PUSH_COMMENT:
                title = getString(R.string.clean_all_comment_message);
                break;
            case SystemPushUtils.SYSTEM_PUSH_PRAISE:
                title = getString(R.string.clean_all_praise_message);
                break;
            case SystemPushUtils.SYSTEM_PUSH_REMIND:
                title = getString(R.string.clean_all_remind_message);
                break;
            default:
                title = null;
                break;
        }
        return title;
    }

    private void startLoad() {
        new LoadPushItemTask().executeOnExecutor(LoadPushItemTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    private void showConfirmDialog() {
        ConfirmDialogFragment.UiConfig uiConfig = new ConfirmDialogFragment.UiConfig(getTitlePrompt(), null);
        ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(uiConfig, confirmListener);
        confirmDialogFragment.show(getFragmentManager(), "LabelStoryCommentMessageFragment");
    }

    private ConfirmDialogFragment.AbsConfirmListener confirmListener
            = new ConfirmDialogFragment.AbsConfirmListener() {

        @Override
        public void onConfirm() {
            int[] types = SystemPushUtils.getFliterType(showType);

            if (isOldMessage) {
                mPushManager.deletePushMessageByTypes(types);
            } else {
                for (int type : types) {
                    switch (type) {
                        case SystemPushType.TYPE_LABEL_STORY_COMMENTS:
                            mPushManager.deletePushMessageByFlag(tag);
                            break;
                        case SystemPushType.TYPE_PHOTO_NOTIFY:
                            if ("3".equals(photoTag)) {
                                mPushManager.deletePushMessageByFlag(photoTag);
                            } else {
                                mPushManager.deletePushMessageByFlag("1");
                                mPushManager.deletePushMessageByFlag("2");
                            }
                            break;
                        case SystemPushType.TYPE_CONFIDE_COMMEND:
                            mPushManager.deletePushMessageByFlag(tag);
                            break;
                        default:
                            mPushManager.deletePushMessageByType(type);
                            break;
                    }
                }
            }
            mAdapter.notifyDataSetChanged();
            startLoad();
        }

        @Override
        public void onCancel() {
            super.onCancel();
        }

    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPushManager.unregisterListener(mPushMessageManagerListener);
        unregisterForContextMenu(mListView);
    }
}
