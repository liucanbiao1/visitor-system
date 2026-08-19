# CLAUDE.md — 校园自动访客管理系统

本文件是 Claude Code 在此项目中的行为准则与上下文记忆库。所有开发决策优先遵循本文件约定。

---

## 1. 系统技术栈（固化）

| 层级 | 技术 | 版本约束 |
|---|---|---|
| 后端框架 | SpringBoot | 2.x（当前 2.7.18） |
| 后端语言 | Java | 1.8（source level） |
| 持久层 | MyBatis + PageHelper + MySQL | MySQL 8.0+（兼容 9.x） |
| 构建工具 | Maven + mvnw | 无 Lombok |
| 认证 | JWT (jjwt 0.9.1) + BCrypt | 无状态 |
| 前端框架 | Vue 3 + Vite | Composition API (`<script setup>`) |
| UI 组件库 | Element Plus | ElementUI 的 Vue 3 替代 |
| HTTP 客户端 | Axios | `src/api/request.js` 统一封装 |
| 图表 | ECharts | 6.x（已集成） |
| 状态管理 | Pinia | `src/store/index.js` |
| 国际化 | vue-i18n v9（前端） + MessageSource（后端） | `Accept-Language` 请求头 |

### 关键约束
- **不使用 Lombok** — JDK 23 编译兼容性问题，所有 getter/setter/constructor 手动编写
- **不使用完整 Spring Security** — 仅使用 `spring-security-crypto`（BCrypt），鉴权通过自定义 `JwtInterceptor` 实现
- **密码加密** — `BCryptPasswordEncoder`（Bean 定义在 [SecurityConfig.java](visitor-backend/src/main/java/com/visitor/config/SecurityConfig.java)）
- **数据库连接参数** — JDBC URL 必须包含 `allowPublicKeyRetrieval=true`，MySQL 8.0+ 默认使用 `caching_sha2_password` 认证插件，JDBC 驱动需要该参数才能完成握手
- **MySQL 服务管理** — Windows 上 MySQL 作为服务运行（如 `MySQL96`），启动前需确保服务状态为 RUNNING（`sc query MySQL96`），必要时用管理员权限执行 `net start MySQL96`
- **本地 JDK 版本** — 运行环境 JDK 23，但源码级别锁定 Java 1.8
- **分页插件** — PageHelper 1.4.7（`pagehelper-spring-boot-starter`），在 Service 层通过 `PageHelper.startPage()` + `PageInfo<>` 自动追加 LIMIT 并执行 COUNT
- **CORS 跨域** — 通过 [WebMvcConfig.java](visitor-backend/src/main/java/com/visitor/config/WebMvcConfig.java) 的 `addCorsMappings()` 配置，使用 `allowedOriginPatterns`（Ant 风格通配符 `http://localhost:*`、`https://*.trycloudflare.com`），不使用 CorsFilter
- **国际化 (i18n)** — 前端 vue-i18n v9（Composition API 模式，`legacy: false`），后端 `AcceptHeaderLocaleResolver` + `ResourceBundleMessageSource`，语言通过 `Accept-Language` 请求头传递
- **Vite 外部访问** — [vite.config.js](visitor-frontend/vite.config.js) 配置 `host: '0.0.0.0'` 监听所有网络接口，`allowedHosts: ['.trycloudflare.com']` 允许 Cloudflare Tunnel 域名

---

## 2. 前后端开发规范

### 2.1 后端规范

**分层架构（严格遵循）：**
```
Controller → Service (interface) → ServiceImpl → Mapper (interface) → XML
```
- Controller 只做参数校验和结果返回，禁止包含业务逻辑
- Service 层负责业务逻辑和事务
- Mapper 只做数据访问，SQL 写在 XML 中

**统一响应格式：**
- 所有接口返回值使用 `Result<T>` 包裹（[Result.java](visitor-backend/src/main/java/com/visitor/common/Result.java)）
- 成功：`Result.success(data)` → `{ code: 200, message: "success", data: ... }`
- 成功带消息：`Result.success(message)` → `{ code: 200, message: "...", data: null }`
- 失败：`Result.error(message)` → `{ code: 500, message: "...", data: null }`

**国际化消息规范（MessageSource）：**
- 资源文件：`messages.properties`（默认英文）、`messages_zh_CN.properties`（中文）、`messages_en_US.properties`（英文）
- 配置类：[I18nConfig.java](visitor-backend/src/main/java/com/visitor/config/I18nConfig.java) — 定义 `MessageSource` Bean 和 `AcceptHeaderLocaleResolver`
- 业务代码中禁止硬编码中文/英文消息字符串，必须通过 `messageSource.getMessage(key, args, LocaleContextHolder.getLocale())` 获取
- 带参数的消息使用 `MessageFormat` 占位符 `{0} {1}`，如 `msg("access.blacklist.alert", visitor.getName())`
- ServiceImpl 中封装 `msg()` 私有方法简化调用：
  ```java
  private String msg(String key, Object... args) {
      return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
  }
  ```

**SQL 安全规范：**
- Mapper XML 中所有参数占位符使用 `#{xxx}`，严禁 `${}` 拼接用户输入
- 排序字段如需动态传入，必须在 Service 层做白名单校验后才能使用 `${}`

**分页规范（PageHelper）：**
- Service 层调用 `PageHelper.startPage(page, pageSize)` 后紧跟 Mapper 查询，PageHelper 自动追加 LIMIT 并执行 COUNT
- 查询结果用 `PageInfo<>(list)` 包装，再将 `getList()` / `getTotal()` / `getPageNum()` / `getPageSize()` 转换为统一 `Map<String, Object>` 响应
- Mapper 接口和 XML 中不写 LIMIT/OFFSET，也不写单独的 count 查询

**包结构约定：**
```
com.visitor
  ├── annotation/     # 自定义注解（如 @RequirePermission）
  ├── common/         # 通用类（Result, GlobalExceptionHandler）
  ├── config/         # 配置类（WebMvc含CORS, JWT拦截器, Security/BCrypt）
  ├── controller/     # 控制器
  ├── dto/            # 数据传输对象
  ├── entity/         # 实体类（与数据库表一一对应）
  ├── mapper/         # MyBatis Mapper 接口
  ├── service/        # 业务接口
  │   └── impl/       # 业务实现
  └── utils/          # 工具类（JWT等）
```

**JWT 鉴权机制：**
- 登录接口 `POST /api/auth/login` 不在拦截范围内
- 其他 `/api/**` 请求由 [JwtInterceptor](visitor-backend/src/main/java/com/visitor/config/JwtInterceptor.java) 拦截
- 需要权限校验的方法上添加 `@RequirePermission("perm:code")` 注解
- Token 存储在 HTTP Header: `Authorization: Bearer <token>`
- Token claims 结构：`{ sub: userId, username, roleCode, permissions (逗号分隔) }`

### 2.2 前端规范

**目录结构：**
```
src/
  ├── api/           # API 请求模块（按业务模块拆分）
  │   ├── request.js     # Axios 实例 + 拦截器（含 Accept-Language）
  │   ├── auth.js        # 认证相关 API
  │   ├── visitor.js     # 访客相关 API
  │   ├── appointment.js # 预约相关 API
  │   ├── access-log.js  # 门禁记录 API
  │   └── statistics.js  # 数据统计 API
  ├── router/        # Vue Router 配置
  ├── store/         # Pinia 状态管理
  ├── locales/       # 语言包（zh.js / en.js）
  ├── i18n/          # vue-i18n 实例（index.js）
  ├── utils/         # 工具函数（reason.js 来访原因翻译映射）
  ├── views/         # 页面组件（9 个页面，全部已 $t() 国际化，无硬编码中文）
  │   ├── Login.vue              # 登录页
  │   ├── Dashboard.vue          # 管理端布局（侧边菜单 + 语言切换）
  │   ├── DashboardHome.vue      # 首页概览（统计卡片 + ECharts 图表）
  │   ├── VisitorList.vue        # 访客列表（分页 + 黑名单管理）
  │   ├── AppointmentForm.vue    # 在线预约申请
  │   ├── AppointmentQuery.vue   # 预约记录查询
  │   ├── AppointmentReview.vue  # 预约审批管理
  │   ├── AccessLog.vue          # 门禁管理（入校/离校登记 + 通行记录）
  │   └── NotFound.vue           # 404 页面
  └── components/    # 可复用组件
```

**API 请求规范：**
- 所有 API 调用必须通过 `src/api/` 下的模块导出，禁止在组件中直接调用 axios
- Axios 请求拦截器自动从 localStorage 读取 token 并附加到 Header
- Axios 请求拦截器根据 `localStorage.lang` 自动附加 `Accept-Language` 请求头（`zh` → `zh-CN`，`en` → `en-US`）
- Axios 响应拦截器统一处理 401（跳转登录）、403（权限不足提示）、500 等错误

**组件化规范：**
- 页面组件放在 `src/views/`，按业务模块划分子目录
- 可复用组件放在 `src/components/`
- 使用 Vue 3 Composition API（`<script setup>` 语法）
- 所有文本使用 `$t('key')` 调用 i18n 翻译，不做硬编码中文
- 表单验证规则的 message 需用 `computed(() => ({...}))` 包裹，以确保语言切换时规则响应式更新
- Element Plus 组件 locale 通过 `<el-config-provider :locale="...">` 动态切换

**i18n 语言切换：**
- 语言包位于 `src/locales/zh.js` 和 `src/locales/en.js`，约 180+ 翻译键，覆盖 common / login / appointment / query / status / error / nav / dashboard / visitor / review / access / notFound / reason 共 13 个模块
- i18n 实例在 `src/i18n/index.js`，默认语言从 `localStorage.lang` 读取，fallback 为 `zh`
- 页面右上角 `el-dropdown` 语言切换按钮（`trigger="click"`）调用 `changeLang(lang)`：写入 `localStorage` 并直接设置 `locale.value = lang` 实现响应式切换，无需刷新页面
- 路由 meta.title 使用 i18n key，`router.afterEach` 中通过 `i18n.global.t(key)` 动态设置 `document.title`
- 后端通过 `Accept-Language` 请求头接收语言偏好，`AcceptHeaderLocaleResolver` 自动解析为 `Locale`

**ECharts 图表国际化：**
- 图表通过 `setOption` 一次性渲染，语言切换不会自动重绘 —— 必须缓存数据到 ref，并用 `watch(locale)` 在语言变化时用 `t()` 重新调用渲染函数（参考 [DashboardHome.vue](visitor-frontend/src/views/DashboardHome.vue)）
- 图表内部所有文本（标题、图例、坐标轴、饼图中心文字）渲染时必须使用 `t()`，禁止硬编码

**来访原因（visit_reason）翻译规范：**
- `appointment.visit_reason` 数据库存英文原文（如 `Campus Tour`），不是 i18n 键，直接渲染不会随语言切换
- 前端统一通过 [reason.js](visitor-frontend/src/utils/reason.js) 的 `translateReason(name)` 翻译：`REASON_KEY_MAP` 映射数据库英文原文 → `reason.*` i18n 键，未知文本原样显示
- 使用位置：首页概览饼图（[DashboardHome.vue](visitor-frontend/src/views/DashboardHome.vue)）、审批页、查询页、门禁页关联预约表
- 新增已知来访原因时，必须同步更新 `REASON_KEY_MAP` + zh.js + en.js 三处

**手机号规范（支持国际号码）：**
- 前端预约表单校验正则：`/^\+?[\d(][\d\s\-()]{4,19}$/`（[AppointmentForm.vue](visitor-frontend/src/views/AppointmentForm.vue)），支持 `+1 2025550123`、`+1-202-555-0123`、`(202) 555-0123`、中国 11 位等格式
- 后端不做手机号格式校验（自由文本），管理员端搜索均为 `LIKE '%keyword%'` 模糊匹配，国际号码可直接搜索
- 数据库 `phone` 字段 VARCHAR(20)，可容纳 E.164 国际号码（最长 16 字符含 `+`）

**路由守卫：**
- 非 `noAuth` 标记的路由需要登录后才能访问，否则跳转 `/login`
- 已登录用户访问 `/login` 时自动重定向到 `/dashboard`

---

## 3. 核心实体对象映射

### 3.1 数据库表与实体类对照

| 表名 | 实体类 | 说明 |
|---|---|---|
| `sys_user` | `SysUser` | 系统用户 / 管理员 |
| `sys_role` | `SysRole` | 角色 |
| `sys_permission` | `SysPermission` | 权限（树形菜单+按钮） |
| `sys_role_permission` | （无实体） | 角色-权限关联表（仅 Mapper XML 中使用） |
| `visitor` | `Visitor` | 访客 |
| `appointment` | `Appointment` | 预约 |
| `access_log` | `AccessLog` | 门禁记录 |

### 3.2 sys_user（用户表）

| Java 字段 | 数据库列 | 类型 | 说明 |
|---|---|---|---|
| `id` | `id` | BIGINT PK | 自增主键 |
| `username` | `username` | VARCHAR(50) UNIQUE | 登录用户名 |
| `password` | `password` | VARCHAR(255) | BCrypt 加密 |
| `phone` | `phone` | VARCHAR(20) | 手机号 |
| `realName` | `real_name` | VARCHAR(50) | 真实姓名 |
| `roleId` | `role_id` | BIGINT FK→sys_role | 角色ID |
| `status` | `status` | TINYINT | 1启用 0禁用 |
| `createTime` | `create_time` | DATETIME | 创建时间 |
| `updateTime` | `update_time` | DATETIME | 更新时间 |

> 额外字段（仅 SQL 查询结果映射，非表字段）：`roleName`、`roleCode`

### 3.3 sys_role（角色表）

| Java 字段 | 数据库列 | 类型 | 说明 |
|---|---|---|---|
| `id` | `id` | BIGINT PK | 自增 |
| `roleName` | `role_name` | VARCHAR(50) | 角色名称 |
| `roleCode` | `role_code` | VARCHAR(50) UNIQUE | ROLE_ADMIN / ROLE_USER |
| `description` | `description` | VARCHAR(255) | 描述 |
| `createTime` | `create_time` | DATETIME | 创建时间 |

### 3.4 sys_permission（权限表）

| Java 字段 | 数据库列 | 类型 | 说明 |
|---|---|---|---|
| `id` | `id` | BIGINT PK | 自增 |
| `permName` | `perm_name` | VARCHAR(100) | 权限名称 |
| `permCode` | `perm_code` | VARCHAR(100) UNIQUE | 权限标识（如 `visitor:list`） |
| `permPath` | `perm_path` | VARCHAR(255) | 权限路径/URL |
| `parentId` | `parent_id` | BIGINT | 父权限ID（0=顶级菜单） |
| `permType` | `perm_type` | VARCHAR(20) | menu / button / api |
| `sortOrder` | `sort_order` | INT | 排序 |
| `icon` | `icon` | VARCHAR(100) | Element Plus 图标名 |
| `createTime` | `create_time` | DATETIME | 创建时间 |

### 3.5 visitor（访客表）

| Java 字段 | 数据库列 | 类型 | 说明 |
|---|---|---|---|
| `id` | `id` | BIGINT PK | 自增 |
| `name` | `name` | VARCHAR(50) | 姓名 |
| `phone` | `phone` | VARCHAR(20) | 手机号（支持国际号码，含 `+` 及空格/括号/连字符） |
| `idCard` | `id_card` | VARCHAR(18) | 身份证号 |
| `gender` | `gender` | TINYINT | 0未知 1男 2女 |
| `photoUrl` | `photo_url` | VARCHAR(255) | 照片路径 |
| `status` | `status` | TINYINT | 1正常 0黑名单 |
| `createTime` | `create_time` | DATETIME | 创建时间 |
| `updateTime` | `update_time` | DATETIME | 更新时间 |

### 3.6 appointment（预约表）

| Java 字段 | 数据库列 | 类型 | 说明 |
|---|---|---|---|
| `id` | `id` | BIGINT PK | 自增 |
| `visitorId` | `visitor_id` | BIGINT FK→visitor | 关联访客 |
| `appointmentTime` | `appointment_time` | DATETIME | 预约访问时间 |
| `visitReason` | `visit_reason` | VARCHAR(255) | 来访原因（存英文原文，前端经 `translateReason()` 翻译显示） |
| `hostName` | `host_name` | VARCHAR(50) | 被访人姓名 |
| `hostDept` | `host_dept` | VARCHAR(100) | 被访部门 |
| `status` | `status` | TINYINT | 0待审核 1已通过 2已拒绝 3已完成 4已取消 |
| `rejectReason` | `reject_reason` | VARCHAR(255) | 拒绝原因 |
| `reviewerId` | `reviewer_id` | BIGINT FK→sys_user | 审核人 |
| `reviewTime` | `review_time` | DATETIME | 审核时间 |
| `createTime` | `create_time` | DATETIME | 创建时间 |
| `updateTime` | `update_time` | DATETIME | 更新时间 |

### 3.7 access_log（门禁记录表）

| Java 字段 | 数据库列 | 类型 | 说明 |
|---|---|---|---|
| `id` | `id` | BIGINT PK | 自增 |
| `visitorId` | `visitor_id` | BIGINT FK→visitor | 关联访客 |
| `appointmentId` | `appointment_id` | BIGINT FK→appointment | 关联预约 |
| `entryTime` | `entry_time` | DATETIME | 进入时间 |
| `exitTime` | `exit_time` | DATETIME | 离开时间 |
| `accessStatus` | `access_status` | TINYINT | 1通行成功 0通行失败 |
| `deviceName` | `device_name` | VARCHAR(100) | 设备名称 |
| `failReason` | `fail_reason` | VARCHAR(255) | 失败原因 |
| `createTime` | `create_time` | DATETIME | 创建时间 |

---

## 4. E-R 关系

```
sys_role ──1:N── sys_user
    │
    N:N (via sys_role_permission)
    │
sys_permission (自引用 parent_id 树形)

visitor ──1:N── appointment ──1:N── access_log
```

---

## 开发进度清单

| 模块 | 状态 | 说明 |
|---|---|---|
| 项目骨架初始化 | 已完成 | SpringBoot 2.7.18 + Vue 3 + Vite |
| 数据库表设计 (schema.sql) | 已完成 | 7 张表 + RBAC 种子数据 |
| JWT 认证 + RBAC 鉴权 | 已完成 | 登录/Token/拦截器/@RequirePermission |
| 访客管理 CRUD | 已完成 | Visitor 实体 + Mapper + Service + 前端 API |
| 访客管理前端页面 | 已完成 | 访客列表（分页+黑名单管理） |
| 预约模块 | 已完成 | Appointment 实体/Mapper/Service + 在线预约页 + 查询页 + 审批页 |
| 门禁记录模块 | 已完成 | 入校/离校登记 + 黑名单告警 + 超时告警 + 通行记录分页 |
| ECharts 统计看板 | 已完成 | 柱状图（日/周/月流量）+ 折线图（分时段趋势）+ 饼图（来访原因占比） |
| PageHelper 分页改造 | 已完成 | Visitor / Appointment / AccessLog 三个模块全部迁移 |
| CORS 跨域配置 | 已完成 | allowedOriginPatterns 通配符（localhost:* + *.trycloudflare.com） |
| 数据库索引优化 | 已完成 | visitor.idx_name、appointment.idx_status_create、access_log.idx_entry_exit |
| 中英文双语切换 | 已完成 | 前端 9 个页面全部 `$t()` 化（180+ 翻译键，13 个模块） + Element Plus locale 响应式切换 + 后端 MessageSource（26 条消息） + Accept-Language 请求头 |
| 首页概览图表国际化修复 | 已完成 | DashboardHome.vue 全部 `$t()` 化 + `watch(locale)` 图表重绘 + 后端周标签 `statistics.week` 消息键 |
| 来访原因多语言映射 | 已完成 | `src/utils/reason.js` 的 `translateReason()` 映射数据库英文原文 → `reason.*` 翻译键（20 种原因），饼图 + 3 个表格统一翻译 |
| 国际手机号支持 | 已完成 | 预约表单正则放开为 `/^\+?[\d(][\d\s\-()]{4,19}$/`，后端无格式校验，管理员端模糊搜索国际号码 |
| MySQL 连接配置 | 已完成 | JDBC URL 添加 `allowPublicKeyRetrieval=true` 解决 MySQL 8.0+ 公钥检索限制 |
| 使用教程 | 已完成 | [USAGE.md](USAGE.md) — 覆盖全部 9 个页面和 3 个用户工作流的详细操作指南 |
| 用户管理页面 | 未完成 | 管理员列表、新增/编辑/删除 |
| 角色管理页面 | 未完成 | 角色列表 |
