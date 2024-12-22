package ma.fstm.ilisi.realtimechat.client;

import ma.fstm.ilisi.realtimechat.common.User;
import java.util.List;

/**
 * Interface representing the controller for the chat client.
 * Handles initialization, sending and receiving messages, updating user lists, and disconnecting.
 */
public interface IChatController {

    /**
     * Initializes the chat client with the specified username.
     *
     * @param username the username of the current user
     */
    void initialize(String username);

    /**
     * Sends a message to a specified receiver.
     *
     * @param message the message to be sent
     * @param receiver the user who will receive the message
     */
    void sendMessage(String message, User receiver);

    /**
     * Receives a message from a sender.
     *
     * @param message the message to be received
     * @param sender the user who sent the message
     */
    void receiveMessage(String message, User sender);

    /**
     * Updates the list of users in the chat.
     *
     * @param users the list of users to be updated
     */
    void updateUserList(List<User> users);

    /**
     * Disconnects the chat client.
     */
    void disconnect();
}