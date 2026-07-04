/**
 * 猜你喜欢 — 基于用户历史行为推荐菜品
 * 成员B：选餐与个性化推荐模块
 */

const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    list: [],
    loading: false,
    page: 1,
    pageSize: 10,
    hasMore: true
  },

  onLoad() {
    this.loadGuessLike()
  },

  onShow() {
    // 每次显示时刷新
    this.loadGuessLike()
  },

  onPullDownRefresh() {
    this.loadGuessLike()
    wx.stopPullDownRefresh()
  },

  /**
   * 加载猜你喜欢推荐
   */
  async loadGuessLike() {
    this.setData({ loading: true })

    try {
      // 使用全局 userId（当前为 Mock 值 1）
      const userId = app.globalData.userId
      const data = await api.getGuessLike(userId)
      this.setData({
        list: data.list || [],
        loading: false
      })
    } catch (err) {
      console.error('[GuessLike] 加载失败:', err)
      this.setData({ loading: false })
    }
  },

  /**
   * 点击推荐菜品 → 详情页
   */
  onItemTap(e) {
    const dish = e.currentTarget.dataset.dish
    getApp().globalData.dishCache = dish
    wx.navigateTo({
      url: `/pages/dish-detail/dish-detail?dishId=${dish.dish_id}&dishName=${encodeURIComponent(dish.dish_name)}`
    })
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ page: this.data.page + 1 })
      this.loadMore()
    }
  },

  async loadMore() {
    this.setData({ hasMore: false })
  }
})
