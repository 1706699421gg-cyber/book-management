<template>
  <div>
    <!-- 工具栏 -->
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="请输入用户名或姓名"
          clearable
          style="width: 280px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        <div class="toolbar-right">
          <el-button v-permission="'user:manage'" type="primary" :icon="Plus" @click="openDialog()">
            新增用户
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 用户列表 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="账号" min-width="130" />
        <el-table-column prop="realName" label="姓名 / 昵称" min-width="130" show-overflow-tooltip />
        <el-table-column label="角色" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" effect="plain">
              {{ row.roleLabel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              :disabled="row.id === currentUserId"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170" align="center">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openDialog(row)">编辑</el-button>
            <el-button link type="warning" :icon="Key" @click="openResetDialog(row)">重置密码</el-button>
            <el-button
              link
              type="danger"
              :icon="Delete"
              :disabled="row.id === currentUserId"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑用户' : '新增用户'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入登录账号" maxlength="50" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名 / 昵称" maxlength="50" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员(全部权限)" value="ADMIN" />
            <el-option label="普通用户(只读)" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="6-50 位" maxlength="50" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="resetVisible" title="重置密码" width="420px" :close-on-click-modal="false">
      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-width="80px">
        <el-form-item label="账号">
          <el-input :model-value="resetTarget.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetForm.newPassword"
            type="password"
            show-password
            placeholder="6-50 位"
            maxlength="50"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetting" @click="handleResetPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Key } from '@element-plus/icons-vue'
import { pageUsers, addUser, updateUser, deleteUser, resetUserPassword } from '../api/user'
import { authState } from '../store/auth'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentUserId = computed(() => (authState.user ? authState.user.id : null))

const query = reactive({
  current: 1,
  size: 10,
  keyword: ''
})

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').substring(0, 19)
}

async function loadData() {
  loading.value = true
  try {
    const res = await pageUsers({
      current: query.current,
      size: query.size,
      keyword: query.keyword ? query.keyword.trim() : undefined
    })
    const page = res.data || {}
    tableData.value = page.records || []
    total.value = page.total || 0
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.current = 1
  loadData()
}

function handleReset() {
  query.keyword = ''
  query.current = 1
  loadData()
}

// ---------------- 新增 / 编辑 ----------------
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)

const emptyForm = {
  id: null,
  username: '',
  realName: '',
  role: 'USER',
  password: '',
  enabled: true
}

const form = reactive({ ...emptyForm })

const rules = {
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    { min: 3, max: 50, message: '账号长度需为 3-50 位', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度需为 6-50 位', trigger: 'blur' }
  ]
}

function openDialog(row) {
  if (row) {
    Object.assign(form, {
      id: row.id,
      username: row.username || '',
      realName: row.realName || '',
      role: row.role || 'USER',
      password: '',
      enabled: row.status === 1
    })
  } else {
    Object.assign(form, emptyForm)
  }
  dialogVisible.value = true
}

function handleSave() {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const data = {
        id: form.id,
        username: form.username.trim(),
        realName: form.realName.trim(),
        role: form.role,
        status: form.enabled ? 1 : 0
      }
      if (!form.id) {
        data.password = form.password
        await addUser(data)
        ElMessage.success('新增成功')
      } else {
        if (form.password) data.password = form.password
        await updateUser(data)
        ElMessage.success('修改成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (e) {
      /* 拦截器已提示 */
    } finally {
      saving.value = false
    }
  })
}

// ---------------- 状态切换 ----------------
async function handleStatusChange(row, enabled) {
  try {
    await updateUser({
      id: row.id,
      username: row.username,
      realName: row.realName,
      role: row.role,
      status: enabled ? 1 : 0
    })
    ElMessage.success(enabled ? '账号已启用' : '账号已禁用')
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    loadData()
  }
}

// ---------------- 重置密码 ----------------
const resetVisible = ref(false)
const resetting = ref(false)
const resetFormRef = ref(null)
const resetTarget = reactive({ id: null, username: '' })
const resetForm = reactive({ newPassword: '' })
const resetRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度需为 6-50 位', trigger: 'blur' }
  ]
}

function openResetDialog(row) {
  resetTarget.id = row.id
  resetTarget.username = row.username
  resetForm.newPassword = ''
  resetVisible.value = true
}

function handleResetPassword() {
  resetFormRef.value.validate(async (valid) => {
    if (!valid) return
    resetting.value = true
    try {
      await resetUserPassword(resetTarget.id, resetForm.newPassword)
      ElMessage.success('密码已重置')
      resetVisible.value = false
    } catch (e) {
      /* 拦截器已提示 */
    } finally {
      resetting.value = false
    }
  })
}

// ---------------- 删除 ----------------
function handleDelete(row) {
  ElMessageBox.confirm(`确定删除用户「${row.username}」吗? 删除后不可恢复!`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      if (tableData.value.length === 1 && query.current > 1) {
        query.current -= 1
      }
      loadData()
    })
    .catch(() => {
      /* 用户取消删除 */
    })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.toolbar-card {
  margin-bottom: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-right {
  margin-left: auto;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
