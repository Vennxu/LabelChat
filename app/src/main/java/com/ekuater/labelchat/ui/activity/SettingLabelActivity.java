package com.ekuater.labelchat.ui.activity;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.delegate.SettingManager;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.util.sharepreferencesUtil;

public class SettingLabelActivity extends BackIconActivity implements OnCheckedChangeListener{
	ToggleButton tb1,tb2,tb3,tb4 ;
	private SettingManager smanag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_lbset);
		setTitle(R.string.set_main_lb);
		tb1 = (ToggleButton)findViewById(R.id.set_lb_switch01);
		tb2 = (ToggleButton)findViewById(R.id.set_lb_switch02);
		tb3 = (ToggleButton)findViewById(R.id.set_lb_switch03);
		tb4 = (ToggleButton)findViewById(R.id.set_lb_switch04);
		
		initSettingData();
		tb1.setOnCheckedChangeListener(this);
		tb2.setOnCheckedChangeListener(this);
		tb3.setOnCheckedChangeListener(this);
		tb4.setOnCheckedChangeListener(this);
		
		smanag = SettingManager.getInstance(this);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		String setValue = isChecked==true?"1":"0";
		String key = "";
		if(buttonView.getId() == R.id.set_lb_switch01){
			sharepreferencesUtil.setValue(this, "stranger_visible", setValue);
			key = "strangerVisible";
		}
		if(buttonView.getId() == R.id.set_lb_switch02){
			sharepreferencesUtil.setValue(this, "dynamic_show", setValue);
			key = "dynamicShow";
		}
		if(buttonView.getId() == R.id.set_lb_switch03){
			sharepreferencesUtil.setValue(this, "up_hint", setValue);
			key = "upHint";
		}
		if(buttonView.getId() == R.id.set_lb_switch04){
			sharepreferencesUtil.setValue(this, "hot_recommend", setValue);
			key = "hotRecommend";
		}
		smanag.updateLabelSetInfo(key, setValue);
	}
	/*
	    *   陌生人可见           stranger_visible
		*	更新后动态显示       dynamic_show
		*	设置标签上限提示     up_hint
		*	向我推荐热门标签     hot_recommend 
	    *   
	 */
	public void initSettingData(){
		String stranger_visible = (String) sharepreferencesUtil.getValue(this, "stranger_visible", "1"); 
		String dynamic_show = (String) sharepreferencesUtil.getValue(this, "dynamic_show", "1");
		String up_hint = (String) sharepreferencesUtil.getValue(this, "up_hint", "1");
		String hot_recommend  = (String) sharepreferencesUtil.getValue(this, "hot_recommend", "1");
		
		tb1.setChecked("1".equals(stranger_visible)?true:false);
		tb2.setChecked("1".equals(dynamic_show)?true:false);
		tb3.setChecked("1".equals(up_hint)?true:false);
		tb4.setChecked("1".equals(hot_recommend)?true:false);
		
	}
}
