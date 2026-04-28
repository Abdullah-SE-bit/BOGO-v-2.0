package org.BOGO.service;

import org.BOGO.domain.common.PersonalDetails;
import org.BOGO.domain.user.User;
import org.BOGO.repository.UserRepository;

public class AuthService {

    private final UserRepository userRepository = new UserRepository();
    


    /**
     * Authenticates a user by email and password.
     * Returns the User object on success, null if credentials are invalid.
     */
    public User login(String email, String password) {
        return null;
    }

    /**
     * Invalidates the session token for the given user ID.
     */
    public void logout(int userID) {}

    /**
     * Registers a new user account with the provided details and role.
     * Returns the persisted User object.
     */
    public User register(PersonalDetails details, String role) {
        return null;
    }

    /**
     * Validates whether the given session token is still active and legitimate.
     */
    public boolean validateSession(String token) {
        return false;
    }

    /**
     * Initiates a password reset flow for the given email address.
     */
    public void resetPassword(String email) {}
}
