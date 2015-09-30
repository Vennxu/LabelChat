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


import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.util.ShowToast;

import java.lang.ref.WeakReference;

/**
 * @author LinYong
 */
public class SendIdentifyCodeFragment extends Fragment {

    public interface IListener {
        public void identifyingCodeSent(String mobile, boolean success);
    }

    public static SendIdentifyCodeFragment newInstance(IListener listener, String scenario) {
        return newInstance(listener, scenario, null);
    }

    public static SendIdentifyCodeFragment newInstance(IListener listener, String scenario,
                                                       String title) {
        SendIdentifyCodeFragment fragment = new SendIdentifyCodeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.mScenario = scenario;
        fragment.mTitle = title;
        fragment.setListener(listener);
        return fragment;
    }

    private String mScenario;
    private String mTitle;
    private WeakReference<IListener> mListener;
    private EditText mPhoneEdit;
    private AccountManager mAccountManager;
    private SimpleProgressDialog mProgressDialog;
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

    public SendIdentifyCodeFragment() {
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
                actionBar.setTitle(R.string.register);
            } else {
                actionBar.setTitle(mTitle);
            }
        }
        mAccountManager = AccountManager.getInstance(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_send_identify_code,
                container, false);
        mPhoneEdit = (EditText) view.findViewById(R.id.register_phone_number);
        mPhoneEdit.setOnFocusChangeListener(mEditFocusChangeListener);
        view.findViewById(R.id.register_submit).setOnClickListener(mClickListener);
        mPhoneEdit.requestFocus();

        return view;
    }

    private String getPhoneNumber() {
        return mPhoneEdit.getText().toString().trim();
    }

    private void notifyIdentifyingCodeSent(String phone, boolean success) {
        if (mListener != null) {
            IListener listener = mListener.get();
            if (listener != null) {
                listener.identifyingCodeSent(phone, success);
            }
        }
    }

    private void onSubmitClick() {
        String phone = getPhoneNumber();
        int mobileLength = getResources().getInteger(R.integer.mobile_length);

        if (phone == null || phone.length() != mobileLength) {
            ShowToast.makeText(getActivity(), R.drawable.emoji_sad, getActivity().
                    getResources().getString(R.string.illegal_phone_number)).show();
        } else {
            sendIdentifyingCode(phone);
        }
    }

    private void sendIdentifyingCode(String mobile) {
        mAccountManager.requestVerifyCode(mobile, mScenario, new CallListener(mobile) {
            @Override
            public void onCallResult(int result, int errorCode, String errorDesc) {
                getActivity().runOnUiThread(new ResultRunnable(mMobile, errorCode));
            }
        });
        showProgressDialog();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = SimpleProgressDialog.newInstance();
            mProgressDialog.show(getFragmentManager(), "SimpleProgressDialog");
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private static abstract class CallListener implements FunctionCallListener {

        protected final String mMobile;

        public CallListener(String mobile) {
            mMobile = mobile;
        }
    }

    private final class ResultRunnable implements Runnable {

        private final String mMobile;
        private final int mErrorCode;

        public ResultRunnable(String mobile, int errorCode) {
            mMobile = mobile;
            mErrorCode = errorCode;
        }

        @Override
        public void run() {
            int resId = R.string.send_verify_code_failed;
            boolean success = false;

            dismissProgressDialog();

            switch (mErrorCode) {
                case CommandErrorCode.REQUEST_SUCCESS:
                    success = true;
                    break;
                case CommandErrorCode.VERIFY_CODE_NOT_EXPIRED:
                    resId = R.string.verify_code_not_expired;
                    break;
                case CommandErrorCode.MOBILE_NOT_EXIST:
                    resId = R.string.mobile_not_exist;
                    break;
                case CommandErrorCode.MOBILE_ALREADY_EXIST:
                    resId = R.string.mobile_registered;
                default:
                    break;
            }
            if (!success) {
                ShowToast.makeText(getActivity(), R.drawable.emoji_cry, getActivity().getResources().getString(resId)).show();
            }

            notifyIdentifyingCodeSent(mMobile, success);
        }
    }
}
