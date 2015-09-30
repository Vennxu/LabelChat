package com.ekuater.labelchat.delegate;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.theme.ChatBgCommand;
import com.ekuater.labelchat.command.theme.ThemeListCommand;
import com.ekuater.labelchat.datastruct.ChatBg;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.UserTheme;
import com.ekuater.labelchat.delegate.imageloader.DisplayOptions;
import com.ekuater.labelchat.delegate.imageloader.OnlineImageLoader;
import com.ekuater.labelchat.delegate.imageloader.TargetSize;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

/**
 * Created by Leo on 2015/3/1.
 *
 * @author LinYong
 */
public class ThemeManager extends BaseManager {

    private static final String TAG = ThemeManager.class.getSimpleName();

    // Query result enum
    public static final int QUERY_RESULT_SUCCESS = 0;
    public static final int QUERY_RESULT_ILLEGAL_ARGUMENTS = 1;
    public static final int QUERY_RESULT_QUERY_FAILURE = 2;
    public static final int QUERY_RESULT_RESPONSE_DATA_ERROR = 3;

    public interface ThemeQueryObserver {
        public void onQueryResult(int result, UserTheme[] themes);
    }

    public interface ChatBgQueryObserver {
        public void onQueryResult(int result, ChatBg[] chatBgs);

    }

    private static ThemeManager sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new ThemeManager(context.getApplicationContext());
        }
    }

    public static ThemeManager getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private final String mImageBaseUrl;
    private final String mChatBgUrl;
    private final String mChatBgThumbUrl;
    private final OnlineImageLoader mImageLoader;

    private ThemeManager(Context context) {
        super(context);
        mImageBaseUrl = context.getString(R.string.config_theme_image_url);
        mChatBgUrl = context.getString(R.string.config_chat_background_url);
        mChatBgThumbUrl = context.getString(R.string.config_chat_background_thumb_url);
        mImageLoader = OnlineImageLoader.getInstance(context);
    }

    public void queryAllThemes(ThemeQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        ThemeListCommand command = new ThemeListCommand();
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof ThemeQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                ThemeQueryObserver observer = (ThemeQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    ThemeListCommand.CommandResponse cmdResp
                            = new ThemeListCommand.CommandResponse(response);
                    UserTheme[] themes = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        themes = cmdResp.getThemes();
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, themes);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public void queryAllChatBg(ChatBgQueryObserver observer) {
        if (observer == null) {
            return;
        }

        ChatBgCommand cmd = new ChatBgCommand();
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof ChatBgQueryObserver)) {
                    return;
                }
                ChatBgQueryObserver observer = (ChatBgQueryObserver) mObj;
                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null);
                    return;
                }
                try {
                    ChatBgCommand.CommandResponse cmdResp = new ChatBgCommand.CommandResponse(response);
                    ChatBg[] chatBgs = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;
                    if (cmdResp.requestSuccess()) {
                        chatBgs = cmdResp.getChatBgs();
                        _ret = QUERY_RESULT_SUCCESS;
                        L.d(TAG, "queryAllChatBg(), success");
                    }
                    observer.onQueryResult(_ret, chatBgs);
                    L.d(TAG, "queryAllChatBg(), size=" + (chatBgs != null ? chatBgs.length : 0));
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(cmd, handler);
    }


    private String getChatBgUrl(String url) {
        return mChatBgUrl + parseUrl(url);
    }

    private String getChatBgThumbUrl(String url) {
        return mChatBgThumbUrl + parseUrl(url);
    }

    public void displayChatBgImage(String url, ImageView imageView, int defaultIcon) {
        if (!TextUtils.isEmpty(url)) {
            mImageLoader.displayImage(getChatBgUrl(url), imageView,
                    newCachedDisplayOptions(defaultIcon));
        } else {
            imageView.setImageResource(defaultIcon);
        }
    }

    public void displayChatBgThumbImage(String url, ImageView imageView, int defaultIcon) {
        if (!TextUtils.isEmpty(url)) {
            mImageLoader.displayImage(getChatBgThumbUrl(url), imageView,
                    newCachedDisplayOptions(defaultIcon));
        } else {
            imageView.setImageResource(defaultIcon);
        }
    }

    public void displayThemeImage(String url, ImageView imageView, int defaultIcon) {
        if (!TextUtils.isEmpty(url)) {
            mImageLoader.displayImage(getImageUrl(url), imageView,
                    newCachedDisplayOptions(defaultIcon));
        } else {
            imageView.setImageResource(defaultIcon);
        }
    }

    private String getImageUrl(String url) {
        return mImageBaseUrl + parseUrl(url);
    }

    private String parseUrl(String url) {
        final int idx = url.lastIndexOf("/");
        return (idx >= 0 && idx < (url.length() - 1)) ? url.substring(idx + 1) : url;
    }

    private DisplayOptions newCachedDisplayOptions(int defaultIcon) {
        return new DisplayOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .defaultImageRes(defaultIcon)
                .build();
    }

    public Bitmap getAvatarBitmap(String url, ShortUrlImageLoadListener listener) {
        if (TextUtils.isEmpty(url)) {
            return null;
        } else {
            return getImageBitmap(getChatBgUrl(url), url, null, null, listener);
        }
    }

    private Bitmap getImageBitmap(String fullUrl, String url, TargetSize targetSize,
                                  DisplayOptions displayOptions,
                                  ShortUrlImageLoadListener listener) {
        ShortUrlImageLoadWrapper wrapper = new ShortUrlImageLoadWrapper(url, listener);
        return mImageLoader.loadImage(fullUrl, targetSize, displayOptions, wrapper);
    }
}
