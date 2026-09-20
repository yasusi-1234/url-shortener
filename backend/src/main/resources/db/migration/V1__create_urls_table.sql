-- id（内部ID）とshort_key（公開キー）を分離：主キーを外部に晒すと連番推測による列挙・覗き見のリスクになるため
CREATE TABLE urls (
    id BIGSERIAL PRIMARY KEY,
    short_key VARCHAR(16) NOT NULL UNIQUE, -- UNIQUE制約はアプリ側の衝突チェックをすり抜けても重複を防ぐDBレベルの保証（多層防御）
    original_url TEXT NOT NULL,
    click_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
