package com.ekuater.labelchat.ui.fragment.friends;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.WeeklyStarConfirmMessage;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.util.ShowToast;

/**
 * @author LinYong
 */
public class WeeklyStarConfirmFragment extends Fragment {

    public static final String EXTRA_MESSAGE_ID = "message_id";

    private WeeklyStarConfirmMessage mConfirmMessage;
    private final FunctionCallListener mCallListener = new FunctionCallListener() {
        @Override
        public void onCallResult(int result, final int errorCode, String errorDesc) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (errorCode == CommandErrorCode.REQUEST_SUCCESS) {
                        ShowToast.makeText(getActivity(), R.drawable.emoji_smile, getString(R.string.accept_invitation_success)).show();
                    } else {
                        ShowToast.makeText(getActivity(), R.drawable.emoji_cry, getString(R.string.accept_invitation_failed)).show();
                    }
                }
            });
        }
    };
    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String session = mConfirmMessage.getSession();
            final ContactsManager contactsManager = ContactsManager.getInstance(getActivity());

            switch (v.getId()) {
                case R.id.btn_reject:
                    contactsManager.acceptWeeklyStarInvitation(session, false, null);
                    break;
                case R.id.btn_agree:
                    contactsManager.acceptWeeklyStarInvitation(session, true, mCallListener);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_team);
        }
        loadMessage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_weekly_star_confirm,
                container, false);
        TextView msgText = (TextView) view.findViewById(R.id.message);
        msgText.setText(mConfirmMessage.getMessage());
        view.findViewById(R.id.btn_reject).setOnClickListener(mClickListener);
        view.findViewById(R.id.btn_agree).setOnClickListener(mClickListener);
        return view;
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
            mConfirmMessage = WeeklyStarConfirmMessage.build(push);
        }

        if (mConfirmMessage == null) {
            getActivity().finish();
        }
        pushMessageManager.updatePushMessageProcessed(messageId);
    }
}
