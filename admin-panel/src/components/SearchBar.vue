<template>
  <el-card shadow="never" class="search-bar">
    <el-form :model="model" inline>
      <el-form-item v-if="showKeyword" label="">
        <el-input
          v-model="model.keyword"
          :placeholder="keywordPlaceholder"
          clearable
          @keyup.enter="$emit('search')"
          @clear="$emit('search')"
          style="width: 220px"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </el-form-item>

      <el-form-item v-for="filter in filters" :key="filter.prop" label="">
        <component
          :is="filter.type === 'select' ? 'el-select' : 'el-date-picker'"
          v-model="model[filter.prop]"
          v-bind="filter.attrs || {}"
          :placeholder="filter.placeholder"
          :clearable="true"
          @change="$emit('search')"
          style="width: 160px"
        >
          <el-option
            v-for="opt in filter.options"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </component>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="$emit('search')">
          <el-icon><Search /></el-icon>搜索
        </el-button>
        <el-button @click="$emit('reset')">
          <el-icon><RefreshRight /></el-icon>重置
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
defineProps({
  model: { type: Object, required: true },
  showKeyword: { type: Boolean, default: true },
  keywordPlaceholder: { type: String, default: '输入关键词搜索...' },
  filters: { type: Array, default: () => [] },
})

defineEmits(['search', 'reset'])
</script>

<style scoped>
.search-bar {
  margin-bottom: 16px;
}

.search-bar :deep(.el-form) {
  display: flex;
  flex-wrap: wrap;
}

.search-bar :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 8px;
}
</style>
