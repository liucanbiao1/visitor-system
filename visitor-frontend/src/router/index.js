import { createRouter, createWebHistory } from 'vue-router'
import i18n from '../i18n'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: 'login', noAuth: true }
  },
  {
    path: '/apply',
    name: 'AppointmentForm',
    component: () => import('../views/AppointmentForm.vue'),
    meta: { title: 'appointment.formTitle', noAuth: true }
  },
  {
    path: '/query',
    name: 'AppointmentQuery',
    component: () => import('../views/AppointmentQuery.vue'),
    meta: { title: 'query.title', noAuth: true }
  },
  {
    path: '/dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { title: 'common.home' },
    redirect: '/dashboard/home',
    children: [
      {
        path: 'home',
        name: 'DashboardHome',
        component: () => import('../views/DashboardHome.vue'),
        meta: { title: 'nav.homeOverview' }
      },
      {
        path: 'appointment/review',
        name: 'AppointmentReview',
        component: () => import('../views/AppointmentReview.vue'),
        meta: { title: 'nav.appointmentReview' }
      },
      {
        path: 'visitor/list',
        name: 'VisitorList',
        component: () => import('../views/VisitorList.vue'),
        meta: { title: 'nav.visitorList' }
      },
      {
        path: 'access/log',
        name: 'AccessLog',
        component: () => import('../views/AccessLog.vue'),
        meta: { title: 'nav.accessLog' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: 'notFound.title', noAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const getTitle = (key) => {
  const t = i18n.global.t
  return t(key) !== key ? t(key) : 'Campus Visitor System'
}

router.beforeEach((to, from, next) => {
  const titleKey = to.meta.title
  const pageTitle = titleKey ? getTitle(titleKey) : ''
  document.title = pageTitle ? `${pageTitle} - ${getTitle('login.title')}` : getTitle('login.title')

  const token = localStorage.getItem('token')
  if (!to.meta.noAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
