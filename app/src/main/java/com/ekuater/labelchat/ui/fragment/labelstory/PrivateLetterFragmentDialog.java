package com.ekuater.labelchat.ui.fragment.labelstory;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;

import org.w3c.dom.Text;

/**
 * Created by Label on 2015/3/12.
 */
public class PrivateLetterFragmentDialog extends DialogFragment {


    private EditText mLetterEidtText;
    private TextView mLetterCancel;
    private TextView mLetterOk;
    private CircleImageView mLetterTx;
    private TextView mLetterName;
    private OnSendEmaileClicklistener onSendEmaileClicklistener = null;
    private String labelStoryId;
    private String imageTxUrl;
    private String name;
    private AvatarManager avatarManager;
    private String mUserId;
    private String message;
    private int position;

    public interface OnSendEmaileClicklistener {
        public void onSendEmaile(String labelStoryId, String userId, String message, int position);
    }

    public static PrivateLetterFragmentDialog newInstance(String labelStoryId, String imageTxUrl, String userId, String name, AvatarManager avatarManager, int position, OnSendEmaileClicklistener onSendEmaileClicklistener) {
        PrivateLetterFragmentDialog instantce = new PrivateLetterFragmentDialog();
        instantce.onSendEmaileClicklistener = onSendEmaileClicklistener;
        instantce.labelStoryId = labelStoryId;
        instantce.imageTxUrl = imageTxUrl;
        instantce.mUserId = userId;
        instantce.position = position;
        instantce.name = name;
        instantce.avatarManager = avatarManager;
        instantce.setStyle(STYLE_NO_TITLE, R.style.Letter);
        return instantce;
    }

    public PrivateLetterFragmentDialog() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_private_letter, container, false);
        mLetterEidtText = (EditText) view.findViewById(R.id.letter_edit);
        mLetterCancel = (TextView) view.findViewById(R.id.letter_btn_cancel);
        mLetterOk = (TextView) view.findViewById(R.id.letter_btn_ok);
        mLetterTx = (CircleImageView) view.findViewById(R.id.letter_tx);
        mLetterName = (TextView) view.findViewById(R.id.letter_name);
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.softInputMode |= WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
        String title = MiscUtils.getUserRemarkName(getActivity(), mUserId);
        mLetterName.setText(title!=null&&title.length()>0?title:name);
        MiscUtils.showAvatarThumb(avatarManager, imageTxUrl, mLetterTx, R.drawable.contact_single);
        mLetterOk.setOnClickListener(onClickListener);
        mLetterCancel.setOnClickListener(onClickListener);
        return view;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.letter_btn_ok:
                    message = mLetterEidtText.getText().toString();
                    if (!TextUtils.isEmpty(message)) {
                        onSendEmaileClicklistener.onSendEmaile(labelStoryId, mUserId, message, position);
                    }
                    dismiss();
                    break;

                case R.id.letter_btn_cancel:

                    dismiss();

                    break;

                default:
                    break;
            }
        }
    };
}
