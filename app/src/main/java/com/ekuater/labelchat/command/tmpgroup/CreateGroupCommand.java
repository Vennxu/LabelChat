package com.ekuater.labelchat.command.tmpgroup;

import android.text.TextUtils;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.command.labels.LabelCmdUtils;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class CreateGroupCommand extends UserCommand {

    private static final String TAG = CreateGroupCommand.class.getSimpleName();
    private static final String URL = CommandUrl.TMP_GROUP_CREATE;

    public CreateGroupCommand() {
        super();
        setUrl(URL);
    }

    public CreateGroupCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamLabels(BaseLabel[] labels) {
        JSONArray jsonArray = LabelCmdUtils.toJsonArray(labels);

        if (jsonArray != null) {
            putParam(CommandFields.TmpGroup.LABEL_ARRAY, jsonArray);
        }
    }

    public void putParamMembers(String[] memberUserIds) {
        if (memberUserIds == null || memberUserIds.length == 0) {
            return;
        }

        JSONArray memberArray = new JSONArray();

        for (String userId : memberUserIds) {
            if (!TextUtils.isEmpty(userId)) {
                JSONObject json = new JSONObject();
                try {
                    json.put(CommandFields.Stranger.USER_ID, userId);
                    memberArray.put(json);
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
            }
        }

        putParam(CommandFields.TmpGroup.USER_ARRAY, memberArray);
    }

    public void putParamNickname(String nickname) {
        putParam(CommandFields.User.NICKNAME, nickname);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public TmpGroup getGroup() {
            return TmpGroupCmdUtils.toTmpGroup(getValueJson(CommandFields.TmpGroup.GROUP));
        }
    }
}
