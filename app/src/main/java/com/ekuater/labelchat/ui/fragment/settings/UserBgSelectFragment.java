package com.ekuater.labelchat.ui.fragment.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.PersonalUpdateInfo;
import com.ekuater.labelchat.datastruct.UserTheme;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.ThemeManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;

/**
 * Created by Leo on 2015/2/8.
 *
 * @author LinYong
 */
public class UserBgSelectFragment extends Fragment {

    private static final int MSG_QUERY_ALL_THEMES_RESULT = 101;

    private ThemeAdapter mThemeAdapter;
    private ImageView mLoadingImage;
    private Handler mHandler;
    private boolean mLoadingThemes;

    private final Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean handled = true;

            switch (msg.what) {
                case MSG_QUERY_ALL_THEMES_RESULT:
                    onQueryAllThemesResult((UserTheme[]) msg.obj);
                    break;
                default:
                    handled = false;
                    break;
            }

            return handled;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        ThemeManager themeManager = ThemeManager.getInstance(activity);
        SettingHelper helper = SettingHelper.getInstance(activity);
        String myTheme = helper.getUserTheme();
        mThemeAdapter = new ThemeAdapter(activity.getLayoutInflater(),
                themeManager, myTheme);
        mHandler = new Handler(mHandlerCallback);
        setHasOptionsMenu(true);

        themeManager.queryAllThemes(new ThemeManager.ThemeQueryObserver() {
            @Override
            public void onQueryResult(int result, UserTheme[] themes) {
                mHandler.sendMessage(mHandler.obtainMessage(
                        MSG_QUERY_ALL_THEMES_RESULT, themes));
            }
        });
        mLoadingThemes = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_bg_select,
                container, false);
        ImageView icon = (ImageView) rootView.findViewById(R.id.icon);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        title.setText(R.string.select_background);
        TextView mConfirmView = (TextView) rootView.findViewById(R.id.right_title);
        mConfirmView.setVisibility(View.VISIBLE);
        mConfirmView.setTextColor(getResources().getColor(R.color.white));
        GridView thumbGrid = (GridView) rootView.findViewById(R.id.thumb_grid);
        thumbGrid.setAdapter(mThemeAdapter);
        thumbGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object object = parent.getAdapter().getItem(position);
                if (object instanceof UserTheme) {
                    UserTheme theme = (UserTheme) object;
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(ThemeShowFragment.ARGS_USER_THEME, theme);
                    UILauncher.launchFragmentInNewActivity(getActivity(),
                            ThemeShowFragment.class, arguments);
                }
            }
        });
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mConfirmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mLoadingImage = (ImageView) rootView.findViewById(R.id.loading);
        updateLoadAnimation();
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        onSelectBackground();
    }

    private void onQueryAllThemesResult(UserTheme[] themes) {
        mLoadingThemes = false;
        updateLoadAnimation();

        if (themes != null && themes.length > 0) {
            mThemeAdapter.updateThemes(themes);
        }
    }

    private void onSelectBackground() {
        Activity activity = getActivity();
        if (activity != null) {
            String selectedTheme = mThemeAdapter.getSelectedThemeName();
            if (!TextUtils.isEmpty(selectedTheme)) {
                SettingHelper helper = SettingHelper.getInstance(activity);
                AccountManager accountManager = AccountManager.getInstance(activity);
                PersonalUpdateInfo newInfo = new PersonalUpdateInfo();
                helper.setUserTheme(selectedTheme);
                newInfo.setTheme(selectedTheme);
                accountManager.updatePersonalInfo(newInfo);
            }
        }
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void updateLoadAnimation() {
        if (mLoadingThemes) {
            startLoadAnimation();
        } else {
            stopLoadAnimation();
        }
    }

    private void startLoadAnimation() {
        if (mLoadingImage != null) {
            mLoadingImage.setVisibility(View.VISIBLE);

            Drawable drawable = mLoadingImage.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                animationDrawable.start();
            }
        }
    }

    private void stopLoadAnimation() {
        if (mLoadingImage != null) {
            Drawable drawable = mLoadingImage.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                animationDrawable.stop();
            }

            mLoadingImage.setVisibility(View.GONE);
        }
    }

    private class ThemeAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private ThemeManager mThemeManager;
        private UserTheme[] mThemes;
        private String mSelectedThemeName;
        private ViewHolder mSelectedHolder;

        private View.OnClickListener mCheckClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder holder = (ViewHolder) v.getTag();
                mSelectedThemeName = holder.theme.getThemeName();
                if (mSelectedHolder != null) {
                    mSelectedHolder.selectView.setChecked(false);
                }
                holder.selectView.setChecked(true);
                mSelectedHolder = holder;
            }
        };

        public ThemeAdapter(LayoutInflater inflater, ThemeManager themeManager,
                            String myThemeName) {
            mInflater = inflater;
            mThemeManager = themeManager;
            mSelectedThemeName = myThemeName;
            mThemes = null;
        }

        public void updateThemes(UserTheme[] themes) {
            mThemes = themes;
            notifyDataSetChanged();
        }

        public String getSelectedThemeName() {
            return mSelectedThemeName;
        }

        @Override
        public int getCount() {
            return mThemes != null ? mThemes.length : 0;
        }

        @Override
        public UserTheme getItem(int position) {
            return mThemes != null ? mThemes[position] : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.user_bg_item, parent, false);
                ViewHolder holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.image);
                holder.selectArea = convertView.findViewById(R.id.select_area);
                holder.selectView = (CheckBox) convertView.findViewById(R.id.select);
                holder.selectArea.setOnClickListener(mCheckClickListener);
                convertView.setTag(holder);
                holder.selectArea.setTag(holder);
            }

            UserTheme theme = getItem(position);
            ViewHolder holder = (ViewHolder) convertView.getTag();
            mThemeManager.displayThemeImage(theme.getThemeThumb(), holder.imageView,
                    R.drawable.pic_loading);
            if (theme.getThemeName().equals(mSelectedThemeName)) {
                holder.selectView.setChecked(true);
                mSelectedHolder = holder;
            } else {
                holder.selectView.setChecked(false);
                if (mSelectedHolder == holder) {
                    mSelectedHolder = null;
                }
            }
            holder.theme = theme;

            return convertView;
        }

        private class ViewHolder {
            public ImageView imageView;
            public View selectArea;
            public CheckBox selectView;
            public UserTheme theme;
        }
    }
}
