package com.ekuater.labelchat.ui.activity;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.delegate.SettingManager;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.util.sharepreferencesUtil;

public class SettingPrivacyActivity extends BackIconActivity implements OnCheckedChangeListener {
	ToggleButton tb1,tb2,tb3,tb4,tb5,tb6 ;
	private SettingManager smanag;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_privacy);
		setTitle(R.string.set_main_privacy);
		tb1 = (ToggleButton)findViewById(R.id.set_priv_switch01);
		tb2 = (ToggleButton)findViewById(R.id.set_priv_switch02);
		tb3 = (ToggleButton)findViewById(R.id.set_priv_switch03);
		tb4 = (ToggleButton)findViewById(R.id.set_priv_switch04);
		tb5 = (ToggleButton)findViewById(R.id.set_priv_switch05);
		tb6 = (ToggleButton)findViewById(R.id.set_priv_switch06); 
		
		initSettingData();
		tb1.setOnCheckedChangeListener(this);
		tb2.setOnCheckedChangeListener(this);
		tb3.setOnCheckedChangeListener(this);
		tb4.setOnCheckedChangeListener(this);
		tb5.setOnCheckedChangeListener(this);
		tb6.setOnCheckedChangeListener(this);
		smanag = SettingManager.getInstance(this);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		String setValue = isChecked==true?"1":"0";
		String key = "";
		if(buttonView.getId() == R.id.set_priv_switch01){
			sharepreferencesUtil.setValue(this, "default_verify", setValue);
			key = "defaultVerify";
		}
		if(buttonView.getId() == R.id.set_priv_switch02){
			sharepreferencesUtil.setValue(this, "mobile_seek", setValue);
			key = "mobileSeek";
		}
		if(buttonView.getId() == R.id.set_priv_switch03){
			sharepreferencesUtil.setValue(this, "unfamiliar_exact_search", setValue);
			key = "unfamiliarExactSearch";
		}
		if(buttonView.getId() == R.id.set_priv_switch04){
			sharepreferencesUtil.setValue(this, "shield_bubbling", setValue);
			key = "shieldBubbling";
		}
		if(buttonView.getId() == R.id.set_priv_switch05){
			sharepreferencesUtil.setValue(this, "nearby_search", setValue);
			key = "nearbyExactSearch";
		}
		if(buttonView.getId() == R.id.set_priv_switch06){
			sharepreferencesUtil.setValue(this, "recommed_stranger", setValue);
			key = "recommedStranger";
		}
		smanag.updatePrivacyInfo(key,setValue);
	}
	/*
	    *  加我为好友验证默认是           default_verify
		*	通过手机号搜索到我             mobile_seek
	    *   不被陌生人精确查找             unfamiliar_exact_search
	    *   屏蔽陌生人冒泡                 shield_bubbling
	    *   不被附近的陌生人搜索到         nearby_search
	    *   不被推荐给陌生人               recommed_stranger
	    *   
	 */
	public void initSettingData(){
		String default_verify = (String) sharepreferencesUtil.getValue(this, "default_verify", "1"); 
		String mobile_seek = (String) sharepreferencesUtil.getValue(this, "mobile_seek", "1");
		String unfamiliar_exact_search = (String) sharepreferencesUtil.getValue(this, "unfamiliar_exact_search", "0");
		String shield_bubbling  = (String) sharepreferencesUtil.getValue(this, "shield_bubbling", "1");
		String nearby_search  = (String) sharepreferencesUtil.getValue(this, "nearby_search", "0");
		String recommed_stranger  = (String) sharepreferencesUtil.getValue(this, "recommed_stranger", "0");
		
		tb1.setChecked("1".equals(default_verify)?true:false);
		tb2.setChecked("1".equals(mobile_seek)?true:false);
		tb3.setChecked("1".equals(unfamiliar_exact_search)?true:false);
		tb4.setChecked("1".equals(shield_bubbling)?true:false);
		tb5.setChecked("1".equals(nearby_search)?true:false);
		tb6.setChecked("1".equals(recommed_stranger)?true:false);
		
	}
	 
}
