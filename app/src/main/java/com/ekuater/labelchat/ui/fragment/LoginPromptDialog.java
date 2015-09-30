package com.ekuater.labelchat.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.UILauncher;

/**
 * @author LinYong
 */
public class LoginPromptDialog extends DialogFragment
        implements View.OnClickListener {

    public interface OnLoginOnclickListener {
        public void onLunchLogin();
    }

    private OnLoginOnclickListener mOnLoginOnclickListener = null;

    public static LoginPromptDialog newInstance() {
        LoginPromptDialog instance = new LoginPromptDialog();
        instance.setStyle(STYLE_NO_TITLE, 0);
        return instance;
    }

    public static LoginPromptDialog newInstance(OnLoginOnclickListener onLoginOnclickListener) {
        LoginPromptDialog instance = new LoginPromptDialog();
        instance.mOnLoginOnclickListener = onLoginOnclickListener;
        instance.setStyle(STYLE_NO_TITLE, 0);
        return instance;
    }

    public LoginPromptDialog() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_prompt_dialog, container, false);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_login).setOnClickListener(this);
        return view;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                    dismiss();
                break;
            case R.id.btn_login:
                if (mOnLoginOnclickListener != null) {
                    mOnLoginOnclickListener.onLunchLogin();
                }
                dismiss();
                UILauncher.launchSignInGuideUI(getActivity());
                break;
            default:
                break;
        }
    }
}
