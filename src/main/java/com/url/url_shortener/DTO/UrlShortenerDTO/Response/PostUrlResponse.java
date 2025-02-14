package com.url.url_shortener.DTO.UrlShortenerDTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostUrlResponse {
    private String shortUrl;
    private String longUrl;
}
