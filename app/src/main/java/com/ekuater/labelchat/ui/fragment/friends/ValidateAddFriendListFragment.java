package com.ekuater.labelchat.ui.fragment.friends;

import android.app.ActionBar;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.ekuater.labelchat.datastruct.ValidateAddFriendMessage;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LinYong
 */
public class ValidateAddFriendListFragment extends Fragment {

    private ValidMessageAdapter mMessageAdapter;
    private PushMessageManager mPushManager;
    private ListView mListView;

    private class LoadMessagesTask extends AsyncTask<Void, Void, List<ValidMessageAdapter.MessageItem>> {

        @Override
        protected List<ValidMessageAdapter.MessageItem> doInBackground(Void... params) {
            final SystemPush[] pushMessages = mPushManager.getPushMessagesByType(
                    SystemPushType.TYPE_VALIDATE_ADD_FRIEND);
            final List<ValidMessageAdapter.MessageItem> messageItems = new ArrayList<>();

            if (pushMessages != null) {
                for (SystemPush pushMessage : pushMessages) {
                    if (pushMessage.getState() == SystemPush.STATE_UNPROCESSED) {
                        mPushManager.updatePushMessageProcessed(pushMessage.getId());
                        pushMessage.setState(SystemPush.STATE_PROCESSED);
                    }

                    try {
                        ValidateAddFriendMessage validateMessage
                                = ValidateAddFriendMessage.build(
                                new JSONObject(pushMessage.getContent()));
                        if (validateMessage != null) {
                            messageItems.add(new ValidMessageAdapter.MessageItem(pushMessage.getId(),
                                    pushMessage.getState(), validateMessage));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return messageItems;
        }

        @Override
        protected void onPostExecute(List<ValidMessageAdapter.MessageItem> messageItems) {
            mMessageAdapter.updateMessageItems(messageItems);
        }
    }

    private final PushMessageManager.AbsListener mPushMessageManagerListener
            = new PushMessageManager.AbsListener() {
        @Override
        public void onPushMessageDataChanged() {
            startQueryMessages();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        mMessageAdapter = new ValidMessageAdapter(activity, new SimpleProgressHelper(this),
                new StrangerHelper(this));
        mPushManager = PushMessageManager.getInstance(activity);
        mPushManager.registerListener(mPushMessageManagerListener);
        startQueryMessages();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPushManager.unregisterListener(mPushMessageManagerListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_validate_add_friend_list,
                container, false);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView title = (TextView) view.findViewById(R.id.title);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title.setText(R.string.validate_friend);
        TextView rightTitle = (TextView) view.findViewById(R.id.right_title);
        rightTitle.setTextColor(getResources().getColor(R.color.white));
        rightTitle.setText(R.string.clean);
        rightTitle.setVisibility(View.VISIBLE);
        rightTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMessageAdapter.getCount() > 0) {
                    showConfirmDialog();
                }
            }
        });
        mListView = (ListView) view.findViewById(R.id.message_list);
        mListView.setAdapter(mMessageAdapter);
        registerForContextMenu(mListView);
        return view;
    }

    private void showConfirmDialog() {
        ConfirmDialogFragment.UiConfig uiConfig = new ConfirmDialogFragment.UiConfig(getActivity().getString(R.string.clean_all_validate_message), null);
        ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(uiConfig, confirmListener);
        confirmDialogFragment.show(getFragmentManager(), "LabelStoryCommentMessageFragment");
    }

    private ConfirmDialogFragment.AbsConfirmListener confirmListener = new ConfirmDialogFragment.AbsConfirmListener() {
        @Override
        public void onConfirm() {
            mPushManager.deletePushMessageByType(SystemPushType.TYPE_VALIDATE_ADD_FRIEND);
            mMessageAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancel() {
            super.onCancel();
        }

    };


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterForContextMenu(mListView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final MenuInflater inflater = getActivity().getMenuInflater();

        switch (v.getId()) {
            case R.id.message_list:
                inflater.inflate(R.menu.delete_menu, menu);
                break;
            default:
                break;
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
                    mPushManager.deletePushMessage(mMessageAdapter.getItem(
                            adapterMenuInfo.position).getMessageId());
                    break;
                default:
                    handled = false;
                    break;
            }
        }

        return handled || super.onContextItemSelected(item);
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void startQueryMessages() {
        new LoadMessagesTask().executeOnExecutor(
                LoadMessagesTask.THREAD_POOL_EXECUTOR, (Void) null);
    }
}
