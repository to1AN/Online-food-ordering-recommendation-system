/**
 * Mock 数据层 —— 模拟后端API返回数据
 * 正式开发时替换为真实 API 调用
 */

// ==================== 用户数据 ====================
const users = [
  { user_id: 1, username: '张三', password: '******', avatar: '', register_time: '2025-01-15 10:30:00' },
  { user_id: 2, username: '李四', password: '******', avatar: '', register_time: '2025-02-20 14:22:00' },
  { user_id: 3, username: '王五', password: '******', avatar: '', register_time: '2025-03-10 09:15:00' },
  { user_id: 4, username: '赵六', password: '******', avatar: '', register_time: '2025-03-18 16:45:00' },
  { user_id: 5, username: '孙七', password: '******', avatar: '', register_time: '2025-04-02 11:30:00' },
  { user_id: 6, username: '周八', password: '******', avatar: '', register_time: '2025-04-12 08:20:00' },
  { user_id: 7, username: '吴九', password: '******', avatar: '', register_time: '2025-04-25 13:10:00' },
  { user_id: 8, username: '郑十', password: '******', avatar: '', register_time: '2025-05-08 17:55:00' },
]

// ==================== 商户数据 ====================
const merchants = [
  { merchant_id: 1, merchant_name: '美味轩餐饮', contact_info: '13800001111', create_time: '2025-01-01' },
  { merchant_id: 2, merchant_name: '香满楼饮食', contact_info: '13800002222', create_time: '2025-01-05' },
  { merchant_id: 3, merchant_name: '聚福阁餐厅', contact_info: '13800003333', create_time: '2025-02-10' },
  { merchant_id: 4, merchant_name: '御膳坊', contact_info: '13800004444', create_time: '2025-02-20' },
  { merchant_id: 5, merchant_name: '百味园食品', contact_info: '13800005555', create_time: '2025-03-01' },
]

// ==================== 档口数据 ====================
const stalls = [
  { stall_id: 1, stall_name: '川味档口', location: '一楼A区', merchant_id: 1, merchant_name: '美味轩餐饮' },
  { stall_id: 2, stall_name: '粤菜档口', location: '一楼B区', merchant_id: 1, merchant_name: '美味轩餐饮' },
  { stall_id: 3, stall_name: '面食档口', location: '二楼A区', merchant_id: 2, merchant_name: '香满楼饮食' },
  { stall_id: 4, stall_name: '铁板烧档口', location: '二楼B区', merchant_id: 2, merchant_name: '香满楼饮食' },
  { stall_id: 5, stall_name: '日料档口', location: '一楼C区', merchant_id: 3, merchant_name: '聚福阁餐厅' },
  { stall_id: 6, stall_name: '烧烤档口', location: '三楼A区', merchant_id: 4, merchant_name: '御膳坊' },
  { stall_id: 7, stall_name: '小吃档口', location: '一楼D区', merchant_id: 5, merchant_name: '百味园食品' },
  { stall_id: 8, stall_name: '饮品档口', location: '二楼C区', merchant_id: 5, merchant_name: '百味园食品' },
]

// ==================== 菜品数据 ====================
const dishes = [
  { dish_id: 1, dish_name: '麻辣香锅', price: 28.00, category: '川菜', stall_id: 1, stall_name: '川味档口', merchant_name: '美味轩餐饮' },
  { dish_id: 2, dish_name: '水煮鱼', price: 38.00, category: '川菜', stall_id: 1, stall_name: '川味档口', merchant_name: '美味轩餐饮' },
  { dish_id: 3, dish_name: '白切鸡', price: 32.00, category: '粤菜', stall_id: 2, stall_name: '粤菜档口', merchant_name: '美味轩餐饮' },
  { dish_id: 4, dish_name: '兰州拉面', price: 15.00, category: '面食', stall_id: 3, stall_name: '面食档口', merchant_name: '香满楼饮食' },
  { dish_id: 5, dish_name: '铁板牛肉', price: 35.00, category: '铁板烧', stall_id: 4, stall_name: '铁板烧档口', merchant_name: '香满楼饮食' },
  { dish_id: 6, dish_name: '三文鱼刺身', price: 48.00, category: '日料', stall_id: 5, stall_name: '日料档口', merchant_name: '聚福阁餐厅' },
  { dish_id: 7, dish_name: '烤羊肉串', price: 25.00, category: '烧烤', stall_id: 6, stall_name: '烧烤档口', merchant_name: '御膳坊' },
  { dish_id: 8, dish_name: '臭豆腐', price: 10.00, category: '小吃', stall_id: 7, stall_name: '小吃档口', merchant_name: '百味园食品' },
  { dish_id: 9, dish_name: '珍珠奶茶', price: 12.00, category: '饮品', stall_id: 8, stall_name: '饮品档口', merchant_name: '百味园食品' },
  { dish_id: 10, dish_name: '酸菜鱼', price: 42.00, category: '川菜', stall_id: 1, stall_name: '川味档口', merchant_name: '美味轩餐饮' },
]

// ==================== 收藏数据 ====================
const favorites = [
  { favorite_id: 1, user_id: 1, username: '张三', dish_id: 1, dish_name: '麻辣香锅', favorite_time: '2025-05-01 12:00:00' },
  { favorite_id: 2, user_id: 1, username: '张三', dish_id: 5, dish_name: '铁板牛肉', favorite_time: '2025-05-02 18:30:00' },
  { favorite_id: 3, user_id: 2, username: '李四', dish_id: 3, dish_name: '白切鸡', favorite_time: '2025-05-03 11:20:00' },
  { favorite_id: 4, user_id: 3, username: '王五', dish_id: 6, dish_name: '三文鱼刺身', favorite_time: '2025-05-04 19:00:00' },
  { favorite_id: 5, user_id: 4, username: '赵六', dish_id: 2, dish_name: '水煮鱼', favorite_time: '2025-05-05 12:45:00' },
  { favorite_id: 6, user_id: 2, username: '李四', dish_id: 7, dish_name: '烤羊肉串', favorite_time: '2025-05-06 20:10:00' },
]

// ==================== 选餐历史数据 ====================
const histories = [
  { history_id: 1, user_id: 1, username: '张三', dish_id: 1, dish_name: '麻辣香锅', score: 5, like_status: true, select_time: '2025-05-01 12:00:00' },
  { history_id: 2, user_id: 1, username: '张三', dish_id: 3, dish_name: '白切鸡', score: 4, like_status: false, select_time: '2025-05-02 12:30:00' },
  { history_id: 3, user_id: 2, username: '李四', dish_id: 5, dish_name: '铁板牛肉', score: 5, like_status: true, select_time: '2025-05-02 18:45:00' },
  { history_id: 4, user_id: 3, username: '王五', dish_id: 6, dish_name: '三文鱼刺身', score: 3, like_status: false, select_time: '2025-05-03 19:15:00' },
  { history_id: 5, user_id: 4, username: '赵六', dish_id: 2, dish_name: '水煮鱼', score: 4, like_status: true, select_time: '2025-05-04 11:50:00' },
  { history_id: 6, user_id: 5, username: '孙七', dish_id: 8, dish_name: '臭豆腐', score: 2, like_status: false, select_time: '2025-05-05 13:20:00' },
  { history_id: 7, user_id: 6, username: '周八', dish_id: 9, dish_name: '珍珠奶茶', score: 5, like_status: true, select_time: '2025-05-06 15:00:00' },
  { history_id: 8, user_id: 2, username: '李四', dish_id: 10, dish_name: '酸菜鱼', score: 4, like_status: true, select_time: '2025-05-07 12:10:00' },
]

// ==================== 统计概览数据 ====================
const dashboardStats = {
  total_users: users.length,
  total_merchants: merchants.length,
  total_stalls: stalls.length,
  total_dishes: dishes.length,
  total_favorites: favorites.length,
  total_histories: histories.length,
  avg_score: 4.0,
  today_selects: 12,
}

// ==================== 模拟延迟 ====================
function delay(ms = 300) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

// ==================== 导出 API ====================
export default {
  // 仪表盘
  async getDashboardStats() {
    await delay()
    return { code: 200, data: dashboardStats }
  },

  // 用户管理
  async getUsers(params = {}) {
    await delay()
    let list = [...users]
    if (params.keyword) {
      list = list.filter(u => u.username.includes(params.keyword) || String(u.user_id).includes(params.keyword))
    }
    const total = list.length
    const page = params.page || 1
    const pageSize = params.pageSize || 10
    const start = (page - 1) * pageSize
    return { code: 200, data: { list: list.slice(start, start + pageSize), total } }
  },
  async deleteUser(id) {
    await delay()
    const idx = users.findIndex(u => u.user_id === id)
    if (idx > -1) users.splice(idx, 1)
    return { code: 200, message: '删除成功' }
  },

  // 商户管理
  async getMerchants(params = {}) {
    await delay()
    let list = [...merchants]
    if (params.keyword) {
      list = list.filter(m => m.merchant_name.includes(params.keyword) || String(m.merchant_id).includes(params.keyword))
    }
    const total = list.length
    const page = params.page || 1
    const pageSize = params.pageSize || 10
    const start = (page - 1) * pageSize
    return { code: 200, data: { list: list.slice(start, start + pageSize), total } }
  },
  async addMerchant(data) {
    await delay()
    const newId = Math.max(...merchants.map(m => m.merchant_id)) + 1
    merchants.push({ merchant_id: newId, ...data, create_time: new Date().toISOString().slice(0, 10) })
    return { code: 200, message: '添加成功' }
  },
  async updateMerchant(data) {
    await delay()
    const idx = merchants.findIndex(m => m.merchant_id === data.merchant_id)
    if (idx > -1) Object.assign(merchants[idx], data)
    return { code: 200, message: '更新成功' }
  },
  async deleteMerchant(id) {
    await delay()
    const idx = merchants.findIndex(m => m.merchant_id === id)
    if (idx > -1) merchants.splice(idx, 1)
    return { code: 200, message: '删除成功' }
  },

  // 档口总览
  async getStalls(params = {}) {
    await delay()
    let list = [...stalls]
    if (params.keyword) {
      list = list.filter(s => s.stall_name.includes(params.keyword) || s.merchant_name.includes(params.keyword) || s.location.includes(params.keyword))
    }
    const total = list.length
    const page = params.page || 1
    const pageSize = params.pageSize || 10
    const start = (page - 1) * pageSize
    return { code: 200, data: { list: list.slice(start, start + pageSize), total } }
  },

  // 菜品总览
  async getDishes(params = {}) {
    await delay()
    let list = [...dishes]
    if (params.keyword) {
      list = list.filter(d => d.dish_name.includes(params.keyword) || d.category.includes(params.keyword) || d.stall_name.includes(params.keyword))
    }
    if (params.category) {
      list = list.filter(d => d.category === params.category)
    }
    const total = list.length
    const page = params.page || 1
    const pageSize = params.pageSize || 10
    const start = (page - 1) * pageSize
    return { code: 200, data: { list: list.slice(start, start + pageSize), total } }
  },

  // 全局数据查询
  async getFavorites(params = {}) {
    await delay()
    let list = [...favorites]
    if (params.keyword) {
      list = list.filter(f => f.username.includes(params.keyword) || f.dish_name.includes(params.keyword))
    }
    const total = list.length
    const page = params.page || 1
    const pageSize = params.pageSize || 10
    const start = (page - 1) * pageSize
    return { code: 200, data: { list: list.slice(start, start + pageSize), total } }
  },
  async getHistories(params = {}) {
    await delay()
    let list = [...histories]
    if (params.keyword) {
      list = list.filter(h => h.username.includes(params.keyword) || h.dish_name.includes(params.keyword))
    }
    const total = list.length
    const page = params.page || 1
    const pageSize = params.pageSize || 10
    const start = (page - 1) * pageSize
    return { code: 200, data: { list: list.slice(start, start + pageSize), total } }
  },
  async queryAll(params) {
    await delay(500)
    return {
      code: 200,
      data: {
        users: users.filter(u => !params.keyword || u.username.includes(params.keyword)),
        merchants: merchants.filter(m => !params.keyword || m.merchant_name.includes(params.keyword)),
        dishes: dishes.filter(d => !params.keyword || d.dish_name.includes(params.keyword)),
        favorites: favorites.filter(f => !params.keyword || f.username.includes(params.keyword)),
      }
    }
  },

  // 数据库备份与恢复
  async getBackupList() {
    await delay()
    return {
      code: 200,
      data: [
        { id: 1, filename: 'backup_20250601_030000.sql', size: '2.3 MB', time: '2025-06-01 03:00:00', type: '自动备份' },
        { id: 2, filename: 'backup_20250602_030000.sql', size: '2.4 MB', time: '2025-06-02 03:00:00', type: '自动备份' },
        { id: 3, filename: 'backup_20250603_030000.sql', size: '2.5 MB', time: '2025-06-03 03:00:00', type: '自动备份' },
        { id: 4, filename: 'backup_20250604_150000.sql', size: '2.5 MB', time: '2025-06-04 15:00:00', type: '手动备份' },
      ]
    }
  },
  async createBackup() {
    await delay(1000)
    return { code: 200, message: '备份成功！文件: backup_' + new Date().toISOString().replace(/[-:T]/g, '').slice(0, 14) + '.sql' }
  },
  async restoreBackup(id) {
    await delay(2000)
    return { code: 200, message: '数据恢复成功！' }
  },
  async deleteBackup(id) {
    await delay()
    return { code: 200, message: '备份文件已删除' }
  },
}
