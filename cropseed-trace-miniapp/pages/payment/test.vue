<template>
    <view class="test-page">
        <view class="header">
            <text class="title">支付功能测试</text>
            <text class="subtitle">毕业设计演示页面</text>
        </view>

        <view class="test-section">
            <view class="section-title">📱 支付方式测试</view>
            
            <view class="test-item">
                <view class="item-header">
                    <text class="item-title">微信支付测试</text>
                    <text class="item-desc">模拟支付成功（适合演示）</text>
                </view>
                <button class="test-btn wechat" @tap="testWechatPay">测试微信支付</button>
            </view>

            <view class="test-item">
                <view class="item-header">
                    <text class="item-title">支付宝支付测试</text>
                    <text class="item-desc">真实沙箱环境二维码</text>
                </view>
                <button class="test-btn alipay" @tap="testAlipayPay">测试支付宝支付</button>
            </view>
        </view>

        <view class="test-section">
            <view class="section-title">🔒 安全机制测试</view>
            
            <view class="test-item">
                <view class="item-header">
                    <text class="item-title">防抖测试</text>
                    <text class="item-desc">快速点击测试按钮防抖</text>
                </view>
                <button class="test-btn" :loading="debounceLoading" @tap="testDebounce">
                    {{ debounceLoading ? '处理中...' : '测试防抖' }}
                </button>
            </view>

            <view class="test-item">
                <view class="item-header">
                    <text class="item-title">库存检查测试</text>
                    <text class="item-desc">模拟库存不足情况</text>
                </view>
                <button class="test-btn" @tap="testStockCheck">测试库存检查</button>
            </view>
        </view>

        <view class="test-section">
            <view class="section-title">📊 支付状态查询</view>
            
            <view class="status-display">
                <view class="status-item">
                    <text class="status-label">最后测试订单：</text>
                    <text class="status-value">{{ lastOrderId || '无' }}</text>
                </view>
                <view class="status-item">
                    <text class="status-label">支付状态：</text>
                    <text class="status-value" :class="statusClass">{{ paymentStatus || '未知' }}</text>
                </view>
                <button class="refresh-btn" @tap="checkPaymentStatus">刷新状态</button>
            </view>
        </view>

        <view class="test-section">
            <view class="section-title">💡 功能说明</view>
            <view class="feature-list">
                <text class="feature-item">✅ 支付方式选择（微信/支付宝）</text>
                <text class="feature-item">✅ 支付宝沙箱真实二维码</text>
                <text class="feature-item">✅ 前端防抖机制</text>
                <text class="feature-item">✅ 后端分布式锁</text>
                <text class="feature-item">✅ 库存检查与扣减</text>
                <text class="feature-item">✅ 支付状态实时查询</text>
                <text class="feature-item">✅ 完整的异常处理</text>
            </view>
        </view>
    </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const debounceLoading = ref(false)
const lastOrderId = ref('')
const paymentStatus = ref('')

const statusClass = computed(() => {
    switch (paymentStatus.value) {
        case 'PAID': return 'status-success'
        case 'UNPAID': return 'status-warning'
        case 'CANCELLED': return 'status-danger'
        default: return 'status-info'
    }
})

// 测试微信支付
function testWechatPay() {
    uni.showModal({
        title: '微信支付测试',
        content: '这是模拟的微信支付，会直接显示支付成功。在真实环境中，这里会调用微信支付SDK。',
        confirmText: '确认支付',
        success: (res) => {
            if (res.confirm) {
                uni.showLoading({ title: '支付中...' })
                setTimeout(() => {
                    uni.hideLoading()
                    uni.showToast({ title: '微信支付成功', icon: 'success' })
                    paymentStatus.value = 'PAID'
                }, 2000)
            }
        }
    })
}

// 测试支付宝支付
function testAlipayPay() {
    // 模拟订单ID
    const testOrderId = Date.now()
    const testAmount = '0.01'
    
    lastOrderId.value = testOrderId.toString()
    paymentStatus.value = 'UNPAID'
    
    uni.navigateTo({
        url: `/pages/payment/alipay?orderId=${testOrderId}&amount=${testAmount}`
    })
}

// 测试防抖
async function testDebounce() {
    if (debounceLoading.value) return
    
    debounceLoading.value = true
    try {
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 2000))
        uni.showToast({ title: '防抖测试完成', icon: 'success' })
    } finally {
        debounceLoading.value = false
    }
}

// 测试库存检查
function testStockCheck() {
    uni.showModal({
        title: '库存检查测试',
        content: '模拟库存不足的情况。在真实环境中，系统会检查商品库存并阻止超量订单。',
        showCancel: false,
        success: () => {
            uni.showToast({ 
                title: '商品库存不足，无法下单', 
                icon: 'none',
                duration: 3000
            })
        }
    })
}

// 检查支付状态
async function checkPaymentStatus() {
    if (!lastOrderId.value) {
        uni.showToast({ title: '请先进行支付测试', icon: 'none' })
        return
    }
    
    try {
        const response = await uni.request({
            url: `http://localhost:8085/api/payment/status/${lastOrderId.value}`,
            method: 'GET'
        })
        
        if (response.data.success) {
            paymentStatus.value = response.data.data.status
            uni.showToast({ title: '状态已更新', icon: 'success' })
        } else {
            uni.showToast({ title: '查询失败', icon: 'none' })
        }
    } catch (error) {
        console.error('查询支付状态失败:', error)
        uni.showToast({ title: '网络错误', icon: 'none' })
    }
}
</script>

<style scoped>
.test-page {
    min-height: 100vh;
    background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
    padding: 40rpx 32rpx;
}

.header {
    text-align: center;
    margin-bottom: 60rpx;
    color: #fff;
}

.title {
    display: block;
    font-size: 48rpx;
    font-weight: 700;
    margin-bottom: 16rpx;
}

.subtitle {
    font-size: 28rpx;
    opacity: 0.8;
}

.test-section {
    background: #fff;
    border-radius: 24rpx;
    padding: 32rpx;
    margin-bottom: 32rpx;
    box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 24rpx;
    padding-bottom: 16rpx;
    border-bottom: 2rpx solid #f0f0f0;
}

.test-item {
    margin-bottom: 24rpx;
}

.test-item:last-child {
    margin-bottom: 0;
}

.item-header {
    margin-bottom: 16rpx;
}

.item-title {
    display: block;
    font-size: 30rpx;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 8rpx;
}

.item-desc {
    font-size: 26rpx;
    color: #666;
    line-height: 1.4;
}

.test-btn {
    width: 100%;
    height: 80rpx;
    border-radius: 16rpx;
    font-size: 30rpx;
    font-weight: 600;
    border: none;
    color: #fff;
    background: linear-gradient(90deg, #667eea, #764ba2);
}

.test-btn.wechat {
    background: linear-gradient(90deg, #09bb07, #00d100);
}

.test-btn.alipay {
    background: linear-gradient(90deg, #1677ff, #69b1ff);
}

.status-display {
    background: #f8f9fa;
    border-radius: 16rpx;
    padding: 24rpx;
}

.status-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
}

.status-item:last-child {
    margin-bottom: 0;
}

.status-label {
    font-size: 28rpx;
    color: #666;
}

.status-value {
    font-size: 28rpx;
    font-weight: 600;
}

.status-success {
    color: #52c41a;
}

.status-warning {
    color: #faad14;
}

.status-danger {
    color: #ff4d4f;
}

.status-info {
    color: #1890ff;
}

.refresh-btn {
    width: 100%;
    height: 64rpx;
    background: #1890ff;
    color: #fff;
    border-radius: 12rpx;
    font-size: 26rpx;
    margin-top: 16rpx;
    border: none;
}

.feature-list {
    display: flex;
    flex-direction: column;
    gap: 12rpx;
}

.feature-item {
    font-size: 28rpx;
    color: #52c41a;
    line-height: 1.5;
}
</style>
