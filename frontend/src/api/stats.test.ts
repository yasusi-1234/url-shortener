import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ApiError } from './shortenUrl';
import { getStats, toStatsDisplayMessage } from './stats';

describe('getStats', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('Given: 統計APIが200と日別データを返す / When: getStatsを呼ぶ / Then: shortKeyとdaily配列を取得できる', async () => {
    const mockResponse = {
      ok: true,
      status: 200,
      json: () => Promise.resolve({ shortKey: 'abc123', daily: [{ date: '2026-09-20', count: 5 }] }),
    } as Response;
    vi.mocked(fetch).mockResolvedValue(mockResponse);

    const result = await getStats('abc123');

    expect(result.shortKey).toBe('abc123');
    expect(result.daily).toEqual([{ date: '2026-09-20', count: 5 }]);
  });

  it('Given: 統計APIが404を返す（該当short_keyなし） / When: getStatsを呼ぶ / Then: ApiErrorを投げる', async () => {
    const mockResponse = { ok: false, status: 404, json: () => Promise.resolve(null) } as Response;
    vi.mocked(fetch).mockResolvedValue(mockResponse);

    await expect(getStats('missing')).rejects.toBeInstanceOf(ApiError);
  });

  it('Given: 統計APIが500を返す / When: getStatsを呼ぶ / Then: 汎用メッセージのApiErrorを投げる', async () => {
    const mockResponse = { ok: false, status: 500, json: () => Promise.resolve(null) } as Response;
    vi.mocked(fetch).mockResolvedValue(mockResponse);

    await expect(getStats('abc123')).rejects.toMatchObject({ message: 'unexpected error' });
  });
});

describe('toStatsDisplayMessage', () => {
  it('Given: short key not foundのApiError / When: toStatsDisplayMessageを呼ぶ / Then: 日本語のエラーメッセージに変換される', () => {
    const message = toStatsDisplayMessage(new ApiError('short key not found', 404));

    expect(message).toBe('指定された短縮URLが見つかりませんでした');
  });

  it('Given: ApiErrorではない例外（ネットワークエラー等） / When: toStatsDisplayMessageを呼ぶ / Then: 汎用の通信エラーメッセージになる', () => {
    const message = toStatsDisplayMessage(new Error('network down'));

    expect(message).toBe('統計情報の取得に失敗しました。しばらくしてから再度お試しください');
  });
});
