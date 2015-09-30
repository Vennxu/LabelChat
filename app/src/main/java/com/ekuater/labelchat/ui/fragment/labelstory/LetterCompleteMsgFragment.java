package com.ekuater.labelchat.ui.fragment.labelstory;

import android.app.ActionBar;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.LetterMessage;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ViewHolder;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.util.TextUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by wenxiang on 2015/3/19.
 */
public class LetterCompleteMsgFragment extends Fragment {

    private ListView listView;
    private PushMessageManager mPushManager;
    private AvatarManager mAvatarManager;
    private CompleterLetterAdapter adapter;
    private Activity activity;
    private ArrayList<LetterMessage> mLetterMessages = new ArrayList<>();
    private final PushMessageManager.AbsListener mPushMessageManagerListener
            = new PushMessageManager.AbsListener() {
        @Override
        public void onNewSystemPushReceived(SystemPush systemPush) {
            super.onNewSystemPushReceived(systemPush);
            if (systemPush.getType() == SystemPushType.TYPE_PRIVATE_LETTER) {
                startQueryMessage();
            }
        }

        @Override
        public void onPushMessageDataChanged() {
            super.onPushMessageDataChanged();
            startQueryMessage();
        }
    };

    public final class LoadLetterMessageTask extends AsyncTask<Void, Void, ArrayList<LetterMessage>> {
        @Override
        protected ArrayList<LetterMessage> doInBackground(Void... params) {

            final SystemPush[] systemPushs = mPushManager.getEveryFlagLastPushMessage();
            final ArrayList<LetterMessage> list = new ArrayList<>();
            if (systemPushs != null) {
                for (SystemPush systemPush : systemPushs) {
                    if (systemPush != null && isLetter(systemPush.getFlag())) {
                        LetterMessage message = LetterMessage.build(systemPush);
                        message.setTime(systemPush.getTime());
                        message.setMessageId(systemPush.getId());
                        message.setState(systemPush.getState());
                        if (message != null) {
                            list.add(message);
                        }
                    }
                }
            }
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<LetterMessage> letterMessage) {
            super.onPostExecute(letterMessage);
            mLetterMessages.clear();
            sortUserLabels(letterMessage);
            mLetterMessages.addAll(letterMessage);
            adapter.notifyDataSetChanged();
        }
    }

    private final Comparator<LetterMessage> mComparator = new Comparator<LetterMessage>() {
        @Override
        public int compare(LetterMessage lhs, LetterMessage rhs) {
            long diff = rhs.getTime() - lhs.getTime();
            diff = (diff != 0) ? diff : (rhs.getTime() - lhs.getTime());
            return (diff > 0) ? 1 : (diff < 0) ? -1 : 0;
        }
    };

    private void sortUserLabels(List<LetterMessage> messageList) {
        Collections.sort(messageList, mComparator);
    }

    private boolean isLetter(String flag) {
        if (flag.equals("1") || flag.equals("2") || flag.equals("3") || flag.equals("comment") || flag.equals("praise") || TextUtil.isEmpty(flag)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mPushManager = PushMessageManager.getInstance(activity);
        mPushManager.registerListener(mPushMessageManagerListener);
        mAvatarManager = AvatarManager.getInstance(activity);
        adapter = new CompleterLetterAdapter();
    }

    private void startQueryMessage() {
        new LoadLetterMessageTask().executeOnExecutor(
                LoadLetterMessageTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LetterMessage letterMessage = (LetterMessage) parent.getAdapter().getItem(position);
            if (letterMessage != null) {
                UILauncher.launchLabelStoryLetterMsgUI(activity, letterMessage.getStranger().getUserId());
                if (letterMessage.getState() == SystemPush.STATE_UNPROCESSED) {
                    PushMessageManager.getInstance(activity).updatePushMessageProcessed(letterMessage.getMessageId());
                    mLetterMessages.get(position).setState(SystemPush.STATE_PROCESSED);
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_complete_letter, container, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(R.string.letter_message);
        view.findViewById(R.id.icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        TextView rightTitle = (TextView) view.findViewById(R.id.right_title);
        rightTitle.setTextColor(getResources().getColor(R.color.white));
        rightTitle.setText(R.string.clean);
        rightTitle.setVisibility(View.VISIBLE);
        rightTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getCount() > 0) {
                    showConfirmDialog();
                }
            }
        });
        listView = (ListView) view.findViewById(R.id.completer_letter_list);
        listView.setOnItemClickListener(onItemClickListener);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
        startQueryMessage();
        return view;
    }


    private void showConfirmDialog() {
        ConfirmDialogFragment.UiConfig uiConfig = new ConfirmDialogFragment.UiConfig(getActivity().getString(R.string.clean_all_private_letter_message), null);
        ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(uiConfig, confirmListener);
        confirmDialogFragment.show(getFragmentManager(), "LabelStoryCommentMessageFragment");
    }

    private ConfirmDialogFragment.AbsConfirmListener confirmListener = new ConfirmDialogFragment.AbsConfirmListener() {
        @Override
        public void onConfirm() {
            mPushManager.deletePushMessageByType(SystemPushType.TYPE_PRIVATE_LETTER);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancel() {
            super.onCancel();
        }

    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.message_list_item_context_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean handler = false;
        ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
        if (menuInfo instanceof AdapterView.AdapterContextMenuInfo) {
            AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
            handler = true;
            switch (item.getItemId()) {
                case R.id.delete:
                    mPushManager.deletePushMessageByFlag(adapter.getItem(adapterContextMenuInfo.position).getStranger().getUserId());
                    break;
                default:
                    handler = false;
                    break;
            }
        }

        return handler || super.onContextItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPushManager.unregisterListener(mPushMessageManagerListener);
        unregisterForContextMenu(listView);
    }

    private class CompleterLetterAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public CompleterLetterAdapter() {
            inflater = LayoutInflater.from(activity);
        }

        @Override
        public int getCount() {
            return mLetterMessages.size();
        }

        @Override
        public LetterMessage getItem(int position) {
            return mLetterMessages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.completer_letter_msg_item, parent, false);
            }

            final LetterMessage message = getItem(position);
            TextView completer_name = (TextView) ViewHolder.get(convertView, R.id.completer_leter_title);
            TextView completer_content = (TextView) ViewHolder.get(convertView, R.id.completer_leter_subtitle);
            CircleImageView completer_tx = (CircleImageView) ViewHolder.get(convertView, R.id.completer_leter_image);
            TextView completer_time = (TextView) ViewHolder.get(convertView, R.id.completer_leter_timestamp);
            TextView completer_title_state = (TextView) ViewHolder.get(convertView, R.id.completer_leter_title_state);
            TextView completer_hint = (TextView) ViewHolder.get(convertView, R.id.completer_leter_hint);
            Stranger stranger = message.getStranger();
            String title = "";
            if (stranger != null) {
                title = MiscUtils.getUserRemarkName(activity, stranger.getUserId());
            }
            completer_name.setText(title != null && title.length() > 0 ? title : stranger != null ? stranger.getNickname() : "");
            completer_content.setText(message.getMessage());
            MiscUtils.showAvatarThumb(mAvatarManager, message.getStranger().getAvatarThumb(), completer_tx, R.drawable.contact_single);
            completer_time.setText(getTimeString(message.getTime()));
            if (message.getTag() == 1) {
                completer_title_state.setTextColor(activity.getResources().getColor(R.color.send_letter_message_state));
                completer_title_state.setText(R.string.send_letter_message_state);
            } else {
                completer_title_state.setTextColor(activity.getResources().getColor(R.color.receive_letter));
                completer_title_state.setText(R.string.receive_letter);
            }
            if (getMessageCount(message.getStranger().getUserId()) == 0) {
                completer_hint.setVisibility(View.GONE);
            } else {
                completer_hint.setVisibility(View.VISIBLE);
                completer_hint.setText(getMessageCount(message.getStranger().getUserId()) + "");
            }
            completer_tx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UILauncher.launchStrangerDetailUI(activity, message.getStranger());
                }
            });
            return convertView;
        }

        public int getMessageCount(String flag) {
            return PushMessageManager.getInstance(activity)
                    .getUnprocessedPushMessageCount(flag);
        }
    }

    private String getTimeString(long time) {
        return DateTimeUtils.getMessageDateString(activity, time);
    }
}
