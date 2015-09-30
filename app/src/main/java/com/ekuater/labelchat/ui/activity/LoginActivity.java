package com.ekuater.labelchat.ui.activity;

import android.app.ActionBar;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.util.ShowToast;

public class LoginActivity extends BackIconActivity
        implements OnClickListener, Handler.Callback {

    // private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String LOGIN_DIALOG_TAG = "LoginDialog";

    private static final int MSG_HANDLE_LOGIN_RESULT = 101;

    private int mAccountMinLength;
    private int mPasswordMinLength;
    private View mSignInBtn;
    private EditText mAccountEdit;
    private EditText mPasswordEdit;

    private SimpleProgressDialog mLoginDialog;
    private Handler mHandler;
    private AccountManager mAccountManager;
    private final AccountManager.IListener mAccountListener
            = new AccountManager.AbsListener() {

        @Override
        public void onLogin(int result) {
            onLoginResult(result);
        }
    };
    private final TextWatcher mAccountTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateSignInBtnEnabled();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    private final TextWatcher mPasswordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateSignInBtnEnabled();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    private final View.OnFocusChangeListener mEditFocusChangeListener
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        ImageView icon = (ImageView) findViewById(R.id.icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.sign_in);

        mHandler = new Handler(this);
        mAccountManager = AccountManager.getInstance(this);
        mAccountManager.registerListener(mAccountListener);
        Resources res = getResources();
        mAccountMinLength = Math.min(res.getInteger(R.integer.mobile_length),
                res.getInteger(R.integer.label_code_min_length));
        mPasswordMinLength = res.getInteger(R.integer.password_min_length);
        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAccountManager.unregisterListener(mAccountListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                login();
                break;
            case R.id.forgot_password:
                UILauncher.launchResetPasswordUI(this);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_HANDLE_LOGIN_RESULT:
                handleLoginResult(msg.arg1);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private void initViews() {
        mAccountEdit = (EditText) findViewById(R.id.account);
        mPasswordEdit = (EditText) findViewById(R.id.password);
        mAccountEdit.addTextChangedListener(mAccountTextWatcher);
        mAccountEdit.setOnFocusChangeListener(mEditFocusChangeListener);
        mPasswordEdit.addTextChangedListener(mPasswordTextWatcher);
        mPasswordEdit.setOnFocusChangeListener(mEditFocusChangeListener);
        mSignInBtn = findViewById(R.id.btn_sign_in);
        View forgotPasswordText = findViewById(R.id.forgot_password);

        mSignInBtn.setOnClickListener(this);
        forgotPasswordText.setOnClickListener(this);

        mAccountEdit.requestFocus();
        updateSignInBtnEnabled();
    }

    private void updateSignInBtnEnabled() {
        String account = getAccountString();
        String password = getPasswordString();

        boolean enable = (account.length() >= mAccountMinLength)
                && (password.length() >= mPasswordMinLength);
        mSignInBtn.setEnabled(enable);
    }

    private String getAccountString() {
        return mAccountEdit.getText().toString().trim();
    }

    private String getPasswordString() {
        return mPasswordEdit.getText().toString().trim();
    }

    private void launchMainUI() {
        UILauncher.launchMainUIWhenJustLogin(this);
        finish();
    }

    private void login() {
        String account = getAccountString();
        String password = getPasswordString();

        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
            ShowToast.makeText(this, R.drawable.emoji_smile, getString(R.string.empty_account_or_password)).show();
        } else {
            mAccountManager.login(account, password);
            showLoginDialog();
        }
    }

    private void onLoginResult(int result) {
        Message message = mHandler.obtainMessage(MSG_HANDLE_LOGIN_RESULT, result, 0);
        mHandler.sendMessage(message);
    }

    private void handleLoginResult(int result) {
        dismissLoginDialog();

        switch (result) {
            case ConstantCode.ACCOUNT_OPERATION_SUCCESS:
                launchMainUI();
                break;
            default:
                ShowToast.makeText(this, R.drawable.emoji_cry, getString(R.string.login_failure)).show();
                break;
        }
    }

    private void showLoginDialog() {
        if (mLoginDialog == null) {
            mLoginDialog = SimpleProgressDialog.newInstance();
            mLoginDialog.show(getSupportFragmentManager(), LOGIN_DIALOG_TAG);
        }
    }

    private void dismissLoginDialog() {
        if (mLoginDialog != null) {
            mLoginDialog.dismiss();
            mLoginDialog = null;
        }
    }
}
