package com.booknook.service;

import com.booknook.dto.WishlistResponseDTO;
import com.booknook.entity.Book;
import com.booknook.entity.User;
import com.booknook.entity.WishlistItem;
import com.booknook.exception.DuplicateWishlistException;
import com.booknook.exception.WishlistLimitException;
import com.booknook.repository.BookRepository;
import com.booknook.repository.UserRepository;
import com.booknook.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WishlistServiceTest — unit tests for all wishlist business rules.
 *
 * We use Mockito to fake the repositories so we don't need a real database.
 * This makes the tests fast and focused on just the business logic.
 */
@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    // ── Mocked dependencies (fake repositories) ───────────────────────────────
    @Mock private WishlistRepository wishlistRepo;
    @Mock private UserRepository     userRepo;
    @Mock private BookRepository     bookRepo;

    // ── The service we are testing (gets mocks injected by Mockito) ───────────
    @InjectMocks
    private WishlistService wishlistService;

    // ── Reusable test objects ─────────────────────────────────────────────────
    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = new User("Test User");
        user.setId(1L);

        book = new Book("Test Book", "Test Author", "Fiction", 299.0, 10);
        book.setId(42L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 1: Happy path — successfully add a book to a wishlist with < 10 books
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("1. Add success — adding a book when count < 10 should work")
    void addToWishlist_success() {
        // Arrange: user and book exist, no duplicate, current count = 5
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepo.findById(42L)).thenReturn(Optional.of(book));
        when(wishlistRepo.existsByUserIdAndBookId(1L, 42L)).thenReturn(false);
        when(wishlistRepo.countByUserId(1L)).thenReturn(5L);
        when(wishlistRepo.save(any(WishlistItem.class))).thenReturn(new WishlistItem(user, book));

        // getWishlist is called at the end — return a list with the new book
        WishlistItem item = new WishlistItem(user, book);
        when(wishlistRepo.findByUserId(1L)).thenReturn(List.of(item));
        when(userRepo.existsById(1L)).thenReturn(true);

        // Act
        WishlistResponseDTO result = wishlistService.addToWishlist(1L, 42L);

        // Assert
        assertThat(result.getCount()).isEqualTo(1);
        assertThat(result.isOver_limit()).isFalse();
        verify(wishlistRepo).save(any(WishlistItem.class));   // save was called
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 2: Limit — adding the 11th book must throw WishlistLimitException (409)
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("2. Limit — 11th book is rejected with WishlistLimitException")
    void addToWishlist_limitReached_throws() {
        // Arrange: user and book exist, no duplicate, BUT count is already 10
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepo.findById(42L)).thenReturn(Optional.of(book));
        when(wishlistRepo.existsByUserIdAndBookId(1L, 42L)).thenReturn(false);
        when(wishlistRepo.countByUserId(1L)).thenReturn(10L);   // already at limit

        // Act + Assert
        assertThatThrownBy(() -> wishlistService.addToWishlist(1L, 42L))
            .isInstanceOf(WishlistLimitException.class)
            .hasMessageContaining("Wishlist limit reached");

        // Make sure save was NEVER called (the add was rejected)
        verify(wishlistRepo, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 3: Duplicate — adding the same book twice must throw DuplicateWishlistException
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("3. Duplicate — same book added twice is rejected with DuplicateWishlistException")
    void addToWishlist_duplicate_throws() {
        // Arrange: book is ALREADY in the wishlist
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepo.findById(42L)).thenReturn(Optional.of(book));
        when(wishlistRepo.existsByUserIdAndBookId(1L, 42L)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> wishlistService.addToWishlist(1L, 42L))
            .isInstanceOf(DuplicateWishlistException.class)
            .hasMessageContaining("already in the wishlist");

        verify(wishlistRepo, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 4: Over-limit flag — if 12 books exist, over_limit must be true
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("4. Over-limit flag — wishlist with 12 books returns over_limit=true")
    void getWishlist_overLimit_flagIsTrue() {
        // Arrange: user exists; wishlist has 12 items
        when(userRepo.existsById(1L)).thenReturn(true);

        // Build 12 fake WishlistItems each pointing to a different book
        List<WishlistItem> items = IntStream.range(0, 12).mapToObj(i -> {
            Book b = new Book("Book " + i, "Author", "Fiction", 299.0, 10);
            b.setId((long) i);
            return new WishlistItem(user, b);
        }).collect(Collectors.toList());

        when(wishlistRepo.findByUserId(1L)).thenReturn(items);

        // Act
        WishlistResponseDTO result = wishlistService.getWishlist(1L);

        // Assert
        assertThat(result.getCount()).isEqualTo(12);
        assertThat(result.isOver_limit()).isTrue();
        assertThat(result.getLimit()).isEqualTo(10);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 5: Remove — removing a book is allowed even when over the limit
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("5. Remove allowed — over-limit user can still remove a book")
    void removeFromWishlist_allowedWhenOverLimit() {
        // Arrange: user exists; item exists in wishlist
        when(userRepo.existsById(1L)).thenReturn(true);
        when(wishlistRepo.existsByUserIdAndBookId(1L, 42L)).thenReturn(true);
        doNothing().when(wishlistRepo).deleteByUserIdAndBookId(1L, 42L);

        // After removal, the list has 11 books (still over limit)
        List<WishlistItem> remaining = IntStream.range(0, 11).mapToObj(i -> {
            Book b = new Book("Book " + i, "Author", "Fiction", 299.0, 10);
            b.setId((long) i);
            return new WishlistItem(user, b);
        }).collect(Collectors.toList());
        when(wishlistRepo.findByUserId(1L)).thenReturn(remaining);

        // Act — should NOT throw any exception
        WishlistResponseDTO result = wishlistService.removeFromWishlist(1L, 42L);

        // Assert
        verify(wishlistRepo).deleteByUserIdAndBookId(1L, 42L);  // deletion happened
        assertThat(result.getCount()).isEqualTo(11);
        assertThat(result.isOver_limit()).isTrue();              // still over limit
    }
}
