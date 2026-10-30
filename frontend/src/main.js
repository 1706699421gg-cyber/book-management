import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import permissionDirective from './directives/permission'

const app = createApp(App)

// 全局注册 Element Plus 所有图标组件, 模板中可直接使用
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.directive('permission', permissionDirective)

app.use(ElementPlus)
app.use(router)
app.mount('#app')
