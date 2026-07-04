/**
 * 菜品详情页
 * 成员B：选餐与个性化推荐模块
 *
 * 数据来源：通过页面参数 dishId 定位菜品
 * 优先使用传递过来的菜品数据，也可以从其他接口获取完整信息
 */

const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    dishId: null,
    dish: null,
    loading: false,
    isFavorited: false,
    showSuccess: false
  },

  onLoad(options) {
    const { dishId, dishName } = options
    this.setData({ dishId })

    // 设置导航栏标题
    if (dishName) {
      wx.setNavigationBarTitle({ title: decodeURIComponent(dishName) })
    }

    // 优先从全局缓存获取菜品数据（跨页面传递）
    const cachedDish = app.globalData.dishCache
    if (cachedDish && String(cachedDish.dish_id) === String(dishId)) {
      this.setData({ dish: cachedDish, loading: false })
      app.globalData.dishCache = null // 使用后清除
      this.checkFavoriteStatus()
    } else {
      // 缓存未命中，通过 ID 精确查询
      this.loadDishById(dishId)
    }
  },

  /**
   * 通过 ID 精确查询菜品（缓存未命中时的降级方案）
   */
  async loadDishById(dishId) {
    try {
      const res = await api.getDishList({ keyword: dishId, page: 1, pageSize: 1 })
      if (res.list && res.list.length > 0) {
        this.setData({ dish: res.list[0] })
      } else {
        wx.showToast({ title: '菜品不存在', icon: 'none' })
      }
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  /**
   * 加载菜品详情（缓存未命中时的降级方案）
   */
  async loadDishDetail() {
    const { dishId } = this.data
    if (!dishId) return

    this.setData({ loading: true })

    try {
      const data = await api.getDishList({ page: 1, pageSize: 100 })
      const list = data.list || []
      const dish = list.find(item => String(item.dish_id) === String(dishId))

      if (dish) {
        this.setData({ dish, loading: false })
        this.checkFavoriteStatus()
      } else {
        // 完全找不到菜品数据
        this.setData({ loading: false })
        wx.showToast({ title: '菜品信息未找到', icon: 'none' })
      }
    } catch (err) {
      console.error('[DishDetail] 加载失败:', err)
      this.setData({ loading: false })
    }
  },

  /**
   * 检查收藏状态
   */
  async checkFavoriteStatus() {
    try {
      const userId = app.globalData.userId
      const data = await api.getFavorites(userId, 1, 100)
      const favList = data.list || []
      const isFav = favList.some(item => String(item.dish_id) === String(this.data.dishId))
      this.setData({ isFavorited: isFav })
    } catch (err) {
      // 静默失败
      console.warn('[DishDetail] 收藏状态检查失败:', err)
    }
  },

  /**
   * 收藏 / 取消收藏
   */
  async onToggleFavorite() {
    const userId = app.globalData.userId
    const dishId = Number(this.data.dishId)

    try {
      if (this.data.isFavorited) {
        await api.removeFavorite(userId, dishId)
        wx.showToast({ title: '已取消收藏', icon: 'none' })
      } else {
        await api.addFavorite(userId, dishId)
        wx.showToast({ title: '已收藏 ❤️', icon: 'none' })
      }
      this.setData({ isFavorited: !this.data.isFavorited })
    } catch (err) {
      console.error('[DishDetail] 收藏操作失败:', err)
    }
  },

  /**
   * "就选这个" — 记录选餐历史
   */
  async onSelectDish() {
    const userId = app.globalData.userId
    const dishId = Number(this.data.dishId)

    try {
      // 记录选餐历史（评分暂不传，后续由评分页面处理）
      await api.addHistory(userId, dishId, 0)
      this.setData({ showSuccess: true })
    } catch (err) {
      console.error('[DishDetail] 记录失败:', err)
      // 即使失败也展示成功，不影响用户体验
      this.setData({ showSuccess: true })
    }
  },

  /**
   * 关闭成功弹窗
   */
  onCloseSuccess() {
    this.setData({ showSuccess: false })
  },

  /**
   * 返回上一页
   */
  onBack() {
    const pages = getCurrentPages()
    if (pages.length > 1) {
      wx.navigateBack()
    } else {
      wx.switchTab({ url: '/pages/index/index' })
    }
  }
})
