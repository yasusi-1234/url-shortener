package com.yasu1234.urlshortener.controller;

import com.yasu1234.urlshortener.dto.ShortenRequest;
import com.yasu1234.urlshortener.dto.ShortenResponse;
import com.yasu1234.urlshortener.entity.Url;
import com.yasu1234.urlshortener.service.ClickService;
import com.yasu1234.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class UrlController {

    private static final Logger log = LoggerFactory.getLogger(UrlController.class);

    private final UrlService urlService;
    private final ClickService clickService;
    private final String baseUrl;

    public UrlController(UrlService urlService, ClickService clickService, @Value("${app.base-url}") String baseUrl) {
        this.urlService = urlService;
        this.clickService = clickService;
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
    public ResponseEntity<Void> redirect(
            @PathVariable String shortKey,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        Url url = urlService.resolveAndRecordClick(shortKey);
        // 計測（clicks記録）の失敗が本来の転送機能を巻き込まないよう、リダイレクト応答の外側で隔離する
        try {
            clickService.recordClick(url.getId(), userAgent);
        } catch (RuntimeException e) {
            log.warn("failed to record click for shortKey={}", shortKey, e);
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url.getOriginalUrl()))
                .build();
    }
}
