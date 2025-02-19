package com.url.url_shortener.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.url.url_shortener.DTO.UrlShortenerDTO.Request.PostUrlRequest;
import com.url.url_shortener.DTO.UrlShortenerDTO.Response.PostUrlResponse;
import com.url.url_shortener.Entity.ShortUrl;
import com.url.url_shortener.Entity.User;
import com.url.url_shortener.Exceptions.InvalidRequest;
import com.url.url_shortener.Repository.ShortUrlRepository;
import com.url.url_shortener.Repository.UserRepository;
import com.url.url_shortener.Utils.JwtUtil;

@Service
public class UrlService {
    @Autowired
    ShortUrlRepository shortUrlRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtil jwtUtil;

    public PostUrlResponse urlShortenerService(PostUrlRequest request, String authToken) {
        if(request.getLongUrl() == null) {
            throw new InvalidRequest("longUrl field cannot be null");
        }
        if(shortUrlRepository.existsByLongUrl(request.getLongUrl())) {
            throw new InvalidRequest("URL already exists");
        }
        validateUrl(request.getLongUrl());
        
        String shortenedUrl;
        if(request.getAlias() != null) {
            checkAliasUnique(request.getAlias());
            shortenedUrl = request.getAlias();
        }
        else {
            String shortUrlHash = generateUniqueKey();
            while (shortUrlRepository.existsByShortenedUrl(shortUrlHash)) {
                shortUrlHash = generateUniqueKey();
            }
            shortenedUrl = shortUrlHash;
        }


        Optional<User> user = userRepository.findById(jwtUtil.extractId(authToken));

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setLongUrl(request.getLongUrl());
        shortUrl.setShortenedUrl(shortenedUrl);
        shortUrl.setAlias(request.getAlias());
        shortUrl.setCreatedBy(user.get());
        shortUrlRepository.save(shortUrl);

        PostUrlResponse response = new PostUrlResponse();
        response.setLongUrl(request.getLongUrl());
        response.setShortUrl(shortenedUrl);
        return response;
    }

private void validateUrl(String url) {
    try {
        URI parsedUrl = new URI(url);
        String scheme = parsedUrl.getScheme();
        if(scheme == null) {
            throw new InvalidRequest("Invalid URL scheme. Only HTTP and HTTPS are supported.");
        }
        if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
            throw new InvalidRequest("Invalid URL scheme. Only HTTP and HTTPS are supported.");
        }
        if (parsedUrl.getHost() == null || parsedUrl.getHost().isEmpty()) {
            throw new InvalidRequest("Invalid URL: Host is missing.");
        }
    } catch (URISyntaxException e) {
        throw new InvalidRequest("Invalid URL syntax.");
    }
}

    private void checkAliasUnique(String alias) {
        if(shortUrlRepository.existsByAlias(alias)) {
            throw new InvalidRequest("Alias already exists");
        }
    }

    private String generateUniqueKey() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for(int i = 0 ; i < 7 ; i++) {
            stringBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        return stringBuilder.toString();
    }
}
