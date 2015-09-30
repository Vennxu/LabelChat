package com.ekuater.daogen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.daogenerator.DaoGenerator;

public class Generator {

    private static final String OUT_DIR = "app/java-gen";
    private static final List<Class<? extends IDBSchema>> sDBSchemaList;

    static {
        sDBSchemaList = new ArrayList<>();
        sDBSchemaList.add(PendingChatDBSchema.class);
        sDBSchemaList.add(StrangerDBSchema.class);
        sDBSchemaList.add(LiteStrangerDBSchema.class);
        sDBSchemaList.add(FollowUserDBSchema.class);
    }

    public static void main(String args[]) throws Exception {
        File outDir = new File(OUT_DIR);
        DaoGenerator daoGenerator = new DaoGenerator();

        if (outDir.exists() || outDir.mkdirs()) {
            for (Class<? extends IDBSchema> clazz : sDBSchemaList) {
                IDBSchema dbSchema = clazz.newInstance();
                daoGenerator.generateAll(dbSchema.getSchema(), OUT_DIR);
            }
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                if (!deleteDir(new File(dir, child))) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
}
