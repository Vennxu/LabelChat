package com.ekuater.labelchat.ui.fragment.get;


import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;


public class GetFragment extends Fragment {
    private static final int MSG_HANDLE_GET_RESULT = 101;
    private TextView mGetName;
    private ImageView mGetSex;
    public static final String GET_GAME_TIME = "time";
    public static final String GET_GAME_INFO = "info";
    public static final String GET_GAME_MUSIC = "music";
    private AvatarManager mAvatarManager;
    private CircleImageView mGetTx;
    private Activity mContext;
    private ContactsManager mContactsManager;
    private Stranger mStranger = null;


    private Handler getHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_HANDLE_GET_RESULT:
                    updateProgerss();
                    break;

            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mAvatarManager = AvatarManager.getInstance(mContext);
        mContactsManager = ContactsManager.getInstance(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        int layoutId = args.getInt(GetViewPagerActivity.VIEW_PAGER_VIEW);
        mStranger = args.getParcelable(GetViewPagerActivity.VIEW_PAGER_INFO);
        Log.i("getfragment", mStranger.toString());
        View view = inflater.inflate(layoutId, container, false);
        mGetTx = (CircleImageView) view.findViewById(R.id.img_get_tx);
        mGetSex = (ImageView) view.findViewById(R.id.img_get_sex);
        mGetName = (TextView) view.findViewById(R.id.text_get_name);
        updateProgerss();
        return view;
    }

    public void updateProgerss() {
        if (mStranger != null) {
            MiscUtils.showAvatarThumb(mAvatarManager,mStranger.getAvatarThumb(),mGetTx);
            if (mStranger.getNickname().length() > 0 && mStranger.getNickname() != null) {
                mGetName.setText(mStranger.getNickname());
            } else {
                mGetName.setText(mStranger.getLabelCode());
            }
            mGetSex.setImageResource(ConstantCode.getSexImageResource(mStranger.getSex()));
        }

    }

}