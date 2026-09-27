# Render へのデプロイ手順（backend）

## 0. 前提：RegionをDBに合わせる

Render の PostgreSQL は **Singapore** リージョンで作成済み。Web Service（このbackend）の Region も **必ず Singapore に合わせる**こと。

理由：Render の **Internal Database URL**（プライベートネットワーク経由、外部公開されず低レイテンシ）は、同一リージョン内のサービス間でのみ疎通する。Web ServiceのRegionがDBと異なると、Internal URLでは接続できない（別リージョンにする場合はExternal Database URL＋`sslmode=require`が必要になり、レイテンシも増える）。

## 1. Render側の設定

- **Language**: Docker
- **Root Directory**: `backend`
- **Dockerfile Path**: `backend/Dockerfile`（Root Directoryを`backend`にした場合は`Dockerfile`のみでも可）
- **Region**: Singapore（DBと同じ）

## 2. 環境変数（Renderの管理画面に手入力する。コード・GitHubには一切含まれない）

| 変数名 | 値の決め方 | 必須 |
|---|---|---|
| `DB_URL` | Renderの **Internal Database URL** は `postgres://user:password@host/dbname` という形式で提供されるが、Spring Bootは `jdbc:postgresql://host/dbname` という**JDBC形式**を要求する。**scheme を `postgres://` → `jdbc:postgresql://` に変え、ユーザー名/パスワード部分は取り除いて** `DB_URL` に設定する（例: `jdbc:postgresql://<host>/<dbname>`）。 | ✅ |
| `DB_USERNAME` | Renderが発行したDBのUsername（Internal Database URLの`user`部分と同じ値） | ✅ |
| `DB_PASSWORD` | Renderが発行したDBのPassword | ✅ |
| `PORT` | **設定不要。Render側が自動で注入する。** | - |
| `APP_BASE_URL` | このbackend自身の公開URL（例: `https://xxxx.onrender.com`）。短縮URLの生成に使われる。Render側でサービスを作成すると発行されるURLが分かった時点で設定する | ✅ |
| `CORS_ALLOWED_ORIGIN` | フロントエンドの本番URL。**フロントのRenderデプロイ後、URLが確定してから設定する**（現時点では未定のためプレースホルダのままでよい） | ✅（フロント確定後） |

- 上記のうち `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` に既定値は無い（コード上 `${DB_URL}` のようにプレースホルダのみで、ローカル開発用のフォールバック値も持たせていない＝設定を忘れると起動時エラーになる形にしてある）。
- `APP_BASE_URL` / `CORS_ALLOWED_ORIGIN` はコード上 `${APP_BASE_URL:http://localhost:8080}` のようにローカル用の既定値を持つが、本番では必ず環境変数で上書きすること。

## 3. Flywayマイグレーションについて

追加の設定は不要。`spring.flyway.enabled: true`（`application.yml`）により、Spring Boot起動時に自動でFlywayが`V1__create_urls_table.sql` / `V2__create_clicks_table.sql`を適用してからアプリが立ち上がる。ローカルでこれまで何度も確認済みの挙動と同じで、ホスティング環境が変わっても動作は変わらない。

## 4. ローカルでの確認結果

- `./gradlew clean build`（DB接続情報を環境変数で与えた状態）→ **BUILD SUCCESSFUL、全13テスト成功**
- Dockerfileは作成・レビュー済みだが、この環境ではDockerデーモンが起動しておらず `docker build` による実機検証は未実施。Render上の初回デプロイ時のビルドログを確認すること。

## 5. デプロイ後の確認手順

1. Renderのデプロイログで `Started UrlShortenerBackendApplication` が出ることを確認
2. `https://<render-url>/api/shorten` にPOSTして201が返るか確認
3. 返ってきた`shortUrl`にアクセスして302リダイレクトすることを確認
4. フロントのRegionと`CORS_ALLOWED_ORIGIN`を確定させてから、フロントから実際に叩けるか確認
