<template>
    <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="(item, index) in breadcrumbList" :key="item.path"
            :to="index === breadcrumbList.length - 1 ? '' : item.path">
            {{ item.meta.title }}
        </el-breadcrumb-item>
    </el-breadcrumb>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 面包屑列表
const breadcrumbList = computed(() => {
    const matched = route.matched.filter(item => item.meta && item.meta.title)
    const first = matched[0]

    if (!isDashboard(first)) {
        matched.unshift({ path: '/dashboard', meta: { title: '首页' } })
    }

    return matched
})

// 判断是否为首页
const isDashboard = (route) => {
    const name = route && route.name
    if (!name) {
        return false
    }
    return name.trim().toLocaleLowerCase() === 'Dashboard'.toLocaleLowerCase()
}
</script>
