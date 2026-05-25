<template>
  <div class="visitor-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('visitor.title') }}</span>
        </div>
      </template>

      <el-form :inline="true" class="filter-form">
        <el-form-item :label="$t('visitor.keyword')">
          <el-input v-model="keyword" :placeholder="$t('visitor.keywordPlaceholder')" clearable style="width: 220px"
            @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="name" :label="$t('visitor.name')" min-width="100" />
        <el-table-column prop="phone" :label="$t('visitor.phone')" min-width="130" />
        <el-table-column prop="idCard" :label="$t('visitor.idCard')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="gender" :label="$t('visitor.gender')" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? $t('visitor.male') : row.gender === 2 ? $t('visitor.female') : $t('status.unknown') }}
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('visitor.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? $t('status.normal') : $t('status.blacklisted') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('visitor.createTime')" min-width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('visitor.operation')" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="danger" size="small" @click="handleToggle(row)">
              {{ $t('visitor.addBlacklist') }}
            </el-button>
            <el-button v-else type="success" size="small" @click="handleToggle(row)">
              {{ $t('visitor.removeBlacklist') }}
            </el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import request from '../api/request'

const { t } = useI18n()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ')
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/visitor/page',
      method: 'get',
      params: {
        page: page.value,
        pageSize: pageSize.value,
        keyword: keyword.value || undefined
      }
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
  keyword.value = ''
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

const handleToggle = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? t('visitor.addBlacklist') : t('visitor.removeBlacklist')
  const confirmMsg = newStatus === 0
    ? t('visitor.confirmBlacklist', { name: row.name })
    : t('visitor.confirmRemoveBlacklist', { name: row.name })
  ElMessageBox.confirm(confirmMsg, t('visitor.operationConfirm'), {
    confirmButtonText: t('common.confirm'),
    type: newStatus === 0 ? 'warning' : 'success'
  }).then(async () => {
    await request({
      url: `/visitor/${row.id}/status`,
      method: 'put',
      data: { status: newStatus }
    })
    ElMessage.success(newStatus === 0
      ? t('visitor.addBlacklistSuccess')
      : t('visitor.removeBlacklistSuccess'))
    fetchData()
  }).catch(() => {})
}

onMounted(() => fetchData())
</script>

<style scoped>
.visitor-page { padding: 4px; }
.card-header { font-size: 16px; font-weight: 600; }
.filter-form { margin-bottom: 8px; }
</style>
