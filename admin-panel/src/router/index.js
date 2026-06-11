import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据总览', icon: 'Odometer' }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/UserManagement.vue'),
        meta: { title: '用户管理', icon: 'UserFilled' }
      },
      {
        path: 'merchants',
        name: 'MerchantManagement',
        component: () => import('@/views/MerchantManagement.vue'),
        meta: { title: '商户管理', icon: 'Shop' }
      },
      {
        path: 'stalls',
        name: 'StallOverview',
        component: () => import('@/views/StallOverview.vue'),
        meta: { title: '档口信息总览', icon: 'Grid' }
      },
      {
        path: 'dishes',
        name: 'DishAudit',
        component: () => import('@/views/DishAudit.vue'),
        meta: { title: '菜品信息总览', icon: 'Dish' }
      },
      {
        path: 'data-query',
        name: 'DataQuery',
        component: () => import('@/views/DataQuery.vue'),
        meta: { title: '全局数据查询', icon: 'Search' }
      },
      {
        path: 'database',
        name: 'DatabaseMaintenance',
        component: () => import('@/views/DatabaseMaintenance.vue'),
        meta: { title: '数据库维护', icon: 'Coin' }
      },
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
