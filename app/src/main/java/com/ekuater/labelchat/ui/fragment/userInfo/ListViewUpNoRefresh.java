package com.ekuater.labelchat.ui.fragment.userInfo;


import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.datastruct.PersonalUser;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.FollowingManager;

public class ListViewUpNoRefresh extends ListView {

    int lastVisibleItemIndex;
    private View footer;
    private int headerHeight;
    private static final int PULL_TO_REFRESH = 0;
    private static final int RELEASE_TO_REFERESH = 1;
    private static final int REFERESHING = 2;
    private static final int DONE = 3;
    private static final float RATIO = 3;
    private static boolean isBack = false;
    private boolean refereshEnable;// 是否可以进行刷新
    private int state;// 当前刷新状态
    boolean isRecorded;
    float startY;
    float firstTempY = 0;
    float secondTempY = 0;

    public ListViewUpNoRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ListViewUpNoRefresh(Context context) {
        super(context);
        init(context);
    }

    @SuppressLint("InflateParams")
    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        footer = inflater.inflate(R.layout.recently_visit_user_count, null);
        measureView(footer);
        headerHeight = footer.getMeasuredHeight();
        footer.setPadding(0, -1 * headerHeight, 0, 0);// 设置与界面上边距的距离
        footer.invalidate();// 控件重绘
        footer.setVisibility(View.INVISIBLE);
        addFooterView(footer);

        state = DONE;
        refereshEnable = true;
    }

    private void measureView(View v) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int measureWidth = ViewGroup.getChildMeasureSpec(0, 0, lp.width);
        int measureHeight;
        if (lp.height > 0) {
            measureHeight = MeasureSpec.makeMeasureSpec(lp.height,
                    MeasureSpec.EXACTLY);
        } else {
            measureHeight = MeasureSpec.makeMeasureSpec(lp.height,
                    MeasureSpec.UNSPECIFIED);
        }
        v.measure(measureWidth, measureHeight);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        lastVisibleItemIndex = getLastVisiblePosition() - 1;// 因为加有一尾视图，所以这里要咸一
        int totalCounts = getCount() - 1;// 因为给listview加了一头一尾）视图所以这里要减二
        if (refereshEnable) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    firstTempY = ev.getY();
                    isRecorded = false;
                    if (getFirstVisiblePosition() == 0) {
                        if (!isRecorded) {
                            startY = ev.getY();
                            isRecorded = true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:

                    if (getLastVisiblePosition() == getCount() - 2
                            || getLastVisiblePosition() == getCount() - 1) {

                        firstTempY = secondTempY;
                        secondTempY = ev.getY();
                        if (!isRecorded) {
                            startY = secondTempY;
                            isRecorded = true;
                        }

                        if (state != REFERESHING) {// 不是正在刷新状态
                            if (state == DONE) {
                                if (startY - secondTempY > 0) {
                                    // 刷新完成/初始状态 --》 进入 下拉刷新
                                    state = PULL_TO_REFRESH;
                                    onFooterStateChange();
                                }
                            }
                            if (state == PULL_TO_REFRESH) {
                                if ((startY - secondTempY) / RATIO > headerHeight
                                        && firstTempY - secondTempY >= 9) {
                                    // 上拉刷新 --》 松开刷新
                                    state = RELEASE_TO_REFERESH;
                                    onFooterStateChange();
                                } else if (startY - secondTempY <= 0) {
                                    // 上拉刷新 --》 回到 刷新完成
                                    state = DONE;
                                    onFooterStateChange();
                                }
                            }
                            if (state == RELEASE_TO_REFERESH) {
                                if (firstTempY - secondTempY < -5) {
                                    state = PULL_TO_REFRESH;
                                    isBack = true;// 从松开刷新 回到的上拉刷新
                                    onFooterStateChange();
                                } else if (secondTempY - startY >= 0) {
                                    // 松开刷新 --》 回到 刷新完成
                                    state = DONE;
                                    onFooterStateChange();
                                }
                            }
                            if ((state == PULL_TO_REFRESH || state == RELEASE_TO_REFERESH)
                                    && secondTempY < startY) {
                                footer.setPadding(
                                        0,
                                        0,
                                        0,
                                        (int) ((startY - secondTempY) / RATIO - headerHeight));
                            }
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    System.out.println("state=" + state);
                    footer.setVisibility(View.GONE);
                    if (state != REFERESHING) {
                        if (state == PULL_TO_REFRESH) {
                            state = DONE;
                            if (getLastVisiblePosition() == getCount() - 1
                                    || getLastVisiblePosition() == getCount() - 2)// 上拉
                                onFooterStateChange();
                        }

                        if (state == RELEASE_TO_REFERESH) {
                            state = DONE;
                            if (getLastVisiblePosition() == getCount() - 1
                                    || getLastVisiblePosition() == getCount() - 2) {
                                // 上拉
                                onFooterStateChange();
                            }
                        }
                    }
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    public void initData(Context context, Stranger stranger) {
        FollowingManager followingManager = FollowingManager.getInstance(context);
        RelativeLayout showArea = (RelativeLayout) footer.findViewById(R.id.show_visitor_area);

        TextView visitCount = (TextView) footer.findViewById(R.id.visitor_quantity);

        TextView hintMessage = (TextView) footer.findViewById(R.id.hint_message);
        FollowUser user = followingManager.getFollowingUser(stranger.getUserId());
        if (stranger.getVisitorCount() < 10) {
            showArea.setVisibility(View.GONE);
        }
        String sex;
        switch (stranger.getSex()) {
            case 1:
                sex = context.getResources().getString(R.string.he);
                break;
            case 2:
                sex = context.getResources().getString(R.string.her);
                break;
            default:
                sex = context.getResources().getString(R.string.he);
                break;
        }
        String visitorQuantity = String.valueOf(stranger.getVisitorCount());

        visitCount.setText(context.getString(R.string.visitor, visitorQuantity, sex));

        if (user != null) {
            hintMessage.setText(context.getString(R.string.is_follow, sex));
        } else {
            hintMessage.setText(context.getString(R.string.un_follow, sex));
        }

    }

    public void initData(Context context, UserContact contact) {
        RelativeLayout showArea = (RelativeLayout) footer.findViewById(R.id.show_visitor_area);
        TextView visitCount = (TextView) footer.findViewById(R.id.visitor_quantity);
        TextView hintMessage = (TextView) footer.findViewById(R.id.hint_message);
        if (contact != null) {
            String visitorQuantity = String.valueOf(contact.getVisitorCount());
            if (contact.getVisitorCount() < 10) {
                showArea.setVisibility(View.GONE);
            }
            String sex;
            switch (contact.getSex()) {
                case 1:
                    sex = context.getResources().getString(R.string.he);
                    break;
                case 2:
                    sex = context.getResources().getString(R.string.her);
                    break;
                default:
                    sex = context.getResources().getString(R.string.he);
                    break;

            }
            visitCount.setText(context.getString(R.string.visitor, visitorQuantity, sex));
            hintMessage.setText(context.getString(R.string.is_friend, sex));
        }
    }

    public void initData(Context context, PersonalUser personalUser) {
        FollowingManager followingManager = FollowingManager.getInstance(context);
        UserContact contact = personalUser.getUserContact();
        RelativeLayout showArea = (RelativeLayout) footer.findViewById(R.id.show_visitor_area);
        FollowUser user = followingManager.getFollowingUser(contact.getUserId());
        TextView visitCount = (TextView) footer.findViewById(R.id.visitor_quantity);
        TextView hintMessage = (TextView) footer.findViewById(R.id.hint_message);
        if (contact != null) {
            String visitorQuantity = String.valueOf(contact.getVisitorCount());
            if (contact.getVisitorCount() < 10) {
                showArea.setVisibility(View.GONE);
            }
            String sex;
            switch (contact.getSex()) {
                case 1:
                    sex = context.getResources().getString(R.string.he);
                    break;
                case 2:
                    sex = context.getResources().getString(R.string.her);
                    break;
                default:
                    sex = context.getResources().getString(R.string.he);
                    break;

            }
            if (PersonalUser.STRANGER == personalUser.getType()){
                visitCount.setText(context.getString(R.string.visitor, visitorQuantity, sex));
                if (user != null) {
                    hintMessage.setText(context.getString(R.string.is_follow, sex));
                } else {
                    hintMessage.setText(context.getString(R.string.un_follow, sex));
                }
            }else if (PersonalUser.CONTACT == personalUser.getType()){
                visitCount.setText(context.getString(R.string.visitor, visitorQuantity, sex));
                hintMessage.setText(context.getString(R.string.is_friend, sex));
            }

        }
    }

    /**
     * 更改尾视图显示状态
     */
    private void onFooterStateChange() {
        switch (state) {
            case PULL_TO_REFRESH:
                footer.setVisibility(View.VISIBLE);
                if (isBack) {
                    isBack = false;
                }
                break;

            case RELEASE_TO_REFERESH:
                break;

            case REFERESHING:
                footer.setPadding(0, 0, 0, 0);
                break;
            case DONE:
                footer.setPadding(0, -1 * headerHeight, 0, 0);
                break;
        }
    }
}
