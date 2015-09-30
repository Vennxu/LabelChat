package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.ui.UILauncher;

/**
 * Created by Administrator on 2015/2/4.
 *
 * @author Fan Chong
 */
public class ContactLabelPage extends BasePage {
    private UserContact mContact;
    private ContactLabelAdapter adapter;
    private View mBackgroundView;

    public ContactLabelPage(Fragment fragment, UserContact contact) {
        super(fragment);
        mContact = contact;
        adapter = new ContactLabelAdapter(mContext, contact);
    }

    public void updateContact(UserContact contact) {
        mContact = contact;
        adapter.updateContact(mContact);
    }

    @Override
    public ListAdapter getContentAdapter() {
        return adapter;
    }

    @Override
    public AdapterView.OnItemClickListener getContentItemClickListener() {
        return adapter;
    }

    @Override
    public void onAddToContentForeground(ViewGroup container) {
        if (mBackgroundView == null) {
            mBackgroundView = LayoutInflater.from(mContext).inflate(
                    R.layout.other_no_label_layout, container, false);
        }
        ViewGroup parent = (ViewGroup) mBackgroundView.getParent();
        if (parent != null) {
            parent.removeView(mBackgroundView);
        }
        container.addView(mBackgroundView);
        mBackgroundView.setVisibility(mContact.getLabels() != null
                ? View.GONE : View.VISIBLE);
    }
}
