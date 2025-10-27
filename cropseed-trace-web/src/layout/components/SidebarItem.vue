<template>
    <div v-if="!item.meta || !item.meta.hidden">
        <!-- 有子菜单的情况 -->
        <el-sub-menu v-if="hasChildren" :index="resolvePath" popper-append-to-body>
            <template #title>
                <el-icon v-if="item.meta && item.meta.icon">
                    <component :is="item.meta.icon" />
                </el-icon>
                <span>{{ item.meta.title }}</span>
            </template>
            <sidebar-item v-for="child in item.children" :key="child.path" :item="child" :base-path="resolvePath" />
        </el-sub-menu>

        <!-- 没有子菜单的情况 -->
        <el-menu-item v-else :index="resolvePath">
            <el-icon v-if="item.meta && item.meta.icon">
                <component :is="item.meta.icon" />
            </el-icon>
            <template #title>
                <span>{{ item.meta.title }}</span>
            </template>
        </el-menu-item>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import { isExternal } from '@/utils/validate'

const props = defineProps({
    item: {
        type: Object,
        required: true
    },
    basePath: {
        type: String,
        default: ''
    }
})

// 是否有子菜单
const hasChildren = computed(() => {
    return props.item.children && props.item.children.length > 0
})

// 解析路径
const resolvePath = computed(() => {
    if (isExternal(props.item.path)) {
        return props.item.path
    }
    if (hasChildren.value) {
        return props.basePath
    }
    // 如果item.path已经是完整路径（以/开头），直接返回
    if (props.item.path.startsWith('/')) {
        return props.item.path
    }
    // 否则拼接basePath
    return props.basePath + '/' + props.item.path
})
</script>
