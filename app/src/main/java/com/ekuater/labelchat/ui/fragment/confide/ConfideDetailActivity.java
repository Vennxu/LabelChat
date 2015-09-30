package com.ekuater.labelchat.ui.fragment.confide;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.ConfideComment;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.ConfideManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.ContentSharer;
import com.ekuater.labelchat.ui.ShareContent;
import com.ekuater.labelchat.ui.activity.base.BaseActivity;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.fragment.friends.StrangerHelper;
import com.ekuater.labelchat.ui.fragment.labelstory.LabelStoryUtils;
import com.ekuater.labelchat.ui.fragment.labelstory.NoScrollListview;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.widget.KeyboardStateView;
import com.ekuater.labelchat.ui.widget.emoji.EmojiEditText;
import com.ekuater.labelchat.ui.widget.emoji.EmojiKeyboard;
import com.ekuater.labelchat.ui.widget.emoji.EmojiSelector;
import com.ekuater.labelchat.util.GeocodeSearcher;

/**
 * Created by Administrator on 2015/4/7.
 *
 * @author XuWenxiang
 */
public class ConfideDetailActivity extends BaseActivity implements Handler.Callback, View.OnClickListener {

    private static final int CONFIDE_COMMENT_LIST_RESULT = 101;
    private static final int CONFIDE_COMMENT_SEND_RESULT = 102;
    private static final int MSG_SEARCH_ADDRESS_RESULT = 103;
    private static final int MSG_SEARCH_ADDRESS = 104;
    private static final int MSG_KEYBOARD_STATE_CHANGED = 105;

    private Activity activity;
    private ConfideManager confideManager;
    private GeocodeSearcher geocodeSearcher;
    private InputMethodManager mInputMethodManager;
    private StrangerHelper mStrangerHelper;
    private ConfideShowAdapter.ViewHolder holder;
    private ConfideShowAdapter adapter;
    private ConfideDetaileAdapter detaileAdapter;
    private SimpleProgressDialog mProgressDialog;
    private Confide mConfide;
    private int requestTime = 0;
    private String position;

    private ImageView loading;
    private LinearLayout loadingLinnear;
    private FrameLayout mDetailHideClick;
    private TextView mInputHint;
    private EmojiSelector mEmojiSelector;
    private ImageButton mFaceImageButton;
    private ImageView mImageNull;
    private Button mSendComents;
    private EmojiEditText mEmojiEditText;
    private ScrollView mScrollView;

    private boolean mIsFaceShow = false;
    private boolean mIsReply = false;
    private boolean isHideKeyboard = false;
    private boolean isShowSoft = false;
    private ConfideComment comment = null;
    private int index;
    private Handler handler;
    private PopupWindow mMorePopup;
    private ContentSharer mContentSharer;

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;
        switch (msg.what) {
            case CONFIDE_COMMENT_LIST_RESULT:
                handlerQueryConfideComment(msg);
                break;
            case CONFIDE_COMMENT_SEND_RESULT:
                handlerAddConfideComment(msg);
                break;
            case MSG_SEARCH_ADDRESS_RESULT:
                handleSearchAddressResult(msg.arg1 != 0,
                        (GeocodeSearcher.SearchAddress) msg.obj);
                break;
            case MSG_SEARCH_ADDRESS:
                searchAddress();
                break;
            case MSG_KEYBOARD_STATE_CHANGED:
                handleKeyboardStateChanged(msg.arg1);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.comment_ui_face_switch_btn:
                mInputHint.setVisibility(View.GONE);
                mEmojiEditText.setVisibility(View.VISIBLE);
                mDetailHideClick.setVisibility(View.VISIBLE);
                if (mIsFaceShow) {
                    mEmojiEditText.requestFocus();
                    mInputMethodManager.showSoftInput(mEmojiEditText, 0);
                    showEmojiSelector(false);
                } else {
                    mInputMethodManager.hideSoftInputFromWindow(mEmojiEditText.getWindowToken(), 0);
                    showEmojiSelector(true);
                }
                break;

            case R.id.comment_ui_input_send_btn:
                String commentContent = mEmojiEditText.getText().toString();
                if (!TextUtils.isEmpty(commentContent)) {
                    if (mIsReply) {
                        String reply = commentContent.substring(mCount, commentContent.length());
                        if (!TextUtils.isEmpty(reply)) {
                            comment.setReplyComment(comment.getComment() + " ");
                            comment.setComment(reply);
                            comment.setReplyFloor(comment.getFloor());
                            comment.setPosition(position);
                            comment.setUserIds(confideManager.getUserIds(detaileAdapter.getConfideComment()));
                        }
                    } else {
                        comment = new ConfideComment();
                        comment.setComment(commentContent + " ");
                        comment.setPosition(position);
                        comment.setConfideId(mConfide.getConfideId());
                        comment.setUserIds(confideManager.getUserIds(detaileAdapter.getConfideComment()));
                    }
                    addConfideComment();
                }
                break;
            case R.id.confide_detaile_item_tx:
                String userId = v.getTag().toString();
                if (!TextUtils.isEmpty(userId) && SettingHelper.getInstance(activity).getAccountUserId().equals(mConfide.getConfideUserId())) {
                    mStrangerHelper.showStranger(userId);
                }
                break;

            case R.id.operation_bar_praise:
                if (!TextUtils.isEmpty(mConfide.getConfideId())) {
                    praiseConfide();
                    if ("Y".equals(mConfide.getConfideIsPraise())) {
                        mConfide.setConfideIsPraise("N");
                        mConfide.setConfidePraiseNum(mConfide.getConfidePraiseNum() - 1);
                    } else {
                        mConfide.setConfideIsPraise("Y");
                        mConfide.setConfidePraiseNum(mConfide.getConfidePraiseNum() + 1);
                    }

                }
                holder.praiseNum.setText(mConfide.getConfidePraiseNum() + "");
                holder.praise.setImageResource(mConfide.getConfideIsPraise().equals("Y") ? R.drawable.ic_praise_pressed : R.drawable.ic_praise_white);
                holder.praiseNum.setVisibility(mConfide.getConfidePraiseNum() == 0 ? View.INVISIBLE:View.VISIBLE);
                break;

            case R.id.right_title:
                if (mMorePopup == null) {
                    setupMorePopup();
                }
                mMorePopup.showAsDropDown(v);
                break;

            case R.id.comment_ui_input_hint:
                mDetailHideClick.setVisibility(View.VISIBLE);
                showSoftInput();
                break;
            case R.id.detail_hint_click:
                showEmojiSelector(false);
                clearContentEditFocus();
                break;
            case R.id.operation_bar_more:
                if (mMorePopup == null) {
                    setupMorePopup();
                }
                mMorePopup.showAsDropDown(v);
                break;
            case R.id.operation_bar_comment_parent:
                mDetailHideClick.setVisibility(View.VISIBLE);
                showSoftInput();
                break;
            default:

                break;
        }
    }

    private void showSoftInput(){
        mEmojiEditText.setVisibility(View.VISIBLE);
        mInputHint.setVisibility(View.GONE);
        mEmojiEditText.requestFocus();
        mInputMethodManager.showSoftInput(mEmojiEditText, 0);
        showEmojiSelector(false);
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ConfideComment parentComment = (ConfideComment) parent.getAdapter().getItem(position);
            if (parentComment != null) {
                comment = new ConfideComment(parentComment);
                mIsReply = true;
                showSoftInput();
                String content = "@" + String.format(getString(R.string.confide_floor), comment.getFloor()) + " ";
                mCount = content.length();
                SpannableString ss = new SpannableString(content);
                ss.setSpan(new ForegroundColorSpan(R.color.colorLabelTextLight), 0, mCount - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mEmojiEditText.setText(ss);
                mEmojiEditText.setSelection(mCount);
            }
        }
    };

    private ConfideDetaileAdapter.GetDateListener getDateListener = new ConfideDetaileAdapter.GetDateListener() {
        @Override
        public void getAdapterDate() {
            queryConfideComment();
        }
    };

    private KeyboardStateView.OnKeyboardStateChangedListener mKeyboardStateChangedListener
            = new KeyboardStateView.OnKeyboardStateChangedListener() {
        @Override
        public void onKeyboardStateChanged(int state) {
            handler.obtainMessage(MSG_KEYBOARD_STATE_CHANGED, state, 0).sendToTarget();
        }
    };

    private View getRootView() {
        return ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_confide_detaile);
        activity = this;
        BaseActivity baseActivity = (BaseActivity) activity;
        baseActivity.setHasContentSharer();
        mContentSharer = baseActivity.getContentSharer();
        paramArgument();
        handler = new Handler(this);
        mStrangerHelper = new StrangerHelper(this);
        confideManager = ConfideManager.getInstance(activity);
        geocodeSearcher = GeocodeSearcher.getInstance(activity);
        mInputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        holder = new ConfideShowAdapter.ViewHolder();
        adapter = new ConfideShowAdapter(activity, this, false);
        detaileAdapter = new ConfideDetaileAdapter(activity, getDateListener, mConfide.getConfideUserId(), mConfide.getConfideSex(), this);
        searchAddress();
        onCreateView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeMessages(MSG_SEARCH_ADDRESS);
    }

    @Override
    protected void initializeActionBar() {
    }

    private void paramArgument() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mConfide = bundle.getParcelable(ConfideUtils.CONFIDE);
            index = bundle.getInt(ConfideUtils.CONFIDE_INDEX);
            isShowSoft = bundle.getBoolean(ConfideUtils.IS_SHOW_SOFT);
        }
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    int mCount = 0;

    private void onCreateView() {
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(getString(R.string.labelstory_input_detail));
        ImageView icon = (ImageView) findViewById(R.id.icon);
        TextView right_titile = (TextView) findViewById(R.id.right_title);
        right_titile.setVisibility(View.GONE);
        right_titile.setText("");
        right_titile.setBackgroundResource(R.drawable.ic_more);
        right_titile.setOnClickListener(this);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        NoScrollListview commentListView = (NoScrollListview) findViewById(R.id.desdcript_comment);
        loading = (ImageView) findViewById(R.id.descript_comment_loading);
        loadingLinnear = (LinearLayout) findViewById(R.id.descript_comment_loading_linear);
        mEmojiSelector = (EmojiSelector) findViewById(R.id.chatting_ui_input_emoji_layout);
        mFaceImageButton = (ImageButton) findViewById(R.id.comment_ui_face_switch_btn);
        mSendComents = (Button) findViewById(R.id.comment_ui_input_send_btn);
        mImageNull = (ImageView) findViewById(R.id.desdcript_comment_null);
        mEmojiEditText = (EmojiEditText) findViewById(R.id.comment_ui_input_edit);
        mInputHint = (TextView) findViewById(R.id.comment_ui_input_hint);
        mDetailHideClick = (FrameLayout) findViewById(R.id.detail_hint_click);
        mScrollView = (ScrollView) findViewById(R.id.descript_scroll);
        KeyboardStateView keyboardStateView = (KeyboardStateView)
                findViewById(R.id.keyboard_state_view);
        keyboardStateView.setOnKeyboardStateChangedListener(mKeyboardStateChangedListener);
        mInputHint.setText(activity.getString(R.string.confide_comment_hint));
        mEmojiEditText.addTextChangedListener(textWatcher);
        mSendComents.setOnClickListener(this);
        mFaceImageButton.setOnClickListener(this);
        mInputHint.setOnClickListener(this);
        mDetailHideClick.setOnClickListener(this);
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
                    if (mEmojiEditText.getText().toString().length() < mCount && mIsReply) {
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
        commentListView.setAdapter(detaileAdapter);
        commentListView.setOnItemClickListener(onItemClickListener);
        initView();
        initDate();
        if (isShowSoft){
            mDetailHideClick.setVisibility(View.VISIBLE);
            showSoftInput();
        }else {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.softInputMode |= WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
        }
        queryConfideComment();
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                mSendComents.setEnabled(false);
                mIsReply = false;
                if (isHideKeyboard){
                    clearContentEditFocus();
                }
            } else {
                mSendComents.setEnabled(true);
            }
        }
    };

    private void clearReply() {
        mIsReply = false;
        mEmojiEditText.setHint(getString(R.string.confide_comment_hint));
    }

    private void initView() {
        if (holder != null) {
            adapter.bindView(holder, getRootView());
        }
    }

    private void initDate() {
        mImageNull.setVisibility(View.VISIBLE);
        mImageNull.setImageResource(LabelStoryUtils.getCommentNull(activity));
        if (holder != null) {
            adapter.bindDate(holder, mConfide, index);
        }
    }

    private void queryConfideComment() {
        ConfideUtils.startAnimation(loadingLinnear, loading);
        requestTime++;
        confideManager.queryConfideComment(mConfide.getConfideId(), String.valueOf(requestTime), new ConfideManager.ConfideCommentListObserver() {
            @Override
            public void onQueryResult(int result, Confide confides) {
                handler.obtainMessage(CONFIDE_COMMENT_LIST_RESULT, result, 0, confides).sendToTarget();
            }
        });
    }

    private void praiseConfide() {
        confideManager.praiseConfide(mConfide.getConfideId(), new FunctionCallListener() {
            @Override
            public void onCallResult(int result, int errorCode, String errorDesc) {

            }
        });
    }

    private void setupMorePopup() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        @SuppressLint("InflateParams")
        View contentView = inflater.inflate(R.layout.option, null);
        TextView report = (TextView) contentView.findViewById(R.id.report);
        TextView share = (TextView) contentView.findViewById(R.id.share);

        report.setOnClickListener(mMorePopupListener);
        share.setOnClickListener(mMorePopupListener);
        mMorePopup = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mMorePopup.setFocusable(true);
        mMorePopup.setOutsideTouchable(true);
        mMorePopup.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
    }

    private final View.OnClickListener mMorePopupListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.share:
                    if (mConfide != null) {
                        mMorePopup.dismiss();
                        shareContent(new ShareContent(
                                getString(R.string.labelstory_item_share_gaveyout),
                                mConfide.getConfideContent(),
                                BitmapFactory.decodeResource(getResources(),
                                        R.drawable.ap_icon_large),
                                getString(R.string.config_confide_detail_url)
                                        + mConfide.getConfideId(),
                                mConfide.getConfideId()));
                    }
                    break;
                case R.id.report:
                    mMorePopup.dismiss();
                    MiscUtils.complainConfide(v.getContext(), mConfide.getConfideId());
                    break;
                default:
                    break;
            }
        }
    };

    private void shareContent(ShareContent content) {
        mContentSharer.setShareContent(content);
        mContentSharer.openSharePanel();
    }

    private void addConfideComment() {
        showProgressDialog();
        confideManager.addConfideComment(comment, new ConfideManager.ConfideCommentObserver() {
            @Override
            public void onQueryResult(int result, ConfideComment comment) {
                handler.obtainMessage(CONFIDE_COMMENT_SEND_RESULT, result, 0, comment).sendToTarget();
            }
        });
    }

    private void searchAddress() {
        LocationInfo location = AccountManager.getInstance(activity).getLocation();
        if (location == null) {
            handler.sendEmptyMessageDelayed(MSG_SEARCH_ADDRESS, 2000);
            return;
        }

        geocodeSearcher.searchAddress(location, new GeocodeSearcher.AddressObserver() {
            @Override
            public void onSearch(boolean success, GeocodeSearcher.SearchAddress address) {
                handler.obtainMessage(MSG_SEARCH_ADDRESS_RESULT,
                        success ? 1 : 0, 0, address).sendToTarget();
            }
        });
    }

    private void handlerQueryConfideComment(Message msg) {
        ConfideUtils.stopAnimation(loadingLinnear, loading);
        if (msg.arg1 == ConstantCode.EXECUTE_RESULT_SUCCESS) {
            Confide confide = (Confide) msg.obj;
            if (confide != null) {
                if (requestTime == 1) {
                    mConfide = confide;
                }
                ConfideComment[] comments = confide.getConfideComments();
                if (comments != null) {
                    detaileAdapter.notifyAdapterList(comments);
                }
                initDate();
            }
        }
        if (requestTime == 1) {
            mScrollView.smoothScrollTo(0, 0);
        }
    }

    private void handlerAddConfideComment(Message msg) {
        dismissProgressDialog();
        if (msg.arg1 == ConstantCode.EXECUTE_RESULT_SUCCESS) {
            mEmojiEditText.setText("");
            ConfideComment addComment = (ConfideComment) msg.obj;
            if (addComment != null) {
                mConfide.setConfideCommentNum(mConfide.getConfideCommentNum() + 1);
                holder.commentNum.setText( mConfide.getConfideCommentNum()+"");
                holder.commentNum.setVisibility(View.VISIBLE);
                detaileAdapter.addComment(addComment);
            }
            showEmojiSelector(false);
            clearContentEditFocus();
        } else {
            ShowToast.makeText(activity, R.drawable.emoji_cry, activity.
                    getResources().getString(R.string.labelstroy_input_comment_faile)).show();
        }
    }

    private void handleSearchAddressResult(boolean success,
                                           GeocodeSearcher.SearchAddress address) {
        if (success && address != null) {
            position = TextUtils.isEmpty(address.city) ? address.province : address.city;
        }
    }

    private void showEmojiSelector(boolean show) {
        mEmojiSelector.setVisibility(show ? View.VISIBLE : View.GONE);
        mFaceImageButton.setImageResource(show ? R.drawable.ic_input_keyboard_selector
                : R.drawable.ic_input_face_selector);
        mIsFaceShow = show;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isBack = false;
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            if (!mIsFaceShow) {
                putParam();
                isBack = true;
            } else {
                mIsFaceShow = true;
                isBack = false;
                showEmojiSelector(false);
            }
        }
        return isBack;
    }

    public void putParam() {
        Intent intent = new Intent();
        intent.putExtra(ConfideUtils.CONFIDE, mConfide);
        intent.putExtra(ConfideUtils.CONFIDE_INDEX, index);
        activity.setResult(Activity.RESULT_OK, intent);
        finish();
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

    private void handleKeyboardStateChanged(int state) {
        switch (state) {
            case KeyboardStateView.KEYBOARD_STATE_HIDE:
                isHideKeyboard = true;
                if (mEmojiEditText.hasFocus()) {
                    clearContentEditFocus();
                }
                break;
            case KeyboardStateView.KEYBOARD_STATE_SHOW:
                isHideKeyboard = false;
                mDetailHideClick.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private void clearContentEditFocus() {
        if (!mIsFaceShow) {
            mEmojiEditText.setVisibility(View.GONE);
            mInputHint.setVisibility(View.VISIBLE);
            mDetailHideClick.setVisibility(View.GONE);
        }
        if(mEmojiEditText.getText().length() == 0) {
            mInputHint.setText(getString(R.string.confide_comment_hint));
        }else{
            mInputHint.setText(mEmojiEditText.getText().toString());
        }
        hideSoftInput();
    }

    private void hideSoftInput() {
        mInputMethodManager.hideSoftInputFromWindow(mEmojiEditText.getWindowToken(), 0);
    }
}
