import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // ================ 新增服务器配置 ================
  server: {
    // 配置代理，解决开发环境下的跨域问题
    proxy: {
      // 当请求路径以 /api 开头时，触发此代理
      '/api': {
        // 代理的目标服务器地址
        target: 'http://localhost:6887',
        // 是否允许跨域
        changeOrigin: true,
        // 重写请求路径，去掉 /api 前缀
        // 例如，前端请求 /api/sys-user/login 会被转发到 http://localhost:6887/sys-user/login
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
  // ============================================
})