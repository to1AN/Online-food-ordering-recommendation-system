<template>
  <div class="user-management">
    <PageHeader title="用户管理">
      <template #actions>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增用户
        </el-button>
        <el-button v-if="selectedRows.length" type="danger" @click="handleBatchDelete" plain>
          <el-icon><Delete /></el-icon>批量删除 ({{ selectedRows.length }})
        </el-button>
      </template>
    </PageHeader>

    <!-- 搜索栏 -->
    <SearchBar
      :model="searchForm"
      :filters="searchFilters"
      keyword-placeholder="搜索用户名或ID"
      @search="handleSearch"
      @reset="handleReset"
    />

    <!-- 数据表格 -->
    <DataTable
      :data="tableData"
      :loading="loading"
      :total="pagination.total"
      v-model:page="pagination.page"
      v-model:page-size="pagination.pageSize"
      selectable
      @page-change="fetchData"
      @selection-change="onSelect"
    >
      <el-table-column prop="user_id" label="用户ID" width="80" align="center" />
      <el-table-column label="头像" width="70" align="center">
        <template #default="{ row }">
          <el-avatar :size="36" :src="row.avatar" icon="UserFilled" />
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <StatusTag
            :status="row.status === 1 ? 'active' : 'disabled'"
            :text="row.status === 1 ? '正常' : '禁用'"
          />
        </template>
      </el-table-column>
      <el-table-column prop="register_time" label="注册时间" width="180" align="center" />
      <el-table-column label="操作" width="240" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleView(row)">
            <el-icon><View /></el-icon>查看
          </el-button>
          <el-button type="success" link size="small" @click="handleEdit(row)">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
          <el-button
            :type="row.status === 1 ? 'warning' : 'success'"
            link
            size="small"
            @click="handleToggleStatus(row)"
          >
            <el-icon><component :is="row.status === 1 ? 'Lock' : 'Unlock'" /></el-icon>
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <ConfirmAction
            title="确定要删除该用户吗？此操作不可恢复"
            @confirm="handleDelete(row.user_id)"
          />
        </template>
      </el-table-column>
    </DataTable>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="500px">
      <el-descriptions v-if="currentUser" :column="2" border>
        <el-descriptions-item label="用户ID">{{ currentUser.user_id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <StatusTag :status="currentUser.status === 1 ? 'active' : 'disabled'" :text="currentUser.status === 1 ? '正常' : '禁用'" />
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentUser.register_time }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <FormDialog
      v-model:visible="dialogVisible"
      :is-edit="isEdit"
      entity-name="用户"
      :form="formData"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <el-input v-model="formData.avatar" placeholder="请输入头像URL（选填）" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="状态" prop="status">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" active-text="正常" inactive-text="禁用" />
        </el-form-item>
      </template>
    </FormDialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'
import PageHeader from '@/components/PageHeader.vue'
import SearchBar from '@/components/SearchBar.vue'
import DataTable from '@/components/DataTable.vue'
import FormDialog from '@/components/FormDialog.vue'
import StatusTag from '@/components/StatusTag.vue'
import ConfirmAction from '@/components/ConfirmAction.vue'

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const currentUser = ref(null)
const selectedRows = ref([])

const searchForm = reactive({ keyword: '', status: '', dateRange: null })

const searchFilters = [
  { prop: 'status', type: 'select', placeholder: '用户状态', options: [
    { label: '正常', value: 1 },
    { label: '禁用', value: 0 },
  ]},
]

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const formData = reactive({
  user_id: null,
  username: '',
  password: '',
  avatar: '',
  status: 1,
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur', min: 6, max: 20 }],
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.getUsers({
      keyword: searchForm.keyword,
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
  searchForm.status = ''
  searchForm.dateRange = null
  pagination.page = 1
  fetchData()
}

const onSelect = (rows) => { selectedRows.value = rows }

const handleView = (row) => { currentUser.value = row; detailVisible.value = true }

const handleAdd = () => {
  isEdit.value = false
  formData.user_id = null
  formData.username = ''
  formData.password = ''
  formData.avatar = ''
  formData.status = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  formData.user_id = row.user_id
  formData.username = row.username
  formData.password = ''
  formData.avatar = row.avatar || ''
  formData.status = row.status ?? 1
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    const data = { ...formData }
    if (isEdit.value && !data.password) delete data.password
    const res = isEdit.value ? await api.updateUser(data) : await api.addUser(data)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      fetchData()
    }
  } finally { submitLoading.value = false }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const res = await api.updateUserStatus(row.user_id, newStatus)
  if (res.code === 200) {
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
    fetchData()
  }
}

const handleDelete = async (id) => {
  const res = await api.deleteUser(id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchData()
  }
}

const handleBatchDelete = async () => {
  const ids = selectedRows.value.map(r => r.user_id)
  const res = await api.batchDeleteUsers(ids)
  if (res.code === 200) {
    ElMessage.success(`已删除 ${ids.length} 个用户`)
    selectedRows.value = []
    fetchData()
  }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.user-management {
  max-width: 1400px;
}
</style>
