package com.url.url_shortener.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.url.url_shortener.Entity.ShortUrl;



public interface ShortUrlRepository extends JpaRepository<ShortUrl, String> {
    boolean existsByAlias(String alias);
    boolean existsByShortenedUrl(String shortenedUrl);
    ShortUrl findByShortenedUrl(String shortenedUrl);
}
