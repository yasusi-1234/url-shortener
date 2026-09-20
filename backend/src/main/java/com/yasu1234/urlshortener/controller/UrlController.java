package com.yasu1234.urlshortener.controller;

import com.yasu1234.urlshortener.dto.ShortenRequest;
import com.yasu1234.urlshortener.dto.ShortenResponse;
import com.yasu1234.urlshortener.entity.Url;
import com.yasu1234.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class UrlController {

    private final UrlService urlService;
    private final String baseUrl;

    public UrlController(UrlService urlService, @Value("${app.base-url}") String baseUrl) {
        this.urlService = urlService;
        this.baseUrl = baseUrl;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        Url url = urlService.shorten(request.url());
        String shortUrl = baseUrl + "/" + url.getShortKey();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ShortenResponse(url.getShortKey(), shortUrl));
    }

    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirect(@PathVariable String shortKey) {
        Url url = urlService.resolveAndRecordClick(shortKey);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url.getOriginalUrl()))
                .build();
    }
}
