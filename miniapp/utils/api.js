/**
 * API 请求封装
 * 成员B：选餐与个性化推荐模块
 */

const config = require('./config')

/**
 * 通用请求方法
 * @param {string} url     - 接口路径（不含 base）
 * @param {string} method  - 请求方法 GET/POST/PUT/DELETE
 * @param {object} data    - 请求参数
 * @param {boolean} useAdmin - 是否使用管理后台 base URL
 * @returns {Promise}
 */
const request = (url, method = 'GET', data = {}, useAdmin = false) => {
  const baseUrl = useAdmin ? config.ADMIN_BASE_URL : config.BASE_URL

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${baseUrl}${url}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 200) {
          resolve(res.data.data)
        } else {
          const msg = (res.data && res.data.message) || '请求失败'
          wx.showToast({ title: msg, icon: 'none', duration: 2000 })
          reject(res.data)
        }
      },
      fail: (err) => {
        console.error('[API] 网络请求失败:', err)
        wx.showToast({ title: '网络异常，请检查网络', icon: 'none', duration: 2000 })
        reject(err)
      }
    })
  })
}

// ==================== 推荐接口 ====================

/**
 * 随机选餐 — 随机返回一道菜
 */
const getRandomDish = () => request('/recommend/random')

/**
 * 今日好评榜 TOP10
 */
const getTodayPraise = () => request('/recommend/today-praise')

/**
 * 猜你喜欢 — 根据用户偏好推荐
 * @param {number} userId
 */
const getGuessLike = (userId) => request('/recommend/guess-like', 'GET', { userId })

// ==================== 收藏接口 ====================

/**
 * 添加收藏
 * @param {number} userId
 * @param {number} dishId
 */
const addFavorite = (userId, dishId) =>
  request('/favorite', 'POST', { userId, dishId })

/**
 * 取消收藏
 * @param {number} userId
 * @param {number} dishId
 */
const removeFavorite = (userId, dishId) =>
  request(`/favorite?userId=${userId}&dishId=${dishId}`, 'DELETE')

/**
 * 获取收藏列表
 * @param {number} userId
 * @param {number} page
 * @param {number} pageSize
 */
const getFavorites = (userId, page = 1, pageSize = 20) =>
  request(`/favorites?userId=${userId}&page=${page}&pageSize=${pageSize}`)

// ==================== 互动接口 ====================

/**
 * 提交评分 / 记录选餐历史
 * @param {number} userId
 * @param {number} dishId
 * @param {number} score  (1~5，可选)
 */
const addHistory = (userId, dishId, score = 0) =>
  request('/score', 'POST', { userId, dishId, score })

/**
 * 点赞 / 取消点赞
 * @param {number} userId
 * @param {number} dishId
 * @param {boolean} likeStatus
 */
const toggleLike = (userId, dishId, likeStatus) =>
  request('/like', 'POST', { userId, dishId, likeStatus })

/**
 * 获取选餐历史
 * @param {number} userId
 * @param {number} page
 * @param {number} pageSize
 */
const getHistory = (userId, page = 1, pageSize = 20) =>
  request(`/history?userId=${userId}&page=${page}&pageSize=${pageSize}`)

// ==================== 管理后台接口（辅助） ====================

/**
 * 获取菜品列表（管理后台接口，用于首页浏览和跑马灯菜品池）
 * @param {object} params  { keyword, category, page, pageSize }
 */
const getDishList = (params = {}) => {
  const { keyword = '', category = '', page = 1, pageSize = 20 } = params
  let url = `/dishes?page=${page}&pageSize=${pageSize}`
  if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
  if (category) url += `&category=${encodeURIComponent(category)}`
  return request(url, 'GET', {}, true)
}

module.exports = {
  request,
  getRandomDish,
  getTodayPraise,
  getGuessLike,
  addFavorite,
  removeFavorite,
  getFavorites,
  addHistory,
  toggleLike,
  getHistory,
  getDishList
}
