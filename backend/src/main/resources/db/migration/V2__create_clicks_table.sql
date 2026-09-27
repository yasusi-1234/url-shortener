-- レベル2：クリック履歴。urls.id への参照を持ち、日別・デバイス別の集計に使う
-- IPアドレスは記録しない（プライバシー配慮。device_type/browserの粒度で分析には十分という設計判断。design.md第10章参照）
CREATE TABLE clicks (
    id BIGSERIAL PRIMARY KEY,
    url_id BIGINT NOT NULL REFERENCES urls(id),
    clicked_at TIMESTAMP NOT NULL DEFAULT now(),
    device_type VARCHAR(32) NOT NULL,
    browser VARCHAR(32) NOT NULL
);

-- 集計API（GET /api/urls/{shortKey}/stats）が url_id 単位・日別にGROUP BYするためのインデックス
CREATE INDEX idx_clicks_url_id_clicked_at ON clicks (url_id, clicked_at);
