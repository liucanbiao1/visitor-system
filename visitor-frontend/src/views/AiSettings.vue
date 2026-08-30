<template>
  <div class="ai-settings-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('aiReview.title') }}</span>
        </div>
      </template>

      <el-alert
        v-if="!settings.apiKeyConfigured"
        type="warning"
        :closable="false"
        show-icon
        :title="$t('aiReview.apiKeyMissing')"
        style="margin-bottom: 16px"
      />

      <el-form label-width="140px" style="max-width: 560px">
        <el-form-item :label="$t('aiReview.enableLabel')">
          <el-switch v-model="settings.enabled" :loading="toggleLoading" @change="handleToggle" />
          <span class="enable-desc">{{ $t('aiReview.enableDesc') }}</span>
        </el-form-item>
      </el-form>

      <el-descriptions :column="2" border size="small" style="max-width: 560px">
        <el-descriptions-item :label="$t('aiReview.modelLabel')">{{ settings.model }}</el-descriptions-item>
        <el-descriptions-item :label="$t('aiReview.baseUrlLabel')">{{ settings.baseUrl }}</el-descriptions-item>
        <el-descriptions-item :label="$t('aiReview.apiKeyLabel')">
          {{ settings.apiKeyConfigured ? settings.apiKeyMasked : $t('aiReview.notConfigured') }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('aiReview.statusLabel')">
          <el-tag :type="settings.enabled ? 'success' : 'info'">
            {{ settings.enabled ? $t('aiReview.statusOn') : $t('aiReview.statusOff') }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>{{ $t('aiReview.logTitle') }}</span>
        </div>
      </template>

      <el-table :data="logs" border stripe v-loading="logLoading">
        <el-table-column prop="visitorName" :label="$t('aiReview.visitorName')" min-width="100" />
        <el-table-column prop="visitorPhone" :label="$t('aiReview.visitorPhone')" min-width="130" />
        <el-table-column :label="$t('aiReview.decision')" min-width="90">
          <template #default="{ row }">
            <el-tag v-if="row.success === 1" :type="row.decision === 1 ? 'success' : 'danger'">
              {{ row.decision === 1 ? $t('aiReview.decisionApprove') : $t('aiReview.decisionReject') }}
            </el-tag>
            <el-tag v-else type="info">{{ $t('aiReview.failed') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('aiReview.reason')" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.success === 1 && row.decision === 2">{{ translateRejectReason(row.reason) }}</span>
            <span v-else-if="row.success === 0">{{ row.errorMessage }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="confidence" :label="$t('aiReview.confidence')" min-width="90">
          <template #default="{ row }">
            <span v-if="row.confidence != null">{{ row.confidence }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" :label="$t('aiReview.risk')" min-width="90">
          <template #default="{ row }">
            <el-tag v-if="row.riskLevel" :type="riskType(row.riskLevel)" size="small">{{ row.riskLevel }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="latencyMs" :label="$t('aiReview.latency')" min-width="100">
          <template #default="{ row }">
            <span v-if="row.latencyMs != null">{{ row.latencyMs }}ms</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="modelName" :label="$t('aiReview.model')" min-width="110" />
        <el-table-column prop="createTime" :label="$t('aiReview.time')" min-width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        style="margin-top: 20px; justify-content: flex-end"
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        :page-sizes="[10, 20, 50]"
        @size-change="onSizeChange"
        @current-change="onPageChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getAiSettings, updateAiSettings, getAiLogs } from '../api/ai'
import { translateRejectReason } from '../utils/reason'

const { t } = useI18n()

const settings = ref({
  enabled: false,
  model: '',
  baseUrl: '',
  apiKeyConfigured: false,
  apiKeyMasked: ''
})
const toggleLoading = ref(false)

const logs = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const logLoading = ref(false)

const fetchSettings = async () => {
  try {
    const res = await getAiSettings()
    settings.value = res.data
  } catch { /* handled by interceptor */ }
}

const fetchLogs = async () => {
  logLoading.value = true
  try {
    const res = await getAiLogs({ page: page.value, pageSize: pageSize.value })
    logs.value = res.data.list || []
    total.value = res.data.total || 0
  } catch { /* handled by interceptor */ }
  finally { logLoading.value = false }
}

const handleToggle = async (value) => {
  toggleLoading.value = true
  try {
    await updateAiSettings(value)
    ElMessage.success(t('aiReview.saveSuccess'))
    fetchLogs()
  } catch {
    settings.value.enabled = !value
  } finally {
    toggleLoading.value = false
  }
}

const onPageChange = (p) => { page.value = p; fetchLogs() }
const onSizeChange = (s) => { pageSize.value = s; page.value = 1; fetchLogs() }

const riskType = (level) => {
  if (level === 'high') return 'danger'
  if (level === 'medium') return 'warning'
  return 'success'
}

const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ')
}

onMounted(() => {
  fetchSettings()
  fetchLogs()
})
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}
.enable-desc {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
}
</style>
