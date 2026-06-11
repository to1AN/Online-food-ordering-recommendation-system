<template>
  <el-card shadow="hover" class="stat-card" :style="{ background: gradient }">
    <div class="stat-card-inner">
      <div class="stat-icon-box">
        <el-icon :size="24" color="#fff">
          <component :is="icon" />
        </el-icon>
      </div>
      <div class="stat-body">
        <div class="stat-value">{{ displayValue }}</div>
        <div class="stat-label">{{ label }}</div>
      </div>
      <div v-if="trend !== 0" class="stat-trend" :class="trend > 0 ? 'up' : 'down'">
        <el-icon><component :is="trend > 0 ? 'CaretTop' : 'CaretBottom'" /></el-icon>
        <span>{{ Math.abs(trend) }}%</span>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: { type: String, required: true },
  value: { type: [Number, String], default: 0 },
  icon: { type: String, default: 'DataAnalysis' },
  gradient: { type: String, default: 'linear-gradient(135deg, #667eea, #764ba2)' },
  trend: { type: Number, default: 0 },
  formatter: { type: Function, default: null },
})

const displayValue = computed(() => {
  if (props.formatter) return props.formatter(props.value)
  return props.value
})
</script>

<style scoped>
.stat-card {
  color: #fff;
  border: none;
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2) !important;
}

.stat-card :deep(.el-card__body) {
  padding: 18px 20px;
}

.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-body {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
}

.stat-label {
  font-size: 13px;
  opacity: 0.85;
  margin-top: 2px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 13px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.2);
}

.stat-trend.up {
  color: #a0ffa0;
}

.stat-trend.down {
  color: #ffa0a0;
}
</style>
