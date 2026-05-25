<template>
  <div class="review-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('review.title') }}</span>
        </div>
      </template>

      <el-form :inline="true" class="filter-form">
        <el-form-item :label="$t('review.filterStatus')">
          <el-select v-model="filterStatus" :placeholder="$t('common.all')" clearable style="width: 140px" @change="handleSearch">
            <el-option :label="$t('status.pending')" :value="0" />
            <el-option :label="$t('status.approved')" :value="1" />
            <el-option :label="$t('status.rejected')" :value="2" />
            <el-option :label="$t('status.completed')" :value="3" />
            <el-option :label="$t('status.cancelled')" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('review.filterKeyword')">
          <el-input v-model="filterKeyword" :placeholder="$t('review.keywordPlaceholder')" clearable style="width: 220px"
            @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="visitorName" :label="$t('review.visitorName')" min-width="100" />
        <el-table-column prop="visitorPhone" :label="$t('review.visitorPhone')" min-width="130" />
        <el-table-column prop="appointmentTime" :label="$t('review.appointmentTime')" min-width="160">
          <template #default="{ row }">
            {{ formatTime(row.appointmentTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="visitReason" :label="$t('review.visitReason')" min-width="150" show-overflow-tooltip />
        <el-table-column prop="hostName" :label="$t('review.hostName')" min-width="100" />
        <el-table-column prop="hostDept" :label="$t('review.hostDept')" min-width="120" show-overflow-tooltip />
        <el-table-column prop="status" :label="$t('review.status')" min-width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('review.createTime')" min-width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('review.operation')" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="handleApprove(row)">{{ $t('review.approve') }}</el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">{{ $t('review.reject') }}</el-button>
            </template>
            <span v-else class="reviewed-text">-</span>
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

    <el-dialog v-model="rejectDialogVisible" :title="$t('review.rejectReason')" width="420px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" :placeholder="$t('review.rejectPlaceholder')" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="danger" :loading="rejectLoading" @click="confirmReject">{{ $t('review.confirmReject') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getAppointmentList, reviewAppointment } from '../api/appointment'

const { t } = useI18n()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const filterStatus = ref(null)
const filterKeyword = ref('')
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const rejectLoading = ref(false)
const currentRow = ref(null)

const statusMap = { 0: 'status.pending', 1: 'status.approved', 2: 'status.rejected', 3: 'status.completed', 4: 'status.cancelled' }
const statusTypeMap = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info', 4: 'info' }

const statusText = (s) => t(statusMap[s] || 'status.unknown')
const statusType = (s) => statusTypeMap[s] || 'info'
const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ')
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAppointmentList({
      page: page.value,
      pageSize: pageSize.value,
      status: filterStatus.value,
      keyword: filterKeyword.value || undefined
    })
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  fetchData()
}

const handleReset = () => {
  filterStatus.value = null
  filterKeyword.value = ''
  page.value = 1
  fetchData()
}

const onPageChange = (p) => {
  page.value = p
  fetchData()
}

const onSizeChange = (s) => {
  pageSize.value = s
  page.value = 1
  fetchData()
}

const handleApprove = (row) => {
  ElMessageBox.confirm(
    t('review.confirmApprove', { name: row.visitorName }),
    t('review.reviewConfirm'),
    {
      confirmButtonText: t('review.confirmApproveBtn'),
      type: 'success'
    }
  ).then(async () => {
    await reviewAppointment(row.id, { status: 1 })
    ElMessage.success(t('review.approveSuccess'))
    fetchData()
  }).catch(() => {})
}

const handleReject = (row) => {
  currentRow.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning(t('review.rejectReasonRequired'))
    return
  }
  rejectLoading.value = true
  try {
    await reviewAppointment(currentRow.value.id, { status: 2, rejectReason: rejectReason.value })
    ElMessage.success(t('review.rejectSuccess'))
    rejectDialogVisible.value = false
    fetchData()
  } finally {
    rejectLoading.value = false
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.review-page { padding: 4px; }
.card-header { font-size: 16px; font-weight: 600; }
.filter-form { margin-bottom: 8px; }
.reviewed-text { color: #c0c4cc; }
</style>
