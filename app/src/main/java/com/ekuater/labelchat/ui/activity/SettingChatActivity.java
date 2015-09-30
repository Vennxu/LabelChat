package com.ekuater.labelchat.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.delegate.SettingManager;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.util.sharepreferencesUtil;

public class SettingChatActivity extends BackIconActivity implements OnCheckedChangeListener{
	ToggleButton tb1,tb6;
	TextView tx3,tx4;
	Resources res;
	String[] mItems = null;
	String[] mItemValue = null;

	AlertDialog.Builder builder;
	private SettingManager smanag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_chat);
		setTitle(R.string.set_main_chat);
		builder = new AlertDialog.Builder(SettingChatActivity.this);
		tb1 = (ToggleButton)findViewById(R.id.set_chat_switch01);
		tb6 = (ToggleButton)findViewById(R.id.set_chat_switch06); 
		tx3 = (TextView)findViewById(R.id.chat_set_textView3);
		tx4 = (TextView)findViewById(R.id.chat_text_view3);
		
		res = getResources();
		mItems = res.getStringArray(R.array.setting_chat_size_key);
		mItemValue = res.getStringArray(R.array.setting_chat_size_value);
		
		initSettingData();
		tb1.setOnCheckedChangeListener(this);
		tb6.setOnCheckedChangeListener(this);
		smanag = SettingManager.getInstance(this);
		}
	
	@Override	
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		String setValue = isChecked==true?"1":"0";
		String key = "";
		if(buttonView.getId() == R.id.set_chat_switch01){
			sharepreferencesUtil.setValue(this, "new_inform_sound", setValue);
			key = "newInformSound";
		}
		if(buttonView.getId() == R.id.set_chat_switch06){
			sharepreferencesUtil.setValue(this, "new_inform_shake", setValue);
			key = "newInformShake";
		}
		smanag.updateChatSetInfo(key, setValue);
		
	}
	/*
	    *   新消息通知 声音           new_inform
	    *   新消息通知 震动           new_inform
		*	聊天背景              chat_background
		*	字体大小              typeface_size
		*	清空聊天记录          empty_chatrecord
	    *   
	 */
	public void initSettingData(){
		String new_inform_sound = (String) sharepreferencesUtil.getValue(this, "new_inform_sound", "1"); 
		String new_inform_shake = (String) sharepreferencesUtil.getValue(this, "new_inform_shake", "1");
		String typeface_size = (String) sharepreferencesUtil.getValue(this, "typeface_size", "5");
		
		tb1.setChecked("1".equals(new_inform_sound)?true:false);
		tb6.setChecked("1".equals(new_inform_shake)?true:false);
		tx4.setText(mItems[sharepreferencesUtil.getIndex( mItemValue, typeface_size) ]);
		
		tx3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
        		 builder.setTitle("字体大小");
        		 builder.setInverseBackgroundForced(true);
                 builder.setItems(mItems, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         //点击后弹出窗口选择了对应的字体大小
                          tx4.setText(mItems[which]);
                          sharepreferencesUtil.setValue(SettingChatActivity.this, "typeface_size", ((String)mItemValue[which]));
                          smanag.updateChatSetInfo("typefaceSize", mItemValue[which]);
                     }
                 });
         		builder.create().show();
            }
        });
	}
}
