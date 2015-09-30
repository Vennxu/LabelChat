package com.ekuater.labelchat.ui.fragment.register;


import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
public class ValidateIdentifyCodeFragment extends Fragment {

    public interface IListener {
        public void validateDone(String mobile, String verifyCode);
    }

    private static final String ARG_MOBILE = "mobile";
    private static final int RESEND_DELAY_INTERVAL = 1000;

    private static final int MSG_HANDLE_VERIFY_CODE_CHECK_RESULT = 101;

    public static ValidateIdentifyCodeFragment newInstance(String mobile, IListener listener) {
        ValidateIdentifyCodeFragment fragment = new ValidateIdentifyCodeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MOBILE, mobile);
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }

    private Activity mActivity;
    private String mVerifyCode;
    private boolean mCheckVerifyCode;
    private SimpleProgressDialog mProgressDialog;
    private int mResendDelay;
    private WeakReference<IListener> mListener;
    private String mArgMobile;
    private EditText mIdentifyCodeEdit;
    private Button mResendButton;
    private AccountManager mAccountManager;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.register_resend_identifying_code:
                    onResendClick();
                    break;
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

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_VERIFY_CODE_CHECK_RESULT:
                    handleVerifyCodeCheckResult(msg.arg1, msg.arg2);
                    break;
                default:
                    break;
            }
        }
    };

    public ValidateIdentifyCodeFragment() {
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
            actionBar.setTitle(R.string.input_identify_code);
        }

        mActivity = activity;

        Resources res = activity.getResources();
        mResendDelay = res.getInteger(R.integer.verify_code_resend_delay);
        mCheckVerifyCode = res.getBoolean(R.bool.check_verify_code);

        // Get arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            mArgMobile = arguments.getString(ARG_MOBILE);
        }
        mAccountManager = AccountManager.getInstance(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_validate_identify_code,
                container, false);
        mIdentifyCodeEdit = (EditText) view.findViewById(R.id.register_identifying_code);
        mIdentifyCodeEdit.setOnFocusChangeListener(mEditFocusChangeListener);
        mResendButton = (Button) view.findViewById(R.id.register_resend_identifying_code);
        mResendButton.setOnClickListener(mOnClickListener);
        view.findViewById(R.id.register_submit).setOnClickListener(mOnClickListener);
        TextView promptView = (TextView) view.findViewById(R.id.register_prompt_phone);
        promptView.setText(getString(R.string.send_identify_code_to_phone, mArgMobile));
        mIdentifyCodeEdit.requestFocus();
        resendDelay();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacks(mResendDelayInterval);
    }

    private int mResendDelayCount;

    private Runnable mResendDelayInterval = new Runnable() {
        @Override
        public void run() {
            resendDelayInternal();
        }
    };

    private void resendDelayInternal() {
        updateResendBtnText(mResendDelayCount);

        if (mResendDelayCount > 0) {
            mHandler.postDelayed(mResendDelayInterval, RESEND_DELAY_INTERVAL);
            mResendDelayCount--;
        } else {
            mResendButton.setEnabled(true);
        }
    }

    private void resendDelay() {
        mResendButton.setEnabled(false);
        mResendDelayCount = mResendDelay;
        resendDelayInternal();
    }

    private void updateResendBtnText(int sec) {
        mResendButton.setText((sec != 0)
                ? getString(R.string.resend_identify_code_time_count, sec)
                : getString(R.string.resend_identify_code));
    }

    private String getIdentifyCode() {
        return mIdentifyCodeEdit.getText().toString().trim();
    }

    private void notifyValidateDone(String verifyCode) {
        if (mListener != null) {
            IListener listener = mListener.get();
            if (listener != null) {
                listener.validateDone(mArgMobile, verifyCode);
            }
        }
    }

    private void onSubmitClick() {
        mVerifyCode = getIdentifyCode();

        if (TextUtils.isEmpty(mVerifyCode)) {
            ShowToast.makeText(getActivity(), R.drawable.emoji_smile, getActivity().
                    getResources().getString(R.string.input_identify_code)).show();
        } else if (mCheckVerifyCode) {
            checkVerifyCode(mArgMobile, mVerifyCode);
        } else {
            notifyValidateDone(mVerifyCode);
        }
    }

    private void checkVerifyCode(String mobile, String verifyCode) {
        mAccountManager.checkVerifyCode(mobile, verifyCode,
                new FunctionCallListener() {
                    @Override
                    public void onCallResult(int result, int errorCode, String errorDesc) {
                        Message msg = mHandler.obtainMessage(MSG_HANDLE_VERIFY_CODE_CHECK_RESULT,
                                result, errorCode);
                        mHandler.sendMessage(msg);
                    }
                });
        showProgressDialog();
    }

    private void handleVerifyCodeCheckResult(int result, int errorCode) {
        dismissProgressDialog();

        switch (result) {
            case FunctionCallListener.RESULT_CALL_SUCCESS:
                notifyValidateDone(mVerifyCode);
                break;
            case FunctionCallListener.RESULT_NETWORK_ERROR:
                ShowToast.makeText(mActivity, R.drawable.emoji_sad, mActivity.
                        getResources().getString(R.string.network_not_available_hint)).show();
                break;
            default:
                if (errorCode == CommandErrorCode.VERIFY_CODE_WRONG) {
                    ShowToast.makeText(mActivity, R.drawable.emoji_sad, mActivity.
                            getResources().getString(R.string.verify_code_wrong)).show();
                } else {
                    ShowToast.makeText(mActivity, R.drawable.emoji_sad, mActivity.
                            getResources().getString(R.string.verify_code_expired)).show();
                }
                break;
        }
    }

    private void onResendClick() {
        sendIdentifyingCode(mArgMobile);
        resendDelay();
    }

    private void sendIdentifyingCode(String mobile) {
        mAccountManager.requestVerifyCode(mobile, null, new CallListener(mobile) {
            @Override
            public void onCallResult(int result, int errorCode, String errorDesc) {
                getActivity().runOnUiThread(new ResultRunnable(errorCode));
            }
        });
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

        private final int mErrorCode;

        public ResultRunnable(int errorCode) {
            mErrorCode = errorCode;
        }

        @Override
        public void run() {
            int resId = R.string.send_verify_code_failed;

            switch (mErrorCode) {
                case CommandErrorCode.REQUEST_SUCCESS:
                    resId = R.string.send_verify_code_success;
                    ShowToast.makeText(getActivity(), R.drawable.emoji_smile, getActivity().
                            getResources().getString(R.string.send_verify_code_success)).show();
                    break;
                case CommandErrorCode.VERIFY_CODE_NOT_EXPIRED:
                    resId = R.string.verify_code_not_expired;
                    ShowToast.makeText(getActivity(), R.drawable.emoji_sad, getActivity().
                            getResources().getString(R.string.verify_code_not_expired)).show();
                    break;
                case CommandErrorCode.MOBILE_NOT_EXIST:
                    resId = R.string.mobile_not_exist;
                    break;
                default:
                    break;
            }
            ShowToast.makeText(getActivity(), R.drawable.emoji_cry, getActivity().
                    getResources().getString(resId)).show();
        }
    }
}
