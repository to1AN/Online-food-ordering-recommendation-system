<template>
  <div class="stall-overview">
    <PageHeader title="档口信息总览">
      <template #actions>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增档口
        </el-button>
      </template>
    </PageHeader>

    <SearchBar
      :model="searchForm"
      :filters="searchFilters"
      keyword-placeholder="搜索档口名称、位置或商户"
      @search="handleSearch"
      @reset="handleReset"
    />

    <DataTable
      :data="tableData"
      :loading="loading"
      :total="pagination.total"
      v-model:page="pagination.page"
      v-model:page-size="pagination.pageSize"
      @page-change="fetchData"
    >
      <el-table-column prop="stall_id" label="档口ID" width="80" align="center" />
      <el-table-column prop="stall_name" label="档口名称" min-width="140" />
      <el-table-column prop="location" label="所在位置" width="120" align="center" />
      <el-table-column prop="merchant_name" label="所属商户" min-width="140" />
      <el-table-column label="菜品数量" width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small" type="warning" style="cursor: pointer" @click="viewDishes(row)">
            {{ row.dish_count ?? 0 }} 道
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="运营状态" width="110" align="center">
        <template #default="{ row }">
          <StatusTag
            :status="row.status === 'open' ? 'open' : 'closed'"
            :text="row.status === 'open' ? '营业中' : '休息中'"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleView(row)">
            <el-icon><View /></el-icon>详情
          </el-button>
          <el-button type="success" link size="small" @click="handleEdit(row)">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
          <ConfirmAction
            :title="`确定要删除「${row.stall_name}」吗？关联的 ${row.dish_count ?? 0} 道菜品也将受影响`"
            @confirm="handleDelete(row.stall_id)"
          />
        </template>
      </el-table-column>
    </DataTable>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="档口详情" width="600px">
      <template v-if="currentStall">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="档口ID">{{ currentStall.stall_id }}</el-descriptions-item>
          <el-descriptions-item label="档口名称">{{ currentStall.stall_name }}</el-descriptions-item>
          <el-descriptions-item label="所在位置">{{ currentStall.location }}</el-descriptions-item>
          <el-descriptions-item label="所属商户">{{ currentStall.merchant_name }}</el-descriptions-item>
          <el-descriptions-item label="菜品数量">{{ currentStall.dish_count ?? 0 }} 道</el-descriptions-item>
          <el-descriptions-item label="运营状态">
            <StatusTag :status="currentStall.status === 'open' ? 'open' : 'closed'" :text="currentStall.status === 'open' ? '营业中' : '休息中'" />
          </el-descriptions-item>
        </el-descriptions>
        <h4 style="margin-top: 16px; margin-bottom: 8px;">菜品列表</h4>
        <el-table :data="stallDishes" size="small" max-height="200" v-loading="loadingDishes">
          <el-table-column prop="dish_name" label="菜品名称" />
          <el-table-column prop="price" label="价格" width="100">
            <template #default="{ row }">&yen;{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="category" label="分类" width="100" />
        </el-table>
        <el-empty v-if="!loadingDishes && stallDishes.length === 0" description="暂无菜品" :image-size="60" />
      </template>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <FormDialog
      v-model:visible="dialogVisible"
      :is-edit="isEdit"
      entity-name="档口"
      :form="formData"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default>
        <el-form-item label="档口名称" prop="stall_name">
          <el-input v-model="formData.stall_name" placeholder="请输入档口名称" />
        </el-form-item>
        <el-form-item label="所在位置" prop="location">
          <el-input v-model="formData.location" placeholder="如：一楼A区" />
        </el-form-item>
        <el-form-item label="所属商户" prop="merchant_id">
          <el-select v-model="formData.merchant_id" placeholder="请选择商户" style="width: 100%">
            <el-option v-for="m in merchantOptions" :key="m.merchant_id" :label="m.merchant_name" :value="m.merchant_id" />
          </el-select>
        </el-form-item>
        <el-form-item label="运营状态" prop="status">
          <el-switch v-model="formData.status" active-value="open" inactive-value="closed" active-text="营业中" inactive-text="休息中" />
        </el-form-item>
      </template>
    </FormDialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'
import PageHeader from '@/components/PageHeader.vue'
import SearchBar from '@/components/SearchBar.vue'
import DataTable from '@/components/DataTable.vue'
import FormDialog from '@/components/FormDialog.vue'
import StatusTag from '@/components/StatusTag.vue'
import ConfirmAction from '@/components/ConfirmAction.vue'

const route = useRoute()
const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const currentStall = ref(null)
const stallDishes = ref([])
const loadingDishes = ref(false)

const searchForm = reactive({ keyword: '', merchant: '' })

const searchFilters = [
  { prop: 'merchant', type: 'select', placeholder: '按商户筛选', options: [
    { label: '美味轩餐饮', value: '美味轩餐饮' },
    { label: '香满楼饮食', value: '香满楼饮食' },
    { label: '聚福阁餐厅', value: '聚福阁餐厅' },
    { label: '御膳坊', value: '御膳坊' },
    { label: '百味园食品', value: '百味园食品' },
  ]},
]

const merchantOptions = ref([])

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const formData = reactive({
  stall_id: null,
  stall_name: '',
  location: '',
  merchant_id: null,
  status: 'open',
})

const formRules = {
  stall_name: [{ required: true, message: '请输入档口名称', trigger: 'blur' }],
  location: [{ required: true, message: '请输入所在位置', trigger: 'blur' }],
  merchant_id: [{ required: true, message: '请选择所属商户', trigger: 'change' }],
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.getStalls({
      keyword: searchForm.keyword,
      merchant: searchForm.merchant,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data.list
      pagination.total = res.data.total
    }
  } finally { loading.value = false }
}

const fetchMerchants = async () => {
  try {
    const res = await api.getMerchants({ pageSize: 100 })
    if (res.code === 200) merchantOptions.value = res.data.list
  } catch { /* use defaults */ }
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.merchant = ''
  pagination.page = 1
  fetchData()
}

const handleView = async (row) => {
  currentStall.value = row
  detailVisible.value = true
  loadingDishes.value = true
  try {
    const res = await api.getStallDishes(row.stall_id)
    if (res.code === 200) stallDishes.value = res.data
  } catch { stallDishes.value = [] }
  finally { loadingDishes.value = false }
}

const viewDishes = (row) => { handleView(row) }

const handleAdd = () => {
  isEdit.value = false
  formData.stall_id = null
  formData.stall_name = ''
  formData.location = ''
  formData.merchant_id = null
  formData.status = 'open'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  formData.stall_id = row.stall_id
  formData.stall_name = row.stall_name
  formData.location = row.location
  formData.merchant_id = row.merchant_id
  formData.status = row.status || 'open'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    const data = {
      stall_name: formData.stall_name,
      location: formData.location,
      merchant_id: formData.merchant_id,
      status: formData.status,
    }
    const res = isEdit.value
      ? await api.updateStall({ stall_id: formData.stall_id, ...data })
      : await api.addStall(data)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      fetchData()
    }
  } finally { submitLoading.value = false }
}

const handleDelete = async (id) => {
  const res = await api.deleteStall(id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchData()
  }
}

onMounted(() => {
  fetchData()
  fetchMerchants()
  // 从商户管理页跳转时传入筛选
  if (route.query.merchant) {
    searchForm.merchant = route.query.merchant
  }
})
</script>

<style scoped>
.stall-overview {
  max-width: 1400px;
}
</style>
