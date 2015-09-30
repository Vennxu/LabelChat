package com.ekuater.labelchat.delegate;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.account.UploadAvatarCommand;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.delegate.imageloader.DisplayOptions;
import com.ekuater.labelchat.delegate.imageloader.OnlineImageLoader;
import com.ekuater.labelchat.delegate.imageloader.TargetSize;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.util.L;
import com.ekuater.labelchat.util.TextUtil;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author LinYong
 */
public final class AvatarManager extends BaseManager {

    private static final String TAG = AvatarManager.class.getSimpleName();
    private static final String AVATAR_SUFFIX = ""; //".jpg";

    public enum UploadFailType {
        UPLOAD_FILE_ERROR,
        REQUEST_PARAM_ERROR,
        RESPONSE_ERROR,
    }

    public interface UploadListener {
        void onUploadFailed(String userId, UploadFailType uploadFailType);

        void onUploadComplete(String userId);

        void onUploadProgress(String userId, long bytesWritten, long totalSize);
    }

    private static AvatarManager sInstance;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AvatarManager(context.getApplicationContext());
        }
    }

    public static AvatarManager getInstance(Context context) {
        if (sInstance == null) {
            initInstance(context);
        }

        return sInstance;
    }

    private final Context mContext;
    private final OnlineImageLoader mImageLoader;
    private final String mAvatarBaseUrl;
    private final String mThumbAvatarBaseUrl;
    private final String mTmpGroupAvatarBaseUrl;
    private final String mStoryImageBaseUrl;
    private final String mStoryImageThumbUrl;
    private final String mChatRoomUrl;
    private final String mCategoryUrl;
    private final String mConfideUrl;

    private final TargetSize mAvatarThumbSize;
    private final DisplayOptions mAvatarThumbDisplayOptions;
    private final TargetSize mAvatarSize;
    private final DisplayOptions mAvatarDisplayOptions;

    private AvatarManager(Context context) {
        super(context);
        mContext = context;
        mImageLoader = OnlineImageLoader.getInstance(context);
        mAvatarBaseUrl = context.getString(R.string.config_avatar_base_url);
        mThumbAvatarBaseUrl = context.getString(R.string.config_thumb_avatar_base_url);
        mTmpGroupAvatarBaseUrl = context.getString(R.string.config_tmp_group_avatar_url);
        mStoryImageThumbUrl = context.getString(R.string.config_label_story_thumb_url);
        mStoryImageBaseUrl = context.getString(R.string.config_label_story_url);
        mChatRoomUrl = context.getString(R.string.config_chat_room_url);
        mCategoryUrl = context.getString(R.string.config_category_url);
        mConfideUrl = context.getString(R.string.config_confide_url);

        mAvatarThumbSize = new TargetSize(100, 100);
        mAvatarSize = new TargetSize(300, 300);
        mAvatarThumbDisplayOptions = new DisplayOptions.Builder()
                .cacheInMemory(true).cacheOnDisk(true).build();
        mAvatarDisplayOptions = new DisplayOptions.Builder()
                .cacheInMemory(false).cacheOnDisk(true).build();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    /**
     * Clear avatar cache in memory and disk
     */
    @SuppressWarnings("UnusedDeclaration")
    public void clearCache() {
        mImageLoader.clearCache();
    }

    /**
     * Get user large avatar from avatar server. Will return avatar Bitmap if avatar cached
     * in memory, or notify listener when get avatar asynchronously.
     *
     * @param url      avatar url
     * @param listener asynchronously load avatar listener
     * @return avatar Bitmap if cached in memory, or null if not cached in memory
     */
    public Bitmap getAvatarBitmap(String url, ShortUrlImageLoadListener listener) {
        if (TextUtils.isEmpty(url)) {
            return null;
        } else {
            return getImageBitmap(getAvatarUrl(url), url, mAvatarSize,
                    mAvatarDisplayOptions, listener);
        }
    }

    /**
     * Get user small avatar from avatar server. Will return avatar Bitmap if avatar cached
     * in memory, or notify listener when get avatar asynchronously.
     *
     * @param url      avatar url
     * @param listener asynchronously load avatar listener
     * @return avatar Bitmap if cached in memory, or null if not cached in memory
     */
    public Bitmap getAvatarThumbBitmap(String url, ShortUrlImageLoadListener listener) {
        if (TextUtils.isEmpty(url)) {
            return null;
        } else {
            return getImageBitmap(getAvatarUrl(url), url, mAvatarThumbSize,
                    mAvatarThumbDisplayOptions, listener);
        }
    }

    public void displayAvatarThumb(String url, ImageView imageView, int defaultIcon) {
        if (!TextUtils.isEmpty(url)) {
            mImageLoader.displayImage(getAvatarThumbUrl(url), imageView,
                    newThumbDisplayOptions(defaultIcon));
        }
    }

    public void displayTmpGroupAvatar(String url, ImageView imageView, int defaultIcon) {
        if (!TextUtils.isEmpty(url)) {
            mImageLoader.displayImage(getTmpGroupAvatarUrl(url), imageView,
                    newThumbDisplayOptions(defaultIcon));
        }
    }

    /**
     * Get LabelStory avatar from avatar server. Will return avatar Bitmap if avatar cached
     * in memory, or notify listener when get avatar asynchronously.
     *
     * @param url      avatar url
     * @param listener asynchronously load avatar listener
     * @return avatar Bitmap if cached in memory, or null if not cached in memory
     */
    public Bitmap getLabelStoryImage(String url, ShortUrlImageLoadListener listener) {
        if (TextUtils.isEmpty(url)) {
            return null;
        } else {
            return getImageBitmap(getStoryImageUrl(url), url, null,
                    mAvatarDisplayOptions, listener);
        }
    }

    public Bitmap getLabelStoryImageThumb(String url, ShortUrlImageLoadListener listener) {
        if (TextUtils.isEmpty(url)) {
            return null;
        } else {
            return getImageBitmap(getStoryImageThumbUrl(url), url, null,
                    mAvatarDisplayOptions, listener);
        }
    }

    public void displayStoryImage(String url, ImageView imageView, int defaultIcon) {
        if (!TextUtils.isEmpty(url)) {
            mImageLoader.displayImage(getStoryImageUrl(url), imageView,
                    newDisplayOptions(false, true, defaultIcon));
        } else {
            imageView.setImageResource(defaultIcon);
        }
    }

    public void displayStoryImageThumb(String url, ImageView imageView, int defaultIcon) {
        if (!TextUtils.isEmpty(url)) {
            mImageLoader.displayImage(getStoryImageThumbUrl(url), imageView,
                    newDisplayOptions(false, true, defaultIcon));
        } else {
            imageView.setImageResource(defaultIcon);
        }
    }

    public void uploadAvatar(File avatarFile, UploadListener listener) {
        final String userId = getUserId();
        final String session = getSession();

        if (TextUtil.isEmpty(userId) || TextUtil.isEmpty(session)) {
            if (listener != null) {
                listener.onUploadFailed(userId, UploadFailType.REQUEST_PARAM_ERROR);
            }
            return;
        }

        try {
            UploadAvatarCommand command = new UploadAvatarCommand(getSession(), userId);
            IUploadResponseHandler handler = new UploadAvatarHandler(mContext, userId, listener);
            command.setAvatarFile(avatarFile);
            mCoreService.doUpload(command, handler);
        } catch (FileNotFoundException e) {
            L.e(TAG, e);
            if (listener != null) {
                listener.onUploadFailed(userId, UploadFailType.REQUEST_PARAM_ERROR);
            }
        }
    }

    public void displayChatRoomAvatar(String url, ImageView imageView, int defaultIcon) {
        if (!TextUtils.isEmpty(url)) {
            mImageLoader.displayImage(getChatRoomAvatarUrl(url), imageView,
                    newThumbDisplayOptions(defaultIcon));
        }
    }

    public void displaySingerAvatar(String url, ImageView imageView, int defaultIcon) {
        if (!TextUtils.isEmpty(url)) {
            mImageLoader.displayImage(url, imageView, newThumbDisplayOptions(defaultIcon));
        }
    }

    public void displayCategoryAvatar(String url, ImageView imageView, int defaultIcon) {
        if (!TextUtils.isEmpty(url)) {
            mImageLoader.displayImage(getCategoryAvatarUrl(url), imageView,
                    newThumbDisplayOptions(defaultIcon));
        }
    }

    public void displayConfideAvatar(String url, ImageView imageView, int defaultIcon) {
        if (!TextUtils.isEmpty(url)) {
            mImageLoader.displayImage(getConfideAvatarUrl(url), imageView,
                    newThumbDisplayOptions(defaultIcon));
        }
    }

    private Bitmap getImageBitmap(String fullUrl, String url, TargetSize targetSize,
                                  DisplayOptions displayOptions,
                                  ShortUrlImageLoadListener listener) {
        ShortUrlImageLoadWrapper wrapper = new ShortUrlImageLoadWrapper(url, listener);
        return mImageLoader.loadImage(fullUrl, targetSize, displayOptions, wrapper);
    }

    private DisplayOptions newThumbDisplayOptions(int defaultIcon) {
        return newDisplayOptions(true, true, defaultIcon);
    }

    private DisplayOptions newDisplayOptions(boolean cacheInMemory, boolean cacheOnDisk,
                                             int defaultIcon) {
        return new DisplayOptions.Builder()
                .cacheInMemory(cacheInMemory)
                .cacheOnDisk(cacheOnDisk)
                .defaultImageRes(defaultIcon)
                .build();
    }

    private String parseUrl(String url) {
        final int idx = url.lastIndexOf("/");
        return (idx >= 0 && idx < (url.length() - 1)) ? url.substring(idx + 1) : url;
    }

    private boolean isLocalAsset(String url) {
        return url.startsWith("assets://");
    }

    private String getAvatarUrl(String url) {
        if (isLocalAsset(url)) {
            return url;
        } else {
            return mAvatarBaseUrl + parseUrl(url) + AVATAR_SUFFIX;
        }
    }

    private String getAvatarThumbUrl(String url) {
        if (isLocalAsset(url)) {
            return url;
        } else {
            return mThumbAvatarBaseUrl + parseUrl(url) + AVATAR_SUFFIX;
        }
    }

    private String getTmpGroupAvatarUrl(String url) {
        return mTmpGroupAvatarBaseUrl + parseUrl(url) + AVATAR_SUFFIX;
    }

    private String getChatRoomAvatarUrl(String url) {
        return mChatRoomUrl + parseUrl(url) + AVATAR_SUFFIX;
    }

    private String getCategoryAvatarUrl(String url) {
        return mCategoryUrl + parseUrl(url) + AVATAR_SUFFIX;
    }

    private String getConfideAvatarUrl(String url) {
        return mConfideUrl + parseUrl(url) + AVATAR_SUFFIX;
    }

    private String getStoryImageUrl(String url) {
        return mStoryImageBaseUrl + parseUrl(url) + AVATAR_SUFFIX;
    }

    private String getStoryImageThumbUrl(String url) {
        return mStoryImageThumbUrl + parseUrl(url) + AVATAR_SUFFIX;
    }

    private static class UploadAvatarHandler implements IUploadResponseHandler {

        private interface Notifier {
            void notify(UploadListener listener);
        }

        private final String userId;
        private final UploadListener listener;
        private final SettingHelper helper;

        public UploadAvatarHandler(Context context, String userId, UploadListener listener) {
            this.userId = userId;
            this.listener = listener;
            this.helper = SettingHelper.getInstance(context);
        }

        @Override
        public void onProgress(final long bytesWritten, final long totalSize) {
            notifyListener(new Notifier() {
                @Override
                public void notify(UploadListener listener) {
                    listener.onUploadProgress(userId, bytesWritten, totalSize);
                }
            });
        }

        @Override
        public void onResponse(int result, String response) {
            L.v(TAG, "onResponse(), response=" + response);

            switch (result) {
                case ConstantCode.EXECUTE_RESULT_SUCCESS:
                    onResponseSuccess(response);
                    break;
                default:
                    notifyUploadFailed(UploadFailType.RESPONSE_ERROR);
                    break;
            }
        }

        private void onResponseSuccess(String response) {
            try {
                UploadAvatarCommand.CommandResponse cmdResp
                        = new UploadAvatarCommand.CommandResponse(response);

                if (cmdResp.requestSuccess()) {
                    // Update personal avatar setting
                    helper.setAccountAvatar(cmdResp.getAvatar());
                    helper.setAccountAvatarThumb(cmdResp.getAvatarThumb());
                    notifyUploadComplete();
                } else {
                    notifyUploadFailed(UploadFailType.RESPONSE_ERROR);
                }
                return;
            } catch (JSONException e) {
                L.w(TAG, e);
            }
            notifyUploadFailed(UploadFailType.UPLOAD_FILE_ERROR);
        }

        private void notifyUploadComplete() {
            notifyListener(new Notifier() {
                @Override
                public void notify(UploadListener listener) {
                    listener.onUploadComplete(userId);
                }
            });
        }

        private void notifyUploadFailed(final UploadFailType uploadFailType) {
            notifyListener(new Notifier() {
                @Override
                public void notify(UploadListener listener) {
                    listener.onUploadFailed(userId, uploadFailType);
                }
            });
        }

        private void notifyListener(Notifier notifier) {
            if (this.listener != null) {
                notifier.notify(this.listener);
            }
        }
    }
}
