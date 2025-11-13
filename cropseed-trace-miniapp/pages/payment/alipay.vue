<template>
    <view class="alipay-page">
        <!-- 支付头部 -->
        <view class="pay-header">
            <view class="pay-icon">💰</view>
            <text class="pay-title">支付宝支付</text>
            <text class="pay-amount">￥{{ amount }}</text>
        </view>

        <!-- 支付状态 -->
        <view class="pay-status">
            <view v-if="paymentStatus === 'pending'" class="status-pending">
                <view class="loading-icon">⏳</view>
                <text class="status-text">正在生成支付二维码...</text>
            </view>
            <view v-else-if="paymentStatus === 'qrcode'" class="status-qrcode">
                <view class="qrcode-container">
                    <image class="qrcode-image" :src="qrcodeUrl" mode="aspectFit" @error="onQrcodeError"></image>
                </view>
                <text class="qrcode-tip">请使用支付宝扫描上方二维码完成支付</text>
                <view class="qrcode-actions">
                    <button class="refresh-btn" @tap="refreshQrcode">刷新二维码</button>
                    <button class="simulate-btn" @tap="triggerSimulatePayment">模拟支付成功</button>
                </view>
            </view>
            <view v-else-if="paymentStatus === 'success'" class="status-success">
                <view class="success-icon">✅</view>
                <text class="status-text">支付成功</text>
                <text class="success-tip">订单已支付，正在跳转...</text>
            </view>
            <view v-else-if="paymentStatus === 'failed'" class="status-failed">
                <view class="failed-icon">❌</view>
                <text class="status-text">支付失败</text>
                <text class="failed-tip">{{ errorMessage || '支付过程中出现错误' }}</text>
                <button class="retry-btn" @tap="retryPayment">重新支付</button>
            </view>
        </view>

        <!-- 支付说明 -->
        <view class="pay-tips">
            <view class="tip-title">支付说明</view>
            <view class="tip-list">
                <text class="tip-item">• 请在15分钟内完成支付，超时订单将自动取消</text>
                <text class="tip-item">• 支付完成后页面会自动跳转，请勿关闭</text>
                <text class="tip-item">• 如遇问题请联系客服处理</text>
            </view>
        </view>

        <!-- 底部操作 -->
        <view class="pay-footer">
            <button class="cancel-btn" @tap="cancelPayment">取消支付</button>
            <button class="check-btn" type="primary" @tap="checkPaymentStatus">查看支付状态</button>
        </view>
    </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import http from '@/common/http.js'

const orderId = ref('')
const amount = ref('0.00')
const paymentStatus = ref('pending') // pending, qrcode, success, failed
const qrcodeUrl = ref('')
const errorMessage = ref('')
let statusCheckTimer = null

onLoad((options) => {
    orderId.value = options.orderId || ''
    amount.value = options.amount || '0.01'
    
    console.log('支付宝支付页面加载:', options)
    
    if (!orderId.value) {
        uni.showToast({ title: '订单信息错误', icon: 'none' })
        setTimeout(() => uni.navigateBack(), 1500)
        return
    }
    
    // 初始化支付
    initAlipayPayment()
})

onMounted(() => {
    // 开始轮询支付状态
    startStatusCheck()
})

onUnmounted(() => {
    // 清理定时器
    if (statusCheckTimer) {
        clearInterval(statusCheckTimer)
    }
})

// 初始化支付宝支付
async function initAlipayPayment() {
    try {
        paymentStatus.value = 'pending'
        
        console.log('开始初始化支付宝支付:', {
            orderId: orderId.value,
            amount: amount.value
        })
        
        // 调用后端API生成支付宝支付二维码
        const response = await http.post('/api/payment/alipay/qrcode', {
            orderId: orderId.value,
            amount: amount.value
        })
        
        console.log('API响应:', response)
        
        if (response && response.qrcodeUrl) {
            qrcodeUrl.value = response.qrcodeUrl
            paymentStatus.value = 'qrcode'
            console.log('二维码生成成功:', qrcodeUrl.value)
        } else {
            throw new Error('二维码生成失败')
        }
    } catch (error) {
        console.error('初始化支付失败:', error)
        paymentStatus.value = 'failed'
        errorMessage.value = error.message || '网络错误，请重试'
        
        // 显示详细错误信息
        uni.showModal({
            title: '支付初始化失败',
            content: `错误信息：${error.message}\n\n是否使用模拟二维码进行演示？`,
            confirmText: '使用模拟',
            cancelText: '返回',
            success: (res) => {
                if (res.confirm) {
                    // 使用模拟二维码
                    generateMockQRCode()
                } else {
                    uni.navigateBack()
                }
            }
        })
    }
}

// 开始轮询支付状态
function startStatusCheck() {
    if (statusCheckTimer) {
        clearInterval(statusCheckTimer)
    }
    
    let checkCount = 0
    const maxChecks = 100 // 最多检查100次（5分钟）
    
    statusCheckTimer = setInterval(async () => {
        if (paymentStatus.value === 'qrcode' && checkCount < maxChecks) {
            checkCount++
            await checkPaymentStatus()
        } else if (checkCount >= maxChecks) {
            // 超时停止轮询
            clearInterval(statusCheckTimer)
            paymentStatus.value = 'failed'
            errorMessage.value = '支付超时，请重新支付'
        }
    }, 3000) // 每3秒检查一次
}

// 检查支付状态
async function checkPaymentStatus() {
    try {
        const response = await http.get(`/api/payment/status/${orderId.value}`)
        
        if (response && response.status) {
            const status = response.status
            if (status === 'PAID') {
                paymentStatus.value = 'success'
                clearInterval(statusCheckTimer)
                
                uni.showToast({ title: '支付成功', icon: 'success' })
                
                // 2秒后跳转回订单详情并刷新数据
                setTimeout(() => {
                    // 通过事件总线通知订单详情页面刷新
                    uni.$emit('orderStatusChanged', { orderId: orderId.value, status: 'PAID' })
                    // 使用 reLaunch 重新加载页面，确保数据刷新
                    uni.reLaunch({
                        url: `/pages/order/detail?id=${orderId.value}`
                    })
                }, 2000)
            } else if (status === 'FAILED' || status === 'CANCELLED') {
                paymentStatus.value = 'failed'
                errorMessage.value = '支付已取消或失败'
                clearInterval(statusCheckTimer)
            }
        }
    } catch (error) {
        console.error('检查支付状态失败:', error)
    }
}

// 刷新二维码
function refreshQrcode() {
    uni.showModal({
        title: '刷新二维码',
        content: '选择二维码类型',
        confirmText: '真实API',
        cancelText: '模拟演示',
        success: (res) => {
            if (res.confirm) {
                initAlipayPayment()
            } else {
                generateMockQRCode()
            }
        }
    })
}

// 重新支付
function retryPayment() {
    refreshQrcode()
}

// 取消支付
function cancelPayment() {
    uni.showModal({
        title: '确认取消',
        content: '确定要取消支付吗？',
        success: (res) => {
            if (res.confirm) {
                if (statusCheckTimer) {
                    clearInterval(statusCheckTimer)
                }
                uni.navigateBack()
            }
        }
    })
}

// 生成模拟二维码
function generateMockQRCode() {
    // 生成一个模拟的二维码（实际上是一个包含支付信息的图片）
    const mockQRData = `alipays://platformapi/startapp?saId=10000007&qrcode=${orderId.value}&amount=${amount.value}&mock=true`
    
    // 这里使用一个在线二维码生成服务作为演示
    qrcodeUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(mockQRData)}`
    
    paymentStatus.value = 'qrcode'
    errorMessage.value = ''
    
    uni.showToast({ 
        title: '模拟二维码已生成', 
        icon: 'success',
        duration: 2000
    })
    
    // 10秒后模拟支付成功
    setTimeout(async () => {
        if (paymentStatus.value === 'qrcode') {
            try {
                // 调用模拟支付成功API
                await simulatePaymentSuccess()
                paymentStatus.value = 'success'
                clearInterval(statusCheckTimer)
                uni.showToast({ title: '模拟支付成功', icon: 'success' })
                setTimeout(() => {
                    uni.navigateBack()
                }, 2000)
            } catch (error) {
                console.error('模拟支付失败:', error)
                paymentStatus.value = 'failed'
                errorMessage.value = '模拟支付失败，请重试'
            }
        }
    }, 10000)
}

// 模拟支付成功
async function simulatePaymentSuccess() {
    try {
        console.log('调用模拟支付API，订单ID:', orderId.value)
        const response = await http.post('/api/payment/simulate-success', {
            orderId: orderId.value,
            paymentMethod: 2 // 支付宝支付
        })
        
        console.log('模拟支付API响应:', response)
        
        // 检查响应结构，后端返回的是 Result.success(result)
        if (response && (response.success || (response.data && response.data.success))) {
            console.log('模拟支付成功')
            return true
        } else {
            console.error('模拟支付API返回失败:', response)
            throw new Error('模拟支付API调用失败')
        }
    } catch (error) {
        console.error('模拟支付API错误:', error)
        throw error
    }
}

// 手动触发模拟支付成功
async function triggerSimulatePayment() {
    if (paymentStatus.value !== 'qrcode') {
        return
    }
    
    uni.showModal({
        title: '模拟支付',
        content: '确定要模拟支付成功吗？',
        success: async (res) => {
            if (res.confirm) {
                try {
                    uni.showLoading({ title: '处理中...' })
                    await simulatePaymentSuccess()
                    paymentStatus.value = 'success'
                    clearInterval(statusCheckTimer)
                    uni.hideLoading()
                    uni.showToast({ title: '支付成功', icon: 'success' })
                    setTimeout(() => {
                        // 通过事件总线通知订单详情页面刷新
                        uni.$emit('orderStatusChanged', { orderId: orderId.value, status: 'PAID' })
                        // 使用 reLaunch 重新加载页面，确保数据刷新
                        uni.reLaunch({
                            url: `/pages/order/detail?id=${orderId.value}`
                        })
                    }, 2000)
                } catch (error) {
                    uni.hideLoading()
                    console.error('模拟支付失败:', error)
                    uni.showToast({ 
                        title: '支付失败：' + error.message, 
                        icon: 'none',
                        duration: 3000
                    })
                }
            }
        }
    })
}

// 二维码加载错误
function onQrcodeError() {
    paymentStatus.value = 'failed'
    errorMessage.value = '二维码加载失败，请重新生成'
}
</script>

<style scoped>
.alipay-page {
    min-height: 100vh;
    background: linear-gradient(180deg, #1677ff 0%, #69b1ff 100%);
    padding: 0 0 env(safe-area-inset-bottom);
}

.pay-header {
    padding: 80rpx 40rpx 60rpx;
    text-align: center;
    color: #fff;
}

.pay-icon {
    font-size: 120rpx;
    margin-bottom: 20rpx;
}

.pay-title {
    display: block;
    font-size: 36rpx;
    font-weight: 600;
    margin-bottom: 16rpx;
}

.pay-amount {
    font-size: 56rpx;
    font-weight: 700;
}

.pay-status {
    margin: 40rpx 40rpx;
    background: #fff;
    border-radius: 24rpx;
    padding: 60rpx 40rpx;
    text-align: center;
    box-shadow: 0 8rpx 32rpx rgba(22, 119, 255, 0.15);
}

.status-pending,
.status-success,
.status-failed {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.loading-icon,
.success-icon,
.failed-icon {
    font-size: 80rpx;
    margin-bottom: 24rpx;
}

.status-text {
    font-size: 32rpx;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 16rpx;
}

.success-tip,
.failed-tip {
    font-size: 28rpx;
    color: #666;
    margin-bottom: 32rpx;
}

.qrcode-container {
    width: 400rpx;
    height: 400rpx;
    border: 2rpx solid #f0f0f0;
    border-radius: 16rpx;
    margin: 0 auto 32rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fff;
}

.qrcode-image {
    width: 360rpx;
    height: 360rpx;
}

.qrcode-tip {
    font-size: 28rpx;
    color: #666;
    margin-bottom: 32rpx;
    line-height: 1.5;
}

.qrcode-actions {
    display: flex;
    justify-content: center;
    gap: 20rpx;
}

.refresh-btn,
.simulate-btn,
.retry-btn {
    padding: 16rpx 32rpx;
    background: #f8f9fa;
    border: 1rpx solid #e9ecef;
    border-radius: 8rpx;
    color: #666;
    font-size: 28rpx;
}

.simulate-btn {
    background: #52c41a;
    border-color: #52c41a;
    color: #fff;
}

.pay-tips {
    margin: 40rpx;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 16rpx;
    padding: 32rpx;
}

.tip-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 20rpx;
}

.tip-list {
    display: flex;
    flex-direction: column;
    gap: 12rpx;
}

.tip-item {
    font-size: 26rpx;
    color: #666;
    line-height: 1.5;
}

.pay-footer {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 24rpx 40rpx calc(24rpx + env(safe-area-inset-bottom));
    background: #fff;
    border-top: 1rpx solid #f0f0f0;
    display: flex;
    gap: 20rpx;
}

.cancel-btn {
    flex: 1;
    height: 88rpx;
    border: 2rpx solid #ddd;
    border-radius: 44rpx;
    background: #fff;
    color: #666;
    font-size: 30rpx;
}

.check-btn {
    flex: 2;
    height: 88rpx;
    border-radius: 44rpx;
    background: linear-gradient(90deg, #1677ff, #69b1ff);
    font-size: 30rpx;
}
</style>
