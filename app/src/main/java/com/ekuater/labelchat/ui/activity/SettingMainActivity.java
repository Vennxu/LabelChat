package com.ekuater.labelchat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.activity.base.TitleIconActivity;

public class SettingMainActivity extends TitleIconActivity implements OnClickListener,
		OnItemClickListener {
	private ListView menulist;
	private Button logout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_main);
		setTitle(R.string.set_main_setting);
		findViews();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Find the Views in the layout<br />
	 * <br />
	 * Auto-created on 2014-08-17 00:58:57 by Android Layout Finder
	 * (http://www.buzzingandroid.com/tools/android-layout-finder)
	 */
	private void findViews() {
		menulist = (ListView) findViewById(R.id.setting_main_list);
		logout = (Button) findViewById(R.id.setting_main_logout);

		menulist.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.setting_main_menu)));
		menulist.setOnItemClickListener(this);
		logout.setOnClickListener(this);
	}

	/**
	 * Handle button click events<br />
	 * <br />
	 * Auto-created on 2014-08-17 00:58:57 by Android Layout Finder
	 * (http://www.buzzingandroid.com/tools/android-layout-finder)
	 */
	@Override
	public void onClick(View v) {
		if (v == logout) {
			// Handle clicks for settingMainLogout
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent();
		switch (position) {
		case 0:
			intent.setClass(this, SettingScoreActivity.class);
			startActivity(intent);
			break;
		case 1:
			intent.setClass(this, SettingLabelActivity.class);
			startActivity(intent);
			break;
		case 2:
			intent.setClass(this, SettingChatActivity.class);
			startActivity(intent);
			break;
		case 3:
			intent.setClass(this, SettingPrivacyActivity.class);
			startActivity(intent);
			break;
		case 4:
			intent.setClass(this, SettingAboutActivity.class);
			startActivity(intent);
			break;
		default:
		}
	}

}
