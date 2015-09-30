package com.ekuater.labelchat.datastruct;

/**
 * Created by Leo on 2015/5/21.
 *
 * @author LinYong
 */
public class InterestTypeProperty {

    private int typeId;
    private int typeIconResId;
    private int itemColorResId;
    private int itemBgResId;

    public InterestTypeProperty(int typeId, int typeIconResId,
                                int itemColorResId, int itemBgResId) {
        this.typeId = typeId;
        this.typeIconResId = typeIconResId;
        this.itemColorResId = itemColorResId;
        this.itemBgResId = itemBgResId;
    }

    public int getTypeId() {
        return typeId;
    }

    public int getTypeIconResId() {
        return typeIconResId;
    }

    public int getItemColorResId() {
        return itemColorResId;
    }

    public int getItemBgResId() {
        return itemBgResId;
    }
}
