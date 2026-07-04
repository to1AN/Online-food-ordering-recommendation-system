/**
 * API 接口层
 * 对接 Spring Boot 后端 (http://localhost:8088/api/admin/)
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api/admin',
  timeout: 10000,
})

// 响应拦截器
http.interceptors.response.use(
  response => response.data,
  error => {
    const msg = error.response?.data?.message || error.message || '请求失败'
    ElMessage.error(msg)
    console.error('API请求失败:', error)
    return Promise.reject(error)
  }
)

export default {
  // ===================== 仪表盘 =====================
  getDashboardStats() {
    return http.get('/dashboard')
  },
  getDashboardTrends() {
    return http.get('/dashboard/trends')
  },
  getScoreDistribution() {
    return http.get('/dashboard/score-distribution')
  },

  // ===================== 用户管理 =====================
  getUsers(params) {
    return http.get('/users', { params })
  },
  addUser(data) {
    return http.post('/users', data)
  },
  updateUser(data) {
    return http.put(`/users/${data.user_id}`, data)
  },
  updateUserStatus(userId, status) {
    return http.put(`/users/${userId}/status`, { status })
  },
  deleteUser(id) {
    return http.delete(`/users/${id}`)
  },
  batchDeleteUsers(ids) {
    return http.post('/users/batch-delete', { ids })
  },

  // ===================== 商户管理 =====================
  getMerchants(params) {
    return http.get('/merchants', { params })
  },
  addMerchant(data) {
    return http.post('/merchants', data)
  },
  updateMerchant(data) {
    return http.put(`/merchants/${data.merchant_id}`, data)
  },
  deleteMerchant(id) {
    return http.delete(`/merchants/${id}`)
  },

  // ===================== 档口管理 =====================
  getStalls(params) {
    return http.get('/stalls', { params })
  },
  addStall(data) {
    return http.post('/stalls', data)
  },
  updateStall(data) {
    return http.put(`/stalls/${data.stall_id}`, data)
  },
  deleteStall(id) {
    return http.delete(`/stalls/${id}`)
  },
  getStallDishes(stallId) {
    return http.get(`/stalls/${stallId}/dishes`)
  },

  // ===================== 菜品审核 =====================
  getDishes(params) {
    return http.get('/dishes', { params })
  },
  getDishDetail(id) {
    return http.get(`/dishes/${id}`)
  },
  auditDish(id, data) {
    // data: { status: 'approved' | 'rejected', reason?: string }
    return http.put(`/dishes/${id}/audit`, data)
  },

  // ===================== 全局数据查询 =====================
  getFavorites(params) {
    return http.get('/favorites', { params })
  },
  getHistories(params) {
    return http.get('/histories', { params })
  },
  exportData(type, params) {
    return http.get(`/export/${type}`, { params, responseType: 'blob' })
  },

  // ===================== 数据库维护 =====================
  getBackupList() {
    return http.get('/backups')
  },
  createBackup() {
    return http.post('/backups')
  },
  restoreBackup(id) {
    return http.post(`/restore/${id}`)
  },
  deleteBackup(id) {
    return http.delete(`/backups/${id}`)
  },
  cleanupBackups(retention) {
    return http.delete('/backups/cleanup', { params: { retention } })
  },

  // ===================== 系统日志 =====================
  getSystemLogs(params) {
    return http.get('/logs', { params })
  },

  // ===================== 菜品统计 =====================
  getDishStats() {
    return http.get('/dishes/stats')
  },
  updateDishStatus(dishId, status) {
    return http.put(`/dishes/${dishId}/status`, { status })
  },

  // ===================== 通知 =====================
  getNotificationCount() {
    return http.get('/notifications/count')
  },
}
