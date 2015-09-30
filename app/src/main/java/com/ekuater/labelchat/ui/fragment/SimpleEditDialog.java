package com.ekuater.labelchat.ui.fragment;

import android.app.Dialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ChsLengthFilter;

/**
 * @author LinYong
 */
public class SimpleEditDialog extends DialogFragment
        implements View.OnClickListener, TextWatcher {

    public interface IListener {
        public void onCancel(CharSequence text);

        public void onOK(CharSequence text);
    }

    public static final class UiConfig {

        public String title;
        public String initText;
        public String editHint;
        public int maxLength;
        public int inputType;
        public int minLines;
        public boolean gravityCenter;
        public boolean showLeftCountHint;
        public boolean cancelable;
        public boolean isNicknameInput;
        public boolean isChangerEditHeight;
        public IListener listener;

        public UiConfig() {
            title = null;
            initText = null;
            editHint = null;
            maxLength = -1;
            inputType = InputType.TYPE_CLASS_TEXT;
            minLines = 1;
            gravityCenter = false;
            showLeftCountHint = true;
            cancelable = true;
            isNicknameInput = false;
            listener = null;
        }
    }

    public static SimpleEditDialog newInstance(UiConfig config) {
        final SimpleEditDialog instance = new SimpleEditDialog();
        instance.applyConfig(config);
        return instance;
    }

    private String mTitle;
    private String mInitText;
    private String mEditHint;
    private int mMaxLength;
    private int mInputType;
    private int mMinLines;
    private boolean mGravityCenter;
    private boolean mShowLeftCountHint;
    private boolean mCancelable;
    private boolean mIsNicknameInput;
    private boolean mIsChangerEditHeight = false;
    private IListener mListener;
    private EditText mEditText;
    private Rect mEditTextPadding;
    private TextView mMaxLeftText;

    public SimpleEditDialog() {
        setStyle(STYLE_NO_TITLE, 0);
        mShowLeftCountHint = true;
    }

    private void applyConfig(UiConfig config) {
        mTitle = config.title;
        mInitText = config.initText;
        mEditHint = config.editHint;
        mMaxLength = config.maxLength;
        mInputType = config.inputType;
        mMinLines = config.minLines;
        mGravityCenter = config.gravityCenter;
        mShowLeftCountHint = config.showLeftCountHint;
        mCancelable = config.cancelable;
        mIsNicknameInput = config.isNicknameInput;
        mIsChangerEditHeight = config.isChangerEditHeight;
        mListener = config.listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_edit_dialog, container, false);
        TextView titleView = (TextView) view.findViewById(R.id.title_header);
        titleView.setText(mTitle);
        mMaxLeftText = (TextView) view.findViewById(R.id.max_left_count);
        mEditText = (EditText) view.findViewById(R.id.edit);
        if(mIsChangerEditHeight){
            RelativeLayout.LayoutParams layoutParams= (RelativeLayout.LayoutParams) mEditText.getLayoutParams();
            layoutParams.height=200;
        }
        mEditText.setText(mInitText);
        mEditText.setHint(mEditHint);
        mEditText.setInputType(mInputType);
        if (mMaxLength > 0) {
            final InputFilter[] filters = mEditText.getFilters();
            final InputFilter lengthFilter = mIsNicknameInput
                    ? new ChsLengthFilter(mMaxLength)
                    : new InputFilter.LengthFilter(mMaxLength);

            if (filters != null && filters.length > 0) {
                final int length = filters.length;
                InputFilter[] newFilters = new InputFilter[length + 1];
                System.arraycopy(filters, 0, newFilters, 0, length);
                newFilters[length] = lengthFilter;
                mEditText.setFilters(newFilters);
            } else {
                mEditText.setFilters(new InputFilter[]{
                        lengthFilter
                });
            }
            if (!mShowLeftCountHint) {
                mMaxLeftText.setVisibility(View.GONE);
            }
        } else {
            mMaxLeftText.setVisibility(View.GONE);
        }
        if (mMinLines > 1) {
            mEditText.setMinLines(mMinLines);
            mEditText.setGravity(Gravity.TOP | Gravity.LEFT);
            mEditText.setSingleLine(false);
        } else {
            mEditText.setSingleLine(true);
            if (mGravityCenter) {
                mEditText.setGravity(Gravity.CENTER);
            }
        }

        Spannable spannable = mEditText.getText();
        if (spannable != null) {
            Selection.setSelection(spannable, 0, spannable.length());
        }

        mEditText.addTextChangedListener(this);

        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);

        mEditTextPadding = new Rect(mEditText.getPaddingLeft(), mEditText.getPaddingTop(),
                mEditText.getPaddingRight(), mEditText.getPaddingBottom());
        updateLeftCountText();

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (!mCancelable) {
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }
        return dialog;
    }

    @Override
    public void onClick(View v) {
        final CharSequence text = mEditText.getText();

        switch (v.getId()) {
            case R.id.btn_cancel:
                dismiss();
                if (mListener != null) {
                    mListener.onCancel(text);
                }
                break;
            case R.id.btn_ok:
                if (mListener != null) {
                    mListener.onOK(text);
                }
                dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        updateLeftCountText();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    private void updateLeftCountText() {
        if (mMaxLeftText.getVisibility() == View.GONE) {
            return;
        }

        mMaxLeftText.setText(String.valueOf(getCountLeft()));
        mEditText.setPadding(
                mEditTextPadding.left,
                mEditTextPadding.top,
                Math.max(mEditTextPadding.right,
                        mMaxLeftText.getMeasuredWidth()),
                mEditTextPadding.bottom);
    }

    private int getCountLeft() {
        final String text = mEditText.getText().toString();

        if (mIsNicknameInput) {
            return mMaxLength * 2 - MiscUtils.getChsStringLength(text);
        } else {
            return TextUtils.isEmpty(text) ? mMaxLength : (mMaxLength - text.length());
        }
    }
}
