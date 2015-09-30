package com.ekuater.labelchat.command.throwphoto;

import android.support.v4.util.ArrayMap;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.ThrowPhoto;
import com.ekuater.labelchat.http.multipart.FilePart;
import com.ekuater.labelchat.http.multipart.MultipartEntity;
import com.ekuater.labelchat.http.multipart.Part;
import com.ekuater.labelchat.http.multipart.StringPart;

import org.apache.http.HttpEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Leo on 2015/1/5.
 *
 * @author LinYong
 */
public class ThrowCommand {

    private static final String URL = CommandUrl.THROW_PHOTO_THROW;

    private final Map<String, String> paramMap = new ArrayMap<>();
    private final List<File> photoList = new ArrayList<>();

    public ThrowCommand(String session, String userId) {
        putParam(CommandFields.Base.CLIENT_TYPE,
                CommandFields.Base.DEFAULT_CLIENT_TYPE);
        putParam(CommandFields.Base.INTERFACE_VERSION,
                CommandFields.Base.DEFAULT_INTERFACE_VERSION);
        putParam(CommandFields.Base.SESSION, session);
        putParam(CommandFields.User.USER_ID, userId);
    }

    private void putParam(String name, String value) {
        paramMap.put(name, value);
    }

    public void putParamLocation(LocationInfo location) {
        putParam(CommandFields.User.LONGITUDE, String.valueOf(location.getLongitude()));
        putParam(CommandFields.User.LATITUDE, String.valueOf(location.getLatitude()));
    }

    public void addPhoto(File photo) throws FileNotFoundException {
        if (photo == null) {
            throw new NullPointerException("ThrowCommand null photo");
        }

        if (!photo.isFile()) {
            throw new FileNotFoundException("ThrowCommand photo not exist");
        }

        photoList.add(photo);
    }

    public HttpEntity toEntity() {
        List<Part> parts = new ArrayList<>();

        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            parts.add(new StringPart(entry.getKey(), entry.getValue(), HTTP.UTF_8));
        }
        for (File photo : photoList) {
            try {
                parts.add(new FilePart(CommandFields.Normal.FILE, photo));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new MultipartEntity(parts.toArray(new Part[parts.size()]));
    }

    public String getUrl() {
        return URL;
    }

    public static class CommandResponse extends BaseCommand.CommandResponse {

        public CommandResponse(JSONObject response) {
            super(response);
        }

        public ThrowPhoto getThrowPhoto() {
            return ThrowCmdUtils.toThrowPhoto(getValueJson(
                    CommandFields.ThrowPhoto.THROW_PHOTO));
        }
    }
}
