<template>
  <div class="dish-overview">
    <div class="page-header">
      <h2 class="page-title">菜品信息总览</h2>
      <el-tag type="info" size="large">共 {{ pagination.total }} 道菜品</el-tag>
    </div>

    <!-- 搜索与筛选 -->
    <el-card shadow="never" class="search-card">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索菜品名称/分类/档口"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterCategory" placeholder="按分类筛选" clearable @change="handleSearch" style="width: 100%">
            <el-option
              v-for="cat in categoryOptions"
              :key="cat"
              :label="cat"
              :value="cat"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 卡片视图 -->
    <el-card shadow="never" class="table-card">
      <div v-loading="loading" class="dish-grid">
        <el-card
          v-for="dish in tableData"
          :key="dish.dish_id"
          shadow="hover"
          class="dish-card"
        >
          <div class="dish-image-placeholder">
            <el-icon :size="48" color="#c0c4cc"><PictureFilled /></el-icon>
          </div>
          <div class="dish-info">
            <h4 class="dish-name">{{ dish.dish_name }}</h4>
            <div class="dish-meta">
              <el-tag size="small" type="warning">{{ dish.category }}</el-tag>
              <span class="dish-price">&yen;{{ dish.price }}</span>
            </div>
            <div class="dish-stall">
              <el-icon><Shop /></el-icon>
              <span>{{ dish.merchant_name }} - {{ dish.stall_name }}</span>
            </div>
          </div>
        </el-card>

        <el-empty v-if="!loading && tableData.length === 0" description="暂无数据" />
      </div>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[6, 12, 24, 48]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '@/api'

const searchKeyword = ref('')
const filterCategory = ref('')
const tableData = ref([])
const loading = ref(false)

const categoryOptions = ['川菜', '粤菜', '面食', '铁板烧', '日料', '烧烤', '小吃', '饮品']

const pagination = reactive({
  page: 1,
  pageSize: 12,
  total: 0,
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.getDishes({
      keyword: searchKeyword.value,
      category: filterCategory.value,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data.list
      pagination.total = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchKeyword.value = ''
  filterCategory.value = ''
  pagination.page = 1
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  margin-bottom: 16px;
}

.dish-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  min-height: 120px;
}

.dish-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.dish-card:hover {
  transform: translateY(-4px);
}

.dish-image-placeholder {
  height: 140px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  margin-bottom: 12px;
}

.dish-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px;
}

.dish-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.dish-price {
  font-size: 18px;
  font-weight: 700;
  color: #F56C6C;
}

.dish-stall {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
