import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/Index.vue' // 引入布局组件

const routes = [
  // ================ 新增登录页路由 ================
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    // 在 meta 中添加一个标记，表示这是一个公开页面，不需要守卫
    meta: { isPublic: true } 
  },
  // ============================================
  {
    path: '/',
    component: Layout,
    redirect: '/agenda/schedule', // 默认重定向到日程页
    children: [
      // --- 日程中心 ---
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
      // --- 系统管理 ---
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
      // --- 个人中心 ---
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

// ================ 新增全局前置守卫 ================
router.beforeEach((to, from, next) => {
  // 1. 获取本地存储的 token
  const token = localStorage.getItem('token');

  // 2. 判断目标页面是否是公开页面 (如登录页)
  if (to.meta.isPublic) {
    // 2.1 如果是公开页面
    if (token && to.name === 'Login') {
      // 如果用户已登录，还想访问登录页，则直接送他回首页
      next({ path: '/' });
    } else {
      // 否则，正常放行
      next();
    }
  } else {
    // 2.2 如果是需要权限的页面
    if (token) {
      // 如果有 token，说明已登录，直接放行
      next();
    } else {
      // 如果没有 token，说明未登录，强制跳转到登录页
      // 使用 replace 防止用户通过浏览器后退按钮回到之前的页面
      next({ name: 'Login', replace: true }); 
    }
  }
});
// ============================================

export default router