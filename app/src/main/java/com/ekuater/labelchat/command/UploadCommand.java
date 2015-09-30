package com.ekuater.labelchat.command;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Leo on 2015/3/18.
 *
 * @author LinYong
 */
public class UploadCommand {

    private final Map<String, String> paramMap = new ArrayMap<>();
    private final List<File> fileList = new ArrayList<>();
    private String url = null;

    public UploadCommand(String session, String userId) {
        putParam(CommandFields.Base.CLIENT_TYPE,
                CommandFields.Base.DEFAULT_CLIENT_TYPE);
        putParam(CommandFields.Base.INTERFACE_VERSION,
                CommandFields.Base.DEFAULT_INTERFACE_VERSION);
        putParam(CommandFields.Base.SESSION, session);
        putParam(CommandFields.User.USER_ID, userId);
    }

    public void cleanParams() {
        paramMap.clear();
    }

    public void putParam(String name, String value) {
        if (name == null) {
            throw new NullPointerException("putParam(), null name");
        }
        if (value == null) {
            throw new NullPointerException("putParam(), null value");
        }
        paramMap.put(name, value);
    }

    public void addFile(File file) throws FileNotFoundException {
        if (file == null) {
            throw new NullPointerException("UploadCommand null photo");
        }
        if (!file.isFile()) {
            throw new FileNotFoundException("UploadCommand photo not exist");
        }
        fileList.add(file);
    }

    public void clearFiles() {
        fileList.clear();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @NonNull
    public Map<String, String> getParamMap() {
        return paramMap;
    }

    @NonNull
    public List<File> getFileList() {
        return fileList;
    }

    public static class CommandResponse extends BaseCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public CommandResponse(JSONObject response) {
            super(response);
        }
    }
}
