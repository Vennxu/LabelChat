package com.ekuater.labelchat.ui.fragment.get;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;

/**
 * Created by Label on 2014/12/10.
 * @author Xu wenxiang
 */
public class GetDialogFragment extends DialogFragment{

    private TextView mChanges,mCancle,mTextMessage,mTitleMessage;
    private Button mKnowBtn;
    private ImageView mImage;
    private String mTitle,mMessage,mKnow;
    private int mRescouce;
    private Activity mActivity;
    private boolean mDismissCancel=true;

    public static GetDialogFragment newInstance(Activity activity ,String title,String messge,String know,int resouce) {
        GetDialogFragment instance = new GetDialogFragment();
        instance.setStyle(STYLE_NO_TITLE, 0);
        instance.mTitle = title;
        instance.mMessage=messge;
        instance.mKnow=know;
        instance.mRescouce=resouce;
        instance.mActivity=activity;
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }
    public interface GetOnclickListener {

        public void onChanges();

        public void onFaileds();

        public void onKnowPeople();
    }
    private GetOnclickListener mGetOnclickListener;
    private final View.OnClickListener mOnclickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {


            switch (v.getId()) {
                case R.id.btn_dismiss:
                    if (mGetOnclickListener != null) {
                        mGetOnclickListener.onFaileds();
                    }
                    break;
                case R.id.btn_changes:
                    if (mGetOnclickListener != null) {
                        mGetOnclickListener.onChanges();
                    }
                    break;
                case R.id.get_dialog_lookinfo:
                    if (mGetOnclickListener!=null){
                        mDismissCancel=false;
                        mGetOnclickListener.onKnowPeople();
                    }
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mDismissCancel){
            mActivity.finish();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.getfeild_dialog_layout,container,false);
        mChanges=(TextView)view.findViewById(R.id.btn_changes);
        mCancle=(TextView)view.findViewById(R.id.btn_dismiss);
        mTextMessage=(TextView)view.findViewById(R.id.get_dialog_message);
        mTitleMessage=(TextView)view.findViewById(R.id.get_dialog_tittlemessage);
        mImage=(ImageView)view.findViewById(R.id.get_dialog_image);
        mKnowBtn=(Button)view.findViewById(R.id.get_dialog_lookinfo);
        mKnowBtn.setOnClickListener(mOnclickListener);
        mCancle.setOnClickListener(mOnclickListener);
        mChanges.setOnClickListener(mOnclickListener);
        if (!TextUtils.isEmpty(mMessage)) {
            mTextMessage.setText(mMessage);
        }
        if (!TextUtils.isEmpty(mTitle)){
            mTitleMessage.setText(mTitle);
        }
        if (!TextUtils.isEmpty(mKnow)){
            mKnowBtn.setText(mKnow);
            mKnowBtn.setVisibility(View.VISIBLE);
            view.findViewById(R.id.get_dialog_titleDivider).setVisibility(View.INVISIBLE);
        }
        if (mRescouce!=0){
            mImage.setBackgroundResource(mRescouce);
        }
        return view;
    }


    public void setGetOnclickListener(GetOnclickListener getOnclickListener){
        this.mGetOnclickListener=getOnclickListener;
    }

}


