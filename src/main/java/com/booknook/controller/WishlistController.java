package com.booknook.controller;

import com.booknook.dto.WishlistResponseDTO;
import com.booknook.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * WishlistController — HTTP layer for wishlist operations.
 * All business rules are in WishlistService — this class only routes requests.
 *
 * Base path: /api/wishlist
 */
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    // ─── GET /api/wishlist/{userId} ───────────────────────────────────────────

    /**
     * Returns the user's wishlist with book list, count, limit (10), and over_limit flag.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<WishlistResponseDTO> getWishlist(@PathVariable Long userId) {
        return ResponseEntity.ok(wishlistService.getWishlist(userId));
    }

    // ─── POST /api/wishlist/{userId}/{bookId} ─────────────────────────────────

    /**
     * Add a book to the user's wishlist.
     * Returns updated wishlist. Business rules (limit, duplicate) enforced in service.
     */
    @PostMapping("/{userId}/{bookId}")
    public ResponseEntity<WishlistResponseDTO> addToWishlist(@PathVariable Long userId,
                                                             @PathVariable Long bookId) {
        return ResponseEntity.ok(wishlistService.addToWishlist(userId, bookId));
    }

    // ─── DELETE /api/wishlist/{userId}/{bookId} ───────────────────────────────

    /**
     * Remove a book from the user's wishlist.
     * Allowed even when over_limit (the user is cleaning up).
     */
    @DeleteMapping("/{userId}/{bookId}")
    public ResponseEntity<WishlistResponseDTO> removeFromWishlist(@PathVariable Long userId,
                                                                   @PathVariable Long bookId) {
        return ResponseEntity.ok(wishlistService.removeFromWishlist(userId, bookId));
    }
}
