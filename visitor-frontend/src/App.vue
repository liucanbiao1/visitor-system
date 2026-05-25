<template>
  <el-config-provider :locale="elLocale">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import en from 'element-plus/dist/locale/en.mjs'

const { locale, t } = useI18n()
const route = useRoute()
const locales = { zh: zhCn, en }
const elLocale = computed(() => locales[locale.value] || zhCn)

const getTitle = (key) => {
  const text = t(key)
  return text !== key ? text : 'Campus Visitor System'
}

watch(locale, () => {
  const titleKey = route.meta?.title
  if (titleKey) {
    document.title = `${getTitle(titleKey)} - ${getTitle('login.title')}`
  } else {
    document.title = getTitle('login.title')
  }
}, { immediate: true })
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}
</style>
