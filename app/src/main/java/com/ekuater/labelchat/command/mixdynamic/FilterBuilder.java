package com.ekuater.labelchat.command.mixdynamic;

import com.ekuater.labelchat.command.CommandFields;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by Leo on 2015/4/21.
 *
 * @author LinYong
 */
public class FilterBuilder {

    private final JSONArray filter;

    public FilterBuilder() {
        filter = new JSONArray();
    }

    public void addType(String type, String[] subtypes) {
        try {
            addTypeInternal(type, subtypes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addType(String type) {
        addType(type, null);
    }

    public JSONArray build() {
        return filter;
    }

    private void addTypeInternal(String type, String[] subtypes) throws JSONException {
        JSONObject typeObj = new JSONObject();
        typeObj.put(CommandFields.Dynamic.FILTER_TYPE, type);
        if (subtypes != null && subtypes.length > 0) {
            typeObj.put(CommandFields.Dynamic.FILTER_SUBTYPE,
                    new JSONArray(Arrays.asList(subtypes)));
        }
        filter.put(typeObj);
    }
}
