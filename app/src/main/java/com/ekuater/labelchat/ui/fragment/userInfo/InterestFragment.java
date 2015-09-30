package com.ekuater.labelchat.ui.fragment.userInfo;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.InterestType;
import com.ekuater.labelchat.datastruct.UserInterest;
import com.ekuater.labelchat.delegate.InterestManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.labelstory.LabelStoryUtils;
import com.ekuater.labelchat.ui.widget.FlowLayout;
import com.ekuater.labelchat.util.InterestUtils;

/**
 * Created by Administrator on 2015/3/24.
 *
 * @author XuWenXiang
 */
public class InterestFragment extends Fragment implements Handler.Callback {

    public static final String INTEREST_TYPE = "interest_type";

    private static final int MSG_GET_INTEREST = 101;

    private static final int REQUEST_ADD_INTEREST = 10;

    private InterestManager mInterestManager;
    private Activity activity;
    private InterestType[] mInterestTypes;
    private String queryUserId;
    private LinearLayout linearLayout;
    private ImageView loading;

    private Handler mHandler = new Handler(this);

    private void onHandlerInterest(Message msg) {
        stopAnimation();
        if (msg.arg1 == ConstantCode.EXECUTE_RESULT_SUCCESS) {
            mInterestTypes = (InterestType[]) msg.obj;
            if (mInterestTypes != null) {
                if (activity == null) {
                    return;
                }
                linearLayout.addView(getChildView());
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        parseArguments();
        mInterestManager = InterestManager.getInstance(activity);
    }

    public void parseArguments() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            queryUserId = bundle.getString(LabelStoryUtils.LABEL_STORY_USER_ID);
        }
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void startAnimation() {
        loading.setVisibility(View.VISIBLE);
        Drawable drawable = loading.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.start();
    }

    private void stopAnimation() {
        if (loading != null) {
            loading.setVisibility(View.GONE);
            Drawable drawable = loading.getDrawable();
            AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
            animationDrawable.stop();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interest, container, false);
        linearLayout = (LinearLayout) view.findViewById(R.id.interest_lists);
        loading = (ImageView) view.findViewById(R.id.interest_loading);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView title = (TextView) view.findViewById(R.id.title);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        title.setText(R.string.my_interest);
        getInterestArray();
        return view;
    }

    private View getChildView() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        LinearLayout interest = new LinearLayout(activity);
        interest.setOrientation(LinearLayout.VERTICAL);
        interest.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

        for (final InterestType interestType : mInterestTypes) {
            if (interestType == null) {
                continue;
            }

            @SuppressLint("InflateParams")
            View labelView = inflater.inflate(R.layout.interest_fragment_item, null, false);
            View movie_parent = labelView.findViewById(R.id.interest_item_parent);
            TextView movie_favourite = (TextView) labelView.findViewById(R.id.interest_item_favourite);
            ImageView movie_icon = (ImageView) labelView.findViewById(R.id.interest_item_icon);
            LinearLayout movie_null = (LinearLayout) labelView.findViewById(R.id.interest_item_null);
            FlowLayout movie = (FlowLayout) labelView.findViewById(R.id.interest_item_content);
            movie_icon.setImageResource(InterestUtils.getInterestTypeIcon(interestType.getTypeId()));
            movie.setHorizontalGap(20);
            movie.setVerticalGap(20);
            movie_favourite.setText(getString(R.string.input_type, interestType.getTypeName()));
            movie_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UILauncher.launchAddInterestUI(InterestFragment.this, activity,
                            REQUEST_ADD_INTEREST, interestType);
                }
            });

            UserInterest[] userInterest = interestType.getUserInterests();
            if (userInterest != null && userInterest.length > 0) {
                for (UserInterest movieType : userInterest) {
                    TextView movieName = (TextView) inflater.inflate(R.layout.interest_name, movie, false);
                    InterestUtils.setInterestColor(activity, movieName, interestType.getTypeId());
                    movieName.setText(movieType.getInterestName());
                    movie.addView(movieName);
                }
                interest.addView(labelView);
            } else {
                movie_null.setVisibility(View.VISIBLE);
                interest.addView(labelView);
            }
        }
        return interest;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ADD_INTEREST:
                onReInitDate(resultCode, data);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_GET_INTEREST:
                onHandlerInterest(msg);
                break;
            default:
                handled = false;
        }
        return handled;
    }

    private void onReInitDate(int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK && intent != null) {
            InterestType interestType = intent.getParcelableExtra(INTEREST_TYPE);
            if (interestType != null) {
                InterestType oldType = findInterestType(interestType.getTypeId());
                if (oldType != null) {
                    oldType.setUserInterests(interestType.getUserInterests());
                    linearLayout.removeAllViews();
                    linearLayout.addView(getChildView());
                }
            }
        }
    }

    private InterestType findInterestType(int typeId) {
        for (InterestType tempType : mInterestTypes) {
            if (tempType != null && tempType.getTypeId() == typeId) {
                return tempType;
            }
        }
        return null;
    }

    private void getInterestArray() {
        startAnimation();
        InterestManager.UserInterestObserver observer = new InterestManager.UserInterestObserver() {
            @Override
            public void onQueryResult(int result, InterestType[] interestTypes) {
                mHandler.obtainMessage(MSG_GET_INTEREST, result,
                        0, interestTypes).sendToTarget();
            }
        };
        mInterestManager.getUserInterest(queryUserId, observer);
    }
}
