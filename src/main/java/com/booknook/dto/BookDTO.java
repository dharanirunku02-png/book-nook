package com.booknook.dto;

import com.booknook.entity.Book;

/**
 * BookDTO — the data we send to the frontend for each book.
 * We use a DTO (Data Transfer Object) instead of the entity directly so that
 * we control exactly what fields are exposed through the REST API.
 */
public class BookDTO {

    private Long   id;
    private String title;
    private String author;
    private String category;
    private Double price;
    private Integer stock;

    // ─── Static factory: create a DTO from an entity ─────────────────────────

    /**
     * Converts a Book entity into a BookDTO.
     * Called in the service layer before sending data to the controller.
     */
    public static BookDTO from(Book book) {
        BookDTO dto = new BookDTO();
        dto.id       = book.getId();
        dto.title    = book.getTitle();
        dto.author   = book.getAuthor();
        dto.category = book.getCategory();
        dto.price    = book.getPrice();
        dto.stock    = book.getStock();
        return dto;
    }

    // ─── Getters (Jackson needs these to serialize the object to JSON) ────────

    public Long    getId()       { return id; }
    public String  getTitle()    { return title; }
    public String  getAuthor()   { return author; }
    public String  getCategory() { return category; }
    public Double  getPrice()    { return price; }
    public Integer getStock()    { return stock; }
}
