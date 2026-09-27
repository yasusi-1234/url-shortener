/**
 * 「短縮キーから統計を見る」入力欄の値から短縮キーを取り出す。
 * 利用者は短縮URL全体（例: http://localhost:8080/Wtairw）を貼り付けることが多いため、
 * URLとして解釈できる場合はパスの末尾セグメントを短縮キーとして扱う。
 * URLとして解釈できない場合は、入力値自体を短縮キーとみなす。
 */
export function extractShortKey(input: string): string {
  const trimmed = input.trim();

  try {
    const url = new URL(trimmed);
    const segments = url.pathname.split('/').filter(Boolean);
    return segments.length > 0 ? segments[segments.length - 1] : trimmed;
  } catch {
    return trimmed;
  }
}
