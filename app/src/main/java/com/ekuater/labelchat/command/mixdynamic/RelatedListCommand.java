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
 * @author Xu wenxiang
 */
public class RelatedListCommand extends UserCommand {

    private static final String URL = CommandUrl.MIX_DYNAMIC_RELATED_LIST;

    public RelatedListCommand() {
        super();
        setUrl(URL);
    }

    public RelatedListCommand(String session, String userId) {
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
