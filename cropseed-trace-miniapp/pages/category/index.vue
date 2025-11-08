<template>
    <view class="category-page">
        <view class="sidebar">
            <scroll-view scroll-y class="sidebar-scroll">
                <view v-for="item in categories" :key="item.id" class="sidebar-item"
                    :class="{ active: activeId === item.id }" @tap="selectCategory(item)">
                    {{ item.categoryName }}
                </view>
            </scroll-view>
        </view>
        <view class="content">
            <scroll-view scroll-y class="content-scroll">
                <view class="content-header">
                    <text class="title">{{ activeCategory?.categoryName || '全部分类' }}</text>
                    <text class="sub-title">共 {{ subCategories.length }} 个品类</text>
                </view>

                <view class="sub-list">
                    <view v-for="item in subCategories" :key="item.id" class="sub-item" @tap="goProductList(item)">
                        <view class="sub-name">{{ item.categoryName }}</view>
                        <view class="sub-count">包含 {{ item.children?.length || 0 }} 个品种</view>
                    </view>
                </view>

                <view v-if="!subCategories.length" class="empty">暂无子分类</view>
            </scroll-view>
        </view>
    </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCategoryTree } from '@/api/product.js'

const categories = ref([])
const activeId = ref(null)

const activeCategory = computed(() => categories.value.find((item) => item.id === activeId.value) || null)
const subCategories = computed(() => activeCategory.value?.children || [])

onMounted(() => {
    loadCategory()
})

async function loadCategory() {
    try {
        const data = await getCategoryTree()
        categories.value = data || []
        if (categories.value.length) {
            activeId.value = categories.value[0].id
        }
    } catch (error) {
        console.error('获取分类失败', error)
        uni.showToast({ title: '获取分类失败', icon: 'none' })
    }
}

function selectCategory(item) {
    activeId.value = item.id
}

function goProductList(item) {
    uni.navigateTo({ url: `/pages/product/list?categoryId=${item.id}` })
}
</script>

<style scoped>
.category-page {
    display: flex;
    height: 100vh;
    background: #f5f5f5;
    box-sizing: border-box;
}

.sidebar {
    width: 200rpx;
    background: #ffffff;
    border-right: 1rpx solid #e8e8e8;
    flex-shrink: 0;
}

.sidebar-scroll {
    height: 100%;
    box-sizing: border-box;
}

.sidebar-item {
    padding: 36rpx 20rpx;
    font-size: 28rpx;
    color: #333333;
    text-align: center;
    position: relative;
    transition: all 0.2s;
    box-sizing: border-box;
    min-height: 100rpx;
    display: flex;
    align-items: center;
    justify-content: center;
}

.sidebar-item::after {
    content: '';
    position: absolute;
    left: 0;
    bottom: 0;
    right: 0;
    height: 1rpx;
    background: #f0f0f0;
    transform: scaleY(0.5);
}

.sidebar-item:last-child::after {
    display: none;
}

.sidebar-item.active {
    color: #2b9939;
    font-weight: 600;
    background: #f0f9f2;
    position: relative;
}

.sidebar-item.active::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6rpx;
    background: #2b9939;
}

.content {
    flex: 1;
    overflow: hidden;
    box-sizing: border-box;
}

.content-scroll {
    height: 100%;
    padding: 32rpx 24rpx;
    box-sizing: border-box;
}

.content-header {
    margin-bottom: 32rpx;
    padding-bottom: 24rpx;
    border-bottom: 1rpx solid #f0f0f0;
}

.title {
    font-size: 34rpx;
    font-weight: 600;
    color: #1a1a1a;
    line-height: 1.4;
    display: block;
    margin-bottom: 12rpx;
}

.sub-title {
    font-size: 24rpx;
    color: #999999;
    line-height: 1.4;
    display: block;
}

.sub-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20rpx;
    box-sizing: border-box;
}

.sub-item {
    background: #ffffff;
    border-radius: 16rpx;
    padding: 32rpx 24rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
    border: 1rpx solid #f0f0f0;
    box-sizing: border-box;
    transition: all 0.2s;
    min-height: 140rpx;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.sub-item:active {
    transform: scale(0.98);
    box-shadow: 0 1rpx 8rpx rgba(0, 0, 0, 0.06);
}

.sub-name {
    font-size: 30rpx;
    font-weight: 500;
    color: #1a1a1a;
    line-height: 1.4;
    margin-bottom: 16rpx;
}

.sub-count {
    font-size: 24rpx;
    color: #999999;
    line-height: 1.4;
}

.empty {
    margin-top: 200rpx;
    text-align: center;
    color: #cccccc;
    font-size: 28rpx;
}
</style>
