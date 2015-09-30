package com.ekuater.labelchat.ui.fragment.register;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.util.ShowToast;

/**
 * @author LinYong
 */
public class RegisterVerifyMobileFragment extends Fragment
        implements View.OnClickListener, Handler.Callback {

    public interface Listener {
        public void verifyDone(String mobile, String verifyCode);
    }

    private static final int MSG_HANDLE_VERIFY_CODE_SEND_RESULT = 101;
    private static final int MSG_HANDLE_RESEND_DELAY_INTERVAL = 102;
    private static final int MSG_HANDLE_VERIFY_CODE_CHECK_RESULT = 103;

    private static final int SECOND_INTERVAL = 1000;

    public static RegisterVerifyMobileFragment newInstance(Listener listener) {
        RegisterVerifyMobileFragment instance = new RegisterVerifyMobileFragment();
        instance.mListener = listener;
        return instance;
    }

    private Listener mListener;

    private Activity mActivity;
    private AccountManager mAccountManager;
    private SimpleProgressDialog mProgressDialog;
    private int mMobileLength;
    private int mVerifyCodeLength;
    private int mResendDelay;

    private boolean mCheckVerifyCode;

    private EditText mMobileEdit;
    private EditText mVerifyCodeEdit;
    private Button mSendVerifyCodeBtn;
    private Button mSubmitBtn;
    private CheckBox mPrivacyCheck;

    private boolean mMobileValid = false;
    private boolean mInReSendDelay = false;
    private boolean mVerifyCodeValid = false;

    private String mMobileNumber;
    private String mVerifyCode;
    private Handler mHandler;

    private TextWatcher mMobileTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mMobileValid = (s.toString().length() == mMobileLength);
            updateSendVerifyCodeBtnEnabled();
            updateSubmitBtnEnabled();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    private TextWatcher mVerifyCodeTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mVerifyCodeValid = (s.toString().length() == mVerifyCodeLength);
            updateSubmitBtnEnabled();
        }

        @Override
        public void afterTextChanged(Editable s) {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(this);
        mActivity = getActivity();
        Resources res = mActivity.getResources();
        mAccountManager = AccountManager.getInstance(mActivity);
        mMobileLength = res.getInteger(R.integer.mobile_length);
        mVerifyCodeLength = res.getInteger(R.integer.verify_code_length);
        mCheckVerifyCode = res.getBoolean(R.bool.check_verify_code);
        mResendDelay = res.getInteger(R.integer.verify_code_resend_delay);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_verify_mobile, container, false);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(R.string.mobile_register);

        mMobileEdit = (EditText) view.findViewById(R.id.mobile_number);
        mVerifyCodeEdit = (EditText) view.findViewById(R.id.verify_code);
        mSendVerifyCodeBtn = (Button) view.findViewById(R.id.send_verify_code);
        mSubmitBtn = (Button) view.findViewById(R.id.submit);
        mPrivacyCheck = (CheckBox) view.findViewById(R.id.privacy_agreement);
        TextView privacyText = (TextView) view.findViewById(R.id.privacy_agreement_text);

        mMobileEdit.addTextChangedListener(mMobileTextWatcher);
        mVerifyCodeEdit.addTextChangedListener(mVerifyCodeTextWatcher);
        mMobileEdit.setOnFocusChangeListener(mEditFocusChangeListener);
        mVerifyCodeEdit.setOnFocusChangeListener(mEditFocusChangeListener);
        mSendVerifyCodeBtn.setOnClickListener(this);
        mSubmitBtn.setOnClickListener(this);
        mPrivacyCheck.setOnClickListener(this);
        privacyText.setOnClickListener(this);

        mMobileValid = false;
        mInReSendDelay = false;
        mVerifyCodeValid = false;
        updateSendVerifyCodeBtnEnabled();
        updateSubmitBtnEnabled();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeMessages(MSG_HANDLE_RESEND_DELAY_INTERVAL);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_verify_code:
                onSendVerifyCode();
                break;
            case R.id.submit:
                onSubmit();
                break;
            case R.id.privacy_agreement:
                onPrivacyCheckToggle();
                break;
            case R.id.privacy_agreement_text:
                UILauncher.launchPrivacyUI(getActivity());
                break;
            default:
                break;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_HANDLE_VERIFY_CODE_SEND_RESULT:
                handleVerifyCodeSendResult(msg.arg2);
                break;
            case MSG_HANDLE_RESEND_DELAY_INTERVAL:
                handleResendDelayInterval(--msg.arg1);
                break;
            case MSG_HANDLE_VERIFY_CODE_CHECK_RESULT:
                handleVerifyCodeCheckResult(msg.arg1, msg.arg2);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void onSendVerifyCode() {
        mMobileNumber = mMobileEdit.getText().toString();
        sendVerifyCode(mMobileNumber);
    }

    private void onSubmit() {
        mVerifyCode = mVerifyCodeEdit.getText().toString();

        if (mCheckVerifyCode) {
            checkVerifyCode(mMobileNumber, mVerifyCode);
        } else {
            if (mListener != null) {
                mMobileNumber = mMobileEdit.getText().toString();
                mListener.verifyDone(mMobileNumber, mVerifyCode);
            }
        }
    }

    private void onPrivacyCheckToggle() {
        updateSubmitBtnEnabled();
    }

    private void updateSendVerifyCodeBtnEnabled() {
        boolean enable = mMobileValid && !mInReSendDelay;
        mSendVerifyCodeBtn.setEnabled(enable);
    }

    private void updateSubmitBtnEnabled() {
        boolean enable = mMobileValid && mVerifyCodeValid && mPrivacyCheck.isChecked();
        mSubmitBtn.setEnabled(enable);
    }

    private void updateSendVerifyCodeBtnText(int sec) {
        mSendVerifyCodeBtn.setText((sec != 0)
                ? getString(R.string.resend_identify_code_time_count, sec)
                : getString(R.string.resend_identify_code));
    }

    private void startResendDelay() {
        Message msg = mHandler.obtainMessage(MSG_HANDLE_RESEND_DELAY_INTERVAL,
                mResendDelay, 0);
        mHandler.sendMessageDelayed(msg, SECOND_INTERVAL);
        mInReSendDelay = true;
        updateSendVerifyCodeBtnEnabled();
    }

    private void handleResendDelayInterval(int sec) {
        if (sec >= 0) {
            Message msg = mHandler.obtainMessage(MSG_HANDLE_RESEND_DELAY_INTERVAL,
                    sec, 0);
            mHandler.sendMessageDelayed(msg, SECOND_INTERVAL);
            updateSendVerifyCodeBtnText(sec);
        } else {
            mInReSendDelay = false;
            updateSendVerifyCodeBtnEnabled();
        }
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

    private void sendVerifyCode(String mobile) {
        mAccountManager.requestVerifyCode(mobile, CommandFields.Normal.SCENARIO_REGISTER,
                new FunctionCallListener() {
                    @Override
                    public void onCallResult(int result, int errorCode, String errorDesc) {
                        Message msg = mHandler.obtainMessage(MSG_HANDLE_VERIFY_CODE_SEND_RESULT,
                                result, errorCode);
                        mHandler.sendMessage(msg);
                    }
                });
        showProgressDialog();
    }

    private void handleVerifyCodeSendResult(int errorCode) {
        dismissProgressDialog();
        switch (errorCode) {
            case CommandErrorCode.REQUEST_SUCCESS:
                startResendDelay();
                ShowToast.makeText(mActivity, R.drawable.emoji_smile, mActivity.
                        getResources().getString(R.string.send_verify_code_success)).show();
                break;
            case CommandErrorCode.VERIFY_CODE_NOT_EXPIRED:
                ShowToast.makeText(mActivity, R.drawable.emoji_sad, mActivity.
                        getResources().getString(R.string.verify_code_not_expired)).show();
                break;
            case CommandErrorCode.MOBILE_ALREADY_EXIST:
                ShowToast.makeText(mActivity, R.drawable.emoji_cry, mActivity.
                        getResources().getString(R.string.mobile_registered)).show();
                break;
            default:
                ShowToast.makeText(mActivity, R.drawable.emoji_cry, mActivity.
                        getResources().getString(R.string.send_verify_code_failed)).show();
                break;
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
                if (mListener != null) {
                    mListener.verifyDone(mMobileNumber, mVerifyCode);
                }
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
}
