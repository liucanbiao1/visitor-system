<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">{{ $t('nav.systemName') }}</div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard/home">
          <el-icon><DataAnalysis /></el-icon>
          <span>{{ $t('nav.homeOverview') }}</span>
        </el-menu-item>
        <el-sub-menu index="visitor">
          <template #title>
            <el-icon><Avatar /></el-icon>
            <span>{{ $t('nav.visitorMgmt') }}</span>
          </template>
          <el-menu-item index="/dashboard/visitor/list">{{ $t('nav.visitorList') }}</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="appointment">
          <template #title>
            <el-icon><Calendar /></el-icon>
            <span>{{ $t('nav.appointmentMgmt') }}</span>
          </template>
          <el-menu-item index="/dashboard/appointment/review">{{ $t('nav.appointmentReview') }}</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="access">
          <template #title>
            <el-icon><Monitor /></el-icon>
            <span>{{ $t('nav.accessMgmt') }}</span>
          </template>
          <el-menu-item index="/dashboard/access/log">{{ $t('nav.accessLog') }}</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="header-title">{{ $t('nav.headerTitle') }}</span>
        <div class="header-right">
          <el-dropdown trigger="click" @command="changeLang" style="margin-right: 16px">
            <span class="lang-btn">
              <el-icon><Switch /></el-icon>
              {{ $t('common.language') }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="zh">{{ $t('common.chinese') }}</el-dropdown-item>
                <el-dropdown-item command="en">{{ $t('common.english') }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username || $t('login.admin') }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>{{ $t('login.modifyPassword') }}</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">{{ $t('login.logout') }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '../store'

const router = useRouter()
const userStore = useUserStore()
const { locale } = useI18n()

const changeLang = (lang) => {
  localStorage.setItem('lang', lang)
  locale.value = lang
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background-color: #304156; overflow-y: auto; }
.logo {
  height: 60px; line-height: 60px; text-align: center;
  color: #fff; font-size: 18px; font-weight: bold;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.el-menu { border-right: none; }
.header {
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.08); padding: 0 20px;
}
.header-title { font-size: 16px; font-weight: 600; color: #303133; }
.header-right { display: flex; align-items: center; }
.lang-btn {
  display: flex; align-items: center; gap: 4px; cursor: pointer;
  color: #909399; font-size: 14px;
}
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.username { color: #606266; }
</style>
