<template>
  <div class="query-container">
    <div class="query-card">
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
      <h2 class="query-title">{{ $t('query.title') }}</h2>
      <el-form :inline="true" size="large" class="search-form">
        <el-form-item :label="$t('query.phoneLabel')">
          <el-input v-model="phone" :placeholder="$t('query.phonePlaceholder')" style="width: 300px" clearable
            @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleQuery">{{ $t('query.queryBtn') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table v-if="searched && list.length > 0" :data="list" border stripe style="width: 100%">
        <el-table-column prop="visitorName" :label="$t('query.name')" min-width="100" />
        <el-table-column prop="appointmentTime" :label="$t('query.appointmentTime')" min-width="160">
          <template #default="{ row }">
            {{ formatTime(row.appointmentTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('query.visitReason')" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ translateReason(row.visitReason) }}
            <span v-if="row.reasonDetail" class="reason-detail">{{ row.reasonDetail }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="hostName" :label="$t('query.hostName')" min-width="100" />
        <el-table-column prop="hostDept" :label="$t('query.hostDept')" min-width="120" show-overflow-tooltip />
        <el-table-column prop="status" :label="$t('query.status')" min-width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('query.rejectReason')" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ translateRejectReason(row.rejectReason) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('query.createTime')" min-width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="searched && list.length === 0" :description="$t('query.noRecords')" />

      <div class="apply-link">
        <el-link type="primary" @click="$router.push('/apply')">{{ $t('query.backLink') }}</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { queryAppointments } from '../api/appointment'
import { translateReason, translateRejectReason } from '../utils/reason'

const { t, locale } = useI18n()
const phone = ref('')
const list = ref([])
const loading = ref(false)
const searched = ref(false)

const statusMap = computed(() => ({
  0: t('status.pending'),
  1: t('status.approved'),
  2: t('status.rejected'),
  3: t('status.completed'),
  4: t('status.cancelled')
}))
const statusTypeMap = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info', 4: 'info' }

const statusText = (s) => statusMap.value[s] || t('status.unknown')
const statusType = (s) => statusTypeMap[s] || 'info'

const changeLang = (lang) => {
  localStorage.setItem('lang', lang)
  locale.value = lang
}

const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ')
}

const handleQuery = async () => {
  if (!phone.value) return
  loading.value = true
  searched.value = true
  try {
    const res = await queryAppointments(phone.value)
    list.value = res.data || []
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.query-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 100vh;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.query-card {
  width: 960px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  position: relative;
}
.query-title {
  text-align: center;
  margin-bottom: 24px;
  color: #303133;
  font-size: 22px;
}
.search-form {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}
.apply-link {
  text-align: center;
  margin-top: 16px;
}
.lang-switch {
  position: absolute;
  top: 16px;
  right: 20px;
}
.reason-detail {
  margin-left: 4px;
  color: #909399;
  font-size: 12px;
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
