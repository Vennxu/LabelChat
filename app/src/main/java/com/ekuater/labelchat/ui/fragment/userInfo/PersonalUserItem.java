package com.ekuater.labelchat.ui.fragment.userInfo;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.FollowingManager;


/**
 * Created by Administrator on 2015/4/28.
 */
public class PersonalUserItem {

    public static class StrangerItem implements PersonalItem.UserItem {

        private Context context;
        private TextView privateLetter;
        private TextView addFriend;
        private TextView unfowllow;
        private LinearLayout attention;
        private LinearLayout add;
        private PopupWindow moreOption;
        private FrameLayout strangerOption;
        private FollowingManager mFollowingManager;
        private UserContact userContact;
        private PersonalItem.UserClickListener userClickListener;

        private View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.private_letter:
                        userClickListener.letterClick();
                        break;
                    case R.id.stranger_talk:
                        userClickListener.tallClick();
                        break;
                    case R.id.invite:
                        userClickListener.inviteClick();
                        break;
                    case R.id.attention:
                        userClickListener.attentionClick();
                        break;
                    case R.id.add:
                        userClickListener.addClick();
                        break;
                    case R.id.stranger_option_list:
                        moreOption.showAtLocation(v, Gravity.RIGHT | Gravity.BOTTOM, 0, strangerOption.getHeight());
                        break;
                    case R.id.report:
                        moreOption.dismiss();
                        userClickListener.reportClick();
                        break;
                    case R.id.unfowllow:
                        moreOption.dismiss();
                        userClickListener.unFollowClick();
                        break;
                    case R.id.add_friend:
                        moreOption.dismiss();
                        userClickListener.addClick();
                        break;
                    default:
                        break;
                }
            }
        };

        public StrangerItem(Context context, UserContact userContact, PersonalItem.UserClickListener userClickListener) {
            this.context = context;
            this.userContact = userContact;
            this.userClickListener = userClickListener;
            mFollowingManager = FollowingManager.getInstance(context);
        }


        @Override
        public View newView(LayoutInflater layoutInflater, ViewGroup parent) {
            View view = layoutInflater.inflate(R.layout.stranger_user_opertion, parent, false);
            View optionView = layoutInflater.inflate(R.layout.stranger_more_opertion, null, false);
            strangerOption = (FrameLayout) view.findViewById(R.id.stranger);
            privateLetter = (TextView) optionView.findViewById(R.id.private_letter);
            addFriend = (TextView) optionView.findViewById(R.id.add_friend);
            unfowllow = (TextView) optionView.findViewById(R.id.unfowllow);
            TextView report = (TextView) optionView.findViewById(R.id.report);
            moreOption = getPopupWindow(optionView);

            LinearLayout talk = (LinearLayout) view.findViewById(R.id.stranger_talk);
            LinearLayout invite = (LinearLayout) view.findViewById(R.id.invite);
            attention = (LinearLayout) view.findViewById(R.id.attention);
            add = (LinearLayout) view.findViewById(R.id.add);
            ImageView optionMore = (ImageView) view.findViewById(R.id.stranger_option_list);

            privateLetter.setOnClickListener(onClickListener);
            talk.setOnClickListener(onClickListener);
            invite.setOnClickListener(onClickListener);
            attention.setOnClickListener(onClickListener);
            add.setOnClickListener(onClickListener);
            optionMore.setOnClickListener(onClickListener);
            addFriend.setOnClickListener(onClickListener);
            unfowllow.setOnClickListener(onClickListener);
            report.setOnClickListener(onClickListener);
            changeUI();
            return view;
        }

        @Override
        public void bindView(View view) {

        }

        public void changeUI() {
            if (mFollowingManager.getFollowingUser(userContact.getUserId()) != null) {
                addFriend.setVisibility(View.GONE);
                unfowllow.setVisibility(View.VISIBLE);
                attention.setVisibility(View.GONE);
                add.setVisibility(View.VISIBLE);
            } else {
                addFriend.setVisibility(View.VISIBLE);
                unfowllow.setVisibility(View.GONE);
                attention.setVisibility(View.VISIBLE);
                add.setVisibility(View.GONE);
            }
        }
    }

    public static class ContactItem implements PersonalItem.UserItem {

        private Context context;
        private PopupWindow moreOption;
        private FrameLayout contactOption;
        private PersonalItem.UserClickListener userClickListener;

        public ContactItem(Context context, PersonalItem.UserClickListener userClickListener) {
            this.context = context;
            this.userClickListener = userClickListener;
        }

        private View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.private_letter:
                        moreOption.dismiss();
                        userClickListener.letterClick();
                        break;
                    case R.id.contact_talk:
                        userClickListener.tallClick();
                        break;
                    case R.id.invite:
                        moreOption.dismiss();
                        userClickListener.inviteClick();
                        break;
                    case R.id.attention:
                        userClickListener.attentionClick();
                        break;
                    case R.id.contact_option_list:
                        moreOption.showAtLocation(v, Gravity.RIGHT | Gravity.BOTTOM, 0, contactOption.getHeight());
                        break;
                    case R.id.report:
                        moreOption.dismiss();
                        userClickListener.reportClick();
                        break;
                    case R.id.delete_friend:
                        moreOption.dismiss();
                        userClickListener.deleteFriendClick();
                        break;
                    default:
                        break;
                }
            }
        };

        @Override
        public View newView(LayoutInflater layoutInflater, ViewGroup parent) {
            View view = layoutInflater.inflate(R.layout.contact_user_opertion, parent, false);
            contactOption = (FrameLayout) view.findViewById(R.id.contact);
            View optionView = layoutInflater.inflate(R.layout.contact_more_opertion, null, false);
            TextView invite = (TextView) optionView.findViewById(R.id.invite);
            TextView privateLetter = (TextView) optionView.findViewById(R.id.private_letter);
            TextView deleteFriend = (TextView) optionView.findViewById(R.id.delete_friend);
            TextView report = (TextView) optionView.findViewById(R.id.report);
            moreOption = getPopupWindow(optionView);
            LinearLayout talkBtn = (LinearLayout) view.findViewById(R.id.contact_talk);
            ImageView optionBtn = (ImageView) view.findViewById(R.id.contact_option_list);

            talkBtn.setOnClickListener(onClickListener);
            optionBtn.setOnClickListener(onClickListener);
            invite.setOnClickListener(onClickListener);
            privateLetter.setOnClickListener(onClickListener);
            deleteFriend.setOnClickListener(onClickListener);
            report.setOnClickListener(onClickListener);
            return view;
        }

        @Override
        public void bindView(View view) {

        }

    }

    public static PopupWindow getPopupWindow(View view) {
        PopupWindow pw = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pw.setFocusable(true);
        pw.setOutsideTouchable(true);
        pw.setBackgroundDrawable(new BitmapDrawable());
        return pw;
    }
}
