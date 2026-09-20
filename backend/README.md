# backend README

URL短縮サービスのバックエンド（API専用）。全体の要求仕様は [../url-shortener-design.md](../url-shortener-design.md)、コーディング規約は [CLAUDE.md](CLAUDE.md) を参照。このファイルは実装済みのコード構造を後から見返すための一覧。

## 1. アーキテクチャ（層ごとの役割）

```
[Controller]  リクエストを受けてServiceを呼ぶだけ。ロジックを書かない
     │
     ▼
[Service]     ビジネスロジック（キー生成・衝突チェック・登録・検索・click_count加算）
     │
     ▼
[Repository]  DBアクセス（Spring Data JPA、urlsテーブル）
     │
     ▼
[Entity]      urlsテーブルに対応するドメインオブジェクト

[DTO]         APIのリクエスト/レスポンス専用の型。Entityを直接APIに晒さない
[Exception]   業務例外の定義 ＋ @RestControllerAdvice による一元ハンドリング
```

- Controllerは薄く保ち、HTTPステータスやレスポンスの組み立てのみ行う。
- 例外（衝突リトライ上限到達、キー未検出など）はServiceが投げ、`GlobalExceptionHandler` がHTTPレスポンスに変換する。Controller自身はtry-catchを持たない。
- Entity（`Url`）はAPIレスポンスに直接使わず、必ずDTOに詰め替えて返す（内部IDを外部に晒さないため）。

## 2. クラス一覧

| クラス | 層 | 役割 |
|---|---|---|
| `UrlShortenerBackendApplication` | 起動 | `@SpringBootApplication` エントリポイント |
| `controller/UrlController` | Controller | `POST /api/shorten`、`GET /{shortKey}` の2エンドポイントを提供 |
| `service/UrlService` | Service | ランダムキー生成（衝突時リトライ、最大10回）、URL登録、short_keyでの検索とclick_count加算 |
| `entity/Url` | Entity | `urls` テーブルに対応。`id`/`shortKey`/`originalUrl`/`clickCount`/`createdAt` を保持。`createdAt` はDBの`DEFAULT now()`に委ねる（`insertable=false`） |
| `repository/UrlRepository` | Repository | `JpaRepository<Url, Long>`。`findByShortKey`、`existsByShortKey` を提供 |
| `dto/ShortenRequest` | DTO | 短縮APIのリクエストボディ。`@NotBlank` + `@Pattern` で `http(s)://` 形式を検証 |
| `dto/ShortenResponse` | DTO | 短縮APIのレスポンスボディ（`shortKey`, `shortUrl`） |
| `dto/ErrorResponse` | DTO | エラーレスポンス共通形式（`{"error": "..."}`） |
| `exception/KeyGenerationException` | Exception | キー生成が最大10回のリトライで全て衝突した場合に投げる |
| `exception/UrlNotFoundException` | Exception | 存在しないshort_keyで検索した場合に投げる |
| `exception/GlobalExceptionHandler` | Exception | `@RestControllerAdvice`。上記の例外とBean Validation違反をHTTPレスポンスに変換 |

## 3. APIエンドポイント一覧

| メソッド | パス | 役割 | 成功レスポンス | 失敗レスポンス |
|---|---|---|---|---|
| POST | `/api/shorten` | 長いURLを登録し、短縮キーを発行する | `201 Created`<br>`{"shortKey": "abc123", "shortUrl": "http://localhost:8080/abc123"}` | `400 Bad Request`（url形式不正）<br>`{"error": "invalid url format"}`<br><br>`500 Internal Server Error`（キー生成失敗）<br>`{"error": "failed to generate short key"}` |
| GET | `/{shortKey}` | short_keyから元URLを引いて302リダイレクト。あわせてclick_countを+1 | `302 Found`<br>`Location: <original_url>` | `404 Not Found`（該当short_keyなし、ボディなし） |

例外→HTTPステータスの対応（`GlobalExceptionHandler`）:

| 例外 | HTTPステータス |
|---|---|
| `MethodArgumentNotValidException`（Bean Validation違反） | 400 |
| `UrlNotFoundException` | 404 |
| `KeyGenerationException` | 500 |

## 4. 主要な設計判断

### なぜランダムキーか（連番Base62変換にしない）
連番だとキーの順番が見えてしまい `+1` で他人の短縮URLを列挙・覗き見できる。「URLを知っていること自体がアクセス権」になっている飛び先（限定公開リンク等）を守れないため、ランダムな6文字（a-zA-Z0-9の62文字から生成）を採用。詳細は設計書5章。

### なぜキー生成のリトライに上限（10回）を設けるか
62^6 ≈ 568億通りのキー空間に対し、レベル1の想定規模では衝突は極めて稀。10回連続衝突する確率は実質ゼロで、それでも起きるならキー空間の枯渇や生成ロジックの異常などリトライでは解決しない別の問題を疑うべき状況。上限を設けずリトライし続けると異常時に無限ループとなり可用性を損なうため、上限に達したら `KeyGenerationException` を投げて早期にエラー化する（500として返す）。

### なぜshort_keyにDBレベルのUNIQUE制約も付けるか
アプリ側の衝突チェック（`existsByShortKey`）をすり抜けても重複を防ぐ、DBレベルでの多層防御。

### なぜ内部ID（id）を外部に晒さないか
主キー（連番）を外部に見せると番号の推測による列挙攻撃・覗き見のリスクになる。外部に公開するのは`short_key`のみとし、レスポンスDTOにも`id`を含めない。

### なぜリダイレクトを302にするか
短縮URLの飛び先は将来差し替えられる可能性があり、また同一URLでも毎回新しいキーを発行する仕様（使い回さない）のため、恒久的なリダイレクトを意味する301ではなく、一時的なリダイレクトを意味する302を使う。ブラウザ・クライアント側にキャッシュされて古い挙動が固定化されるのを避ける狙いもある。

### なぜURLの形式チェックを `http://`/`https://` の前方一致にするか
シンプルな判定で明らかに不正な入力（空文字、プロトコルなしの文字列など）を弾くための最低限の検証。Bean Validation（`@Pattern`）で宣言的に表現し、Controllerにロジックを持ち込まない。

### なぜDB接続情報（`DB_URL`/`DB_USERNAME`/`DB_PASSWORD`）に既定値を持たせないか
機密情報のため、application.ymlにハードコードせず環境変数必須にしている（本番想定の書き方）。ローカル開発時は起動前に環境変数を設定する必要がある。

### なぜbase-url（`APP_BASE_URL`）には既定値（`http://localhost:8080`）を持たせるか
base-urlはshort_keyと組み合わさって外部に公開される情報であり、秘匿すべき機密情報ではない。DB接続情報とは性質が異なるため、開発時にすぐ動かせるようローカル既定値を用意し、本番環境では環境変数で上書きする。

### なぜスキーマ管理にFlywayを使うか（Hibernateのddl-autoで自動生成しない）
`urls`テーブルのUNIQUE制約やDEFAULT now()といった設計意図をSQLマイグレーション（`db/migration/V1__create_urls_table.sql`）として明示的に残し、レビュー可能にするため。Hibernateの`ddl-auto`は`validate`にとどめ、entityとテーブルの整合性検証のみに使う。

## 5. テスト

- `service/UrlServiceTest`：キー生成の成功・衝突時リトライ・上限到達時の例外、click_count加算、未検出時の例外をMockitoでモック化した`UrlRepository`を使って検証済み。
- Controllerの`@WebMvcTest`は未実装（今後の課題）。
- CORS設定は未実装（フロントエンド実装時に対応予定）。

## 6. 起動方法

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/url_shortener"
$env:DB_USERNAME="<db-user>"
$env:DB_PASSWORD="<db-password>"
./gradlew.bat bootRun
```

- `./gradlew.bat test` … テスト実行
- `./gradlew.bat build` … ビルド
