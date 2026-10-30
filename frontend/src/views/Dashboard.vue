<template>
  <div class="dashboard">
    <!-- 三个数字卡片 -->
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon icon-book">
            <el-icon :size="34"><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ summary.bookTotal }}</div>
            <div class="stat-label">图书总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon icon-category">
            <el-icon :size="34"><Collection /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ summary.categoryTotal }}</div>
            <div class="stat-label">分类总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon icon-stock">
            <el-icon :size="34"><Box /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ summary.stockTotal }}</div>
            <div class="stat-label">库存总量</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 柱状图 -->
    <el-card shadow="never" class="chart-card" v-loading="chartLoading">
      <template #header>
        <div class="chart-title">
          <el-icon><Histogram /></el-icon>
          <span>按分类统计图书数量</span>
          <el-button link type="primary" :icon="Refresh" class="refresh-btn" @click="loadAll">
            刷新
          </el-button>
        </div>
      </template>
      <div ref="chartRef" class="chart"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getSummary, getCategoryCount } from '../api/stats'

// 数字卡片数据
const summary = reactive({
  bookTotal: 0,
  categoryTotal: 0,
  stockTotal: 0
})

// 图表
const chartRef = ref(null)
const chartLoading = ref(false)
let chartInstance = null

async function loadSummary() {
  try {
    const res = await getSummary()
    Object.assign(summary, {
      bookTotal: Number(res.data.bookTotal) || 0,
      categoryTotal: Number(res.data.categoryTotal) || 0,
      stockTotal: Number(res.data.stockTotal) || 0
    })
  } catch (e) {
    /* 拦截器已提示, 此处仅阻止报错扩散 */
  }
}

async function loadChart() {
  chartLoading.value = true
  try {
    const res = await getCategoryCount()
    const list = res.data || []
    await nextTick()
    if (!chartInstance) {
      chartInstance = echarts.init(chartRef.value)
    }
    const names = list.map((item) => item.name)
    const totals = list.map((item) => item.total)
    chartInstance.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      grid: { left: '2%', right: '4%', top: 30, bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        name: '分类',
        data: names,
        axisLabel: {
          interval: 0,
          rotate: names.length > 8 ? 30 : 0
        }
      },
      yAxis: {
        type: 'value',
        name: '数量',
        minInterval: 1
      },
      series: [
        {
          name: '图书数量',
          type: 'bar',
          barWidth: '45%',
          itemStyle: {
            borderRadius: [4, 4, 0, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#409eff' },
              { offset: 1, color: '#79bbff' }
            ])
          },
          label: { show: true, position: 'top', color: '#606266' },
          data: totals
        }
      ]
    })
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    chartLoading.value = false
  }
}

function loadAll() {
  loadSummary()
  loadChart()
}

function handleResize() {
  if (chartInstance) chartInstance.resize()
}

onMounted(() => {
  loadAll()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
/* ---------- 数字卡片 ---------- */
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 12px;
  color: #fff;
  flex-shrink: 0;
}

.icon-book {
  background: linear-gradient(135deg, #409eff, #79bbff);
}

.icon-category {
  background: linear-gradient(135deg, #67c23a, #95d475);
}

.icon-stock {
  background: linear-gradient(135deg, #e6a23c, #f3d19e);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  margin-top: 4px;
  font-size: 14px;
  color: #909399;
}

/* ---------- 图表卡片 ---------- */
.chart-card {
  margin-top: 16px;
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.refresh-btn {
  margin-left: auto;
}

.chart {
  width: 100%;
  height: 420px;
}
</style>
