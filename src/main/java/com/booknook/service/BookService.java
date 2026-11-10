package com.booknook.service;

import com.booknook.dto.BookDTO;
import com.booknook.entity.Book;
import com.booknook.exception.BookNotFoundException;
import com.booknook.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BookService — all business logic for the book catalog lives here.
 * Controllers just call service methods; they don't touch repositories directly.
 */
@Service
@Transactional          // every public method runs inside a database transaction
public class BookService {

    private final BookRepository bookRepo;

    // Spring automatically injects BookRepository through the constructor
    public BookService(BookRepository bookRepo) {
        this.bookRepo = bookRepo;
    }

    // ─── Read operations ──────────────────────────────────────────────────────

    /**
     * Search books by title/author, optionally filter by category, and sort.
     *
     * @param search   free-text search string (nullable)
     * @param category category filter (nullable)
     * @param sort     "price_asc" | "price_desc" | null (default: title A-Z)
     */
    @Transactional(readOnly = true)
    public List<BookDTO> getBooks(String search, String category, String sort) {
        // Normalize empty strings to null so the JPQL handles them cleanly
        String s   = (search   != null && !search.isBlank())   ? search.trim()   : null;
        String cat = (category != null && !category.isBlank()) ? category.trim() : null;

        List<Book> books = bookRepo.searchAndFilter(s, cat);

        // Apply sorting in memory (small dataset, fine for this scale)
        if ("price_asc".equalsIgnoreCase(sort)) {
            books = books.stream().sorted(Comparator.comparingDouble(Book::getPrice)).collect(Collectors.toList());
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            books = books.stream().sorted(Comparator.comparingDouble(Book::getPrice).reversed()).collect(Collectors.toList());
        }
        // default sort is by title (already applied in the JPQL ORDER BY)

        return books.stream().map(BookDTO::from).collect(Collectors.toList());
    }

    /**
     * Get a single book by id. Throws BookNotFoundException (→ 404) if missing.
     */
    @Transactional(readOnly = true)
    public BookDTO getBookById(Long id) {
        Book book = bookRepo.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
        return BookDTO.from(book);
    }

    /** Return the list of distinct categories. */
    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return bookRepo.findAllCategories();
    }

    // ─── Write operations ─────────────────────────────────────────────────────

    /** Create a new book and return the saved DTO. */
    public BookDTO createBook(Book book) {
        Book saved = bookRepo.save(book);
        return BookDTO.from(saved);
    }

    /**
     * Update an existing book. Throws 404 if not found.
     * We update each field individually instead of replacing the whole entity.
     */
    public BookDTO updateBook(Long id, Book updated) {
        Book existing = bookRepo.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));

        existing.setTitle(updated.getTitle());
        existing.setAuthor(updated.getAuthor());
        existing.setCategory(updated.getCategory());
        existing.setPrice(updated.getPrice());
        existing.setStock(updated.getStock());

        return BookDTO.from(bookRepo.save(existing));
    }

    /** Delete a book. Throws 404 if not found. */
    public void deleteBook(Long id) {
        if (!bookRepo.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepo.deleteById(id);
    }
}
