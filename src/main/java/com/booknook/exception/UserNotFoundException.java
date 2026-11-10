package com.booknook.exception;

/**
 * Thrown when a user with the given id is not found in the database.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
}
