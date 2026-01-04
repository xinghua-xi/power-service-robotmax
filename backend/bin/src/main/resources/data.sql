INSERT INTO users (
  username, password, real_name, role, 
  face_data, face_registered, is_active, 
  created_at, updated_at
) VALUES (
  'admin', 
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
  '管理员', 
  'ADMIN', 
  'test-face-data', 
  1,  -- 替换true为1
  1, 
  NOW(), 
  NOW()
) ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  real_name = VALUES(real_name),
  role = VALUES(role),
  face_data = VALUES(face_data),
  face_registered = VALUES(face_registered),
  is_active = VALUES(is_active),
  updated_at = NOW();

-- 插入服务类型数据
INSERT INTO service_types (name, description, icon, sort_order, is_active, created_at, updated_at)
VALUES 
('电费查询', '电费查询相关知识', 'icon-electric', 1, 1, NOW(), NOW()),
('故障报修', '故障报修相关知识', 'icon-repair', 2, 1, NOW(), NOW()),
('业务咨询', '业务咨询相关知识', 'icon-consult', 3, 1, NOW(), NOW()),
('缴费方式', '缴费方式相关知识', 'icon-payment', 4, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  icon = VALUES(icon),
  sort_order = VALUES(sort_order),
  is_active = VALUES(is_active),
  updated_at = NOW();

-- 插入知识库数据
INSERT INTO knowledge_base (question, answer, service_type_id, keywords, hit_count, is_active, created_at, updated_at)
VALUES 
('如何查询电费余额？', '您可以通过微信公众号、支付宝生活缴费、电力APP或拨打客服热线查询电费余额。', 1, '电费查询,余额,微信,支付宝', 0, 1, NOW(), NOW()),
('电费账单如何查看？', '您可以登录电力APP或网上营业厅，在个人中心查看历史账单明细。', 1, '电费账单,查看,APP,网上营业厅', 0, 1, NOW(), NOW()),
('电表故障如何报修？', '请拨打客服热线95598进行故障报修，或登录电力APP提交报修申请。', 2, '电表故障,报修,95598,APP', 0, 1, NOW(), NOW()),
('家里停电怎么办？', '首先检查是否是自己家的问题，如总开关是否跳闸；如果是整栋楼停电，请拨打95598咨询。', 2, '停电,处理,95598,开关', 0, 1, NOW(), NOW()),
('如何办理用电开户？', '携带身份证、房产证等相关证件到当地供电营业厅办理用电开户手续。', 3, '用电开户,办理,证件,营业厅', 0, 1, NOW(), NOW()),
('电费可以通过哪些方式缴纳？', '电费可以通过微信、支付宝、银行代扣、电力APP、营业厅现金缴费等方式缴纳。', 4, '电费缴费,方式,微信,支付宝,银行代扣', 0, 1, NOW(), NOW()),
('如何办理电费过户？', '买卖双方携带身份证、房产证等证件到供电营业厅办理电费过户手续。', 3, '电费过户,办理,证件,营业厅', 0, 1, NOW(), NOW()),
('峰谷电价是什么意思？', '峰谷电价是指将一天24小时分为高峰、低谷等多个时段，不同时段执行不同的电价标准。', 4, '峰谷电价,时段,电价标准', 0, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  question = VALUES(question),
  answer = VALUES(answer),
  service_type_id = VALUES(service_type_id),
  keywords = VALUES(keywords),
  hit_count = VALUES(hit_count),
  is_active = VALUES(is_active),
  updated_at = NOW();

-- 插入服务类型数据
INSERT INTO service_types (name, description, icon, sort_order, is_active, created_at, updated_at)
VALUES 
('电费查询', '电费查询相关知识', 'icon-electric', 1, 1, NOW(), NOW()),
('故障报修', '故障报修相关知识', 'icon-repair', 2, 1, NOW(), NOW()),
('业务咨询', '业务咨询相关知识', 'icon-consult', 3, 1, NOW(), NOW()),
('缴费方式', '缴费方式相关知识', 'icon-payment', 4, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  icon = VALUES(icon),
  sort_order = VALUES(sort_order),
  is_active = VALUES(is_active),
  updated_at = NOW();

-- 插入知识库数据
INSERT INTO knowledge_base (question, answer, service_type_id, keywords, hit_count, is_active, created_at, updated_at)
VALUES 
('如何查询电费余额？', '您可以通过微信公众号、支付宝生活缴费、电力APP或拨打客服热线查询电费余额。', 1, '电费查询,余额,微信,支付宝', 0, 1, NOW(), NOW()),
('电费账单如何查看？', '您可以登录电力APP或网上营业厅，在个人中心查看历史账单明细。', 1, '电费账单,查看,APP,网上营业厅', 0, 1, NOW(), NOW()),
('电表故障如何报修？', '请拨打客服热线95598进行故障报修，或登录电力APP提交报修申请。', 2, '电表故障,报修,95598,APP', 0, 1, NOW(), NOW()),
('家里停电怎么办？', '首先检查是否是自己家的问题，如总开关是否跳闸；如果是整栋楼停电，请拨打95598咨询。', 2, '停电,处理,95598,开关', 0, 1, NOW(), NOW()),
('如何办理用电开户？', '携带身份证、房产证等相关证件到当地供电营业厅办理用电开户手续。', 3, '用电开户,办理,证件,营业厅', 0, 1, NOW(), NOW()),
('电费可以通过哪些方式缴纳？', '电费可以通过微信、支付宝、银行代扣、电力APP、营业厅现金缴费等方式缴纳。', 4, '电费缴费,方式,微信,支付宝,银行代扣', 0, 1, NOW(), NOW()),
('如何办理电费过户？', '买卖双方携带身份证、房产证等证件到供电营业厅办理电费过户手续。', 3, '电费过户,办理,证件,营业厅', 0, 1, NOW(), NOW()),
('峰谷电价是什么意思？', '峰谷电价是指将一天24小时分为高峰、低谷等多个时段，不同时段执行不同的电价标准。', 4, '峰谷电价,时段,电价标准', 0, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  question = VALUES(question),
  answer = VALUES(answer),
  service_type_id = VALUES(service_type_id),
  keywords = VALUES(keywords),
  hit_count = VALUES(hit_count),
  is_active = VALUES(is_active),
  updated_at = NOW();