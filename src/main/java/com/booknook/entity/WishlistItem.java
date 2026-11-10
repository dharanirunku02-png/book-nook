package com.booknook.entity;

import jakarta.persistence.*;

/**
 * WishlistItem entity — links a User to a Book they want to save.
 *
 * The @UniqueConstraint on (user_id, book_id) makes it impossible for
 * the same book to appear twice in one user's wishlist at the database level.
 * We also check this in the service layer for a friendly error message.
 */
@Entity
@Table(
    name = "wishlist_item",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_book",
        columnNames = {"user_id", "book_id"}
    )
)
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many wishlist items → one user.
     * FetchType.LAZY means the user object is loaded only when we access it,
     * which saves a database query when we just need the item id.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Many wishlist items → one book. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // ─── Constructors ────────────────────────────────────────────────────────

    public WishlistItem() {}

    public WishlistItem(User user, Book book) {
        this.user = user;
        this.book = book;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public Long getId()           { return id; }
    public void setId(Long id)    { this.id = id; }

    public User getUser()         { return user; }
    public void setUser(User u)   { this.user = u; }

    public Book getBook()         { return book; }
    public void setBook(Book b)   { this.book = b; }
}
