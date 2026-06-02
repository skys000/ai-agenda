import { createApp } from 'vue'
import './style.css'
import App from './App.vue'

// 1. 引入 Element Plus 和 样式
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 2. 引入中文语言包
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
// 3. 引入图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// 4. 引入路由 (稍后创建)
import router from './router'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus, { locale: zhCn })
app.use(router) // 挂载路由
app.mount('#app')