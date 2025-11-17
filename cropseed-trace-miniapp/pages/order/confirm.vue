<template>
    <view class="confirm-page">
        <view class="address-card" @tap="chooseAddress">
            <view v-if="address" class="address-info">
                <view class="row">
                    <text class="name">{{ address.consignee }}</text>
                    <text class="phone">{{ address.phone }}</text>
                </view>
                <view class="detail">{{ address.province }}{{ address.city }}{{ address.district }}{{
                    address.detailAddress }}</view>
            </view>
            <view v-else class="address-empty">
                <text>请选择收货地址</text>
            </view>
            <text class="arrow">›</text>
        </view>

        <view class="goods-card">
            <view class="title">商品信息</view>
            <view v-for="(item, index) in items" :key="index" class="goods-item">
                <image class="cover" :src="(item.imageUrl || item.seedImage) || '/static/no-image-available.png'"
                    mode="aspectFill"></image>
                <view class="info">
                    <view class="name">{{ item.seedName }}</view>
                    <view class="spec">数量：{{ item.quantity }}</view>
                </view>
                <view class="price">￥{{ (item.unitPrice * item.quantity).toFixed(2) }}</view>
            </view>
        </view>

        <view class="remark-card">
            <view class="label">订单备注</view>
            <textarea v-model="remarks" placeholder="选填，可填写与订单相关的说明" maxlength="100" />
        </view>

        <view class="summary-card">
            <view class="row">
                <text>商品金额</text>
                <text>￥{{ goodsAmount.toFixed(2) }}</text>
            </view>
            <view class="row">
                <text>运费</text>
                <text>￥{{ freightAmount.toFixed(2) }}</text>
            </view>
            <view class="row total">
                <text>应付金额</text>
                <text class="value">￥{{ payableAmount.toFixed(2) }}</text>
            </view>
        </view>

        <view class="footer">
            <view class="amount">合计：<text class="value">￥{{ payableAmount.toFixed(2) }}</text></view>
            <button class="submit" type="primary" :loading="submitting" @tap="submitOrder">提交订单</button>
        </view>
    </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useCartStore } from '@/stores/cart.js'
import { useOrderStore } from '@/stores/order.js'
import { useUserStore } from '@/stores/user.js'
import { getDefaultAddress } from '@/api/address.js'

const cartStore = useCartStore()
const orderStore = useOrderStore()
const userStore = useUserStore()

const items = ref([])
const address = ref(null)
const remarks = ref('')
const freightAmount = ref(0)
const submitting = ref(false)
const source = ref('')

const goodsAmount = computed(() => {
    return items.value.reduce((sum, item) => {
        const price = Number(item.unitPrice) || 0
        const quantity = Number(item.quantity) || 0
        return sum + price * quantity
    }, 0)
})
const payableAmount = computed(() => {
    const amount = goodsAmount.value + (Number(freightAmount.value) || 0)
    return Math.max(0, amount)
})

onLoad((options) => {
    source.value = options?.from || ''
    console.log('订单确认页面onLoad，source:', source.value)
})

onShow(() => {
    console.log('订单确认页面onShow')
    initItems()
    restoreSelectedAddress()
    loadAddress()
})

function initItems() {
    console.log('initItems - orderStore.confirmItems:', orderStore.confirmItems)
    console.log('initItems - orderStore.confirmSource:', orderStore.confirmSource)
    
    if (orderStore.confirmItems && orderStore.confirmItems.length > 0) {
        items.value = orderStore.confirmItems.map(item => ({
            ...item,
            // 确保有必要的字段
            seedId: item.seedId || item.id,
            quantity: item.quantity || 1,
            unitPrice: item.unitPrice || 0,
            seedName: item.seedName || item.name || '',
            imageUrl: item.imageUrl || item.seedImage || item.image || ''
        }))
        console.log('initItems - 处理后的items:', items.value)
    } else {
        items.value = []
        console.log('initItems - 没有商品数据')
    }
}

function restoreSelectedAddress() {
    const cache = uni.getStorageSync('tempSelectedAddress')
    if (cache) {
        address.value = cache
        uni.removeStorageSync('tempSelectedAddress')
    }
}

async function loadAddress() {
    if (address.value) {
        return
    }
    try {
        const data = await getDefaultAddress()
        if (data) {
            address.value = data
        }
    } catch (error) {
        console.warn('获取默认地址失败', error)
    }
}

function chooseAddress() {
    uni.navigateTo({ url: '/pages/address/list?mode=select' })
}

async function submitOrder() {
    if (!userStore.isLoggedIn) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
    }
    if (!items.value.length) {
        uni.showToast({ title: '请选择商品', icon: 'none' })
        return
    }
    if (!address.value) {
        uni.showToast({ title: '请选择收货地址', icon: 'none' })
        return
    }
    if (submitting.value) return

    submitting.value = true
    try {
        const payload = {
            addressId: address.value.id || address.value.addressId,
            remarks: remarks.value || ''
        }

        // 根据来源设置不同的参数
        if (source.value === 'buyNow') {
            // 立即购买：使用goods参数
            payload.goods = items.value.map((item) => ({
                seedId: item.seedId || item.id,
                quantity: item.quantity || 1,
                unitPrice: item.unitPrice || 0
            }))
        } else {
            // 购物车：使用cartIds参数
            const cartIds = items.value
                .map((item) => item.cartId || item.id)
                .filter(Boolean)
            if (cartIds.length > 0) {
                payload.cartIds = cartIds
            } else {
                uni.showToast({ title: '请选择商品', icon: 'none' })
                submitting.value = false
                return
            }
        }

        const order = await orderStore.submitOrder(payload)
        orderStore.clearConfirm()
        
        // 如果是从购物车提交订单，删除已提交的购物车商品
        if (source.value === 'cart') {
            const cartIds = items.value
                .map((item) => item.cartId || item.id)
                .filter(Boolean)
            if (cartIds.length > 0) {
                // 先从本地删除，提升用户体验
                await cartStore.removeItems(cartIds)
            }
            // 再重新获取服务器数据，确保数据同步
            await cartStore.fetchCart(true)
        }
        uni.showToast({ title: '下单成功', icon: 'success' })
        setTimeout(() => {
            const orderId = order?.id || order?.orderId
            if (orderId) {
                uni.redirectTo({ url: `/pages/order/detail?id=${orderId}` })
            } else {
                uni.redirectTo({ url: '/pages/order/list' })
            }
        }, 800)
    } catch (error) {
        console.error('提交订单失败', error)
        uni.showToast({ title: error.message || error.msg || '提交失败', icon: 'none' })
    } finally {
        submitting.value = false
    }
}
</script>

<style scoped>
.confirm-page {
    min-height: 100vh;
    padding-bottom: 160rpx;
    background: #f7f9fb;
}

.address-card {
    margin: 24rpx;
    padding: 32rpx;
    background: linear-gradient(90deg, #e7f8eb, #ffffff);
    border-radius: 24rpx;
    display: flex;
    align-items: center;
}

.address-info {
    flex: 1;
    color: #1a1a1a;
}

.row {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.name {
    font-size: 32rpx;
    font-weight: 600;
}

.phone {
    font-size: 28rpx;
}

.detail {
    margin-top: 16rpx;
    font-size: 26rpx;
    color: #555;
}

.address-empty {
    flex: 1;
    color: #999;
}

.arrow {
    font-size: 40rpx;
    color: #aaa;
}

.goods-card,
.remark-card,
.summary-card {
    margin: 24rpx;
    background: #ffffff;
    border-radius: 24rpx;
    padding: 32rpx;
}

.title {
    font-size: 32rpx;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 24rpx;
}

.goods-item {
    display: flex;
    align-items: center;
    margin-bottom: 24rpx;
}

.goods-item:last-child {
    margin-bottom: 0;
}

.cover {
    width: 160rpx;
    height: 160rpx;
    border-radius: 16rpx;
    background: #f5f5f5;
}

.info {
    flex: 1;
    margin-left: 20rpx;
}

.name {
    font-size: 30rpx;
    color: #1a1a1a;
}

.spec {
    margin-top: 12rpx;
    color: #999;
    font-size: 24rpx;
}

.price {
    color: #e73a32;
    font-size: 28rpx;
    font-weight: 600;
}

.remark-card textarea {
    width: 100%;
    min-height: 160rpx;
    margin-top: 16rpx;
    padding: 16rpx;
    background: #f5f7fa;
    border-radius: 16rpx;
}

.summary-card .row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20rpx;
    font-size: 28rpx;
    color: #333;
}

.summary-card .row:last-child {
    margin-bottom: 0;
}

.total {
    font-weight: 600;
}

.value {
    color: #e73a32;
}

.footer {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    padding: 20rpx 24rpx;
    background: #ffffff;
    box-shadow: 0 -6rpx 24rpx rgba(0, 0, 0, 0.08);
}

.amount {
    flex: 1;
    font-size: 28rpx;
    color: #333;
}

.footer .value {
    font-size: 32rpx;
    font-weight: 700;
}

.submit {
    width: 220rpx;
    border-radius: 44rpx;
    background: linear-gradient(90deg, #2b9939, #53bf68);
}
</style>
