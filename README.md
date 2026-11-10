# 📚 Book Nook — Cozy Magical Library

A full-stack online bookstore catalog and inventory management web app built with **Java 17 + Spring Boot 3 + Spring Data JPA + H2 (File Mode) + Vanilla HTML/CSS/JS**.

🌐 **Live Demo on GitHub Pages**: [https://dharanirunku02-png.github.io/book-nook/](https://dharanirunku02-png.github.io/book-nook/)  
📦 **GitHub Repository**: [https://github.com/dharanirunku02-png/book-nook](https://github.com/dharanirunku02-png/book-nook)

---

## 🚀 Setup & Run (Local Java Backend)

### Prerequisites
- Java 17+ (or Corretto / OpenJDK 17)
- Maven 3.8+ (or use the included `./mvnw` wrapper)

### One-command start

```bash
# Using standard Maven
mvn spring-boot:run

# Or using the included Maven Wrapper (Windows PowerShell)
.\mvnw.cmd spring-boot:run

# Or using the included Maven Wrapper (Linux / macOS)
./mvnw spring-boot:run
```

Then open **http://localhost:8080** in your browser.

The H2 console is at **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:file:./data/booknook-db`
- Username: `sa` / Password: *(blank)*

---

## 🗂 Project Structure

```
src/
├── main/
│   ├── java/com/booknook/
│   │   ├── BookNookApplication.java       ← Spring Boot entry point
│   │   ├── entity/
│   │   │   ├── Book.java                  ← Book table
│   │   │   ├── User.java                  ← User table (app_user)
│   │   │   └── WishlistItem.java          ← Wishlist join table
│   │   ├── repository/
│   │   │   ├── BookRepository.java        ← JPQL search + filter
│   │   │   ├── UserRepository.java
│   │   │   └── WishlistRepository.java    ← count, exists, delete
│   │   ├── dto/
│   │   │   ├── BookDTO.java
│   │   │   ├── UserDTO.java
│   │   │   └── WishlistResponseDTO.java   ← count + limit + over_limit
│   │   ├── service/
│   │   │   ├── BookService.java           ← catalog CRUD + search
│   │   │   └── WishlistService.java       ← all business rules
│   │   ├── controller/
│   │   │   ├── BookController.java        ← /api/books, /api/users, /api/categories
│   │   │   └── WishlistController.java    ← /api/wishlist/**
│   │   ├── exception/
│   │   │   ├── BookNotFoundException.java
│   │   │   ├── UserNotFoundException.java
│   │   │   ├── WishlistLimitException.java
│   │   │   ├── DuplicateWishlistException.java
│   │   │   └── GlobalExceptionHandler.java ← @RestControllerAdvice
│   │   └── config/
│   │       ├── CorsConfig.java            ← CORS setup
│   │       └── DataLoader.java            ← seed data on startup
│   └── resources/
│       ├── application.properties         ← H2 file mode + JPA settings
│       └── static/
│           ├── index.html                 ← SPA shell
│           ├── style.css                  ← Midnight blue magical theme
│           └── app.js                     ← All frontend logic
└── test/
    └── java/com/booknook/service/
        └── WishlistServiceTest.java       ← 5 JUnit 5 unit tests
```

---

## 📡 REST API

### Books
| Method | URL | Description |
|--------|-----|-------------|
| `GET`  | `/api/books` | List all books (params: `search`, `category`, `sort`) |
| `GET`  | `/api/books/{id}` | Get single book |
| `POST` | `/api/books` | Create a book (validated) |
| `PUT`  | `/api/books/{id}` | Update a book (validated) |
| `DELETE` | `/api/books/{id}` | Delete a book |
| `GET`  | `/api/categories` | List all distinct categories |

**Query params for GET /api/books:**
- `search=harry` — search title or author (case-insensitive)
- `category=Fiction` — filter by category
- `sort=price_asc` or `sort=price_desc`

### Users
| Method | URL | Description |
|--------|-----|-------------|
| `GET`  | `/api/users` | List all users |

### Wishlist
| Method | URL | Description |
|--------|-----|-------------|
| `GET`  | `/api/wishlist/{userId}` | Get wishlist (books + count + limit + over_limit) |
| `POST` | `/api/wishlist/{userId}/{bookId}` | Add book to wishlist |
| `DELETE` | `/api/wishlist/{userId}/{bookId}` | Remove book from wishlist |

---

## 🏛️ Business Rules

| Rule | Behaviour |
|------|-----------|
| **Max 10 books** | Adding when count ≥ 10 → HTTP 409 "Wishlist limit reached…" |
| **No duplicates** | Same book twice → HTTP 409 |
| **Over-limit flag** | count > 10 → `over_limit: true`; user can remove but not add |
| **404 not found** | Unknown user or book → HTTP 404 with message |

---

## 🧪 Run Tests

```bash
mvn test
```

Tests are in `WishlistServiceTest.java` and cover:
1. ✅ Add success (count < 10)
2. ❌ 11th book rejected (409)
3. ❌ Duplicate rejected (409)
4. 🚩 Over-limit flag (count = 12)
5. ✅ Remove allowed when over the limit

---

## 🎨 Frontend Features

- **Cozy magical library theme**: midnight blue bg, gold accents, cream cards, coral buttons
- **Book cards** with colored spine (like a real book), hover lift & tilt animation
- **Category icons** — each category has a unique color and emoji icon
- **Live search** without page reload (debounced 350ms)
- **Category filter** + **price sort** via API calls
- **User switcher** to test different wishlists
- **Wishlist counter** `7/10` with animated progress bar (turns red when full)
- **Friendly popup** when limit is hit
- **Yellow banner** when over_limit is true; add buttons disabled
- **Dark/Light mode** toggle (preference saved to localStorage)
- **Fully responsive** for phone, tablet, laptop

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Database | H2 (file mode) |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| Frontend | HTML5 + CSS3 + Vanilla JS |
| Fonts | Google Fonts (Playfair Display + Poppins) |
| Tests | JUnit 5 + Mockito |
