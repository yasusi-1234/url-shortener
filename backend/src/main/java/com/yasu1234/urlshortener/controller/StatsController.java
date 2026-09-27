package com.yasu1234.urlshortener.controller;

import com.yasu1234.urlshortener.dto.DailyCountDto;
import com.yasu1234.urlshortener.dto.StatsResponse;
import com.yasu1234.urlshortener.repository.DailyClickCount;
import com.yasu1234.urlshortener.service.ClickService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StatsController {

    private final ClickService clickService;

    public StatsController(ClickService clickService) {
        this.clickService = clickService;
    }

    @GetMapping("/api/urls/{shortKey}/stats")
    public ResponseEntity<StatsResponse> stats(@PathVariable String shortKey) {
        List<DailyClickCount> daily = clickService.getDailyStats(shortKey);
        List<DailyCountDto> dailyDtos = daily.stream()
                .map(d -> new DailyCountDto(d.getDay(), d.getCount()))
                .toList();
        return ResponseEntity.ok(new StatsResponse(shortKey, dailyDtos));
    }
}
