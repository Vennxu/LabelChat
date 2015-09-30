package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemLabel;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.datastruct.UserTheme;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.util.L;

/**
 * Created by Leo on 2015/2/6.
 *
 * @author LinYong
 */
public class ContactUserAdapter extends AbsUserAdapter {

    private static final String TAG = ContactUserAdapter.class.getSimpleName();

    private static final int MSG_CONTACT_DEFRIENDED_ME = 101;
    private static final int MSG_QUERY_CONTACT_INFO_RESULT = 102;
    private static final int MSG_RECOMMEND_LABEL_RESULT = 103;

    private static final int REQUEST_SELECT_SYSTEM_LABEL = 10001;

    private UserContact contact;
    private ContactsManager contactsManager;
    private UserLabelManager labelManager;
    private SimpleProgressHelper progressHelper;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONTACT_DEFRIENDED_ME:
                    handleContactDefriendedMe((String) msg.obj);
                    break;
                case MSG_QUERY_CONTACT_INFO_RESULT:
                    handleQueryContactInfoResult(msg.arg1, (UserContact) msg.obj);
                    break;
                case MSG_RECOMMEND_LABEL_RESULT:
                    handleRecommendLabelResult(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };

    private final ContactsManager.IListener mContactListener
            = new ContactsManager.AbsListener() {
        @Override
        public void onContactDefriendedMe(String friendUserId) {
            Message message = mHandler.obtainMessage(MSG_CONTACT_DEFRIENDED_ME, friendUserId);
            mHandler.sendMessage(message);
        }
    };

    public ContactUserAdapter(Fragment fragment, UserAdapterListener listener,
                              UserContact contact) {
        super(fragment, listener);
        this.contact = contact;
        Activity activity = fragment.getActivity();
        baseUserInfo = BaseUserInfo.fromContact(contact);
        contactsManager = ContactsManager.getInstance(activity);
        labelManager = UserLabelManager.getInstance(activity);
        progressHelper = new SimpleProgressHelper(fragment);
    }

    @Override
    protected BasePage newContentPage(PageEnum page) {
        BasePage newPage;

        switch (page) {
            case USER_INFO:
                newPage = new ContactInfoPage(fragment, contact);
                break;
            case LABEL:
                newPage = new ContactLabelPage(fragment, contact);
                break;
            case LABEL_STORY:
                newPage = new MyLabelStoryPage(fragment, new Stranger(contact));
                break;
            case THROW_PHOTO:
                newPage = new UserThrowPhotosPage(fragment, contact.getUserId());
                break;
            default:
                newPage = null;
                break;
        }

        return newPage;
    }

    @Override
    public boolean showAvatarRightIcon() {
        return true;
    }

    @Override
    public int getAvatarRightIcon() {
        return R.drawable.ic_recommend_label_to_user;
    }

    @Override
    public void onAvatarRightIconClick() {
        onRecommendLabel();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        contactsManager.registerListener(mContactListener);
        queryContactInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        contactsManager.unregisterListener(mContactListener);
    }

    @Override
    public void setupOperationBar(LayoutInflater inflater, ViewGroup container) {
        View bar = inflater.inflate(R.layout.user_show_contact_bar, container, false);
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_send_message:
                        UILauncher.launchChattingUI(getActivity(), baseUserInfo.userId);
                        onBackIconClick();
                        break;
                    default:
                        break;
                }
            }
        };
        bar.findViewById(R.id.btn_send_message).setOnClickListener(clickListener);
        container.addView(bar);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SELECT_SYSTEM_LABEL:
                handleSelectSystemLabel(resultCode, data);
                break;
            default:
                break;
        }
    }

    @Override
    public UserTheme getUserTheme() {
        return contact.getTheme();
    }

    @Override
    protected void onPageEvent(PageEvent event) {
        super.onPageEvent(event);
        switch (event.event) {
            case CONTACT_UPDATE:
                onContactUpdateEvent((UserContact) event.extra);
                break;
            default:
                break;
        }
    }

    private void onContactUpdateEvent(UserContact contact) {
        if (contact != null && this.contact.getUserId()
                .equals(contact.getUserId())) {
            this.contact = contact;
            baseUserInfo = BaseUserInfo.fromContact(contact);
            updateBaseUserInfo();
        }
    }

    private void handleContactDefriendedMe(String friendUserId) {
        if (contact != null && contact.getUserId().equals(friendUserId)) {
            onBackIconClick();
        }
    }

    private void queryContactInfo() {
        contactsManager.queryContactInfo(contact.getUserId(),
                new ContactsManager.ContactQueryObserver() {
                    @Override
                    public void onQueryResult(int result, UserContact contact) {
                        Message msg = mHandler.obtainMessage(MSG_QUERY_CONTACT_INFO_RESULT,
                                result, 0, contact);
                        mHandler.sendMessage(msg);
                    }
                });
    }

    private void handleQueryContactInfoResult(int result, UserContact contact) {
        L.v(TAG, "handleQueryContactInfoResult(), result=%1$d, count=%2$s", result, contact);
        if (contact != null && contact.getUserId().equals(this.contact.getUserId())) {
            contact.setId(this.contact.getId());
            this.contact = contact;
            baseUserInfo = BaseUserInfo.fromContact(contact);
            updateBaseUserInfo();
            contactsManager.updateContact(contact);
            updateLabelPage();
        }
    }

    private void updateLabelPage() {
        BasePage basePage = getPage(PageEnum.LABEL);
        if (basePage instanceof ContactLabelPage) {
            ContactLabelPage labelPage = (ContactLabelPage) basePage;
            labelPage.updateContact(contact);
        }
    }

    private void onRecommendLabel() {
        UserLabel[] labels = contact.getLabels();
        String[] filterLabelIds = new String[labels.length];

        for (int i = 0; i < labels.length; ++i) {
            filterLabelIds[i] = labels[i].getId();
        }
        UILauncher.launchSelectSystemLabelUI(fragment,
                REQUEST_SELECT_SYSTEM_LABEL, filterLabelIds,
                fragment.getString(R.string.recommend_label_for_someone,
                        fragment.getString(contact.getSex() == ConstantCode.USER_SEX_FEMALE
                                ? R.string.her : R.string.he)),
                fragment.getString(R.string.select), 1);
    }

    private void handleSelectSystemLabel(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            Parcelable[] parcelables = data.getParcelableArrayExtra("selected_labels");
            if (parcelables != null && parcelables.length > 0) {
                SystemLabel label = (SystemLabel) parcelables[0];
                recommendLabel(new BaseLabel[]{label.toBaseLabel()});
            }
        }
    }

    private void recommendLabel(BaseLabel[] labels) {
        progressHelper.show();
        labelManager.recommendLabel(contact.getUserId(), labels, new FunctionCallListener() {
            @Override
            public void onCallResult(int result, int errorCode, String errorDesc) {
                mHandler.sendMessage(mHandler.obtainMessage(MSG_RECOMMEND_LABEL_RESULT,
                        result, errorCode));
            }
        });
    }

    private void handleRecommendLabelResult(int result) {
        Activity activity = getActivity();
        int resId;

        progressHelper.dismiss();
        switch (result) {
            case FunctionCallListener.RESULT_CALL_SUCCESS:
                resId = R.string.recommend_label_success;
                break;
            default:
                resId = R.string.recommend_label_failed;
                break;
        }

        if (activity != null) {
            Toast.makeText(activity, resId, Toast.LENGTH_SHORT).show();
        }
    }
}
