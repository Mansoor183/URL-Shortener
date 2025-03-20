package com.url.url_shortener.Repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.url.url_shortener.Entity.ShortUrl;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface ShortUrlRepository extends JpaRepository<ShortUrl, String> {

    @Cacheable(value = "shortUrlCache", key = "#shortenedUrl")
    @Query(value = "select long_url from short_url where shortened_url = :shortenedUrl", nativeQuery = true)
    Optional<String> findByShortenedUrl(@Param("shortenedUrl") String shortenedUrl);

    boolean existsByAlias(String alias);
    boolean existsByShortenedUrl(String shortenedUrl);
    boolean existsByLongUrl(String longUrl);
}
