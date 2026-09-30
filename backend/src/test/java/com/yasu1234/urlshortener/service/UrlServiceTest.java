package com.yasu1234.urlshortener.service;

import com.yasu1234.urlshortener.entity.Url;
import com.yasu1234.urlshortener.exception.CustomKeyAlreadyInUseException;
import com.yasu1234.urlshortener.exception.KeyGenerationException;
import com.yasu1234.urlshortener.exception.ReservedShortKeyException;
import com.yasu1234.urlshortener.exception.UrlNotFoundException;
import com.yasu1234.urlshortener.repository.UrlRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
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

        Url result = urlService.shorten("https://example.com", null);

        assertThat(result.getShortKey()).hasSize(6);
        assertThat(result.getOriginalUrl()).isEqualTo("https://example.com");
        verify(urlRepository, times(1)).existsByShortKey(anyString());
    }

    @Test
    void shorten_retriesOnce_whenFirstKeyCollides() {
        when(urlRepository.existsByShortKey(anyString())).thenReturn(true, false);
        when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Url result = urlService.shorten("https://example.com", null);

        assertThat(result.getShortKey()).hasSize(6);
        verify(urlRepository, times(2)).existsByShortKey(anyString());
    }

    @Test
    void shorten_throwsKeyGenerationException_whenAllTenAttemptsCollide() {
        when(urlRepository.existsByShortKey(anyString())).thenReturn(true);

        assertThatThrownBy(() -> urlService.shorten("https://example.com", null))
                .isInstanceOf(KeyGenerationException.class);

        verify(urlRepository, times(10)).existsByShortKey(anyString());
        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    @DisplayName("Given: 予約語でも重複でもないcustomKeyが指定されている / When: URLを短縮登録する / Then: そのキーがそのまま使われる（再生成は行われない）")
    void shorten_usesCustomKeyAsIs_whenCustomKeyIsAvailable() {
        when(urlRepository.existsByShortKey("my-campaign")).thenReturn(false);
        when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Url result = urlService.shorten("https://example.com", "my-campaign");

        assertThat(result.getShortKey()).isEqualTo("my-campaign");
        verify(urlRepository, times(1)).existsByShortKey("my-campaign");
    }

    @Test
    @DisplayName("Given: customKeyが予約語と一致する（例: api） / When: URLを短縮登録する / Then: ReservedShortKeyExceptionを投げ、保存は行われない")
    void shorten_throwsReservedShortKeyException_whenCustomKeyIsReserved() {
        assertThatThrownBy(() -> urlService.shorten("https://example.com", "api"))
                .isInstanceOf(ReservedShortKeyException.class);

        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    @DisplayName("Given: customKeyが予約語の大文字表記（例: API） / When: URLを短縮登録する / Then: 大文字小文字を区別せず予約語として弾かれる")
    void shorten_throwsReservedShortKeyException_whenCustomKeyIsReservedIgnoringCase() {
        assertThatThrownBy(() -> urlService.shorten("https://example.com", "API"))
                .isInstanceOf(ReservedShortKeyException.class);

        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    @DisplayName("Given: customKeyが既に使われている / When: URLを短縮登録する / Then: CustomKeyAlreadyInUseExceptionを投げ、再生成は行われない")
    void shorten_throwsCustomKeyAlreadyInUseException_whenCustomKeyIsTaken() {
        when(urlRepository.existsByShortKey("taken")).thenReturn(true);

        assertThatThrownBy(() -> urlService.shorten("https://example.com", "taken"))
                .isInstanceOf(CustomKeyAlreadyInUseException.class);

        verify(urlRepository, times(1)).existsByShortKey("taken");
        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    @DisplayName("Given: 事前チェックは通過したがshort_keyのUNIQUE制約違反で保存に失敗する（競合） / When: URLを短縮登録する / Then: CustomKeyAlreadyInUseExceptionに変換される")
    void shorten_convertsShortKeyUniqueViolation_toCustomKeyAlreadyInUseException() {
        when(urlRepository.existsByShortKey("racey")).thenReturn(false);
        when(urlRepository.save(any(Url.class))).thenThrow(shortKeyUniqueViolation());

        assertThatThrownBy(() -> urlService.shorten("https://example.com", "racey"))
                .isInstanceOf(CustomKeyAlreadyInUseException.class);
    }

    @Test
    @DisplayName("Given: short_key以外の制約違反で保存に失敗する（将来他の制約が増えた場合を想定） / When: URLを短縮登録する / Then: キー重複とはみなさず、元の例外がそのまま伝播する")
    void shorten_propagatesDataIntegrityViolation_whenNotShortKeyConstraint() {
        when(urlRepository.existsByShortKey("custom")).thenReturn(false);
        DataIntegrityViolationException otherViolation = new DataIntegrityViolationException(
                "wrapped", new ConstraintViolationException("other constraint", new SQLException("x"), "some_other_constraint"));
        when(urlRepository.save(any(Url.class))).thenThrow(otherViolation);

        assertThatThrownBy(() -> urlService.shorten("https://example.com", "custom"))
                .isSameAs(otherViolation)
                .isNotInstanceOf(CustomKeyAlreadyInUseException.class);
    }

    private static DataIntegrityViolationException shortKeyUniqueViolation() {
        return new DataIntegrityViolationException(
                "wrapped", new ConstraintViolationException("duplicate key", new SQLException("x"), "urls_short_key_key"));
    }

    @Test
    @DisplayName("Given: 保存時にDB一意制約違反以外の想定外の例外が発生する / When: URLを短縮登録する / Then: その例外はそのまま伝播する（握りつぶされない）")
    void shorten_propagatesUnexpectedException_whenNotDataIntegrityViolation() {
        when(urlRepository.existsByShortKey("custom")).thenReturn(false);
        when(urlRepository.save(any(Url.class))).thenThrow(new IllegalStateException("unexpected db error"));

        assertThatThrownBy(() -> urlService.shorten("https://example.com", "custom"))
                .isInstanceOf(IllegalStateException.class)
                .isNotInstanceOf(CustomKeyAlreadyInUseException.class);
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
