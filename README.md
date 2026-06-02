# 智能日程管家

智能日程管家是一个基于 **Spring Boot + Spring AI + Vue3** 的 AI 日程管理系统。项目围绕“用自然语言管理个人日程”展开，支持用户登录、日程 CRUD、分类管理、多会话 AI 对话、对话记录留存和后台权限菜单管理。

本项目来自校内人工智能大模型应用实训，重点实践 Java 后端工程和大模型应用集成。

## 核心亮点

- **Spring AI 对话集成**：通过 OpenAI 兼容接口接入大模型，在后端统一封装普通对话和流式对话。
- **Function Calling 工具调用**：注册 `addSchedule` 工具，将“明天下午三点开组会”这类自然语言指令转为结构化日程写入。
- **Redis 会话记忆**：自定义 `ChatMemory`，按会话保存最近多轮消息，兼顾上下文连续性与 token 成本控制。
- **JWT 鉴权体系**：使用 Spring Security + JWT 实现无状态登录认证和接口访问保护。
- **后台管理能力**：实现用户、角色、菜单、日程分类、日程记录、AI 消息等模块的接口和页面。
- **前后端完整闭环**：Vue3 + Element Plus + FullCalendar 提供日程视图、AI 聊天和系统管理界面。

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 后端 | Java 17, Spring Boot 3.4, Spring AI, Spring Security, MyBatis-Plus |
| 数据 | MySQL, Redis |
| 前端 | Vue3, Vite, Element Plus, Vue Router, Axios, FullCalendar |
| 工具 | JWT, Knife4j, Maven |

## 系统架构

```text
Vue3 前端
  ├─ 日程视图 / AI 对话 / 后台管理
  ▼
Spring Boot API
  ├─ Spring Security + JWT 鉴权
  ├─ MyBatis-Plus 业务数据访问
  ├─ Spring AI ChatClient
  │   ├─ Redis ChatMemory 多轮对话记忆
  │   └─ Function Calling 日程工具调用
  ▼
MySQL / Redis / OpenAI 兼容模型服务
```

## 项目结构

```text
ai-agenda/
├── ai-agenda-backend/      # Spring Boot 后端
│   ├── src/main/java/      # Controller、Service、Mapper、配置类
│   └── src/main/resources/ # application 配置、Mapper XML、Prompt 模板
└── ai-agenda-frontend/     # Vue3 前端
    ├── src/api/            # 接口封装
    ├── src/layout/         # 后台布局
    ├── src/router/         # 路由和登录守卫
    └── src/views/          # 业务页面
```

## 快速启动

### 1. 准备环境

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8+
- Redis 6+
- 硅基流动或其他 OpenAI 兼容模型服务

### 2. 初始化数据库

```sql
CREATE DATABASE ai_agenda_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 配置后端环境变量

```powershell
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="your_mysql_password"
$env:SILICONFLOW_API_KEY="your_api_key"
$env:JWT_SECRET="replace_with_a_long_random_secret"
```

也可以参考：

```text
ai-agenda-backend/src/main/resources/application-example.yml
```

### 4. 启动后端

方式一：使用 IntelliJ IDEA 打开 `ai-agenda-backend`，运行 `AiAgendaApplication`。

方式二：使用本机 Maven：

```powershell
cd ai-agenda-backend
mvn spring-boot:run
```

后端默认端口：`6887`。

### 5. 启动前端

```powershell
cd ai-agenda-frontend
npm install
npm run dev
```

前端环境变量参考：

```text
ai-agenda-frontend/.env.example
```

## 简历描述参考

> 智能日程管家：基于 Spring Boot 3、Spring AI、Redis、MyBatis-Plus 和 Vue3 实现的 AI 日程管理系统。负责后端架构设计、JWT 鉴权、多轮对话记忆、Function Calling 工具调用和前端管理页面开发，实现自然语言新增日程、多会话 AI 对话、日程分类与系统管理等功能。
