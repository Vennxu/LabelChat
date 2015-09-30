package com.ekuater.labelchat.ui.fragment.friends;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.util.ShowToast;

/**
 * Created by Leo on 2015/4/10.
 *
 * @author LinYong
 */
public class UserDiscoveryFragment extends Fragment implements Handler.Callback {

    private static final int MSG_HANDLE_EXACT_SEARCH_RESULT = 101;

    private Handler mHandler;
    private Fragment mNewUserFragment;
    private ContactsManager mContactsManager;
    private int mLabelCodeMinLength;
    private int mLabelCodeMaxLength;
    private int mMobileLength;

    private EditText mSearchEdit;
    private TextView mSearchBt;
    private ProgressBar mSearchProgress;

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            final String text = s.toString();
            final int length = text.length();
            int deleteCount;
            boolean searchEnable;

            if (text.startsWith("1")) {  // if mobile number search
                deleteCount = length - mMobileLength;
                searchEnable = (length >= mMobileLength);
            } else {  // else label code search
                deleteCount = length - mLabelCodeMaxLength;
                searchEnable = (length >= mLabelCodeMinLength);
            }

            if (deleteCount > 0) {
                int start = mSearchEdit.getSelectionStart() - deleteCount;
                int end = mSearchEdit.getSelectionEnd();
                mSearchEdit.removeTextChangedListener(this);
                s.delete(start, end);
                mSearchEdit.setSelection(start);
                mSearchEdit.addTextChangedListener(this);
            }

            mSearchBt.setEnabled(searchEnable);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        ActionBar actionBar = activity.getActionBar();
        Resources res = activity.getResources();
        if (actionBar != null) {
            actionBar.hide();
        }

        mHandler = new Handler(this);
        Bundle args = new Bundle();
        args.putBoolean(NewUserFragment.EXTRA_AS_PART, true);
        mNewUserFragment = Fragment.instantiate(activity,
                NewUserFragment.class.getName(), args);
        mContactsManager = ContactsManager.getInstance(activity);
        mMobileLength = res.getInteger(R.integer.mobile_length);
        mLabelCodeMinLength = res.getInteger(R.integer.label_code_min_length);
        mLabelCodeMaxLength = res.getInteger(R.integer.label_code_max_length);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_discovery, container, false);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        ImageView icon = (ImageView) rootView.findViewById(R.id.icon);
        title.setText(R.string.search);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        showFragment(mNewUserFragment);

        mSearchProgress = (ProgressBar) rootView.findViewById(R.id.contact_progress);
        mSearchEdit = (EditText) rootView.findViewById(R.id.contact_edit);
        mSearchBt = (TextView) rootView.findViewById(R.id.contact_conform_post);
        mSearchEdit.addTextChangedListener(mTextWatcher);
        mSearchBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchProgress.setVisibility(View.VISIBLE);
                mSearchBt.setVisibility(View.GONE);
                onExactSearch(mSearchEdit.getText().toString());
            }
        });
        rootView.findViewById(R.id.focus_view).requestFocus();
        return rootView;
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_HANDLE_EXACT_SEARCH_RESULT:
                handleExactSearchResult(msg.arg1, (Stranger) msg.obj);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, fragment.getClass().getName());
        transaction.commitAllowingStateLoss();
    }

    private void onExactSearch(String searchWord) {
        mContactsManager.exactSearchUser(searchWord,
                new ContactsManager.UserQueryObserver() {
                    @Override
                    public void onQueryResult(int result, Stranger stranger) {
                        Message message = mHandler.obtainMessage(MSG_HANDLE_EXACT_SEARCH_RESULT,
                                result, 0, stranger);
                        mHandler.sendMessage(message);
                    }
                });
    }

    private void handleExactSearchResult(int result, Stranger stranger) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        mSearchProgress.setVisibility(View.GONE);
        mSearchBt.setVisibility(View.VISIBLE);
        switch (result) {
            case ContactsManager.QUERY_RESULT_SUCCESS:
                if (stranger != null) {
                    if (mContactsManager.getUserContactByUserId(stranger.getUserId()) != null) {
                        UILauncher.launchFriendDetailUI(activity, stranger.getUserId());
                    } else {
                        UILauncher.launchStrangerDetailUI(activity, stranger);
                    }
                } else {
                    ShowToast.makeText(activity, R.drawable.emoji_sad,
                            getString(R.string.number_search_no_user)).show();
                }
                break;
            case ContactsManager.QUERY_RESULT_ILLEGAL_ARGUMENTS:
                ShowToast.makeText(activity, R.drawable.emoji_smile,
                        getString(R.string.input_mobile_or_label_code_prompt,
                        mMobileLength, mLabelCodeMinLength)).show();
                break;
            default:
                ShowToast.makeText(activity, R.drawable.emoji_sad,
                        getString(R.string.number_search_no_user)).show();
                break;
        }
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }
}
