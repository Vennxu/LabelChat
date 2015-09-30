package com.ekuater.labelchat.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;

public class SettingAboutActivity extends BackIconActivity {
	WebView show;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_setting_about);
		setTitle(R.string.set_main_about);
		
		show = (WebView)findViewById(R.id.set_about_webView1);
		show.loadUrl("www.163.com");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		return super.onCreateOptionsMenu(menu);
		
	}
}
