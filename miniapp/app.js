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

    console.log('[App] 小程序启动', systemInfo.model)
  },

  /**
   * 全局数据
   * userId 当前硬编码为 1，登录功能由成员A开发中
   */
  globalData: {
    userId: 1,           // Mock 用户ID
    userInfo: null,      // 用户信息（登录后填充）
    systemInfo: null,    // 系统信息
    statusBarHeight: 0,  // 状态栏高度
    dishCache: null      // 跨页面菜品数据缓存（导航到详情页前设置）
  }
})
