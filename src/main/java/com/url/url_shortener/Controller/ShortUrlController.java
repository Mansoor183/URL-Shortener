package com.url.url_shortener.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.url.url_shortener.DTO.UrlShortenerDTO.Request.PostUrlRequest;
import com.url.url_shortener.DTO.UrlShortenerDTO.Response.PostUrlResponse;
import com.url.url_shortener.Entity.ShortUrl;
import com.url.url_shortener.Exceptions.InvalidRequest;
import com.url.url_shortener.Exceptions.ResourceNotFound;
import com.url.url_shortener.Repository.ShortUrlRepository;
import com.url.url_shortener.Service.ShortUrlHitService;
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
    @Autowired
    ShortUrlHitService service;

    @PostMapping("/api/url/shortener")
    public ResponseEntity<PostUrlResponse> urlShortener(@RequestBody PostUrlRequest request, @RequestHeader("Authorization") String authToken) {
        if(request == null) {
            throw new InvalidRequest("Request body needed");
        }
        PostUrlResponse response = urlService.urlShortenerService(request, authToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("{request}")
    public ResponseEntity<Void> urlRedirect(@PathVariable String request) {
        ShortUrl shortUrl = service.findShortUrl(request);
        if(shortUrl == null) {
            throw new ResourceNotFound("URL not found : " + request);
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(shortUrl.getLongUrl())).build();
    }
}
