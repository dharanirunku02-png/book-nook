package com.booknook.controller;

import com.booknook.dto.BookDTO;
import com.booknook.dto.UserDTO;
import com.booknook.entity.Book;
import com.booknook.repository.UserRepository;
import com.booknook.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * BookController — HTTP layer for books, categories, and users.
 * Thin controller: just validate, call service, return response.
 *
 * Base path: /api
 */
@RestController
@RequestMapping("/api")
public class BookController {

    private final BookService    bookService;
    private final UserRepository userRepo;   // used only for the GET /api/users endpoint

    public BookController(BookService bookService, UserRepository userRepo) {
        this.bookService = bookService;
        this.userRepo    = userRepo;
    }

    // ─── GET /api/books ───────────────────────────────────────────────────────

    /**
     * Returns all books, with optional query parameters:
     *   ?search=harry       → title or author contains "harry"
     *   ?category=Fiction   → books in Fiction only
     *   ?sort=price_asc     → sorted cheapest first
     *   ?sort=price_desc    → sorted most expensive first
     */
    @GetMapping("/books")
    public ResponseEntity<List<BookDTO>> getBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort) {

        return ResponseEntity.ok(bookService.getBooks(search, category, sort));
    }

    // ─── GET /api/books/{id} ──────────────────────────────────────────────────

    @GetMapping("/books/{id}")
    public ResponseEntity<BookDTO> getBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    // ─── POST /api/books ──────────────────────────────────────────────────────

    /**
     * Create a new book.
     * @Valid triggers the validation annotations on the Book entity
     * (title not blank, price > 0, stock >= 0).
     */
    @PostMapping("/books")
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody Book book) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(book));
    }

    // ─── PUT /api/books/{id} ──────────────────────────────────────────────────

    @PutMapping("/books/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id,
                                              @Valid @RequestBody Book book) {
        return ResponseEntity.ok(bookService.updateBook(id, book));
    }

    // ─── DELETE /api/books/{id} ───────────────────────────────────────────────

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();   // 204 No Content
    }

    // ─── GET /api/categories ──────────────────────────────────────────────────

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(bookService.getCategories());
    }

    // ─── GET /api/users ───────────────────────────────────────────────────────

    /** Returns all users so the frontend can populate the user-switcher. */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getUsers() {
        List<UserDTO> users = userRepo.findAll()
            .stream()
            .map(UserDTO::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}
