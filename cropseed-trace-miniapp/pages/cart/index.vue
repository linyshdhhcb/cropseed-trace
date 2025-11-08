<template>
    <view class="cart-page">
        <view v-if="!userStore.isLoggedIn" class="login-hint">
            <text>登录后可同步购物车</text>
            <button class="login-btn" size="mini" type="primary" @tap="goLogin">去登录</button>
        </view>

        <scroll-view class="cart-scroll" scroll-y enable-back-to-top>
            <view v-if="!cartStore.list.length && !loading" class="empty">
                <image src="/static/empty-cart.png" mode="widthFix"></image>
                <view class="text">购物车还是空的，快去选购吧～</view>
                <button class="go-buy" @tap="goHome">去逛逛</button>
            </view>

            <view v-else class="cart-list">
                <view v-for="item in cartStore.list" :key="item.id" class="cart-item">
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
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useCartStore } from '@/stores/cart.js'
import { useUserStore } from '@/stores/user.js'
import { useOrderStore } from '@/stores/order.js'

const cartStore = useCartStore()
const userStore = useUserStore()
const orderStore = useOrderStore()
const loading = ref(false)

onShow(async () => {
    userStore.loadFromStorage()
    if (!userStore.isLoggedIn) return
    loading.value = true
    await cartStore.fetchCart(true)
    loading.value = false
})

function goLogin() {
    uni.navigateTo({ url: '/pages/auth/login' })
}

function goHome() {
    uni.switchTab({ url: '/pages/home/index' })
}

async function toggleItem(item) {
    await cartStore.toggleSelected(item.id, !item.selected)
}

async function toggleAll() {
    await cartStore.toggleSelectAll(!cartStore.isAllSelected)
}

async function decrease(item) {
    if (item.quantity <= 1) return
    const newQty = item.quantity - 1
    await cartStore.changeQuantity(item.id, newQty)
}

async function increase(item) {
    const newQty = item.quantity + 1
    await cartStore.changeQuantity(item.id, newQty)
}

async function changeQuantity(item) {
    const qty = Math.max(1, Number(item.quantity) || 1)
    await cartStore.changeQuantity(item.id, qty)
}

async function removeItem(item) {
    const confirmed = await new Promise((resolve) => {
        uni.showModal({
            title: '提示',
            content: '确认删除该商品？',
            success: (res) => resolve(res.confirm)
        })
    })
    if (confirmed) {
        await cartStore.removeItem(item.id)
    }
}

function settle() {
    if (!cartStore.selectedItems.length) {
        uni.showToast({ title: '请选择结算商品', icon: 'none' })
        return
    }
    orderStore.setConfirmItems([...cartStore.selectedItems], 'cart')
    uni.navigateTo({ url: '/pages/order/confirm?from=cart' })
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
