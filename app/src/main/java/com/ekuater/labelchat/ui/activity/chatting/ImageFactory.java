package com.ekuater.labelchat.ui.activity.chatting;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.ekuater.labelchat.EnvConfig;
import com.ekuater.labelchat.util.ChatThumbnailFactory;
import com.ekuater.labelchat.util.UniqueFileName;

import java.io.File;

/**
 * @author LinYong
 *         This class use to decode image message preview image
 */
public class ImageFactory {

    private static final String TAG = ImageFactory.class.getSimpleName();

    private ChatThumbnailFactory mThumbnailFactory;
    private File mThumbnailDir;

    public ImageFactory(Context context, String userId) {
        mThumbnailFactory = new ChatThumbnailFactory(context);
        mThumbnailDir = EnvConfig.getImageChatMsgThumbnailDirectory(userId);

        if ((!mThumbnailDir.exists() && !mThumbnailDir.mkdirs())
                || (mThumbnailDir.exists() && mThumbnailDir.isFile())) {
            Log.d(TAG, "make record dir failed, dir=" + mThumbnailDir);
        }
    }

    private File getThumbnailFile(String fileName) {
        return new File(mThumbnailDir, TextUtils.isEmpty(fileName)
                ? UniqueFileName.getUniqueFileName("jpg") : fileName);
    }

    public Bitmap decodeThumbnail(String fileName) {
        return mThumbnailFactory.decodeThumbnail(getThumbnailFile(fileName).getPath());
    }

    public Bitmap generateThumbnail(String imagePath) {
        return mThumbnailFactory.generateThumbnail(imagePath);
    }

    public String generateThumbnailFile(String imagePath) {
        Bitmap thumbnailBmp = generateThumbnail(imagePath);
        return mThumbnailFactory.compressThumbnail(thumbnailBmp,
                getThumbnailFile(null).getPath());
    }
}
