<template>
  <div>
    <!-- 工具栏 -->
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <span class="toolbar-tip">
          <el-icon><InfoFilled /></el-icon>
          分类下存在图书时不可删除; 图书新增/编辑时通过下拉框选择分类
        </span>
        <div class="toolbar-right">
          <el-button
            v-permission="'category:add'"
            type="primary"
            :icon="Plus"
            @click="openDialog()"
          >
            新增分类
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="description" label="分类描述" min-width="260" show-overflow-tooltip />
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column
          v-if="canEdit || canDelete"
          label="操作"
          width="160"
          align="center"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              v-permission="'category:edit'"
              link
              type="primary"
              :icon="Edit"
              @click="openDialog(row)"
            >
              编辑
            </el-button>
            <el-button
              v-permission="'category:delete'"
              link
              type="danger"
              :icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑分类' : '新增分类'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="分类描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述(选填)"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { listCategories, addCategory, updateCategory, deleteCategory } from '../api/category'
import { hasPermission } from '../store/auth'

const loading = ref(false)
const tableData = ref([])

// 按钮级权限: 普通用户只读
const canEdit = computed(() => hasPermission('category:edit'))
const canDelete = computed(() => hasPermission('category:delete'))

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').substring(0, 19)
}

async function loadData() {
  loading.value = true
  try {
    const res = await listCategories()
    tableData.value = res.data || []
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

// ---------------- 新增/编辑 ----------------
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)

const emptyForm = {
  id: null,
  name: '',
  description: ''
}

const form = reactive({ ...emptyForm })

const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 50, message: '名称不能超过 50 个字符', trigger: 'blur' }
  ]
}

function openDialog(row) {
  if (row) {
    Object.assign(form, {
      id: row.id,
      name: row.name || '',
      description: row.description || ''
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
      const data = { ...form }
      if (data.id) {
        await updateCategory(data)
        ElMessage.success('修改成功')
      } else {
        delete data.id
        await addCategory(data)
        ElMessage.success('新增成功')
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

// ---------------- 删除(后端校验分类下是否有图书) ----------------
function handleDelete(row) {
  ElMessageBox.confirm(`确定删除分类「${row.name}」吗?`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await deleteCategory(row.id)
      ElMessage.success('删除成功')
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

.toolbar-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

.toolbar-right {
  margin-left: auto;
}
</style>
