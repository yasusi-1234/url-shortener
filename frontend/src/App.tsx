import { useState, type FormEvent } from 'react';
import { shortenUrl, toDisplayMessage, type ShortenResponse } from './api/shortenUrl';
import UrlStatsChart from './components/UrlStatsChart';
import { extractShortKey } from './utils/extractShortKey';
import './App.css';

function App() {
  const [url, setUrl] = useState('');
  const [result, setResult] = useState<ShortenResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [copied, setCopied] = useState(false);

  const [lookupKey, setLookupKey] = useState('');
  const [activeLookupKey, setActiveLookupKey] = useState<string | null>(null);

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setResult(null);
    setCopied(false);

    const trimmed = url.trim();
    if (!trimmed) {
      setError('URLを入力してください');
      return;
    }

    setError(null);
    setIsSubmitting(true);
    try {
      const response = await shortenUrl(trimmed);
      setResult(response);
    } catch (err) {
      setError(toDisplayMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleCopy() {
    if (!result) {
      return;
    }
    await navigator.clipboard.writeText(result.shortUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  }

  function handleLookupSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const trimmed = lookupKey.trim();
    if (!trimmed) {
      return;
    }
    setActiveLookupKey(extractShortKey(trimmed));
  }

  return (
    <main id="app">
      <h1>URL短縮サービス</h1>

      <form onSubmit={handleSubmit}>
        <input
          type="text"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          placeholder="https://example.com/very/long/path"
          aria-label="短縮したいURL"
        />
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? '短縮中...' : '短縮する'}
        </button>
      </form>

      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}

      {result && (
        <div className="result">
          <a href={result.shortUrl} target="_blank" rel="noreferrer">
            {result.shortUrl}
          </a>
          <button type="button" onClick={handleCopy}>
            {copied ? 'コピーしました' : 'コピー'}
          </button>
        </div>
      )}

      {result && <UrlStatsChart key={result.shortKey} shortKey={result.shortKey} />}

      <hr />

      <section className="lookup">
        <h2>短縮キーから統計を見る</h2>
        <form onSubmit={handleLookupSubmit}>
          <input
            type="text"
            value={lookupKey}
            onChange={(e) => setLookupKey(e.target.value)}
            placeholder="短縮URL または キーを入力"
            aria-label="統計を見たい短縮URLまたは短縮キー"
            required
          />
          <button type="submit">表示する</button>
        </form>

        {activeLookupKey && <UrlStatsChart key={activeLookupKey} shortKey={activeLookupKey} autoLoad />}
      </section>
    </main>
  );
}

export default App;
