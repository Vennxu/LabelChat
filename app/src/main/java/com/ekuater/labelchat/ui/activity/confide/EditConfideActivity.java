package com.ekuater.labelchat.ui.activity.confide;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.confide.ConfidePublishEvent;
import com.ekuater.labelchat.datastruct.confide.PublishContent;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.ConfideManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.QueryResult;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UIEventBusHub;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.util.ViewUtils;
import com.ekuater.labelchat.ui.widget.KeyboardStateView;
import com.ekuater.labelchat.util.ColorUtils;
import com.ekuater.labelchat.util.GeocodeSearcher;

import java.util.Random;

import de.greenrobot.event.EventBus;

/**
 * Created by Leo on 2015/4/7.
 *
 * @author LinYong
 */
public class EditConfideActivity extends BackIconActivity
        implements View.OnClickListener, Handler.Callback {

    private static final int MSG_SEARCH_ADDRESS_RESULT = 100;
    private static final int MSG_PUBLISH_CONFIDE_RESULT = 101;
    private static final int MSG_KEYBOARD_STATE_CHANGED = 102;
    private static final int MSG_SEARCH_ADDRESS = 103;

    private static final int REQUEST_SELECT_ROLE = 100;

    private Handler mHandler;
    private EventBus mUIEventBus;
    private InputMethodManager mInputMethodManager;
    private GeocodeSearcher mGeocodeSearcher;
    private ConfideManager mConfideManager;
    private ContactsManager mContactManager;
    private SettingHelper mSettingHelper;
    private int mContentMaxLength;
    private int[] mConfideColors;
    private String[] mConfideBgs;
    private int mConfideColor;
    private int mConfideBg;
    private String mConfideBgKey;
    private Random mRandom;
    private String mSelectedRole;
    private SimpleProgressHelper mProgressHelper;

    private TextView mPublishBtn;
    private EditText mContentEdit;
    private TextView mRemainText;
    private TextView mRegionText;
    private ImageView mRegionSwitchBtn;
    private TextView mRoleText;
    private View mContentHintView;
    private View mEditAreaView;
    private View mFocusView;

    private boolean mRegionEnable;

    private TextWatcher mContentTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onConfideContentChanged();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    private View.OnFocusChangeListener mContentFocusChangeListener
            = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            updateContentHintEnable();
        }
    };
    private KeyboardStateView.OnKeyboardStateChangedListener mKeyboardStateChangedListener
            = new KeyboardStateView.OnKeyboardStateChangedListener() {
        @Override
        public void onKeyboardStateChanged(int state) {
            mHandler.obtainMessage(MSG_KEYBOARD_STATE_CHANGED, state, 0).sendToTarget();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_confide);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        initData();
        initView(getWindow().getDecorView());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(MSG_SEARCH_ADDRESS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SELECT_ROLE:
                handleRoleSelected(resultCode, data);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP
                && mContentEdit.hasFocus()
                && !ViewUtils.touchEventInView(mContentEdit, ev)) {
            clearContentEditFocus();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.publish:
                onPublishConfide();
                break;
            case R.id.role:
                onSelectRole();
                break;
            case R.id.random_color:
                updateConfideColor();
                break;
            case R.id.region_switch:
                onSwitchRegion();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_SEARCH_ADDRESS_RESULT:
                handleSearchAddressResult(msg.arg1 != 0,
                        (GeocodeSearcher.SearchAddress) msg.obj);
                break;
            case MSG_PUBLISH_CONFIDE_RESULT:
                onPublishConfideResult(msg.arg1, (Confide) msg.obj);
                break;
            case MSG_KEYBOARD_STATE_CHANGED:
                handleKeyboardStateChanged(msg.arg1);
                break;
            case MSG_SEARCH_ADDRESS:
                searchAddress();
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private void initData() {
        mHandler = new Handler(this);
        mUIEventBus = UIEventBusHub.getDefaultEventBus();
        mInputMethodManager = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        mGeocodeSearcher = GeocodeSearcher.getInstance(this);
        mConfideManager = ConfideManager.getInstance(this);
        mContactManager = ContactsManager.getInstance(this);
        mSettingHelper = SettingHelper.getInstance(this);
        mContentMaxLength = getResources().getInteger(R.integer.confide_max_length);
        mConfideColors = getConfideColors();
        mConfideBgs = getConfidebg();
        mRandom = new Random();
        mSelectedRole = "";
        mProgressHelper = new SimpleProgressHelper(this);
        mRegionEnable = true;
        searchAddress();
    }

    private void initView(View rootView) {
        ImageView icon = (ImageView) rootView.findViewById(R.id.icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        View randomColorBtn = rootView.findViewById(R.id.random_color);
        KeyboardStateView keyboardStateView = (KeyboardStateView)
                rootView.findViewById(R.id.keyboard_state_view);
        mPublishBtn = (TextView) rootView.findViewById(R.id.publish);
        mContentEdit = (EditText) rootView.findViewById(R.id.confide_input);
        mRemainText = (TextView) rootView.findViewById(R.id.confide_input_remain);
        mRegionText = (TextView) rootView.findViewById(R.id.region);
        mRegionSwitchBtn = (ImageView) rootView.findViewById(R.id.region_switch);
        mRoleText = (TextView) rootView.findViewById(R.id.role);
        mContentHintView = rootView.findViewById(R.id.confide_hint);
        mEditAreaView = rootView.findViewById(R.id.edit_area);
        mFocusView = rootView.findViewById(R.id.focus_view);

        randomColorBtn.setOnClickListener(this);
        keyboardStateView.setOnKeyboardStateChangedListener(mKeyboardStateChangedListener);
        mPublishBtn.setOnClickListener(this);
        mRegionText.setText("");
        mRoleText.setOnClickListener(this);
        mContentEdit.addTextChangedListener(mContentTextWatcher);
        mContentEdit.setOnFocusChangeListener(mContentFocusChangeListener);
        mRegionSwitchBtn.setOnClickListener(this);

        updateConfideColor();
        onConfideContentChanged();
        hideSoftInput();
    }

    private void searchAddress() {
        LocationInfo location = AccountManager.getInstance(this).getLocation();
        if (location == null) {
            mHandler.sendEmptyMessageDelayed(MSG_SEARCH_ADDRESS, 2000);
            return;
        }

        mGeocodeSearcher.searchAddress(location, new GeocodeSearcher.AddressObserver() {
            @Override
            public void onSearch(boolean success, GeocodeSearcher.SearchAddress address) {
                mHandler.obtainMessage(MSG_SEARCH_ADDRESS_RESULT,
                        success ? 1 : 0, 0, address).sendToTarget();
            }
        });
    }

    private void handleSearchAddressResult(boolean success,
                                           GeocodeSearcher.SearchAddress address) {
        if (success && address != null) {
            String city = TextUtils.isEmpty(address.city) ? address.province : address.city;
            mRegionText.setText(city);
        }
    }

    private void onPublishConfide() {
        String content = mContentEdit.getText().toString();
        String bgColor = ColorUtils.toColorString(mConfideColor);
        String bgImg =getResources().getResourceEntryName(mConfideBg);
        int gender = mSettingHelper.getAccountSex();
        String position = mRegionEnable ? mRegionText.getText().toString() : "";

        if (TextUtils.isEmpty(mSelectedRole)) {
            ShowToast.makeText(this, R.drawable.emoji_sad,
                    getString(R.string.select_confide_role_prompt)).show();
            return;
        }

        PublishContent publish = new PublishContent();
        publish.setContent(content);
        publish.setBgColor(bgColor);
        publish.setBgImg(mConfideBgKey);
        publish.setRole(mSelectedRole);
        publish.setGender(gender);
        publish.setPosition(position);
        mConfideManager.publishConfide(publish, mContactManager.getUserIds(this), new ConfideManager.PublishObserver() {
            @Override
            public void onPublishResult(int result, Confide confide) {
                mHandler.obtainMessage(MSG_PUBLISH_CONFIDE_RESULT, result, 0,
                        confide).sendToTarget();
            }
        });
        mProgressHelper.show();
    }

    private void onPublishConfideResult(int result, Confide confide) {
        mProgressHelper.dismiss();

        if (result == QueryResult.RESULT_SUCCESS) {
            ShowToast.makeText(this, R.drawable.emoji_smile,
                    getString(R.string.confide_publish_success)).show();
            if (confide != null) {
                mUIEventBus.post(new ConfidePublishEvent(confide));
            }
            finish();
        } else {
            ShowToast.makeText(this, R.drawable.emoji_sad,
                    getString(R.string.confide_publish_failed)).show();
        }
    }

    private void onSelectRole() {
        UILauncher.launchShowConfideRoleUI(this, REQUEST_SELECT_ROLE);
    }

    private void handleRoleSelected(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String role = data != null ? data.getStringExtra("ConfideRole") : "";

            if (!TextUtils.isEmpty(role)) {
                mSelectedRole = role;
                mRoleText.setText(getString(R.string.selected_confide_role, mSelectedRole));
                updatePublishEnable();
            }
        } else {
            ShowToast.makeText(this, R.drawable.emoji_sad,
                    getString(R.string.select_confide_role_failed)).show();
        }
    }

    private void onSwitchRegion() {
        mRegionEnable = !mRegionEnable;
        updateRegionEnable();
    }

    private void updateRegionEnable() {
        if (mRegionEnable) {
            mRegionText.setVisibility(View.VISIBLE);
            mRegionSwitchBtn.setImageResource(R.drawable.ic_region_delete);
        } else {
            mRegionText.setVisibility(View.GONE);
            mRegionSwitchBtn.setImageResource(R.drawable.ic_region_add);
        }
    }

    private int[] getConfideColors() {
        Resources res = getResources();
        String[] colorStrings = res.getStringArray(R.array.confide_colors);
        int length = colorStrings.length;
        int[] colors = new int[length];

        for (int i = 0; i < length; ++i) {
            colors[i] = ColorUtils.parseColor(colorStrings[i]);
        }
        return colors;
    }

    private String[] getConfidebg() {
        Resources res = getResources();
        String [] key = res.getStringArray(R.array.confide_image_key);
        return key;
    }

    private int randomColor() {
        return mConfideColors[mRandom.nextInt(mConfideColors.length)];
    }

    private int randomBg() {
        mConfideBgKey = mConfideBgs[mRandom.nextInt(mConfideBgs.length)];
        return mConfideManager.getConfideBs().get(mConfideBgKey);
    }

    private void updateConfideColor() {
        mConfideColor = randomColor();
        mConfideBg = randomBg();
        mEditAreaView.setBackgroundResource(mConfideBg);
    }

    private void onConfideContentChanged() {
        updateContentRemainText();
        updateContentHintEnable();
        updatePublishEnable();
    }

    private void updatePublishEnable() {
        Resources res = getResources();
        boolean enable = mContentEdit.getText().length() > 0;
        int color = res.getColor((enable && !TextUtils.isEmpty(mSelectedRole)
                ? R.color.confide_publish_enable : R.color.confide_publish_disable));
        mPublishBtn.setTextColor(color);
        mPublishBtn.setEnabled(enable);
    }

    private void updateContentHintEnable() {
        final int length = mContentEdit.getText().length();
        final boolean enable = (length <= 0) && !mContentEdit.hasFocus();
        mContentHintView.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    private void updateContentRemainText() {
        final int length = mContentEdit.getText().length();
        mRemainText.setText(getString(R.string.confide_input_remain, mContentMaxLength - length));
    }

    private void hideSoftInput() {
        mInputMethodManager.hideSoftInputFromWindow(mContentEdit.getWindowToken(), 0);
    }

    private void clearContentEditFocus() {
        mFocusView.requestFocus();
        hideSoftInput();
    }

    private void handleKeyboardStateChanged(int state) {
        switch (state) {
            case KeyboardStateView.KEYBOARD_STATE_HIDE:
                if (mContentEdit.hasFocus()) {
                    clearContentEditFocus();
                }
                break;
            case KeyboardStateView.KEYBOARD_STATE_SHOW:
                break;
            default:
                break;
        }
    }
}
