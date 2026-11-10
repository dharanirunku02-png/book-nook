package com.booknook.exception;

/**
 * Thrown when a book with the given id is not found in the database.
 * RuntimeException means we don't have to declare it in every method signature.
 */
public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("Book not found with id: " + id);
    }
}
