package com.ekuater.labelchat.ui.fragment.labels;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.delegate.UserLabelManager;

/**
 * @author LinYong
 */
public class LabelOptionDialogFragment extends DialogFragment implements View.OnClickListener {

    public interface LabelOptionListener{
        public void onSuccese();
    }

    public static LabelOptionDialogFragment newInstance(BaseLabel label) {
        return newInstance(null, label);
    }

    public static LabelOptionDialogFragment newInstance(DialogFragment callDialog,
                                                        BaseLabel label) {
        LabelOptionDialogFragment instance = new LabelOptionDialogFragment();
        instance.mLabel = label;
        instance.mCallDialog = callDialog;
        return instance;
    }
    public static LabelOptionDialogFragment newInstance(BaseLabel label,LabelOptionListener labelOptionListener) {
        LabelOptionDialogFragment instance = new LabelOptionDialogFragment();
        instance.mLabel = label;
        instance.mLabelOptionListener=labelOptionListener;
        return instance;
    }

    private static final int MSG_ADDING_LABEL_HOLD = 101;
    private static final int MSG_ADDING_LABEL_RESULT = 102;

    private BaseLabel mLabel;
    private DialogFragment mCallDialog;
    private Context mContext;
    private UserLabelManager mLabelManager;
    private LabelOptionListener mLabelOptionListener=null;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADDING_LABEL_HOLD:
                    break;
                case MSG_ADDING_LABEL_RESULT:
                    onAddLabelResult(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };
    private UserLabelManager.IListener mLabelListener = new UserLabelManager.AbsListener() {
        @Override
        public void onLabelAdded(int result) {
            Message msg = mHandler.obtainMessage(MSG_ADDING_LABEL_RESULT, result, 0);
            mHandler.sendMessage(msg);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        setStyle(STYLE_NO_TITLE, 0);
        mContext = activity;
        mLabelManager = UserLabelManager.getInstance(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_label_confirm_dialog, container, false);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(mLabel.getName());
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                addLabel();
                break;
            default:
                break;
        }
        dismiss();
    }

    public void show(String tag) {
        if (mCallDialog != null) {
            show(mCallDialog.getFragmentManager(), tag);
        }
    }

    private void addLabel() {
        mLabelManager.registerListener(mLabelListener);
        mLabelManager.addUserLabels(new BaseLabel[]{mLabel});
        mHandler.sendEmptyMessageDelayed(MSG_ADDING_LABEL_HOLD, 10 * 1000);
    }

    private void onAddLabelResult(int result) {
        boolean success = (result == ConstantCode.LABEL_OPERATION_SUCCESS);

        mHandler.removeMessages(MSG_ADDING_LABEL_HOLD);
        Toast.makeText(mContext,
                success ? R.string.add_label_success
                        : R.string.add_label_failure,
                Toast.LENGTH_SHORT).show();
        mLabelManager.unregisterListener(mLabelListener);
        if(mLabelOptionListener!=null) {
            if (success) {
                mLabelOptionListener.onSuccese();
            }
        }
    }
}
