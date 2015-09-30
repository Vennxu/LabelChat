package com.ekuater.labelchat.ui.fragment.register;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.ui.util.ChsLengthFilter;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;

/**
 * @author LinYong
 */
public class RegisterSetUserInfoFragment extends Fragment implements View.OnClickListener {

    public interface Listener {
        public void setUserInfoDone(String mobile, String verifyCode, String password,
                                    String nickname, int gender);
    }

    public static RegisterSetUserInfoFragment newInstance(String mobile,
                                                          String verifyCode,
                                                          Listener listener) {
        RegisterSetUserInfoFragment instance = new RegisterSetUserInfoFragment();
        instance.mArgMobile = mobile;
        instance.mArgVerifyCode = verifyCode;
        instance.mListener = listener;
        return instance;
    }

    private String mArgMobile;
    private String mArgVerifyCode;
    private Listener mListener;

    private Activity mActivity;

    private EditText mNicknameEdit;
    private TextView mNicknameLeftText;
    private int mMaxNicknameLength;
    private EditText mPasswordEdit;
    private EditText mPasswordAgainEdit;
    private ImageView mGenderImage;
    private Button mSubmitBtn;
    private boolean mGenderMale = true;

    private final TextWatcher mNicknameTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateNicknameLeftText();
            updateSubmitBtnEnabled();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    private final TextWatcher mPasswordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateSubmitBtnEnabled();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    private final TextWatcher mPasswordAgainTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateSubmitBtnEnabled();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    private View.OnFocusChangeListener mEditFocusChangeListener
            = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            final ViewParent parent = v.getParent();
            if (parent instanceof View) {
                final View parentView = (View) parent;
                parentView.setActivated(hasFocus);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();

        Resources res = mActivity.getResources();
        mMaxNicknameLength = res.getInteger(R.integer.nickname_max_length);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_set_user_info,
                container, false);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(R.string.register_setting);

        mPasswordEdit = (EditText) view.findViewById(R.id.password);
        mPasswordEdit.setOnFocusChangeListener(mEditFocusChangeListener);
        mPasswordEdit.addTextChangedListener(mPasswordTextWatcher);
        mPasswordAgainEdit = (EditText) view.findViewById(R.id.password_again);
        mPasswordAgainEdit.addTextChangedListener(mPasswordAgainTextWatcher);
        mPasswordAgainEdit.setOnFocusChangeListener(mEditFocusChangeListener);
        mNicknameEdit = (EditText) view.findViewById(R.id.nickname_edit);
        mNicknameLeftText = (TextView) view.findViewById(R.id.nickname_left);
        mNicknameEdit.addTextChangedListener(mNicknameTextWatcher);
        mNicknameEdit.setOnFocusChangeListener(mEditFocusChangeListener);
        mGenderImage = (ImageView) view.findViewById(R.id.gender_select);
        mSubmitBtn = (Button) view.findViewById(R.id.submit);

        mGenderImage.setOnClickListener(this);
        mSubmitBtn.setOnClickListener(this);

        final InputFilter[] filters = mNicknameEdit.getFilters();
        final InputFilter lengthFilter = new ChsLengthFilter(mMaxNicknameLength);

        if (filters != null && filters.length > 0) {
            final int length = filters.length;
            InputFilter[] newFilters = new InputFilter[length + 1];
            System.arraycopy(filters, 0, newFilters, 0, length);
            newFilters[length] = lengthFilter;
            mNicknameEdit.setFilters(newFilters);
        } else {
            mNicknameEdit.setFilters(new InputFilter[]{
                    lengthFilter
            });
        }

        updateNicknameLeftText();

        updateGenderImage();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit:
                onSubmitClick();
                break;
            case R.id.gender_select:
                onGenderClick();
                break;
            default:
                break;
        }
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void onGenderClick() {
        mGenderMale = !mGenderMale;
        updateGenderImage();
    }

    private void updateGenderImage() {
        mGenderImage.setImageResource(mGenderMale ? R.drawable.input_male
                : R.drawable.input_female);
    }

    private void updateSubmitBtnEnabled() {
        boolean enable = !TextUtils.isEmpty(getNicknameString())
                && !TextUtils.isEmpty(getPasswordString())
                && !TextUtils.isEmpty(getPasswordAgainString());
        mSubmitBtn.setEnabled(enable);
    }

    private void onSubmitClick() {
        String password = getPasswordString();
        String againPassword = getPasswordAgainString();
        String nickname = getNicknameString();

        if (TextUtils.isEmpty(nickname)) {
            ShowToast.makeText(mActivity, R.drawable.emoji_smile, mActivity.getResources().getString(R.string.enter_nickname_prompt)).show();
            mNicknameEdit.requestFocus();
        } else if (TextUtils.isEmpty(password) || password.length() < 6
                || password.length() > 20) {
            ShowToast.makeText(mActivity, R.drawable.emoji_smile, mActivity.getResources().getString(R.string.enter_password_prompt)).show();
            mPasswordEdit.requestFocus();
        } else if (!password.equals(againPassword)) {
            ShowToast.makeText(mActivity, R.drawable.emoji_sad, mActivity.getResources().getString(R.string.inconformity_input_password)).show();
            mPasswordEdit.requestFocus();
        } else {
            notifySetPasswordResult(password, nickname);
        }
    }

    private String getNicknameString() {
        return mNicknameEdit.getText().toString().trim();
    }

    private String getPasswordString() {
        return mPasswordEdit.getText().toString().trim();
    }

    private String getPasswordAgainString() {
        return mPasswordAgainEdit.getText().toString().trim();
    }

    private void notifySetPasswordResult(String password, String nickname) {
        if (mListener != null) {
            mListener.setUserInfoDone(mArgMobile, mArgVerifyCode, password, nickname,
                    mGenderMale ? ConstantCode.USER_SEX_MALE : ConstantCode.USER_SEX_FEMALE);
        }
    }

    private void updateNicknameLeftText() {
        mNicknameLeftText.setText(String.valueOf(getNicknameLeft()));
    }

    private int getNicknameLeft() {
        final String text = mNicknameEdit.getText().toString();
        return mMaxNicknameLength * 2 - MiscUtils.getChsStringLength(text);
    }
}
