package com.booknook.repository;

import com.booknook.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * UserRepository — standard CRUD for User.
 * JpaRepository already gives us: findById, findAll, save, deleteById, etc.
 * No custom queries needed here.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // All needed methods come from JpaRepository — nothing extra required
}
