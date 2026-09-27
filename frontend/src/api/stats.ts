import { ApiError } from './shortenUrl';

export interface DailyCount {
  date: string;
  count: number;
}

export interface StatsResponse {
  shortKey: string;
  daily: DailyCount[];
}

export async function getStats(shortKey: string): Promise<StatsResponse> {
  const response = await fetch(`${import.meta.env.VITE_API_BASE}/api/urls/${shortKey}/stats`);

  if (!response.ok) {
    const message = response.status === 404 ? 'short key not found' : 'unexpected error';
    throw new ApiError(message, response.status);
  }

  return (await response.json()) as StatsResponse;
}

const ERROR_MESSAGES: Record<string, string> = {
  'short key not found': '指定された短縮URLが見つかりませんでした',
};

export function toStatsDisplayMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return ERROR_MESSAGES[error.message] ?? error.message;
  }
  return '統計情報の取得に失敗しました。しばらくしてから再度お試しください';
}
