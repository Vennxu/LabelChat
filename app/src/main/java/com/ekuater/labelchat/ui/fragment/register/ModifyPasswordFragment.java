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

/**
 * @author LinYong
 */
public class ModifyPasswordFragment extends Fragment implements View.OnClickListener {

    private static final String PROGRESS_DIALOG_TAG = "ProgressDialog";

    private SimpleProgressDialog mLoginDialog;
    private EditText mOldPasswordEdit;
    private EditText mNewPasswordEdit;
    private EditText mNewPasswordConfirmEdit;

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
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.modify_password);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_modify_password, container, false);
        view.findViewById(R.id.submit).setOnClickListener(this);
        mOldPasswordEdit = (EditText) view.findViewById(R.id.old_password);
        mNewPasswordEdit = (EditText) view.findViewById(R.id.new_password);
        mNewPasswordConfirmEdit = (EditText) view.findViewById(R.id.new_password_confirm);

        mOldPasswordEdit.setOnFocusChangeListener(mEditFocusChangeListener);
        mNewPasswordEdit.setOnFocusChangeListener(mEditFocusChangeListener);
        mNewPasswordConfirmEdit.setOnFocusChangeListener(mEditFocusChangeListener);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit:
                onSubmitClick();
                break;
            default:
                break;
        }
    }

    private String getOldPassword() {
        return mOldPasswordEdit.getText().toString().trim();
    }

    private String getNewPassword() {
        return mNewPasswordEdit.getText().toString().trim();
    }

    private String getNewPasswordConfirm() {
        return mNewPasswordConfirmEdit.getText().toString().trim();
    }

    private void onSubmitClick() {
        final Activity activity = getActivity();
        final String oldPassword = getOldPassword();
        final String newPassword = getNewPassword();
        final String newPasswordConfirm = getNewPasswordConfirm();

        if (TextUtils.isEmpty(oldPassword)) {
            ShowToast.makeText(activity, R.drawable.emoji_smile, activity.
                    getResources().getString(R.string.input_old_password_hit)).show();
            mOldPasswordEdit.requestFocus();
        } else if (TextUtils.isEmpty(newPassword)) {

            ShowToast.makeText(activity, R.drawable.emoji_smile, activity.
                    getResources().getString(R.string.enter_new_password_prompt)).show();
            mNewPasswordEdit.requestFocus();
        } else if (TextUtils.isEmpty(newPasswordConfirm)) {
            ShowToast.makeText(activity, R.drawable.emoji_smile, activity.
                    getResources().getString(R.string.enter_new_password_confirm_prompt)).show();
            mNewPasswordConfirmEdit.requestFocus();
        } else if (!newPassword.equals(newPasswordConfirm)) {
            ShowToast.makeText(activity, R.drawable.emoji_sad, activity.
                    getResources().getString(R.string.inconformity_input_password)).show();
            mNewPasswordConfirmEdit.requestFocus();
        } else {
            modifyPassword(oldPassword, newPassword);
        }
    }

    private void modifyPassword(String oldPassword, String newPassword) {
        final Activity activity = getActivity();
        final AccountManager accountManager = AccountManager.getInstance(activity);
        final FunctionCallListener listener = new FunctionCallListener() {
            @Override
            public void onCallResult(int result, int errorCode, String errorDesc) {
                if (errorCode == CommandErrorCode.REQUEST_SUCCESS) {
                    showToastInUiThread(R.drawable.emoji_smile, activity.getResources().getString(R.string.modify_password_success));
                } else {
                    showToastInUiThread(R.drawable.emoji_cry, activity.getResources().getString(R.string.modify_password_failed));

                }
                getActivity().finish();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissLoginProgress();
                    }
                });
            }
        };

        showProgressDialog();
        accountManager.modifyPasswordNormally(oldPassword, newPassword, listener);
    }

    private void runOnUiThread(Runnable action) {
        getActivity().runOnUiThread(action);
    }

    private void showToastInUiThread(final int resId, final String context) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShowToast.makeText(getActivity(), resId, context).show();
            }
        });
    }

    private void showProgressDialog() {
        dismissLoginProgress();
        mLoginDialog = SimpleProgressDialog.newInstance();
        mLoginDialog.show(getFragmentManager(), PROGRESS_DIALOG_TAG);
    }

    private void dismissLoginProgress() {
        if (mLoginDialog != null) {
            mLoginDialog.dismiss();
            mLoginDialog = null;
        }
    }
}
