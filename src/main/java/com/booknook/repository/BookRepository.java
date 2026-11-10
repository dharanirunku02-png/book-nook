package com.booknook.repository;

import com.booknook.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * BookRepository — Spring Data JPA takes care of all the SQL automatically.
 * We just declare method signatures and Spring writes the queries for us.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Find books that match a free-text search AND an optional category filter.
     * JPQL query:
     *   - Searches in both title and author (case-insensitive using LOWER)
     *   - If category is null or empty, it is ignored (the OR :cat IS NULL part)
     */
    @Query("""
        SELECT b FROM Book b
        WHERE (:search IS NULL OR
               LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:cat IS NULL OR LOWER(b.category) = LOWER(:cat))
        ORDER BY b.title
        """)
    List<Book> searchAndFilter(@Param("search") String search,
                               @Param("cat") String category);

    /**
     * Returns a distinct, sorted list of all categories currently in the database.
     * Used to build the category filter dropdown.
     */
    @Query("SELECT DISTINCT b.category FROM Book b ORDER BY b.category")
    List<String> findAllCategories();
}
