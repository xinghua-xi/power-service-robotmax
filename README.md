# 电力服务智能系统

一个基于 Spring Boot + Vue 3 的电力服务智能助手系统，为电力用户提供在线咨询、故障报修、电费查询等服务。

## 项目概述

本系统是西把栅供电所的电力服务平台，集成了 AI 智能对话、实时电力监控、用户认证等功能，旨在提升电力服务效率和用户体验。

## 技术栈

### 后端
- Java 21
- Spring Boot 3.3.4
- Spring Security + JWT 认证
- Spring Data JPA
- MySQL 数据库
- Redis 缓存
- DeepSeek AI API（流式对话）

### 前端
- Vue 3.3
- Vue Router 4
- Axios
- Vue CLI 5

## 项目结构

```
├── backend/                    # 后端项目
│   ├── src/main/java/com/powerservice/system/
│   │   ├── config/            # 配置类（Security、Web）
│   │   ├── controller/        # 控制器
│   │   ├── dto/               # 数据传输对象
│   │   ├── entity/            # 实体类
│   │   ├── repository/        # 数据访问层
│   │   ├── service/           # 业务逻辑层
│   │   └── util/              # 工具类
│   └── pom.xml
│
└── frontend/                   # 前端项目
    ├── src/
    │   ├── api/               # API 接口
    │   ├── components/        # 组件
    │   ├── services/          # 服务层
    │   ├── views/             # 页面视图
    │   └── router/            # 路由配置
    └── package.json
```

## 功能特性

- 🤖 AI 智能对话：集成 DeepSeek API，支持流式响应
- 📊 电力监控：实时电力数据图表展示
- 🔐 用户认证：支持账号密码登录和面容识别登录
- 📝 故障报修：在线提交电力故障报修
- 💰 电费查询：查询电量余额和账户信息
- 📷 文件读取：摄像头扫描文件功能
- 📚 知识库：电力服务常见问题解答

## 环境要求

- JDK 21+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+

## 快速开始

### 后端启动

1. 配置数据库连接（修改 `application.yml`）

2. 构建并运行：
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8081`

### 前端启动

1. 安装依赖：
```bash
cd frontend
npm install
```

2. 配置环境变量（复制 `.env.example` 为 `.env`）：
```bash
cp .env.example .env
```

3. 启动开发服务器：
```bash
npm run serve
```

前端服务默认运行在 `http://localhost:8080`

### 生产构建

```bash
# 前端构建
cd frontend
npm run build

# 后端打包
cd backend
mvn clean package
```

## API 接口

| 模块 | 接口 | 说明 |
|------|------|------|
| 认证 | POST /api/auth/login | 账号密码登录 |
| 认证 | POST /api/auth/face-login | 面容登录 |
| 聊天 | GET /api/chat/stream | AI 流式对话 |
| 聊天 | GET /api/chat/history/{sessionId} | 获取聊天历史 |
| 监控 | GET /api/monitor/electricity | 获取电力数据 |
| 用户 | GET /api/users | 获取用户列表 |
| 知识库 | GET /api/knowledge-base | 获取知识库 |

## 配置说明

### 后端配置 (application.yml)

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/power_service
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379
```

### 前端配置 (.env)

```env
VUE_APP_API_BASE_URL=http://localhost:8081
VUE_APP_TITLE=电力服务系统
```

## 开发团队

风风

## 许可证

MIT License
