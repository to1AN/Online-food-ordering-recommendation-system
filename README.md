# 网上点餐推荐系统

在线点餐智能推荐平台，包含管理后台（Web）与微信小程序端。

## 项目结构

```
├── admin-panel/                # 管理后台（Vue 3 + Spring Boot）
│   ├── src/                    # 前端源码
│   │   ├── api/                # API 接口层
│   │   ├── components/         # 公共组件
│   │   ├── layout/             # 布局组件
│   │   ├── router/             # 路由配置
│   │   └── views/              # 页面视图
│   │       ├── Dashboard.vue           # 数据总览（ECharts 图表）
│   │       ├── UserManagement.vue      # 用户管理
│   │       ├── MerchantManagement.vue  # 商户管理
│   │       ├── StallOverview.vue       # 档口信息总览
│   │       ├── DishAudit.vue           # 菜品信息总览
│   │       ├── DataQuery.vue           # 全局数据查询
│   │       └── DatabaseMaintenance.vue # 数据库维护
│   ├── server-springboot/      # 后端源码（Spring Boot 3.2.6）
│   │   └── src/main/java/com/foodrec/admin/
│   │       ├── controller/     # 控制器
│   │       ├── service/        # 业务逻辑
│   │       ├── mapper/         # 数据访问层
│   │       ├── entity/         # 数据实体
│   │       └── common/         # 公共配置
│   ├── vite.config.js          # Vite 配置（含 API 代理）
│   └── package.json
└── README.md
```

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端框架 | Vue 3 (Composition API) | 3.5 |
| UI 组件库 | Element Plus | 2.14 |
| 图表 | ECharts | 6.1 |
| 路由 | Vue Router | 4.6 |
| HTTP 客户端 | Axios | 1.17 |
| 后端框架 | Spring Boot | 3.2.6 |
| ORM | MyBatis-Plus | 3.5.7 |
| 数据库 | MySQL | 8.0 |
| Java | JDK | 21 |
| 构建工具 | Maven / Vite | — |

## 快速开始

### 环境要求
- **Node.js** ≥ 18
- **JDK** ≥ 21
- **Maven** ≥ 3.8
- **MySQL** 8.0

### 1. 数据库

创建数据库并导入数据：

```sql
CREATE DATABASE IF NOT EXISTS food_recommendation
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

修改 `admin-panel/server-springboot/src/main/resources/application.yml` 中的数据库连接信息：

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
```

后端运行在 `http://localhost:9999`

### 3. 启动前端

```bash
cd admin-panel
npm install
npm run dev
```

前端运行在 `http://localhost:5173`，API 请求自动代理到后端 `9999` 端口。

## 管理后台功能

| 模块 | 功能 |
|------|------|
| 数据总览 | 统计卡片、菜品分类饼图、档口菜品柱状图、7天趋势折线图、评分分布 |
| 用户管理 | 查看用户列表、搜索、编辑、启用/禁用、删除 |
| 商户管理 | 查看商户列表、新增、编辑、删除、查看档口数量 |
| 档口总览 | 查看档口列表、新增、编辑、删除、查看所属菜品 |
| 菜品总览 | 菜品卡片展示、按分类筛选、查看详情 |
| 数据查询 | 用户/菜品/收藏/选餐历史查询，支持关键词搜索和导出 |
| 数据库维护 | 备份列表、手动备份、恢复、删除过期备份、系统日志 |

## 协作开发

```bash
# 克隆仓库
git clone https://github.com/to1AN/Online-food-ordering-recommendation-system.git

# 创建功能分支
git checkout -b feature/你的功能名

# 提交代码
git add .
git commit -m "feat: 功能描述"

# 推送并发起 Pull Request
git push -u origin feature/你的功能名
```

提交规范：

| 前缀 | 用途 |
|------|------|
| `feat:` | 新功能 |
| `fix:` | 修复 Bug |
| `refactor:` | 重构 |
| `style:` | 样式调整 |
| `docs:` | 文档 |

## 常见问题

**端口被占用**
```bash
# Windows 查看并结束占用端口的进程
netstat -ano | findstr 9999
taskkill /PID <PID> /F
```

**SSL 证书错误（Git 推送时）**
```bash
git config --global http.sslBackend schannel
```
