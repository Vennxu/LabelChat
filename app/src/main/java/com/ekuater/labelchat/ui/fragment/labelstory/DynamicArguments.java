package com.ekuater.labelchat.ui.fragment.labelstory;

import com.ekuater.labelchat.datastruct.LabelStory;

/**
 * Created by Administrator on 2015/5/8.
 *
 * @author FanChong
 */

public class DynamicArguments {
    private LabelStory labelStory;
    private int tag=0;
    private boolean isShowFragment = false;
    private boolean isShowTitle = false;
    private boolean isPraise = false;
    private boolean isComment = false;
    private boolean isShowKeyBroad = false;

    public boolean isPraise() {
        return isPraise;
    }

    public void setIsPraise(boolean isPraise) {
        this.isPraise = isPraise;
    }

    public boolean isComment() {
        return isComment;
    }

    public void setIsComment(boolean isComment) {
        this.isComment = isComment;
    }

    public boolean isShowKeyBroad() {
        return isShowKeyBroad;
    }

    public void setIsShowKeyBroad(boolean isShowKeyBroad) {
        this.isShowKeyBroad = isShowKeyBroad;
    }

    public boolean isShowTitle() {
        return isShowTitle;
    }

    public void setIsShowTitle(boolean isShowTitle) {
        this.isShowTitle = isShowTitle;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public boolean isShowFragment() {
        return isShowFragment;
    }

    public void setIsShowFragment(boolean isShowFragment) {
        this.isShowFragment = isShowFragment;
    }

    public LabelStory getLabelStory() {
        return labelStory;
    }

    public void setLabelStory(LabelStory labelStory) {
        this.labelStory = labelStory;
    }


}
