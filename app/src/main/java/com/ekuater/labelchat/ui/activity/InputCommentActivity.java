package com.ekuater.labelchat.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ekuater.labelchat.R;

import com.ekuater.labelchat.ui.activity.base.BaseActivity;
import com.ekuater.labelchat.ui.fragment.labelstory.LabelStoryUtils;
import com.ekuater.labelchat.ui.widget.KeyboardStateView;
import com.ekuater.labelchat.ui.widget.emoji.EmojiEditText;
import com.ekuater.labelchat.ui.widget.emoji.EmojiKeyboard;
import com.ekuater.labelchat.ui.widget.emoji.EmojiSelector;
import com.ekuater.labelchat.ui.widget.emoji.ShowContentTextView;


/**
 * Created by Administrator on 2015/4/21.
 */
public class InputCommentActivity extends BaseActivity {

    public static final int MSG_KEYBOARD_STATE_CHANGED = 101;
    public static final String REPLY_NAME = "reply_name";
    public static final String DYNAMIC_TYPE = "type";
    private EmojiSelector mEmojiSelector;
    private ImageButton mFaceImageButton;
    private EmojiEditText mEmojiEditText;
    private FrameLayout mInputLinear;
    private ShowContentTextView mInputHint;
    private Button mSendComment;
    private boolean mIsFaceShow = false;
    private boolean isGroup = true;
    private InputMethodManager mInputMethodManager;
    private String replyName = null;
    private int mCount = 0;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_KEYBOARD_STATE_CHANGED:
                    handleKeyboardStateChanged(msg.arg1);
                    break;
            }
        }
    };

    private KeyboardStateView.OnKeyboardStateChangedListener mKeyboardStateChangedListener
            = new KeyboardStateView.OnKeyboardStateChangedListener() {
        @Override
        public void onKeyboardStateChanged(int state) {
            Message message = Message.obtain(handler, MSG_KEYBOARD_STATE_CHANGED, state, 0);
            handler.sendMessage(message);
        }
    };


    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (isGroup) {
                if (s.length() == 0) {
                    isGroup = true;
                    mSendComment.setEnabled(false);
                    if (isHideKeyboard){
                        clearContentEditFocus();
                    }
                } else {
                    mSendComment.setEnabled(true);
                }
            }else{
                if (s.length() == mCount){
                    mSendComment.setEnabled(false);
                }else{
                    mSendComment.setEnabled(true);
                }
            }
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.comment_ui_face_switch_btn:
                    if (mIsFaceShow) {
                        mInputMethodManager.showSoftInput(mEmojiEditText, 0);
                        showEmojiSelector(false);
                    } else {
                        mInputMethodManager.hideSoftInputFromWindow(mEmojiEditText.getWindowToken(), 0);
                        showEmojiSelector(true);
                    }
                    break;
                case R.id.input_comment:
                    finish();
                    break;

                case R.id.comment_ui_input_send_btn:
                    String content = mEmojiEditText.getText().toString();
                    String commentContent = isGroup ? content:content.substring(mCount,content.length());
                    if (!TextUtils.isEmpty(commentContent)) {
                        Intent intent = new Intent();
                        intent.putExtra(LabelStoryUtils.CONTENT, commentContent);
                        intent.putExtra(LabelStoryUtils.IS_GROUP, isGroup);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                    break;
                case R.id.comment_ui_input_hint:
//                mDetailHideClick.setVisibility(View.VISIBLE);
                    mEmojiEditText.requestFocus();
                    showSoftInput();
                    break;
            }
        }
    };

    private void showSoftInput(){
        mEmojiEditText.setVisibility(View.VISIBLE);
        mInputHint.setVisibility(View.GONE);
        mInputMethodManager.showSoftInput(mEmojiEditText, 0);
        showEmojiSelector(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_comment);
        mInputMethodManager = (InputMethodManager) this.getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        getArgments();
        initView();
        showSoftInput();
    }

    private void getArgments() {
        Intent intent = getIntent();
        replyName = intent.getStringExtra(REPLY_NAME);
        mCount = TextUtils.isEmpty(replyName) ? 0 : replyName.length();
        isGroup = TextUtils.isEmpty(replyName) ? true : false;
    }

    private void initView() {
        mEmojiSelector = (EmojiSelector) findViewById(R.id.chatting_ui_input_emoji_layout);
        mFaceImageButton = (ImageButton) findViewById(R.id.comment_ui_face_switch_btn);
        mSendComment = (Button) findViewById(R.id.comment_ui_input_send_btn);
        mEmojiEditText = (EmojiEditText) findViewById(R.id.comment_ui_input_edit);
        mInputLinear = (FrameLayout) findViewById(R.id.input_comment);
        mInputLinear.setOnClickListener(mOnClickListener);
        mEmojiEditText.addTextChangedListener(textWatcher);
        mInputHint = (ShowContentTextView) findViewById(R.id.comment_ui_input_hint);
        mInputHint.setOnClickListener(mOnClickListener);
        mSendComment.setOnClickListener(mOnClickListener);
        mFaceImageButton.setOnClickListener(mOnClickListener);
        mEmojiEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mInputMethodManager.showSoftInput(mEmojiEditText, 0);
                showEmojiSelector(false);
                return false;
            }
        });
        mEmojiEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (mEmojiEditText.getText().toString().length() < mCount && !isGroup) {
                        mEmojiEditText.setText("");
                        clearReply();
                    }
                }
                return false;
            }
        });
        mEmojiSelector.setOnEmojiClickedListener(new EmojiSelector.OnEmojiClickedListener() {
            @Override
            public void onEmojiClicked(String emoji) {
                EmojiKeyboard.input(mEmojiEditText, emoji);
            }

            @Override
            public void onBackspace() {
                EmojiKeyboard.backspace(mEmojiEditText);
            }
        });
        SpannableString ss;
        if (!TextUtils.isEmpty(replyName)) {
            String content = "@" + replyName + " ";
            mCount = content.length();
            ss = new SpannableString(content);
            ss.setSpan(new ForegroundColorSpan(R.color.colorLabelTextLight), 0,
                    mCount - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            mCount = 0;
            ss = null;
        }

        mEmojiEditText.setText(ss);
        mEmojiEditText.setSelection(mCount);
    }

    private void showEmojiSelector(boolean show) {
        mEmojiSelector.setVisibility(show ? View.VISIBLE : View.GONE);
        mFaceImageButton.setImageResource(show ? R.drawable.ic_input_keyboard_selector
                : R.drawable.ic_input_face_selector);
        mIsFaceShow = show;
    }

    private void clearReply() {
        isGroup = true;
        replyName = "";
        mSendComment.setEnabled(false);
        mEmojiEditText.setHint(getString(R.string.labelstroy_input_comment_hint));
    }

    @Override
    protected void initializeActionBar() {

    }
    private boolean isHideKeyboard = false;

    private void handleKeyboardStateChanged(int state) {
        switch (state) {
            case KeyboardStateView.KEYBOARD_STATE_HIDE:
                isHideKeyboard = true;
                clearContentEditFocus();
                break;
            case KeyboardStateView.KEYBOARD_STATE_SHOW:
                isHideKeyboard = false;
                break;
            default:
                break;
        }
    }

    private void clearContentEditFocus() {
        if (!mIsFaceShow) {
            mEmojiEditText.setVisibility(View.GONE);
            mInputHint.setVisibility(View.VISIBLE);
        }
        if(mEmojiEditText.getText().length() == 0) {
            mInputHint.setText(getString(R.string.labelstroy_input_comment_hint));
        }else{
            mInputHint.setText(mEmojiEditText.getText().toString());
        }
        hideSoftInput();
    }

    private void hideSoftInput() {
        mInputMethodManager.hideSoftInputFromWindow(mEmojiEditText.getWindowToken(), 0);
    }
}
