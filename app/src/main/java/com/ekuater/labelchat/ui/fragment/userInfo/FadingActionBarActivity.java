package com.ekuater.labelchat.ui.fragment.userInfo;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.ui.fragment.userInfo.FadingActionBarHelper;

/**
 * Created by Administrator on 2015/3/17.
 *
 * @author FanChong
 */
public class FadingActionBarActivity extends BackIconActivity {
    public static final String EXTRA_SHOW_FRAGMENT = ":android:show_fragment";
    public static final String EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":android:show_fragment_args";

    private String mFragmentClass;
    private Bundle mFragmentArguments;
    private FadingActionBarHelper mFadingActionBarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
      ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.my_actionbar);
        parseFragmentAndArguments();
        showFragment();
        mFadingActionBarHelper = new FadingActionBarHelper(getActionBar(),
                getResources().getDrawable(R.color.actionBarStyle));
    }

    private void parseFragmentAndArguments() {
        final Intent intent = getIntent();
        mFragmentClass = intent.getStringExtra(EXTRA_SHOW_FRAGMENT);
        mFragmentArguments = intent.getBundleExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS);
    }

    private void showFragment() {
        Fragment fragment = Fragment.instantiate(this, mFragmentClass, mFragmentArguments);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, mFragmentClass);
        transaction.commitAllowingStateLoss();
    }

    public FadingActionBarHelper getFadingActionBarHelper() {
        return mFadingActionBarHelper;
    }
}
