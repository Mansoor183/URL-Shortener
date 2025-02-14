package com.url.url_shortener.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.url.url_shortener.DTO.UrlShortenerDTO.Request.PostUrlRequest;
import com.url.url_shortener.DTO.UrlShortenerDTO.Response.PostUrlResponse;
import com.url.url_shortener.Entity.ShortUrl;
import com.url.url_shortener.Repository.ShortUrlRepository;
import com.url.url_shortener.Service.UrlService;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping
public class ShortUrlController {
    @Autowired
    UrlService urlService;
    @Autowired
    ShortUrlRepository shortUrlRepository;
    @PostMapping("/api/url/shortener")
    public ResponseEntity<PostUrlResponse> urlShortener(@RequestBody PostUrlRequest request, @RequestHeader("Authorization") String authToken) {
        System.out.println("authToken : " + authToken);
        PostUrlResponse response = urlService.urlShortenerService(request, authToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("{request}")
    public ResponseEntity<Void> urlRedirect(@PathVariable String request) {
        ShortUrl shortUrl = shortUrlRepository.findByShortenedUrl(request);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(shortUrl.getLongUrl())).build();
    }
}
