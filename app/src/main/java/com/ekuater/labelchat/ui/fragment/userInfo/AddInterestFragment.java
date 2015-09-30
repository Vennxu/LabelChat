package com.ekuater.labelchat.ui.fragment.userInfo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.InterestType;
import com.ekuater.labelchat.datastruct.InterestTypeProperty;
import com.ekuater.labelchat.datastruct.UserInterest;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.InterestManager;
import com.ekuater.labelchat.ui.util.CompatUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.widget.FlowLayout;
import com.ekuater.labelchat.util.InterestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2015/3/25.
 *
 * @author XuWenxiang
 */
public class AddInterestFragment extends Activity {

    private InterestType mInterestType;
    private UserInterest[] mUserInterest;
    private FlowLayout flowLayout;
    private EditText editText;
    private TextView postText;
    private TextView numText;
    private LayoutInflater layoutInflater;
    private InterestManager mInterestManager;

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            numText.setText((12 - s.length()) + "");
            if (s.length() == 0) {
                postText.setEnabled(false);
            } else {
                postText.setEnabled(true);
            }
        }
    };

    private void setInterest() {
        List<String> userIds = null;
        String userInterestName = null;
        if (mUserInterest != null && mInterestType.getUserInterests() == null) {
            userIds = null;
        } else if (mUserInterest == null && mInterestType.getUserInterests() != null
                && mInterestType.getUserInterests().length > 0) {
            userInterestName = mInterestType.getUserInterests()[0].getInterestName();
            userIds = ContactsManager.getInstance(this).getUserIds(this);
        } else if (mUserInterest != null && mInterestType.getUserInterests() != null) {
            List<UserInterest> userInterests = Arrays.asList(mUserInterest);
            List<UserInterest> tmpUserInterest = Arrays.asList(mInterestType.getUserInterests());
            ArrayList<UserInterest> aUserInterests = new ArrayList<>();
            ArrayList<UserInterest> aTmpUserInterests = new ArrayList<>();
            aUserInterests.addAll(userInterests);
            aTmpUserInterests.addAll(tmpUserInterest);
            aTmpUserInterests.removeAll(aUserInterests);
            if (aTmpUserInterests.size() > 0) {
                userInterestName = aTmpUserInterests.get(0).getInterestName();
            }
        }
        mInterestManager.setUserInterest(mInterestType, userIds, userInterestName);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_add_interest);
        layoutInflater = LayoutInflater.from(this);
        mInterestManager = InterestManager.getInstance(this);
        parseArguments();
        initView();
    }

    public void initView() {
        ImageView icon = (ImageView) findViewById(R.id.icon);
        TextView title = (TextView) findViewById(R.id.title);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setInterest();
                Intent intent = new Intent();
                intent.putExtra(InterestFragment.INTEREST_TYPE, mInterestType);
                setResult(RESULT_OK, intent);
                AddInterestFragment.this.finish();
            }
        });
        title.setText(getString(R.string.input) + mInterestType.getTypeName());
        flowLayout = (FlowLayout) findViewById(R.id.add_interest_content);
        flowLayout.setHorizontalGap(20);
        flowLayout.setVerticalGap(20);
        editText = (EditText) findViewById(R.id.add_interest_edit);
        editText.addTextChangedListener(textWatcher);
        postText = (TextView) findViewById(R.id.add_interest_post);
        numText = (TextView) findViewById(R.id.add_interest_num);
        postText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = editText.getText().toString();
                if (!TextUtils.isEmpty(content)) {
                    ArrayList<UserInterest> userInterests = new ArrayList<>();
                    if (mInterestType.getUserInterests() != null && mInterestType.getUserInterests().length > 0) {
                        userInterests.addAll(Arrays.asList(mInterestType.getUserInterests()));
                        for (int i = 0; i < userInterests.size(); i++) {
                            if (content.equals(userInterests.get(i).getInterestName())) {
                                ShowToast.makeText(AddInterestFragment.this, R.drawable.emoji_sad,
                                        getString(R.string.exit_interest)).show();
                                return;
                            }
                        }
                    }
                    UserInterest userInterest = new UserInterest();
                    userInterest.setInterestName(content);
                    userInterest.setInterestType(mInterestType.getTypeId());
                    userInterests.add(userInterest);
                    final int size = userInterests.size();
                    mInterestType.setUserInterests(userInterests.toArray(new UserInterest[size]));
                    addInterestView(userInterest);
                    editText.setText("");
                }
            }
        });
        initDate();
    }

    private void parseArguments() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mInterestType = bundle.getParcelable(InterestFragment.INTEREST_TYPE);
            if (mInterestType != null) {
                mUserInterest = mInterestType.getUserInterests();
            }
        }
    }

    private void initDate() {
        UserInterest[] interests = mInterestType != null ? mInterestType.getUserInterests() : null;
        if (interests == null || interests.length <= 0) {
            return;
        }

        for (UserInterest interest : interests) {
            addInterestView(interest);
        }
    }

    private void addInterestView(final UserInterest interest) {
        final View view = layoutInflater.inflate(R.layout.interest_add_flow_item, flowLayout, false);
        LinearLayout parent = (LinearLayout) view.findViewById(R.id.flow_item_parent);
        TextView name = (TextView) view.findViewById(R.id.flow_item_name);
        ImageView delete = (ImageView) view.findViewById(R.id.flow_item_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<UserInterest> userInterests = new ArrayList<>();
                userInterests.addAll(Arrays.asList(mInterestType.getUserInterests()));
                userInterests.remove(interest);
                final int size = userInterests.size();
                mInterestType.setUserInterests(size == 0 ? null
                        : userInterests.toArray(new UserInterest[size]));
                flowLayout.removeView(view);
            }
        });
        InterestTypeProperty property = InterestUtils.getTypeProperty(mInterestType.getTypeId());
        Resources res = getResources();
        GradientDrawable interestDrawable = (GradientDrawable)
                res.getDrawable(R.drawable.interest_bg);

        if (interestDrawable != null) {
            interestDrawable.setColor(res.getColor(property.getItemBgResId()));
            CompatUtils.setBackground(parent, interestDrawable);
        } else {
            parent.setBackgroundColor(property.getItemBgResId());
        }
        name.setTextColor(res.getColor(property.getItemColorResId()));
        name.setText(interest.getInterestName());
        flowLayout.addView(view);
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            setInterest();
            Intent intent = new Intent();
            intent.putExtra(InterestFragment.INTEREST_TYPE, mInterestType);
            setResult(RESULT_OK, intent);
            finish();
        }
        return true;
    }
}
