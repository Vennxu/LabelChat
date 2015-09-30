package com.ekuater.labelchat.notificationcenter;

/**
 * @author LinYong
 */
public interface INotificationMediator {

    public static final int SCENARIO_NORMAL = 0;
    public static final int SCENARIO_MAIN_UI = 1;
    public static final int SCENARIO_CHATTING_UI = 2;

    public void enterScenario(int scenario);

    public void exitScenario(int scenario);
}
