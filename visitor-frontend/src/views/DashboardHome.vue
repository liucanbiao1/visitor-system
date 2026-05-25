<template>
  <div class="dashboard-home">
    <h2>{{ $t('dashboard.welcome') }}</h2>

    <el-row :gutter="20" style="margin-top: 24px" v-loading="overviewLoading">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic :title="$t('dashboard.todayVisitors')" :value="overview.todayVisitors" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic :title="$t('dashboard.onCampus')" :value="overview.onCampus" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic :title="$t('dashboard.monthlyVisitors')" :value="overview.monthlyVisitors" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic :title="$t('dashboard.pendingApprovals')" :value="overview.pendingApprovals" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Bar Chart: Visitor Traffic -->
    <el-card style="margin-top: 20px" v-loading="trafficLoading">
      <template #header>
        <div class="chart-header">
          <span>{{ $t('dashboard.trafficStats') }}</span>
          <el-radio-group v-model="trafficPeriod" size="small" @change="fetchTrafficStats">
            <el-radio-button value="day">{{ $t('dashboard.last7Days') }}</el-radio-button>
            <el-radio-button value="week">{{ $t('dashboard.last4Weeks') }}</el-radio-button>
            <el-radio-button value="month">{{ $t('dashboard.last12Months') }}</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="barChartRef" class="chart-box"></div>
    </el-card>

    <!-- Line Chart + Pie Chart side by side -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="14">
        <el-card v-loading="timeDistLoading">
          <template #header>
            <span>{{ $t('dashboard.timeDistribution') }}</span>
          </template>
          <div ref="lineChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card v-loading="reasonDistLoading">
          <template #header>
            <span>{{ $t('dashboard.reasonDistribution') }}</span>
          </template>
          <div ref="pieChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import * as echarts from 'echarts'
import { getOverview, getTrafficStats, getTimeDistribution, getReasonDistribution } from '../api/statistics'

const { t } = useI18n()

// --- overview ---
const overviewLoading = ref(false)
const overview = reactive({ todayVisitors: 0, onCampus: 0, monthlyVisitors: 0, pendingApprovals: 0 })

const fetchOverview = async () => {
  overviewLoading.value = true
  try {
    const res = await getOverview()
    Object.assign(overview, res.data)
  } finally { overviewLoading.value = false }
}

// --- bar chart (traffic) ---
const trafficPeriod = ref('day')
const trafficLoading = ref(false)
const barChartRef = ref(null)
let barChart = null

const fetchTrafficStats = async () => {
  trafficLoading.value = true
  try {
    const res = await getTrafficStats(trafficPeriod.value)
    const data = res.data || []
    renderBarChart(data)
  } finally { trafficLoading.value = false }
}

const renderBarChart = (data) => {
  if (!barChart) return
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.label), axisLabel: { rotate: data.length > 10 ? 45 : 0 } },
    yAxis: { type: 'value', name: t('dashboard.appointmentCount') },
    series: [{ type: 'bar', data: data.map(d => d.count), itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] } }]
  })
}

// --- line chart (time distribution) ---
const timeDistLoading = ref(false)
const lineChartRef = ref(null)
let lineChart = null

const fetchTimeDistribution = async () => {
  timeDistLoading.value = true
  try {
    const res = await getTimeDistribution()
    const data = res.data || []
    renderLineChart(data)
  } finally { timeDistLoading.value = false }
}

const renderLineChart = (data) => {
  if (!lineChart) return
  const morning = t('dashboard.morning')
  const afternoon = t('dashboard.afternoon')
  const evening = t('dashboard.evening')
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: [morning, afternoon, evening], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.date) },
    yAxis: { type: 'value', name: t('dashboard.appointmentCount') },
    series: [
      { name: morning, type: 'line', data: data.map(d => d.morning), smooth: true, itemStyle: { color: '#5470c6' } },
      { name: afternoon, type: 'line', data: data.map(d => d.afternoon), smooth: true, itemStyle: { color: '#91cc75' } },
      { name: evening, type: 'line', data: data.map(d => d.evening), smooth: true, itemStyle: { color: '#fac858' } }
    ]
  })
}

// --- pie chart (reason distribution) ---
const reasonDistLoading = ref(false)
const pieChartRef = ref(null)
let pieChart = null

const fetchReasonDistribution = async () => {
  reasonDistLoading.value = true
  try {
    const res = await getReasonDistribution()
    const data = res.data || []
    renderPieChart(data)
  } finally { reasonDistLoading.value = false }
}

const renderPieChart = (data) => {
  if (!pieChart) return
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: '5%', top: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } },
      data: data.map(d => ({ name: d.name, value: d.value }))
    }]
  })
}

// --- init charts ---
const initCharts = async () => {
  await nextTick()
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
    window.addEventListener('resize', () => barChart?.resize())
  }
  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value)
    window.addEventListener('resize', () => lineChart?.resize())
  }
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    window.addEventListener('resize', () => pieChart?.resize())
  }
}

const loadAllData = () => {
  fetchOverview()
  fetchTrafficStats()
  fetchTimeDistribution()
  fetchReasonDistribution()
}

onBeforeUnmount(() => {
  barChart?.dispose()
  lineChart?.dispose()
  pieChart?.dispose()
})

onMounted(async () => {
  await initCharts()
  loadAllData()
})
</script>

<style scoped>
.dashboard-home { padding: 4px; }
.dashboard-home h2 { margin: 0; color: #303133; }
.chart-header { display: flex; align-items: center; justify-content: space-between; }
.chart-box { width: 100%; height: 340px; }
</style>
