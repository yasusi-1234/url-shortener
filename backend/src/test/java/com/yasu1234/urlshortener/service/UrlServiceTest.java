package com.yasu1234.urlshortener.service;

import com.yasu1234.urlshortener.entity.Url;
import com.yasu1234.urlshortener.exception.KeyGenerationException;
import com.yasu1234.urlshortener.exception.UrlNotFoundException;
import com.yasu1234.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    private UrlService urlService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        urlService = new UrlService(urlRepository);
    }

    @Test
    void shorten_generatesKeyOnFirstTry_whenNoCollision() {
        when(urlRepository.existsByShortKey(anyString())).thenReturn(false);
        when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Url result = urlService.shorten("https://example.com");

        assertThat(result.getShortKey()).hasSize(6);
        assertThat(result.getOriginalUrl()).isEqualTo("https://example.com");
        verify(urlRepository, times(1)).existsByShortKey(anyString());
    }

    @Test
    void shorten_retriesOnce_whenFirstKeyCollides() {
        when(urlRepository.existsByShortKey(anyString())).thenReturn(true, false);
        when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Url result = urlService.shorten("https://example.com");

        assertThat(result.getShortKey()).hasSize(6);
        verify(urlRepository, times(2)).existsByShortKey(anyString());
    }

    @Test
    void shorten_throwsKeyGenerationException_whenAllTenAttemptsCollide() {
        when(urlRepository.existsByShortKey(anyString())).thenReturn(true);

        assertThatThrownBy(() -> urlService.shorten("https://example.com"))
                .isInstanceOf(KeyGenerationException.class);

        verify(urlRepository, times(10)).existsByShortKey(anyString());
        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    void resolveAndRecordClick_incrementsClickCount_whenKeyExists() {
        Url existing = new Url("abc123", "https://example.com");
        when(urlRepository.findByShortKey("abc123")).thenReturn(Optional.of(existing));
        when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Url result = urlService.resolveAndRecordClick("abc123");

        assertThat(result.getClickCount()).isEqualTo(1L);
        ArgumentCaptor<Url> captor = ArgumentCaptor.forClass(Url.class);
        verify(urlRepository).save(captor.capture());
        assertThat(captor.getValue().getClickCount()).isEqualTo(1L);
    }

    @Test
    void resolveAndRecordClick_throwsUrlNotFoundException_whenKeyDoesNotExist() {
        when(urlRepository.findByShortKey("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.resolveAndRecordClick("missing"))
                .isInstanceOf(UrlNotFoundException.class);
    }
}
