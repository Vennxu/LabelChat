package com.ekuater.labelchat.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ekuater.labelchat.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author LinYong
 */
public final class ChatThumbnailFactory {

    private static final int DEFAULT_COMPRESS_QUALITY = 90;

    private final int mThumbnailMaxHeight;
    private final int mThumbnailMaxWidth;

    public ChatThumbnailFactory(Context context) {
        Resources res = context.getResources();
        mThumbnailMaxHeight = res.getInteger(R.integer.image_msg_thumb_max_height_pixels);
        mThumbnailMaxWidth = res.getInteger(R.integer.image_msg_thumb_max_width_pixels);
    }

    private Bitmap decodeImageThumbnail(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap;

        options.inPreferredConfig = Bitmap.Config.RGB_565;

        // get image real size
        options.inJustDecodeBounds = true;
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        float realWidth = options.outWidth;
        float realHeight = options.outHeight;

        // decode the thumbnail image now
        if (realHeight > 0 && realWidth > 0) {
            int scale = (int) (Math.max(realWidth / mThumbnailMaxWidth,
                    realHeight / mThumbnailMaxHeight) + 0.5F);
            options.inSampleSize = (scale >= 1) ? scale : 1;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(imagePath, options);
        }

        return bitmap;
    }

    public Bitmap decodeThumbnail(String thumbnailPath) {
        File thumbnailFile = new File(thumbnailPath);

        if (!thumbnailFile.exists() || !thumbnailFile.isFile()) {
            return null;
        }

        return decodeImageThumbnail(thumbnailFile.getPath());
    }

    public Bitmap generateThumbnail(String imagePath) {
        File imageFile = new File(imagePath);

        if (!imageFile.exists() || !imageFile.isFile()) {
            return null;
        }

        return decodeImageThumbnail(imagePath);
    }

    public String generateThumbnailFile(String imagePath, String thumbnailPath) {
        Bitmap thumbnailBmp = generateThumbnail(imagePath);
        return compressThumbnail(thumbnailBmp, thumbnailPath);
    }

    public String compressThumbnail(Bitmap thumbnailBmp, String thumbnailPath) {
        if (thumbnailBmp == null) {
            return null;
        }

        File thumbnailFile = new File(thumbnailPath);
        File parentDir = thumbnailFile.getParentFile();
        FileOutputStream out = null;

        try {
            if (parentDir.exists() || parentDir.mkdirs()) {
                out = new FileOutputStream(thumbnailFile);
                thumbnailBmp.compress(Bitmap.CompressFormat.JPEG, DEFAULT_COMPRESS_QUALITY, out);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return thumbnailFile.getName();
    }
}
