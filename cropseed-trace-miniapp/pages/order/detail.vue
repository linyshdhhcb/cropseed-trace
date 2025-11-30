<template>
    <view class="detail-page" v-if="order">
        <!-- 订单状态卡片 -->
        <view class="status-section" :class="'status-bg-' + order.orderStatus">
            <view class="status-icon-wrapper">
                <text class="status-icon-emoji">{{ statusIcon(order.orderStatus) }}</text>
            </view>
            <view class="status-info">
                <text class="status-text">{{ statusText(order.orderStatus) }}</text>
                <text class="status-desc" v-if="order.orderStatus !== 0">{{ statusDesc(order.orderStatus) }}</text>
                <!-- 待支付订单显示倒计时 -->
                <view class="countdown-wrapper" v-if="order.orderStatus === 0">
                    <text class="countdown-label">剩余支付时间：</text>
                    <text class="countdown-time" :class="{ 'countdown-urgent': remainingSeconds < 300 }">
                        {{ formatCountdown(remainingSeconds) }}
                    </text>
                </view>
            </view>
        </view>

        <!-- 订单信息卡片 -->
        <view class="info-card">
            <view class="info-row">
                <text class="info-label">订单号</text>
                <text class="info-value">{{ order.orderNo }}</text>
            </view>
            <view class="info-row">
                <text class="info-label">下单时间</text>
                <text class="info-value">{{ formatTime(order.createTime) }}</text>
            </view>
            <view class="info-row" v-if="order.paymentTime">
                <text class="info-label">支付时间</text>
                <text class="info-value">{{ formatTime(order.paymentTime) }}</text>
            </view>
        </view>

        <!-- 收货地址卡片 -->
        <view class="address-card">
            <view class="card-header">
                <text class="card-title">收货信息</text>
            </view>
            <view class="address-content">
                <view class="address-row">
                    <text class="consignee">{{ order.consignee }}</text>
                    <text class="phone">{{ order.phone }}</text>
                </view>
                <view class="address-detail">{{ order.address }}</view>
            </view>
        </view>

        <!-- 商品明细卡片 -->
        <view class="goods-card">
            <view class="card-header">
                <text class="card-title">商品明细</text>
                <text class="goods-count" v-if="order.items || order.orderItems">
                    共{{ (order.items || order.orderItems || []).length }}件商品</text>
            </view>
            <view class="goods-list">
                <view v-for="(item, idx) in (order.items || order.orderItems || [])"
                    :key="item.id || item.orderItemId || idx" class="goods-item">
                    <image class="goods-image"
                        :src="item.seedImage || item.imageUrl || '/static/no-image-available.png'" mode="aspectFill"
                        @tap="previewImage(item.seedImage || item.imageUrl)"></image>
                    <view class="goods-info">
                        <view class="goods-name">{{ item.seedName }}</view>
                        <view class="goods-specs">
                            <text class="spec-item">数量：{{ item.quantity }}</text>
                            <text class="spec-item">单价：￥{{ formatAmount(item.unitPrice) }}</text>
                        </view>
                    </view>
                    <view class="goods-price">
                        <text class="price-amount">￥{{ formatAmount(item.totalAmount || (item.unitPrice *
                            item.quantity)) }}</text>
                    </view>
                </view>
            </view>
        </view>

        <!-- 费用明细卡片 -->
        <view class="summary-card">
            <view class="card-header">
                <text class="card-title">费用明细</text>
            </view>
            <view class="summary-list">
                <view class="summary-row">
                    <text class="summary-label">商品金额</text>
                    <text class="summary-value">￥{{ formatAmount(order.totalAmount) }}</text>
                </view>
                <view class="summary-row" v-if="order.discountAmount && order.discountAmount > 0">
                    <text class="summary-label">优惠金额</text>
                    <text class="summary-value discount">-￥{{ formatAmount(order.discountAmount) }}</text>
                </view>
                <view class="summary-row">
                    <text class="summary-label">运费</text>
                    <text class="summary-value">￥{{ formatAmount(order.freightAmount) }}</text>
                </view>
                <view class="summary-divider"></view>
                <view class="summary-row total-row">
                    <text class="summary-label">应付金额</text>
                    <text class="summary-value total-amount">￥{{ formatAmount(order.payableAmount) }}</text>
                </view>
                <view class="summary-row" v-if="order.paidAmount && order.paidAmount > 0">
                    <text class="summary-label">实付金额</text>
                    <text class="summary-value paid-amount">￥{{ formatAmount(order.paidAmount) }}</text>
                </view>
            </view>
        </view>

        <!-- 操作记录卡片 -->
        <view class="operation-card" v-if="operationLogs && operationLogs.length > 0">
            <view class="card-header">
                <text class="card-title">操作记录</text>
            </view>
            <view class="log-list">
                <view v-for="(log, idx) in operationLogs" :key="log.id || idx" class="log-item">
                    <view class="log-dot"></view>
                    <view class="log-content">
                        <view class="log-header">
                            <text class="log-operation">{{ log.operation }}</text>
                            <text class="log-time">{{ formatTime(log.createTime) }}</text>
                        </view>
                        <view class="log-remark" v-if="log.remark">{{ log.remark }}</view>
                    </view>
                </view>
            </view>
        </view>

        <!-- 底部安全距离 -->
        <view class="safe-area-bottom"></view>

        <!-- 底部操作栏 -->
        <view class="footer" v-if="showActions">
            <button v-if="order.orderStatus === 0" class="footer-btn secondary" :loading="cancelLoading" @tap="cancel">取消订单</button>
            <button v-if="order.orderStatus === 0" class="footer-btn primary" type="primary" :loading="payLoading" @tap="showPaymentModal">立即支付</button>
            <button v-if="order.orderStatus === 3" class="footer-btn primary" type="primary" :loading="confirmLoading"
                @tap="confirmReceipt">确认收货</button>
            <button v-if="order.orderStatus === 4 || order.orderStatus === 5" class="footer-btn secondary" :loading="deleteLoading"
                @tap="deleteOrder">删除订单</button>
        </view>

        <!-- 支付方式选择弹窗 -->
        <view class="payment-modal" v-if="showPayment" @tap="hidePaymentModal">
            <view class="payment-content" @tap.stop>
                <view class="payment-header">
                    <text class="payment-title">选择支付方式</text>
                    <text class="payment-close" @tap="hidePaymentModal">×</text>
                </view>
                <view class="payment-amount">
                    <text class="amount-label">支付金额</text>
                    <text class="amount-value">￥{{ order?.payableAmount || 0 }}</text>
                </view>
                <view class="payment-methods">
                    <view class="payment-method" @tap="selectPaymentMethod(1)">
                        <view class="method-icon wechat-icon">💬</view>
                        <view class="method-info">
                            <text class="method-name">微信支付</text>
                            <text class="method-desc">使用微信快捷支付</text>
                        </view>
                        <view class="method-radio" :class="{ active: selectedPaymentMethod === 1 }"></view>
                    </view>
                    <view class="payment-method" @tap="selectPaymentMethod(2)">
                        <view class="method-icon alipay-icon">💰</view>
                        <view class="method-info">
                            <text class="method-name">支付宝支付</text>
                            <text class="method-desc">扫码支付，安全便捷</text>
                        </view>
                        <view class="method-radio" :class="{ active: selectedPaymentMethod === 2 }"></view>
                    </view>
                </view>
                <view class="payment-actions">
                    <button class="payment-cancel" @tap="hidePaymentModal">取消</button>
                    <button class="payment-confirm" type="primary" :loading="payLoading" @tap="confirmPayment">确认支付</button>
                </view>
            </view>
        </view>
    </view>
    <view v-else class="loading-state">
        <text class="loading-text">加载中...</text>
    </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useOrderStore } from '@/stores/order.js'
import { cancelOrder, payOrder, confirmReceipt as confirmReceiptApi, deleteOrder as deleteOrderApi } from '@/api/order.js'

const orderStore = useOrderStore()
const order = ref(null)
const payLoading = ref(false)
const cancelLoading = ref(false)
const confirmLoading = ref(false)
const deleteLoading = ref(false)
const operationLogs = ref([])
const showPayment = ref(false)
const selectedPaymentMethod = ref(1) // 1: 微信支付, 2: 支付宝支付
let orderId = null

// 倒计时相关
const ORDER_TIMEOUT_MINUTES = 30 // 订单超时时间（分钟）
const remainingSeconds = ref(0)
let countdownTimer = null

onLoad(async (options) => {
    orderId = Number(options?.id)
    if (!orderId) {
        uni.showToast({ title: '订单不存在', icon: 'none' })
        setTimeout(() => uni.navigateBack(), 1200)
        return
    }
    await loadDetail()
})

// 页面显示时刷新数据（从支付页面返回时会触发）
onShow(async () => {
    if (orderId) {
        await loadDetail()
    }
})

// 监听支付状态变化事件
onMounted(() => {
    uni.$on('orderStatusChanged', handleOrderStatusChanged)
})

onUnmounted(() => {
    uni.$off('orderStatusChanged', handleOrderStatusChanged)
    // 清理倒计时定时器
    stopCountdown()
})

// 监听订单变化，启动或停止倒计时
watch(() => order.value, (newOrder) => {
    if (newOrder && newOrder.orderStatus === 0) {
        startCountdown(newOrder.createTime)
    } else {
        stopCountdown()
    }
}, { immediate: true })

// 处理订单状态变化
function handleOrderStatusChanged(data) {
    if (data.orderId === orderId && data.status === 'PAID') {
        // 延迟刷新，确保后端数据已更新
        setTimeout(async () => {
            await loadDetail()
        }, 500)
    }
}

async function loadDetail() {
    try {
        console.log('正在刷新订单详情，订单ID:', orderId)
        const data = await orderStore.fetchOrderDetail(orderId)
        if (!data) {
            uni.showToast({ title: '订单不存在', icon: 'none' })
            setTimeout(() => uni.navigateBack(), 1200)
            return
        }
        console.log('订单详情数据:', data)
        order.value = {
            ...data,
            items: data?.items || data?.orderItems || []
        }
        operationLogs.value = data?.logs || data?.operationLogs || []
        console.log('订单状态已更新为:', order.value.orderStatus)
        
        // 如果是待支付订单，启动倒计时
        if (order.value.orderStatus === 0) {
            startCountdown(order.value.createTime)
        }
    } catch (error) {
        console.error('获取订单详情失败', error)
        uni.showToast({ title: '获取订单详情失败', icon: 'none' })
    }
}

// 启动倒计时
function startCountdown(createTime) {
    stopCountdown() // 先停止之前的倒计时
    
    if (!createTime) return
    
    const updateRemaining = () => {
        const createDate = new Date(createTime)
        const now = new Date()
        const elapsed = Math.floor((now - createDate) / 1000) // 已过去的秒数
        const timeout = ORDER_TIMEOUT_MINUTES * 60 // 超时秒数
        const remaining = timeout - elapsed
        
        if (remaining <= 0) {
            remainingSeconds.value = 0
            stopCountdown()
            // 订单已超时，刷新页面获取最新状态
            uni.showToast({ title: '订单已超时', icon: 'none' })
            setTimeout(() => loadDetail(), 1500)
        } else {
            remainingSeconds.value = remaining
        }
    }
    
    updateRemaining() // 立即执行一次
    countdownTimer = setInterval(updateRemaining, 1000) // 每秒更新
}

// 停止倒计时
function stopCountdown() {
    if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
    }
}

// 格式化倒计时显示
function formatCountdown(seconds) {
    if (seconds <= 0) return '00:00'
    const minutes = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

function formatTime(time) {
    if (!time) return ''
    if (typeof time === 'string') {
        const date = new Date(time)
        if (isNaN(date.getTime())) return time
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        return `${year}-${month}-${day} ${hours}:${minutes}`
    }
    return time
}

function formatAmount(amount) {
    if (amount == null || amount === undefined) return '0.00'
    if (typeof amount === 'number') return amount.toFixed(2)
    if (typeof amount === 'string') return parseFloat(amount || '0').toFixed(2)
    return '0.00'
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
    return map[status] || '未知状态'
}

function statusDesc(status) {
    const map = {
        0: '请尽快完成支付', // 待支付状态使用倒计时显示
        1: '订单审核中，请耐心等待',
        2: '商品准备中，请耐心等待',
        3: '商品已发出，请注意查收',
        4: '订单已完成，感谢您的购买',
        5: '订单已取消',
        6: '退款申请处理中'
    }
    return map[status] || ''
}

function statusIcon(status) {
    const map = {
        0: '💰',
        1: '⏳',
        2: '📦',
        3: '🚚',
        4: '✅',
        5: '❌',
        6: '🔄'
    }
    return map[status] || '📋'
}

const showActions = computed(() => {
    if (!order.value) return false
    const status = order.value.orderStatus
    return status === 0 || status === 3 || status === 4 || status === 5
})

function previewImage(url) {
    if (!url) return
    const images = (order.value.items || order.value.orderItems || [])
        .map(item => item.seedImage || item.imageUrl)
        .filter(Boolean)
    if (images.length > 0) {
        uni.previewImage({
            urls: images,
            current: url
        })
    }
}

async function cancel() {
    if (cancelLoading.value) return
    
    const confirmed = await new Promise((resolve) => {
        uni.showModal({ title: '提示', content: '确定取消该订单？', success: (res) => resolve(res.confirm) })
    })
    if (!confirmed) return
    
    cancelLoading.value = true
    try {
        await cancelOrder(orderId, '用户取消')
        uni.showToast({ title: '已取消', icon: 'success' })
        await loadDetail()
    } catch (error) {
        uni.showToast({ title: '取消失败', icon: 'none' })
    } finally {
        cancelLoading.value = false
    }
}

// 显示支付方式选择弹窗
function showPaymentModal() {
    showPayment.value = true
}

// 隐藏支付方式选择弹窗
function hidePaymentModal() {
    showPayment.value = false
}

// 选择支付方式
function selectPaymentMethod(method) {
    selectedPaymentMethod.value = method
}

// 确认支付
async function confirmPayment() {
    if (payLoading.value) return
    
    payLoading.value = true
    try {
        if (selectedPaymentMethod.value === 1) {
            // 微信支付 - 模拟支付成功
            await payOrder(orderId, 1)
            uni.showToast({ title: '微信支付成功', icon: 'success' })
            hidePaymentModal()
            setTimeout(() => {
                loadDetail()
            }, 1000)
        } else if (selectedPaymentMethod.value === 2) {
            // 支付宝支付 - 跳转到支付宝支付页面
            hidePaymentModal()
            uni.navigateTo({ 
                url: `/pages/payment/alipay?orderId=${orderId}&amount=${order.value.payableAmount}` 
            })
        }
    } catch (error) {
        uni.showToast({ title: error.message || '支付失败', icon: 'none' })
    } finally {
        payLoading.value = false
    }
}

// 兼容旧的支付方法（保留向后兼容）
async function pay() {
    showPaymentModal()
}

async function confirmReceipt() {
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
        setTimeout(() => {
            loadDetail()
        }, 1000)
    } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' })
    }
}

async function deleteOrder() {
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
        setTimeout(() => {
            uni.navigateBack()
        }, 1000)
    } catch (error) {
        uni.showToast({ title: error.message || '删除失败', icon: 'none' })
    }
}
</script>

<style scoped>
.detail-page {
    padding-bottom: 180rpx;
    background: #f5f5f5;
    min-height: 100vh;
}

/* 订单状态区域 */
.status-section {
    padding: 60rpx 32rpx 40rpx;
    display: flex;
    align-items: center;
    gap: 24rpx;
}

.status-bg-0 {
    background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
}

.status-bg-1 {
    background: linear-gradient(135deg, #2196f3 0%, #64b5f6 100%);
}

.status-bg-2 {
    background: linear-gradient(135deg, #ffc107 0%, #ffd54f 100%);
}

.status-bg-3 {
    background: linear-gradient(135deg, #03a9f4 0%, #4fc3f7 100%);
}

.status-bg-4 {
    background: linear-gradient(135deg, #2b9939 0%, #53bf68 100%);
}

.status-bg-5 {
    background: linear-gradient(135deg, #9e9e9e 0%, #bdbdbd 100%);
}

.status-bg-6 {
    background: linear-gradient(135deg, #e91e63 0%, #f06292 100%);
}

.status-icon-wrapper {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.status-icon-emoji {
    font-size: 60rpx;
    line-height: 1;
}

.status-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12rpx;
}

/* 倒计时样式 */
.countdown-wrapper {
    display: flex;
    align-items: center;
    gap: 8rpx;
    margin-top: 8rpx;
}

.countdown-label {
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.9);
}

.countdown-time {
    font-size: 32rpx;
    font-weight: 700;
    color: #fff;
    background: rgba(255, 255, 255, 0.2);
    padding: 6rpx 16rpx;
    border-radius: 8rpx;
    font-family: 'Courier New', monospace;
}

.countdown-time.countdown-urgent {
    background: rgba(255, 0, 0, 0.3);
    animation: blink 1s infinite;
}

@keyframes blink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.6; }
}

.loading-state {
    padding: 200rpx 0;
    text-align: center;
    color: #888;
}

.loading-text {
    font-size: 28rpx;
}

/* 支付方式选择弹窗 */
.payment-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: flex-end;
    z-index: 1000;
}

.payment-content {
    width: 100%;
    background: #fff;
    border-radius: 24rpx 24rpx 0 0;
    padding: 0 0 env(safe-area-inset-bottom);
    animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
    from {
        transform: translateY(100%);
    }
    to {
        transform: translateY(0);
    }
}

.payment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32rpx 32rpx 24rpx;
    border-bottom: 1rpx solid #f0f0f0;
}

.payment-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #1a1a1a;
}

.payment-close {
    font-size: 48rpx;
    color: #999;
    line-height: 1;
}

.payment-amount {
    padding: 32rpx;
    text-align: center;
    background: #f8f9fa;
    margin: 0 32rpx;
    border-radius: 16rpx;
    margin-top: 24rpx;
}

.amount-label {
    display: block;
    font-size: 28rpx;
    color: #666;
    margin-bottom: 8rpx;
}

.amount-value {
    font-size: 48rpx;
    font-weight: 700;
    color: #e73a32;
}

.payment-methods {
    padding: 32rpx;
}

.payment-method {
    display: flex;
    align-items: center;
    padding: 24rpx 20rpx;
    border: 2rpx solid #f0f0f0;
    border-radius: 16rpx;
    margin-bottom: 16rpx;
    transition: all 0.3s;
}

.payment-method:last-child {
    margin-bottom: 0;
}

.method-icon {
    width: 80rpx;
    height: 80rpx;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
    margin-right: 20rpx;
}

.wechat-icon {
    background: linear-gradient(135deg, #09bb07, #00d100);
}

.alipay-icon {
    background: linear-gradient(135deg, #1677ff, #69b1ff);
}

.method-info {
    flex: 1;
}

.method-name {
    display: block;
    font-size: 30rpx;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 4rpx;
}

.method-desc {
    font-size: 24rpx;
    color: #666;
}

.method-radio {
    width: 40rpx;
    height: 40rpx;
    border: 2rpx solid #ddd;
    border-radius: 50%;
    position: relative;
    transition: all 0.3s;
}

.method-radio.active {
    border-color: #2b9939;
    background: #2b9939;
}

.method-radio.active::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 16rpx;
    height: 16rpx;
    background: #fff;
    border-radius: 50%;
}

.payment-method:has(.method-radio.active) {
    border-color: #2b9939;
    background: rgba(43, 153, 57, 0.05);
}

.payment-actions {
    display: flex;
    gap: 20rpx;
    padding: 0 32rpx 32rpx;
}

.payment-cancel {
    flex: 1;
    height: 88rpx;
    border: 2rpx solid #ddd;
    border-radius: 44rpx;
    background: #fff;
    color: #666;
    font-size: 30rpx;
}

.payment-confirm {
    flex: 2;
    height: 88rpx;
    border-radius: 44rpx;
    background: linear-gradient(90deg, #2b9939, #52bf68);
    font-size: 30rpx;
}

/* 通用卡片样式 */
.info-card,
.address-card,
.goods-card,
.summary-card,
.operation-card {
    margin: 24rpx;
    background: #ffffff;
    border-radius: 20rpx;
    padding: 0;
    overflow: hidden;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx;
    border-bottom: 1rpx solid #f0f0f0;
}

.card-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #1a1a1a;
    position: relative;
    padding-left: 16rpx;
}

.card-title::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 6rpx;
    height: 28rpx;
    background: linear-gradient(180deg, #2b9939, #53bf68);
    border-radius: 3rpx;
}

/* 订单信息卡片 */
.info-card {
    padding: 24rpx;
}

.info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
}

.info-row:last-child {
    border-bottom: none;
}

.info-label {
    font-size: 28rpx;
    color: #666;
}

.info-value {
    font-size: 28rpx;
    color: #1a1a1a;
    font-weight: 500;
}

/* 地址卡片 */
.address-content {
    padding: 24rpx;
}

.address-row {
    display: flex;
    align-items: center;
    gap: 24rpx;
    margin-bottom: 16rpx;
}

.consignee {
    font-size: 32rpx;
    font-weight: 600;
    color: #1a1a1a;
}

.phone {
    font-size: 28rpx;
    color: #666;
}

.address-detail {
    font-size: 28rpx;
    color: #555;
    line-height: 1.8;
}

/* 商品卡片 */
.goods-count {
    font-size: 24rpx;
    color: #999;
}

.goods-list {
    padding: 0 24rpx 24rpx;
}

.goods-item {
    display: flex;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
}

.goods-item:last-child {
    border-bottom: none;
}

.goods-image {
    width: 160rpx;
    height: 160rpx;
    border-radius: 12rpx;
    background: #f5f5f5;
    flex-shrink: 0;
}

.goods-info {
    flex: 1;
    margin-left: 24rpx;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-height: 160rpx;
}

.goods-name {
    font-size: 30rpx;
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

.goods-specs {
    display: flex;
    flex-direction: column;
    gap: 8rpx;
    margin-top: 16rpx;
}

.spec-item {
    font-size: 24rpx;
    color: #999;
}

.goods-price {
    margin-left: 24rpx;
    text-align: right;
}

.price-amount {
    font-size: 32rpx;
    color: #e73a32;
    font-weight: 700;
}

/* 费用明细卡片 */
.summary-list {
    padding: 0 24rpx 24rpx;
}

.summary-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 0;
    font-size: 28rpx;
}

.summary-label {
    color: #666;
}

.summary-value {
    color: #1a1a1a;
    font-weight: 500;
}

.summary-value.discount {
    color: #2b9939;
}

.summary-divider {
    height: 1rpx;
    background: #f0f0f0;
    margin: 16rpx 0;
}

.total-row {
    padding-top: 24rpx;
    border-top: 1rpx solid #f0f0f0;
}

.total-amount {
    font-size: 36rpx;
    color: #e73a32;
    font-weight: 700;
}

.paid-amount {
    color: #2b9939;
    font-weight: 600;
}

/* 操作记录卡片 */
.log-list {
    padding: 0 24rpx 24rpx;
}

.log-item {
    display: flex;
    padding: 20rpx 0;
    border-left: 2rpx solid #f0f0f0;
    margin-left: 16rpx;
    padding-left: 32rpx;
    position: relative;
}

.log-item:last-child {
    border-left: none;
}

.log-dot {
    position: absolute;
    left: -8rpx;
    top: 28rpx;
    width: 16rpx;
    height: 16rpx;
    border-radius: 50%;
    background: #2b9939;
    border: 3rpx solid #fff;
    box-shadow: 0 0 0 2rpx #2b9939;
}

.log-content {
    flex: 1;
}

.log-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8rpx;
}

.log-operation {
    font-size: 28rpx;
    color: #1a1a1a;
    font-weight: 500;
}

.log-time {
    font-size: 24rpx;
    color: #999;
}

.log-remark {
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
    margin-top: 8rpx;
}

/* 底部安全距离 */
.safe-area-bottom {
    height: 140rpx;
}

/* 底部操作栏 */
.footer {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    padding: 20rpx 24rpx;
    padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
    background: #ffffff;
    box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.08);
    z-index: 100;
    gap: 16rpx;
}

.footer-btn {
    min-width: 160rpx;
    height: 80rpx;
    line-height: 80rpx;
    border-radius: 40rpx;
    font-size: 28rpx;
    font-weight: 600;
    border: none;
    padding: 0 32rpx;
}

.footer-btn.secondary {
    background: #f5f5f5;
    color: #666;
}

.footer-btn.primary {
    background: linear-gradient(90deg, #2b9939, #53bf68);
    color: #fff;
}

/* 加载状态 */
.loading-state {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 200rpx 0;
}

.loading-text {
    font-size: 28rpx;
    color: #999;
}
</style>
