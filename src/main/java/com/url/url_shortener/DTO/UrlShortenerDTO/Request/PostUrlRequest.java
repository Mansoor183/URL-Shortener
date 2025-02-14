package com.url.url_shortener.DTO.UrlShortenerDTO.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostUrlRequest {
    private String alias;
    private String longUrl;
    private String domainId;
}
