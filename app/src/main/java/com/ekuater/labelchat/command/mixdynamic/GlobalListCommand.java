package com.ekuater.labelchat.command.mixdynamic;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.mixdynamic.DynamicWrapper;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Leo on 2015/4/16.
 *
 * @author LinYong
 */
public class GlobalListCommand extends UserCommand {

    private static final String URL = CommandUrl.MIX_DYNAMIC_GLOBAL_LIST;

    public GlobalListCommand() {
        super();
        setUrl(URL);
    }

    public GlobalListCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamRequestTime(String requestTime) {
        putParam(CommandFields.Normal.REQUEST_TIME, requestTime);
    }

    public void putParamFilter(JSONArray filter) {
        putParam(CommandFields.Dynamic.FILTERS, filter);
    }

    public void putParamFilter(FilterBuilder builder) {
        putParamFilter(builder.build());
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public DynamicWrapper[] getWrappers() {
            return MixDynamicCmdUtils.toWrapperArray(getValueJsonArray(
                    CommandFields.Dynamic.DYNAMIC_ARRAY));
        }
    }
}
