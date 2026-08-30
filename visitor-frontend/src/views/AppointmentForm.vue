<template>
  <div class="apply-container">
    <div class="apply-card">
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
      <h2 class="apply-title">{{ $t('appointment.formTitle') }}</h2>
      <el-form :model="form" :rules="rules" ref="formRef" size="large" label-width="100px">
        <el-form-item :label="$t('appointment.name')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('appointment.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('appointment.phone')" prop="phone">
          <el-input v-model="form.phone" :placeholder="$t('appointment.phonePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('appointment.idCard')" prop="idCard">
          <el-input v-model="form.idCard" :placeholder="$t('appointment.idCardPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('appointment.gender')" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">{{ $t('appointment.male') }}</el-radio>
            <el-radio :value="2">{{ $t('appointment.female') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('appointment.hostName')" prop="hostName">
          <el-input v-model="form.hostName" :placeholder="$t('appointment.hostNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('appointment.hostDept')" prop="hostDept">
          <el-input v-model="form.hostDept" :placeholder="$t('appointment.hostDeptPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('appointment.appointmentTime')" prop="appointmentTime">
          <el-date-picker
            v-model="form.appointmentTime"
            type="datetime"
            :placeholder="$t('appointment.timePlaceholder')"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('appointment.visitReason')" prop="visitReason">
          <el-select v-model="form.visitReason" :placeholder="$t('appointment.reasonPlaceholder')" style="width: 100%"
            @change="handleReasonChange">
            <el-option v-for="option in reasonOptions" :key="option.value" :label="$t(option.key)" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.visitReason === 'Other'" :label="$t('reason.otherDetail')" prop="reasonDetail">
          <el-input v-model="form.reasonDetail" :placeholder="$t('reason.otherPlaceholder')" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="handleSubmit">
            {{ $t('appointment.submitBtn') }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="query-link">
        <el-link type="primary" @click="$router.push('/query')">{{ $t('appointment.queryLink') }}</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { submitAppointment } from '../api/appointment'
import { REASON_OPTIONS } from '../utils/reason'

const { t, locale } = useI18n()
const formRef = ref(null)
const loading = ref(false)
const reasonOptions = REASON_OPTIONS

const changeLang = (lang) => {
  localStorage.setItem('lang', lang)
  locale.value = lang
}

const form = reactive({
  name: '',
  phone: '',
  idCard: '',
  gender: 0,
  hostName: '',
  hostDept: '',
  appointmentTime: '',
  visitReason: '',
  reasonDetail: ''
})

const handleReasonChange = () => {
  if (form.visitReason !== 'Other') {
    form.reasonDetail = ''
  }
  formRef.value?.clearValidate('reasonDetail')
}

const rules = computed(() => ({
  name: [{ required: true, message: t('appointment.nameRequired'), trigger: 'blur' }],
  phone: [
    { required: true, message: t('appointment.phoneRequired'), trigger: 'blur' },
    { pattern: /^\+?[\d(][\d\s\-()]{4,19}$/, message: t('appointment.phoneInvalid'), trigger: 'blur' }
  ],
  hostName: [{ required: true, message: t('appointment.hostNameRequired'), trigger: 'blur' }],
  appointmentTime: [{ required: true, message: t('appointment.timeRequired'), trigger: 'change' }],
  visitReason: [{ required: true, message: t('appointment.reasonRequired'), trigger: 'blur' }],
  reasonDetail: [{
    required: true,
    trigger: 'blur',
    validator: (rule, value, callback) => {
      if (form.visitReason === 'Other' && !value.trim()) {
        callback(new Error(t('reason.otherRequired')))
      } else {
        callback()
      }
    }
  }]
}))

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

const handleSubmit = () => {
  formRef.value.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      await submitAppointment({ ...form, idCard: form.idCard || undefined })
      ElMessage.success(t('appointment.submitSuccess'))
      formRef.value.resetFields()
    } catch {
      // handled by request interceptor
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.apply-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 100vh;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.apply-card {
  width: 560px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  position: relative;
}
.apply-title {
  text-align: center;
  margin-bottom: 32px;
  color: #303133;
  font-size: 22px;
}
.query-link {
  text-align: center;
  margin-top: 8px;
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
