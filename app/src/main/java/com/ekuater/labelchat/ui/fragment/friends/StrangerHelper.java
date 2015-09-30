package com.ekuater.labelchat.ui.fragment.friends;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.PersonalUser;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.util.ShowToast;

/**
 * Created by Leo on 2015/2/11.
 *
 * @author LinYong
 */
public class StrangerHelper {

    private static final int MSG_QUERY_USER_INFO_RESULT = 101;

    private final Context context;
    private final SimpleProgressHelper progressHelper;
    private final ContactsManager contactsManager;
    private final Handler handler;

    public StrangerHelper(Fragment fragment) {
        this(fragment.getActivity(), new SimpleProgressHelper(fragment));
    }

    public StrangerHelper(FragmentActivity activity) {
        this(activity, new SimpleProgressHelper(activity));
    }

    private StrangerHelper(Context context, SimpleProgressHelper progressHelper) {
        this.context = context;
        this.progressHelper = progressHelper;
        this.contactsManager = ContactsManager.getInstance(context);
        this.handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                boolean handled = true;

                switch (msg.what) {
                    case MSG_QUERY_USER_INFO_RESULT:
                        handleQueryStrangerResult((Stranger) msg.obj);
                        break;
                    default:
                        handled = false;
                        break;
                }

                return handled;
            }
        });
    }

    public void showStranger(String strangerUserId) {
        ContactsManager.UserQueryObserver observer = new ContactsManager.UserQueryObserver() {
            @Override
            public void onQueryResult(int result, Stranger user) {
                Message msg = handler.obtainMessage(MSG_QUERY_USER_INFO_RESULT,
                        result, 0, user);
                handler.sendMessage(msg);
            }
        };
        contactsManager.queryUserInfo(strangerUserId, observer);
        progressHelper.show();
    }

    private void handleQueryStrangerResult(Stranger user) {
        progressHelper.dismiss();
        if (user != null) {
            if (user.getUserId().equals(SettingHelper.getInstance(context).getAccountUserId())) {
                UILauncher.launchMyInfoUI(context);
            } else if (contactsManager.getUserContactByUserId(user.getUserId()) == null) {
                UILauncher.launchPersonalDetailUI(context, new PersonalUser(PersonalUser.STRANGER, new UserContact(user)));
            } else if (contactsManager.getUserContactByUserId(user.getUserId()) != null) {
                for (UserContact userContact : contactsManager.getAllUserContact()) {
                    if (userContact.getUserId().equals(user.getUserId())) {
                        UILauncher.launchPersonalDetailUI(context, new PersonalUser(PersonalUser.CONTACT, userContact));
                    }
                }
            } else {
                ShowToast.makeText(context, R.drawable.emoji_sad, context.getResources().getString(R.string.query_stranger_failed)).show();
            }
        }
    }
}
