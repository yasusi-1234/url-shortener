package com.yasu1234.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "clicks")
public class Click {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url_id", nullable = false)
    private Long urlId;

    // INSERT時にJavaから値を渡さず、DBのDEFAULT now()に委ねる（Urlエンティティのcreated_atと同じ方針）
    @Column(name = "clicked_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime clickedAt;

    @Column(name = "device_type", nullable = false, length = 32)
    private String deviceType;

    @Column(name = "browser", nullable = false, length = 32)
    private String browser;

    protected Click() {
        // JPA用
    }

    public Click(Long urlId, String deviceType, String browser) {
        this.urlId = urlId;
        this.deviceType = deviceType;
        this.browser = browser;
    }

    public Long getId() {
        return id;
    }

    public Long getUrlId() {
        return urlId;
    }

    public LocalDateTime getClickedAt() {
        return clickedAt;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getBrowser() {
        return browser;
    }
}
