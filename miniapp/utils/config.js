/**
 * 全局配置常量
 * 成员B：选餐与个性化推荐模块
 */

// 后端 API 基础地址
const BASE_URL = 'http://localhost:9999/api/miniapp'

// 管理后台 API（用于获取菜品列表等）
const ADMIN_BASE_URL = 'http://localhost:9999/api/admin'

// 默认分页
const DEFAULT_PAGE_SIZE = 10

// Mock 用户ID（登录功能由成员A开发中）
const MOCK_USER_ID = 1

// 跑马灯配置
const MARQUEE_CONFIG = {
  interval: 80,        // 切换间隔（毫秒）
  poolSize: 20,        // 菜品池大小
  minPoolSize: 8       // 最小菜品数
}

// 分类列表
const CATEGORIES = [
  { key: '', label: '全部' },
  { key: '川菜', label: '川菜' },
  { key: '粤菜', label: '粤菜' },
  { key: '湘菜', label: '湘菜' },
  { key: '鲁菜', label: '鲁菜' },
  { key: '闽菜', label: '闽菜' },
  { key: '浙菜', label: '浙菜' },
  { key: '苏菜', label: '苏菜' },
  { key: '徽菜', label: '徽菜' },
  { key: '面食', label: '面食' },
  { key: '小吃', label: '小吃' },
  { key: '饮品', label: '饮品' }
]

module.exports = {
  BASE_URL,
  ADMIN_BASE_URL,
  DEFAULT_PAGE_SIZE,
  MOCK_USER_ID,
  MARQUEE_CONFIG,
  CATEGORIES
}
