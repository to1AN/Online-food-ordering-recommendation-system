/**
 * 首页 — 菜品浏览 + 推荐入口
 * 成员B：选餐与个性化推荐模块
 */

const api = require('../../utils/api')
const config = require('../../utils/config')

Page({
  data: {
    // 搜索
    keyword: '',

    // 分类
    categories: config.CATEGORIES,
    activeCategory: '',

    // Banner
    banners: [
      { title: '今天吃什么？', desc: '随机选餐帮你决定', emoji: '🎲', bgColor: 'linear-gradient(135deg, #FF6B35, #FF8C5A)' },
      { title: '好评如潮', desc: '看看大家都在点什么', emoji: '⭐', bgColor: 'linear-gradient(135deg, #FF4757, #FF6B81)' },
      { title: '猜你喜欢', desc: '专属你的美食推荐', emoji: '💝', bgColor: 'linear-gradient(135deg, #FFA502, #FFBE76)' }
    ],

    // 菜品列表
    dishes: [],
    page: 1,
    pageSize: 20,
    total: 0,
    loading: false,
    noMore: false
  },

  onLoad() {
    this.loadDishes()
  },

  onShow() {
    // 每次显示首页时刷新（可能从其他页面返回）
  },

  onPullDownRefresh() {
    this.setData({ page: 1, dishes: [], noMore: false })
    this.loadDishes()
    wx.stopPullDownRefresh()
  },

  // ==================== 搜索 ====================

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearchConfirm() {
    this.setData({ page: 1, dishes: [], noMore: false })
    this.loadDishes()
  },

  onClearSearch() {
    this.setData({ keyword: '', page: 1, dishes: [], noMore: false })
    this.loadDishes()
  },

  // ==================== 分类切换 ====================

  onCategoryChange(e) {
    const key = e.currentTarget.dataset.key
    // 点击相同分类则取消筛选
    const newKey = this.data.activeCategory === key ? '' : key
    this.setData({
      activeCategory: newKey,
      page: 1,
      dishes: [],
      noMore: false
    })
    this.loadDishes()
  },

  // ==================== 菜品加载 ====================

  async loadDishes() {
    if (this.data.loading) return

    const { keyword, activeCategory, page, pageSize } = this.data

    this.setData({ loading: true })

    try {
      const data = await api.getDishList({
        keyword,
        category: activeCategory,
        page,
        pageSize
      })

      const newDishes = data.list || []
      const total = data.total || 0

      this.setData({
        dishes: page === 1 ? newDishes : [...this.data.dishes, ...newDishes],
        total,
        loading: false,
        noMore: this.data.dishes.length + newDishes.length >= total
      })
    } catch (err) {
      console.error('[Index] 加载菜品失败:', err)
      this.setData({ loading: false })
    }
  },

  // 加载更多
  onLoadMore() {
    if (this.data.loading || this.data.noMore) return

    this.setData({ page: this.data.page + 1 }, () => {
      this.loadDishes()
    })
  },

  // ==================== 菜品点击 → 详情页 ====================

  onDishTap(e) {
    const dish = e.currentTarget.dataset.dish
    // 缓存菜品数据，避免详情页重复请求
    getApp().globalData.dishCache = dish
    wx.navigateTo({
      url: `/pages/dish-detail/dish-detail?dishId=${dish.dish_id}&dishName=${encodeURIComponent(dish.dish_name)}`
    })
  },

  // ==================== 图片加载失败兜底 ====================

  onImageError(e) {
    const index = e.currentTarget.dataset.index
    // 微信小程序 image 组件在 binderror 时无法直接替换 src
    // 使用一个标记来区分，或者直接用 wx:if 切换占位图
    console.warn('[Index] 图片加载失败:', this.data.dishes[index]?.dish_name)
  }
})
