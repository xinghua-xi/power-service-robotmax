// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import Screensaver from '@/views/Screensaver.vue'
import Login from '@/views/Login.vue'
import Home from '@/views/Home.vue'  // 电力服务页面

const routes = [
    {
        path: '/',
        name: 'Screensaver',
        component: Screensaver
    },
    {
        path: '/login',
        name: 'Login',
        component: Login
    },
    {
        path: '/home',
        name: 'Home',
        component: Home,
        meta: { requiresAuth: true } // 需要登录才能访问
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const isAuthenticated = localStorage.getItem('isLoggedIn') === 'true'

    if (to.meta.requiresAuth && !isAuthenticated) {
        // 如果需要登录但用户未登录，重定向到登录页面
        next('/login')
    } else if (to.name === 'Login' && isAuthenticated) {
        // 如果用户已登录但访问登录页面，重定向到主页
        next('/home')
    } else {
        next()
    }
})

export default router