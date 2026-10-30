<template>
  <el-container class="layout-container">
    <!-- 左侧菜单: 内容由后端按角色权限下发 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo">
        <el-icon :size="26"><Reading /></el-icon>
        <span v-show="!isCollapse" class="logo-text">图书管理系统</span>
      </div>
      <el-menu
        :default-active="route.path"
        router
        class="side-menu"
        :collapse="isCollapse"
        :collapse-transition="false"
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部标题栏 -->
      <el-header class="layout-header">
        <div class="header-title">
          <el-button link class="collapse-btn" @click="toggleCollapse">
            <el-icon :size="18"><Menu /></el-icon>
          </el-button>
          <span>{{ route.meta.title || '首页' }}</span>
        </div>

        <!-- 当前登录用户 -->
        <div class="header-right">
          <el-tag :type="isAdmin ? 'danger' : 'info'" effect="plain" size="small">
            {{ roleLabel }}
          </el-tag>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-entry">
              <el-avatar :size="28" class="user-avatar">{{ avatarText }}</el-avatar>
              <span class="user-name">{{ displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><Avatar /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { authState, logout } from '../store/auth'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)

const menus = computed(() => authState.menus)
const displayName = computed(
  () => (authState.user && (authState.user.realName || authState.user.username)) || '未登录'
)
const roleLabel = computed(() => (authState.user && authState.user.roleLabel) || '')
const isAdmin = computed(() => authState.user && authState.user.role === 'ADMIN')
const avatarText = computed(() => {
  const name = (authState.user && authState.user.username) || '?'
  return name.charAt(0).toUpperCase()
})

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

async function handleCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
    return
  }
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定退出登录吗?', '退出确认', {
        confirmButtonText: '退出',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch (e) {
      return
    }
    await logout()
    ElMessage.success('已退出登录')
    router.replace('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

/* ---------- 左侧菜单 ---------- */
.layout-aside {
  display: flex;
  flex-direction: column;
  background-color: #001529;
  overflow: hidden;
  transition: width 0.2s;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 60px;
  color: #fff;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.side-menu {
  flex: 1;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #a6adb4;
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.08);
  --el-menu-active-color: #ffffff;
  border-right: none;
}

.side-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
}

.side-menu :deep(.el-menu-item.is-active) {
  background-color: #1890ff;
  color: #fff;
}

/* ---------- 顶部标题 ---------- */
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 10;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.collapse-btn {
  padding: 0;
  color: #606266;
}

.collapse-btn:hover {
  color: #409eff;
}

/* ---------- 右上角用户信息 ---------- */
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 6px;
  cursor: pointer;
  color: #303133;
  transition: background-color 0.2s;
}

.user-entry:hover {
  background-color: #f5f7fa;
}

.user-avatar {
  background: linear-gradient(135deg, #409eff, #1f6fd0);
  color: #fff;
  font-size: 13px;
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

/* ---------- 主内容区 ---------- */
.layout-main {
  padding: 16px;
  overflow-y: auto;
}
</style>
