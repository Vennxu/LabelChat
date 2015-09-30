package com.ekuater.labelchat.ui.fragment.friends;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.BubbleUpMessage;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.util.MiscUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 冒泡结果页面
 * <p/>
 * project：LabelChat
 *
 * @author Jeffy Chen
 *         create time 2014-9-14 下午12:05:54
 * @version v1.0
 */
public class BubblingResultFragment extends Fragment {

    public static final String EXTRA_MESSAGE_ID = "message_id";

    private StrangerAdapter mStrangerAdapter;

    private final AdapterView.OnItemClickListener mItemClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object object = parent.getItemAtPosition(position);
            if (object instanceof Stranger) {
                Stranger stranger = (Stranger) object;
                UILauncher.launchStrangerDetailUI(view.getContext(), stranger);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.bubble_up);
        }

        mStrangerAdapter = new StrangerAdapter(activity, new Handler());
        loadMessage();
    }

    private void loadMessage() {
        Bundle args = getArguments();
        long messageId = -1;
        PushMessageManager pushMessageManager = PushMessageManager.getInstance(getActivity());

        if (args != null) {
            messageId = args.getLong(EXTRA_MESSAGE_ID, messageId);
        }

        SystemPush push = pushMessageManager.getPushMessage(messageId);
        if (push != null) {
            BubbleUpMessage bubbleUpMessage = BubbleUpMessage.build(push);
            if (bubbleUpMessage != null) {
                List<Stranger> list = new ArrayList<Stranger>();
                for (Stranger stranger : bubbleUpMessage.getBubbleUpStrangers()) {
                    if (stranger != null) {
                        list.add(stranger);
                    }
                }
                mStrangerAdapter.updateStrangerList(list);
            }
        }
        pushMessageManager.updatePushMessageProcessed(messageId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_bubbling_result,
                container, false);
        GridView gridView = (GridView) view.findViewById(R.id.bubbling_users);
        gridView.setAdapter(mStrangerAdapter);
        gridView.setOnItemClickListener(mItemClickListener);
        TextView hintText = (TextView) view.findViewById(R.id.bubbling_hint);
        hintText.setText(getString(R.string.bubbling_result_hint, mStrangerAdapter.getCount()));

        return view;
    }

    private static class StrangerAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;
        private final Handler mUiHandler;
        private final AvatarManager mAvatarManager;
        private List<Stranger> mStrangerList = new ArrayList<Stranger>();

        public StrangerAdapter(Context context, Handler uiHandler) {
            super();
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mUiHandler = uiHandler;
            mAvatarManager = AvatarManager.getInstance(context);
        }

        public synchronized void updateStrangerList(List<Stranger> list) {
            mStrangerList = list;
            notifyDataSetChangedInUI();
        }

        private void notifyDataSetChangedInUI() {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getCount() {
            return mStrangerList.size();
        }

        @Override
        public Stranger getItem(int position) {
            return mStrangerList.get(position);
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
            bindView(convertView, position);
            return convertView;
        }

        private View newView(ViewGroup parent) {
            return mInflater.inflate(R.layout.bubbling_result_item, parent, false);
        }

        private void bindView(View view, int position) {
            final Stranger stranger = getItem(position);

            TextView nameView = (TextView) view.findViewById(R.id.nickname);
            ImageView avatarImage = (ImageView) view.findViewById(R.id.avatar_image);
            nameView.setText(stranger.getShowName());
            MiscUtils.showAvatarThumb(mAvatarManager, stranger.getAvatarThumb(), avatarImage);
        }
    }
}
