package com.ekuater.labelchat.ui.fragment.labels;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.delegate.UserLabelManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author FanChong
 */
public class AddLabelDialogFrament extends DialogFragment implements View.OnClickListener {

    public static final String EXTRA_LABEL = "label";
    private BaseLabel[] mLabel;


    private Context mContext;

    public static AddLabelDialogFrament newInstance(BaseLabel[] labels, IConfirmListener listener) {
        AddLabelDialogFrament instance = new AddLabelDialogFrament();
        Bundle arguments = new Bundle();
        arguments.putParcelableArray(EXTRA_LABEL, labels);
        instance.setArguments(arguments);
        instance.mConfirmListener = listener;
        return instance;
    }

    private IConfirmListener mConfirmListener;

    public interface IConfirmListener {
        public void onCancel();

        public void onConfirm();
    }

    public static class AbsConfirmListener implements IConfirmListener {

        @Override
        public void onCancel() {

        }

        @Override
        public void onConfirm() {

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setStyle(STYLE_NO_TITLE, 0);
        parseArguments();
    }

    private void parseArguments() {
        Bundle args = getArguments();
        mLabel = null;
        if (args != null) {
            Parcelable[] parcelables = args.getParcelableArray(EXTRA_LABEL);
            if (parcelables != null && parcelables.length > 0) {
                mLabel = new BaseLabel[parcelables.length];
                for (int i = 0; i < parcelables.length; ++i) {
                    mLabel[i] = (BaseLabel) parcelables[i];
                }
            }
        }
        if (TextUtils.isEmpty(mLabel.toString())) {
            dismiss();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_label_dialog, container, false);
        TextView labelView = (TextView) view.findViewById(R.id.label);
        List<String> labelList = new ArrayList<String>();
        for (BaseLabel labelName : mLabel) {
            labelList.add(labelName.getName());
        }
        if (labelList != null && labelList.size() > 0) {
            StringBuilder sb = new StringBuilder();
            String wordSeparator = getString(R.string.word_separator);

            for (int i = 0; i <= labelList.size() - 2; ++i) {
                sb.append(labelList.get(i));
                sb.append(wordSeparator);
            }
            sb.append(labelList.get(labelList.size() - 1));
            labelView.setText(sb.toString());
        }
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                if (mConfirmListener != null) {
                    mConfirmListener.onConfirm();
                }

                break;
            default:
                break;
        }
        dismiss();
    }

}
