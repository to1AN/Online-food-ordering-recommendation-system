<template>
  <div class="merchant-management">
    <PageHeader title="商户管理">
      <template #actions>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增商户
        </el-button>
      </template>
    </PageHeader>

    <SearchBar
      :model="searchForm"
      keyword-placeholder="搜索商户名称或ID"
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
      <el-table-column prop="merchant_id" label="商户ID" width="80" align="center" />
      <el-table-column label="商户Logo" width="80" align="center">
        <template #default="{ row }">
          <el-avatar :size="40" :src="row.logo" shape="square">
            <el-icon :size="20"><Shop /></el-icon>
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="merchant_name" label="商户名称" min-width="150" />
      <el-table-column prop="contact_info" label="联系方式" width="150" align="center" />
      <el-table-column label="关联档口" width="100" align="center">
        <template #default="{ row }">
          <el-tag
            :type="row.stall_count > 0 ? 'success' : 'info'"
            size="small"
            style="cursor: pointer"
            @click="goToStalls(row)"
          >
            {{ row.stall_count ?? 0 }} 个
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="create_time" label="入驻时间" width="140" align="center" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
          <ConfirmAction
            :title="`确定要删除「${row.merchant_name}」吗？关联的 ${row.stall_count ?? 0} 个档口和菜品也将受到影响`"
            @confirm="handleDelete(row.merchant_id)"
          />
        </template>
      </el-table-column>
    </DataTable>

    <FormDialog
      v-model:visible="dialogVisible"
      :is-edit="isEdit"
      entity-name="商户"
      :form="formData"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default>
        <el-form-item label="商户名称" prop="merchant_name">
          <el-input v-model="formData.merchant_name" placeholder="请输入商户名称" />
        </el-form-item>
        <el-form-item label="联系方式" prop="contact_info">
          <el-input v-model="formData.contact_info" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
      </template>
    </FormDialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'
import PageHeader from '@/components/PageHeader.vue'
import SearchBar from '@/components/SearchBar.vue'
import DataTable from '@/components/DataTable.vue'
import FormDialog from '@/components/FormDialog.vue'
import ConfirmAction from '@/components/ConfirmAction.vue'

const router = useRouter()
const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const formData = reactive({
  merchant_id: null,
  merchant_name: '',
  contact_info: '',
})

const formRules = {
  merchant_name: [{ required: true, message: '请输入商户名称', trigger: 'blur' }],
  contact_info: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.getMerchants({
      keyword: searchForm.keyword,
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
  pagination.page = 1
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  formData.merchant_id = null
  formData.merchant_name = ''
  formData.contact_info = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  formData.merchant_id = row.merchant_id
  formData.merchant_name = row.merchant_name
  formData.contact_info = row.contact_info
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    const res = isEdit.value
      ? await api.updateMerchant({ merchant_id: formData.merchant_id, merchant_name: formData.merchant_name, contact_info: formData.contact_info })
      : await api.addMerchant({ merchant_name: formData.merchant_name, contact_info: formData.contact_info })
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      fetchData()
    }
  } finally { submitLoading.value = false }
}

const handleDelete = async (id) => {
  const res = await api.deleteMerchant(id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchData()
  }
}

const goToStalls = (row) => {
  router.push({ path: '/stalls', query: { merchant: row.merchant_name } })
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.merchant-management {
  max-width: 1400px;
}
</style>
