package com.yasu1234.urlshortener.dto;

import java.util.List;

public record StatsResponse(String shortKey, List<DailyCountDto> daily) {
}
