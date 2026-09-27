package com.yasu1234.urlshortener.service;

import com.yasu1234.urlshortener.entity.Click;
import com.yasu1234.urlshortener.entity.Url;
import com.yasu1234.urlshortener.exception.UrlNotFoundException;
import com.yasu1234.urlshortener.repository.ClickRepository;
import com.yasu1234.urlshortener.repository.DailyClickCount;
import com.yasu1234.urlshortener.repository.UrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClickService {

    private final ClickRepository clickRepository;
    private final UrlRepository urlRepository;

    public ClickService(ClickRepository clickRepository, UrlRepository urlRepository) {
        this.clickRepository = clickRepository;
        this.urlRepository = urlRepository;
    }

    @Transactional
    public void recordClick(Long urlId, String userAgent) {
        String deviceType = UserAgentClassifier.detectDeviceType(userAgent);
        String browser = UserAgentClassifier.detectBrowser(userAgent);
        clickRepository.save(new Click(urlId, deviceType, browser));
    }

    @Transactional(readOnly = true)
    public List<DailyClickCount> getDailyStats(String shortKey) {
        Url url = urlRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new UrlNotFoundException(shortKey));
        return clickRepository.countDailyClicksByUrlId(url.getId());
    }
}
