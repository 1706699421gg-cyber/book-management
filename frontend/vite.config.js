import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    // 端口被占用时直接报错, 避免自动切到 5174 导致脚本打开错误地址
    strictPort: true,
    // 不自动用默认浏览器打开: 由 start.ps1 统一用 Edge 无痕窗口(InPrivate)打开登录页,
    // 可规避本机"普通窗口点击最小化后被系统钩子还原"的问题
    open: false,
    proxy: {
      // 开发环境把 /api 请求转发到 Spring Boot 后端
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
