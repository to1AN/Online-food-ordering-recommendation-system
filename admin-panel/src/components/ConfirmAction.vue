<template>
  <el-popconfirm
    :title="title"
    :confirm-button-text="confirmText"
    :cancel-button-text="'取消'"
    @confirm="$emit('confirm')"
  >
    <template #reference>
      <el-button :type="buttonType" link size="small">
        <el-icon><component :is="icon" /></el-icon>{{ label }}
      </el-button>
    </template>
  </el-popconfirm>
</template>

<script setup>
defineProps({
  title: { type: String, default: '确定要执行此操作吗？' },
  confirmText: { type: String, default: '确定' },
  label: { type: String, default: '删除' },
  buttonType: { type: String, default: 'danger' },
  icon: { type: String, default: 'Delete' },
})

defineEmits(['confirm'])
</script>
