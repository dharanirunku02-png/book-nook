package com.booknook.exception;

/**
 * Thrown when a user tries to add a book but already has 10 or more books.
 * Returns HTTP 409 Conflict.
 */
public class WishlistLimitException extends RuntimeException {

    public WishlistLimitException() {
        super("Wishlist limit reached. Remove a book to add a new one.");
    }
}
