# CLAUDE.md（frontend / React・TypeScript）

## 役割
このディレクトリは **画面（フロントエンド）**。backend の REST API を叩いて動作する。
UI の要件は、親フォルダの `url-shortener-design.md` 第7章を参照。

## 技術
- React + TypeScript
- ビルドツール: Vite（開発サーバーは例として 5173 番）
- パッケージ管理: npm（Gradle は使わない）

## 作るもの（レベル1）
- URL 入力フォーム＋短縮ボタン1つ。
- 送信で `POST /api/shorten` を呼び、返ってきた shortUrl を画面に表示する。
- 短縮URLのコピー用ボタン（任意）。
- 入力が空・不正な場合の簡単なエラー表示。

## コーディング規約
- TypeScript の型をきちんと付ける（API のレスポンス型を interface / type で定義する）。any を避ける。
- API を叩く処理は、画面コンポーネントに直書きせず、専用の関数/モジュールに分ける（責務の分離）。
- backend の URL（http://localhost:8080 等）は環境変数（.env の VITE_API_BASE 等）で持ち、直書きしない。

## backend との連携
- backend は別ポートで動くので、CORS は backend 側で許可される前提。
- API 仕様（リクエスト/レスポンスの形）は url-shortener-design.md 第6章に合わせる。

## コマンド
- `npm install`   … 依存インストール
- `npm run dev`   … 開発サーバー起動
- `npm run build` … 本番ビルド
