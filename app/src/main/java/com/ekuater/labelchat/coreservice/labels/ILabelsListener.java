
package com.ekuater.labelchat.coreservice.labels;

/**
 * @author LinYong
 */
public interface ILabelsListener {

    /**
     * notify the user labels has been updated.
     */
    public void onLabelUpdated();

    /**
     * notify the label add operation result.
     */
    public void onLabelAdded(int result);

    /**
     * notify the label delete operation result.
     */
    public void onLabelDeleted(int result);
}
