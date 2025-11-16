<template>
    <view class="cart-page">
        <view v-if="!userStore.isLoggedIn" class="login-hint">
            <text>登录后可同步购物车</text>
            <button class="login-btn" size="mini" type="primary" @tap="goLogin">去登录</button>
        </view>

        <scroll-view class="cart-scroll" scroll-y enable-back-to-top>
            <!-- 加载状态 -->
            <view v-if="loading" class="loading-state">
                <text>加载中...</text>
            </view>
            
            <!-- 空购物车状态 -->
            <view v-else-if="!cartStore.list.length" class="empty">
                <image src="/static/empty-cart.png" mode="widthFix"></image>
                <view class="text">购物车还是空的，快去选购吧～</view>
                <button class="go-buy" @tap="goHome">去逛逛</button>
            </view>

            <!-- 购物车列表 -->
            <view v-else class="cart-list">
                <view v-for="item in cartStore.list" :key="item.cartId || item.id" class="cart-item">
                    <label class="checkbox" @tap="toggleItem(item)">
                        <view class="check" :class="{ checked: item.selected }"></view>
                    </label>
                    <image class="cover" :src="(item.seedImage || item.imageUrl) || '/static/no-image-available.png'"
                        mode="aspectFill"></image>
                    <view class="info">
                        <view class="name">{{ item.seedName }}</view>
                        <view class="price">￥{{ item.unitPrice }}</view>
                        <view class="bottom">
                            <view class="stepper">
                                <view class="btn" @tap="decrease(item)">-</view>
                                <input class="input" type="number" v-model.number="item.quantity"
                                    @blur="changeQuantity(item)" />
                                <view class="btn" @tap="increase(item)">+</view>
                            </view>
                            <view class="remove" @tap="removeItem(item)">删除</view>
                        </view>
                    </view>
                </view>
            </view>
        </scroll-view>

        <view class="cart-footer" v-if="cartStore.list.length">
            <label class="checkbox" @tap="toggleAll">
                <view class="check" :class="{ checked: cartStore.isAllSelected }"></view>
                <text class="label">全选</text>
            </label>
            <view class="summary">
                <view class="amount">合计：<text class="value">￥{{ cartStore.selectedAmount.toFixed(2) }}</text></view>
                <view class="desc">已选 {{ cartStore.selectedQuantity }} 件</view>
            </view>
            <button class="settle-btn" type="primary" @tap="settle">结算</button>
        </view>
    </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useCartStore } from '@/stores/cart.js'
import { useUserStore } from '@/stores/user.js'
import { useOrderStore } from '@/stores/order.js'
import { reportBehavior } from '@/api/product.js'

const cartStore = useCartStore()
const userStore = useUserStore()
const orderStore = useOrderStore()
const loading = ref(false)

onShow(async () => {
    console.log('购物车页面onShow触发')
    userStore.loadFromStorage()
    console.log('用户登录状态:', userStore.isLoggedIn)
    
    if (!userStore.isLoggedIn) {
        // 未登录时也尝试从本地存储加载购物车
        cartStore.loadFromStorage()
        console.log('未登录，从本地加载购物车数据:', cartStore.list.length)
        return
    }
    
    loading.value = true
    try {
        await cartStore.fetchCart(true)
        console.log('已登录，从服务器获取购物车数据:', cartStore.list.length)
    } catch (error) {
        console.error('获取购物车数据失败:', error)
        // 如果网络请求失败，尝试从本地存储加载
        cartStore.loadFromStorage()
    } finally {
        loading.value = false
    }
})

function goHome() {
    uni.switchTab({ url: '/pages/home/index' })
}

function goLogin() {
    uni.navigateTo({ url: '/pages/auth/login' })
}

// 上报页面浏览行为
async function reportPageView() {
    if (!userStore.isLoggedIn) return
    
    try {
        await reportBehavior({
            seedId: 0, // 0表示页面浏览
            behaviorType: 1,
            duration: 3,
            source: '购物车页面'
        })
    } catch (error) {
        console.warn('上报页面浏览失败:', error)
    }
}

// 上报删除行为
async function reportRemoveBehavior(seedId) {
    if (!userStore.isLoggedIn || !seedId) return
    
    try {
        await reportBehavior({
            seedId: seedId,
            behaviorType: 2, // 2-取消关注/删除
            duration: 1,
            source: '购物车-删除商品'
        })
    } catch (error) {
        console.warn('上报删除行为失败:', error)
    }
}

// 上报结算行为
async function reportSettleBehavior(selectedItems) {
    if (!userStore.isLoggedIn || !selectedItems.length) return
    
    try {
        // 为每个选中的商品上报购买意向
        for (const item of selectedItems) {
            await reportBehavior({
                seedId: item.seedId,
                behaviorType: 5, // 5-购买意向
                duration: item.quantity,
                source: '购物车-结算'
            })
        }
    } catch (error) {
        console.warn('上报结算行为失败:', error)
    }
}

// 页面初始化
onMounted(() => {
    console.log('购物车页面onMounted触发')
    // 初始化时先从本地存储加载数据
    cartStore.loadFromStorage()
    userStore.loadFromStorage()
    console.log('初始化加载购物车数据:', cartStore.list.length)
    
    // 延迟上报页面浏览
    setTimeout(() => {
        reportPageView()
    }, 1500)
})

async function toggleItem(item) {
    const cartId = item.cartId || item.id
    if (!cartId) {
        uni.showToast({ title: '购物车ID不存在', icon: 'none' })
        return
    }
    await cartStore.toggleSelected(cartId, !item.selected)
}

async function toggleAll() {
    await cartStore.toggleSelectAll(!cartStore.isAllSelected)
}

async function decrease(item) {
    if (item.quantity <= 1) return
    const cartId = item.cartId || item.id
    if (!cartId) {
        uni.showToast({ title: '购物车ID不存在', icon: 'none' })
        return
    }
    const newQty = item.quantity - 1
    await cartStore.changeQuantity(cartId, newQty)
}

async function increase(item) {
    const cartId = item.cartId || item.id
    if (!cartId) {
        uni.showToast({ title: '购物车ID不存在', icon: 'none' })
        return
    }
    const newQty = item.quantity + 1
    await cartStore.changeQuantity(cartId, newQty)
}

async function changeQuantity(item) {
    const cartId = item.cartId || item.id
    if (!cartId) {
        uni.showToast({ title: '购物车ID不存在', icon: 'none' })
        return
    }
    const qty = Math.max(1, Number(item.quantity) || 1)
    await cartStore.changeQuantity(cartId, qty)
}

async function removeItem(item) {
    try {
        await cartStore.removeItem(item.cartId || item.id)
        
        // 上报删除行为
        reportRemoveBehavior(item.seedId || item.id)
        
        uni.showToast({ title: '已删除', icon: 'success' })
    } catch (error) {
        uni.showToast({ title: '删除失败', icon: 'none' })
    }
}

function settle() {
    if (!cartStore.hasSelected) {
        uni.showToast({ title: '请选择商品', icon: 'none' })
        return
    }
    
    const selectedItems = cartStore.selectedItems
    
    // 上报结算行为
    reportSettleBehavior(selectedItems)
    
    // 使用正确的订单store方法
    orderStore.setConfirmItems(selectedItems, 'cart')
    uni.navigateTo({ url: '/pages/order/confirm' })
}
</script>

<style scoped>
.cart-page {
    min-height: 100vh;
    background: #f7f9fb;
    display: flex;
    flex-direction: column;
}

.login-hint {
    padding: 24rpx 32rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff5e8;
    color: #c97a0a;
}

.login-btn {
    background: #2b9939;
    border-radius: 40rpx;
}

.cart-scroll {
    flex: 1;
}

.loading-state {
    padding: 100rpx 0;
    text-align: center;
    color: #999;
    font-size: 28rpx;
}

.empty {
    margin-top: 160rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #999;
}

.empty image {
    width: 320rpx;
    margin-bottom: 24rpx;
}

.go-buy {
    margin-top: 24rpx;
    width: 200rpx;
    border-radius: 40rpx;
    background: #2b9939;
    color: #fff;
}

.cart-list {
    padding: 24rpx;
}

.cart-item {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;
    padding: 20rpx;
    background: #ffffff;
    border-radius: 20rpx;
    box-shadow: 0 6rpx 18rpx rgba(0, 0, 0, 0.05);
}

.checkbox {
    width: 60rpx;
    display: flex;
    justify-content: center;
}

.check {
    width: 32rpx;
    height: 32rpx;
    border-radius: 50%;
    border: 2rpx solid #ddd;
}

.check.checked {
    background: #2b9939;
    border-color: #2b9939;
}

.cover {
    width: 160rpx;
    height: 160rpx;
    margin: 0 20rpx;
    background: #f5f5f5;
    border-radius: 16rpx;
}

.info {
    flex: 1;
}

.name {
    font-size: 30rpx;
    color: #1a1a1a;
    font-weight: 600;
}

.price {
    margin-top: 12rpx;
    color: #e73a32;
    font-size: 28rpx;
}

.bottom {
    margin-top: 24rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.stepper {
    display: flex;
    align-items: center;
    border: 1rpx solid #e0e0e0;
    border-radius: 40rpx;
    overflow: hidden;
}

.stepper .btn {
    width: 56rpx;
    text-align: center;
    background: #f5f5f5;
}

.stepper .input {
    width: 96rpx;
    text-align: center;
}

.remove {
    color: #999;
    font-size: 24rpx;
}

.cart-footer {
    display: flex;
    align-items: center;
    padding: 16rpx 24rpx;
    background: #ffffff;
    box-shadow: 0 -6rpx 24rpx rgba(0, 0, 0, 0.08);
}

.cart-footer .checkbox {
    display: flex;
    align-items: center;
    width: auto;
    margin-right: 20rpx;
}

.label {
    margin-left: 12rpx;
    font-size: 26rpx;
    color: #333;
}

.summary {
    flex: 1;
}

.amount {
    font-size: 28rpx;
    color: #333;
}

.value {
    color: #e73a32;
    font-weight: 600;
}

.desc {
    margin-top: 4rpx;
    font-size: 22rpx;
    color: #999;
}

.settle-btn {
    width: 200rpx;
    border-radius: 44rpx;
    background: linear-gradient(90deg, #2b9939, #52bf68);
}
</style>
