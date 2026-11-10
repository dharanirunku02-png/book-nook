package com.booknook.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Book entity — represents a single book in the catalog.
 * JPA maps this Java class to a "book" table in the H2 database.
 */
@Entity
@Table(name = "book")
public class Book {

    /** Auto-generated primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Book title — must not be blank (validated on create/update) */
    @NotBlank(message = "Title must not be blank")
    @Column(nullable = false)
    private String title;

    /** Author name — must not be blank */
    @NotBlank(message = "Author must not be blank")
    @Column(nullable = false)
    private String author;

    /** Category (e.g. Fiction, Science, etc.) */
    @Column(nullable = false)
    private String category;

    /** Price in Indian Rupees — must be greater than 0 */
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Column(nullable = false)
    private Double price;

    /** Stock quantity — cannot be negative */
    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private Integer stock;

    // ─── Constructors ────────────────────────────────────────────────────────

    public Book() {}

    public Book(String title, String author, String category, Double price, Integer stock) {
        this.title    = title;
        this.author   = author;
        this.category = category;
        this.price    = price;
        this.stock    = stock;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public Long getId()                   { return id; }
    public void setId(Long id)            { this.id = id; }

    public String getTitle()              { return title; }
    public void setTitle(String title)    { this.title = title; }

    public String getAuthor()             { return author; }
    public void setAuthor(String author)  { this.author = author; }

    public String getCategory()           { return category; }
    public void setCategory(String c)     { this.category = c; }

    public Double getPrice()              { return price; }
    public void setPrice(Double price)    { this.price = price; }

    public Integer getStock()             { return stock; }
    public void setStock(Integer stock)   { this.stock = stock; }
}
