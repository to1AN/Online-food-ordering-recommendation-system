<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑' + entityName : '新增' + entityName"
    width="560px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
    @closed="$emit('closed')"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <slot :form="form" />
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  isEdit: { type: Boolean, default: false },
  entityName: { type: String, default: '' },
  form: { type: Object, required: true },
  rules: { type: Object, default: () => ({}) },
  loading: { type: Boolean, default: false },
})

const emit = defineEmits(['update:visible', 'submit', 'closed'])
const formRef = ref(null)

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    emit('submit')
  } catch {
    // validation failed
  }
}

defineExpose({ formRef })
</script>
