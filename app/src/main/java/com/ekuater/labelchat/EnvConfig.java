
package com.ekuater.labelchat;

import android.os.Environment;

import com.ekuater.labelchat.util.L;
import com.ekuater.labelchat.util.UniqueFileName;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Global Environment configuration
 *
 * @author LinYong
 */
public final class EnvConfig {

    public static final String ROOT_DIR_NAME = "LabelChat";

    public static final File STORAGE_DIR = Environment.getExternalStorageDirectory();

    public static final File ROOT_DIR = new File(STORAGE_DIR, ROOT_DIR_NAME);

    public static final String CHAT_MSG_DIR_NAME = "ChatMsg";

    public static final File CHAT_MSG_DIR = new File(ROOT_DIR, CHAT_MSG_DIR_NAME);

    public static final String VOICE_CHAT_MSG_DIR_NAME = "voice";

    public static final String IMAGE_CHAT_MSG_DIR_NAME = "image";

    public static final String IMAGE_CHAT_MSG_THUMBNAIL_DIR_NAME = "thumbnail";

    public static final String CONTACT_AVATAR_DIR_NAME = "ContactAvatar";

    public static final File CONTACT_AVATAR_DIR = new File(ROOT_DIR, CONTACT_AVATAR_DIR_NAME);

    public static final String TEMP_FILE_DIR_NAME = "temp";

    public static final File TEMP_FILE_DIR = new File(ROOT_DIR, TEMP_FILE_DIR_NAME);

    static {
        makeEnvDirs();
    }

    private static final String TAG = EnvConfig.class.getSimpleName();

    private static void makeEnvDirs() {
        if (!ROOT_DIR.exists()) {
            if (!ROOT_DIR.mkdirs()) {
                L.d(TAG, "makeEnvDirs(), make root dir failed");
            }
        }

        File noMediaFile = new File(ROOT_DIR, ".nomedia");
        if (!noMediaFile.exists()) {
            // make no media flag file.
            try {
                FileOutputStream out = new FileOutputStream(noMediaFile);
                out.write("no media flag".getBytes());
                out.flush();
                out.close();
            } catch (Exception e) {
                L.d(TAG, "makeEnvDirs(), make no media flag file failed");
            }
        }
    }

    public static File getChatMsgDirectory(String userId) {
        return new File(CHAT_MSG_DIR, String.valueOf(userId));
    }

    public static File getVoiceChatMsgDirectory(String userId) {
        return new File(getChatMsgDirectory(userId), VOICE_CHAT_MSG_DIR_NAME);
    }

    public static File getImageChatMsgDirectory(String userId) {
        return new File(getChatMsgDirectory(userId), IMAGE_CHAT_MSG_DIR_NAME);
    }

    public static File getImageChatMsgThumbnailDirectory(String userId) {
        return new File(getImageChatMsgDirectory(userId),
                IMAGE_CHAT_MSG_THUMBNAIL_DIR_NAME);
    }

    public static File genTempFile(String extension) {
        final File parent = TEMP_FILE_DIR;

        if (parent.exists() || parent.mkdirs()) {
            return new File(parent, UniqueFileName.getUniqueFileName(extension));
        } else {
            return null;
        }
    }
}
