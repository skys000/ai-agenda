<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <el-icon><DataAnalysis /></el-icon>
        <span>AI 日程管家</span>
      </div>
      <el-menu
        active-text-color="#ffd04b"
        background-color="#304156"
        text-color="#fff"
        :router="true"
        :default-active="$route.path"
      >
        <menu-item v-for="menu in menuData" :key="menu.id" :item="menu" base-path="" />
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="page-heading">
          <span>{{ route.meta.title || '工作台' }}</span>
          <small>AI Agenda Assistant</small>
        </div>
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

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuTree } from '@/api/menu'
import MenuItem from './components/MenuItem.vue'
import bus from '@/utils/eventBus.js'

const menuData = ref([])
const router = useRouter()
const route = useRoute()
const currentUsername = ref('用户')

const getCurrentUsername = () => {
  const username = localStorage.getItem('username')
  currentUsername.value = username || '用户'
}

const handleProfile = () => {
  router.push('/profile')
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    ElMessage.success('退出登录成功')
    router.replace('/login')
  }).catch(() => {
    ElMessage.info('已取消退出')
  })
}

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
    const res = await getMenuTree()
    if (res && res.code === 200) {
      let rawData = []
      if (Array.isArray(res.data)) {
        rawData = res.data
      } else if (res.data && Array.isArray(res.data.children)) {
        rawData = res.data.children
      } else {
        rawData = res.data ? [res.data] : []
      }

      const normalizedData = normalizeMenuData(rawData)
      if (normalizedData.length === 0) {
        menuData.value = defaultMenuData
      } else {
        menuData.value = normalizedData
      }
    } else {
      menuData.value = defaultMenuData
    }
  } catch {
    menuData.value = defaultMenuData
  }
}

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
  getCurrentUsername()
  bus.on('menu-updated', () => {
    getMenuData()
  })
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100vw;
}
.sidebar {
  background-color: #304156;
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
.page-heading {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.page-heading span {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}
.page-heading small {
  color: #8a94a6;
  font-size: 12px;
}
.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
