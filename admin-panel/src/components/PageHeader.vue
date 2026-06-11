<template>
  <div class="page-header">
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">
          <el-icon><HomeFilled /></el-icon> 首页
        </el-breadcrumb-item>
        <el-breadcrumb-item v-if="parent" :to="parent.to">{{ parent.label }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="page-header-bottom">
      <h2 class="page-title">{{ title }}</h2>
      <div class="page-actions">
        <slot name="actions" />
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  parent: { type: Object, default: null },
})
</script>

<style scoped>
.page-header {
  margin-bottom: 16px;
}

.breadcrumb {
  margin-bottom: 8px;
}

.page-header-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
  position: relative;
  padding-left: 12px;
}

.page-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, #667eea, #764ba2);
  border-radius: 2px;
}

.page-actions {
  display: flex;
  gap: 8px;
}
</style>
