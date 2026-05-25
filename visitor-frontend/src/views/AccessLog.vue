<template>
  <div class="access-log-page">
    <!-- Overstay alerts -->
    <el-alert
      v-for="alert in overstayAlerts"
      :key="alert.id"
      type="error"
      :closable="false"
      show-icon
      style="margin-bottom: 8px"
    >
      <template #title>
        <strong>{{ $t('access.overstayAlert') }}</strong>
        {{ $t('access.overstayMsg', { name: alert.visitorName, phone: alert.visitorPhone, time: formatTime(alert.entryTime) }) }}
      </template>
    </el-alert>

    <el-card>
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- Tab 1: Entry Registration -->
        <el-tab-pane :label="$t('access.entryReg')" name="entry">
          <div class="entry-section">
            <el-form :inline="true">
              <el-form-item :label="$t('access.phone')">
                <el-input v-model="searchPhone" :placeholder="$t('access.phonePlaceholder')" style="width: 220px" clearable
                  @keyup.enter="handleSearchVisitor" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="searchLoading" @click="handleSearchVisitor">{{ $t('access.searchVisitor') }}</el-button>
              </el-form-item>
            </el-form>

            <div v-if="searchedVisitor" class="visitor-result">
              <el-descriptions :column="2" border size="small" :title="$t('access.visitorInfo')">
                <el-descriptions-item :label="$t('access.name')">{{ searchedVisitor.name }}</el-descriptions-item>
                <el-descriptions-item :label="$t('access.phone')">{{ searchedVisitor.phone }}</el-descriptions-item>
                <el-descriptions-item :label="$t('access.idCard')">{{ searchedVisitor.idCard || '-' }}</el-descriptions-item>
                <el-descriptions-item :label="$t('access.status')">
                  <el-tag :type="searchedVisitor.status === 1 ? 'success' : 'danger'">
                    {{ searchedVisitor.status === 1 ? $t('status.normal') : $t('status.blacklisted') }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>

              <div v-if="searchedVisitor.status === 0" style="margin-top: 12px">
                <el-alert type="error" :closable="false" show-icon
                  :title="$t('access.blacklistAlert')" />
              </div>

              <div v-if="approvedAppointments.length > 0" style="margin-top: 16px">
                <h4>{{ $t('access.linkedAppointments') }}</h4>
                <el-table :data="approvedAppointments" border stripe size="small">
                  <el-table-column prop="appointmentTime" :label="$t('access.appointmentTime')" min-width="150">
                    <template #default="{ row }">{{ formatTime(row.appointmentTime) }}</template>
                  </el-table-column>
                  <el-table-column prop="hostName" :label="$t('access.hostName')" min-width="100" />
                  <el-table-column prop="hostDept" :label="$t('access.hostDept')" min-width="120" />
                  <el-table-column prop="visitReason" :label="$t('access.visitReason')" min-width="120" show-overflow-tooltip />
                  <el-table-column :label="$t('access.operation')" width="120">
                    <template #default="{ row }">
                      <el-button type="primary" size="small" @click="handleEntry(searchedVisitor.id, row.id)">
                        {{ $t('access.entryRegBtn') }}
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>

              <div style="margin-top: 16px">
                <el-button type="success" @click="handleEntry(searchedVisitor.id, null)">
                  {{ $t('access.manualEntry') }}
                </el-button>
              </div>
            </div>

            <el-empty v-if="searchedVisitor === null && searchAttempted" :description="$t('access.visitorNotFound')" />
          </div>
        </el-tab-pane>

        <!-- Tab 2: Exit Registration -->
        <el-tab-pane :label="$t('access.exitReg')" name="exit">
          <el-table :data="onCampusList" border stripe v-loading="onCampusLoading">
            <el-table-column prop="visitorName" :label="$t('access.visitorName')" min-width="100" />
            <el-table-column prop="visitorPhone" :label="$t('access.visitorPhone')" min-width="130" />
            <el-table-column prop="entryTime" :label="$t('access.entryTime')" min-width="160">
              <template #default="{ row }">{{ formatTime(row.entryTime) }}</template>
            </el-table-column>
            <el-table-column :label="$t('access.duration')" min-width="100">
              <template #default="{ row }">
                {{ calcDuration(row.entryTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="deviceName" :label="$t('access.device')" min-width="100">
              <template #default="{ row }">{{ row.deviceName || $t('access.frontGate') }}</template>
            </el-table-column>
            <el-table-column :label="$t('access.operation')" width="120">
              <template #default="{ row }">
                <el-button type="warning" size="small" @click="handleExit(row)">{{ $t('access.exitRegBtn') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="onCampusList.length === 0 && !onCampusLoading" :description="$t('access.noOnCampus')" />
        </el-tab-pane>

        <!-- Tab 3: Access Records -->
        <el-tab-pane :label="$t('access.records')" name="records">
          <el-form :inline="true" class="filter-form">
            <el-form-item :label="$t('access.filterStatus')">
              <el-select v-model="filterAccessStatus" :placeholder="$t('common.all')" clearable style="width: 140px" @change="handleRecordSearch">
                <el-option :label="$t('access.passSuccess')" :value="1" />
                <el-option :label="$t('access.passFail')" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('common.keyword')">
              <el-input v-model="filterKeyword" :placeholder="$t('visitor.keywordPlaceholder')" clearable style="width: 220px"
                @keyup.enter="handleRecordSearch" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRecordSearch">{{ $t('common.search') }}</el-button>
              <el-button @click="handleRecordReset">{{ $t('common.reset') }}</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="recordTable" border stripe v-loading="recordLoading">
            <el-table-column prop="visitorName" :label="$t('access.visitorName')" min-width="100" />
            <el-table-column prop="visitorPhone" :label="$t('access.visitorPhone')" min-width="130" />
            <el-table-column prop="entryTime" :label="$t('access.entryTime')" min-width="160">
              <template #default="{ row }">{{ formatTime(row.entryTime) }}</template>
            </el-table-column>
            <el-table-column prop="exitTime" :label="$t('access.exitTime')" min-width="160">
              <template #default="{ row }">{{ formatTime(row.exitTime) }}</template>
            </el-table-column>
            <el-table-column prop="accessStatus" :label="$t('access.accessStatus')" min-width="100">
              <template #default="{ row }">
                <el-tag :type="row.accessStatus === 1 ? 'success' : 'danger'">
                  {{ row.accessStatus === 1 ? $t('access.success') : $t('access.fail') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="failReason" :label="$t('access.failReason')" min-width="150" show-overflow-tooltip />
            <el-table-column prop="deviceName" :label="$t('access.device')" min-width="100">
              <template #default="{ row }">{{ row.deviceName || '-' }}</template>
            </el-table-column>
            <el-table-column prop="createTime" :label="$t('access.recordTime')" min-width="160">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="recordTotal > 0"
            style="margin-top: 20px; justify-content: flex-end"
            background
            layout="total, sizes, prev, pager, next"
            :total="recordTotal"
            :page-size="recordPageSize"
            :current-page="recordPage"
            :page-sizes="[10, 20, 50]"
            @size-change="onRecordSizeChange"
            @current-change="onRecordPageChange"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { recordEntry, recordExit, getAccessLogPage, getOnCampusVisitors, getOverstayAlerts } from '../api/access-log'
import request from '../api/request'

const { t } = useI18n()

const activeTab = ref('entry')

// --- overstay ---
const overstayAlerts = ref([])
const fetchOverstay = async () => {
  try {
    const res = await getOverstayAlerts()
    overstayAlerts.value = res.data || []
  } catch { /* ignore */ }
}

// --- entry ---
const searchPhone = ref('')
const searchLoading = ref(false)
const searchAttempted = ref(false)
const searchedVisitor = ref(null)
const approvedAppointments = ref([])

const handleSearchVisitor = async () => {
  if (!searchPhone.value) return
  searchLoading.value = true
  searchAttempted.value = true
  searchedVisitor.value = null
  approvedAppointments.value = []
  try {
    const res = await request({ url: '/visitor/page', method: 'get', params: { page: 1, pageSize: 1, keyword: searchPhone.value } })
    const list = res.data.list || []
    if (list.length > 0) {
      searchedVisitor.value = list[0]
      const apptRes = await request({ url: '/appointment/list', method: 'get', params: { page: 1, pageSize: 50, status: 1, keyword: searchPhone.value } })
      approvedAppointments.value = apptRes.data.list || []
    }
  } catch { /* ignore */ }
  finally { searchLoading.value = false }
}

const handleEntry = async (visitorId, appointmentId) => {
  const confirmMsg = appointmentId ? t('access.confirmEntry') : t('access.confirmManualEntry')
  try {
    await ElMessageBox.confirm(confirmMsg, t('access.entryConfirmTitle'), { confirmButtonText: t('access.confirmEntryBtn'), type: 'success' })
    const res = await recordEntry({ visitorId, appointmentId, deviceName: t('access.frontGate') })
    if (res.data.alert) {
      ElMessage.warning(res.data.message)
    } else {
      ElMessage.success(t('access.entrySuccess'))
    }
    searchedVisitor.value = null
    searchAttempted.value = false
    searchPhone.value = ''
    approvedAppointments.value = []
    fetchOverstay()
    fetchOnCampus()
  } catch { /* cancelled */ }
}

// --- exit ---
const onCampusList = ref([])
const onCampusLoading = ref(false)
const fetchOnCampus = async () => {
  onCampusLoading.value = true
  try {
    const res = await getOnCampusVisitors()
    onCampusList.value = res.data || []
  } finally { onCampusLoading.value = false }
}

const handleExit = async (row) => {
  try {
    await ElMessageBox.confirm(
      t('access.confirmExit', { name: row.visitorName }),
      t('access.exitConfirmTitle'),
      { confirmButtonText: t('access.confirmExitBtn'), type: 'warning' }
    )
    await recordExit(row.id)
    ElMessage.success(t('access.exitSuccess'))
    fetchOnCampus()
    fetchOverstay()
    if (activeTab.value === 'records') fetchRecords()
  } catch { /* cancelled */ }
}

// --- records ---
const recordTable = ref([])
const recordTotal = ref(0)
const recordPage = ref(1)
const recordPageSize = ref(10)
const recordLoading = ref(false)
const filterKeyword = ref('')
const filterAccessStatus = ref(null)

const fetchRecords = async () => {
  recordLoading.value = true
  try {
    const res = await getAccessLogPage({
      page: recordPage.value,
      pageSize: recordPageSize.value,
      keyword: filterKeyword.value || undefined,
      accessStatus: filterAccessStatus.value
    })
    recordTable.value = res.data.list || []
    recordTotal.value = res.data.total || 0
  } finally { recordLoading.value = false }
}

const handleRecordSearch = () => {
  recordPage.value = 1
  fetchRecords()
}

const handleRecordReset = () => {
  filterKeyword.value = ''
  filterAccessStatus.value = null
  recordPage.value = 1
  fetchRecords()
}

const onRecordPageChange = (p) => { recordPage.value = p; fetchRecords() }
const onRecordSizeChange = (s) => { recordPageSize.value = s; recordPage.value = 1; fetchRecords() }

// --- common ---
const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ')
}

const calcDuration = (entryTime) => {
  if (!entryTime) return '-'
  const entry = new Date(entryTime.replace(' ', 'T'))
  const now = new Date()
  const diff = Math.floor((now - entry) / 1000)
  const hours = Math.floor(diff / 3600)
  const minutes = Math.floor((diff % 3600) / 60)
  if (hours > 0) return `${hours}h ${minutes}m`
  return `${minutes}m`
}

const onTabChange = (name) => {
  if (name === 'exit') fetchOnCampus()
  if (name === 'records') fetchRecords()
}

onMounted(() => {
  fetchOverstay()
})
</script>

<style scoped>
.access-log-page { padding: 4px; }
.filter-form { margin-bottom: 8px; }
.visitor-result { margin-top: 16px; }
.entry-section { min-height: 200px; }
</style>
