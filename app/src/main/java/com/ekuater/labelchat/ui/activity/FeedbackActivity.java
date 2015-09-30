package com.ekuater.labelchat.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.delegate.MiscManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.util.ShowToast;

public class FeedbackActivity extends BackIconActivity implements Handler.Callback {

    private static final int MSG_HANDLE_FEEDBACK_RESULT = 101;

    private EditText mMessageEdit;
    private EditText mContactEdit;
    private Button mSubmitBtn;
    private SimpleProgressDialog mProgressDialog;
    private Handler mHandler = new Handler(this);
    private final TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mSubmitBtn.setEnabled(!TextUtils.isEmpty(s.toString()));
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        initView();
        setListener();
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_HANDLE_FEEDBACK_RESULT:
                handleFeedbackResult(msg.arg1 != 0);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private void initView() {
        ImageView icon = (ImageView) findViewById(R.id.icon);
        TextView title = (TextView) findViewById(R.id.title);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title.setText(R.string.feedback);
        mSubmitBtn = (Button) findViewById(R.id.btn_feedback);
        mMessageEdit = (EditText) findViewById(R.id.feedback_message);
        mMessageEdit.addTextChangedListener(mTextWatcher);
        mContactEdit = (EditText) findViewById(R.id.contact_info);
        mSubmitBtn.setEnabled(!TextUtils.isEmpty(mMessageEdit.getText().toString()));
    }

    private void uploadFeedbackMessage() {
        final String message = mMessageEdit.getText().toString();
        final String contactInfo = mContactEdit.getText().toString();
        final String nickname = SettingHelper.getInstance(this).getAccountNickname();

        if (!TextUtils.isEmpty(message)) {
            MiscManager miscManager = MiscManager.getInstance(this);
            miscManager.uploadFeedbackSuggestion(nickname, message, contactInfo,
                    new FunctionCallListener() {
                        @Override
                        public void onCallResult(int result, int errorCode, String errorDesc) {
                            final boolean success = (errorCode == CommandErrorCode.REQUEST_SUCCESS);
                            final Message msg = mHandler.obtainMessage(
                                    MSG_HANDLE_FEEDBACK_RESULT,
                                    success ? 1 : 0, 0);
                            mHandler.sendMessage(msg);
                        }
                    });
            showProgressDialog();
        } else {
            ShowToast.makeText(this, R.drawable.emoji_sad, this.getResources().getString(R.string.feedback_message_empty)).show();
        }
    }

    private void handleFeedbackResult(boolean success) {
        dismissProgressDialog();
        if (success) {
            ShowToast.makeText(this, R.drawable.emoji_smile,
                    getString(R.string.feedback_success_prompt)).show();
            finish();
        } else {
            ShowToast.makeText(this, R.drawable.emoji_sad,
                    getString(R.string.feedback_submit_failure)).show();
        }
    }

    private void setListener() {
        mSubmitBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                uploadFeedbackMessage();
            }
        });
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = SimpleProgressDialog.newInstance();
            mProgressDialog.show(getSupportFragmentManager(),
                    "SimpleProgressDialog");
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
