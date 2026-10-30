import { hasPermission } from '../store/auth'

/**
 * 按钮级权限指令:
 *   v-permission="'book:add'"
 *   v-permission="['book:edit', 'book:delete']"  // 需要同时具备
 * 无权限时直接从 DOM 中移除元素, 避免"看得到点不了"的伪权限。
 */
function apply(el, binding) {
  const required = Array.isArray(binding.value) ? binding.value : [binding.value]
  const allowed = required.length === 0 || required.every((item) => hasPermission(item))
  if (!allowed) {
    if (el.parentNode) {
      el.parentNode.removeChild(el)
    } else {
      el.style.display = 'none'
    }
  }
}

export default {
  mounted(el, binding) {
    apply(el, binding)
  },
  updated(el, binding) {
    apply(el, binding)
  }
}
