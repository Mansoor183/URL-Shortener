package com.url.url_shortener.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.url.url_shortener.Entity.ShortUrl;
import com.url.url_shortener.Repository.ShortUrlRepository;

@Service
public class ShortUrlHitService {
    @Autowired
    ShortUrlRepository shortUrlRepository;

    @Cacheable(value = "shortUrls", key = "#request")
    public ShortUrl findShortUrl(String request) {
        return shortUrlRepository.findByShortenedUrl(request);
    }
}
