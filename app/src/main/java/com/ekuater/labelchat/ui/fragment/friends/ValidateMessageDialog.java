package com.ekuater.labelchat.ui.fragment.friends;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ekuater.labelchat.R;

/**
 * Created by Leo on 2015/1/16.
 *
 * @author LinYong
 */
public class ValidateMessageDialog extends DialogFragment
        implements View.OnClickListener {

    public interface Listener {
        public void onCancel(String message);

        public void onSubmit(String message);
    }

    private interface ListenerNotifier {
        public void notify(Listener listener);
    }

    public static ValidateMessageDialog newInstance(Listener listener) {
        ValidateMessageDialog instance = new ValidateMessageDialog();
        instance.mListener = listener;
        return instance;
    }

    private Listener mListener;
    private EditText mMessageEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.validate_message_dialog, container, false);
        mMessageEdit = (EditText) view.findViewById(R.id.input_validate_message);
        view.findViewById(R.id.cancel).setOnClickListener(this);
        view.findViewById(R.id.send).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                notifyCancel();
                break;
            case R.id.send:
                notifySubmit();
                break;
            default:
                break;
        }
        dismiss();
    }

    private void notifyListener(ListenerNotifier notifier) {
        if (mListener != null) {
            notifier.notify(mListener);
        }
    }

    private String getEditMessage() {
        return mMessageEdit.getText().toString().trim();
    }

    private void notifyCancel() {
        notifyListener(new CancelNotifier(getEditMessage()));
    }

    private void notifySubmit() {
        notifyListener(new SubmitNotifier(getEditMessage()));
    }

    private static class CancelNotifier implements ListenerNotifier {

        private final String message;

        public CancelNotifier(String message) {
            this.message = message;
        }

        @Override
        public void notify(Listener listener) {
            listener.onCancel(message);
        }
    }

    private static class SubmitNotifier implements ListenerNotifier {

        private final String message;

        public SubmitNotifier(String message) {
            this.message = message;
        }

        @Override
        public void notify(Listener listener) {
            listener.onSubmit(message);
        }
    }
}
