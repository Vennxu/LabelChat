package com.ekuater.labelchat.command.tag;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.datastruct.TagType;

import org.json.JSONException;

/**
 * Created by Leo on 2015/3/15.
 *
 * @author LinYong
 */
public class ListTagTypeCommand extends BaseCommand {

    private static final String URL = CommandUrl.TAG_LIST_TAG_TYPE;

    public ListTagTypeCommand() {
        super();
        setUrl(URL);
    }

    public static class CommandResponse extends BaseCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public TagType[] getTagTypes() {
            return TagCmdUtils.toTagTypeArray(getValueJsonArray(CommandFields.Tag.TAG_TYPE_ARRAY));
        }
    }
}
