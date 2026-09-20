package com.yasu1234.urlshortener.service;

import com.yasu1234.urlshortener.entity.Url;
import com.yasu1234.urlshortener.exception.KeyGenerationException;
import com.yasu1234.urlshortener.exception.UrlNotFoundException;
import com.yasu1234.urlshortener.repository.UrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class UrlService {

    private static final String KEY_ALPHABET =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int KEY_LENGTH = 6;
    // 62^6 ≈ 568億通りのキー空間に対し10回連続衝突は実質起こり得ない水準。
    // それでも失敗する場合はリトライでは解決しない異常とみなし、無限ループを避けて例外にする。
    private static final int MAX_KEY_GENERATION_RETRY = 10;

    private final UrlRepository urlRepository;
    private final SecureRandom random = new SecureRandom();

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Transactional
    public Url shorten(String originalUrl) {
        String shortKey = generateUniqueShortKey();
        return urlRepository.save(new Url(shortKey, originalUrl));
    }

    @Transactional
    public Url resolveAndRecordClick(String shortKey) {
        Url url = urlRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new UrlNotFoundException(shortKey));
        url.setClickCount(url.getClickCount() + 1);
        return urlRepository.save(url);
    }

    private String generateUniqueShortKey() {
        for (int attempt = 0; attempt < MAX_KEY_GENERATION_RETRY; attempt++) {
            String candidate = randomKey();
            if (!urlRepository.existsByShortKey(candidate)) {
                return candidate;
            }
        }
        throw new KeyGenerationException(
                "failed to generate a unique short key after " + MAX_KEY_GENERATION_RETRY + " attempts");
    }

    private String randomKey() {
        StringBuilder sb = new StringBuilder(KEY_LENGTH);
        for (int i = 0; i < KEY_LENGTH; i++) {
            sb.append(KEY_ALPHABET.charAt(random.nextInt(KEY_ALPHABET.length())));
        }
        return sb.toString();
    }
}
