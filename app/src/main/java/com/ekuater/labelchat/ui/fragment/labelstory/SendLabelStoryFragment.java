package com.ekuater.labelchat.ui.fragment.labelstory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LabelStoryCategory;
import com.ekuater.labelchat.delegate.AlbumManager;
import com.ekuater.labelchat.delegate.PostStoryListener;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.widget.emoji.EmojiEditText;
import com.ekuater.labelchat.ui.widget.emoji.EmojiKeyboard;
import com.ekuater.labelchat.ui.widget.emoji.EmojiSelector;
import com.ekuater.labelchat.ui.fragment.labels.LabelSelectDialog;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.fragment.throwphoto.PhotoCompressor;
import com.ekuater.labelchat.ui.util.ViewHolder;
import com.ekuater.labelchat.ui.widget.emoji.ShowContentTextView;
import com.ekuater.labelchat.util.BmpUtils;
import com.ekuater.labelchat.util.L;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Label on 2015/1/8.
 *
 * @author Xu wenxiang
 */
public class SendLabelStoryFragment extends BackIconActivity {

    private static final String TAG = SendLabelStoryFragment.class.getSimpleName();

    public static final int REQUEST_SELECT_IMAGE = 102;
    public static final int REQUEST_DETAIL_IMAGE = 103;
    private static final int REQUEST_TEXT_CODE = 0;
    private static final int REQUEST_IMAGE_CODE = 1;
    public static final String DETAIL_IMAGE_LIST = "detail_image_list";
    public static final String DETAIL_IMAGE_SELECTED = "detail_image_selected";
    public static final String SEND_REQUEST_DATA = "send_request_data";
    private EmojiEditText mEditText;
    private ImageButton mEmojiImageButton;
    private EmojiSelector mEmojiSelector;
    private ShowContentTextView mTextFlag;
    private KeyboardListenRelativeLayout mKeyboardListenerRelativeLayout;
    private LinearLayout mEmojiLinear;
    private GridView mGridView;
    private Button mBundingBtn;
    private ArrayList<String> mArrayList = new ArrayList<String>();
    private SelectPictureAdapter mSelectiPictureAdapter;
    private Activity mContext;
    private TextView mRemainText;
    private boolean mIsFaceShow = false;
    private InputMethodManager mInputMethodManager;
    private LabelStoryManager mLabelStoryManager;
    private SimpleProgressDialog mProgressDialog;
    private TextView title;
    private boolean isHideEmoji = true;
    private LabelStoryCategory category;
    private File[] mCompressedPhotoFiles = null;

    private Button mSendLabelStory;
    private String content = null;
    private Display display;

    private Handler sendLabelStoryHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mCompressedPhotoFiles != null && mCompressedPhotoFiles.length > 0) {
                deleteTempPhotoFiles();
            }
            dismissProgressDialog();
            handleSendLabelStoryResult(msg.what, msg.obj);

        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == mArrayList.size()) {
                int count = 5;
                UILauncher.launchSelectPhotoImageUI(mContext, REQUEST_SELECT_IMAGE, getResources().getString(R.string.labelstory_input_select_image), getResources().getString(R.string.labelstory_input_finish), (count), mArrayList);
            } else {
                UILauncher.launchLabelStoryDetailPhotoUI(mContext, REQUEST_DETAIL_IMAGE, mArrayList, position);
            }
        }
    };
    private TextWatcher mTextWachter = new TextWatcher() {
        int textCount = 100;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                content = null;
                mEditText.setTextSize(18);
                mRemainText.setText(mContext.getResources().getString(R.string.labelstory_input_send_remain) + textCount +
                        mContext.getResources().getString(R.string.labelstory_input_send_remainz));
            } else {
                content = mEditText.getText().toString();
                mEditText.setTextSize(18);
                int remainCount = textCount - s.length();
                mRemainText.setText(mContext.getResources().getString(R.string.labelstory_input_send_remain) + remainCount +
                        mContext.getResources().getString(R.string.labelstory_input_send_remainz));
            }
        }
    };

    private class CompressListener implements PhotoCompressor.OnCompressListener {

        private final String content;

        public CompressListener(String content) {
            this.content = content;
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onFinish(File[] compressedPhotoFiles) {
            sendImageLabelStory(compressedPhotoFiles, content);
        }
    }

    private TextView released;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_labelstory_send);
        getActionBar().hide();
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
        ImageView icon = (ImageView) findViewById(R.id.icon);
        released = (TextView) findViewById(R.id.right_title);
        released.setVisibility(View.VISIBLE);
        released.setText(getResources().getString(R.string.labelstory_input_send));
        released.setEnabled(false);
        released.setTextColor(getResources().getColor(R.color.colorLightDark));
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title = (TextView) findViewById(R.id.title);
        title.setText(getString(R.string.labelstory_input_send_story, 0));
        mContext = this;
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mSelectiPictureAdapter = new SelectPictureAdapter(mContext);
        mLabelStoryManager = LabelStoryManager.getInstance(mContext);
        released.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                content = mEditText.getText().toString();
                if (mArrayList.size() > 0) {
                    showProgressDialog();
                    File[] files = new File[mArrayList.size()];
                    for (int i = 0; i < mArrayList.size(); i++) {
                        files[i] = new File(mArrayList.get(i));
                    }
                    PhotoCompressor.compress(files, new CompressListener(content));
                } else {
                    if (!TextUtils.isEmpty(content)) {
                        showProgressDialog();
                        sendTextLabelStory(content);
                    } else {
                        ShowToast.makeText(mContext, R.drawable.emoji_sad, mContext.
                                getResources().getString(R.string.labelstory_input_send_null)).show();
                    }
                }

            }
        });
        WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
        lp.softInputMode |= WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        initView();
    }


    private void sendTextLabelStory(String content) {
        LabelStoryManager.LabelStoryQueryObserver labelStoryQueryObserver = new LabelStoryManager.LabelStoryQueryObserver() {
            @Override
            public void onQueryResult(int result, LabelStory[] labelStories, boolean remaining, int frendsCount) {
                Message message = Message.obtain(sendLabelStoryHandler, result, labelStories);
                sendLabelStoryHandler.sendMessage(message);
            }

            @Override
            public void onPraiseQueryResult(int result, boolean remaining) {

            }
        };
        String categoryId = null;
        if (category != null) {
            categoryId = category.getmCategoryId();
        }
        mLabelStoryManager.sendLabelStory(categoryId, content, labelStoryQueryObserver);
    }

    private void deleteTempPhotoFiles() {
        for (File file : mCompressedPhotoFiles) {
            if (!file.delete()) {
                L.v(TAG, "onPhotoThrowResult(), delete file %1$s failed.", file.getPath());
            }
        }
    }

    private void sendImageLabelStory(File[] files, String content) {
        try {
            mCompressedPhotoFiles = files;
            String categoryId = null;
            if (category != null) {
                categoryId = category.getmCategoryId();
            }
            mLabelStoryManager.sendImageLabelStory(AlbumManager.getInstance(mContext).getRelatedUser(), mCompressedPhotoFiles, content, categoryId, new PostStoryListener() {
                @Override
                public void onPostResult(int result, int errorCode, String errorDesc, LabelStory[] labelStories) {
                    Message msg = Message.obtain(sendLabelStoryHandler, errorCode, labelStories);
                    sendLabelStoryHandler.sendMessage(msg);
                }
            });
        } catch (FileNotFoundException e) {
            dismissProgressDialog();
            deleteTempPhotoFiles();
            ShowToast.makeText(mContext, R.drawable.emoji_cry, mContext.
                    getResources().getString(R.string.labelstory_input_send_failed)).show();
        }


    }

    private void handleSendLabelStoryResult(int errorCode, Object obj) {
        if (errorCode == CommandErrorCode.REQUEST_SUCCESS || errorCode == ConstantCode.EXECUTE_RESULT_SUCCESS) {
            ShowToast.makeText(mContext, R.drawable.emoji_smile, mContext.
                    getResources().getString(R.string.labelstory_input_send_succese)).show();
            content = null;
            finish();
        } else {
            ShowToast.makeText(mContext, R.drawable.emoji_cry, mContext.
                    getResources().getString(R.string.labelstory_input_send_failed)).show();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = SimpleProgressDialog.newInstance();
            mProgressDialog.show(getSupportFragmentManager(), "SimpleProgressDialog");
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public void initView() {
        mKeyboardListenerRelativeLayout = (KeyboardListenRelativeLayout) findViewById(R.id.labelstory_fragment_send_keyboard);
        mEmojiLinear = (LinearLayout) findViewById(R.id.labelstory_fragment_send_emojilinear);
        mEditText = (EmojiEditText) findViewById(R.id.labelstory_fragment_send_input);
        mTextFlag = (ShowContentTextView) findViewById(R.id.labelstory_fragment_send_flag);
        mTextFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditext(true);
                showSoftInput();
                isHideEmoji = true;
                mEmojiImageButton.setImageResource(R.drawable.ic_labelstory_send_emoji_pressed);
                mEmojiSelector.setVisibility(View.GONE);
            }
        });
        mEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isHideEmoji = true;
                mEmojiImageButton.setImageResource(R.drawable.ic_labelstory_send_emoji_pressed);
                mEmojiSelector.setVisibility(View.GONE);
                return false;
            }
        });
        mGridView = (GridView) findViewById(R.id.labelstory_fragment_send_grid);
        int height = (display.getWidth() - (BmpUtils.dp2px(this, 10) * 5)) / 3;
        mGridView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height * 2 + (BmpUtils.dp2px(this, 10))));
        mEmojiImageButton = (ImageButton) findViewById(R.id.labelstory_fragment_send_face);
        mEmojiSelector = (EmojiSelector) findViewById(R.id.chatting_ui_input_emoji_layout);
        mRemainText = (TextView) findViewById(R.id.labelstory_fragment_send_text);
        mBundingBtn = (Button) findViewById(R.id.labelstory_fragment_send_bunding);
        if (category != null) {
            mBundingBtn.setTextColor(getResources().getColor(R.color.colorLabelTextLight));
        }
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mEditText.addTextChangedListener(mTextWachter);
        mGridView.setAdapter(mSelectiPictureAdapter);
        mGridView.setOnItemClickListener(onItemClickListener);
        mEmojiImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsFaceShow) {
                    mEditText.requestFocus();
                    mInputMethodManager.showSoftInput(mEditText, 0);
                    showEmojiSelector(false);
                } else {
                    showEmojiSelector(true);
                    mInputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                }
            }
        });
        mEmojiSelector.setOnEmojiClickedListener(new EmojiSelector.OnEmojiClickedListener() {
            @Override
            public void onEmojiClicked(String emoji) {
                EmojiKeyboard.input(mEditText, emoji);
            }

            @Override
            public void onBackspace() {
                EmojiKeyboard.backspace(mEditText);
            }
        });
        mKeyboardListenerRelativeLayout.setOnKeyboardStateChangedListener(new KeyboardListenRelativeLayout.IOnKeyboardStateChangedListener() {
            @Override
            public void onKeyboardStateChanged(int state) {
                switch (state) {
                    case KeyboardListenRelativeLayout.KEYBOARD_STATE_HIDE://软键盘隐藏
                        if (isHideEmoji) {
                            mEmojiLinear.setVisibility(View.INVISIBLE);
                            mTextFlag.setText(mEditText.getText().toString());
                            showEditext(false);
                        } else {
                            mEmojiLinear.setVisibility(View.VISIBLE);
                        }
                        break;
                    case KeyboardListenRelativeLayout.KEYBOARD_STATE_SHOW://软键盘显示
                        mEmojiLinear.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }
        });
        mInputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    private void showEmojiSelector(boolean show) {
        mEmojiSelector.setVisibility(show ? View.VISIBLE : View.GONE);
        isHideEmoji = show ? false:true;
        mEmojiImageButton.setImageResource(show ? R.drawable.ic_labelstory_send_keyboard_pressed:R.drawable.ic_labelstory_send_emoji_pressed);
        mIsFaceShow = show;
    }

    private void showEditext(boolean isShow) {
        mTextFlag.setVisibility(isShow ? View.GONE : View.VISIBLE);
        mEditText.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }


    private void showSoftInput() {
        mEditText.requestFocus();
        mInputMethodManager.showSoftInput(mEditText, 0);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                onSelectPictureResult(resultCode, data);
                break;
            case REQUEST_DETAIL_IMAGE:
                onDetailPictureResult(resultCode, data);
                break;
            default:
                break;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (!mIsFaceShow) {
                return super.onKeyDown(keyCode, event);
            }else{
                mTextFlag.setText(mEditText.getText().toString());
                mEmojiImageButton.setImageResource(R.drawable.ic_labelstory_send_emoji_pressed);
                mEmojiSelector.setVisibility(View.INVISIBLE);
                mEmojiLinear.setVisibility(View.INVISIBLE);
                mIsFaceShow = false;
                showEditext(false);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    public void onDetailPictureResult(int resultCode, Intent data) {
        if (resultCode == mContext.RESULT_OK && data != null) {
            mArrayList.clear();
            mArrayList.addAll(data.getStringArrayListExtra(DETAIL_IMAGE_LIST));
            mSelectiPictureAdapter.notifyDataSetChanged();
            released.setEnabled(false);
            released.setTextColor(getResources().getColor(R.color.colorLightDark));
            updateTitleText();
        }
    }

    private void onSelectPictureResult(int resultCode, Intent data) {
        if (resultCode == mContext.RESULT_OK && data != null) {
            String[] imagePaths = data.getStringArrayExtra("file_path");
            boolean isTemp = data.getBooleanExtra("isTemp", false);
            List<String> lists = Arrays.asList(imagePaths);
            mArrayList.clear();
            mArrayList.addAll(lists);
            if (mArrayList != null && mArrayList.size() > 0) {
                released.setEnabled(true);
                released.setTextColor(getResources().getColor(R.color.white));
            }
            updateTitleText();
            mSelectiPictureAdapter.notifyDataSetChanged();
        }
    }

    private void updateTitleText() {
        title.setText(getString(R.string.labelstory_input_send_story, mArrayList.size()));
    }

    private class SelectPictureAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Context context;
        int margin;
        int weight;

        public SelectPictureAdapter(Context context) {
            this.context = context;
            mInflater = LayoutInflater.from(context);
            margin = MiscUtils.dp2px(context, 10);
            weight = (display.getWidth() - (margin * 5)) / 3;
        }

        @Override
        public int getCount() {
            return mArrayList.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.item_send_image, parent, false);
                holder.imageView = (ImageView) convertView.findViewById(R.id.labelstory_item_image);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(weight, weight);
                holder.imageView.setLayoutParams(params);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (position == mArrayList.size()) {
                holder.imageView.setImageResource(R.drawable.ic_addpic_unfocused);
                if (position == 5) {
                    holder.imageView.setVisibility(View.GONE);
                }
            } else {
                holder.imageView.setImageBitmap(BmpUtils.zoomDownBitmap(new File(mArrayList.get(position)), 100, 100));
            }
            return convertView;
        }
    }


    public static Bitmap readBitmap(String imgPath) {
        try {
            return BitmapFactory.decodeFile(imgPath);
        } catch (Throwable e) {
            return null;
        }
    }

    public class ViewHolder {
        private ImageView imageView;
    }
}
