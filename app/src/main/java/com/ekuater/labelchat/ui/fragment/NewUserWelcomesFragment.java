package com.ekuater.labelchat.ui.fragment;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.util.DateTimeUtils;

/**
 * Created by Administrator on 2014/12/26.
 *
 * @author FanChong
 */
public class NewUserWelcomesFragment extends Fragment {

    public static final String NEW_USER_WELCOME = "new_user_welcome";

    private long messageTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseArgument();
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_user_welcomes, container, false);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(R.string.app_team);
        TextView time = (TextView) view.findViewById(R.id.show_tiem);
        time.setText(DateTimeUtils.getMessageDateString(getActivity(), messageTime));
        return view;
    }

    private void parseArgument() {
        Bundle argument = getArguments();
        if (argument != null) {
            messageTime = argument.getLong(NEW_USER_WELCOME);
        }
    }
}
