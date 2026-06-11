<template>
  <div class="data-query">
    <PageHeader title="全局数据查询" />

    <!-- 搜索栏 -->
    <SearchBar
      :model="searchForm"
      :filters="searchFilters"
      keyword-placeholder="输入关键词全局搜索..."
      @search="handleGlobalSearch"
      @reset="handleReset"
    >
      <template #default="{ model }">
        <!-- extra date filter -->
      </template>
    </SearchBar>

    <!-- Tab 数据视图 -->
    <el-card shadow="never" class="table-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane name="users">
          <template #label>
            <span>用户数据 <el-badge :value="userPagination.total" class="tab-badge" /></span>
          </template>
          <DataTable
            :data="userData"
            :loading="loadingUsers"
            :total="userPagination.total"
            v-model:page="userPagination.page"
            v-model:page-size="userPagination.pageSize"
            :page-sizes="[5, 10, 20]"
            @page-change="fetchUsers"
          >
            <el-table-column prop="user_id" label="用户ID" width="80" align="center" />
            <el-table-column label="头像" width="60" align="center">
              <template #default="{ row }">
                <el-avatar :size="30" :src="row.avatar" icon="UserFilled" />
              </template>
            </el-table-column>
            <el-table-column prop="username" label="用户名" min-width="120" />
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <StatusTag :status="row.status === 1 ? 'active' : 'disabled'" :text="row.status === 1 ? '正常' : '禁用'" />
              </template>
            </el-table-column>
            <el-table-column prop="register_time" label="注册时间" width="170" align="center" />
          </DataTable>
        </el-tab-pane>

        <el-tab-pane name="dishes">
          <template #label>
            <span>菜品数据 <el-badge :value="dishPagination.total" class="tab-badge" /></span>
          </template>
          <DataTable
            :data="dishData"
            :loading="loadingDishes"
            :total="dishPagination.total"
            v-model:page="dishPagination.page"
            v-model:page-size="dishPagination.pageSize"
            :page-sizes="[5, 10, 20]"
            @page-change="fetchDishes"
          >
            <el-table-column prop="dish_id" label="菜品ID" width="80" align="center" />
            <el-table-column prop="dish_name" label="菜品名称" min-width="140" />
            <el-table-column prop="price" label="价格" width="100" align="center">
              <template #default="{ row }">&yen;{{ row.price }}</template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="100" align="center">
              <template #default="{ row }"><el-tag size="small">{{ row.category }}</el-tag></template>
            </el-table-column>
            <el-table-column label="审核状态" width="100" align="center">
              <template #default="{ row }">
                <StatusTag :status="row.audit_status || 'approved'" :text="auditStatusText[row.audit_status] || '已通过'" />
              </template>
            </el-table-column>
            <el-table-column prop="merchant_name" label="所属商户" min-width="120" />
            <el-table-column prop="stall_name" label="所属档口" min-width="120" />
          </DataTable>
        </el-tab-pane>

        <el-tab-pane name="favorites">
          <template #label>
            <span>收藏记录 <el-badge :value="favoritePagination.total" class="tab-badge" /></span>
          </template>
          <DataTable
            :data="favoriteData"
            :loading="loadingFavorites"
            :total="favoritePagination.total"
            v-model:page="favoritePagination.page"
            v-model:page-size="favoritePagination.pageSize"
            :page-sizes="[5, 10, 20]"
            @page-change="fetchFavorites"
          >
            <el-table-column prop="favorite_id" label="记录ID" width="80" align="center" />
            <el-table-column prop="username" label="用户名" min-width="120" />
            <el-table-column prop="dish_name" label="菜品名称" min-width="150" />
            <el-table-column prop="favorite_time" label="收藏时间" width="180" align="center" />
          </DataTable>
        </el-tab-pane>

        <el-tab-pane name="histories">
          <template #label>
            <span>选餐历史 <el-badge :value="historyPagination.total" class="tab-badge" /></span>
          </template>
          <DataTable
            :data="historyData"
            :loading="loadingHistories"
            :total="historyPagination.total"
            v-model:page="historyPagination.page"
            v-model:page-size="historyPagination.pageSize"
            :page-sizes="[5, 10, 20]"
            @page-change="fetchHistories"
          >
            <el-table-column prop="history_id" label="记录ID" width="80" align="center" />
            <el-table-column prop="username" label="用户名" min-width="120" />
            <el-table-column prop="dish_name" label="菜品名称" min-width="150" />
            <el-table-column label="评分" width="180" align="center">
              <template #default="{ row }">
                <el-rate v-model="row.score" disabled show-score text-color="#ff9900" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="点赞" width="80" align="center">
              <template #default="{ row }">
                <StatusTag :status="row.like_status ? 'liked' : 'unliked'" :text="row.like_status ? '已赞' : '未赞'" />
              </template>
            </el-table-column>
            <el-table-column prop="select_time" label="选餐时间" width="180" align="center" />
          </DataTable>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 导出按钮 -->
    <div style="text-align: right; margin-top: 8px;">
      <el-button @click="handleExport">
        <el-icon><Download /></el-icon>导出当前数据 (CSV)
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'
import PageHeader from '@/components/PageHeader.vue'
import SearchBar from '@/components/SearchBar.vue'
import DataTable from '@/components/DataTable.vue'
import StatusTag from '@/components/StatusTag.vue'

const activeTab = ref('users')
const searchForm = reactive({ keyword: '', dateRange: null })

const searchFilters = [
  { prop: 'dateRange', type: 'el-date-picker', placeholder: '选择日期范围', attrs: { type: 'daterange', 'range-separator': '至', 'start-placeholder': '开始', 'end-placeholder': '结束', 'value-format': 'YYYY-MM-DD' } },
]

const auditStatusText = { pending: '待审核', approved: '已通过', rejected: '已拒绝' }

// 用户
const userData = ref([]); const loadingUsers = ref(false)
const userPagination = reactive({ page: 1, pageSize: 10, total: 0 })
// 菜品
const dishData = ref([]); const loadingDishes = ref(false)
const dishPagination = reactive({ page: 1, pageSize: 10, total: 0 })
// 收藏
const favoriteData = ref([]); const loadingFavorites = ref(false)
const favoritePagination = reactive({ page: 1, pageSize: 10, total: 0 })
// 历史
const historyData = ref([]); const loadingHistories = ref(false)
const historyPagination = reactive({ page: 1, pageSize: 10, total: 0 })

const fetchUsers = async () => {
  loadingUsers.value = true
  try {
    const res = await api.getUsers({
      keyword: searchForm.keyword,
      page: userPagination.page,
      pageSize: userPagination.pageSize,
      dateRange: searchForm.dateRange,
    })
    if (res.code === 200) {
      userData.value = res.data.list
      userPagination.total = res.data.total
    }
  } finally { loadingUsers.value = false }
}

const fetchDishes = async () => {
  loadingDishes.value = true
  try {
    const res = await api.getDishes({
      keyword: searchForm.keyword,
      page: dishPagination.page,
      pageSize: dishPagination.pageSize,
    })
    if (res.code === 200) {
      dishData.value = res.data.list
      dishPagination.total = res.data.total
    }
  } finally { loadingDishes.value = false }
}

const fetchFavorites = async () => {
  loadingFavorites.value = true
  try {
    const res = await api.getFavorites({
      keyword: searchForm.keyword,
      page: favoritePagination.page,
      pageSize: favoritePagination.pageSize,
    })
    if (res.code === 200) {
      favoriteData.value = res.data.list
      favoritePagination.total = res.data.total
    }
  } finally { loadingFavorites.value = false }
}

const fetchHistories = async () => {
  loadingHistories.value = true
  try {
    const res = await api.getHistories({
      keyword: searchForm.keyword,
      page: historyPagination.page,
      pageSize: historyPagination.pageSize,
      dateRange: searchForm.dateRange,
    })
    if (res.code === 200) {
      historyData.value = res.data.list
      historyPagination.total = res.data.total
    }
  } finally { loadingHistories.value = false }
}

const handleGlobalSearch = () => {
  userPagination.page = 1; dishPagination.page = 1
  favoritePagination.page = 1; historyPagination.page = 1
  handleTabChange(activeTab.value)
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.dateRange = null
  handleGlobalSearch()
}

const handleTabChange = (tabName) => {
  const fetchers = { users: fetchUsers, dishes: fetchDishes, favorites: fetchFavorites, histories: fetchHistories }
  if (fetchers[tabName]) fetchers[tabName]()
}

const handleExport = () => {
  ElMessage.info(`正在导出${activeTab.value}数据...`)
  // 实际导出请求: api.exportData(activeTab.value, searchForm)
}

onMounted(() => { fetchUsers() })
</script>

<style scoped>
.data-query {
  max-width: 1400px;
}

.table-card {
  margin-bottom: 4px;
}

.tab-badge :deep(.el-badge__content) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  font-size: 10px;
  height: 16px;
  line-height: 16px;
  padding: 0 5px;
  top: -4px;
}
</style>
