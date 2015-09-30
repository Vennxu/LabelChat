package com.ekuater.labelchat.ui.fragment.labelstory;


import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Label on 2015/1/8.
 *
 * @author Xu wenxiang
 */
//自定义ImageButton，模拟ImageButton，并在其下方显示文字
//提供Button的部分接口
public class CustomImageButton extends LinearLayout {

    public CustomImageButton(Context context, int imageResId, int textResId) {
        super(context);

        mButtonImage = new ImageView(context);
        mButtonText = new TextView(context);

        setImageResource(imageResId);
        mButtonImage.setPadding(0, 0, 0, 0);

        setText(textResId);
        setTextColor(0xFFFFFFFF);
        mButtonText.setPadding(0, 10, 0, 0);

        //设置本布局的属性
        setClickable(true);  //可点击
        setFocusable(true);  //可聚焦
        setBackgroundResource(0);  //布局才用普通按钮的背景
        setOrientation(LinearLayout.HORIZONTAL);  //垂直布局
        setGravity(Gravity.CENTER);
        //首先添加Image，然后才添加Text
        //添加顺序将会影响布局效果
        addView(mButtonImage);
        addView(mButtonText);
    }

    // ----------------public method-----------------------------
  /*
   * setImageResource方法
   */
    public void setImageResource(int resId) {
        mButtonImage.setImageResource(resId);
    }

    /*
     * setText方法
     */
    public void setText(int resId) {
        mButtonText.setText(resId);
    }

    public void setText(CharSequence buttonText) {
        mButtonText.setText(buttonText);
    }

    /*
     * setTextColor方法
     */
    public void setTextColor(int color) {
        mButtonText.setTextColor(color);
    }

    /*
         * setTextSize方法
         */
    public void setTextSize(int size) {
        mButtonText.setTextSize(size);
    }



    // ----------------private attribute-----------------------------
    private ImageView mButtonImage = null;
    private TextView mButtonText = null;
}