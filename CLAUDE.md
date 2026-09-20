# CLAUDE.md（プロジェクト全体・共通ルール）

## このプロジェクトについて
URL短縮サービス（bit.ly のミニ版）。
学習目的：「AIに手を動かさせ、要所を人間が設計・レビューする」開発ワークフローを身につける。

**要求仕様は `url-shortener-design.md` を参照すること。** 実装前に必ず読むこと。

## 構成（モノレポ）
```
url-shortener/
├── backend/   … Spring Boot（Java）。API専用。詳細は backend/CLAUDE.md
├── frontend/  … React + TypeScript。画面。詳細は frontend/CLAUDE.md
└── url-shortener-design.md … 要求仕様書
```
- フロントとバックは分離（SPA + REST API）。バックは画面を持たず、JSONを返すことに専念する。
- ビルドツールは別々：backend=Gradle、frontend=npm/Vite。**混ぜない。**
- Git はこの親フォルダで1つ（モノレポ）。

## 進め方のルール
- **まず backend を作って動作確認 → その後 frontend**。同時に作らない。
- 大きな機能を一度に作らず、小さく作って動かし、レビューしてから次に進む。
- 生成したコードは要求仕様書と照らして説明できる状態にする（なぜこうしたかをコメント等で残す）。
- 今回のスコープはレベル1（要求仕様書の第8章「スコープ外」は実装しない）。

## Git
- `.gitignore` は Java/Gradle と React/Vite の両方に対応させる。
- **node_modules/ は絶対にコミットしない。** build/、.gradle/、dist/ も除外する。
