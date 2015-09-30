package com.ekuater.labelchat.datastruct.mixdynamic;

import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.LabelStory;

/**
 * Created by Leo on 2015/4/18.
 *
 * @author LinYong
 */
public final class WrapperUtils {

    public static DynamicType toDynamicType(LabelStory story)
            throws WrapperException {
        if (story == null) {
            throw new NullPointerException("null story");
        }

        DynamicType type;

        switch (story.getType()) {
            case LabelStory.TYPE_TXT_IMG:
                type = DynamicType.TXT;
                break;
            case LabelStory.TYPE_ONLINEAUDIO:
            case LabelStory.TYPE_AUDIO:
                type = DynamicType.AUDIO;
                break;
            case LabelStory.TYPE_BANKNOTE:
                type = DynamicType.BANKNOTE;
                break;
            default:
                throw new WrapperException("unsupported story type");
        }
        return type;
    }

    public static DynamicWrapper fromStory(LabelStory story)
            throws WrapperException {
        if (story == null) {
            throw new NullPointerException("null story");
        }

        DynamicType type = toDynamicType(story);
        DynamicWrapper wrapper = new DynamicWrapper();
        wrapper.setObjectId(story.getLabelStoryId());
        wrapper.setDynamic(story);
        wrapper.setTime(story.getCreateDate());
        wrapper.setType(type);
        return wrapper;
    }

    public static DynamicWrapper fromConfide(Confide confide) {
        if (confide == null) {
            throw new NullPointerException("null confide");
        }

        DynamicWrapper wrapper = new DynamicWrapper();
        wrapper.setDynamic(confide);
        wrapper.setObjectId(confide.getConfideId());
        wrapper.setTime(confide.getConfideCreateDate());
        wrapper.setType(DynamicType.CONFIDE);
        return wrapper;
    }
}
