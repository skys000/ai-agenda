<template>
  <el-container class="layout-container">
    <!-- 左侧菜单 -->
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <el-icon><DataAnalysis /></el-icon>
        <span>AI 日程管家</span>
      </div>
      <!-- 菜单组件 -->
      <el-menu
        active-text-color="#ffd04b"
        background-color="#304156"
        text-color="#fff"
        :router="true"
        :default-active="$route.path"
      >
        <!-- 递归渲染菜单 -->
        <menu-item v-for="menu in menuData" :key="menu.id" :item="menu" base-path="" />
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="breadcrumb">面包屑导航</div>
        <div class="user-info">
          <el-dropdown>
            <span class="el-dropdown-link">
              {{ currentUsername }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleProfile">个人中心</el-dropdown-item>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuTree } from '@/api/menu'
import MenuItem from './components/MenuItem.vue' // 引入递归菜单组件
import bus from '@/utils/eventBus.js' // 引入事件总线

const menuData = ref([])
const router = useRouter()
const currentUsername = ref('用户')

// 获取当前用户名
const getCurrentUsername = () => {
  const username = localStorage.getItem('username')
  currentUsername.value = username || '用户'
}

// 个人中心处理函数
const handleProfile = () => {
  router.push('/profile')
}

// 退出登录处理函数
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 删除token
    localStorage.removeItem('token')
    // 显示退出成功消息
    ElMessage.success('退出登录成功')
    // 跳转到登录页面
    router.replace('/login')
  }).catch(() => {
    // 用户取消操作
    ElMessage.info('已取消退出')
  })
}

// 默认菜单数据，用于后端数据获取失败时的降级方案
const defaultMenuData = [
  {
    id: 1,
    menuName: '日程中心',
    path: '/agenda',
    icon: 'Calendar',
    children: [
      { id: 11, menuName: '我的日程', path: '/agenda/schedule', icon: 'List' },
      { id: 12, menuName: 'AI 助手', path: '/agenda/chat', icon: 'ChatDotRound' },
      { id: 13, menuName: '分类管理', path: '/agenda/category', icon: 'FolderOpened' },
      { id: 14, menuName: 'AI 会话日志', path: '/agenda/ai-message', icon: 'Document' }
    ]
  },
  {
    id: 2,
    menuName: '系统管理',
    path: '/system',
    icon: 'Setting',
    children: [
      { id: 21, menuName: '用户管理', path: '/system/user', icon: 'User' },
      { id: 22, menuName: '角色管理', path: '/system/role', icon: 'UserFilled' },
      { id: 23, menuName: '菜单管理', path: '/system/menu', icon: 'Menu' }
    ]
  }
]

const getMenuData = async () => {
  try {
    // 调用后端接口获取菜单树
    const res = await getMenuTree()
    console.log('菜单数据响应:', res) // 调试日志
    
    if (res && res.code === 200) {
      // 兼容多种数据格式
      let rawData = []
      if (Array.isArray(res.data)) {
        rawData = res.data
      } else if (res.data && Array.isArray(res.data.children)) {
        rawData = res.data.children
      } else {
        rawData = res.data ? [res.data] : []
      }
      
      console.log('解析后的菜单数据:', rawData) // 调试日志
      
      // 确保菜单数据有正确的字段名
      const normalizedData = normalizeMenuData(rawData)
      
      // 只有当后端返回的数据确实为空时，才使用默认菜单
      if (normalizedData.length === 0) {
        console.warn('后端返回菜单为空，使用默认菜单')
        menuData.value = defaultMenuData
      } else {
        // 使用后端返回的数据
        menuData.value = normalizedData
        console.log('使用后端菜单数据，菜单数量:', menuData.value.length)
      }
    } else {
      console.error('获取菜单失败:', res?.msg || '未知错误')
      // 后端返回错误时才使用默认菜单
      menuData.value = defaultMenuData
    }
  } catch (error) {
    console.error('获取菜单失败，使用默认菜单:', error)
    // 网络错误时才使用默认菜单
    menuData.value = defaultMenuData
  }
}

// 规范化菜单数据，确保字段名一致性
const normalizeMenuData = (menus) => {
  if (!Array.isArray(menus)) return []
  
  return menus.map(menu => ({
    id: menu.id,
    menuName: menu.menuName || menu.name || menu.title || '未命名菜单',
    path: menu.path || menu.url || menu.route || '',
    icon: menu.icon || 'Menu',
    children: menu.children ? normalizeMenuData(menu.children) : []
  }))
}

onMounted(() => {
  getMenuData()
  getCurrentUsername() // 获取当前用户名
  
  // 【订阅】监听菜单更新事件
  bus.on('menu-updated', () => {
    console.log('收到菜单更新广播，正在刷新左侧菜单...')
    // 强制刷新菜单数据
    getMenuData()
    // 添加调试信息
    console.log('左侧菜单栏已重新加载菜单数据')
  })
  
  // 添加事件总线调试
  console.log('左侧菜单栏已注册菜单更新监听器')
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100vw; /* 确保占满整个视口宽度 */
}
.sidebar {
  background-color: #304156;
  /* 防止菜单内容过多时出现滚动条 */
  overflow-x: hidden;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}
.el-menu { border-right: none; }
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}
.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  /* 关键：让 main 区域可以滚动 */
  overflow-y: auto;
}
</style>