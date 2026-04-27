package org.BOGO.service;

import org.BOGO.domain.communication.Message;
import org.BOGO.domain.communication.Notification;
import org.BOGO.domain.user.User;
import org.BOGO.repository.UserRepository;
import java.util.List;

public class NotificationService {

    private final UserRepository userRepository;

    public NotificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates and delivers a notification to a single user.
     */
    public void sendNotification(User user, Message message) {}

    /**
     * Broadcasts a notification to a list of users simultaneously.
     */
    public void sendBulkNotification(List<User> users, Message message) {}

    /**
     * Marks the given notification as read.
     */
    public void markNotificationRead(int notificationID) {}

    /**
     * Returns all unread notifications for the given user ID.
     */
    public List<Notification> getUnreadNotifications(int userID) {
        return null;
    }
}
