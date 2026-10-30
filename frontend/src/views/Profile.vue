<template>
  <div>
    <!-- 账号信息 -->
    <el-card shadow="never" class="profile-card">
      <div class="profile-head">
        <el-avatar :size="60" class="profile-avatar">{{ avatarText }}</el-avatar>
        <div class="profile-title">
          <div class="name-line">
            <span class="name">{{ user.realName || user.username || '未登录' }}</span>
            <el-tag :type="isAdmin ? 'danger' : 'info'" effect="plain" size="small">{{ roleLabel }}</el-tag>
            <el-tag :type="user.status === 1 ? 'success' : 'danger'" effect="plain" size="small">
              {{ user.status === 1 ? '已启用' : '已禁用' }}
            </el-tag>
          </div>
          <div class="account-line">账号: {{ user.username || '-' }}</div>
        </div>
      </div>

      <el-descriptions :column="2" border class="profile-desc">
        <el-descriptions-item label="账号">{{ user.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名 / 昵称">{{ user.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ roleLabel }}</el-descriptions-item>
        <el-descriptions-item label="可访问菜单">{{ menuTitles }}</el-descriptions-item>
        <el-descriptions-item label="操作权限" :span="2">
          <div class="permission-list">
            <el-tag
              v-for="code in permissions"
              :key="code"
              size="small"
              effect="plain"
              class="permission-tag"
            >
              {{ permissionLabel(code) }}
            </el-tag>
            <span v-if="!permissions.length" class="empty-tip">暂无权限</span>
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 修改密码 -->
    <el-card shadow="never" class="password-card">
      <template #header>
        <span class="card-title">修改密码</span>
      </template>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
        class="password-form"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="6-50 位" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changePasswordApi } from '../api/auth'
import { authState, clearSession } from '../store/auth'

const router = useRouter()
const user = computed(() => authState.user || {})
const permissions = computed(() => authState.permissions || [])
const roleLabel = computed(() => user.value.roleLabel || '')
const isAdmin = computed(() => user.value.role === 'ADMIN')
const avatarText = computed(() => (user.value.username || '?').charAt(0).toUpperCase())
const menuTitles = computed(() => (authState.menus || []).map((item) => item.title).join(' / ') || '-')

const PERMISSION_LABELS = {
  'stats:view': '查看数据统计',
  'book:view': '查看图书',
  'book:add': '新增图书',
  'book:edit': '编辑图书',
  'book:delete': '删除图书',
  'category:view': '查看分类',
  'category:add': '新增分类',
  'category:edit': '编辑分类',
  'category:delete': '删除分类',
  'user:manage': '用户管理',
  'profile:view': '个人中心'
}

function permissionLabel(code) {
  return PERMISSION_LABELS[code] || code
}

const formRef = ref(null)
const saving = ref(false)
const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度需为 6-50 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

function handleSubmit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await changePasswordApi({
        oldPassword: form.oldPassword,
        newPassword: form.newPassword
      })
      ElMessage.success('密码修改成功, 请重新登录')
      clearSession()
      router.replace('/login')
    } catch (e) {
      /* 拦截器已提示 */
    } finally {
      saving.value = false
    }
  })
}
</script>

<style scoped>
.profile-card {
  margin-bottom: 16px;
}

.profile-head {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 22px;
}

.profile-avatar {
  background: linear-gradient(135deg, #409eff, #1f6fd0);
  color: #fff;
  font-size: 24px;
}

.name-line {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.account-line {
  font-size: 13px;
  color: #909399;
}

.permission-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.permission-tag {
  margin: 0;
}

.empty-tip {
  font-size: 13px;
  color: #909399;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.password-form {
  max-width: 460px;
}
</style>
