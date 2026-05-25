# 校园自动访客管理系统

SpringBoot + Vue 3 全栈项目，实现访客预约、门禁通行、数据统计分析等功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | SpringBoot 2.7.18 |
| 持久层 | MyBatis + PageHelper + MySQL 8.0（兼容 9.x） |
| 认证 | JWT + BCrypt（无状态） |
| 前端框架 | Vue 3 + Vite |
| UI 组件库 | Element Plus |
| 图表 | ECharts 6.x |
| 状态管理 | Pinia |
| 国际化 | vue-i18n v9（前端） + Spring MessageSource（后端） |

## 环境要求

- **JDK** 8+（开发环境 JDK 23，源码级别 Java 1.8）
- **Maven** 3.6+（含 mvnw 包装器，无需手动安装）
- **Node.js** 18+
- **MySQL** 8.0+（兼容 9.6）

## 快速开始

### 0. 确保 MySQL 服务已启动

Windows 上 MySQL 作为服务运行，启动前先检查状态：

```bash
# 查看 MySQL 服务状态（服务名可能是 MySQL96 / MySQL80 等）
sc query MySQL96
```

如果显示 `STATE : 1 STOPPED`，需要**以管理员身份**打开命令行启动：

```bash
net start MySQL96
```

### 1. 数据库初始化

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE IF NOT EXISTS visitor_system;

# 执行建库建表脚本（含种子数据）
USE visitor_system;
SOURCE d:/VS code/visitor system/visitor-backend/src/main/resources/db/schema.sql;
```

> 如果已有数据库只需要增量索引优化，执行：
> ```bash
> SOURCE /path/to/visitor-backend/src/main/resources/db/add-indexes.sql;
> ```

### 2. 数据库连接配置

编辑 `visitor-backend/src/main/resources/application.yml`，修改数据库连接信息（替换为实际 MySQL root 密码）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/visitor_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: your_password_here
```

### 3. 启动后端（SpringBoot）

```bash
cd visitor-backend

# macOS / Linux
./mvnw spring-boot:run

# Windows
mvnw spring-boot:run
```

后端默认运行在 **http://localhost:8080**

### 4. 启动前端（Vue 3 + Vite）

```bash
cd visitor-frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

前端默认运行在 **http://localhost:3000**，开发模式下 `/api` 请求自动代理到后端 8080 端口。

> 如果 3000 端口被占用，Vite 会自动切换为 3001（或下一个可用端口）。后端 CORS 使用通配符 `localhost:*`，兼容任意端口。

### 5. 访问系统

浏览器打开 **http://localhost:3000**，使用以下账号登录：

| 角色 | 用户名 | 密码 | 权限 |
|------|--------|------|------|
| 系统管理员 | admin | admin123 | 全部功能 |
| 普通管理员 | （暂未创建） | — | 访客/预约查看 |

> 详细操作指南请参阅 **[USAGE.md](USAGE.md)**（覆盖全部 9 个页面、3 个用户工作流、FAQ）。

### 6. 公网访问（可选 — Cloudflare Tunnel）

如需任何联网设备访问本地前端，可使用 Cloudflare Tunnel 内网穿透（无需域名和账号）：

```bash
# 下载 cloudflared（Windows，一次性操作）
curl -L -o "%USERPROFILE%/cloudflared.exe" "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe"

# macOS
brew install cloudflared

# 启动隧道（前端需先运行在 3000 端口）
cloudflared tunnel --url http://localhost:3000
```

运行后会生成类似 `https://xxx.trycloudflare.com` 的公网 URL，任何联网设备可通过该地址访问系统。

> 此方式为开发测试用途，免费版无 SLA 保障。如需生产环境部署，建议使用云服务器。

## 项目结构

```
visitor system/
├── visitor-backend/               # SpringBoot 后端
│   ├── src/main/java/com/visitor/
│   │   ├── annotation/            # 自定义注解（@RequirePermission）
│   │   ├── common/                # 通用类（Result, GlobalExceptionHandler）
│   │   ├── config/                # 配置（WebMvc含CORS, JWT 拦截器, BCrypt）
│   │   ├── controller/            # 控制器
│   │   ├── dto/                   # 数据传输对象
│   │   ├── entity/                # 实体类
│   │   ├── mapper/                # MyBatis Mapper 接口
│   │   ├── service/               # 业务接口
│   │   │   └── impl/              # 业务实现
│   │   └── utils/                 # 工具类（JWT）
│   └── src/main/resources/
│       ├── db/schema.sql          # 建表 + 种子数据
│       ├── messages.properties    # i18n 资源（中/英文）
│       └── mapper/                # MyBatis XML 映射
├── visitor-frontend/              # Vue 3 前端
│   └── src/
│       ├── api/                   # Axios 请求模块
│       ├── router/                # Vue Router 配置
│       ├── store/                 # Pinia 状态管理
│       ├── locales/               # 语言包（zh.js / en.js，各 160+ 翻译键）
│       ├── i18n/                  # vue-i18n 实例
│       └── views/                 # 页面组件（9 个页面，全部已国际化）
└── README.md
```

## 核心接口

### 认证
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/login` | 登录 | 否 |

### 访客管理
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/visitor/page` | 分页列表 | visitor:list |
| POST | `/api/visitor/add` | 新增访客 | visitor:add |
| PUT | `/api/visitor/edit` | 编辑访客 | visitor:edit |
| DELETE | `/api/visitor/delete/{id}` | 删除访客 | visitor:delete |
| PUT | `/api/visitor/{id}/blacklist` | 黑名单切换 | visitor:edit |

### 预约管理
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/appointment/submit` | 在线预约 | 否 |
| GET | `/api/appointment/query` | 预约查询 | 否 |
| GET | `/api/appointment/list` | 审批列表 | 是 |
| PUT | `/api/appointment/{id}/review` | 审批 | appointment:review |

### 门禁管理
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/access-log/entry` | 入校登记 | access-log:list |
| PUT | `/api/access-log/{id}/exit` | 离校登记 | access-log:list |
| GET | `/api/access-log/page` | 通行记录 | access-log:list |
| GET | `/api/access-log/on-campus` | 当前在校 | access-log:list |
| GET | `/api/access-log/overstay` | 超时告警 | access-log:list |

### 统计分析
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/statistics/overview` | 首页概览 | dashboard |
| GET | `/api/statistics/traffic?period=day` | 流量统计 | dashboard |
| GET | `/api/statistics/time-distribution` | 时段分布 | dashboard |
| GET | `/api/statistics/reason-distribution` | 原因分布 | dashboard |

## 语言切换

系统支持中文和英文两种语言，前端 9 个页面和后端 25 条业务消息均已国际化。

- **覆盖范围**：前端全部文本（标签、按钮、表格、提示、图表、表单验证）+ Element Plus 组件内置文本 + 页面标题 + 后端业务消息
- **访客端**：页面右上角有语言切换下拉菜单（AppointmentForm、AppointmentQuery、Login）
- **管理端**：Dashboard 顶部右侧用户头像旁有语言切换按钮
- **切换方式**：选择语言后页面即时响应切换（`locale.value = lang`），无需刷新，语言偏好保存在 `localStorage.lang`
- **后端响应**：前端 Axios 拦截器自动在 `Accept-Language` 请求头中携带当前语言，后端根据请求头返回对应语言的提示消息
- **注意事项**：`el-dropdown` 语言切换器必须使用 `trigger="click"` 以确保 `@command` 事件可靠触发

## 安全说明

- 所有 MyBatis SQL 使用 `#{}` 占位符，防止 SQL 注入
- 密码使用 BCrypt 加密存储
- JWT Token 过期时间 24 小时
- 跨域允许 `localhost` 任意端口和 `*.trycloudflare.com`（开发模式 + Cloudflare Tunnel）

## 常见问题

**Q: 后端启动报 "Public Key Retrieval is not allowed"？**

MySQL 8.0+ 默认使用 `caching_sha2_password` 认证插件。JDBC 驱动在未启用 SSL 时需要显式允许公钥检索。将 JDBC URL 中的 `allowPublicKeyRetrieval=true` 参数补上即可。详见上方"数据库连接配置"部分的 URL 示例。

**Q: 后端报 "Communications link failure" 或 "Failed to obtain JDBC Connection"？**

通常是 MySQL 服务未启动。执行 `sc query MySQL96`（或 `MySQL80`）检查服务状态，如果 STATE 显示 STOPPED，用管理员命令行执行 `net start MySQL96` 启动服务。

**Q: MySQL 9.x 执行 schema.sql 报 "Data too long" 错误？**

MySQL 9.x 对 utf8mb4 处理更严格。如果遇到此问题，可通过命令行逐条执行 INSERT 语句，或先 `SOURCE schema.sql` 建表，再手动执行种子数据部分。

**Q: 前端启动在 3001 而非 3000？**

Vite 检测到 3000 被占用后会自动切换端口。后端 CORS 使用 `localhost:*` 通配符，兼容任意端口，不影响使用。

**Q: 如何切换系统语言？**

访客端页面（预约申请、预约查询、登录）右上角和管理端 Dashboard 顶部均有语言切换下拉菜单。选择语言后页面即时切换（无需刷新），语言偏好保存在浏览器中。后端会根据前端发送的 `Accept-Language` 请求头返回对应语言的提示消息。

**Q: Cloudflare Tunnel 访问时提示拦截/403？**

Vite 默认只允许 `localhost` 访问。需确保 `vite.config.js` 中配置了 `allowedHosts: ['.trycloudflare.com']` 和 `host: '0.0.0.0'`，且后端 CORS 的 `allowedOriginPatterns` 包含 `https://*.trycloudflare.com`。重启前后端后生效。