package com.booknook.config;

import com.booknook.entity.Book;
import com.booknook.entity.User;
import com.booknook.entity.WishlistItem;
import com.booknook.repository.BookRepository;
import com.booknook.repository.UserRepository;
import com.booknook.repository.WishlistRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DataLoader — runs once when the app starts and fills the database with sample data.
 * CommandLineRunner is a Spring Boot hook that calls the run() method after startup.
 *
 * Data loaded:
 *   - 20 books across 6 categories with realistic rupee prices
 *   - 3 users:
 *       User 1 (Priya)  → empty wishlist
 *       User 2 (Arjun)  → 9 books (one below the limit)
 *       User 3 (Meera)  → 12 books (over the limit — tests the over_limit flag)
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final BookRepository     bookRepo;
    private final UserRepository     userRepo;
    private final WishlistRepository wishlistRepo;

    public DataLoader(BookRepository bookRepo, UserRepository userRepo, WishlistRepository wishlistRepo) {
        this.bookRepo     = bookRepo;
        this.userRepo     = userRepo;
        this.wishlistRepo = wishlistRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("📚 Loading sample data...");

        // ── 20 Books across 6 categories ──────────────────────────────────────

        List<Book> books = bookRepo.saveAll(List.of(

            // Fiction (5 books)
            new Book("The Midnight Library",          "Matt Haig",          "Fiction",   499.0,  45),
            new Book("The Alchemist",                 "Paulo Coelho",       "Fiction",   299.0,  80),
            new Book("Ikigai",                        "Héctor García",      "Fiction",   349.0,  60),
            new Book("Where the Crawdads Sing",       "Delia Owens",        "Fiction",   599.0,  30),
            new Book("The Kite Runner",               "Khaled Hosseini",    "Fiction",   399.0,   8),   // low stock

            // Science (3 books)
            new Book("A Brief History of Time",       "Stephen Hawking",    "Science",   549.0,  25),
            new Book("The Gene: An Intimate History", "Siddhartha Mukherjee","Science",  799.0,  15),
            new Book("Cosmos",                        "Carl Sagan",         "Science",   649.0,   3),   // very low stock

            // History (3 books)
            new Book("Sapiens",                       "Yuval Noah Harari",  "History",   699.0,  50),
            new Book("India After Gandhi",            "Ramachandra Guha",   "History",   899.0,  20),
            new Book("The Discovery of India",        "Jawaharlal Nehru",   "History",   449.0,  35),

            // Tech (4 books)
            new Book("Clean Code",                    "Robert C. Martin",   "Tech",      749.0,  40),
            new Book("The Pragmatic Programmer",      "David Thomas",       "Tech",      849.0,  22),
            new Book("Design Patterns",               "Gang of Four",       "Tech",      999.0,  18),
            new Book("You Don't Know JS",             "Kyle Simpson",       "Tech",      399.0,   0),   // out of stock

            // Kids (3 books)
            new Book("Harry Potter & the Sorcerer's Stone", "J.K. Rowling","Kids",      449.0,  70),
            new Book("The Little Prince",             "Antoine de Saint-Exupéry","Kids", 249.0,  55),
            new Book("Matilda",                       "Roald Dahl",         "Kids",      299.0,   5),   // low stock

            // Self-Help (2 books)
            new Book("Atomic Habits",                 "James Clear",        "Self-Help", 599.0,  90),
            new Book("The Power of Now",              "Eckhart Tolle",      "Self-Help", 449.0,  33)
        ));

        // ── 3 Users ───────────────────────────────────────────────────────────

        User priya  = userRepo.save(new User("Priya Sharma"));   // empty wishlist
        User arjun  = userRepo.save(new User("Arjun Mehta"));    // 9 books (near limit)
        User meera  = userRepo.save(new User("Meera Nair"));     // 12 books (over limit)

        // ── Arjun's wishlist: 9 books (safe — one slot remaining) ─────────────
        List<Book> arjunBooks = books.subList(0, 9);   // first 9 books
        for (Book b : arjunBooks) {
            wishlistRepo.save(new WishlistItem(arjun, b));
        }

        // ── Meera's wishlist: 12 books (over-limit — inserted directly) ───────
        // We bypass the service on purpose to test the over_limit flag.
        List<Book> meeraBooks = books.subList(0, 12);  // first 12 books
        for (Book b : meeraBooks) {
            wishlistRepo.save(new WishlistItem(meera, b));
        }

        System.out.println("✅ Sample data loaded: " + books.size() + " books, 3 users");
        System.out.println("   Priya  (id=" + priya.getId()  + ") → 0 wishlist books");
        System.out.println("   Arjun  (id=" + arjun.getId()  + ") → 9 wishlist books");
        System.out.println("   Meera  (id=" + meera.getId()  + ") → 12 wishlist books (over limit)");
    }
}
