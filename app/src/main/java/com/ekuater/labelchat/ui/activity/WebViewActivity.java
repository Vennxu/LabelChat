package com.ekuater.labelchat.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;

/**
 * @author LinYong
 */
public class WebViewActivity extends BackIconActivity implements View.OnClickListener {

    public static final String EXTRA_URL = "extra_url";

    private ProgressBar mProgressBar;
    private final WebViewClient mClient = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            mProgressBar.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        findViewById(R.id.close).setOnClickListener(this);

        String url = getIntent().getStringExtra(EXTRA_URL);
        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.setWebViewClient(mClient);
        webView.loadUrl(url);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close:
                finish();
                break;
            default:
                break;
        }
    }
}
