# API 接口文档

## 通用说明

- **Base URL（管理后台）**：`http://localhost:9999/api/admin`
- **Base URL（小程序）**：`http://localhost:9999/api/miniapp`
- **请求方式**：JSON
- **响应格式**：

```json
{
  "code": 200,        // 200 = 成功，500 = 失败
  "message": "success",
  "data": {}          // 具体数据，类型见各接口
}
```

- **字段命名**：请求 / 响应均使用 `snake_case`（如 `user_id`、`merchant_name`、`dish_name`）
- **分页参数**：`page` 从 1 开始，`pageSize` 默认 10

---

## 一、管理后台 `/api/admin`

### 1. 数据总览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/dashboard` | 仪表盘统计 |
| GET | `/dashboard/trends` | 近 7 天选餐趋势 |
| GET | `/dashboard/score-distribution` | 评分分布（1~5 星） |

**GET /dashboard 响应：**
```json
{
  "code": 200,
  "data": {
    "totalUsers": 10,
    "totalMerchants": 5,
    "totalStalls": 8,
    "totalDishes": 36,
    "totalFavorites": 12,
    "totalHistories": 45,
    "avgScore": 3.8,
    "todaySelects": 3,
    "categoryDistribution": [
      { "category": "川菜", "value": 8 },
      { "category": "粤菜", "value": 5 }
    ],
    "stallDistribution": [
      { "name": "川味档口", "value": 12 },
      { "name": "粤菜档口", "value": 6 }
    ]
  }
}
```

**GET /dashboard/trends 响应：**
```json
{
  "code": 200,
  "data": {
    "dates": ["6/6", "6/7", "6/8", "6/9", "6/10", "6/11", "6/12"],
    "values": [5, 8, 3, 10, 7, 12, 4]
  }
}
```

**GET /dashboard/score-distribution 响应：**
```json
{
  "code": 200,
  "data": [2, 3, 5, 8, 10]
  //        1星 2星 3星 4星 5星
}
```

### 2. 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/users?keyword=&page=&pageSize=` | 用户列表 |
| DELETE | `/users/{id}` | 删除用户 |

**GET /users 响应 data 字段：**
```json
{
  "list": [
    {
      "user_id": 1,
      "username": "张三",
      "avatar": "https://...",
      "phone": "13800138000",
      "status": 1,
      "register_time": "2025-06-01 12:00:00"
    }
  ],
  "total": 10
}
```

### 3. 商户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/merchants?keyword=&page=&pageSize=` | 商户列表 |
| POST | `/merchants` | 新增商户 |
| PUT | `/merchants/{id}` | 编辑商户 |
| DELETE | `/merchants/{id}` | 删除商户 |

**POST / PUT 请求体：**
```json
{
  "merchant_name": "川渝味道",
  "logo": "https://...",
  "contact_info": "13800138000",
  "description": "正宗川菜"
}
```

### 4. 档口总览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/stalls?keyword=&page=&pageSize=` | 档口列表（含商户名） |

**GET /stalls 响应 data.list 字段：**
```json
{
  "stall_id": 1,
  "stall_name": "川味档口",
  "location": "A区",
  "merchant_id": 1,
  "merchant_name": "川渝味道"
}
```

### 5. 菜品总览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/dishes?keyword=&category=&page=&pageSize=` | 菜品列表（含档口、商户名） |

**GET /dishes 响应 data.list 字段：**
```json
{
  "dish_id": 1,
  "dish_name": "宫保鸡丁",
  "price": 18.00,
  "category": "川菜",
  "description": "经典川菜",
  "image_url": "https://...",
  "stall_id": 1,
  "stall_name": "川味档口",
  "merchant_name": "川渝味道"
}
```

### 6. 全局数据查询

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/favorites?keyword=&page=&pageSize=` | 收藏记录 |
| GET | `/histories?keyword=&page=&pageSize=` | 选餐历史 |

### 7. 数据库维护

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/backups` | 备份列表 |
| POST | `/backups` | 手动备份 |
| POST | `/restore/{id}` | 恢复备份 |
| DELETE | `/backups/{id}` | 删除备份 |

---

## 二、微信小程序 `/api/miniapp`

### 1. 登录

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/login` | 微信登录 |

**请求体：** `{ "code": "wx.login()返回值" }`

### 2. 推荐

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/recommend/random` | 随机推荐一道菜 |
| GET | `/recommend/today-praise` | 今日好评榜 TOP10 |
| GET | `/recommend/guess-like?userId=` | 猜你喜欢 |

### 3. 收藏

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/favorite` | 添加收藏 `{ userId, dishId }` |
| DELETE | `/favorite?userId=&dishId=` | 取消收藏 |
| GET | `/favorites?userId=&page=&pageSize=` | 我的收藏列表 |

### 4. 互动

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/score` | 评分 `{ userId, dishId, score(1~5) }` |
| POST | `/like` | 点赞/取消 `{ userId, dishId, likeStatus }` |

### 5. 历史

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/history?userId=&page=&pageSize=` | 选餐历史 |

---

## 错误码

| code | 含义 |
|------|------|
| 200 | 成功 |
| 500 | 失败（message 字段含具体原因） |
