<template>
  <div class="dashboard">
    <PageHeader title="数据总览" />

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="12" :sm="8" :lg="4" v-for="card in statCards" :key="card.label">
        <StatCard
          :label="card.label"
          :value="card.value"
          :icon="card.icon"
          :gradient="card.gradient"
          :trend="card.trend"
        />
      </el-col>
    </el-row>

    <!-- 图表第一行 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>菜品分类占比</span>
              <el-tag size="small" type="info">饼图</el-tag>
            </div>
          </template>
          <div ref="pieChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>各档口菜品数量</span>
              <el-tag size="small" type="info">柱状图</el-tag>
            </div>
          </template>
          <div ref="barChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表第二行 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>近7天选餐趋势</span>
              <el-tag size="small" type="warning">折线图</el-tag>
            </div>
          </template>
          <div ref="lineChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>评分分布</span>
              <el-tag size="small" type="info">柱状图</el-tag>
            </div>
          </template>
          <div ref="scoreChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统概览 -->
    <el-card shadow="never" class="overview-card">
      <template #header>
        <div class="chart-header">
          <span>系统概览</span>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="用户总数">
          <strong>{{ stats.totalUsers }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="商户总数">
          <strong>{{ stats.totalMerchants }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="档口总数">
          <strong>{{ stats.totalStalls }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="菜品总数">
          <strong>{{ stats.totalDishes }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="收藏总数">
          <strong>{{ stats.totalFavorites }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="选餐记录数">
          <strong>{{ stats.totalHistories }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="平均评分">
          <el-rate :model-value="stats.avgScore" disabled show-score text-color="#ff9900" size="small" />
        </el-descriptions-item>
        <el-descriptions-item label="今日选餐次数">
          <el-tag type="success" size="small">{{ stats.todaySelects }} 次</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="系统运行状态">
          <StatusTag status="active" text="运行中" />
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import * as echarts from 'echarts/core'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import api from '@/api'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import StatusTag from '@/components/StatusTag.vue'

echarts.use([PieChart, BarChart, LineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const stats = reactive({
  totalUsers: 0,
  totalMerchants: 0,
  totalStalls: 0,
  totalDishes: 0,
  totalFavorites: 0,
  totalHistories: 0,
  avgScore: 0,
  todaySelects: 0,
})

const statCards = ref([])
const pieChartRef = ref(null)
const barChartRef = ref(null)
const lineChartRef = ref(null)
const scoreChartRef = ref(null)

let pieChart = null
let barChart = null
let lineChart = null
let scoreChart = null

const GRADIENTS = [
  'linear-gradient(135deg, #667eea, #764ba2)',
  'linear-gradient(135deg, #f093fb, #f5576c)',
  'linear-gradient(135deg, #4facfe, #00f2fe)',
  'linear-gradient(135deg, #43e97b, #38f9d7)',
  'linear-gradient(135deg, #fa709a, #fee140)',
  'linear-gradient(135deg, #a18cd1, #fbc2eb)',
]

const CATEGORY_COLORS = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#F0B27A', '#82E0AA', '#FF8A5C', '#6C5CE7']

function initPieChart(data) {
  if (!pieChartRef.value) return
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { fontSize: 11 } },
    series: [{
      type: 'pie',
      radius: ['50%', '72%'],
      center: ['50%', '45%'],
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: data.map((d, i) => ({ ...d, itemStyle: { color: CATEGORY_COLORS[i % CATEGORY_COLORS.length] } })),
    }],
  })
}

function initBarChart(data) {
  if (!barChartRef.value) return
  if (!barChart) barChart = echarts.init(barChartRef.value)
  barChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '8%', bottom: '3%', top: 8, containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(d => d.name),
      axisLabel: { fontSize: 11, rotate: data.length > 6 ? 30 : 0 },
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      data: data.map((d, i) => ({
        value: d.value,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#a18cd1' },
          ]),
          borderRadius: [6, 6, 0, 0],
        },
      })),
      barWidth: '50%',
    }],
  })
}

function initLineChart(data) {
  if (!lineChartRef.value) return
  if (!lineChart) lineChart = echarts.init(lineChartRef.value)
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: 8, containLabel: true },
    xAxis: { type: 'category', data: data.dates, boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'line',
      data: data.values,
      smooth: true,
      lineStyle: { color: '#667eea', width: 2 },
      itemStyle: { color: '#667eea' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(102,126,234,0.3)' },
          { offset: 1, color: 'rgba(102,126,234,0.02)' },
        ]),
      },
      symbol: 'circle',
      symbolSize: 6,
    }],
  })
}

function initScoreChart(data) {
  if (!scoreChartRef.value) return
  if (!scoreChart) scoreChart = echarts.init(scoreChartRef.value)
  const colors = ['#F56C6C', '#E6A23C', '#E6A23C', '#409EFF', '#67C23A']
  scoreChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: 8, containLabel: true },
    xAxis: { type: 'category', data: ['1星', '2星', '3星', '4星', '5星'] },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      data: data.map((v, i) => ({
        value: v,
        itemStyle: { color: colors[i], borderRadius: [6, 6, 0, 0] },
      })),
      barWidth: '50%',
    }],
  })
}

function resizeAll() {
  pieChart?.resize()
  barChart?.resize()
  lineChart?.resize()
  scoreChart?.resize()
}

let dashboardResult = null

onMounted(async () => {
  try {
    const res = await api.getDashboardStats()
    if (res.code === 200) {
      dashboardResult = res // store for chart use below
      Object.assign(stats, res.data)
      statCards.value = [
        { label: '用户总数', value: stats.totalUsers, icon: 'UserFilled', gradient: GRADIENTS[0], trend: 12 },
        { label: '商户总数', value: stats.totalMerchants, icon: 'Shop', gradient: GRADIENTS[1], trend: 0 },
        { label: '档口总数', value: stats.totalStalls, icon: 'Grid', gradient: GRADIENTS[2], trend: 5 },
        { label: '菜品总数', value: stats.totalDishes, icon: 'Dish', gradient: GRADIENTS[3], trend: 8 },
        { label: '收藏总数', value: stats.totalFavorites, icon: 'StarFilled', gradient: GRADIENTS[4], trend: -3 },
        { label: '选餐记录', value: stats.totalHistories, icon: 'Tickets', gradient: GRADIENTS[5], trend: 15 },
      ]
    }
  } catch (e) {
    // API failed, show empty state
  }

  await nextTick()

  // Load chart data
  try {
    // Pie chart: category distribution from dashboard data
    const categoryData = dashboardResult?.data?.categoryDistribution || []
    if (categoryData.length > 0) {
      initPieChart(categoryData.map(d => ({ name: d.category, value: d.value })))
    }

    // Bar chart: stall distribution from dashboard data
    const stallData = dashboardResult?.data?.stallDistribution || []
    if (stallData.length > 0) {
      initBarChart(stallData.map(d => ({ name: d.name, value: d.value })))
    }

    const [trendsRes, scoreRes] = await Promise.all([
      api.getDashboardTrends(),
      api.getScoreDistribution(),
    ])

    if (trendsRes?.code === 200 && trendsRes?.data) {
      initLineChart(trendsRes.data)
    }

    if (scoreRes?.code === 200 && scoreRes?.data) {
      initScoreChart(scoreRes.data)
    }
  } catch {
    // Charts will remain empty if data fails
  }

  window.addEventListener('resize', resizeAll)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeAll)
  pieChart?.dispose()
  barChart?.dispose()
  lineChart?.dispose()
  scoreChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
}

.stat-cards {
  margin-bottom: 16px;
}

.stat-cards :deep(.el-col) {
  margin-bottom: 12px;
}

.chart-row {
  margin-bottom: 16px;
}

.chart-card {
  border-radius: 8px;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chart-box {
  height: 280px;
}

.overview-card {
  border-radius: 8px;
}

.overview-card :deep(.el-descriptions__label) {
  font-weight: 500;
  color: #606266;
}
</style>
