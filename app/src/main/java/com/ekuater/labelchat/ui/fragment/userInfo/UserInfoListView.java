package com.ekuater.labelchat.ui.fragment.userInfo;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.FollowingManager;

/**
 * Created by Administrator on 2015/3/24.
 */
public class UserInfoListView extends ListView implements AbsListView.OnScrollListener {

    private int footViewHeight;
    private View footView;

    public UserInfoListView(Context context) {
        super(context);
        initFootView(context);
    }

    public UserInfoListView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initFootView(context);
    }

    public UserInfoListView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        initFootView(context);
    }

    private void initFootView(Context context) {
        LayoutInflater mInflater = LayoutInflater.from(context);
        footView = mInflater.inflate(R.layout.recently_visit_user_count, null);
        footView.measure(0, 0);
        footViewHeight = footView.getMeasuredHeight();
        footView.setPadding(0, 0, 0, -footViewHeight);
        footView.invalidate();
        addFooterView(footView);
        setOnScrollListener(this);
    }


    public void initData(Context context, Stranger stranger) {
        FollowingManager followingManager = FollowingManager.getInstance(context);
        RelativeLayout showArea = (RelativeLayout) footView.findViewById(R.id.show_visitor_area);

        TextView visitCount = (TextView) footView.findViewById(R.id.visitor_quantity);

        TextView hintMessage = (TextView) footView.findViewById(R.id.hint_message);
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
        RelativeLayout showArea = (RelativeLayout) footView.findViewById(R.id.show_visitor_area);
        TextView visitCount = (TextView) footView.findViewById(R.id.visitor_quantity);
        TextView hintMessage = (TextView) footView.findViewById(R.id.hint_message);
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


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    int mDown;
    int diff;


    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDown = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                diff = (int) ev.getY();
                if (mDown - diff > footViewHeight) {
                    footView.setPadding(0, 0, 0, footViewHeight);
                } else {
                    footView.setPadding(0, 0, 0, mDown - diff);
                }
                break;
            case MotionEvent.ACTION_UP:
                ObjectAnimator animator = ObjectAnimator.ofInt(this, "footMove",
                        footViewHeight, -footViewHeight);
                animator.setDuration(500);
                animator.start();
                break;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setFootMove(int footMove) {
        footView.setPadding(0, 0, 0, footMove);
    }
}
