package com.yasu1234.urlshortener.dto;

import java.time.LocalDate;

public record DailyCountDto(LocalDate date, long count) {
}
