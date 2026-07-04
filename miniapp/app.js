/**
 * 网上点餐推荐系统 - 小程序入口
 * 成员B：选餐与个性化推荐模块
 */
App({
  onLaunch() {
    // 获取系统信息
    const systemInfo = wx.getSystemInfoSync()
    this.globalData.systemInfo = systemInfo
    this.globalData.statusBarHeight = systemInfo.statusBarHeight

    // 恢复登录状态
    const token = wx.getStorageSync('token')
    const userInfo = wx.getStorageSync('userInfo')
    if (token && userInfo) {
      this.globalData.token = token
      this.globalData.userId = userInfo.userId || null
      this.globalData.userInfo = userInfo
      this.globalData.isLoggedIn = true
    }

    console.log('[App] 小程序启动', systemInfo.model)
  },

  /**
   * 设置登录状态
   */
  setLoginState(token, userInfo) {
    this.globalData.token = token
    this.globalData.userId = userInfo.userId || null
    this.globalData.userInfo = userInfo
    this.globalData.isLoggedIn = true
    wx.setStorageSync('token', token)
    wx.setStorageSync('userInfo', userInfo)
  },

  /**
   * 清除登录状态
   */
  clearLoginState() {
    this.globalData.token = null
    this.globalData.userId = null
    this.globalData.userInfo = null
    this.globalData.isLoggedIn = false
    wx.removeStorageSync('token')
    wx.removeStorageSync('userInfo')
  },

  /**
   * 全局数据
   */
  globalData: {
    token: null,         // 登录令牌
    userId: null,        // 用户ID（登录后填充）
    userInfo: null,      // 用户信息（登录后填充）
    isLoggedIn: false,   // 是否已登录
    systemInfo: null,    // 系统信息
    statusBarHeight: 0,  // 状态栏高度
    dishCache: null      // 跨页面菜品数据缓存（导航到详情页前设置）
  }
})
