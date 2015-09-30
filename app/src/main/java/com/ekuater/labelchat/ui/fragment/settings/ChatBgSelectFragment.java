package com.ekuater.labelchat.ui.fragment.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
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
import com.ekuater.labelchat.datastruct.ChatBg;
import com.ekuater.labelchat.delegate.ShortUrlImageLoadListener;
import com.ekuater.labelchat.delegate.ThemeManager;
import com.ekuater.labelchat.delegate.imageloader.LoadFailType;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.util.ACache;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2015/4/10.
 */
public class ChatBgSelectFragment extends Fragment {
    private static final int MSG_QUERY_ALL_CHAT_BACKGROUND_RESULT = 101;

    private ThemeManager mThemeManager;
    private ImageView mLoadingImage;
    private ChatBgAdapter adapter;
    private boolean mLoadingThemes;
    private ACache mACache;
    private SettingHelper mSettingHelper;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_QUERY_ALL_CHAT_BACKGROUND_RESULT:
                    handlerChatBackgroundResult((ChatBg[]) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    private void queryChatBackgroundResult() {
        mThemeManager.queryAllChatBg(new ThemeManager.ChatBgQueryObserver() {
            @Override
            public void onQueryResult(int result, ChatBg[] chatBgs) {
                Message message = mHandler.obtainMessage(MSG_QUERY_ALL_CHAT_BACKGROUND_RESULT, chatBgs);
                mHandler.sendMessage(message);
            }
        });
    }

    private void handlerChatBackgroundResult(ChatBg[] chatBgs) {
        mLoadingThemes = false;
        updateLoadAnimation();

        if (chatBgs != null && chatBgs.length > 0) {
            adapter.updateThemes(chatBgs);
        }
    }

    private ChatBg mChatBg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mACache = ACache.get(activity);
        mThemeManager = ThemeManager.getInstance(activity);
        mSettingHelper = SettingHelper.getInstance(activity);
        JSONObject jsonObject = mACache.getAsJSONObject("ChatBg");
        if (jsonObject != null) {
            try {
                mChatBg = new ChatBg();
                mChatBg.setId(jsonObject.getInt("ChatBgId"));
                mChatBg.setBgImg(jsonObject.getString("ChatBgImg"));
                mChatBg.setBgThumb(jsonObject.getString("ChatBgThumbImg"));
                mChatBg.setSerialNum(jsonObject.getInt("ChatBgSerialNum"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            mChatBg = mSettingHelper.getChatBg();
        }
        adapter = new ChatBgAdapter(activity.getLayoutInflater(), mThemeManager, mChatBg);
        queryChatBackgroundResult();
        mLoadingThemes = true;

    }

    private TextView mConfirmView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat_bg_select,
                container, false);
        ImageView icon = (ImageView) rootView.findViewById(R.id.icon);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        mConfirmView = (TextView) rootView.findViewById(R.id.right_title);
        mConfirmView.setVisibility(View.VISIBLE);
        mConfirmView.setTextColor(getResources().getColor(R.color.white));
        title.setText(R.string.select_chat_background);
        GridView thumbGrid = (GridView) rootView.findViewById(R.id.thumb_grid);
        thumbGrid.setAdapter(adapter);
        thumbGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object object = parent.getAdapter().getItem(position);
                if (object instanceof ChatBg) {
                    ChatBg chatBg = (ChatBg) object;
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(ThemeShowFragment.ARGS_CHAT_BACKGROUND, chatBg);
                    UILauncher.launchFragmentInNewActivity(getActivity(),
                            ThemeShowFragment.class, arguments);
                }

            }
        });
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectChatBg();

            }
        });
        mConfirmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectChatBg();
            }
        });
        mLoadingImage = (ImageView) rootView.findViewById(R.id.loading);
        updateLoadAnimation();
        return rootView;
    }

    private void selectChatBg() {
        JSONObject jsonObject = new JSONObject();
        ChatBg chatBg = adapter.getChatBg();

        if (chatBg != null) {
            try {
                jsonObject.put("ChatBgId", chatBg.getId());
                jsonObject.put("ChatBgImg", chatBg.getBgImg());
                jsonObject.put("ChatBgThumbImg", chatBg.getBgThumb());
                jsonObject.put("ChatBgSerialNum", chatBg.getSerialNum());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mACache.put("ChatBg", jsonObject);
            mSettingHelper.setChatBg(chatBg);
            final String chatBgName = chatBg.getBgImg();
            if (!TextUtils.isEmpty(chatBgName)) {
                mThemeManager.getAvatarBitmap(chatBgName, new ShortUrlImageLoadListener() {
                    @Override
                    public void onLoadFailed(String shortUrl, LoadFailType loadFailType) {

                    }

                    @Override
                    public void onLoadComplete(String shortUrl, Bitmap loadedImage) {
                        mACache.put("ChatBackground", loadedImage);

                    }
                });
            }
        }
        getActivity().finish();
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

    private class ChatBgAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private ThemeManager mThemeManager;
        private ChatBg[] mChatBgs;

        private ChatBg mChatBg;
        private ViewHolder mSelectedHolder;

        private View.OnClickListener mCheckClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder holder = (ViewHolder) v.getTag();
                mChatBg = holder.chatBg;
                if (mSelectedHolder != null) {
                    mSelectedHolder.selectView.setChecked(false);
                }
                holder.selectView.setChecked(true);
                mSelectedHolder = holder;
            }
        };

        public ChatBgAdapter(LayoutInflater inflater, ThemeManager themeManager,
                             ChatBg chatBg) {
            mInflater = inflater;
            mThemeManager = themeManager;
            mChatBg = chatBg;
            mChatBgs = null;
        }

        public void updateThemes(ChatBg[] chatBgs) {
            mChatBgs = chatBgs;
            notifyDataSetChanged();
        }

        public ChatBg getChatBg() {
            return mChatBg;
        }

        @Override
        public int getCount() {
            return mChatBgs != null ? mChatBgs.length : 0;
        }

        @Override
        public ChatBg getItem(int position) {
            return mChatBgs != null ? mChatBgs[position] : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.chat_bg_item, parent, false);
                ViewHolder holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.image);
                holder.selectArea = convertView.findViewById(R.id.select_area);
                holder.selectView = (CheckBox) convertView.findViewById(R.id.select);
                holder.selectArea.setOnClickListener(mCheckClickListener);
                convertView.setTag(holder);
                holder.selectArea.setTag(holder);
            }
            ChatBg chatBg = getItem(position);
            ViewHolder holder = (ViewHolder) convertView.getTag();
            mThemeManager.displayChatBgThumbImage(chatBg.getBgThumb(), holder.imageView,
                    R.drawable.pic_loading);
            if (mChatBg != null) {
                if (chatBg.getId() == mChatBg.getId()) {
                    holder.selectView.setChecked(true);
                    mSelectedHolder = holder;
                } else {
                    holder.selectView.setChecked(false);
                    if (mSelectedHolder == holder) {
                        mSelectedHolder = null;
                    }
                }
            }
            holder.chatBg = chatBg;
            return convertView;
        }

        private class ViewHolder {
            public ImageView imageView;
            public View selectArea;
            public CheckBox selectView;
            public ChatBg chatBg;
        }
    }
}
