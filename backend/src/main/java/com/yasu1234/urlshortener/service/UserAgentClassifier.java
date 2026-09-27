package com.yasu1234.urlshortener.service;

/**
 * リクエストのUser-Agentヘッダーから device_type / browser を判定する。
 * 外部ライブラリを使わず、想定される代表的なUAパターンのみを判定する簡易実装（design.md第10章）。
 */
final class UserAgentClassifier {

    static final String UNKNOWN = "unknown";

    private UserAgentClassifier() {
    }

    static String detectDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return UNKNOWN;
        }
        return userAgent.contains("Mobi") ? "mobile" : "pc";
    }

    static String detectBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return UNKNOWN;
        }
        // Edge/ChromeのUAは互いに他ブラウザ名を含むため、判定順序が重要
        // （例: EdgeのUAは"Chrome"も含み、ChromeのUAは"Safari"も含む）
        if (userAgent.contains("Edg")) {
            return "Edge";
        }
        if (userAgent.contains("Chrome")) {
            return "Chrome";
        }
        if (userAgent.contains("Firefox")) {
            return "Firefox";
        }
        if (userAgent.contains("Safari")) {
            return "Safari";
        }
        return "Other";
    }
}
