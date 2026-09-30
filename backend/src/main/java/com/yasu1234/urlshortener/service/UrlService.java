package com.yasu1234.urlshortener.service;

import com.yasu1234.urlshortener.entity.Url;
import com.yasu1234.urlshortener.exception.CustomKeyAlreadyInUseException;
import com.yasu1234.urlshortener.exception.KeyGenerationException;
import com.yasu1234.urlshortener.exception.ReservedShortKeyException;
import com.yasu1234.urlshortener.exception.UrlNotFoundException;
import com.yasu1234.urlshortener.repository.UrlRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
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

    // V1マイグレーションでshort_keyにUNIQUE制約を付けた際、PostgreSQLが自動生成した制約名
    // （<テーブル名>_<列名>_key という命名規則）。この名前が変わった場合はここも合わせて更新する必要がある。
    private static final String SHORT_KEY_UNIQUE_CONSTRAINT = "urls_short_key_key";

    private final UrlRepository urlRepository;
    private final SecureRandom random = new SecureRandom();

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Transactional
    public Url shorten(String originalUrl, String customKey) {
        if (customKey == null) {
            String shortKey = generateUniqueShortKey();
            return urlRepository.save(new Url(shortKey, originalUrl));
        }
        return shortenWithCustomKey(originalUrl, customKey);
    }

    private Url shortenWithCustomKey(String originalUrl, String customKey) {
        if (ReservedShortKeys.contains(customKey)) {
            throw new ReservedShortKeyException(customKey);
        }
        if (urlRepository.existsByShortKey(customKey)) {
            throw new CustomKeyAlreadyInUseException(customKey);
        }
        try {
            return urlRepository.save(new Url(customKey, originalUrl));
        } catch (DataIntegrityViolationException e) {
            // 事前チェック(existsByShortKey)と実際のINSERTの間の競合に対する最終防衛。
            // short_keyのUNIQUE制約違反であることを制約名で確認したうえで変換する。
            // 他の制約違反（将来urlsテーブルに別の制約が増えた場合等）まで
            // 「キー重複」と誤って報告しないよう、該当しない場合はそのまま伝播させる。
            if (isShortKeyUniqueViolation(e)) {
                throw new CustomKeyAlreadyInUseException(customKey);
            }
            throw e;
        }
    }

    private boolean isShortKeyUniqueViolation(DataIntegrityViolationException e) {
        return e.getCause() instanceof ConstraintViolationException cve
                && SHORT_KEY_UNIQUE_CONSTRAINT.equals(cve.getConstraintName());
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
