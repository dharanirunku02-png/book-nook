package com.booknook.dto;

import java.util.List;

/**
 * WishlistResponseDTO — the full wishlist API response.
 *
 * Fields:
 *   books      — list of books in the wishlist
 *   count      — how many books are in the list right now
 *   limit      — maximum allowed (always 10)
 *   over_limit — true if count > 10 (user can remove but not add)
 */
public class WishlistResponseDTO {

    private List<BookDTO> books;
    private int           count;
    private int           limit     = 10;   // business constant
    private boolean       over_limit;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public WishlistResponseDTO(List<BookDTO> books) {
        this.books      = books;
        this.count      = books.size();
        this.over_limit = this.count > this.limit;
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public List<BookDTO> getBooks()      { return books; }
    public int           getCount()      { return count; }
    public int           getLimit()      { return limit; }

    /** JSON key will be "over_limit" because of the field name */
    public boolean       isOver_limit()  { return over_limit; }
}
