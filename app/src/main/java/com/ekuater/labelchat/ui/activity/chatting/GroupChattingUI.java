package com.ekuater.labelchat.ui.activity.chatting;


import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.ekuater.labelchat.EnvConfig;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.widget.emoji.EmojiEditText;
import com.ekuater.labelchat.ui.widget.emoji.EmojiSelector;
import com.ekuater.labelchat.util.FileUtils;
import com.ekuater.labelchat.util.L;
import com.ekuater.labelchat.util.UniqueFileName;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by Label on 2014/12/5.
 */
public class GroupChattingUI extends BackIconActivity {
    public static final String GROUP_FRIEND = "group_friend";
    public static final String GROUP_LABEL_NAME = "group_label_NAME";
    private static final String TAG = "ChattingUI";
    private static final int REQUEST_SELECT_IMAGE = 102;

    private ChatMsgListView mMsgListView;
    private ImageButton mFaceSwitchBtn;
    private Button mSendChatMsgBtn;
    private ImageButton mInputAttachBtn;
    private EmojiEditText mChatMsgEdit;
    private EmojiSelector mEmojiSelector;
    private ImageButton mRecordSwitchButton;
    private View mTextInputArea;
    private RecordButton mRecordButton;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private InputMethodManager mInputMethodManager;

    private boolean mIsFaceShow = false;
    private boolean mInRecordInput = false;
    private ImageFactory mImageFactory;

    private String mGroupLabelName;
    private ArrayList<Stranger> mStrangerList;

    private void parseArguments() {
        Intent intent = this.getIntent();
        if (intent != null) {
            mStrangerList = intent.getParcelableArrayListExtra(GROUP_FRIEND);
            mGroupLabelName = intent.getStringExtra(GROUP_LABEL_NAME);
        }
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mGroupLabelName+"-"+ SettingHelper.getInstance(this).getAccountNickname());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_ui);


        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //TODO
        mImageFactory = new ImageFactory(this, null);
        parseArguments();
        ActionBar actionBar = this.getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mGroupLabelName);
        }
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                onSelectPictureResult(resultCode, data);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_information_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = true;

        switch (item.getItemId()) {
            case R.id.group_information_menu:
//                UILauncher.launchGroupInformationUI(this,mStrangerList,mGroupLabelName);
                break;
            default:
                handled = false;
                break;
        }
        return handled || super.onOptionsItemSelected(item);
    }

    private void initView() {
        mWindowLayoutParams = getWindow().getAttributes();
        mMsgListView = (ChatMsgListView) findViewById(R.id.chatting_ui_msg_list_view);
        mFaceSwitchBtn = (ImageButton) findViewById(R.id.chatting_ui_face_switch_btn);
        mSendChatMsgBtn = (Button) findViewById(R.id.chatting_ui_input_send_btn);
        mChatMsgEdit = (EmojiEditText) findViewById(R.id.chatting_ui_input_edit);
        mEmojiSelector = (EmojiSelector) findViewById(R.id.chatting_ui_input_emoji_layout);
        mTextInputArea = findViewById(R.id.text_input_area);
        mRecordButton = (RecordButton) findViewById(R.id.start_record);
        mRecordSwitchButton = (ImageButton) findViewById(R.id.chatting_ui_record_btn);
        mInputAttachBtn = (ImageButton) findViewById(R.id.chatting_ui_input_attach_btn);


        mChatMsgEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean sendVisible = !TextUtils.isEmpty(s) && !mInRecordInput;
                updateSendChatMsgBtnVisibility(sendVisible);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mRecordSwitchButton.setOnClickListener(mOnCLickListener);
        mFaceSwitchBtn.setOnClickListener(mOnCLickListener);
        mSendChatMsgBtn.setOnClickListener(mOnCLickListener);
        mRecordButton.setRecordListener(new RecordButton.IRecordListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onCanceled(int result) {
                ShowToast.makeText(GroupChattingUI.this, R.drawable.emoji_sad, getString(R.string.cancel_record_prompt_show)).show();
            }

            @Override
            public void onFinished(String recordFileName, long recordTime) {
                L.v(TAG, "IRecordListener::onFinished()"
                        + ",recordFileName=" + recordFileName
                        + ",recordTime" + recordTime);
                ChatMessage chatMessage = buildVoiceChatMessage(recordFileName, recordTime);
            }

            @Override
            public void onFailure(int result) {

            }
        });
        mInputAttachBtn.setOnClickListener(mOnCLickListener);

    }

    private void updateSendChatMsgBtnVisibility(boolean visible) {
        mSendChatMsgBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
        mInputAttachBtn.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    private View.OnClickListener mOnCLickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.chatting_ui_record_btn:
                    onRecordSwitchBtnClicked();
                    break;
                case R.id.chatting_ui_face_switch_btn:
                    onFaceSwitchBtnClicked();
                    break;
                case R.id.chatting_ui_input_send_btn:
                    onSendMsgChatBtnClicked();
                    break;
                case R.id.chatting_ui_input_attach_btn:
                    onInputAttachBtnClicked();
                    break;
            }
        }
    };

    private void onRecordSwitchBtnClicked() {
        mInRecordInput = !mInRecordInput;
        mTextInputArea.setVisibility(mInRecordInput ? View.GONE : View.VISIBLE);
        mRecordButton.setVisibility(mInRecordInput ? View.VISIBLE : View.GONE);
        updateSendChatMsgBtnVisibility(!mInRecordInput
                && !TextUtils.isEmpty(mChatMsgEdit.getText()));
        mRecordSwitchButton.setImageResource(mInRecordInput ? R.drawable.ic_keyboard_selector
                : R.drawable.ic_record_selector);
        if (mInRecordInput) {
            mInputMethodManager.hideSoftInputFromWindow(mChatMsgEdit.getWindowToken(), 0);
            showEmojiSelector(false);
        }
    }

    private void onFaceSwitchBtnClicked() {
        if (isFaceKeyboardShow()) {
            mInputMethodManager.showSoftInput(mChatMsgEdit, 0);
            showEmojiSelector(false);
        } else {
            mInputMethodManager.hideSoftInputFromWindow(mChatMsgEdit.getWindowToken(), 0);
            showEmojiSelector(true);
        }
    }

    private boolean isFaceKeyboardShow() {
        return mIsFaceShow;
    }

    private void showEmojiSelector(boolean show) {
        mEmojiSelector.setVisibility(show ? View.VISIBLE : View.GONE);
        mFaceSwitchBtn.setImageResource(show ? R.drawable.ic_input_keyboard_selector
                : R.drawable.ic_input_face_selector);
        mIsFaceShow = show;
    }

    private void onSendMsgChatBtnClicked() {
        String chatText = mChatMsgEdit.getText().toString();
        if (chatText != null && chatText.length() > 0) {
            ChatMessage chatMessage = buildTextChatMessage(chatText);
            mChatMsgEdit.setText("");
        }
    }

    private void onInputAttachBtnClicked() {
        UILauncher.launchMultiSelectImageUI(this, REQUEST_SELECT_IMAGE);
    }


    private void onSelectPictureResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            String[] imagePaths = data.getStringArrayExtra("file_path");
            boolean isTemp = data.getBooleanExtra("isTemp", false);

            if (imagePaths != null && imagePaths.length > 0) {
                new PictureSelectTask(imagePaths, isTemp).executeOnExecutor(
                        PictureSelectTask.THREAD_POOL_EXECUTOR,
                        (String) null);
            }
        }
    }

    private ChatMessage buildChatMessage() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setDirection(ChatMessage.DIRECTION_SEND);
        chatMessage.setState(ChatMessage.STATE_SENDING);
        return chatMessage;
    }

    private ChatMessage buildTextChatMessage(String chatText) {
        ChatMessage chatMessage = buildChatMessage();
        chatMessage.setType(ChatMessage.TYPE_TEXT);
        chatMessage.setContent(chatText);
        return chatMessage;
    }

    private ChatMessage buildVoiceChatMessage(String fileName, long time) {
        ChatMessage chatMessage = buildChatMessage();
        chatMessage.setType(ChatMessage.TYPE_VOICE);
        chatMessage.setContent(fileName);
        chatMessage.setPreview(String.valueOf(time));
        return chatMessage;
    }

    private ChatMessage buildImageChatMessage(String fileName, String previewFileName) {
        ChatMessage chatMessage = buildChatMessage();
        chatMessage.setType(ChatMessage.TYPE_IMAGE);
        chatMessage.setContent(fileName);
        chatMessage.setPreview(previewFileName);
        return chatMessage;
    }

    private class PictureSelectTask extends AsyncTask<String, String, Void> {
        private String[] mImagePaths;
        private boolean mIsTemp;

        public PictureSelectTask(String[] imagePath, boolean isTemp) {
            super();
            this.mImagePaths = imagePath;
            this.mIsTemp = isTemp;
        }

        @Override
        protected Void doInBackground(String... params) {
            for (String path : mImagePaths) {
                if (TextUtils.isEmpty(path)) {
                    continue;
                }

                File src = new File(path);
                if (!src.exists() || !src.isFile()) {
                    continue;
                }

                File dest = getImageFile();
                FileUtils.copyFile(src, dest);
                publishProgress(dest.getName(), mImageFactory.generateThumbnailFile(
                        dest.getPath()));

                if (mIsTemp) {
                    if (src.delete()) {
                        L.v(TAG, "PicSelectTask.doInBackground(), delete temp file:" + path);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }
    }

    private File getImageFile() {
        //TODO
        return new File(EnvConfig.getImageChatMsgDirectory(null),
                UniqueFileName.getUniqueFileName("jpg"));
    }

}