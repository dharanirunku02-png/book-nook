package com.booknook.repository;

import com.booknook.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * WishlistRepository — custom queries for the wishlist business rules.
 */
@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    /**
     * Load all wishlist items for a user, together with the Book data.
     * JOIN FETCH avoids the "N+1 query" problem — it fetches item + book in one SQL.
     */
    @Query("SELECT wi FROM WishlistItem wi JOIN FETCH wi.book WHERE wi.user.id = :userId")
    List<WishlistItem> findByUserId(@Param("userId") Long userId);

    /**
     * Count how many books are in a user's wishlist.
     * Used to enforce the 10-book limit.
     */
    long countByUserId(Long userId);

    /**
     * Check if a specific book is already in a user's wishlist.
     * Returns true/false quickly without loading any objects.
     */
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    /**
     * Remove a specific wishlist entry identified by user + book.
     * @Modifying tells Spring Data this is a write operation (not a SELECT).
     * @Transactional ensures the delete runs within a transaction.
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByUserIdAndBookId(Long userId, Long bookId);
}
