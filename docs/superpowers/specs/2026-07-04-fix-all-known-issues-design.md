# 修复全部已知问题 — 设计文档

> 基于 CLAUDE.md 第 9 节记录的 32 个已知问题，按成员模块边界，分 6 个阶段全面修复。
> 日期：2026-07-04

---

## 一、总体策略

**执行顺序：** 后端基础 → 后端业务 → 后端架构 → 小程序 → 管理后台 → 测试

**成员边界原则：** 每次修改只影响对应成员的模块文件，共享基础设施（`common/`）独立于成员归属。

---

## 二、阶段①：后端基础修复（3 个问题）

### 2.1 全局异常处理器

**新增文件：** `common/GlobalExceptionHandler.java`

- `@RestControllerAdvice` 注解
- 捕获 `MethodArgumentNotValidException` → 400，返回校验失败字段列表
- 捕获 `RuntimeException` → 500，返回通用错误信息
- 捕获 `Exception` → 500，兜底处理
- 统一返回 `Result` 格式

### 2.2 输入校验

**修改文件：** 所有 Controller 类

- 所有 `@RequestBody` 参数加 `@Valid`
- 关键 Entity/DTO 字段加 Bean Validation 注解（`@NotBlank`, `@NotNull`, `@Min`, `@Max`, `@Size`, `@Pattern`）

### 2.3 配置外部化

**修改文件：** `application.yml`

```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:123456}  # 环境变量，本地开发用默认值

wechat:
  miniapp:
    appid: ${WECHAT_APPID:your-appid-here}
    secret: ${WECHAT_SECRET:your-secret-here}
```

---

## 三、阶段②：后端业务逻辑修复（5 个问题）

### 3.1 关键词过滤计数修复

**修改文件：** `AdminServiceImpl.java`

- `getFavoriteCount(String keyword)`：构建 `LambdaQueryWrapper<Favorite>`，当 keyword 不为空时加 `like(Favorite::getUserId, keyword)`。userId 是 Long 类型，改用 `eq` 或转字符串后 `like`。
- `getHistoryCount(String keyword)`：同上，对 `SelectionHistory` 表操作。
- `getUserCount` 中 `like(User::getUserId, keyword)` bug：Long 类型不支持 `like`，改为 `eq` 精确匹配或改为按 username 模糊搜索。

### 3.2 删除用户级联清理

**修改文件：** `AdminServiceImpl.java` — `deleteUser(Long id)`

操作顺序：
1. 删除 `favorite` 表中 `user_id = id` 的记录
2. 删除 `selection_history` 表中 `user_id = id` 的记录
3. 删除 `user` 表中 `user_id = id` 的记录

使用 `@Transactional` 保证原子性。

### 3.3 删除商户级联清理

**修改文件：** `AdminServiceImpl.java` — `deleteMerchant(Long id)`

操作顺序：
1. 查询该商户所有档口 ID
2. 删除这些档口下的所有菜品
3. 删除这些档口
4. 删除商户

使用 `@Transactional` 保证原子性。

### 3.4 真实数据库备份

**修改文件：** `AdminServiceImpl.java` — 备份相关方法

- 新增 `backup/` 目录（在项目根目录或用户目录）
- `createBackup()` 使用 `ProcessBuilder` 执行 `mysqldump` 命令
- 文件名：`backup_yyyyMMdd_HHmmss.sql`
- `restoreBackup(Long id)` 使用 `mysql` 命令导入
- `getBackupList()` 从文件系统读取
- `deleteBackup(Long id)` 删除对应文件
- 错误处理：mysqldump 不可用时返回明确错误信息

### 3.5 N+1 查询优化

**修改文件：** `RecommendServiceImpl.java`, `DishMapper.java`

- `DishMapper` 新增 `selectBatchWithStall(@Param("dishIds") List<Long> dishIds)` 批量 JOIN 查询
- Service 层用 Stream 或循环构建 `Map<Long, Stall>` 缓存
- `getGuessLike` 不再逐条查档口信息

---

## 四、阶段③：后端架构升级（2 个问题）

### 4.1 JWT + Spring Security

**新增文件：**

| 文件 | 归属 | 说明 |
|---|---|---|
| `common/JwtUtils.java` | 共享 | JWT 生成/解析/校验，HS256 签名，7 天有效期 |
| `common/JwtAuthenticationFilter.java` | 共享 | `OncePerRequestFilter`，从 Authorization 头提取 token |
| `common/SecurityConfig.java` | 共享 | SecurityFilterChain 配置，放行登录接口 |

**修改文件：**

| 文件 | 变动 |
|---|---|
| `pom.xml` | 添加 `spring-boot-starter-security`、`jjwt-api`、`jjwt-impl`、`jjwt-jackson` |
| `AuthServiceImpl.java` | 登录成功返回 JWT token 替代当前简单 token |
| `common/SpaRouterConfig.java` | 确保 SPA 路由回退与 Security 共存 |

**Security 规则：**
- `POST /api/miniapp/login` → 放行
- `GET /api/admin/dishes` → 放行（小程序首页浏览不需要登录）
- `GET /api/miniapp/recommend/**` → 放行（随机推荐、好评榜不需要登录）
- 其余 `/api/miniapp/**` → 需要认证
- 其余 `/api/admin/**` → 需要认证
- 无状态 Session，关闭 CSRF

### 4.2 DTO 层

**按成员分包：**

```
# 成员 A — 认证与互动
controller/miniapp/dto/
├── LoginRequest.java
├── LoginResponse.java
├── ProfileUpdateRequest.java
├── ScoreRequest.java
├── FavoriteRequest.java

# 成员 B — 推荐（如需要可后续添加）
# controller/miniapp/dto/
# （RecommendController 目前返回 Dish 实体，暂不需 DTO）

# 成员 C — 商户后台
controller/admin/dto/
├── StallSaveRequest.java
├── DishSaveRequest.java

# 成员 D — 管理员后台
controller/admin/dto/
├── DashboardVO.java
├── TrendVO.java
├── ScoreDistributionVO.java
├── PageResult.java
```

**映射规则：**
- Controller 层接收/返回 DTO
- DTO ↔ Entity 手动转换（在 Controller 私有方法中）
- Service 层接口不变

---

## 五、阶段④：小程序修复（9 个问题）

### 成员 A 模块（登录态 + mine 页面）

| # | 问题 | 文件 | 修复 |
|---|---|---|---|
| 1 | userId 默认 1 | `app.js` | `userId` 默认改为 `null`；`clearLoginState` 同步 |
| 2 | 请求无 token | `utils/api.js` | `request()` 加 `Authorization: Bearer <token>` 头；401 时跳转登录 |
| 5 | toggleLike 无调用 | `pages/mine/mine.js` | 历史记录列表添加点赞/取消按钮 |
| 6 | getProfile 无调用 | `pages/mine/mine.js` | `onShow` 调用 `getProfile` 刷新用户信息 |
| 7 | 无默认头像 | `pages/mine/mine.wxml` | `<image>` 添加 `mode="aspectFill"` + CSS 兜底背景色 |

### 成员 B 模块（推荐 + 浏览页面）

| # | 问题 | 文件 | 修复 |
|---|---|---|---|
| 3 | 详情回退低效 | `pages/dish-detail/dish-detail.js` | 优先 dishCache，未命中时请求带 dishId 参数的精确查询 |
| 4 | 半星渲染 bug | `pages/today-praise/today-praise.js` | 修正星级计算：`fullStars = Math.floor(score)`，`halfStar = (score - fullStars) >= 0.5` |
| 8 | 下拉刷新未启用 | `pages/index/index.json` + `.js` | 添加 `enablePullDownRefresh: true`；`onPullDownRefresh` 重置数据 |
| 9 | 3 页面无分页 | `guess-like`, `today-praise`, `mine` | 各页面 `onReachBottom` 追加加载，pageSize=10 |

---

## 六、阶段⑤：管理后台修复（11 个问题）

### 成员 C 模块

| # | 问题 | 文件 | 修复 |
|---|---|---|---|
| 7 | 统计只算当前页 | `DishAudit.vue` | 后端新增 `GET /api/admin/dishes/stats` 端点返回全局统计（总数/分类数/均价）；前端调用此端点 |
| 8 | 档口筛选硬编码 | `StallOverview.vue` | 从 `getMerchants()` API 动态获取商户列表 |

### 成员 D 模块

| # | 问题 | 文件 | 修复 |
|---|---|---|---|
| 1 | 导出未实现 | `DataQuery.vue` | 4 个 Tab 各加导出按钮；后端新增 `GET /api/admin/export/{type}` 返回 CSV 文件流 |
| 2 | 备份设置不持久化 | `DatabaseMaintenance.vue` | 自动备份设置（开关/频率）存入 `localStorage` |
| 3 | 备份下载未实现 | `DatabaseMaintenance.vue` | 后端新增 `GET /api/admin/backups/{id}/download`；前端触发下载 |
| 4 | 顶栏菜单无事件 | `AdminLayout.vue` | "个人信息"→弹窗、"设置"→跳转、"退出"→清除 token + 刷新 |
| 5 | 通知数字硬编码 | `AdminLayout.vue` | 调用 `GET /api/admin/notifications/count` 获取待审核菜品数和备份总数，动态显示 |
| 6 | 菜品无审核功能 | `DishAudit.vue` | 每卡片加审核按钮（通过/拒绝）；状态用 StatusTag 展示（pending=黄色"待审核"、approved=绿色"已通过"、rejected=红色"已拒绝"）；默认筛选"待审核" |

**Dish 表需新增字段：** `status VARCHAR(20) DEFAULT 'approved'`

### 清理

| # | 问题 | 文件 | 修复 |
|---|---|---|---|
| 9 | Pinia 无 Store | `main.js` | 保持 Pinia 安装，不强制创建空 Store |
| 10 | 死代码 | `DishOverview.vue` | 删除 |
| 11 | 死代码 | `api/mock.js` | 删除 |

---

## 七、阶段⑥：测试补齐（1 个问题）

### 单元测试

**新增依赖：** `spring-boot-starter-test`（已存在）、H2 内存数据库（测试用）

**测试文件（按成员模块）：**

```
# 成员 A
src/test/java/com/foodrec/admin/service/miniapp/
├── AuthServiceImplTest.java
└── InteractionServiceImplTest.java

# 成员 B
src/test/java/com/foodrec/admin/service/miniapp/
└── RecommendServiceImplTest.java

# 成员 C
src/test/java/com/foodrec/admin/service/admin/
└── MerchantServiceImplTest.java

# 成员 D
src/test/java/com/foodrec/admin/service/admin/
└── AdminServiceImplTest.java
```

- 使用 `@SpringBootTest` + `@Transactional`（测试后回滚）
- 每个 Service 至少覆盖核心方法的正常路径和边界条件
- Mock 微信 API 调用

---

## 八、跨阶段依赖的后端变更

部分管理后台前端修复需要后端新增端点，这些端点在阶段⑤开始前完成：

| 前端修复 | 所需后端变更 | 归属 |
|---|---|---|
| 导出功能 | 新增 `GET /api/admin/export/users`、`/dishes`、`/favorites`、`/histories`，返回 CSV 文件流 | 成员 D |
| 备份下载 | 新增 `GET /api/admin/backups/{id}/download`，返回 SQL 文件流 | 成员 D |
| 通知铃铛 | 新增 `GET /api/admin/notifications/count`，返回 `{ pendingDishes: N, totalBackups: N }` | 成员 D |
| 菜品审核 | Dish 表加 `status VARCHAR(20) DEFAULT 'approved'`；新增 `PUT /api/admin/dishes/{id}/status` 端点（body: `{ status: "approved" | "rejected" | "pending" }`） | 成员 C |
| 菜品统计 | 新增 `GET /api/admin/dishes/stats`，返回 `{ total, categoryCount, avgPrice }`（全局统计，非当前页） | 成员 C |

## 九、文件变更汇总

| 阶段 | 新增文件 | 修改文件 | 删除文件 |
|---|---|---|---|
| ① | 1 | 7+ | 0 |
| ② | 0 | 3 | 0 |
| ③ | 3 | 3 | 0 |
| ④ | 0 | 9 | 0 |
| ⑤（含后端补充） | 5 (后端端点) | 8 | 2 |
| ⑥ | 4 | 1 (pom.xml) | 0 |
| **合计** | **13** | **31** | **2** |

---

## 十、风险与注意事项

1. **Spring Security 引入**可能影响管理后台前端的请求（需确保 Authorization 头正确传递）
2. **数据库备份**依赖系统安装的 `mysqldump` 命令，需在配置中校验路径
3. **Dish 表新增 status 字段**需要执行 SQL 迁移
4. **JWT token** 需要小程序端存储并在每次请求中携带
5. 所有修改保持向后兼容，不破坏现有 API 的调用约定
