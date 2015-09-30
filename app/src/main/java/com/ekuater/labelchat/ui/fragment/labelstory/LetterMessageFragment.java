package com.ekuater.labelchat.ui.fragment.labelstory;

import android.app.ActionBar;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LetterMessage;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.widget.CircleImageView;


import java.util.ArrayList;


/**
 * Created by Label on 2015/3/12.
 */
public class LetterMessageFragment extends Fragment{

    public static final String LETTER_FLAGS = "letter_flags";
    public static final int SEND_LETTER_RESULT_CODE = 101;

    private EditText mLetterEidtText;
    private TextView mLetterCancel;
    private TextView mLetterOk;
    private CircleImageView mLetterTx;
    private TextView mLetterName;
    private TextView mTitle;
    private LinearLayout mLetterList;
    private SimpleProgressDialog mProgressDialog;

    private AvatarManager avatarManager;
    private PushMessageManager mPushManager;
    private LabelStoryManager mLabelStoryManager;

    private Activity activity;
    private ArrayList<LetterMessage> mLetterMessage;
    private SettingHelper mSettingHelper;
    private String letterMsg = null;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case SEND_LETTER_RESULT_CODE:
                    dismissProgressDialog();
                    onLetterHandler(msg);
                    break;

                default:
                    break;
            }
        }
    };
    private void onLetterHandler(Message message){
        switch (message.arg1){
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                LabelStoryUtils.insertSystemPush(mPushManager,mLetterMessage.get(0).getStranger(),letterMsg);
                startQueryMessage();
                mLetterEidtText.setText("");
                letterMsg = null;
                ShowToast.makeText(activity, R.drawable.emoji_smile, activity.
                        getResources().getString(R.string.send_letter_succese)).show();
                break;
            default:
                ShowToast.makeText(activity, R.drawable.emoji_cry, activity.
                        getResources().getString(R.string.send_letter_failed)).show();
                break;

        }
    }
    public final class LoadLetterMessageTask extends AsyncTask<Void, Void, ArrayList<LetterMessage>> {
        @Override
        protected ArrayList<LetterMessage> doInBackground(Void... params) {
            if (getFlags() == null){
                return null;
            }
            final SystemPush[] systemPushs = mPushManager.getPushMessagesByFlags(
                    getFlags());
            final ArrayList<LetterMessage> list = new ArrayList<LetterMessage>();
            if (systemPushs != null) {
                Log.d("sys",systemPushs.length+systemPushs.toString());
                for (SystemPush systemPush : systemPushs) {
                    if (systemPush!=null) {
                        LetterMessage message = LetterMessage.build(systemPush);
                        if (systemPush.getState() == SystemPush.STATE_UNPROCESSED) {
                            PushMessageManager.getInstance(activity).updatePushMessageProcessed(systemPush.getId());
                            message.setState(SystemPush.STATE_PROCESSED);
                        }
                        if (message != null) {
                            list.add(message);
                        }
                    }
                }
            }
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<LetterMessage> letterMessage) {
            super.onPostExecute(letterMessage);
            if (mLetterMessage != null){
                mLetterMessage.clear();
                mLetterMessage = null;
                destroyView();
                mLetterList.removeAllViews();
            }
            if (letterMessage != null && letterMessage.size() > 0) {
                mLetterMessage = letterMessage;
                String title = "";
                Stranger stranger = null;
                if (letterMessage.get(0) != null && letterMessage.get(0).getStranger() != null) {

                    stranger = letterMessage.get(0).getStranger();
                    title = MiscUtils.getUserRemarkName(getActivity(), stranger.getUserId());
                }
                String name = title != null && title.length() > 0 ? title : stranger != null ? stranger.getNickname() : "";
                mLetterName.setText(name);
                mTitle.setText(name);
                MiscUtils.showAvatarThumb(avatarManager, letterMessage.get(0).getStranger().getAvatarThumb(), mLetterTx, R.drawable.contact_single);
                mLetterList.setVisibility(View.VISIBLE);
                addChildView();
            }
        }
    }

    private void startQueryMessage() {
        new LoadLetterMessageTask().executeOnExecutor(
                LoadLetterMessageTask.THREAD_POOL_EXECUTOR, (Void) null);
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null){
            actionBar.hide();
        }
        avatarManager = AvatarManager.getInstance(activity);
        mPushManager = PushMessageManager.getInstance(activity);
        mLabelStoryManager = LabelStoryManager.getInstance(activity);
        mSettingHelper = SettingHelper.getInstance(activity);

    }

    private String getFlags() {
        Bundle bundle = getArguments();
        return bundle == null ? null : bundle.getString(LETTER_FLAGS);
    }

    private void addChildView() {
        for (int i = 0; i < mLetterMessage.size(); i++) {
            LetterMessage letterMessage = mLetterMessage.get(i);
            TextView textView = new TextView(activity);
            if(letterMessage.getTag() == 1)
                textView.setTextColor(getResources().getColor(R.color.letter_msg_me));
            else{
                textView.setTextColor(getResources().getColor(R.color.letter_msg_other));
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0,20,0,0);
            textView.setLayoutParams(params);
            textView.setText(letterMessage.getMessage());
            textView.setTextSize(18);
            mLetterList.addView(textView);
        }
    }
    private void destroyView(){
        for (int i = 0;i < mLetterList.getChildCount();i++){
            mLetterList.removeView(mLetterList.getChildAt(i));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_letter_message, container, false);
        mLetterEidtText = (EditText) view.findViewById(R.id.letter_message_edit);
        mLetterCancel = (TextView) view.findViewById(R.id.letter_message_cancel);
        mLetterOk = (TextView) view.findViewById(R.id.letter_message_ok);
        mLetterTx = (CircleImageView) view.findViewById(R.id.letter_message_tx);
        mLetterName = (TextView) view.findViewById(R.id.letter_message_name);
        mLetterList = (LinearLayout) view.findViewById(R.id.letter_message_list);
        mTitle = (TextView) view.findViewById(R.id.title);
//        mLetterList.setAdapter(mLetterAdapter);
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.softInputMode |= WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
        mLetterTx.setOnClickListener(onClickListener);
        mLetterOk.setOnClickListener(onClickListener);
        mLetterCancel.setOnClickListener(onClickListener);
        view.findViewById(R.id.icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        startQueryMessage();
        return view;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.letter_message_ok:
                     letterMsg = mLetterEidtText.getText().toString();
                    if (!TextUtils.isEmpty(letterMsg)){
                        showProgressDialog();
                        sendLetter(getFlags(),letterMsg);
                    }

                    break;
                case R.id.letter_message_cancel:
                    getActivity().finish();
                    break;

                case R.id.letter_message_tx:
                    UILauncher.launchStrangerDetailUI(activity, mLetterMessage.get(0).getStranger());
                    break;
                default:
                    break;
            }
        }
    };

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = SimpleProgressDialog.newInstance();
            mProgressDialog.show(getFragmentManager(), "SimpleProgressDialog");
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void sendLetter(String strangerUserId, String message) {

        LabelStoryManager.LabelStoryLetterQueryObserver observer = new LabelStoryManager.LabelStoryLetterQueryObserver() {
            @Override
            public void onQueryResult(int result, boolean remaining) {
                Message message = Message.obtain(handler, SEND_LETTER_RESULT_CODE, result);
                handler.sendMessage(message);
            }
        };
        mLabelStoryManager.letterLabelStory(null,strangerUserId, message, observer);
    }

}
