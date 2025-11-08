<template>
    <view class="after-sale-page">
        <scroll-view class="scroll" scroll-y refresher-enabled :refresher-triggered="refreshing"
            @refresherrefresh="refresh" @scrolltolower="loadMore">
            <view v-if="!records.length && !loading" class="empty">暂无售后记录</view>

            <view v-for="item in records" :key="item.id" class="card" @tap="goDetail(item)">
                <view class="row">
                    <text class="no">售后单号：{{ item.afterSalesNo }}</text>
                    <text class="status">{{ statusText(item.status) }}</text>
                </view>
                <view class="row">
                    <text>订单号：{{ item.orderNo }}</text>
                    <text>类型：{{ typeText(item.type) }}</text>
                </view>
                <view class="reason">原因：{{ item.reason }}</view>
                <view class="time">申请时间：{{ item.createTime }}</view>
            </view>

            <view v-if="loading" class="loading">加载中...</view>
            <view v-else-if="finished" class="finished">没有更多了</view>
        </scroll-view>

        <button class="apply-btn" type="primary" @tap="apply">发起售后</button>
    </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAfterSaleList } from '@/api/afterSale.js'

const records = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

onShow(() => {
    loadList(true)
})

async function loadList(reset = false) {
    if (loading.value) return
    if (reset) {
        page.value = 1
        finished.value = false
        records.value = []
    }
    if (finished.value) return

    loading.value = true
    try {
        const data = await getAfterSaleList({ page: page.value, size: size.value })
        const list = data?.records || []
        total.value = data?.total || 0
        records.value = reset ? list : records.value.concat(list)
        finished.value = records.value.length >= total.value
        page.value += 1
    } catch (error) {
        console.error('获取售后记录失败', error)
    } finally {
        loading.value = false
        refreshing.value = false
    }
}

function refresh() {
    refreshing.value = true
    loadList(true)
}

function loadMore() {
    if (!finished.value) {
        loadList(false)
    }
}

function apply() {
    uni.navigateTo({ url: '/pages/afterSale/apply' })
}

function goDetail(item) {
    uni.navigateTo({ url: `/pages/afterSale/apply?id=${item.id}` })
}

function statusText(status) {
    const map = {
        0: '待处理',
        1: '审核通过',
        2: '审核拒绝',
        3: '已完成'
    }
    return map[status] || '未知状态'
}

function typeText(type) {
    return type === 2 ? '换货' : '退货'
}
</script>

<style scoped>
.after-sale-page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background: #f7f9fb;
}

.scroll {
    flex: 1;
    padding: 24rpx;
}

.card {
    background: #ffffff;
    border-radius: 24rpx;
    padding: 28rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 6rpx 18rpx rgba(0, 0, 0, 0.05);
}

.row {
    display: flex;
    justify-content: space-between;
    font-size: 26rpx;
    color: #333;
}

.status {
    color: #2b9939;
}

.reason {
    margin-top: 16rpx;
    color: #666;
}

.time {
    margin-top: 12rpx;
    color: #999;
}

.empty,
.loading,
.finished {
    text-align: center;
    color: #999;
    padding: 80rpx 0;
}

.apply-btn {
    margin: 24rpx;
    border-radius: 44rpx;
    background: linear-gradient(90deg, #2b9939, #53bf68);
}
</style>
