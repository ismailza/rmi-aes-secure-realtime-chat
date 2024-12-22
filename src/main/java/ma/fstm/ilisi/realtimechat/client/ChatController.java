package ma.fstm.ilisi.realtimechat.client;

import ma.fstm.ilisi.realtimechat.common.User;
import javax.swing.*;
import java.util.List;

/**
 * Controller class for the chat client.
 * Manages the interaction between the GUI and the chat client.
 */
public class ChatController implements IChatController {
    private final ChatGUI gui;
    private final ChatClient client;

    /**
     * Constructs a new ChatController with the specified GUI.
     * Initializes the chat client instance.
     *
     * @param gui the GUI for the chat client
     * @throws Exception if an error occurs during client initialization
     */
    public ChatController(ChatGUI gui) throws Exception {
        this.gui = gui;
        this.client = ChatClient.getInstance();
    }

    /**
     * Initializes the chat client with the specified username.
     *
     * @param username the username of the current user
     */
    @Override
    public void initialize(String username) {
        client.initialize(username, this);
    }

    /**
     * Sends a message to a specified receiver.
     *
     * @param message the message to be sent
     * @param receiver the user who will receive the message
     */
    @Override
    public void sendMessage(String message, User receiver) {
        client.sendMessage(message, receiver);
    }

    /**
     * Receives a message from a sender and displays it in the GUI.
     *
     * @param message the message to be received
     * @param sender the user who sent the message
     */
    @Override
    public void receiveMessage(String message, User sender) {
        SwingUtilities.invokeLater(() ->
                gui.displayMessage(sender.getUsername() + ": " + message, sender)
        );
    }

    /**
     * Updates the list of users in the chat.
     *
     * @param users the list of users to be updated
     */
    @Override
    public void updateUserList(List<User> users) {
        SwingUtilities.invokeLater(() ->
                gui.updateUserList(users.stream()
                        .filter(user -> !user.equals(client.getCurrentUser()))
                        .toList())
        );
    }

    /**
     * Disconnects the chat client.
     */
    @Override
    public void disconnect() {
        client.disconnect();
    }
}