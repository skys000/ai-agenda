# 智能日程管家

基于 Spring Boot、Spring AI 与 Vue3 的 AI 日程管理系统。系统支持用户登录、日程管理、分类管理、角色菜单管理、多会话 AI 对话，并通过 Function Calling 将自然语言指令转化为日程新增操作。

## 功能特性

- AI 对话式日程管理：接入 OpenAI 兼容模型，通过 Spring AI 实现自然语言交互。
- Function Calling 工具调用：将“明天下午三点开会”等指令解析并写入日程表。
- 多轮对话记忆：基于 Redis 实现会话记忆存储，并限制历史消息长度以控制上下文成本。
- 用户认证与权限保护：使用 Spring Security + JWT 实现登录认证和接口访问控制。
- 业务管理模块：支持用户、角色、菜单、日程分类、日程记录、AI 消息等 CRUD。
- 前端交互界面：基于 Vue3、Element Plus 与 FullCalendar 实现日程视图、AI 对话和后台管理页面。

## 技术栈

**后端**

- Java 17
- Spring Boot 3.4
- Spring AI
- Spring Security
- MyBatis-Plus
- MySQL
- Redis
- JWT
- Knife4j

**前端**

- Vue3
- Vite
- Element Plus
- Vue Router
- Axios
- FullCalendar

## 项目结构

```text
ai-agenda-public/
├── ai-agenda-backend/      # Spring Boot 后端
│   ├── src/main/java/      # 后端源码
│   └── src/main/resources/ # 配置、Mapper、Prompt 模板
└── ai-agenda-frontend/     # Vue3 前端
    └── src/                # 前端页面、路由、接口封装
```

## 后端运行

1. 创建 MySQL 数据库：

```sql
CREATE DATABASE ai_agenda_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 配置环境变量：

```powershell
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="your_mysql_password"
$env:SILICONFLOW_API_KEY="your_api_key"
$env:JWT_SECRET="replace_with_a_long_random_secret"
```

3. 启动后端：

```powershell
cd ai-agenda-backend
.\mvnw.cmd spring-boot:run
```

默认服务端口为 `6887`。

## 前端运行

```powershell
cd ai-agenda-frontend
npm install
npm run dev
```

## 说明

本项目来自校内人工智能大模型应用实训，重点实践 Java 后端工程与大模型应用集成，包括 Spring AI 对话、Function Calling、Redis 会话记忆、JWT 鉴权与 Vue3 前端管理页面。
