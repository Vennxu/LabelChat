
package com.ekuater.labelchat.coreservice;

import android.os.Looper;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.coreservice.command.ICommandResponseHandler;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.datastruct.SystemPush;

/**
 * @author LinYong
 */
public interface ICoreServiceCallback {

    /**
     * Send new chat message
     *
     * @param chatMessage the new chat message to be sent
     */
    public void sendNewChatMessage(ChatMessage chatMessage);

    /**
     * execute command
     *
     * @param command command to be executed
     * @param handler response handler
     */
    public void executeCommand(RequestCommand command, ICommandResponseHandler handler);

    /**
     * Pre-treat command, such as add session id to command.
     *
     * @param command command to be pre-treated
     * @return pre-treated command
     */
    public BaseCommand preTreatCommand(BaseCommand command);

    /**
     * Get current login account communication session
     *
     * @return current login account communication session.
     */
    public String getAccountSession();

    /**
     * Get current login account user id
     *
     * @return current login account user id
     */
    public String getAccountUserId();

    /**
     * Get current login account label code
     *
     * @return current login account label code
     */
    public String getAccountLabelCode();

    /**
     * Get current login account password
     *
     * @return current login account password
     */
    public String getAccountPassword();

    /**
     * Executes the given task at some time in the future.
     *
     * @param task the runnable task
     */
    public void execute(Runnable task);

    /**
     * Get current network available or not
     *
     * @return current network available
     */
    public boolean isNetworkAvailable();

    /**
     * Run task in main thread of CoreService
     *
     * @param r     task
     * @param delay delay
     */
    public void runDelayed(Runnable r, long delay);

    /**
     * Run task in process thread of CoreService
     *
     * @param r     task
     * @param delay delay
     */
    public void runDelayedInProcess(Runnable r, long delay);

    /**
     * Get Looper of process thread in CoreService
     */
    public Looper getProcessLooper();

    /**
     * Clear all chat messages of userId
     *
     * @param userId userId
     */
    public void clearChatHistory(String userId);

    /**
     * Add a new ChatMessage to chat history database
     *
     * @param chatMessage new chat message
     */
    public void addNewChatMessage(ChatMessage chatMessage);

    public void notifyCoreService(CoreServiceNotifier notifier);
}
