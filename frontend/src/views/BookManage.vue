<template>
  <div>
    <!-- 搜索栏 -->
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="请输入书名或作者关键字"
          clearable
          style="width: 300px"
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
          <el-button
            v-permission="'book:add'"
            type="primary"
            :icon="Plus"
            @click="openDialog()"
          >
            新增图书
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        :row-class-name="rowClassName"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="书名" min-width="170" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" min-width="120" show-overflow-tooltip />
        <el-table-column prop="isbn" label="ISBN" min-width="140" show-overflow-tooltip />
        <el-table-column label="分类" width="110" align="center">
          <template #default="{ row }">{{ categoryName(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column label="价格(元)" width="100" align="right">
          <template #default="{ row }">
            {{ row.price != null ? Number(row.price).toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="库存" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.stock === 0" type="danger" effect="dark">缺货</el-tag>
            <el-tag v-else type="success" effect="plain">{{ row.stock }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="出版日期" width="115" align="center">
          <template #default="{ row }">{{ formatDate(row.publishDate) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170" align="center">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column
          v-if="canEdit || canDelete"
          label="操作"
          width="150"
          align="center"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              v-permission="'book:edit'"
              link
              type="primary"
              :icon="Edit"
              @click="openDialog(row)"
            >
              编辑
            </el-button>
            <el-button
              v-permission="'book:delete'"
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

      <!-- 分页 -->
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑图书' : '新增图书'"
      width="620px"
      :close-on-click-modal="false"
      @closed="formRef && formRef.resetFields()"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="书名" prop="name">
          <el-input v-model="form.name" placeholder="请输入书名" maxlength="100" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者" maxlength="50" />
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" placeholder="请输入 ISBN 编号" maxlength="20" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select
            v-model="form.categoryId"
            placeholder="请选择分类"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格(元)" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 180px" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :step="1" style="width: 180px" />
        </el-form-item>
        <el-form-item label="出版日期" prop="publishDate">
          <el-date-picker
            v-model="form.publishDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择出版日期"
            style="width: 100%"
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
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { pageBooks, addBook, updateBook, deleteBook } from '../api/book'
import { listCategories } from '../api/category'
import { hasPermission } from '../store/auth'

// ---------------- 查询状态 ----------------
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const categories = ref([])

const query = reactive({
  current: 1,
  size: 10,
  keyword: ''
})

// ---------------- 按钮级权限(普通用户只读) ----------------
const canEdit = computed(() => hasPermission('book:edit'))
const canDelete = computed(() => hasPermission('book:delete'))

// ---------------- 分类映射(表格显示分类名) ----------------
const categoryMap = computed(() => {
  const map = {}
  categories.value.forEach((item) => {
    map[item.id] = item.name
  })
  return map
})

function categoryName(id) {
  return id == null ? '未分类' : categoryMap.value[id] || '未分类'
}

// ---------------- 工具函数 ----------------
function formatDate(value) {
  return value ? String(value).substring(0, 10) : '-'
}

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').substring(0, 19)
}

// 库存为 0 的行标红
function rowClassName({ row }) {
  return row.stock === 0 ? 'row-out-of-stock' : ''
}

// ---------------- 数据加载 ----------------
async function loadCategories() {
  try {
    const res = await listCategories()
    categories.value = res.data || []
  } catch (e) {
    /* 拦截器已提示 */
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await pageBooks({
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

// ---------------- 新增/编辑 ----------------
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)

const emptyForm = {
  id: null,
  name: '',
  author: '',
  isbn: '',
  categoryId: null,
  price: 0,
  stock: 0,
  publishDate: null
}

const form = reactive({ ...emptyForm })

const rules = {
  name: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

function openDialog(row) {
  if (row) {
    // 编辑: 回填
    Object.assign(form, {
      id: row.id,
      name: row.name || '',
      author: row.author || '',
      isbn: row.isbn || '',
      categoryId: row.categoryId,
      price: row.price != null ? Number(row.price) : 0,
      stock: row.stock != null ? Number(row.stock) : 0,
      publishDate: row.publishDate ? String(row.publishDate).substring(0, 10) : null
    })
  } else {
    // 新增: 重置
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
      if (data.publishDate) {
        data.publishDate = String(data.publishDate).substring(0, 10)
      }
      if (data.id) {
        await updateBook(data)
        ElMessage.success('修改成功')
      } else {
        delete data.id
        await addBook(data)
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

// ---------------- 删除(带确认) ----------------
function handleDelete(row) {
  ElMessageBox.confirm(`确定删除图书《${row.name}》吗? 删除后不可恢复!`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await deleteBook(row.id)
      ElMessage.success('删除成功')
      // 当前页只剩一条且不是第一页时, 回退一页
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
  loadCategories()
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

/* 库存为 0 的行整行标红 */
:deep(.el-table .row-out-of-stock),
:deep(.el-table .row-out-of-stock td.el-table__cell) {
  background-color: #fef0f0 !important;
  color: #f56c6c;
}
</style>
