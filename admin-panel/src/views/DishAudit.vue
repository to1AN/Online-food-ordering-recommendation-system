<template>
  <div class="dish-audit">
    <PageHeader title="菜品信息总览" />

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="audit-stats">
      <el-col :span="8">
        <StatCard label="菜品总数" :value="stats.total" icon="Dish" gradient="linear-gradient(135deg, #667eea, #764ba2)" />
      </el-col>
      <el-col :span="8">
        <StatCard label="分类数" :value="stats.categoryCount" icon="Grid" gradient="linear-gradient(135deg, #4facfe, #00f2fe)" />
      </el-col>
      <el-col :span="8">
        <StatCard label="平均价格" :value="'¥' + stats.avgPrice" icon="Coin" gradient="linear-gradient(135deg, #43e97b, #38f9d7)" />
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <SearchBar
      :model="searchForm"
      :filters="searchFilters"
      keyword-placeholder="搜索菜品名称"
      @search="handleSearch"
      @reset="handleReset"
    />

    <!-- 菜品卡片列表 -->
    <div v-loading="loading" class="dish-list">
      <div
        v-for="dish in tableData"
        :key="dish.dish_id"
        class="dish-card"
        @click="handleViewDetail(dish)"
      >
        <div class="dish-card-image">
          <el-image
            v-if="dish.image_url"
            :src="dish.image_url"
            fit="cover"
            style="width: 100%; height: 100%"
          >
            <template #error>
              <el-icon :size="40" color="#c0c4cc"><PictureFilled /></el-icon>
            </template>
          </el-image>
          <el-icon v-else :size="40" color="#c0c4cc"><PictureFilled /></el-icon>
        </div>
        <div class="dish-card-body">
          <div class="dish-card-top">
            <h4 class="dish-name">{{ dish.dish_name }}</h4>
            <el-tag size="small" type="warning">{{ dish.category }}</el-tag>
          </div>
          <div class="dish-meta">
            <span>🏪 {{ dish.merchant_name }} · {{ dish.stall_name }}</span>
          </div>
          <div class="dish-meta">
            <span class="dish-price">&yen;{{ dish.price }}</span>
          </div>
          <div v-if="dish.description" class="dish-desc">{{ dish.description }}</div>
        </div>
        <div v-if="dish.status === 'pending'" class="dish-card-actions">
          <el-button size="small" type="success" @click.stop="handleApprove(dish)">通过</el-button>
          <el-button size="small" type="danger" @click.stop="handleReject(dish)">拒绝</el-button>
        </div>
        <StatusTag v-else-if="dish.status"
          :status="dish.status"
          :text="dish.status === 'approved' ? '已通过' : dish.status === 'rejected' ? '已拒绝' : '待审核'" />
      </div>
    </div>

    <el-empty v-if="!loading && tableData.length === 0" description="暂无菜品数据" />

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="pagination.total > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[6, 12, 24]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="菜品详情" width="550px">
      <template v-if="currentDish">
        <div style="display: flex; gap: 16px;">
          <div style="width: 200px; height: 180px; background: #f5f7fa; border-radius: 8px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; overflow: hidden;">
            <el-image
              v-if="currentDish.image_url"
              :src="currentDish.image_url"
              fit="cover"
              style="width: 100%; height: 100%;"
            />
            <el-icon v-else :size="48" color="#c0c4cc"><PictureFilled /></el-icon>
          </div>
          <el-descriptions :column="1" border style="flex: 1;">
            <el-descriptions-item label="菜品名称">{{ currentDish.dish_name }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ currentDish.category }}</el-descriptions-item>
            <el-descriptions-item label="价格">&yen;{{ currentDish.price }}</el-descriptions-item>
            <el-descriptions-item label="所属商户">{{ currentDish.merchant_name }}</el-descriptions-item>
            <el-descriptions-item label="所属档口">{{ currentDish.stall_name }}</el-descriptions-item>
            <el-descriptions-item v-if="currentDish.description" label="描述">{{ currentDish.description }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'
import PageHeader from '@/components/PageHeader.vue'
import SearchBar from '@/components/SearchBar.vue'
import StatCard from '@/components/StatCard.vue'
import StatusTag from '@/components/StatusTag.vue'

const tableData = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const currentDish = ref(null)

const searchForm = reactive({ keyword: '', category: '', status: '' })

const searchFilters = [
  { prop: 'category', type: 'select', placeholder: '菜品分类', options: [
    { label: '川菜', value: '川菜' }, { label: '粤菜', value: '粤菜' },
    { label: '面食', value: '面食' }, { label: '铁板烧', value: '铁板烧' },
    { label: '日料', value: '日料' }, { label: '烧烤', value: '烧烤' },
    { label: '小吃', value: '小吃' }, { label: '饮品', value: '饮品' },
  ]},
  { prop: 'status', type: 'select', placeholder: '审核状态', options: [
    { label: '待审核', value: 'pending' },
    { label: '已通过', value: 'approved' },
    { label: '已拒绝', value: 'rejected' },
    { label: '全部', value: '' },
  ]},
]

const pagination = reactive({ page: 1, pageSize: 12, total: 0 })

const stats = reactive({ total: 0, categoryCount: 0, avgPrice: '0' })

const fetchStats = async () => {
    try {
        const res = await api.getDishStats()
        if (res && res.code === 200 && res.data) {
            stats.total = res.data.total
            stats.categoryCount = res.data.categoryCount
            stats.avgPrice = Number(res.data.avgPrice).toFixed(1)
        }
    } catch (e) { /* ignore */ }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.getDishes({
      keyword: searchForm.keyword,
      category: searchForm.category,
      status: searchForm.status,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data.list
      pagination.total = res.data.total
    }
  } finally { loading.value = false }
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.category = ''
  searchForm.status = ''
  pagination.page = 1
  fetchData()
}

const handleViewDetail = (dish) => {
  currentDish.value = dish
  detailVisible.value = true
}

const handleApprove = async (dish) => {
    try {
        await api.updateDishStatus(dish.dish_id, 'approved')
        ElMessage.success('已通过')
        fetchData()
    } catch (e) { ElMessage.error('操作失败') }
}

const handleReject = async (dish) => {
    try {
        await api.updateDishStatus(dish.dish_id, 'rejected')
        ElMessage.success('已拒绝')
        fetchData()
    } catch (e) { ElMessage.error('操作失败') }
}

onMounted(() => { fetchData(); fetchStats() })
</script>

<style scoped>
.dish-audit {
  max-width: 1400px;
}

.audit-stats {
  margin-bottom: 16px;
}

.dish-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 14px;
  min-height: 200px;
}

.dish-card {
  display: flex;
  gap: 14px;
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  border-left: 4px solid #667eea;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  cursor: pointer;
}

.dish-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.dish-card-image {
  width: 80px;
  height: 80px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.dish-card-body {
  flex: 1;
  min-width: 0;
}

.dish-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.dish-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 12px;
  margin-bottom: 2px;
}

.dish-price {
  color: #F56C6C;
  font-weight: 600;
  font-size: 16px;
}

.dish-desc {
  font-size: 11px;
  color: #c0c4cc;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.dish-card-actions {
    display: flex;
    gap: 8px;
    margin-top: 8px;
    justify-content: flex-end;
}
</style>
