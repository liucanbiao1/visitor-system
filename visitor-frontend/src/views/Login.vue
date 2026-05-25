<template>
  <div class="login-container">
    <div class="login-card">
      <div class="lang-switch">
        <el-dropdown trigger="click" @command="changeLang">
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
      </div>
      <h2 class="login-title">{{ $t('login.title') }}</h2>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" :placeholder="$t('login.username')" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" :placeholder="$t('login.password')" prefix-icon="Lock" show-password
            @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="handleLogin">{{ $t('login.loginBtn') }}</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '../store'

const router = useRouter()
const userStore = useUserStore()
const { t, locale } = useI18n()
const formRef = ref(null)
const loading = ref(false)

const changeLang = (lang) => {
  localStorage.setItem('lang', lang)
  locale.value = lang
}

const form = reactive({
  username: 'admin',
  password: 'admin123'
})

const rules = computed(() => ({
  username: [{ required: true, message: t('login.usernameRequired'), trigger: 'blur' }],
  password: [{ required: true, message: t('login.passwordRequired'), trigger: 'blur' }]
}))

const handleLogin = () => {
  formRef.value.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login(form.username, form.password)
      ElMessage.success(t('login.loginSuccess'))
      router.push('/dashboard')
    } catch {
      // error already handled by request interceptor
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  position: relative;
}

.login-title {
  text-align: center;
  margin-bottom: 32px;
  color: #303133;
  font-size: 22px;
}

.lang-switch {
  position: absolute;
  top: 16px;
  right: 20px;
}
.lang-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #909399;
  font-size: 14px;
}
</style>
