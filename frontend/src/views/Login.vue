<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-circle circle-1"></div>
    <div class="bg-circle circle-2"></div>
    <div class="bg-circle circle-3"></div>

    <div class="login-card">
      <!-- 品牌区 -->
      <div class="brand">
        <div class="brand-icon">
          <el-icon :size="30"><Reading /></el-icon>
        </div>
        <div class="brand-text">
          <div class="brand-title">图书管理系统</div>
          <div class="brand-sub">Book Management System</div>
        </div>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            size="large"
            placeholder="请输入账号"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            size="large"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          class="login-btn"
          :loading="loading"
          @click="handleLogin"
        >
          登 录
        </el-button>
      </el-form>

      <!-- 演示账号 -->
      <div class="account-tips">
        <div class="tips-title">演示账号(点击自动填入)</div>
        <div class="tips-list">
          <button type="button" class="tip-item" @click="fill('admin', 'admin123')">
            <el-tag size="small" type="danger" effect="dark">管理员</el-tag>
            <span>admin / admin123</span>
          </button>
          <button type="button" class="tip-item" @click="fill('reader', 'user123')">
            <el-tag size="small" type="info" effect="plain">普通用户</el-tag>
            <span>reader / user123</span>
          </button>
        </div>
      </div>
    </div>

    <footer class="login-footer">图书管理系统 · 请使用您的账号登录</footer>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login, firstAccessiblePath } from '../store/auth'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

function fill(username, password) {
  form.username = username
  form.password = password
}

function handleLogin() {
  if (loading.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const data = await login(form.username.trim(), form.password)
      ElMessage.success(`欢迎回来, ${data.user.realName || data.user.username}`)
      const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
      await router.replace(redirect || firstAccessiblePath())
    } catch (e) {
      /* 错误提示由 axios 拦截器统一处理 */
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #1f3a5f 0%, #27527f 45%, #4a7fb5 100%);
}

/* 背景装饰圆 */
.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.07);
}

.circle-1 {
  width: 420px;
  height: 420px;
  top: -120px;
  left: -100px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  right: -80px;
  bottom: -60px;
}

.circle-3 {
  width: 160px;
  height: 160px;
  right: 18%;
  top: 16%;
  background: rgba(255, 255, 255, 0.05);
}

.login-card {
  position: relative;
  z-index: 1;
  width: 400px;
  max-width: 90vw;
  padding: 38px 36px 28px;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 18px 50px rgba(8, 24, 44, 0.35);
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 30px;
}

.brand-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  border-radius: 12px;
  color: #fff;
  background: linear-gradient(135deg, #409eff, #1f6fd0);
  flex-shrink: 0;
}

.brand-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2d3d;
  letter-spacing: 1px;
}

.brand-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #a8b3c0;
  letter-spacing: 0.5px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  margin-top: 6px;
  font-size: 16px;
  letter-spacing: 4px;
}

.account-tips {
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px dashed #ebeef5;
}

.tips-title {
  margin-bottom: 10px;
  font-size: 12px;
  color: #909399;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 7px 10px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafbfc;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}

.tip-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
  color: #409eff;
}

.login-footer {
  position: relative;
  z-index: 1;
  margin-top: 24px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.55);
}
</style>
