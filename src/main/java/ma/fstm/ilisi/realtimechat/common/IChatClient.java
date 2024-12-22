package ma.fstm.ilisi.realtimechat.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface representing a chat client in the real-time chat application.
 * Extends the Remote interface to support remote method invocation (RMI).
 */
public interface IChatClient extends Remote {

    /**
     * Receives a message from a sender.
     *
     * @param message the message to be received
     * @param sender the user who sent the message
     * @throws RemoteException if a remote communication error occurs
     */
    void receiveMessage(String message, User sender) throws RemoteException;

    /**
     * Updates the list of users in the chat.
     *
     * @param users the list of users to be updated
     * @throws RemoteException if a remote communication error occurs
     */
    void updateUserList(List<User> users) throws RemoteException;
}