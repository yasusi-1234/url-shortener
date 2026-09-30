package com.yasu1234.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShortenRequest(
        @NotBlank(message = "invalid url format")
        @Pattern(regexp = "^https?://.+", message = "invalid url format")
        String url,

        @Pattern(regexp = "^[a-z0-9_-]+$", message = "小文字の英字・数字・ハイフン・アンダースコアのみ使えます")
        @Size(min = 1, max = 30, message = "1〜30文字で入力してください")
        String customKey
) {
}
