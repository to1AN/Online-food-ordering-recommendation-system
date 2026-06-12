# 网上点餐推荐系统

在线点餐智能推荐平台，包含管理后台（Web）与微信小程序端。

## 项目结构

```
├── admin-panel/                    # 管理后台（Vue 3 + Spring Boot）
│   ├── src/
│   │   ├── api/                    # API 接口层
│   │   ├── components/             # 公共组件（7个）
│   │   ├── layout/                 # 布局组件
│   │   ├── router/                 # 路由配置
│   │   └── views/
│   │       ├── Dashboard.vue               # 数据总览（ECharts 图表）
│   │       ├── UserManagement.vue          # 用户管理
│   │       ├── MerchantManagement.vue      # 商户管理
│   │       ├── StallOverview.vue           # 档口信息总览
│   │       ├── DishAudit.vue               # 菜品信息总览
│   │       ├── DataQuery.vue               # 全局数据查询
│   │       └── DatabaseMaintenance.vue     # 数据库维护
│   ├── server-springboot/          # 后端（Spring Boot 3.2.6）
│   │   └── src/main/java/com/foodrec/admin/
│   │       ├── controller/         # AdminController / MiniAppController
│   │       ├── service/            # 业务逻辑（含猜你喜欢推荐算法）
│   │       ├── mapper/             # MyBatis-Plus 数据访问层
│   │       ├── entity/             # 6 张表实体类
│   │       └── common/             # 公共配置
│   └── package.json
├── miniapp/                        # 微信小程序
│   ├── app.js / app.json / app.wxss  # 应用入口
│   ├── pages/
│   │   ├── index/                  # 首页
│   │   ├── dish-detail/            # 菜品详情
│   │   ├── guess-like/             # 猜你喜欢
│   │   ├── random/                 # 随机选餐
│   │   └── today-praise/           # 今日好评榜
│   └── utils/                      # 工具函数
├── API.md                          # 接口文档
└── README.md
```

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 管理后台前端 | Vue 3 + Element Plus | 3.5 / 2.14 |
| 图表 | ECharts | 6.1 |
| 小程序 | 微信原生开发 | — |
| 后端 | Spring Boot | 3.2.6 |
| ORM | MyBatis-Plus | 3.5.7 |
| 数据库 | MySQL | 8.0 |
| JDK | 21 | — |
| 构建 | Maven / Vite | — |

## 快速开始

### 环境要求

- **Node.js** ≥ 18
- **JDK** ≥ 21
- **Maven** ≥ 3.8
- **MySQL** 8.0
- **微信开发者工具**（小程序调试用）

### 1. 数据库

```sql
CREATE DATABASE IF NOT EXISTS food_recommendation
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

修改 `admin-panel/server-springboot/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/food_recommendation?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
    username: root
    password: 你的密码
```

### 2. 启动后端

```bash
cd admin-panel/server-springboot
mvn spring-boot:run -DskipTests
# → http://localhost:9999
```

### 3. 启动管理后台

```bash
cd admin-panel
npm install
npm run dev
# → http://localhost:5173
```

### 4. 调试小程序

1. 打开**微信开发者工具**
2. 导入项目 → 选择 `miniapp/` 目录
3. AppID：`wx072e7aa4c218a0f6`
4. 勾选「不校验合法域名」

## 管理后台功能

| 模块 | 功能 |
|------|------|
| 数据总览 | 6 项统计卡片、菜品分类饼图、档口菜品柱状图、7 天趋势、评分分布 |
| 用户管理 | 用户列表、搜索、删除 |
| 商户管理 | 商户列表、新增、编辑、删除 |
| 档口总览 | 档口列表、查看所属菜品 |
| 菜品总览 | 菜品卡片展示、分类筛选、详情弹窗 |
| 数据查询 | 用户 / 菜品 / 收藏 / 选餐历史 四 Tab 查询 |
| 数据库维护 | 备份列表、手动备份、数据恢复、删除 |

## 小程序功能

| 模块 | 功能 |
|------|------|
| 登录 | 微信一键登录（自动注册） |
| 首页 | 菜品浏览 |
| 随机选餐 | 随机推荐一道菜 |
| 今日好评榜 | 当天高分菜品 TOP 10 |
| 猜你喜欢 | 基于历史偏好 + 收藏的分类推荐 |
| 菜品详情 | 查看菜品完整信息 |
| 互动 | 评分（1-5）+ 点赞 / 取消 |
| 收藏 | 收藏 / 取消收藏 / 收藏列表 |
| 历史 | 选餐历史记录 |

## 推荐算法

**猜你喜欢** —— 基于内容的混合推荐：

1. 分析用户近期高分（≥ 3 分）选餐记录，提取偏好分类
2. 结合用户收藏菜品的分类
3. 排除已尝试菜品，在偏好分类中推荐未试过的新菜品

## 协作开发

```bash
git clone https://github.com/to1AN/Online-food-ordering-recommendation-system.git
git checkout -b feature/你的功能名
git commit -m "feat: 功能描述"
git push -u origin feature/你的功能名
```

提交规范：`feat:` / `fix:` / `refactor:` / `style:` / `docs:`

## 接口文档

详见 [API.md](./API.md)，含管理后台 16 个 + 小程序 10 个接口的说明及响应示例。

## 成员分工

| 成员 | 模块 | 主要功能 |
|------|------|----------|
| 成员 A | 用户认证与互动 | 登录注册、收藏管理、评分点赞、选餐历史 |
| 成员 B | 选餐与个性化推荐 | 首页浏览、随机选餐、猜你喜欢、今日好评榜、菜品详情 |
| 成员 C | 商户后台 | 档口管理、菜品管理、经营数据查询 |
| 成员 D | 管理员后台 | 用户管理、商户管理、数据查询、数据库备份与恢复 |

## 常见问题

**端口被占用**
```bash
netstat -ano | findstr 9999
taskkill /PID <PID> /F
```
