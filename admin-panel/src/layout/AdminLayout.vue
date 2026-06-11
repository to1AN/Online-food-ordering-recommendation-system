<template>
  <el-container class="admin-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="admin-aside">
      <div class="logo-area">
        <div class="logo-icon">
          <el-icon :size="20" color="#fff"><Dish /></el-icon>
        </div>
        <span v-show="!isCollapse" class="logo-title">点餐管理系统</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="transparent"
        text-color="#a8b2d1"
        active-text-color="#667eea"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>数据总览</template>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><UserFilled /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/merchants">
          <el-icon><Shop /></el-icon>
          <template #title>商户管理</template>
        </el-menu-item>
        <el-menu-item index="/stalls">
          <el-icon><Grid /></el-icon>
          <template #title>档口信息总览</template>
        </el-menu-item>
        <el-menu-item index="/dishes">
          <el-icon><Dish /></el-icon>
          <template #title>菜品信息总览</template>
        </el-menu-item>
        <el-menu-item index="/data-query">
          <el-icon><Search /></el-icon>
          <template #title>全局数据查询</template>
        </el-menu-item>
        <el-menu-item index="/database">
          <el-icon><Coin /></el-icon>
          <template #title>数据库维护</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主体 -->
    <el-container>
      <!-- 顶栏 -->
      <el-header class="admin-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse" :size="20">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <span class="header-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-badge :value="3" class="notice-badge">
            <el-icon :size="20"><Bell /></el-icon>
          </el-badge>
          <el-dropdown trigger="click">
            <span class="admin-avatar">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="admin-name">管理员</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <el-icon><UserFilled /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item>
                  <el-icon><Setting /></el-icon>系统设置
                </el-dropdown-item>
                <el-dropdown-item divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '管理后台')
</script>

<style scoped>
.admin-container {
  height: 100vh;
}

.admin-aside {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 40%, #0f3460 100%);
  overflow: hidden;
  transition: width 0.3s;
}

.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-icon {
  width: 34px;
  height: 34px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-title {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  margin-left: 10px;
  white-space: nowrap;
}

.el-menu {
  border-right: none;
}

.el-menu-item {
  font-size: 14px;
}

.el-menu-item.is-active {
  background: rgba(102, 126, 234, 0.12) !important;
  border-left: 3px solid #667eea;
  color: #667eea !important;
}

.el-menu-item:not(.is-active):hover {
  background: rgba(255, 255, 255, 0.04) !important;
}

.admin-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  padding: 0 24px;
  z-index: 10;
  height: 56px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.collapse-btn {
  cursor: pointer;
  color: #606266;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #667eea;
}

.header-title {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a2e;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notice-badge {
  cursor: pointer;
}

.notice-badge :deep(.el-badge__content) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
}

.admin-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #606266;
}

.admin-name {
  font-size: 14px;
}

.admin-main {
  background: #f0f2f5;
  padding: 20px 24px;
  overflow-y: auto;
}
</style>
