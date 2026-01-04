USE power_service_db;

-- 更新所有null的bot_response为''
UPDATE chat_records SET bot_response = '' WHERE bot_response IS NULL;

-- 修改字段为NOT NULL约束
ALTER TABLE chat_records MODIFY COLUMN bot_response TEXT NOT NULL;