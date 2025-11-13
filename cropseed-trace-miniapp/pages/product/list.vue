<template>
    <view class="product-list-page">
        <view class="search-header">
            <view class="search-box">
                <image class="search-icon" src="/static/search.png" mode="aspectFit"></image>
                <input v-model="keyword" class="search-input" type="text" placeholder="搜索种子名称、关键字"
                    @confirm="onSearch" />
                <view class="btn" @tap="onSearch">搜索</view>
            </view>
        </view>

        <scroll-view class="list-scroll" scroll-y enable-back-to-top @scrolltolower="loadMore" refresher-enabled
            :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
            <view class="filters">
                <view v-for="item in sortOptions" :key="item.value" class="filter-item"
                    :class="{ active: sortField === item.value }" @tap="changeSort(item)">
                    <image class="filter-icon" :src="item.icon" mode="aspectFit"></image>
                    <text>{{ item.label }}</text>
                </view>
            </view>

            <view class="product-list">
                <view v-for="item in list" :key="item.id" class="product-item" @tap="goDetail(item)">
                    <image class="cover" :src="item.imageUrl || '/static/no-image-available.png'" mode="aspectFill">
                    </image>
                    <view class="info">
                        <view class="name">{{ item.seedName }}</view>
                        <view class="desc">{{ item.characteristics || '优质品种' }}</view>
                        <view class="bottom">
                            <view class="price">￥{{ item.unitPrice }}</view>
                            <view class="stock-info">
                                <text class="stock-text" :class="{ 'low-stock': item.totalStock < 10, 'out-stock': item.totalStock <= 0 }">
                                    库存{{ item.totalStock > 0 ? item.totalStock + '件' : '缺货' }}
                                </text>
                            </view>
                        </view>
                    </view>
                </view>
            </view>

            <view v-if="loading" class="loading">加载中...</view>
            <view v-else-if="finished && !list.length" class="empty">暂无相关商品</view>
            <view v-else-if="finished" class="finished">没有更多了</view>
        </scroll-view>
    </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { getProductList } from '@/api/product.js'

const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const keyword = ref('')
const categoryId = ref()
const sortField = ref('')
const sortOrder = ref('')

const sortOptions = [
    { label: '综合', value: '', icon: '/static/product/comprehensive.png' },
    { label: '价格升序', value: 'priceAsc', icon: '/static/product/asc.png' },
    { label: '价格降序', value: 'priceDesc', icon: '/static/product/desc.png' },
    { label: '热度', value: 'popularity', icon: '/static/product/heat.png' }
]

onLoad((options) => {
    if (options?.categoryId) {
        categoryId.value = Number(options.categoryId)
    }
    if (options?.keyword) {
        keyword.value = options.keyword
    }
    loadList(true)
})

onPullDownRefresh(() => {
    onRefresh()
})

onReachBottom(() => {
    loadMore()
})

function getSortParams() {
    if (sortField.value === 'priceAsc') {
        return { sortField: 'unit_price', sortOrder: 'asc' }
    }
    if (sortField.value === 'priceDesc') {
        return { sortField: 'unit_price', sortOrder: 'desc' }
    }
    if (sortField.value === 'popularity') {
        return { sortField: 'create_time', sortOrder: 'desc' }  // 暂时用创建时间代替热度
    }
    return { sortField: '', sortOrder: '' }
}

async function loadList(reset = false) {
    if (loading.value) return
    if (reset) {
        page.value = 1
        finished.value = false
        list.value = []
    }
    if (finished.value) return

    loading.value = true
    try {
        const sortParams = getSortParams()
        const params = {
            page: page.value,
            size: size.value
        }
        if (keyword.value) {
            params.seedName = keyword.value
        }
        if (typeof categoryId.value === 'number' && !Number.isNaN(categoryId.value)) {
            params.categoryId = categoryId.value
        }
        if (sortParams.sortField) {
            params.sortField = sortParams.sortField
        }
        if (sortParams.sortOrder) {
            params.sortOrder = sortParams.sortOrder
        }

        console.log('商品列表请求参数:', params)
        console.log('当前排序字段:', sortField.value)
        console.log('排序参数:', sortParams)

        const data = await getProductList(params)
        console.log('商品列表API响应:', data)
        
        const records = data?.list || []
        total.value = data?.total || 0
        list.value = reset ? records : list.value.concat(records)
        console.log('商品列表数据:', list.value)
        finished.value = list.value.length >= total.value
        page.value += 1
    } catch (error) {
        console.error('获取商品列表失败', error)
        uni.showToast({ title: '获取数据失败', icon: 'none' })
    } finally {
        loading.value = false
        refreshing.value = false
        uni.stopPullDownRefresh()
    }
}

function onSearch() {
    loadList(true)
}

function onRefresh() {
    refreshing.value = true
    loadList(true)
}

function loadMore() {
    if (!finished.value) {
        loadList(false)
    }
}

function changeSort(option) {
    console.log('切换排序:', option)
    sortField.value = option.value
    
    // 给用户一个视觉反馈
    uni.showToast({
        title: `切换到${option.label}`,
        icon: 'none',
        duration: 1000
    })
    
    loadList(true)
}

function goDetail(item) {
    uni.navigateTo({ url: `/pages/product/detail?id=${item.id}` })
}
</script>

<style scoped>
.product-list-page {
    min-height: 100vh;
    background-color: #f7f9fb;
}

.search-header {
    padding: 24rpx 32rpx 16rpx;
    background-color: #ffffff;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.search-box {
    display: flex;
    align-items: center;
    background: #f2f4f7;
    border-radius: 40rpx;
    padding: 0 24rpx;
}

.search-icon {
    width: 32rpx;
    height: 32rpx;
    margin-right: 12rpx;
}

.search-input {
    flex: 1;
    padding: 0 16rpx;
    height: 80rpx;
    line-height: 80rpx;
}

.btn {
    color: #2b9939;
    font-size: 28rpx;
}

.list-scroll {
    height: calc(100vh - 120rpx);
}

.filters {
    display: flex;
    padding: 20rpx 32rpx;
    background: #ffffff;
    justify-content: space-between;
}

.filter-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    font-size: 26rpx;
    color: #666;
}

.filter-item.active {
    color: #2b9939;
    font-weight: 600;
}

.filter-icon {
    width: 48rpx;
    height: 48rpx;
}

.product-list {
    padding: 0 32rpx 32rpx;
}

.product-item {
    display: flex;
    margin-bottom: 24rpx;
    background: #ffffff;
    border-radius: 20rpx;
    overflow: hidden;
    box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.06);
}

.cover {
    width: 220rpx;
    height: 220rpx;
    background: #f5f5f5;
}

.info {
    flex: 1;
    padding: 24rpx;
}

.name {
    font-size: 30rpx;
    font-weight: 600;
    color: #1a1a1a;
}

.desc {
    margin-top: 12rpx;
    font-size: 24rpx;
    color: #999;
    line-height: 36rpx;
}

.bottom {
    margin-top: 32rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.price {
    color: #e73a32;
    font-size: 32rpx;
    font-weight: 600;
}

.tag {
    font-size: 22rpx;
    color: #2b9939;
}

.stock-info {
    display: flex;
    align-items: center;
}

.stock-text {
    font-size: 22rpx;
    color: #2b9939;
}

.stock-text.low-stock {
    color: #ff9500;
}

.stock-text.out-stock {
    color: #ff3b30;
}

.loading,
.finished,
.empty {
    text-align: center;
    padding: 32rpx 0;
    color: #999;
}
</style>
