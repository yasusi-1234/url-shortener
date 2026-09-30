export interface ShortenResponse {
  shortKey: string;
  shortUrl: string;
}

interface ErrorResponseBody {
  error: string;
}

export class ApiError extends Error {
  readonly status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

export async function shortenUrl(url: string, customKey?: string): Promise<ShortenResponse> {
  const response = await fetch(`${import.meta.env.VITE_API_BASE}/api/shorten`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ url, customKey }),
  });

  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as ErrorResponseBody | null;
    throw new ApiError(body?.error ?? 'unexpected error', response.status);
  }

  return (await response.json()) as ShortenResponse;
}

const ERROR_MESSAGES: Record<string, string> = {
  'invalid url format': 'URLの形式が正しくありません（http:// または https:// で始まる必要があります）',
  'failed to generate short key': '短縮キーの生成に失敗しました。時間をおいて再度お試しください',
};

export function toDisplayMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return ERROR_MESSAGES[error.message] ?? error.message;
  }
  return '通信エラーが発生しました。しばらくしてから再度お試しください';
}
