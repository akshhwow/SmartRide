// ============================================================
// FILE 1: UserRepository.java
// ============================================================
// 📌 What is a Repository?
//    A Repository is an interface that gives you FREE database
//    operations without writing SQL!
//
//    By extending JpaRepository, you automatically get:
//    - save(user)        → INSERT or UPDATE
//    - findById(id)      → SELECT WHERE id = ?
//    - findAll()         → SELECT * FROM users
//    - delete(user)      → DELETE WHERE id = ?
//    - count()           → SELECT COUNT(*)
//
//    You can also write custom queries using method names!
//    Spring automatically understands: findByEmail → SELECT WHERE email = ?
// ============================================================

package com.smartride.repository;

import com.smartride.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository - Database operations for User table
 *
 * JpaRepository<User, Long> means:
 *   - User = the Entity/Table we're working with
 *   - Long = the data type of the Primary Key (id)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring auto-generates: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // SELECT * FROM users WHERE phone = ?
    Optional<User> findByPhone(String phone);

    // SELECT COUNT(*) FROM users WHERE email = ?
    boolean existsByEmail(String email);

    // SELECT COUNT(*) FROM users WHERE phone = ?
    boolean existsByPhone(String phone);
}
