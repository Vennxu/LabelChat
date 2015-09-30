package com.ekuater.labelchat.coreservice.tmpgroup;

import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.TmpGroupTime;

/**
 * @author LinYong
 */
public interface ITmpGroupListener {

    /**
     * Group create result
     *
     * @param result result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
     * @param labels group labels
     * @param group  new create group information
     */
    public void onCreateGroupRequestResult(int result, BaseLabel[] labels, TmpGroup group);

    /**
     * Group dismiss result
     *
     * @param result  result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
     * @param groupId groupId
     */
    public void onDismissGroupRequestResult(int result, String groupId);

    /**
     * Query group information from server result
     *
     * @param result  result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
     * @param groupId groupId
     * @param group   group information
     */
    public void onQueryGroupInfoResult(int result, String groupId, TmpGroup group);

    /**
     * Member quit group request result
     *
     * @param result  result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
     * @param groupId groupId
     */
    public void onQuitGroupResult(int result, String groupId);

    /**
     * Get group system time result
     *
     * @param result    result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
     * @param groupId   groupId
     * @param groupTime group time return from server
     */
    public void onQueryGroupSystemTimeResult(int result, String groupId, TmpGroupTime groupTime);

    /**
     * @param groupId       groupId
     * @param timeRemaining group dismiss time remaining
     */
    public void onGroupDismissRemind(String groupId, long timeRemaining);
}
