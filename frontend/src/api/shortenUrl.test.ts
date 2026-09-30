import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ApiError, shortenUrl, toDisplayMessage } from './shortenUrl';

describe('shortenUrl', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('Given: 短縮APIが200と結果を返す / When: shortenUrlを呼ぶ / Then: shortKeyとshortUrlを取得できる', async () => {
    const mockResponse = {
      ok: true,
      json: () => Promise.resolve({ shortKey: 'abc123', shortUrl: 'http://localhost:8080/abc123' }),
    } as Response;
    vi.mocked(fetch).mockResolvedValue(mockResponse);

    const result = await shortenUrl('https://example.com');

    expect(result.shortKey).toBe('abc123');
    expect(result.shortUrl).toBe('http://localhost:8080/abc123');
  });

  it('Given: カスタムキーを指定せずに短縮する / When: shortenUrlを呼ぶ / Then: リクエストボディにcustomKeyが含まれない', async () => {
    const mockResponse = {
      ok: true,
      json: () => Promise.resolve({ shortKey: 'abc123', shortUrl: 'http://localhost:8080/abc123' }),
    } as Response;
    vi.mocked(fetch).mockResolvedValue(mockResponse);

    await shortenUrl('https://example.com');

    const [, options] = vi.mocked(fetch).mock.calls[0];
    const body = JSON.parse((options as RequestInit).body as string);
    expect(body).toEqual({ url: 'https://example.com' });
  });

  it('Given: カスタムキーを指定して短縮する / When: shortenUrlを呼ぶ / Then: リクエストボディにcustomKeyが含まれる', async () => {
    const mockResponse = {
      ok: true,
      json: () => Promise.resolve({ shortKey: 'my-campaign', shortUrl: 'http://localhost:8080/my-campaign' }),
    } as Response;
    vi.mocked(fetch).mockResolvedValue(mockResponse);

    await shortenUrl('https://example.com', 'my-campaign');

    const [, options] = vi.mocked(fetch).mock.calls[0];
    const body = JSON.parse((options as RequestInit).body as string);
    expect(body).toEqual({ url: 'https://example.com', customKey: 'my-campaign' });
  });

  it('Given: 短縮APIがエラーを返す / When: shortenUrlを呼ぶ / Then: ApiErrorを投げる', async () => {
    const mockResponse = {
      ok: false,
      status: 409,
      json: () => Promise.resolve({ error: 'そのキーは既に使われています' }),
    } as Response;
    vi.mocked(fetch).mockResolvedValue(mockResponse);

    await expect(shortenUrl('https://example.com', 'taken')).rejects.toBeInstanceOf(ApiError);
  });
});

describe('toDisplayMessage', () => {
  it('Given: 既知のエラーメッセージ（invalid url format）のApiError / When: toDisplayMessageを呼ぶ / Then: 日本語のエラーメッセージに変換される', () => {
    const message = toDisplayMessage(new ApiError('invalid url format', 400));

    expect(message).toBe('URLの形式が正しくありません（http:// または https:// で始まる必要があります）');
  });

  it('Given: 変換テーブルに無いメッセージ（カスタムキー関連など既に日本語のメッセージ）のApiError / When: toDisplayMessageを呼ぶ / Then: そのままのメッセージが使われる', () => {
    const message = toDisplayMessage(new ApiError('そのキーは既に使われています', 409));

    expect(message).toBe('そのキーは既に使われています');
  });
});
