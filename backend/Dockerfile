# Render (Language: Docker) 向けのビルド定義。
# 前提：Web ServiceのRegionは、接続するPostgres（Singapore）と必ず同じにすること。
#       Render の Internal Database URL は同一リージョン内のプライベートネットワークでのみ疎通する。

# ---- Build stage ----
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# 依存関係の解決だけ先に行い、Dockerのレイヤーキャッシュを効かせる
# （ソースだけを変更した再ビルドで、依存ライブラリを毎回ダウンロードし直さないようにする）
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew
RUN ./gradlew --no-daemon dependencies

# アプリ本体をビルドする。テストはここでは実行しない
# （このビルド環境はDBに接続できないため、DB接続を要するテストは別途CI/ローカルで実施する）
COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 非rootユーザーで実行する
RUN groupadd -r spring && useradd -r -g spring spring
# 成果物名は build.gradle の bootJar.archiveFileName で app.jar に固定してある
# （plain jarも無効化済みのため build/libs/ には app.jar のみが生成される）
COPY --from=build /app/build/libs/app.jar app.jar
USER spring

# Renderは環境変数PORTでこのコンテナが待受けるべきポートを指定してくる。
# application.yml の server.port: ${PORT:8080} でこれを受け取る（未設定時はローカル用に8080）。
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
