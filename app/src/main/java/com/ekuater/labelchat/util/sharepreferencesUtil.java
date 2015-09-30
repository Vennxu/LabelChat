
package com.ekuater.labelchat.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * @author biyubin
 * @since 2014-09-25
 */
public class sharepreferencesUtil {

    /**
     * 设置sharePreference文件中的字段的值
     * 
     * @param ctx  上下文
     * @param key 字段
     * @param value 值
     * @return
     */
    @SuppressWarnings("deprecation")
	@SuppressLint("WorldReadableFiles")
	public static boolean setValue(Context ctx,String key,Object value) {
    	boolean status = false;
    	SharedPreferences spf = null;
    	spf = ctx.getSharedPreferences("LB_SETTING_SP", Context.MODE_WORLD_READABLE );
    	String type = value.getClass().getSimpleName();//获取数据类型
    	Editor editor = spf.edit();
    	if(spf != null){
    		if("String".equals(type)){
    			editor.putString(key, (String)value);
    		}
    		if("Integer".equals(type)){
    			editor.putInt(key, (Integer)value);
    		}
    		if("Boolean".equals(type)){
    			editor.putBoolean(key, (Boolean)value);
    		}
    		if("Long".equals(type)){
    			editor.putLong(key, (Long)value);
    		}
    		if("Float".equals(type)){
    			editor.putFloat(key, (Float)value);
    		}
    		status = editor.commit();
    	}
    	return status;
    	
    }
    
    
    /**
     * 获取sharePreference文件中的字段的值
     * 
     * @param ctx  上下文
     * @param key 字段
     * @param value 默认值，没有获取到值就返回默认的value
     * @return
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("WorldReadableFiles")
	public static Object getValue(Context ctx,String key,Object value) {
    	SharedPreferences spf = null;
    	spf = ctx.getSharedPreferences("LB_SETTING_SP", Context.MODE_WORLD_READABLE );
    	String type = value.getClass().getSimpleName();//获取数据类型
    	if(spf != null){
    		if("String".equals(type)){
    			return spf.getString(key, (String)value);
    		}
    		if("Integer".equals(type)){
    			return spf.getInt(key, (Integer)value);
    		}
    		if("Boolean".equals(type)){
    			return spf.getBoolean(key, (Boolean)value);
    		}
    		if("Long".equals(type)){
    			return spf.getLong(key, (Long)value);
    		}
    		if("Float".equals(type)){
    			return spf.getFloat(key, (Float)value);
    		}
    	}
    	return null;
    }
    
    //获取数组中元素对应的下标
    public static int getIndex(String[] array, String value){
    	int cow = -1;
    	for(int i=0; i<array.length; i++ ){
    		if(value.equals(array[i]))
    			cow = i; 			
    	}
    	return cow;
    }
}
