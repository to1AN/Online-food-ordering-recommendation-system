/**
 * 个人中心 — 登录/收藏/历史/评分
 * 成员A：用户认证与互动模块
 */

const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    isLoggedIn: false,
    userInfo: null,

    // 统计
    favoriteCount: 0,
    historyCount: 0,
    ratedCount: 0,

    // Tab 切换
    activeTab: 'favorites', // favorites | history | ratings

    // 收藏列表
    favorites: [],
    favoritesLoading: false,

    // 历史列表
    histories: [],
    historiesLoading: false,

    // 评分列表
    ratings: [],
    ratingsLoading: false,

    // 编辑昵称
    showNicknameEdit: false,
    nicknameInput: '',

    // 重新评分
    showRatingPopup: false,
    ratingDish: null,
    ratingValue: 0,

    // 分页
    page: 1,
    pageSize: 10,
    hasMore: true
  },

  onLoad() {
    // 页面首次加载
  },

  onShow() {
    this.syncLoginState()
    if (this.data.isLoggedIn) {
      this.loadCurrentTab()
      this.refreshProfile()
    }
  },

  /**
   * 同步登录状态
   */
  syncLoginState() {
    const isLoggedIn = app.globalData.isLoggedIn || false
    const userInfo = app.globalData.userInfo || null
    this.setData({ isLoggedIn, userInfo })
  },

  /**
   * 刷新个人信息（从服务端获取最新数据）
   */
  async refreshProfile() {
    try {
      const profile = await api.getProfile(app.globalData.userId)
      if (profile) {
        const userInfo = {
          userId: profile.userId,
          username: profile.username,
          avatar: profile.avatar
        }
        app.globalData.userInfo = userInfo
        wx.setStorageSync('userInfo', userInfo)
        this.setData({ userInfo })
      }
    } catch (e) {
      // silent fail
    }
  },

  /**
   * 微信一键登录
   */
  onLogin() {
    wx.login({
      success: (res) => {
        if (res.code) {
          api.login(res.code).then((data) => {
            const { token, user } = data
            const userInfo = {
              userId: user.userId,
              username: user.username,
              avatar: user.avatar
            }
            app.setLoginState(token, userInfo)
            this.setData({ isLoggedIn: true, userInfo })
            wx.showToast({ title: '登录成功', icon: 'success' })
            this.loadCurrentTab()
          }).catch(() => {
            wx.showToast({ title: '登录失败，请重试', icon: 'none' })
          })
        } else {
          wx.showToast({ title: '获取微信授权失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.showToast({ title: '微信登录不可用', icon: 'none' })
      }
    })
  },

  /**
   * 退出登录
   */
  onLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          api.logout().finally(() => {
            app.clearLoginState()
            this.setData({
              isLoggedIn: false,
              userInfo: null,
              favorites: [],
              histories: [],
              ratings: [],
              favoriteCount: 0,
              historyCount: 0,
              ratedCount: 0
            })
            wx.showToast({ title: '已退出', icon: 'success' })
          })
        }
      }
    })
  },

  // ==================== 编辑昵称 ====================

  onEditNickname() {
    this.setData({
      showNicknameEdit: true,
      nicknameInput: this.data.userInfo?.username || ''
    })
  },

  onNicknameInput(e) {
    this.setData({ nicknameInput: e.detail.value })
  },

  onNicknameConfirm() {
    const username = this.data.nicknameInput.trim()
    if (!username) {
      wx.showToast({ title: '昵称不能为空', icon: 'none' })
      return
    }
    api.updateProfile(app.globalData.userId, { username }).then(() => {
      const userInfo = { ...this.data.userInfo, username }
      app.globalData.userInfo = userInfo
      wx.setStorageSync('userInfo', userInfo)
      this.setData({ userInfo, showNicknameEdit: false })
      wx.showToast({ title: '昵称已更新', icon: 'success' })
    }).catch(() => {
      wx.showToast({ title: '更新失败', icon: 'none' })
    })
  },

  onNicknameCancel() {
    this.setData({ showNicknameEdit: false })
  },

  // ==================== Tab 切换 ====================

  onTabChange(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab }, () => {
      this.loadCurrentTab()
    })
  },

  loadCurrentTab() {
    switch (this.data.activeTab) {
      case 'favorites':
        this.loadFavorites()
        break
      case 'history':
        this.loadHistory()
        break
      case 'ratings':
        this.loadRatings()
        break
    }
  },

  // ==================== 收藏列表 ====================

  async loadFavorites() {
    if (this.data.favoritesLoading) return
    this.setData({ favoritesLoading: true })
    try {
      const data = await api.getFavorites(app.globalData.userId, 1, 100)
      const favorites = data.list || data || []
      this.setData({
        favorites: favorites,
        favoriteCount: favorites.length,
        favoritesLoading: false
      })
    } catch (e) {
      this.setData({ favoritesLoading: false })
    }
  },

  onRemoveFavorite(e) {
    const dish = e.currentTarget.dataset.dish
    wx.showModal({
      title: '提示',
      content: `确定取消收藏「${dish.dish_name}」吗？`,
      success: (res) => {
        if (res.confirm) {
          api.removeFavorite(app.globalData.userId, dish.dish_id).then(() => {
            wx.showToast({ title: '已取消收藏', icon: 'success' })
            this.loadFavorites()
          }).catch(() => {
            wx.showToast({ title: '操作失败', icon: 'none' })
          })
        }
      }
    })
  },

  // ==================== 历史列表 ====================

  async loadHistory() {
    if (this.data.historiesLoading) return
    this.setData({ historiesLoading: true })
    try {
      const data = await api.getHistory(app.globalData.userId, 1, 100)
      const histories = data.list || data || []
      this.setData({
        histories: histories,
        historyCount: histories.length,
        ratedCount: histories.filter(h => h.score && h.score > 0).length,
        historiesLoading: false
      })
    } catch (e) {
      this.setData({ historiesLoading: false })
    }
  },

  /**
   * 点赞 / 取消点赞
   */
  onToggleLike(e) {
    const dish = e.currentTarget.dataset.dish
    const newStatus = !dish.like_status
    wx.showToast({ title: newStatus ? '已点赞' : '已取消点赞', icon: 'success', duration: 1000 })
    api.toggleLike(app.globalData.userId, dish.dish_id, newStatus)
      .then(() => this.loadHistory())
      .catch(() => wx.showToast({ title: '操作失败', icon: 'none' }))
  },

  // ==================== 评分列表 ====================

  async loadRatings() {
    if (this.data.ratingsLoading) return
    this.setData({ ratingsLoading: true })
    try {
      const data = await api.getHistory(app.globalData.userId, 1, 100)
      const all = data.list || data || []
      const ratings = all.filter(h => h.score && h.score > 0)
      this.setData({
        ratings: ratings,
        ratedCount: ratings.length,
        ratingsLoading: false
      })
    } catch (e) {
      this.setData({ ratingsLoading: false })
    }
  },

  onOpenRating(e) {
    const dish = e.currentTarget.dataset.dish
    this.setData({
      showRatingPopup: true,
      ratingDish: dish,
      ratingValue: dish.score || 0
    })
  },

  onStarTap(e) {
    const value = parseInt(e.currentTarget.dataset.value)
    this.setData({ ratingValue: value })
  },

  onRatingConfirm() {
    const { ratingDish, ratingValue } = this.data
    if (ratingValue === 0) {
      wx.showToast({ title: '请选择评分', icon: 'none' })
      return
    }
    api.addHistory(app.globalData.userId, ratingDish.dish_id, ratingValue).then(() => {
      wx.showToast({ title: '评分成功', icon: 'success' })
      this.setData({ showRatingPopup: false })
      this.loadRatings()
    }).catch(() => {
      wx.showToast({ title: '评分失败', icon: 'none' })
    })
  },

  onRatingCancel() {
    this.setData({ showRatingPopup: false })
  },

  // ==================== 导航 ====================

  onDishTap(e) {
    const dish = e.currentTarget.dataset.dish
    app.globalData.dishCache = dish
    wx.navigateTo({
      url: `/pages/dish-detail/dish-detail?dishId=${dish.dish_id}&dishName=${encodeURIComponent(dish.dish_name)}`
    })
  },

  // ==================== 头像加载失败兜底 ====================

  onAvatarError() {
    // default fallback — handled by wxml conditional rendering
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ page: this.data.page + 1 })
      this.loadMore()
    }
  },

  async loadMore() {
    const { activeTab } = this.data
    if (activeTab === 'favorites') {
      this.setData({ hasMore: false })
    } else if (activeTab === 'history') {
      this.setData({ hasMore: false })
    }
  }
})
