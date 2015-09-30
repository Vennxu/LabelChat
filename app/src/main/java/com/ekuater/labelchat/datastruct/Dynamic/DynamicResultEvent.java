package com.ekuater.labelchat.datastruct.Dynamic;

import com.ekuater.labelchat.datastruct.LabelStory;

/**
 * Created by Administrator on 2015/5/16.
 */
public class DynamicResultEvent {

    private final LabelStory labelStory;
//    private final int position;

    public DynamicResultEvent(LabelStory labelStory) {
        this.labelStory = labelStory;
//        this.position = position;
    }

    public LabelStory getLabelStory() {
        return labelStory;
    }

//    public int getPosition(){
//        return position;
//    }

}
