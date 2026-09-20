# CLAUDE.md（backend / Spring Boot・Java）

## 役割
このディレクトリは **API専用のバックエンド**。画面（Thymeleaf等のテンプレート）は持たない。JSONを返すことに専念する。
API仕様の詳細は、親フォルダの `url-shortener-design.md` 第6章を参照。

## 技術
- Java / Spring Boot
- DB: PostgreSQL
- ビルド: Gradle（build.gradle はこの backend 直下）

## アーキテクチャ（レイヤード）
- `controller/` … REST エンドポイント。リクエストを受けて Service を呼ぶだけ。ロジックを書かない
- `service/`    … ビジネスロジック（キー発行・衝突チェック・登録・検索）を集約
- `repository/` … DB アクセス（urls テーブル）
- `model/` または `entity/` … ドメインオブジェクト
- `dto/`        … API のリクエスト/レスポンス用。エンティティを直接APIに晒さない

## コーディング規約
- コンストラクタインジェクションを使う（フィールドインジェクションは使わない）
- 入力検証は Bean Validation（@Valid 等）を使う
- 例外は @ControllerAdvice で一元的にハンドリングする
- 層をまたぐ責務の混在を避ける（Controller にロジックを書かない、等）

## このアプリ固有のルール
- 短縮キーは a-z A-Z 0-9 の62文字からランダムに6文字生成し、DBに既存なら生成し直す（衝突チェック）。
- short_key には UNIQUE 制約を付け、DBレベルでも重複を防ぐ。
- 内部ID（id）は外部レスポンスに出さない。外部に見せるのは short_key のみ。
- 同一URLの再登録でも毎回新しいキーを発行する。
- 存在しない short_key へのアクセスは 404 を返す。
- フロント（別オリジン）から /api/** を叩けるよう CORS を設定する。

## テスト
- Service のロジックは単体テストを書く（特にキー生成・衝突時の再生成）
- Controller は @WebMvcTest 等でテストする

## コマンド
- `./gradlew bootRun` … 起動
- `./gradlew test`    … テスト
- `./gradlew build`   … ビルド
