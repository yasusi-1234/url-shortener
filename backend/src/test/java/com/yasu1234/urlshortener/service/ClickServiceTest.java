package com.yasu1234.urlshortener.service;

import com.yasu1234.urlshortener.entity.Click;
import com.yasu1234.urlshortener.entity.Url;
import com.yasu1234.urlshortener.exception.UrlNotFoundException;
import com.yasu1234.urlshortener.repository.ClickRepository;
import com.yasu1234.urlshortener.repository.DailyClickCount;
import com.yasu1234.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClickServiceTest {

    @Mock
    private ClickRepository clickRepository;

    @Mock
    private UrlRepository urlRepository;

    private ClickService clickService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        clickService = new ClickService(clickRepository, urlRepository);
    }

    @Test
    @DisplayName("Given: モバイル端末のChromeのUser-Agent / When: クリックを記録する / Then: device_typeがmobile、browserがChromeで保存される")
    void recordClick_savesMobileChrome() {
        String mobileChromeUa =
                "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Mobi Chrome/119.0 Mobile Safari/537.36";

        clickService.recordClick(1L, mobileChromeUa);

        ArgumentCaptor<Click> captor = ArgumentCaptor.forClass(Click.class);
        verify(clickRepository).save(captor.capture());
        assertThat(captor.getValue().getUrlId()).isEqualTo(1L);
        assertThat(captor.getValue().getDeviceType()).isEqualTo("mobile");
        assertThat(captor.getValue().getBrowser()).isEqualTo("Chrome");
    }

    @Test
    @DisplayName("Given: PC(Windows)のFirefoxのUser-Agent / When: クリックを記録する / Then: device_typeがpc、browserがFirefoxで保存される")
    void recordClick_savesPcFirefox() {
        String pcFirefoxUa = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0";

        clickService.recordClick(2L, pcFirefoxUa);

        ArgumentCaptor<Click> captor = ArgumentCaptor.forClass(Click.class);
        verify(clickRepository).save(captor.capture());
        assertThat(captor.getValue().getDeviceType()).isEqualTo("pc");
        assertThat(captor.getValue().getBrowser()).isEqualTo("Firefox");
    }

    @Test
    @DisplayName("Given: PC(Mac)のSafariのUser-Agent（Chromeを含まない） / When: クリックを記録する / Then: browserがSafariで保存される")
    void recordClick_savesSafari() {
        String safariUa =
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 Version/17.0 Safari/605.1.15";

        clickService.recordClick(3L, safariUa);

        ArgumentCaptor<Click> captor = ArgumentCaptor.forClass(Click.class);
        verify(clickRepository).save(captor.capture());
        assertThat(captor.getValue().getDeviceType()).isEqualTo("pc");
        assertThat(captor.getValue().getBrowser()).isEqualTo("Safari");
    }

    @Test
    @DisplayName("Given: EdgeのUser-Agent（Chromeも含む） / When: クリックを記録する / Then: ChromeではなくEdgeと判定される")
    void recordClick_prefersEdgeOverChrome() {
        String edgeUa =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/119.0 Safari/537.36 Edg/119.0";

        clickService.recordClick(4L, edgeUa);

        ArgumentCaptor<Click> captor = ArgumentCaptor.forClass(Click.class);
        verify(clickRepository).save(captor.capture());
        assertThat(captor.getValue().getBrowser()).isEqualTo("Edge");
    }

    @Test
    @DisplayName("Given: User-Agentが送られてこない（null） / When: クリックを記録する / Then: device_type/browserともにunknownで保存される")
    void recordClick_fallsBackToUnknown_whenUserAgentMissing() {
        clickService.recordClick(5L, null);

        ArgumentCaptor<Click> captor = ArgumentCaptor.forClass(Click.class);
        verify(clickRepository).save(captor.capture());
        assertThat(captor.getValue().getDeviceType()).isEqualTo("unknown");
        assertThat(captor.getValue().getBrowser()).isEqualTo("unknown");
    }

    @Test
    @DisplayName("Given: 存在するshort_keyのURL / When: 日別統計を取得する / Then: そのURLのurl_idで日別集計を取得できる")
    void getDailyStats_returnsDailyCounts_whenShortKeyExists() {
        Url existing = new Url("abc123", "https://example.com");
        when(urlRepository.findByShortKey("abc123")).thenReturn(Optional.of(existing));
        DailyClickCount day1 = dailyClickCount(LocalDate.of(2026, 9, 20), 5);
        when(clickRepository.countDailyClicksByUrlId(existing.getId())).thenReturn(List.of(day1));

        List<DailyClickCount> result = clickService.getDailyStats("abc123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDay()).isEqualTo(LocalDate.of(2026, 9, 20));
        assertThat(result.get(0).getCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("Given: 存在しないshort_key / When: 日別統計を取得する / Then: UrlNotFoundExceptionを投げる")
    void getDailyStats_throwsUrlNotFoundException_whenShortKeyDoesNotExist() {
        when(urlRepository.findByShortKey("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clickService.getDailyStats("missing"))
                .isInstanceOf(UrlNotFoundException.class);
    }

    private static DailyClickCount dailyClickCount(LocalDate day, long count) {
        return new DailyClickCount() {
            @Override
            public LocalDate getDay() {
                return day;
            }

            @Override
            public long getCount() {
                return count;
            }
        };
    }
}
