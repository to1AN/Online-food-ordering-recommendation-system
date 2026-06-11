<template>
  <el-tag
    :type="tagType"
    :size="size"
    :effect="effect"
    v-bind="$attrs"
  >
    <el-icon v-if="showDot" style="margin-right: 4px;">
      <component :is="dotIcon" />
    </el-icon>
    {{ text }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'

const STATUS_MAP = {
  normal: { type: 'success', icon: 'CircleCheckFilled' },
  active: { type: 'success', icon: 'CircleCheckFilled' },
  disabled: { type: 'danger', icon: 'CircleCloseFilled' },
  inactive: { type: 'info', icon: 'CircleCloseFilled' },
  pending: { type: 'warning', icon: 'Clock' },
  approved: { type: 'success', icon: 'CircleCheckFilled' },
  rejected: { type: 'danger', icon: 'CircleCloseFilled' },
  open: { type: 'success', icon: 'CircleCheckFilled' },
  closed: { type: 'info', icon: 'CircleCloseFilled' },
  liked: { type: 'danger', icon: 'StarFilled' },
  unliked: { type: 'info', icon: 'Star' },
  auto: { type: 'success', icon: 'Clock' },
  manual: { type: '', icon: 'UserFilled' },
  verified: { type: 'success', icon: 'CircleCheckFilled' },
  failed: { type: 'danger', icon: 'CircleCloseFilled' },
  verifying: { type: 'warning', icon: 'Loading' },
}

const props = defineProps({
  status: { type: String, default: 'normal' },
  text: { type: String, default: '' },
  size: { type: String, default: 'small' },
  effect: { type: String, default: 'light' },
  showDot: { type: Boolean, default: true },
})

const config = computed(() => STATUS_MAP[props.status] || STATUS_MAP.normal)
const tagType = computed(() => config.value.type)
const dotIcon = computed(() => config.value.icon)
</script>
