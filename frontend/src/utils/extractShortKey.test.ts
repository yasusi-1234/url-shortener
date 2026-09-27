import { describe, expect, it } from 'vitest';
import { extractShortKey } from './extractShortKey';

describe('extractShortKey', () => {
  it('Given: 短縮URL全体が入力される（例: http://localhost:8080/Wtairw） / When: 短縮キーを抽出する / Then: 末尾のキー部分だけが取り出される', () => {
    expect(extractShortKey('http://localhost:8080/Wtairw')).toBe('Wtairw');
  });

  it('Given: 短縮キーだけが入力される（例: Wtairw） / When: 短縮キーを抽出する / Then: そのままの値が使われる', () => {
    expect(extractShortKey('Wtairw')).toBe('Wtairw');
  });

  it('Given: 末尾にスラッシュが付いた短縮URLが入力される（例: http://localhost:8080/Wtairw/） / When: 短縮キーを抽出する / Then: 末尾のキー部分だけが取り出される（空文字にならない）', () => {
    expect(extractShortKey('http://localhost:8080/Wtairw/')).toBe('Wtairw');
  });

  it('Given: 前後に空白を含む入力 / When: 短縮キーを抽出する / Then: 空白が取り除かれた値になる', () => {
    expect(extractShortKey('  Wtairw  ')).toBe('Wtairw');
  });

  it('Given: https付きの短縮URLが入力される / When: 短縮キーを抽出する / Then: スキームに関わらず末尾のキー部分だけが取り出される', () => {
    expect(extractShortKey('https://example.com/abc123')).toBe('abc123');
  });
});
