<template>
    <view class="order-list-page">
        <view class="tabs">
            <view v-for="tab in statusTabs" :key="tab.value" class="tab-item"
                :class="{ active: currentStatus === tab.value }" @tap="switchStatus(tab.value)">
                {{ tab.label }}
            </view>
        </view>

        <scroll-view class="order-scroll" scroll-y enable-back-to-top @scrolltolower="loadMore" refresher-enabled
            :refresher-triggered="refreshing" @refresherrefresh="refresh">
            <!-- 空状态 -->
            <view v-if="!orders.length && !loading" class="empty-state">
                <image class="empty-icon" src="/static/empty-cart.png" mode="aspectFit"></image>
                <text class="empty-text">暂无订单</text>
                <text class="empty-desc">快去选购心仪的商品吧～</text>
                <button class="empty-btn" @tap="goShopping">去逛逛</button>
            </view>

            <!-- 订单列表 -->
            <view v-for="item in orders" :key="item.id || item.orderId" class="order-card">
                <view class="card-header">
                    <view class="order-info">
                        <text class="order-no">订单号：{{ item.orderNo }}</text>
                        <text class="order-time">{{ formatTime(item.createTime) }}</text>
                        <!-- 待支付订单显示倒计时 -->
                        <view class="countdown-row" v-if="item.orderStatus === 0">
                            <text class="countdown-label">剩余支付时间：</text>
                            <text class="countdown-time" :class="{ 'countdown-urgent': getRemaining(item.createTime) < 300 }">
                                {{ formatCountdown(getRemaining(item.createTime)) }}
                            </text>
                        </view>
                    </view>
                    <view class="status-badge" :class="'status-' + item.orderStatus">
                        <text class="status-text">{{ statusText(item.orderStatus) }}</text>
                    </view>
                </view>

                <view class="goods-section" @tap="goDetail(item)">
                    <view v-for="(goods, idx) in (item.items || item.orderItems || [])"
                        :key="goods.id || goods.orderItemId || idx" class="goods-item">
                        <image class="goods-image"
                            :src="goods.seedImage || goods.imageUrl || '/static/no-image-available.png'"
                            mode="aspectFill"></image>
                        <view class="goods-info">
                            <view class="goods-name">{{ goods.seedName }}</view>
                            <view class="goods-spec">
                                <text>数量：{{ goods.quantity }}</text>
                                <text class="goods-price">￥{{ formatAmount(goods.unitPrice) }}</text>
                            </view>
                        </view>
                    </view>
                </view>

                <view class="card-footer">
                    <view class="order-total">
                        <text class="total-label">合计：</text>
                        <text class="total-amount">￥{{ formatAmount(item.payableAmount) }}</text>
                    </view>
                    <view class="action-buttons">
                        <button class="action-btn secondary" size="mini" @tap.stop="goDetail(item)">查看详情</button>
                        <button v-if="item.orderStatus === 0" class="action-btn primary" size="mini"
                            @tap.stop="payOrder(item)">立即支付</button>
                        <button v-else-if="item.orderStatus === 3" class="action-btn primary" size="mini"
                            @tap.stop="confirmReceipt(item)">确认收货</button>
                        <button v-else-if="item.orderStatus === 4 || item.orderStatus === 5"
                            class="action-btn secondary" size="mini" @tap.stop="deleteOrder(item)">删除订单</button>
                    </view>
                </view>
            </view>

            <!-- 加载状态 -->
            <view v-if="loading" class="loading-state">
                <text class="loading-text">加载中...</text>
            </view>
            <view v-else-if="finished && orders.length > 0" class="finished-state">
                <text class="finished-text">没有更多了</text>
            </view>
        </scroll-view>
    </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'
import { useOrderStore } from '@/stores/order.js'
import { payOrder as payOrderApi, confirmReceipt as confirmReceiptApi, deleteOrder as deleteOrderApi } from '@/api/order.js'

const orderStore = useOrderStore()

// 倒计时相关
const ORDER_TIMEOUT_MINUTES = 30 // 订单超时时间（分钟）
const currentTime = ref(Date.now()) // 当前时间戳，用于响应式更新
let countdownTimer = null

const statusTabs = [
    { label: '全部', value: '' },
    { label: '待付款', value: 0 },
    { label: '待审核', value: 1 },
    { label: '待发货', value: 2 },
    { label: '待收货', value: 3 },
    { label: '已完成', value: 4 }
]

const currentStatus = ref('')
const orders = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

onShow(() => {
    loadOrders(true)
    startCountdownTimer()
})

onMounted(() => {
    startCountdownTimer()
})

onUnmounted(() => {
    stopCountdownTimer()
})

// 启动倒计时定时器
function startCountdownTimer() {
    stopCountdownTimer()
    // 每秒更新当前时间，触发倒计时重新计算
    countdownTimer = setInterval(() => {
        currentTime.value = Date.now()
        // 检查是否有订单超时，如果有则刷新列表
        checkTimeoutOrders()
    }, 1000)
}

// 停止倒计时定时器
function stopCountdownTimer() {
    if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
    }
}

// 检查是否有订单超时
function checkTimeoutOrders() {
    const hasTimeout = orders.value.some(item => {
        if (item.orderStatus === 0) {
            const remaining = getRemaining(item.createTime)
            return remaining <= 0
        }
        return false
    })
    if (hasTimeout) {
        // 有订单超时，刷新列表
        loadOrders(true)
    }
}

// 计算剩余时间（秒）
function getRemaining(createTime) {
    if (!createTime) return 0
    const createDate = new Date(createTime)
    const now = currentTime.value
    const elapsed = Math.floor((now - createDate.getTime()) / 1000)
    const timeout = ORDER_TIMEOUT_MINUTES * 60
    return Math.max(0, timeout - elapsed)
}

// 格式化倒计时显示
function formatCountdown(seconds) {
    if (seconds <= 0) return '已超时'
    const minutes = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

onReachBottom(() => {
    loadMore()
})

onPullDownRefresh(() => {
    refresh()
})

async function loadOrders(reset = false) {
    if (loading.value) return
    if (reset) {
        page.value = 1
        finished.value = false
        orders.value = []
    }
    if (finished.value) return

    loading.value = true
    try {
        const params = {
            page: page.value,
            size: size.value
        }
        
        // 只有当状态不为空字符串时才添加 orderStatus 参数
        if (currentStatus.value !== '') {
            params.orderStatus = currentStatus.value
        }
        
        const data = await orderStore.fetchOrders(params)
        console.log('订单列表API响应:', data)
        
        // 兼容不同的数据结构：records 或 list
        const records = data?.records || data?.list || []
        total.value = parseInt(data?.total || data?.totalCount || 0)
        
        console.log('解析的订单数据:', records)
        console.log('订单总数:', total.value)
        orders.value = reset ? records : orders.value.concat(records)
        console.log('最终orders数组:', orders.value)
        finished.value = orders.value.length >= total.value
        page.value += 1
    } catch (error) {
        console.error('获取订单失败', error)
    } finally {
        loading.value = false
        refreshing.value = false
        uni.stopPullDownRefresh()
    }
}

function switchStatus(status) {
    if (currentStatus.value === status) return
    currentStatus.value = status
    loadOrders(true)
}

function refresh() {
    refreshing.value = true
    loadOrders(true)
}

function loadMore() {
    if (!finished.value) {
        loadOrders(false)
    }
}

function statusText(status) {
    const map = {
        0: '待付款',
        1: '待审核',
        2: '待发货',
        3: '已发货',
        4: '已完成',
        5: '已取消',
        6: '退款中'
    }
    return map[status] || '未知'
}

const statusIconMap = {
    0: '/static/order/pending-payment.png',
    2: '/static/order/pending-shipment.png',
    3: '/static/order/pending-receipt.png',
    4: '/static/order/completed.png'
}

function statusIcon(status) {
    return statusIconMap[status] || '/static/order/pending-shipment.png'
}

function formatAmount(amount) {
    if (amount == null || amount === undefined) return '0.00'
    if (typeof amount === 'number') return amount.toFixed(2)
    if (typeof amount === 'string') return parseFloat(amount || '0').toFixed(2)
    return '0.00'
}

function formatTime(time) {
    if (!time) return ''
    if (typeof time === 'string') {
        // 如果是字符串，尝试格式化
        const date = new Date(time)
        if (isNaN(date.getTime())) return time
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        return `${date.getFullYear()}-${month}-${day} ${hours}:${minutes}`
    }
    return time
}

function goShopping() {
    uni.switchTab({ url: '/pages/home/index' })
}

async function confirmReceipt(item) {
    const orderId = item.id || item.orderId
    if (!orderId) {
        uni.showToast({ title: '订单ID不存在', icon: 'none' })
        return
    }
    const confirmed = await new Promise((resolve) => {
        uni.showModal({
            title: '确认收货',
            content: '确认已收到商品？',
            success: (res) => resolve(res.confirm)
        })
    })
    if (!confirmed) return

    try {
        await confirmReceiptApi(orderId)
        uni.showToast({ title: '确认收货成功', icon: 'success' })
        loadOrders(true)
    } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' })
    }
}

async function deleteOrder(item) {
    const orderId = item.id || item.orderId
    if (!orderId) {
        uni.showToast({ title: '订单ID不存在', icon: 'none' })
        return
    }
    const confirmed = await new Promise((resolve) => {
        uni.showModal({
            title: '删除订单',
            content: '确定删除该订单？',
            success: (res) => resolve(res.confirm)
        })
    })
    if (!confirmed) return

    try {
        await deleteOrderApi(orderId)
        uni.showToast({ title: '删除成功', icon: 'success' })
        loadOrders(true)
    } catch (error) {
        uni.showToast({ title: error.message || '删除失败', icon: 'none' })
    }
}

function goDetail(item) {
    const orderId = item.id || item.orderId
    if (!orderId) {
        uni.showToast({ title: '订单ID不存在', icon: 'none' })
        return
    }
    uni.navigateTo({ url: `/pages/order/detail?id=${orderId}` })
}

async function payOrder(item) {
    const orderId = item.id || item.orderId
    if (!orderId) {
        uni.showToast({ title: '订单ID不存在', icon: 'none' })
        return
    }
    try {
        await payOrderApi(orderId, 1)
        uni.showToast({ title: '支付成功', icon: 'success' })
        loadOrders(true)
    } catch (error) {
        uni.showToast({ title: error.message || '支付失败', icon: 'none' })
    }
}
</script>

<style scoped>
.order-list-page {
    min-height: 100vh;
    background: #f5f5f5;
    display: flex;
    flex-direction: column;
}

/* 标签栏 */
.tabs {
    display: flex;
    background: #ffffff;
    padding: 16rpx 0 12rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
    position: sticky;
    top: 0;
    z-index: 10;
}

.tab-item {
    flex: 1;
    text-align: center;
    font-size: 28rpx;
    color: #666;
    padding: 12rpx 0;
    position: relative;
    transition: color 0.3s;
}

.tab-item.active {
    color: #2b9939;
    font-weight: 600;
}

.tab-item.active::after {
    content: '';
    position: absolute;
    left: 50%;
    bottom: 0;
    width: 60rpx;
    height: 4rpx;
    background: linear-gradient(90deg, #2b9939, #53bf68);
    border-radius: 2rpx;
    transform: translateX(-50%);
}

/* 滚动区域 */
.order-scroll {
    flex: 1;
    padding: 24rpx;
}

/* 空状态 */
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 120rpx 40rpx;
    text-align: center;
}

.empty-icon {
    width: 200rpx;
    height: 200rpx;
    opacity: 0.6;
    margin-bottom: 32rpx;
}

.empty-text {
    font-size: 32rpx;
    color: #999;
    margin-bottom: 12rpx;
}

.empty-desc {
    font-size: 26rpx;
    color: #bbb;
    margin-bottom: 40rpx;
}

.empty-btn {
    width: 200rpx;
    height: 72rpx;
    line-height: 72rpx;
    background: linear-gradient(90deg, #2b9939, #53bf68);
    color: #fff;
    border-radius: 36rpx;
    font-size: 28rpx;
    border: none;
}

/* 订单卡片 */
.order-card {
    background: #ffffff;
    border-radius: 20rpx;
    margin-bottom: 24rpx;
    overflow: hidden;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 24rpx;
    border-bottom: 1rpx solid #f0f0f0;
}

.order-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
}

.order-no {
    font-size: 26rpx;
    color: #666;
    font-weight: 500;
}

.order-time {
    font-size: 24rpx;
    color: #999;
}

/* 倒计时样式 */
.countdown-row {
    display: flex;
    align-items: center;
    gap: 8rpx;
    margin-top: 8rpx;
}

.countdown-label {
    font-size: 24rpx;
    color: #f57c00;
}

.countdown-time {
    font-size: 26rpx;
    font-weight: 700;
    color: #f57c00;
    background: #fff3e0;
    padding: 4rpx 12rpx;
    border-radius: 6rpx;
    font-family: 'Courier New', monospace;
}

.countdown-time.countdown-urgent {
    color: #d32f2f;
    background: #ffebee;
    animation: blink 1s infinite;
}

@keyframes blink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.6; }
}

.status-badge {
    padding: 8rpx 20rpx;
    border-radius: 20rpx;
    font-size: 24rpx;
}

.status-0 {
    background: #fff3e0;
    color: #f57c00;
}

.status-1 {
    background: #e3f2fd;
    color: #1976d2;
}

.status-2 {
    background: #fff9c4;
    color: #f9a825;
}

.status-3 {
    background: #e1f5fe;
    color: #0288d1;
}

.status-4 {
    background: #e8f5e9;
    color: #2b9939;
}

.status-5 {
    background: #fafafa;
    color: #999;
}

.status-6 {
    background: #fce4ec;
    color: #c2185b;
}

.status-text {
    font-weight: 500;
}

/* 商品区域 */
.goods-section {
    padding: 24rpx;
    background: #fafafa;
}

.goods-item {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;
    padding: 16rpx;
    background: #fff;
    border-radius: 16rpx;
}

.goods-item:last-child {
    margin-bottom: 0;
}

.goods-image {
    width: 120rpx;
    height: 120rpx;
    border-radius: 12rpx;
    background: #f5f5f5;
    flex-shrink: 0;
}

.goods-info {
    flex: 1;
    margin-left: 20rpx;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-height: 120rpx;
}

.goods-name {
    font-size: 28rpx;
    color: #1a1a1a;
    font-weight: 500;
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    line-clamp: 2;
}

.goods-spec {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12rpx;
    font-size: 24rpx;
    color: #999;
}

.goods-price {
    color: #e73a32;
    font-weight: 600;
    font-size: 26rpx;
}

/* 卡片底部 */
.card-footer {
    padding: 24rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1rpx solid #f0f0f0;
}

.order-total {
    display: flex;
    align-items: baseline;
    gap: 8rpx;
}

.total-label {
    font-size: 26rpx;
    color: #666;
}

.total-amount {
    font-size: 32rpx;
    color: #e73a32;
    font-weight: 700;
}

.action-buttons {
    display: flex;
    gap: 16rpx;
}

.action-btn {
    min-width: 140rpx;
    height: 64rpx;
    line-height: 64rpx;
    border-radius: 32rpx;
    font-size: 26rpx;
    border: none;
    padding: 0 24rpx;
}

.action-btn.secondary {
    background: #f5f5f5;
    color: #666;
}

.action-btn.primary {
    background: linear-gradient(90deg, #2b9939, #53bf68);
    color: #fff;
}

/* 加载状态 */
.loading-state,
.finished-state {
    text-align: center;
    padding: 40rpx 0;
}

.loading-text,
.finished-text {
    font-size: 26rpx;
    color: #999;
}
</style>
