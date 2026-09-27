package com.yasu1234.urlshortener.repository;

import java.time.LocalDate;

public interface DailyClickCount {

    LocalDate getDay();

    long getCount();
}
