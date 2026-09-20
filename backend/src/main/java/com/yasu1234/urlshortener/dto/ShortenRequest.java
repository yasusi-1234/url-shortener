package com.yasu1234.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ShortenRequest(
        @NotBlank(message = "invalid url format")
        @Pattern(regexp = "^https?://.+", message = "invalid url format")
        String url
) {
}
