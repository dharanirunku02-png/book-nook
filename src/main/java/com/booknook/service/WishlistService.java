package com.booknook.service;

import com.booknook.dto.BookDTO;
import com.booknook.dto.WishlistResponseDTO;
import com.booknook.entity.Book;
import com.booknook.entity.User;
import com.booknook.entity.WishlistItem;
import com.booknook.exception.*;
import com.booknook.repository.BookRepository;
import com.booknook.repository.UserRepository;
import com.booknook.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * WishlistService — all wishlist business rules are enforced here.
 *
 * Rules:
 *   1. Max 10 books per user. If count >= 10, reject with 409.
 *   2. No duplicates. Same book twice → 409.
 *   3. If count > 10 (data injected directly), over_limit = true.
 *      User can still remove, but cannot add.
 *   4. 404 when user or book is not found.
 */
@Service
@Transactional
public class WishlistService {

    /** Maximum number of books allowed in a wishlist */
    public static final int WISHLIST_LIMIT = 10;

    private final WishlistRepository wishlistRepo;
    private final UserRepository     userRepo;
    private final BookRepository     bookRepo;

    public WishlistService(WishlistRepository wishlistRepo,
                           UserRepository     userRepo,
                           BookRepository     bookRepo) {
        this.wishlistRepo = wishlistRepo;
        this.userRepo     = userRepo;
        this.bookRepo     = bookRepo;
    }

    // ─── Get Wishlist ─────────────────────────────────────────────────────────

    /**
     * Returns the wishlist for a user, including count, limit, and over_limit flag.
     * Throws UserNotFoundException (404) if the user doesn't exist.
     */
    @Transactional(readOnly = true)
    public WishlistResponseDTO getWishlist(Long userId) {
        // Make sure the user exists before showing the wishlist
        if (!userRepo.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        List<WishlistItem> items = wishlistRepo.findByUserId(userId);

        // Convert each WishlistItem → BookDTO for the response
        List<BookDTO> books = items.stream()
            .map(item -> BookDTO.from(item.getBook()))
            .collect(Collectors.toList());

        return new WishlistResponseDTO(books);
    }

    // ─── Add to Wishlist ──────────────────────────────────────────────────────

    /**
     * Adds a book to a user's wishlist.
     *
     * Business rules checked in order:
     *   1. User must exist → 404
     *   2. Book must exist → 404
     *   3. Book must not already be in the list → 409 duplicate
     *   4. Count must be < WISHLIST_LIMIT → 409 limit
     */
    public WishlistResponseDTO addToWishlist(Long userId, Long bookId) {
        // Rule 1: user must exist
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        // Rule 2: book must exist
        Book book = bookRepo.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));

        // Rule 3: reject duplicates before checking limit (friendlier message)
        if (wishlistRepo.existsByUserIdAndBookId(userId, bookId)) {
            throw new DuplicateWishlistException(userId, bookId);
        }

        // Rule 4: enforce the 10-book limit
        long currentCount = wishlistRepo.countByUserId(userId);
        if (currentCount >= WISHLIST_LIMIT) {
            throw new WishlistLimitException();
        }

        // All checks passed — save the new entry
        wishlistRepo.save(new WishlistItem(user, book));

        // Return the refreshed wishlist
        return getWishlist(userId);
    }

    // ─── Remove from Wishlist ─────────────────────────────────────────────────

    /**
     * Removes a book from a user's wishlist.
     * Allowed even when over-limit (rule 3).
     * Throws 404 if the user or the item is not found.
     */
    public WishlistResponseDTO removeFromWishlist(Long userId, Long bookId) {
        // User must exist
        if (!userRepo.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        // The entry must actually be there
        if (!wishlistRepo.existsByUserIdAndBookId(userId, bookId)) {
            throw new BookNotFoundException(bookId);
        }

        wishlistRepo.deleteByUserIdAndBookId(userId, bookId);

        return getWishlist(userId);
    }
}
