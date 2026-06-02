import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/Index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { isPublic: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/agenda/schedule',
    children: [
      {
        path: 'agenda/schedule',
        name: 'Schedule',
        component: () => import('../views/agenda/ScheduleView.vue'),
        meta: { title: '我的日程' }
      },
      {
        path: 'agenda/chat',
        name: 'Chat',
        component: () => import('../views/agenda/ChatView.vue'),
        meta: { title: 'AI 助手' }
      },
      {
        path: 'agenda/category',
        name: 'Category',
        component: () => import('../views/agenda/CategoryView.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'agenda/ai-message',
        name: 'AiMessage',
        component: () => import('../views/agenda/AiMessageView.vue'),
        meta: { title: 'AI 会话日志' }
      },
      {
        path: 'system/user',
        name: 'User',
        component: () => import('../views/system/UserView.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'system/role',
        name: 'Role',
        component: () => import('../views/system/RoleView.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'system/menu',
        name: 'Menu',
        component: () => import('../views/system/MenuView.vue'),
        meta: { title: '菜单管理' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/ProfileView.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.isPublic) {
    if (token && to.name === 'Login') {
      next({ path: '/' })
    } else {
      next()
    }
  } else if (token) {
    next()
  } else {
    next({ name: 'Login', replace: true })
  }
})

export default router
