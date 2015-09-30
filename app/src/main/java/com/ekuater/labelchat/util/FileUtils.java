package com.ekuater.labelchat.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author LinYong
 */
public final class FileUtils {

    public static boolean deleteFolder(File folder) {
        return deleteFilesUnder(folder) && folder.delete();
    }

    public static boolean deleteFilesUnder(File folder) {
        if (!folder.exists() || !folder.isDirectory()) {
            return true;
        }

        boolean flag = true;

        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                if (!file.delete()) {
                    flag = false;
                }
            } else if (file.isDirectory()) {
                if (!deleteFolder(file)) {
                    flag = false;
                }
            }
        }

        return flag;
    }

    public static boolean copyFile(File src, File dest) {
        InputStream in = null;
        OutputStream out = null;

        File destParent = dest.getParentFile();
        if (!destParent.exists() && !destParent.mkdirs()) {
            return false;
        }

        boolean _ret = false;
        // copy image file
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dest);
            byte[] buf = new byte[1024 * 5];
            int len;

            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
            _ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return _ret;
    }
}
