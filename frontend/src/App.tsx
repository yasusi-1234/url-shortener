import { useState, type FormEvent } from 'react';
import { shortenUrl, toDisplayMessage, type ShortenResponse } from './api/shortenUrl';
import UrlStatsChart from './components/UrlStatsChart';
import { extractShortKey } from './utils/extractShortKey';
import './App.css';

function App() {
  const [url, setUrl] = useState('');
  const [customKey, setCustomKey] = useState('');
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
      const response = await shortenUrl(trimmed, customKey.trim() || undefined);
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

      <form onSubmit={handleSubmit} className="shorten-form">
        <div className="input-row">
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
        </div>

        <div className="custom-key-field">
          <input
            type="text"
            value={customKey}
            onChange={(e) => setCustomKey(e.target.value)}
            placeholder="my-campaign"
            aria-label="カスタム短縮キー（任意）"
          />
          <p className="help-text">使える文字：小文字の英字・数字・ハイフン・アンダースコア、1〜30文字</p>
        </div>
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
