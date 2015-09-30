package com.ekuater.labelchat.ui.fragment.main;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.UserLabel;

/**
 * @author LinYong
 */
public class LabelShowOperationDialog  extends DialogFragment {

    public static final String ARG_USER_LABEL = "UserLabel";

    public interface IOperationListener {
        public void onModify(UserLabel label);

        public void onDelete(UserLabel label);

        public void onSearchFriend(UserLabel label);

        public void onViewRelativeLabel(UserLabel label);
    }

    public static LabelShowOperationDialog newInstance(UserLabel label,
                                                       IOperationListener listener) {
        LabelShowOperationDialog instance = new LabelShowOperationDialog();
        Bundle bundle = new Bundle();

        bundle.putParcelable(ARG_USER_LABEL, label);
        instance.setArguments(bundle);
        instance.setOperationListener(listener);
        instance.setStyle(STYLE_NO_TITLE, 0);

        return instance;
    }

    private UserLabel mUserLabel;
    private IOperationListener mOperationListener;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();

            switch (v.getId()) {
                case R.id.modify:
                    if (mOperationListener != null) {
                        mOperationListener.onModify(mUserLabel);
                    }
                    break;
                case R.id.delete:
                    if (mOperationListener != null) {
                        mOperationListener.onDelete(mUserLabel);
                    }
                    break;
                case R.id.search_friend:
                    if (mOperationListener != null) {
                        mOperationListener.onSearchFriend(mUserLabel);
                    }
                    break;
                case R.id.relative_label:
                    if (mOperationListener != null) {
                        mOperationListener.onViewRelativeLabel(mUserLabel);
                    }
                    break;
                case R.id.close:
                    break;
                default:
                    break;
            }
        }
    };

    public void setOperationListener(IOperationListener listener) {
        mOperationListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

        if (bundle != null) {
            mUserLabel = bundle.getParcelable(ARG_USER_LABEL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_label_show_operation_dialog,
                container, false);
        TextView headerText = (TextView) view.findViewById(R.id.title_header);
        headerText.setText(mUserLabel.getName());
        view.findViewById(R.id.modify).setOnClickListener(mOnClickListener);
        view.findViewById(R.id.delete).setOnClickListener(mOnClickListener);
        view.findViewById(R.id.search_friend).setOnClickListener(mOnClickListener);
        view.findViewById(R.id.relative_label).setOnClickListener(mOnClickListener);
        view.findViewById(R.id.close).setOnClickListener(mOnClickListener);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
