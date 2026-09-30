-- カスタム短縮キー機能（design.md第11章）：カスタムキーは最大30文字まで許容するため拡張
ALTER TABLE urls ALTER COLUMN short_key TYPE VARCHAR(30);
