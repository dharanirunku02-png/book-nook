package com.booknook.exception;

/**
 * Thrown when a user tries to add a book that is already in their wishlist.
 * Returns HTTP 409 Conflict.
 */
public class DuplicateWishlistException extends RuntimeException {

    public DuplicateWishlistException(Long userId, Long bookId) {
        super("Book " + bookId + " is already in the wishlist of user " + userId + ".");
    }
}
