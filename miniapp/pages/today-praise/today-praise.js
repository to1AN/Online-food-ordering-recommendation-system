/**
 * 今日好评榜 — 今日评分≥4分的菜品TOP10
 * 成员B：选餐与个性化推荐模块
 */

const api = require('../../utils/api')

Page({
  data: {
    list: [],
    loading: false
  },

  onLoad() {
    this.loadPraiseList()
  },

  onShow() {
    // 每次显示时刷新数据
    this.loadPraiseList()
  },

  onPullDownRefresh() {
    this.loadPraiseList()
    wx.stopPullDownRefresh()
  },

  /**
   * 加载今日好评榜
   */
  async loadPraiseList() {
    this.setData({ loading: true })

    try {
      const data = await api.getTodayPraise()
      this.setData({
        list: data.list || [],
        loading: false
      })
    } catch (err) {
      console.error('[TodayPraise] 加载失败:', err)
      this.setData({ loading: false })
    }
  },

  /**
   * 点击菜品 → 详情页
   */
  onItemTap(e) {
    const dish = e.currentTarget.dataset.dish
    getApp().globalData.dishCache = dish
    wx.navigateTo({
      url: `/pages/dish-detail/dish-detail?dishId=${dish.dish_id}&dishName=${encodeURIComponent(dish.dish_name)}`
    })
  }
})
