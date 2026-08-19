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
          <div ref="pieChartRef" class="chart-box pie-chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import * as echarts from 'echarts'
import { getOverview, getTrafficStats, getTimeDistribution, getReasonDistribution } from '../api/statistics'
import { translateReason } from '../utils/reason'

const { t, locale } = useI18n()

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
const trafficData = ref([])
let barChart = null

const fetchTrafficStats = async () => {
  trafficLoading.value = true
  try {
    const res = await getTrafficStats(trafficPeriod.value)
    trafficData.value = res.data || []
    renderBarChart(trafficData.value)
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
const timeDistData = ref([])
let lineChart = null

const fetchTimeDistribution = async () => {
  timeDistLoading.value = true
  try {
    const res = await getTimeDistribution()
    timeDistData.value = res.data || []
    renderLineChart(timeDistData.value)
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
const reasonDistData = ref([])
let pieChart = null

const fetchReasonDistribution = async () => {
  reasonDistLoading.value = true
  try {
    const res = await getReasonDistribution()
    reasonDistData.value = res.data || []
    renderPieChart(reasonDistData.value)
  } finally { reasonDistLoading.value = false }
}

const renderPieChart = (data) => {
  if (!pieChart) return

  const total = data.reduce((sum, item) => sum + item.value, 0)

  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: {
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      icon: 'circle',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { fontSize: 11, overflow: 'break' },
      formatter: (name) => name.length > 18 ? name.slice(0, 16) + '...' : name,
      type: 'scroll',
      pageIconColor: '#409eff'
    },
    series: [{
      name: t('dashboard.reasonDistribution'),
      type: 'pie',
      radius: ['45%', '68%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        position: 'outside',
        formatter: '{b} : {d}%',
        lineHeight: 16,
        fontSize: 11,
        color: '#333'
      },
      emphasis: {
        scale: true,
        label: { show: true, fontWeight: 'bold' }
      },
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      data: data.map(d => ({ name: translateReason(d.name), value: d.value }))
    }],
    graphic: [{
      type: 'text',
      left: 'center',
      top: '40%',
      style: {
        text: `${t('dashboard.total')}\n${total}`,
        fill: '#409eff',
        fontSize: 16,
        fontWeight: 'bold',
        textAlign: 'center',
        lineHeight: 24
      },
      z: 100
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

// re-render charts with cached data when language changes
watch(locale, () => {
  renderBarChart(trafficData.value)
  renderLineChart(timeDistData.value)
  renderPieChart(reasonDistData.value)
})

onBeforeUnmount(() => {
  barChart?.dispose()
  lineChart?.dispose()
  pieChart?.dispose()
  window.removeEventListener('resize', () => {})
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
.pie-chart-box { height: 400px; }

@media (max-width: 768px) {
  .pie-chart-box { height: 360px; }
  .chart-box { height: 280px; }
}
</style>
