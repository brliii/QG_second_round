import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Detail from '../views/Detail.vue'
import LostCreate from '../views/LostCreate.vue'
import PickedCreate from '../views/PickedCreate.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Profile from '../views/Profile.vue'
import ClaimCreate from '../views/ClaimCreate.vue'
import MyClaims from '../views/MyClaims.vue'
import ReceivedClaims from '../views/ReceivedClaims.vue'
import MyMessages from '../views/Messages.vue'
import Admin from '../views/Admin/index.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home,
      meta: {}
    },
    {
      path: '/detail/:type/:id',
      name: 'Detail',
      component: Detail,
      meta: {}
    },
    {
      path: '/lost/create',
      name: 'LostCreate',
      component: LostCreate,
      meta: { requiresAuth: true }
    },
    {
      path: '/picked/create',
      name: 'PickedCreate',
      component: PickedCreate,
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'Login',
      component: Login,
    },
    {
      path: '/register',
      name: 'Register',
      component: Register,
    },
    {
      path: '/profile',
      name: 'Profile',
      component: Profile,
      meta: { requiresAuth: true }
    },
    {
      path: '/claim/create/:pickedId',
      name: 'ClaimCreate',
      component: ClaimCreate,
      meta: { requiresAuth: true }
    },
    {
      path: '/my-claims',
      name: 'MyClaims',
      component: MyClaims,
      meta: { requiresAuth: true }
    },
    {
      path: '/received-claims',
      name: 'ReceivedClaims',
      component: ReceivedClaims,
      meta: { requiresAuth: true }
    },
    {
      path: '/messages',
      name: 'MyMessages',
      component: MyMessages,
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'Admin',
      component: Admin,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const isLoggedIn = !!token

  // 如果访问的是登录或注册页面，直接放行
  if (to.path === '/login' || to.path === '/register') {
    next()
    return
  }

  // 如果需要认证但没有登录，重定向到登录页面
  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
    return
  }

  // 如果需要管理员权限但不是管理员，重定向到首页
  if (to.meta.requiresAdmin && (!isLoggedIn || localStorage.getItem('role') !== '1')) {
    next('/')
    return
  }

  // 其他情况正常放行
  next()
})

export default router
