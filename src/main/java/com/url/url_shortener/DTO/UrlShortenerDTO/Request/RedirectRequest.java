package com.url.url_shortener.DTO.UrlShortenerDTO.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RedirectRequest {
    private String url;
}
