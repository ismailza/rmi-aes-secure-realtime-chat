package ma.fstm.ilisi.realtimechat.common;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a user in the real-time chat application.
 */
public class User implements Serializable {
    private String username;
    private boolean isOnline;

    /**
     * Constructs a new User with the specified username.
     * The user is set to be online by default.
     *
     * @param username the username of the user
     */
    public User(String username){
        this.username = username;
        this.isOnline = true;
    }

    /**
     * Returns the username of the user.
     *
     * @return the username of the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the user.
     *
     * @param username the new username of the user
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns whether the user is online.
     *
     * @return true if the user is online, false otherwise
     */
    public boolean isOnline() {
        return isOnline;
    }

    /**
     * Sets the online status of the user.
     *
     * @param online the new online status of the user
     */
    public void setOnline(boolean online){
        this.isOnline = online;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two users are considered equal if they have the same username.
     *
     * @param object the reference object with which to compare
     * @return true if this object is the same as the object argument, false otherwise
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        User user = (User) object;
        return Objects.equals(username, user.username);
    }

    /**
     * Returns a hash code value for the user.
     *
     * @return a hash code value for this user
     */
    @Override
    public int hashCode() {
        return Objects.hash(username, isOnline);
    }
}