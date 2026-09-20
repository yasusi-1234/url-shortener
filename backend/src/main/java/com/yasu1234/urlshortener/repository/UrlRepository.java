package com.yasu1234.urlshortener.repository;

import com.yasu1234.urlshortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortKey(String shortKey);

    boolean existsByShortKey(String shortKey);
}
