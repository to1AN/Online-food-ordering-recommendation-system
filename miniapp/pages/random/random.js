/**
 * 随机选餐 — 跑马灯效果
 * 成员B：选餐与个性化推荐模块 · 核心功能
 *
 * 实现思路：
 * 1. 页面加载时从管理后台获取全部菜品，构建菜品池
 * 2. 用户点击"开始"：setInterval 每 80ms 顺序切换菜品，视觉上形成快速滚动
 * 3. 用户点击"停止"：clearInterval，定格当前菜品为结果
 * 4. "就吃这个"记录选餐历史，"再选一次"重新启动
 */

const api = require('../../utils/api')
const config = require('../../utils/config')
const app = getApp()

Page({
  data: {
    // 菜品池
    pool: [],
    poolLoading: false,
    poolEmpty: false,

    // 跑马灯状态
    isRunning: false,
    currentDish: null,   // 当前显示菜品
    result: null,         // 最终选中结果

    // 确认弹窗
    showConfirm: false,

    // 内部状态（不渲染）
    _timer: null,
    _currentIndex: 0
  },

  onLoad() {
    this.loadDishPool()
  },

  onShow() {
    // 如果菜品池为空，重新加载
    if (this.data.pool.length === 0 && !this.data.poolLoading) {
      this.loadDishPool()
    }
  },

  onHide() {
    // 页面隐藏时停止跑马灯
    this._clearTimer()
  },

  onUnload() {
    this._clearTimer()
  },

  // ==================== 菜品池加载 ====================

  /**
   * 加载菜品池 — 获取所有菜品供跑马灯使用
   */
  async loadDishPool() {
    this.setData({ poolLoading: true, poolEmpty: false })

    try {
      // 获取足够多的菜品（多次请求或增大pageSize）
      const data = await api.getDishList({ page: 1, pageSize: 100 })
      let pool = data.list || []

      // 如果菜品不够，尝试多次调用随机接口补充
      if (pool.length < config.MARQUEE_CONFIG.minPoolSize) {
        const randomDishes = await this._fetchRandomDishes(
          config.MARQUEE_CONFIG.poolSize - pool.length
        )
        // 去重合并
        const existingIds = new Set(pool.map(d => d.dish_id))
        randomDishes.forEach(d => {
          if (!existingIds.has(d.dish_id)) {
            pool.push(d)
          }
        })
      }

      this.setData({
        pool,
        poolLoading: false,
        poolEmpty: pool.length === 0
      })

      console.log(`[Random] 菜品池已就绪，共 ${pool.length} 道菜`)
    } catch (err) {
      console.error('[Random] 菜品池加载失败:', err)
      this.setData({
        poolLoading: false,
        poolEmpty: true
      })
    }
  },

  /**
   * 多次调用随机接口补充菜品
   * @param {number} count - 需要获取的数量
   */
  async _fetchRandomDishes(count) {
    const dishes = []
    const maxRetries = Math.min(count * 2, 50) // 最多重试50次

    for (let i = 0; i < maxRetries && dishes.length < count; i++) {
      try {
        const data = await api.getRandomDish()
        if (data && data.dish) {
          // 去重
          if (!dishes.find(d => d.dish_id === data.dish.dish_id)) {
            dishes.push(data.dish)
          }
        }
      } catch (err) {
        // 个别请求失败不影响整体
        console.warn('[Random] 单次随机获取失败:', err)
      }
    }

    return dishes
  },

  // ==================== 跑马灯控制 ====================

  /**
   * 开始滚动
   */
  onStart() {
    const { pool, isRunning } = this.data
    if (isRunning || pool.length === 0) return

    // 打乱菜品池，让每次效果不同
    const shuffled = this._shuffle([...pool])

    this.setData({
      isRunning: true,
      result: null,
      currentDish: shuffled[0],
      showConfirm: false
    })

    // 启动定时器，每 80ms 切换一道菜
    let index = 0
    this._currentIndex = 0

    this._timer = setInterval(() => {
      index = (index + 1) % shuffled.length
      this._currentIndex = index
      this.setData({
        currentDish: shuffled[index]
      })
    }, config.MARQUEE_CONFIG.interval)

    // 触觉反馈
    wx.vibrateShort({ type: 'medium' })
  },

  /**
   * 停止滚动 — 定格当前菜品为结果
   */
  onStop() {
    if (!this.data.isRunning) return

    this._clearTimer()

    // 定格当前菜品
    this.setData({
      isRunning: false,
      result: this.data.currentDish
    })

    // 触觉反馈
    wx.vibrateShort({ type: 'heavy' })
  },

  /**
   * "就吃这个" — 记录选餐历史
   */
  async onConfirm() {
    const { result } = this.data
    if (!result) return

    const userId = app.globalData.userId

    try {
      await api.addHistory(userId, result.dish_id, 0)
      console.log('[Random] 选餐历史已记录:', result.dish_name)
    } catch (err) {
      console.error('[Random] 记录选餐历史失败:', err)
    }

    this.setData({ showConfirm: true })
  },

  /**
   * "再选一次" — 重新启动跑马灯
   */
  onRetry() {
    this.setData({
      result: null,
      currentDish: null,
      showConfirm: false
    })
    // 稍微延迟再启动，让过渡更自然
    setTimeout(() => {
      this.onStart()
    }, 300)
  },

  /**
   * 关闭确认弹窗
   */
  onCloseConfirm() {
    this.setData({ showConfirm: false })
  },

  /**
   * 查看选中菜品详情
   */
  onViewDetail() {
    const { result } = this.data
    if (!result) return

    getApp().globalData.dishCache = result
    wx.navigateTo({
      url: `/pages/dish-detail/dish-detail?dishId=${result.dish_id}&dishName=${encodeURIComponent(result.dish_name)}`
    })
  },

  // ==================== 工具方法 ====================

  /**
   * 清除定时器
   */
  _clearTimer() {
    if (this._timer) {
      clearInterval(this._timer)
      this._timer = null
    }
  },

  /**
   * Fisher-Yates 洗牌算法
   * @param {Array} arr
   * @returns {Array}
   */
  _shuffle(arr) {
    for (let i = arr.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [arr[i], arr[j]] = [arr[j], arr[i]]
    }
    return arr
  }
})
