package com.booknook.entity;

import jakarta.persistence.*;

/**
 * User entity — represents someone who can browse books and manage a wishlist.
 * Kept simple: just an id and a name. No passwords / auth in scope.
 */
@Entity
@Table(name = "app_user")   // "user" is a reserved word in SQL, so we use "app_user"
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // ─── Constructors ────────────────────────────────────────────────────────

    public User() {}

    public User(String name) {
        this.name = name;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public Long getId()            { return id; }
    public void setId(Long id)     { this.id = id; }

    public String getName()        { return name; }
    public void setName(String n)  { this.name = n; }
}
