package com.ekuater.labelchat.ui.activity.base;

import android.app.ActionBar;
import android.view.MenuItem;

import com.ekuater.labelchat.R;

/**
 * @author LinYong
 */
public abstract class BackIconActivity extends BaseActivity {

    @Override
    protected void initializeActionBar() {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayUseLogoEnabled(true);
            /*actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);*/
            actionBar.setIcon(R.drawable.lc_ic_ab_back);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = true;

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                handled = false;
                break;
        }

        return handled || super.onOptionsItemSelected(item);
    }
}
