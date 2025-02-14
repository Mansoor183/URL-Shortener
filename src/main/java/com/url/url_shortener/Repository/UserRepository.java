package com.url.url_shortener.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.url.url_shortener.Entity.User;


public interface  UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findById(String id);
}
