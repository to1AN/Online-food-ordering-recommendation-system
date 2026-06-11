<template>
  <el-card shadow="never" class="data-table-card">
    <template #header v-if="$slots.header">
      <div class="card-header">
        <slot name="header" />
      </div>
    </template>

    <el-table
      :data="data"
      stripe
      border
      v-loading="loading"
      :header-cell-style="{ background: '#f5f7fa', color: '#1a1a2e', fontWeight: 600 }"
      @selection-change="onSelect"
      v-bind="$attrs"
    >
      <el-table-column v-if="selectable" type="selection" width="50" align="center" />
      <slot />
    </el-table>

    <el-empty v-if="!loading && data.length === 0" description="暂无数据" />

    <div class="pagination-wrapper" v-if="showPagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="currentPageSize"
        :page-sizes="pageSizes"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="$emit('page-change')"
        @current-change="$emit('page-change')"
      />
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  pageSizes: { type: Array, default: () => [5, 10, 20, 50] },
  showPagination: { type: Boolean, default: true },
  selectable: { type: Boolean, default: false },
})

const emit = defineEmits(['update:page', 'update:pageSize', 'page-change', 'selection-change'])

const currentPage = computed({
  get: () => props.page,
  set: (v) => emit('update:page', v),
})

const currentPageSize = computed({
  get: () => props.pageSize,
  set: (v) => emit('update:pageSize', v),
})

const onSelect = (rows) => {
  emit('selection-change', rows)
}
</script>

<style scoped>
.data-table-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
