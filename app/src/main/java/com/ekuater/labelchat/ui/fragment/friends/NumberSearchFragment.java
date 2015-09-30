package com.ekuater.labelchat.ui.fragment.friends;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.util.L;

/**
 * @author LinYong
 */
public class NumberSearchFragment extends Fragment
        implements View.OnClickListener, TextWatcher {

    private static final String TAG = NumberSearchFragment.class.getSimpleName();

    private static final int MSG_HANDLE_EXACT_SEARCH_RESULT = 101;

    private ContactsManager mContactsManager;
    private EditText mKeywordEdit;
    private View mSearchBtn;
    private ImageView mSearchImage;
    private int mMobileLength;
    private int mLabelCodeMinLength;
    private int mLabelCodeMaxLength;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_EXACT_SEARCH_RESULT:
                    handleExactSearchResult(msg.arg1, (Stranger) msg.obj);
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
        final Resources res = activity.getResources();
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.search_friend);
        }

        mContactsManager = ContactsManager.getInstance(activity);
        mMobileLength = res.getInteger(R.integer.mobile_length);
        mLabelCodeMinLength = res.getInteger(R.integer.label_code_min_length);
        mLabelCodeMaxLength = res.getInteger(R.integer.label_code_max_length);

        // Force show soft input
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.softInputMode |= WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_number_search, container, false);
        mSearchBtn = view.findViewById(R.id.btn_search);
        mKeywordEdit = (EditText) view.findViewById(R.id.search_input);
        mSearchBtn.setOnClickListener(this);
        mKeywordEdit.addTextChangedListener(this);
        mKeywordEdit.setText(null);
        mSearchImage = (ImageView) view.findViewById(R.id.loading);
        mSearchImage.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopSearchAnimation();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                onExactSearch(mKeywordEdit.getText().toString());
                break;
            default:
                break;
        }
    }

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
            int start = mKeywordEdit.getSelectionStart() - deleteCount;
            int end = mKeywordEdit.getSelectionEnd();
            mKeywordEdit.removeTextChangedListener(this);
            s.delete(start, end);
            mKeywordEdit.setSelection(start);
            mKeywordEdit.addTextChangedListener(this);
        }

        mSearchBtn.setEnabled(searchEnable);
    }

    private void finish() {
        getActivity().finish();
    }

    private void onExactSearch(String searchWord) {
        L.v(TAG, "onExactSearch(), searchWord=" + searchWord);

        startSearchAnimation();
        mContactsManager.exactSearchUser(searchWord,
                new ContactsManager.UserQueryObserver() {
                    @Override
                    public void onQueryResult(int result, Stranger stranger) {
                        L.v(TAG, "onQueryResult(), result=" + result);
                        Message message = mHandler.obtainMessage(MSG_HANDLE_EXACT_SEARCH_RESULT,
                                result, 0, stranger);
                        mHandler.sendMessage(message);
                    }
                });
    }

    private void handleExactSearchResult(int result, Stranger stranger) {
        final Activity activity = getActivity();

        L.v(TAG, "handleExactSearchResult(), result=" + result);

        stopSearchAnimation();

        switch (result) {
            case ContactsManager.QUERY_RESULT_SUCCESS:
                if (stranger != null) {
                    if (mContactsManager.getUserContactByUserId(stranger.getUserId()) != null) {
                        UILauncher.launchFriendDetailUI(activity, stranger.getUserId());
                        finish();
                    } else {
                        UILauncher.launchStrangerDetailUI(activity, stranger);
                    }
                } else {
                    ShowToast.makeText(activity, R.drawable.emoji_sad, getString(R.string.number_search_no_user)).show();
                }
                break;
            case ContactsManager.QUERY_RESULT_ILLEGAL_ARGUMENTS:
                ShowToast.makeText(activity, R.drawable.emoji_smile, getString(R.string.input_mobile_or_label_code_prompt,
                        mMobileLength, mLabelCodeMinLength)).show();
                break;
            default:
                ShowToast.makeText(activity, R.drawable.emoji_sad, getString(R.string.number_search_no_user)).show();
                break;
        }
    }

    private void startSearchAnimation() {
        if (mSearchImage != null) {
            mSearchImage.setVisibility(View.VISIBLE);

            Drawable drawable = mSearchImage.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                animationDrawable.start();
            }
        }
    }

    private void stopSearchAnimation() {
        if (mSearchImage != null) {
            Drawable drawable = mSearchImage.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                animationDrawable.stop();
            }

            mSearchImage.setVisibility(View.GONE);
        }
    }
}
