package com.ekuater.labelchat.ui.fragment.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ChatBg;
import com.ekuater.labelchat.datastruct.UserTheme;
import com.ekuater.labelchat.delegate.ThemeManager;

/**
 * Created by Leo on 2015/3/2.
 *
 * @author LinYong
 */
public class ThemeShowFragment extends Fragment {

    public static final String ARGS_USER_THEME = "args_user_theme";
    public static final String ARGS_CHAT_BACKGROUND = "args_chat_background";

    private ThemeManager mThemeManager;
    private UserTheme mUserTheme;
    private ChatBg mChatBg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        mThemeManager = ThemeManager.getInstance(activity);
        parseArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_theme_show, container, false);
        View closeBtn = rootView.findViewById(R.id.close);
        ImageView themeImageView = (ImageView) rootView.findViewById(R.id.theme_image);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        if (mUserTheme != null) {
            mThemeManager.displayThemeImage(mUserTheme.getTopImg(), themeImageView,
                    R.drawable.pic_loading);
        } else if (mChatBg != null) {
            mThemeManager.displayChatBgImage(mChatBg.getBgImg(), themeImageView,
                    R.drawable.pic_loading);
        }
        return rootView;
    }

    private void parseArguments() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUserTheme = arguments.getParcelable(ARGS_USER_THEME);
            mChatBg = arguments.getParcelable(ARGS_CHAT_BACKGROUND);
        } else {
            mUserTheme = null;
        }
    }
}
