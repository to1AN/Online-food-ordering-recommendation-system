<template>
  <div class="database-maintenance">
    <PageHeader title="数据库维护" />

    <!-- 统计+操作区 -->
    <el-row :gutter="16" class="action-row">
      <el-col :span="8">
        <el-card shadow="never" class="stat-item">
          <el-statistic title="备份文件数" :value="backupList.length" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="stat-item">
          <el-statistic title="上次备份" :value="lastBackupTime || '暂无'">
            <template #suffix>
              <StatusTag v-if="lastBackupTime" status="active" text="正常" :show-dot="false" size="small" />
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <div style="display: flex; gap: 8px; justify-content: flex-end; padding-top: 12px;">
          <el-button type="primary" @click="handleCreateBackup" :loading="backupLoading">
            <el-icon><Upload /></el-icon>立即备份
          </el-button>
          <el-button type="warning" @click="autoBackupVisible = true" plain>
            <el-icon><Setting /></el-icon>自动备份设置
          </el-button>
        </div>
      </el-col>
    </el-row>

    <!-- 备份趋势图 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span>备份大小趋势</span>
          <el-button text size="small" @click="fetchBackupList">
            <el-icon><RefreshRight /></el-icon>刷新
          </el-button>
        </div>
      </template>
      <div ref="trendChartRef" class="trend-chart-box"></div>
    </el-card>

    <!-- 备份列表 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>备份文件列表</span>
          <el-button v-if="backupList.length > 3" type="danger" text size="small" @click="handleCleanup">
            <el-icon><Delete /></el-icon>清理过期备份
          </el-button>
        </div>
      </template>

      <DataTable
        :data="backupList"
        :loading="loading"
        :show-pagination="false"
      >
        <el-table-column prop="id" label="编号" width="60" align="center" />
        <el-table-column prop="filename" label="文件名" min-width="280">
          <template #default="{ row }">
            <el-icon><Document /></el-icon>
            <span style="margin-left: 6px">{{ row.filename }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="文件大小" width="120" align="center" />
        <el-table-column prop="time" label="备份时间" width="200" align="center" />
        <el-table-column label="备份类型" width="120" align="center">
          <template #default="{ row }">
            <StatusTag
              :status="row.type === '自动备份' ? 'auto' : 'manual'"
              :text="row.type"
            />
          </template>
        </el-table-column>
        <el-table-column label="校验状态" width="100" align="center">
          <template #default="{ row }">
            <StatusTag
              :status="row.verified || 'verified'"
              :text="row.verified === 'failed' ? '失败' : row.verified === 'verifying' ? '校验中' : '成功'"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              title="确定要恢复到此备份版本吗？当前数据将被覆盖"
              @confirm="handleRestore(row.id)"
            >
              <template #reference>
                <el-button type="warning" link size="small">
                  <el-icon><Refresh /></el-icon>恢复
                </el-button>
              </template>
            </el-popconfirm>
            <el-button type="primary" link size="small" @click="handleDownload(row)">
              <el-icon><Download /></el-icon>下载
            </el-button>
            <ConfirmAction
              title="确定要删除该备份文件吗？"
              @confirm="handleDeleteBackup(row.id)"
            />
          </template>
        </el-table-column>
      </DataTable>
    </el-card>

    <!-- 系统操作日志 -->
    <el-card shadow="never" class="log-card">
      <template #header>
        <div class="card-header">
          <span>系统操作日志</span>
          <span style="font-size: 12px; color: #909399;">最近 20 条</span>
        </div>
      </template>
      <el-table
        :data="systemLogs"
        stripe
        size="small"
        v-loading="loadingLogs"
        max-height="280"
        :header-cell-style="{ background: '#f5f7fa', color: '#1a1a2e' }"
      >
        <el-table-column prop="id" label="编号" width="60" align="center" />
        <el-table-column prop="action" label="操作" min-width="180" />
        <el-table-column prop="operator" label="操作人" width="120" align="center" />
        <el-table-column prop="time" label="操作时间" width="180" align="center" />
        <el-table-column label="结果" width="80" align="center">
          <template #default="{ row }">
            <StatusTag :status="row.result === 'success' ? 'active' : 'failed'" :text="row.result === 'success' ? '成功' : '失败'" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 自动备份设置弹窗 -->
    <el-dialog v-model="autoBackupVisible" title="自动备份设置" width="500px">
      <el-form label-width="100px">
        <el-form-item label="自动备份">
          <el-switch v-model="autoBackupEnabled" />
          <span style="margin-left: 8px; color: #909399;">{{ autoBackupEnabled ? '已开启' : '已关闭' }}</span>
        </el-form-item>
        <el-form-item label="备份频率">
          <el-select v-model="backupFrequency" style="width: 200px">
            <el-option label="每天" value="daily" />
            <el-option label="每周" value="weekly" />
            <el-option label="每月" value="monthly" />
          </el-select>
        </el-form-item>
        <el-form-item label="备份时间">
          <el-time-picker v-model="backupTime" format="HH:mm" placeholder="选择时间" />
        </el-form-item>
        <el-form-item label="保留份数">
          <el-input-number v-model="backupRetention" :min="1" :max="30" />
          <span style="margin-left: 8px; color: #909399;">份（超出自动清理）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="autoBackupVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAutoBackupSettings">保存设置</el-button>
      </template>
    </el-dialog>

    <!-- 清理确认弹窗 -->
    <el-dialog v-model="cleanupVisible" title="清理过期备份" width="450px">
      <p>当前共有 <strong>{{ backupList.length }}</strong> 个备份文件。</p>
      <el-form label-width="100px">
        <el-form-item label="保留最近">
          <el-input-number v-model="cleanupRetention" :min="1" :max="30" /> 份
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cleanupVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmCleanup">确认清理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { TooltipComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import api from '@/api'
import PageHeader from '@/components/PageHeader.vue'
import DataTable from '@/components/DataTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import ConfirmAction from '@/components/ConfirmAction.vue'

echarts.use([LineChart, TooltipComponent, GridComponent, CanvasRenderer])

const backupList = ref([])
const loading = ref(false)
const backupLoading = ref(false)
const loadingLogs = ref(false)
const systemLogs = ref([])
const trendChartRef = ref(null)
let trendChart = null

// 自动备份设置
const autoBackupVisible = ref(false)
const cleanupVisible = ref(false)
const autoBackupEnabled = ref(true)
const backupFrequency = ref('daily')
const backupTime = ref(new Date(2025, 0, 1, 3, 0))
const backupRetention = ref(7)
const cleanupRetention = ref(3)

const lastBackupTime = computed(() => backupList.value[0]?.time || null)

const fetchBackupList = async () => {
  loading.value = true
  try {
    const res = await api.getBackupList()
    if (res.code === 200) {
      backupList.value = res.data
      await nextTick()
      initTrendChart()
    }
  } finally { loading.value = false }
}

const fetchLogs = async () => {
  loadingLogs.value = true
  try {
    const res = await api.getSystemLogs({ limit: 20 })
    if (res.code === 200) systemLogs.value = res.data
  } finally { loadingLogs.value = false }
}

function initTrendChart() {
  if (!trendChartRef.value || backupList.value.length < 2) return
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)

  const reversed = [...backupList.value].reverse()
  const sizes = reversed.map(b => parseFloat(b.size))
  const times = reversed.map(b => b.time?.slice(5, 10) || '')

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: 8, containLabel: true },
    xAxis: { type: 'category', data: times },
    yAxis: { type: 'value', axisLabel: { formatter: '{value} MB' } },
    series: [{
      type: 'line',
      data: sizes,
      smooth: true,
      lineStyle: { color: '#667eea', width: 2 },
      itemStyle: { color: '#667eea' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(102,126,234,0.25)' },
          { offset: 1, color: 'rgba(102,126,234,0.02)' },
        ]),
      },
    }],
  })
}

const handleCreateBackup = async () => {
  backupLoading.value = true
  try {
    const res = await api.createBackup()
    if (res.code === 200) {
      ElMessage.success(res.message || '备份成功')
      await fetchBackupList()
      fetchLogs()
    }
  } finally { backupLoading.value = false }
}

const handleRestore = async (id) => {
  const res = await api.restoreBackup(id)
  if (res.code === 200) ElMessage.success(res.message || '数据恢复成功')
  fetchLogs()
}

const handleDownload = (row) => {
  window.open(`/api/admin/backups/${row.filename}/download`, '_blank')
}

const handleDeleteBackup = async (id) => {
  const res = await api.deleteBackup(id)
  if (res.code === 200) {
    ElMessage.success('备份文件已删除')
    backupList.value = backupList.value.filter(b => b.id !== id)
  }
}

const handleCleanup = () => {
  cleanupVisible.value = true
}

const confirmCleanup = async () => {
  const res = await api.cleanupBackups(cleanupRetention.value)
  if (res.code === 200) {
    ElMessage.success(`已清理过期备份，保留最近 ${cleanupRetention.value} 份`)
    cleanupVisible.value = false
    fetchBackupList()
    fetchLogs()
  }
}

const saveAutoBackupSettings = () => {
  localStorage.setItem('autoBackup_enabled', autoBackupEnabled.value)
  localStorage.setItem('autoBackup_frequency', backupFrequency.value)
  ElMessage.success('自动备份设置已保存')
  autoBackupVisible.value = false
}

onMounted(() => {
  autoBackupEnabled.value = localStorage.getItem('autoBackup_enabled') === 'true'
  backupFrequency.value = localStorage.getItem('autoBackup_frequency') || 'daily'
  fetchBackupList()
  fetchLogs()
})

onBeforeUnmount(() => {
  trendChart?.dispose()
})
</script>

<style scoped>
.database-maintenance {
  max-width: 1400px;
}

.action-row {
  margin-bottom: 16px;
}

.chart-card {
  margin-bottom: 16px;
}

.trend-chart-box {
  height: 220px;
}

.table-card {
  margin-bottom: 16px;
}

.log-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
