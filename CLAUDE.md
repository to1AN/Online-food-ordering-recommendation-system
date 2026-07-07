# 网上点餐推荐系统 — 完整项目文档

> 本文档为项目的完整技术描述，涵盖架构、数据库、三端功能、API、成员分工及已知问题。
> 最后更新：2026-07-04

---

## 一、项目概览

一个**在线点餐智能推荐平台**，包含三端：

| 端 | 目录 | 技术 | 说明 |
|---|---|---|---|
| 微信小程序 | `miniapp/` | 微信原生开发 | 普通用户端（C端） |
| 管理后台前端 | `admin-panel/` | Vue 3 + Element Plus | 管理员/商户 Web 端 |
| 后端服务 | `admin-panel/server-springboot/` | Spring Boot 3.2.6 + MyBatis-Plus | 统一后端，同时服务小程序和管理后台 |

**端口约定：** 后端 `localhost:9999`，管理后台前端 dev `localhost:5173`（Vite 代理 `/api` → `localhost:9999`）。

---

## 二、技术栈

| 层级 | 技术 | 版本 |
|---|---|---|
| 小程序 | 微信原生（WXML/WXSS/JS） | 基础库 3.16.1 |
| 管理后台前端 | Vue 3 (Composition API `<script setup>`) | 3.5 |
| UI 库 | Element Plus (中文) | 2.14 |
| 图表 | ECharts | 6 |
| 路由 | Vue Router 4（Hash 模式） | — |
| 状态管理 | Pinia 3（已安装，未使用） | — |
| HTTP 客户端 | Axios | 1.17 |
| 构建工具 | Vite | 8 |
| 后端框架 | Spring Boot | 3.2.6 |
| ORM | MyBatis-Plus（分页插件） | 3.5.7 |
| 数据库 | MySQL | 8.0 |
| JDK | OpenJDK | 21 |
| 构建 | Maven | ≥ 3.8 |

---

## 三、项目目录结构

```
项目根目录/
├── CLAUDE.md                          # 本文档
├── API.md                             # API 接口文档
├── README.md                          # 项目说明
├── .gitignore
│
├── admin-panel/                       # 管理后台
│   ├── index.html                     # SPA 入口
│   ├── package.json                   # 前端依赖
│   ├── vite.config.js                 # Vite 配置（代理 /api → localhost:9999）
│   ├── public/
│   │   ├── favicon.svg
│   │   └── icons.svg
│   ├── src/
│   │   ├── main.js                    # Vue 入口（注册 Element Plus / Pinia / Router）
│   │   ├── App.vue                    # 根组件（<router-view />）
│   │   ├── api/
│   │   │   ├── index.js               # Axios 实例 + 所有 API 方法（30+ 端点）
│   │   │   └── mock.js                # Mock 数据（未使用，死代码）
│   │   ├── assets/                    # 静态资源
│   │   ├── components/                # 7 个公共组件
│   │   │   ├── ConfirmAction.vue      # 删除确认气泡
│   │   │   ├── DataTable.vue          # 表格 + 分页
│   │   │   ├── FormDialog.vue         # 新增/编辑弹窗
│   │   │   ├── PageHeader.vue         # 面包屑 + 标题
│   │   │   ├── SearchBar.vue          # 搜索/筛选栏
│   │   │   ├── StatCard.vue           # 统计卡片
│   │   │   └── StatusTag.vue          # 状态标签
│   │   ├── layout/
│   │   │   └── AdminLayout.vue        # 侧边栏 + 顶栏 + 内容区布局
│   │   ├── router/
│   │   │   └── index.js               # 7 个路由（懒加载）
│   │   └── views/
│   │       ├── Dashboard.vue          # 数据总览
│   │       ├── UserManagement.vue     # 用户管理
│   │       ├── MerchantManagement.vue # 商户管理
│   │       ├── StallOverview.vue      # 档口信息总览
│   │       ├── DishAudit.vue          # 菜品信息总览
│   │       ├── DataQuery.vue          # 全局数据查询
│   │       ├── DatabaseMaintenance.vue # 数据库维护
│   │       └── DishOverview.vue       # [死代码] 旧版菜品页面，未被路由引用
│   │
│   └── server-springboot/             # Spring Boot 后端
│       ├── pom.xml
│       └── src/main/
│           ├── java/com/foodrec/admin/
│           │   ├── AdminApplication.java
│           │   ├── common/
│           │   │   ├── MyBatisPlusConfig.java    # 分页插件
│           │   │   ├── Result.java               # 统一响应体
│           │   │   └── SpaRouterConfig.java      # SPA 路由回退
│           │   ├── controller/
│           │   │   ├── admin/
│           │   │   │   ├── AdminController.java   # 仪表盘/用户/商户/收藏/历史/备份
│           │   │   │   └── MerchantController.java # 档口/菜品 CRUD
│           │   │   └── miniapp/
│           │   │       ├── AuthController.java    # 登录/登出/个人信息
│           │   │       ├── InteractionController.java # 收藏/评分/点赞/历史
│           │   │       └── RecommendController.java   # 随机/好评榜/猜你喜欢
│           │   ├── entity/             # 6 个实体类
│           │   │   ├── User.java
│           │   │   ├── Merchant.java
│           │   │   ├── Stall.java
│           │   │   ├── Dish.java
│           │   │   ├── Favorite.java
│           │   │   └── SelectionHistory.java
│           │   ├── mapper/             # 6 个 Mapper 接口
│           │   │   ├── UserMapper.java
│           │   │   ├── MerchantMapper.java
│           │   │   ├── StallMapper.java
│           │   │   ├── DishMapper.java
│           │   │   ├── FavoriteMapper.java
│           │   │   └── SelectionHistoryMapper.java
│           │   └── service/
│           │       ├── admin/
│           │       │   ├── AdminService.java (接口)
│           │       │   ├── AdminServiceImpl.java
│           │       │   ├── MerchantService.java (接口)
│           │       │   └── MerchantServiceImpl.java
│           │       └── miniapp/
│           │           ├── AuthService.java (接口)
│           │           ├── AuthServiceImpl.java
│           │           ├── InteractionService.java (接口)
│           │           ├── InteractionServiceImpl.java
│           │           ├── RecommendService.java (接口)
│           │           └── RecommendServiceImpl.java
│           └── resources/
│               ├── application.yml
│               └── static/            # 预构建的 Vue SPA 静态文件
│
└── miniapp/                           # 微信小程序
    ├── app.js                         # 应用入口（登录状态管理）
    ├── app.json                       # 页面注册 + TabBar 配置
    ├── app.wxss                       # 全局样式（暖橙主题）
    ├── project.config.json            # 项目配置
    ├── utils/
    │   ├── config.js                  # BASE_URL、分类列表等常量
    │   └── api.js                     # HTTP 请求封装 + 所有 API 方法
    └── pages/
        ├── index/                     # 首页（菜品浏览）
        ├── random/                    # 随机选餐（跑马灯）
        ├── guess-like/                # 猜你喜欢
        ├── today-praise/              # 今日好评榜
        ├── dish-detail/               # 菜品详情
        └── mine/                      # 个人中心（登录/收藏/历史/评分）
```

---

## 四、数据库设计

### 4.1 ER 实体关系

```
Merchant (1) ──拥有──> (N) Stall (1) ──提供──> (N) Dish
                                                        │
User (1) ──产生──> (N) Selection_History (N) <──关联── (N) Dish
User (1) ──收藏──> (N) Favorite (N) <──被收藏── (N) Dish
```

- 商家与档口：一对多（一个商家管理多个档口）
- 档口与菜品：一对多（一个档口提供多个菜品）
- 用户与菜品：通过「选餐历史」和「收藏」建立多对多关系

### 4.2 六张数据表

#### User 表（用户）

| 字段名 | 数据类型 | 约束 | 说明 |
|---|---|---|---|
| user_id | BIGINT | PK, 自增 | 用户编号 |
| username | VARCHAR(50) | NOT NULL | 用户名 |
| password | VARCHAR(100) | NOT NULL | 登录密码（微信登录时为空串） |
| avatar | VARCHAR(255) | NULL | 用户头像 URL |
| openid | VARCHAR(100) | NULL | 微信 openid（用于小程序登录） |
| register_time | TIMESTAMP | — | 注册时间 |

#### Merchant 表（商户）

| 字段名 | 数据类型 | 约束 | 说明 |
|---|---|---|---|
| merchant_id | BIGINT | PK, 自增 | 商户编号 |
| merchant_name | VARCHAR(100) | NOT NULL | 商户名称 |
| contact_info | VARCHAR(50) | NOT NULL | 联系方式 |
| create_time | DATE | — | 创建时间 |

#### Stall 表（档口）

| 字段名 | 数据类型 | 约束 | 说明 |
|---|---|---|---|
| stall_id | BIGINT | PK, 自增 | 档口编号 |
| stall_name | VARCHAR(100) | NOT NULL | 档口名称 |
| location | VARCHAR(100) | NOT NULL | 所在位置 |
| merchant_id | BIGINT | FK → Merchant | 所属商户 |

#### Dish 表（菜品）

| 字段名 | 数据类型 | 约束 | 说明 |
|---|---|---|---|
| dish_id | BIGINT | PK, 自增 | 菜品编号 |
| dish_name | VARCHAR(100) | NOT NULL | 菜品名称 |
| price | DECIMAL(8,2) | NOT NULL | 菜品价格 |
| category | VARCHAR(50) | NOT NULL | 菜品分类（川菜/粤菜/面食等 12 类） |
| description | TEXT | NULL | 菜品描述 |
| image_url | VARCHAR(255) | NULL | 菜品图片 URL |
| stall_id | BIGINT | FK → Stall | 所属档口 |

#### Selection_History 表（选餐历史）

| 字段名 | 数据类型 | 约束 | 说明 |
|---|---|---|---|
| history_id | BIGINT | PK, 自增 | 历史记录编号 |
| user_id | BIGINT | FK → User | 用户编号 |
| dish_id | BIGINT | FK → Dish | 菜品编号 |
| score | TINYINT | NULL | 用户评分（1~5） |
| like_status | BOOLEAN | NULL | 点赞状态 |
| select_time | TIMESTAMP | NOT NULL | 选餐时间 |

#### Favorite 表（收藏记录）

| 字段名 | 数据类型 | 约束 | 说明 |
|---|---|---|---|
| favorite_id | BIGINT | PK, 自增 | 收藏编号 |
| user_id | BIGINT | FK → User | 用户编号 |
| dish_id | BIGINT | FK → Dish | 菜品编号 |
| favorite_time | TIMESTAMP | NOT NULL | 收藏时间 |

### 4.3 菜品分类枚举（12 类）

川菜、粤菜、湘菜、鲁菜、苏菜、浙菜、闽菜、徽菜、面食、铁板烧、日料、烧烤、小吃、饮品

> 注：前端 `config.js` 定义了 12 个分类，与后端数据库 `category` 字段对应。

---

## 五、成员分工与模块边界

### 成员 A — 用户认证与互动模块

**职责：** 用户注册登录、个人信息维护、收藏管理、评分点赞、选餐历史

**涉及数据库表：** User, Favorite, SelectionHistory

#### 后端文件

| 文件 | 说明 |
|---|---|
| `controller/miniapp/AuthController.java` | 登录/登出/个人信息（4 个端点） |
| `controller/miniapp/InteractionController.java` | 收藏/评分/点赞/历史（6 个端点） |
| `service/miniapp/AuthService.java` | 认证接口 |
| `service/miniapp/AuthServiceImpl.java` | 微信登录（jscode2session + 自动注册 + Token 生成） |
| `service/miniapp/InteractionService.java` | 互动接口 |
| `service/miniapp/InteractionServiceImpl.java` | 收藏/评分/点赞/历史业务逻辑 |
| `entity/User.java` | 用户实体 |
| `entity/Favorite.java` | 收藏实体 |
| `entity/SelectionHistory.java` | 选餐历史实体 |
| `mapper/UserMapper.java` | 用户数据访问 |
| `mapper/FavoriteMapper.java` | 收藏数据访问（含自定义联表查询） |
| `mapper/SelectionHistoryMapper.java` | 历史数据访问（含自定义联表查询） |

#### 小程序文件

| 文件 | 说明 |
|---|---|
| `pages/mine/*` | 个人中心页面（登录/登出/昵称编辑/收藏列表/历史列表/评分列表） |
| `app.js` | 登录状态管理（setLoginState / clearLoginState） |
| `utils/api.js` | login / logout / getProfile / updateProfile 等 API |

#### API 端点（10 个）

| 方法 | 路径 | 功能 |
|---|---|---|
| POST | `/api/miniapp/login` | 微信登录（自动注册） |
| POST | `/api/miniapp/logout` | 退出登录 |
| GET | `/api/miniapp/profile` | 获取个人信息 |
| PUT | `/api/miniapp/profile` | 更新个人信息 |
| POST | `/api/miniapp/favorite` | 添加收藏 |
| DELETE | `/api/miniapp/favorite` | 取消收藏 |
| GET | `/api/miniapp/favorites` | 收藏列表 |
| POST | `/api/miniapp/score` | 评分菜品（1-5） |
| POST | `/api/miniapp/like` | 点赞/取消 |
| GET | `/api/miniapp/history` | 选餐历史 |

---

### 成员 B — 选餐与个性化推荐模块

**职责：** 首页菜品浏览、随机选餐、猜你喜欢、今日好评榜、菜品详情

**涉及数据库表：** Dish, SelectionHistory, Favorite

#### 后端文件

| 文件 | 说明 |
|---|---|
| `controller/miniapp/RecommendController.java` | 推荐相关（3 个端点） |
| `service/miniapp/RecommendService.java` | 推荐接口 |
| `service/miniapp/RecommendServiceImpl.java` | 随机/好评榜/猜你喜欢算法 |
| `entity/Dish.java` | 菜品实体 |
| `mapper/DishMapper.java` | 菜品数据访问（含随机查询、好评榜聚合、联表分页） |

#### 小程序文件

| 文件 | 说明 |
|---|---|
| `pages/index/*` | 首页（搜索/分类筛选/Banner/分页菜品网格） |
| `pages/random/*` | 随机选餐（跑马灯老虎机效果） |
| `pages/guess-like/*` | 猜你喜欢（基于偏好分类推荐） |
| `pages/today-praise/*` | 今日好评榜（TOP10） |
| `pages/dish-detail/*` | 菜品详情（图片/价格/描述/收藏/选餐） |
| `utils/api.js` | getRandomDish / getTodayPraise / getGuessLike 等 API |
| `utils/config.js` | CATEGORIES 分类列表、MARQUEE_CONFIG 跑马灯配置 |

#### API 端点（3 个 + 1 个复用）

| 方法 | 路径 | 功能 |
|---|---|---|
| GET | `/api/miniapp/recommend/random` | 随机推荐一道菜 |
| GET | `/api/miniapp/recommend/today-praise` | 今日好评榜 TOP10 |
| GET | `/api/miniapp/recommend/guess-like` | 猜你喜欢（需 userId） |
| GET | `/api/admin/dishes` | 菜品列表（首页和详情页复用管理端接口） |

#### 推荐算法详解

**猜你喜欢（基于内容的混合推荐）：**
1. 查询用户近期评分 ≥ 3 的选餐记录，提取偏好分类
2. 查询用户收藏菜品的分类，合并到偏好列表
3. 在每个偏好分类中，排除已尝试的菜品，推荐未试过的新菜品
4. 每个分类最多推荐 4 道

**今日好评榜：**
- 当天评分 ≥ 4 的菜品，按平均分 DESC、好评数 DESC 排序，取 TOP 10

**随机推荐：**
- `SELECT * FROM dish ORDER BY RAND() LIMIT 1`

---

### 成员 C — 商户后台模块

**职责：** 档口管理、菜品管理、经营数据查询

**涉及数据库表：** Merchant, Stall, Dish

#### 后端文件

| 文件 | 说明 |
|---|---|
| `controller/admin/MerchantController.java` | 档口/菜品 CRUD（10 个端点） |
| `service/admin/MerchantService.java` | 商户服务接口 |
| `service/admin/MerchantServiceImpl.java` | 档口/菜品业务逻辑 |
| `entity/Merchant.java` | 商户实体 |
| `entity/Stall.java` | 档口实体 |
| `entity/Dish.java` | 菜品实体 |
| `mapper/MerchantMapper.java` | 商户数据访问 |
| `mapper/StallMapper.java` | 档口数据访问（含联表查询） |
| `mapper/DishMapper.java` | 菜品数据访问 |

#### 管理后台 Vue 文件

| 文件 | 说明 |
|---|---|
| `views/MerchantManagement.vue` | 商户 CRUD（列表/新增/编辑/删除） |
| `views/StallOverview.vue` | 档口 CRUD（列表/新增/编辑/详情/删除） |
| `views/DishAudit.vue` | 菜品总览（只读卡片网格） |

#### API 端点（10 个）

| 方法 | 路径 | 功能 |
|---|---|---|
| GET | `/api/admin/stalls` | 档口列表（分页） |
| POST | `/api/admin/stalls` | 新增档口 |
| PUT | `/api/admin/stalls/{id}` | 编辑档口 |
| DELETE | `/api/admin/stalls/{id}` | 删除档口（级联删除菜品） |
| GET | `/api/admin/stalls/{id}/dishes` | 档口下菜品列表 |
| GET | `/api/admin/dishes` | 菜品列表（分页+分类筛选） |
| GET | `/api/admin/dishes/{id}` | 菜品详情 |
| POST | `/api/admin/dishes` | 新增菜品 |
| PUT | `/api/admin/dishes/{id}` | 编辑菜品 |
| DELETE | `/api/admin/dishes/{id}` | 删除菜品 |

---

### 成员 D — 管理员后台模块

**职责：** 用户管理、商户管理、数据查询、数据库备份与恢复

**涉及数据库表：** User, Merchant, Stall, Dish, Favorite, SelectionHistory（全部 6 张表）

#### 后端文件

| 文件 | 说明 |
|---|---|
| `controller/admin/AdminController.java` | 仪表盘/用户/商户/收藏/历史/备份（16 个端点） |
| `service/admin/AdminService.java` | 管理服务接口 |
| `service/admin/AdminServiceImpl.java` | 管理业务逻辑 |
| `common/Result.java` | 统一响应体 |
| `common/MyBatisPlusConfig.java` | 分页插件配置 |
| `common/SpaRouterConfig.java` | SPA 路由回退配置 |

#### 管理后台 Vue 文件

| 文件 | 说明 |
|---|---|
| `layout/AdminLayout.vue` | 整体布局（侧边栏+顶栏） |
| `views/Dashboard.vue` | 数据总览（6 项统计+4 个图表） |
| `views/UserManagement.vue` | 用户管理（CRUD+批量删除） |
| `views/DataQuery.vue` | 全局数据查询（4 Tab） |
| `views/DatabaseMaintenance.vue` | 数据库维护（备份/恢复/日志） |
| `components/*` | 7 个公共组件 |
| `api/index.js` | Axios 实例 + 所有 API 方法 |
| `router/index.js` | 路由配置 |

#### API 端点（16 个）

| 方法 | 路径 | 功能 |
|---|---|---|
| GET | `/api/admin/dashboard` | 仪表盘统计 |
| GET | `/api/admin/dashboard/trends` | 7 天选餐趋势 |
| GET | `/api/admin/dashboard/score-distribution` | 评分分布 |
| GET | `/api/admin/users` | 用户列表 |
| DELETE | `/api/admin/users/{id}` | 删除用户 |
| GET | `/api/admin/merchants` | 商户列表 |
| POST | `/api/admin/merchants` | 新增商户 |
| PUT | `/api/admin/merchants/{id}` | 编辑商户 |
| DELETE | `/api/admin/merchants/{id}` | 删除商户 |
| GET | `/api/admin/favorites` | 收藏记录查询 |
| GET | `/api/admin/histories` | 选餐历史查询 |
| GET | `/api/admin/backups` | 备份列表 |
| POST | `/api/admin/backups` | 创建备份 |
| POST | `/api/admin/restore/{id}` | 恢复备份 |
| DELETE | `/api/admin/backups/{id}` | 删除备份 |
| GET | `/api/admin/logs` | 系统操作日志 |

---

## 六、后端（Spring Boot）详细说明

### 6.1 配置（application.yml）

```yaml
server:
  port: 9999

spring:
  jackson:
    property-naming-strategy: SNAKE_CASE   # JSON 字段全部 snake_case
  datasource:
    url: jdbc:mysql://localhost:3306/food_recommendation?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
    username: root
    password: 123456

wechat:
  miniapp:
    appid: your-appid-here      # 占位符，需替换
    secret: your-secret-here    # 占位符，需替换

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted    # 已配置但实体中未使用
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 6.2 统一响应体（Result.java）

```json
{
  "code": 200,       // 200=成功, 500=失败
  "message": "success",
  "data": {}         // 具体数据
}
```

### 6.3 全部 API 端点速查（38 个）

| # | 方法 | 路径 | 所属 Controller | 功能 |
|---|---|---|---|---|
| 1 | GET | `/api/admin/dashboard` | AdminController | 仪表盘统计 |
| 2 | GET | `/api/admin/dashboard/trends` | AdminController | 7天趋势 |
| 3 | GET | `/api/admin/dashboard/score-distribution` | AdminController | 评分分布 |
| 4 | GET | `/api/admin/users` | AdminController | 用户列表 |
| 5 | DELETE | `/api/admin/users/{id}` | AdminController | 删除用户 |
| 6 | GET | `/api/admin/merchants` | AdminController | 商户列表 |
| 7 | POST | `/api/admin/merchants` | AdminController | 新增商户 |
| 8 | PUT | `/api/admin/merchants/{id}` | AdminController | 编辑商户 |
| 9 | DELETE | `/api/admin/merchants/{id}` | AdminController | 删除商户 |
| 10 | GET | `/api/admin/favorites` | AdminController | 收藏查询 |
| 11 | GET | `/api/admin/histories` | AdminController | 历史查询 |
| 12 | GET | `/api/admin/backups` | AdminController | 备份列表 |
| 13 | POST | `/api/admin/backups` | AdminController | 创建备份 |
| 14 | POST | `/api/admin/restore/{id}` | AdminController | 恢复备份 |
| 15 | DELETE | `/api/admin/backups/{id}` | AdminController | 删除备份 |
| 16 | GET | `/api/admin/stalls` | MerchantController | 档口列表 |
| 17 | POST | `/api/admin/stalls` | MerchantController | 新增档口 |
| 18 | PUT | `/api/admin/stalls/{id}` | MerchantController | 编辑档口 |
| 19 | DELETE | `/api/admin/stalls/{id}` | MerchantController | 删除档口 |
| 20 | GET | `/api/admin/stalls/{id}/dishes` | MerchantController | 档口菜品 |
| 21 | GET | `/api/admin/dishes` | MerchantController | 菜品列表 |
| 22 | GET | `/api/admin/dishes/{id}` | MerchantController | 菜品详情 |
| 23 | POST | `/api/admin/dishes` | MerchantController | 新增菜品 |
| 24 | PUT | `/api/admin/dishes/{id}` | MerchantController | 编辑菜品 |
| 25 | DELETE | `/api/admin/dishes/{id}` | MerchantController | 删除菜品 |
| 26 | POST | `/api/miniapp/login` | AuthController | 微信登录 |
| 27 | POST | `/api/miniapp/logout` | AuthController | 退出登录 |
| 28 | GET | `/api/miniapp/profile` | AuthController | 个人信息 |
| 29 | PUT | `/api/miniapp/profile` | AuthController | 更新信息 |
| 30 | POST | `/api/miniapp/favorite` | InteractionController | 添加收藏 |
| 31 | DELETE | `/api/miniapp/favorite` | InteractionController | 取消收藏 |
| 32 | GET | `/api/miniapp/favorites` | InteractionController | 收藏列表 |
| 33 | POST | `/api/miniapp/score` | InteractionController | 评分 |
| 34 | POST | `/api/miniapp/like` | InteractionController | 点赞 |
| 35 | GET | `/api/miniapp/history` | InteractionController | 选餐历史 |
| 36 | GET | `/api/miniapp/recommend/random` | RecommendController | 随机推荐 |
| 37 | GET | `/api/miniapp/recommend/today-praise` | RecommendController | 今日好评榜 |
| 38 | GET | `/api/miniapp/recommend/guess-like` | RecommendController | 猜你喜欢 |

### 6.4 Mapper 自定义查询

| Mapper | 方法 | 说明 |
|---|---|---|
| DishMapper | `selectDishWithStall` | 联表查询菜品+档口+商户（分页） |
| DishMapper | `countDishWithStall` | 对应的 COUNT 查询 |
| DishMapper | `selectRandomDish` | `ORDER BY RAND() LIMIT 1` |
| DishMapper | `selectTodayPraise` | 当天好评榜聚合（score≥4, AVG, GROUP BY, LIMIT 10） |
| FavoriteMapper | `selectFavoriteWithNames` | 联表查询收藏+用户+菜品名（分页） |
| FavoriteMapper | `selectFavoritesByUserId` | 用户收藏列表+菜品详情（分页） |
| SelectionHistoryMapper | `selectHistoryWithNames` | 联表查询历史+用户+菜品名（分页） |
| SelectionHistoryMapper | `selectHistoryByUserId` | 用户历史列表+菜品详情（分页） |
| StallMapper | `selectStallWithMerchant` | 联表查询档口+商户名（分页） |
| StallMapper | `countStallWithMerchant` | 对应的 COUNT 查询 |
| StallMapper | `selectStallDishCount` | 各档口菜品数量统计 |
| UserMapper | （无自定义） | 仅使用 MyBatis-Plus 内置 CRUD |
| MerchantMapper | （无自定义） | 仅使用 MyBatis-Plus 内置 CRUD |

---

## 七、小程序（微信原生）详细说明

### 7.1 全局配置

**app.json：**
- 注册 6 个页面：index, random, guess-like, today-praise, dish-detail, mine
- TabBar 5 个标签：首页、随机、猜你喜欢、好评榜、我的
- 主题色：`#FF6B35`（暖橙色）

**app.js：**
- 启动时从 wx.storage 恢复登录状态（token, userInfo）
- 提供 `setLoginState(token, userInfo)` 和 `clearLoginState()` 方法
- `globalData`：token, userId(默认1), userInfo, isLoggedIn, systemInfo, dishCache

**utils/config.js：**
- `BASE_URL`: `http://localhost:9999/api/miniapp`
- `ADMIN_BASE_URL`: `http://localhost:9999/api/admin`
- `MOCK_USER_ID`: 1
- `CATEGORIES`: 12 个菜品分类
- `MARQUEE_CONFIG`: 跑马灯参数（80ms 间隔）

**utils/api.js：**
- 通用 `request()` 封装 wx.request，支持 `useAdmin` 切换管理端接口
- 导出：login, logout, getProfile, updateProfile, getRandomDish, getTodayPraise, getGuessLike, addFavorite, removeFavorite, getFavorites, addHistory, toggleLike, getHistory, getDishList

### 7.2 各页面功能

#### 首页（index）
- 搜索栏（实时输入+确认搜索+清空）
- 水平滚动分类标签（12 类，点击切换，再点取消）
- Banner 轮播（3 张幻灯片，自动播放）
- 菜品网格（2 列，分页加载，无限滚动）
- 骨架屏加载动画
- API：`getDishList`（复用管理端接口）

#### 随机选餐（random）
- 老虎机跑马灯效果（80ms 刷新，Fisher-Yates 洗牌）
- 从管理端加载菜品池（最多 100 道）
- 开始/停止按钮 + 震动反馈
- 选中后弹窗：查看详情 / 再来一次
- 记录选餐历史
- API：`getDishList`, `getRandomDish`, `addHistory`

#### 猜你喜欢（guess-like）
- 个性化推荐卡片列表（推荐原因+菜品信息）
- 每次进入 Tab 刷新
- 支持下拉刷新
- 空状态提示
- API：`getGuessLike`

#### 今日好评榜（today-praise）
- 当天高分菜品 TOP 10 排行榜
- 金银铜牌标识前 3 名
- 星级评分展示
- 支持下拉刷新
- API：`getTodayPraise`

#### 菜品详情（dish-detail）
- 全屏大图 + 返回按钮
- 菜品信息（名称/价格/分类/档口）
- 统计数据（平均分/好评数/价格）
- 描述信息
- 固定底栏：收藏按钮 + "选这道" 按钮
- 选中后弹窗确认
- 优先使用 globalData.dishCache 缓存，未命中时请求 API
- API：`getDishList`（回退查找）, `getFavorites`, `addFavorite`/`removeFavorite`, `addHistory`

#### 个人中心（mine）
- 未登录：登录引导页（微信一键登录）
- 已登录：头像+昵称+ID+编辑昵称
- 统计栏（收藏数/历史数/评分数）
- 三个子 Tab：
  - 收藏列表（支持取消收藏）
  - 选餐历史（含评分/点赞标识）
  - 评分记录（支持重新评分，1-5 星选择）
- 退出登录按钮
- API：`login`, `logout`, `updateProfile`, `getFavorites`, `getHistory`, `removeFavorite`, `addHistory`

---

## 八、管理后台前端（Vue3）详细说明

### 8.1 路由配置（7 个页面）

| 路径 | 组件 | 标题 | 图标 |
|---|---|---|---|
| `/dashboard` | Dashboard.vue | 数据总览 | Odometer |
| `/users` | UserManagement.vue | 用户管理 | UserFilled |
| `/merchants` | MerchantManagement.vue | 商户管理 | Shop |
| `/stalls` | StallOverview.vue | 档口信息总览 | Grid |
| `/dishes` | DishAudit.vue | 菜品信息总览 | Dish |
| `/data-query` | DataQuery.vue | 全局数据查询 | Search |
| `/database` | DatabaseMaintenance.vue | 数据库维护 | Coin |

> 路由使用 Hash 模式（`createWebHashHistory`）。

### 8.2 各页面功能

#### 数据总览（Dashboard）
- 6 个统计卡片（用户/商户/档口/菜品/收藏/历史，各带渐变色和趋势箭头）
- 4 个 ECharts 图表：
  - 菜品分类饼图
  - 档口菜品柱状图
  - 7 天选餐趋势折线图
  - 评分分布柱状图
- 系统概览描述表
- API：`getDashboardStats`, `getDashboardTrends`, `getScoreDistribution`

#### 用户管理（UserManagement）
- CRUD：列表/新增/编辑/详情/启禁用/删除/批量删除
- 搜索（用户名/ID）+ 状态筛选
- 分页表格
- API：`getUsers`, `addUser`, `updateUser`, `updateUserStatus`, `deleteUser`, `batchDeleteUsers`

#### 商户管理（MerchantManagement）
- CRUD：列表/新增/编辑/删除
- 搜索（商户名/联系方式）
- 链接数可点击跳转到档口页面
- API：`getMerchants`, `addMerchant`, `updateMerchant`, `deleteMerchant`

#### 档口总览（StallOverview）
- CRUD：列表/新增/编辑/详情/删除
- 搜索 + 商户筛选
- 详情弹窗含嵌套菜品子表格
- 支持从商户页面跳转预填筛选
- API：`getStalls`, `getMerchants`, `addStall`, `updateStall`, `deleteStall`, `getStallDishes`

#### 菜品总览（DishAudit）
- 只读展示（无编辑功能）
- 卡片网格布局（非表格）
- 3 个统计卡片（总数/分类数/均价）
- 搜索 + 分类筛选
- 详情弹窗
- API：`getDishes`

#### 全局数据查询（DataQuery）
- 4 个 Tab：用户/菜品/收藏/选餐历史
- 每个 Tab 独立分页
- 关键词搜索 + 日期范围筛选
- 导出按钮（目前仅提示，未实现）
- API：`getUsers`, `getDishes`, `getFavorites`, `getHistories`

#### 数据库维护（DatabaseMaintenance）
- 统计卡片（备份数/最近备份时间）
- 备份大小趋势折线图
- 备份文件列表（恢复/下载/删除）
- 系统操作日志表格
- 自动备份设置弹窗
- 清理过期备份功能
- API：`getBackupList`, `getSystemLogs`, `createBackup`, `restoreBackup`, `deleteBackup`, `cleanupBackups`

### 8.3 公共组件（7 个）

| 组件 | 用途 | 关键 Props |
|---|---|---|
| DataTable | 表格+分页 | data, total, page, pageSize |
| FormDialog | 新增/编辑弹窗 | visible, isEdit, entityName, form, rules |
| SearchBar | 搜索/筛选栏 | model, showKeyword, filters |
| StatCard | 统计卡片 | label, value, icon, gradient, trend |
| StatusTag | 状态标签 | status, text |
| PageHeader | 面包屑+标题 | title, breadcrumb |
| ConfirmAction | 删除确认 | title, confirmText |

### 8.4 布局（AdminLayout）
- 可折叠侧边栏（220px / 64px）
- 深色渐变背景（#1a1a2e → #0f3460）
- 顶栏：折叠按钮 + 页面标题 + 通知铃铛 + 头像下拉
- 内容区：浅灰背景（#f0f2f5）

---

## 九、已知问题与不完整实现

### 后端问题

| # | 问题 | 位置 | 严重度 |
|---|---|---|---|
| 1 | 收藏/历史计数忽略关键词过滤 | AdminServiceImpl | 中 |
| 2 | 删除用户不级联清理收藏和历史 | AdminServiceImpl | 中 |
| 3 | 删除商户不级联清理档口和菜品 | AdminServiceImpl | 中 |
| 4 | 数据库备份/恢复是内存模拟（非真实） | AdminServiceImpl | 低 |
| 5 | 微信登录使用 mock openid（无真实 appid/secret） | AuthServiceImpl | 低 |
| 6 | 无 Spring Security / JWT 认证 | 全局 | 低 |
| 7 | 无全局异常处理器 | 全局 | 中 |
| 8 | 无输入校验（@Valid） | Controller 层 | 中 |
| 9 | 无单元测试 | 全局 | 低 |
| 10 | 无 DTO 层，直接使用 Entity 和 Map | 全局 | 低 |
| 11 | N+1 查询（getDishDetail, getGuessLike） | Service 层 | 低 |
| 12 | 硬编码数据库密码 | application.yml | 低 |

### 小程序问题

| # | 问题 | 位置 | 严重度 |
|---|---|---|---|
| 1 | 未登录时 userId 默认为 1，推荐不个性化 | 全局 | 中 |
| 2 | 请求未携带 auth token | api.js | 中 |
| 3 | 菜品详情回退查询低效（加载 100 条再筛选） | dish-detail | 低 |
| 4 | 今日好评榜半星渲染 bug | today-praise | 低 |
| 5 | toggleLike API 已定义但无页面调用 | api.js | 低 |
| 6 | getProfile API 已定义但无页面调用 | api.js | 低 |
| 7 | 缺少默认头像图片 | mine | 低 |
| 8 | index 页面 onPullDownRefresh 未在 json 启用 | index | 低 |
| 9 | guess-like/today-praise/mine 无分页 | 各页面 | 低 |

### 管理后台问题

| # | 问题 | 位置 | 严重度 |
|---|---|---|---|
| 1 | 数据导出功能未实现（注释掉） | DataQuery.vue | 低 |
| 2 | 自动备份设置不持久化 | DatabaseMaintenance.vue | 低 |
| 3 | 备份下载功能未实现 | DatabaseMaintenance.vue | 低 |
| 4 | 顶栏下拉菜单（个人信息/设置/退出）无事件 | AdminLayout.vue | 低 |
| 5 | 通知铃铛数字硬编码为 3 | AdminLayout.vue | 低 |
| 6 | 菜品总览无审核功能（Audit 名不副实） | DishAudit.vue | 低 |
| 7 | 均价/分类数只计算当前页数据 | DishAudit.vue | 低 |
| 8 | 档口筛选下拉框硬编码商户名 | StallOverview.vue | 低 |
| 9 | Pinia 已安装但无 Store | main.js | 低 |
| 10 | DishOverview.vue 是死代码 | views/ | 低 |
| 11 | mock.js 未被任何文件引用 | api/mock.js | 低 |

---

## 十、开发指南

### 环境要求
- Node.js ≥ 18
- JDK ≥ 21
- Maven ≥ 3.8
- MySQL 8.0
- 微信开发者工具

### 数据库初始化
```sql
CREATE DATABASE IF NOT EXISTS food_recommendation
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 启动后端
```bash
cd admin-panel/server-springboot
mvn spring-boot:run -DskipTests
# → http://localhost:9999
```

### 启动管理后台
```bash
cd admin-panel
npm install
npm run dev
# → http://localhost:5173
```

### 调试小程序
1. 打开微信开发者工具
2. 导入项目 → 选择 `miniapp/` 目录
3. AppID：`wx072e7aa4c218a0f6`
4. 勾选「不校验合法域名」

### Git 提交规范
```
feat: 新功能
fix: 修复
refactor: 重构
style: 样式
docs: 文档
```
