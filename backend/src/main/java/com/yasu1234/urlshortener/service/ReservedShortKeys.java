package com.yasu1234.urlshortener.service;

import java.util.Set;

/**
 * 既存のパス・エンドポイントやブラウザが自動的にリクエストするパスと衝突しうる短縮キーの予約語（design.md第11章）。
 */
final class ReservedShortKeys {

    private static final Set<String> RESERVED = Set.of(
            "api", "admin", "login", "stats", "favicon.ico", "robots.txt"
    );

    private ReservedShortKeys() {
    }

    static boolean contains(String key) {
        return RESERVED.contains(key.toLowerCase());
    }
}
