package com.ekuater.labelchat.ui.fragment.register;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.util.ShowToast;

import java.lang.ref.WeakReference;

/**
 * @author LinYong
 */
public class SetPasswordFragment extends Fragment {

    public interface IListener {
        public void setPasswordResult(String mobile, String verifyCode, String password);
    }

    private static final String ARG_MOBILE = "mobile";
    private static final String ARG_VERIFY_CODE = "verify_code";

    public static SetPasswordFragment newInstance(String mobile, String verifyCode,
                                                  IListener listener) {
        return newInstance(mobile, verifyCode, null, listener);
    }

    public static SetPasswordFragment newInstance(String mobile, String verifyCode,
                                                  String title,
                                                  IListener listener) {
        SetPasswordFragment fragment = new SetPasswordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MOBILE, mobile);
        args.putString(ARG_VERIFY_CODE, verifyCode);
        fragment.setArguments(args);
        fragment.mTitle = title;
        fragment.setListener(listener);
        return fragment;
    }

    private String mTitle;
    private WeakReference<IListener> mListener;
    private String mArgMobile;
    private String mArgVerifyCode;
    private EditText mPasswordEdit;
    private EditText mPasswordAgainEdit;
    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.register_submit:
                    onSubmitClick();
                    break;
                default:
                    break;
            }
        }
    };
    private View.OnFocusChangeListener mEditFocusChangeListener
            = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            final ViewParent parent = v.getParent();
            if (parent instanceof View) {
                final View parentView = (View) parent;
                parentView.setActivated(hasFocus);
            }
        }
    };

    public SetPasswordFragment() {
    }

    public void setListener(IListener listener) {
        if (listener == null) {
            mListener = null;
        } else {
            mListener = new WeakReference<IListener>(listener);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            if (TextUtils.isEmpty(mTitle)) {
                actionBar.setTitle(R.string.set_password);
            } else {
                actionBar.setTitle(mTitle);
            }
        }

        Bundle args = getArguments();
        if (args != null) {
            mArgMobile = args.getString(ARG_MOBILE);
            mArgVerifyCode = args.getString(ARG_VERIFY_CODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_set_password, container, false);
        view.findViewById(R.id.register_submit).setOnClickListener(mClickListener);
        TextView phoneText = (TextView) view.findViewById(R.id.register_phone);
        phoneText.setText(mArgMobile);
        mPasswordEdit = (EditText) view.findViewById(R.id.register_password);
        mPasswordEdit.setOnFocusChangeListener(mEditFocusChangeListener);
        mPasswordAgainEdit = (EditText) view.findViewById(R.id.register_password_again);
        mPasswordAgainEdit.setOnFocusChangeListener(mEditFocusChangeListener);
        mPasswordEdit.requestFocus();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void onSubmitClick() {
        String password = getPasswordString();
        String againPassword = getPasswordAgainString();

        if (TextUtils.isEmpty(password) || password.length() < 6
                || password.length() > 20) {
            ShowToast.makeText(getActivity(), R.drawable.emoji_smile, getActivity().
                    getResources().getString(R.string.enter_password_prompt)).show();

            mPasswordEdit.requestFocus();
        } else if (!password.equals(againPassword)) {
            ShowToast.makeText(getActivity(), R.drawable.emoji_sad, getActivity().
                    getResources().getString(R.string.inconformity_input_password)).show();

            mPasswordEdit.requestFocus();
        } else {
            notifySetPasswordResult(password);
        }
    }

    private String getPasswordString() {
        return mPasswordEdit.getText().toString().trim();
    }

    private String getPasswordAgainString() {
        return mPasswordAgainEdit.getText().toString().trim();
    }

    private void notifySetPasswordResult(String password) {
        if (mListener != null) {
            IListener listener = mListener.get();
            if (listener != null) {
                listener.setPasswordResult(mArgMobile, mArgVerifyCode, password);
            }
        }
    }
}
