import { useCallback, useEffect, useState } from 'react';
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { getStats, toStatsDisplayMessage, type DailyCount } from '../api/stats';

interface Props {
  shortKey: string;
  /** trueの場合、ボタン操作を待たずマウント時に自動で統計を取得する（デフォルトは従来通りボタンで手動取得） */
  autoLoad?: boolean;
}

function UrlStatsChart({ shortKey, autoLoad = false }: Props) {
  const [daily, setDaily] = useState<DailyCount[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const loadStats = useCallback(async () => {
    setError(null);
    setIsLoading(true);
    try {
      const response = await getStats(shortKey);
      setDaily(response.daily);
    } catch (err) {
      setError(toStatsDisplayMessage(err));
    } finally {
      setIsLoading(false);
    }
  }, [shortKey]);

  useEffect(() => {
    if (autoLoad) {
      void loadStats();
    }
  }, [autoLoad, loadStats]);

  return (
    <div className="stats">
      {!autoLoad && daily === null && (
        <button type="button" onClick={loadStats} disabled={isLoading}>
          {isLoading ? '読み込み中...' : '統計を見る'}
        </button>
      )}

      {autoLoad && isLoading && <p>読み込み中...</p>}

      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}

      {daily !== null && daily.length === 0 && <p>まだアクセスがありません</p>}

      {daily !== null && daily.length > 0 && (
        <ResponsiveContainer width="100%" height={240}>
          <BarChart data={daily}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="date" />
            <YAxis allowDecimals={false} />
            <Tooltip />
            <Bar dataKey="count" fill="#aa3bff" name="クリック数" />
          </BarChart>
        </ResponsiveContainer>
      )}
    </div>
  );
}

export default UrlStatsChart;
