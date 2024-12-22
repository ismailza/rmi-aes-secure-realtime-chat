package ma.fstm.ilisi.realtimechat.server;

import ma.fstm.ilisi.realtimechat.common.IChatClient;
import ma.fstm.ilisi.realtimechat.common.IChatServer;
import ma.fstm.ilisi.realtimechat.common.User;
import ma.fstm.ilisi.realtimechat.common.aes.AESEncryption;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the chat server in the real-time chat application.
 * Extends UnicastRemoteObject and implements IChatServer for RMI support.
 */
public class ChatServer extends UnicastRemoteObject implements IChatServer {
    private final Map<IChatClient, User> connectedClients;
    private final Map<String, IChatClient> clientsByUsername;
    private final User serverUser;
    private final AESEncryption aes;

    /**
     * Constructs a new ChatServer instance.
     * Initializes the maps for connected clients and clients by username, and sets up the server user and AES encryption.
     *
     * @throws RemoteException if a remote communication error occurs
     */
    public ChatServer() throws RemoteException {
        this.connectedClients = new ConcurrentHashMap<>();
        this.clientsByUsername = new ConcurrentHashMap<>();
        this.serverUser = new User("Server");
        this.aes = new AESEncryption();
    }

    /**
     * Registers a new client with the chat server.
     * Adds the client to the connected clients map and broadcasts the updated user list and a join message.
     *
     * @param client the chat client to be registered
     * @param user the user associated with the client
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public synchronized void registerClient(IChatClient client, User user) throws RemoteException {
        connectedClients.put(client, user);
        clientsByUsername.put(user.getUsername(), client);
        broadcastUserList();
        broadcastMessage("User " + user.getUsername() + " has joined the chat");
    }

    /**
     * Unregisters a client from the chat server.
     * Removes the client from the connected clients map and broadcasts the updated user list and a leave message.
     *
     * @param client the chat client to be unregistered
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public synchronized void unregisterClient(IChatClient client) throws RemoteException {
        User user = connectedClients.get(client);
        if (user != null) {
            user.setOnline(false);
            connectedClients.remove(client);
            clientsByUsername.remove(user.getUsername());
            broadcastUserList();
            broadcastMessage("User " + user.getUsername() + " has left the chat");
        }
    }

    /**
     * Updates the online status of a user.
     * Broadcasts the updated user list if the user is found.
     *
     * @param username the username of the user
     * @param isOnline the new online status of the user
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void updateUserStatus(String username, boolean isOnline) throws RemoteException {
        IChatClient client = clientsByUsername.get(username);
        if (client != null) {
            User user = connectedClients.get(client);
            if (user != null) {
                user.setOnline(isOnline);
                broadcastUserList();
            }
        }
    }

    /**
     * Sends a message from a sender to a receiver.
     *
     * @param message the message to be sent
     * @param sender the user who sent the message
     * @param receiver the user who will receive the message
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void sendMessage(String message, User sender, User receiver) throws RemoteException {
        IChatClient receiverClient = clientsByUsername.get(receiver.getUsername());
        if (receiverClient != null) {
            receiverClient.receiveMessage(message, sender);
        }
    }

    /**
     * Returns the encryption key used by the server.
     *
     * @return the encryption key
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public byte[] getEncryptionKey() throws RemoteException {
        return aes.getKey();
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param message the message to be broadcasted
     */
    private void broadcastMessage(String message) {
        connectedClients.keySet().forEach(client -> {
            try {
                client.receiveMessage(message, serverUser);
            } catch (RemoteException e) {
                System.err.println("Failed to send message to client: " + e.getMessage());
                try {
                    unregisterClient(client);
                } catch (RemoteException ex) {
                    System.err.println("Failed to unregister client: " + ex.getMessage());
                }
            }
        });
    }

    /**
     * Broadcasts the updated user list to all connected clients.
     */
    private void broadcastUserList() {
        List<User> users = new ArrayList<>(connectedClients.values());
        connectedClients.keySet().forEach(client -> {
            try {
                client.updateUserList(users);
            } catch (RemoteException e) {
                System.err.println("Failed to update user list for client: " + e.getMessage());
                try {
                    unregisterClient(client);
                } catch (RemoteException ex) {
                    System.err.println("Failed to unregister client: " + ex.getMessage());
                }
            }
        });
    }

    /**
     * Main method to start the chat server.
     * Creates a new ChatServer instance, binds it to the RMI registry, and starts the server.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            ChatServer server = new ChatServer();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("ChatService", server);
            System.out.println("Chat Server is running...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}