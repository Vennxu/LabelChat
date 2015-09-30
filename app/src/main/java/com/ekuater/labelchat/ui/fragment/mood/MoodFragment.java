package com.ekuater.labelchat.ui.fragment.mood;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.MoodUser;
import com.ekuater.labelchat.datastruct.UserGroup;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.MoodManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.ui.widget.FlowLayout;
import com.ekuater.labelchat.util.TextUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/5/8.
 */
public class MoodFragment extends Fragment {

    private Activity activity;
    private TextView mSelectText;
    private FlowLayout mMoodUserList;
    private Button mMoodSend;
    private RelativeLayout mCustomParent;
    private TextView mCustomNum;
    private LayoutInflater mInflater;
    private AvatarManager mAvatarManager;
    private MoodManager mMoodManager;
    private InputMethodManager mInputMethodManager;
    private ArrayList<UserGroup> userGroups = null;
    private ArrayList<MoodUser> moodUsers = null;

    private FlowLayout mEmojiList;
    private EditText mEmojiCustom;
    private String content;



    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MoodUtils.MOOD_SEND_RESULT:
                    if (activity == null) {
                        return;
                    }
                    if (msg.arg1 == MoodManager.QUERY_RESULT_SUCCESS) {
                        ShowToast.makeText(activity, R.drawable.emoji_smile, activity.getString(R.string.mood_send_succese)).show();
                    } else {
                        ShowToast.makeText(activity, R.drawable.emoji_cry, getString(R.string.mood_send_failed)).show();
                    }
                    break;
            }
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.mood_send:
                    if (moodUsers == null && moodUsers.size() == 0 && TextUtil.isEmpty(content)) {
                        return;
                    }
                    if (moodUsers != null && moodUsers.size() > 0 && !TextUtil.isEmpty(content)) {
                        moodSend();
                        activity.finish();
                    } else if (moodUsers == null || moodUsers.size() == 0) {
                        ShowToast.makeText(activity, R.drawable.emoji_cry, getString(R.string.mood_select_other_user)).show();
                    } else if (TextUtil.isEmpty(content)) {
                        ShowToast.makeText(activity, R.drawable.emoji_smile, getString(R.string.mood_select_mood)).show();
                    }
                    break;
                case R.id.emoji_custom:
                    content = mEmojiCustom.getText().toString();
                    mCustomParent.setBackgroundResource(R.drawable.mood_emoji_coner_select);
                    mEmojiCustom.setTextColor(Color.WHITE);
                    mEmojiCustom.setHintTextColor(Color.WHITE);
                    updateTextContent();
                    isSend();
                    break;
                default:
                    break;
            }
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0){
                content = s.toString();
                mCustomNum.setText(getActivity().getResources().getString(R.string.emoji_num, content.length()));
            }else{
                content = null;
                mCustomNum.setText(getActivity().getResources().getString(R.string.emoji_num, 0));
            }
            isSend();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        moodUsers = new ArrayList<>();
        mAvatarManager = AvatarManager.getInstance(activity);
        mMoodManager = MoodManager.getInstance(activity);
        mInputMethodManager = (InputMethodManager) activity.getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.softInputMode |= WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_mood, container, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        mSelectText = (TextView) view.findViewById(R.id.mood_select_text);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        title.setText(getString(R.string.mood));
        mMoodUserList = (FlowLayout) view.findViewById(R.id.mood_select_user);
        mEmojiList = (FlowLayout) view.findViewById(R.id.emoji_list);
        mMoodSend = (Button) view.findViewById(R.id.mood_send);
        mEmojiCustom = (EditText) view.findViewById(R.id.emoji_custom);
        mCustomNum = (TextView) view.findViewById(R.id.emoji_custom_num);
        mCustomParent = (RelativeLayout) view.findViewById(R.id.emoji_custom_parent);
        mEmojiCustom.setOnClickListener(mOnClickListener);
        mEmojiCustom.addTextChangedListener(mTextWatcher);
        mMoodSend.setOnClickListener(mOnClickListener);
        mMoodUserList.setHorizontalGap(20);
        mMoodUserList.setVerticalGap(20);
        mEmojiList.setHorizontalGap(20);
        mEmojiList.setVerticalGap(20);
        mSelectText.setText(getString(R.string.mood_select_user, 0, 20));
        getChildView(inflater);
        getUserView();
        return view;
    }

    private TextView text;

    private void getChildView(LayoutInflater inflater) {
        int[] moodEmojis = MoodUtils.getEmojiArray(getActivity(), R.array.mood_emoji);
        String[] moodText = getResources().getStringArray(R.array.mood_text);
        String[] moodConten = getResources().getStringArray(R.array.mood_text_content);
        for (int i = 0; i < moodText.length; i++) {
            View rootView = inflater.inflate(R.layout.mood_list_item, mEmojiList, false);
            TextView textView = (TextView) rootView.findViewById(R.id.mood_item_text);
            Drawable drawables = getResources().getDrawable(moodEmojis[i]);
            drawables.setBounds(0, 0, drawables.getIntrinsicWidth(), drawables.getIntrinsicHeight());
            textView.setCompoundDrawables(drawables, null, null, null);
            textView.setTag(moodConten[i]);
            textView.setText(moodText[i]);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (text != null) {
                        text.setBackgroundResource(R.drawable.mood_emoji_coner);
                        text.setTextColor(getActivity().getResources().getColor(R.color.mood_emoji_radio_text));
                    }
                    text = (TextView) v;
                    text.getTag();
                    String prefix = "";
                    if (text.getTag() != null) {
                        prefix = text.getTag().toString();
                        content = prefix + text.getText().toString();
                    }
                    text.setBackgroundResource(R.drawable.mood_emoji_coner_select);
                    text.setTextColor(Color.WHITE);
                    isSend();
                    updateCustomContent();
                }
            });
            mEmojiList.addView(rootView);
        }
    }

    private void getUserView() {
        mMoodUserList.removeAllViews();
        for (int i = 0; i < moodUsers.size() + 1; i++) {
            CircleImageView rootView = (CircleImageView) mInflater.inflate(R.layout.mood_user_item, mMoodUserList, false).findViewById(R.id.mood_user_tx);
            if (i == moodUsers.size()) {
                rootView.setImageResource(R.drawable.ic_mood_add_people);
                rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UILauncher.launchMoodUserListUI(MoodFragment.this, userGroups, moodUsers, MoodUtils.MOOD_USER_LIST_CODE);
                    }
                });
            } else {
                MiscUtils.showAvatarThumb(mAvatarManager, moodUsers.get(i).getAvatarThumb(), rootView, R.drawable.contact_single);
            }
            mMoodUserList.addView(rootView);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MoodUtils.MOOD_USER_LIST_CODE && resultCode == Activity.RESULT_OK) {
            moodUsers.clear();
            moodUsers = data.getParcelableArrayListExtra(MoodUtils.USERIDS);
            userGroups = data.getParcelableArrayListExtra(MoodUtils.USER_GROUP);
            mSelectText.setText(getString(R.string.mood_select_user, moodUsers.size(), 20));
            isSend();
            getUserView();
        }
    }

    private void isSend() {
        if (moodUsers != null && moodUsers.size() > 0 && !TextUtil.isEmpty(content)) {
            mMoodSend.setEnabled(true);
        } else {
            mMoodSend.setEnabled(false);
        }
    }

    private void updateCustomContent() {
        mInputMethodManager.hideSoftInputFromWindow(mEmojiCustom.getWindowToken(), 0);
        mCustomParent.setBackgroundResource(R.drawable.mood_emoji_coner);
        mEmojiCustom.setTextColor(getActivity().getResources().getColor(R.color.mood_emoji_radio_text));
        mEmojiCustom.setHintTextColor(getActivity().getResources().getColor(R.color.mood_emoji_radio_text));
    }

    private void updateTextContent() {
        if (text != null) {
            text.setBackgroundResource(R.drawable.mood_emoji_coner);
            text.setTextColor(getActivity().getResources().getColor(R.color.mood_emoji_radio_text));
        }
    }

    private void moodSend() {
        mMoodManager.moodSend(content, moodUsers, new MoodManager.MoodSendObserver() {
            @Override
            public void onQueryResult(int result) {
                Message message = handler.obtainMessage(MoodUtils.MOOD_SEND_RESULT, result, 0);
                handler.sendMessage(message);
            }
        });
    }
}
