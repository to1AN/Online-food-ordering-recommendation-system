/**
 * 今日好评榜 — 今日评分≥4分的菜品TOP10
 * 成员B：选餐与个性化推荐模块
 */

const api = require('../../utils/api')

Page({
  data: {
    list: [],
    loading: false,
    page: 1,
    pageSize: 10,
    hasMore: true
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
      const list = (data.list || []).map(item => {
        const avgScore = parseFloat(item.avg_score || 0)
        const fullStars = Math.floor(avgScore)
        const hasHalfStar = (avgScore - fullStars) >= 0.25 && (avgScore - fullStars) < 0.75
        const extraFull = (avgScore - fullStars) >= 0.75 ? 1 : 0
        const totalFull = fullStars + extraFull
        const showHalf = hasHalfStar && extraFull === 0
        const emptyCount = 5 - totalFull - (showHalf ? 1 : 0)
        return {
          ...item,
          fullStars: totalFull,
          hasHalfStar: showHalf,
          emptyStars: emptyCount,
          fullStarArr: new Array(totalFull).fill(0),
          halfStarArr: showHalf ? [0] : [],
          emptyStarArr: new Array(emptyCount).fill(0)
        }
      })
      this.setData({ list, loading: false })
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
