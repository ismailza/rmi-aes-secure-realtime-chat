package ma.fstm.ilisi.realtimechat.client;

import ma.fstm.ilisi.realtimechat.common.IChatClient;
import ma.fstm.ilisi.realtimechat.common.IChatServer;
import ma.fstm.ilisi.realtimechat.common.User;
import ma.fstm.ilisi.realtimechat.common.aes.AESEncryption;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Represents a chat client in the real-time chat application.
 * Extends UnicastRemoteObject and implements IChatClient for RMI support.
 */
public class ChatClient extends UnicastRemoteObject implements IChatClient {
    private static volatile ChatClient instance;

    private IChatServer chatServer;
    private User currentUser;
    private IChatController controller;
    private AESEncryption aes;

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes the ChatClient instance.
     *
     * @throws RemoteException if a remote communication error occurs
     */
    private ChatClient() throws RemoteException {
        super();
    }

    /**
     * Returns the singleton instance of ChatClient.
     * Creates a new instance if it does not exist.
     *
     * @return the singleton instance of ChatClient
     * @throws RemoteException if a remote communication error occurs
     */
    public static ChatClient getInstance() throws RemoteException {
        if (instance == null) {
            synchronized (ChatClient.class) {
                if (instance == null) {
                    instance = new ChatClient();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes the chat client with the specified username and controller.
     * Registers the client with the chat server and initializes AES encryption.
     *
     * @param username the username of the current user
     * @param controller the controller to handle chat client operations
     */
    public void initialize(String username, IChatController controller) {
        try {
            this.currentUser = new User(username);
            this.controller = controller;

            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            this.chatServer = (IChatServer) registry.lookup("ChatService");

            // Get encryption key from server and initialize AES
            byte[] key = chatServer.getEncryptionKey();
            this.aes = new AESEncryption(key);

            // Register client with server
            this.chatServer.registerClient(this, currentUser);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize chat client", e);
        }
    }

    /**
     * Receives a message from a sender and processes it.
     * Decrypts the message if it is from a user.
     *
     * @param message the message to be received
     * @param sender the user who sent the message
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void receiveMessage(String message, User sender) throws RemoteException {
        try {
            // Check if it's a system message (from server)
            if ("Server".equals(sender.getUsername())) {
                // System messages are not encrypted
                controller.receiveMessage(message, sender);
            } else {
                // User messages are encrypted
                String decryptedMessage = aes.decrypt(message);
                controller.receiveMessage(decryptedMessage, sender);
            }
        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
        }
    }

    /**
     * Updates the list of users in the chat.
     *
     * @param users the list of users to be updated
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void updateUserList(List<User> users) throws RemoteException {
        controller.updateUserList(users);
    }

    /**
     * Sends a message to a receiver.
     * Encrypts the message before sending.
     *
     * @param message the message to be sent
     * @param receiver the user who will receive the message
     */
    public void sendMessage(String message, User receiver) {
        try {
            String encryptedMessage = aes.encrypt(message);
            chatServer.sendMessage(encryptedMessage, currentUser, receiver);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message", e);
        }
    }

    /**
     * Disconnects the chat client from the server.
     * Updates the user's online status and unregisters the client.
     */
    public void disconnect() {
        try {
            if (chatServer != null && currentUser != null) {
                currentUser.setOnline(false);
                chatServer.updateUserStatus(currentUser.getUsername(), false);
                chatServer.unregisterClient(this);
            }
        } catch (Exception e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }

    /**
     * Returns the current user.
     *
     * @return the current user
     */
    public User getCurrentUser() {
        return currentUser;
    }
}