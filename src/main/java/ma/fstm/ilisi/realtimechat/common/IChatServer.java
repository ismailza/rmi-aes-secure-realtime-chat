package ma.fstm.ilisi.realtimechat.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface representing a chat server for real-time communication.
 */
public interface IChatServer extends Remote {

    /**
     * Registers a client with the chat server.
     *
     * @param client the client to register
     * @param user the user associated with the client
     * @throws RemoteException if a remote communication error occurs
     */
    void registerClient(IChatClient client, User user) throws RemoteException;

    /**
     * Unregisters a client from the chat server.
     *
     * @param client the client to unregister
     * @throws RemoteException if a remote communication error occurs
     */
    void unregisterClient(IChatClient client) throws RemoteException;

    /**
     * Updates the online status of a user.
     *
     * @param username the username of the user
     * @param isOnline the online status of the user
     * @throws RemoteException if a remote communication error occurs
     */
    void updateUserStatus(String username, boolean isOnline) throws RemoteException;

    /**
     * Sends a message from one user to another.
     *
     * @param message the message to send
     * @param sender the user sending the message
     * @param receiver the user receiving the message
     * @throws RemoteException if a remote communication error occurs
     */
    void sendMessage(String message, User sender, User receiver) throws RemoteException;

    /**
     * Retrieves the encryption key used for secure communication.
     *
     * @return the encryption key as a byte array
     * @throws RemoteException if a remote communication error occurs
     */
    byte[] getEncryptionKey() throws RemoteException;
}
