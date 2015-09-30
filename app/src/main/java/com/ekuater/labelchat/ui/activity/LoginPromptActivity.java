package com.ekuater.labelchat.ui.activity;

import android.os.Bundle;
import android.view.View;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.TitleIconActivity;

/**
 * Created by Leo on 2015/4/24.
 *
 * @author LinYong
 */
public class LoginPromptActivity extends TitleIconActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_prompt_dialog);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_login).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                UILauncher.launchSignInGuideUI(this);
                break;
            default:
                break;
        }
        finish();
    }
}
