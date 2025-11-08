<template>
    <view class="user-page">
        <view class="header">
            <image class="bg" src="/static/user-bg.png" mode="widthFix"></image>
            <view class="profile">
                <image class="avatar" :src="userAvatar" mode="aspectFill"></image>
                <view class="info">
                    <view class="name">{{ userName }}</view>
                    <view class="desc" :class="{ actionable: userStore.isLoggedIn }" @tap="handleDescTap">
                        {{ userDesc }}
                    </view>
                </view>
                <button v-if="!userStore.isLoggedIn" class="login-btn" size="mini" @tap="goLogin">登录</button>
            </view>
        </view>

        <view class="order-entry">
            <view class="title">
                我的订单
                <text class="more" @tap="goOrder('')">全部订单 ></text>
            </view>
            <view class="order-status">
                <view class="status-item" v-for="item in orderStatusItems" :key="item.status"
                    @tap="goOrder(item.status)">
                    <image class="status-icon" :src="item.icon" mode="aspectFit"></image>
                    <text>{{ item.label }}</text>
                </view>
            </view>
        </view>

        <view class="menu-list">
            <view class="menu-item" @tap="goAddress">
                <text>收货地址</text>
                <text class="arrow">›</text>
            </view>
            <view class="menu-item" @tap="goAfterSale">
                <text>售后服务</text>
                <text class="arrow">›</text>
            </view>
            <view class="menu-item" @tap="goProtocol('user')">
                <text>用户协议</text>
                <text class="arrow">›</text>
            </view>
            <view class="menu-item" @tap="goProtocol('privacy')">
                <text>隐私政策</text>
                <text class="arrow">›</text>
            </view>
            <view class="menu-item" @tap="logout" v-if="userStore.isLoggedIn">
                <text>退出登录</text>
            </view>
        </view>
    </view>
</template>

<script setup>
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user.js'

const userStore = useUserStore()

const orderStatusItems = [
    { status: '0', label: '待付款', icon: '/static/order/pending-payment.png' },
    { status: '2', label: '待发货', icon: '/static/order/pending-shipment.png' },
    { status: '3', label: '待收货', icon: '/static/order/pending-receipt.png' },
    { status: '4', label: '已完成', icon: '/static/order/completed.png' }
]

onShow(() => {
    userStore.loadFromStorage()
})

const userName = computed(() => {
    if (!userStore.isLoggedIn) return '未登录用户'
    return userStore.userInfo?.nickname || userStore.userInfo?.username || '微信用户'
})

const userDesc = computed(() => {
    if (!userStore.isLoggedIn) return '登录后可查看订单和地址'
    return userStore.userInfo?.phone || '完善更多信息享个性化服务'
})

const userAvatar = computed(() => {
    if (!userStore.isLoggedIn) return '/static/default-avatar.png'
    return userStore.userInfo?.avatarUrl || '/static/default-avatar.png'
})

function goLogin() {
    uni.navigateTo({ url: '/pages/auth/login' })
}

function handleDescTap() {
    if (!userStore.isLoggedIn) {
        goLogin()
        return
    }
    uni.navigateTo({ url: '/pages/user/profile-edit' })
}

function goOrder(status) {
    uni.navigateTo({ url: `/pages/order/list?status=${status}` })
}

function goAddress() {
    uni.navigateTo({ url: '/pages/address/list' })
}

function goAfterSale() {
    uni.navigateTo({ url: '/pages/afterSale/list' })
}

function goProtocol(type) {
    uni.navigateTo({ url: `/pages/user/protocol?type=${type}` })
}

async function logout() {
    const confirm = await new Promise((resolve) => {
        uni.showModal({ title: '提示', content: '确定退出登录？', success: (res) => resolve(res.confirm) })
    })
    if (confirm) {
        await userStore.logout()
        uni.showToast({ title: '已退出', icon: 'success' })
    }
}
</script>

<style scoped>
.user-page {
    min-height: 100vh;
    background: #f7f9fb;
}

.header {
    position: relative;
    height: 320rpx;
}

.header .bg {
    width: 100%;
    height: 100%;
}

.profile {
    position: absolute;
    left: 32rpx;
    right: 32rpx;
    bottom: -60rpx;
    background: linear-gradient(90deg, #2b9939, #53bf68);
    border-radius: 24rpx;
    padding: 32rpx;
    display: flex;
    align-items: center;
    color: #fff;
    box-shadow: 0 12rpx 32rpx rgba(43, 153, 57, 0.3);
}

.avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    border: 4rpx solid rgba(255, 255, 255, 0.6);
    margin-right: 24rpx;
}

.info {
    flex: 1;
}

.name {
    font-size: 34rpx;
    font-weight: 600;
}

.desc {
    margin-top: 8rpx;
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.85);
    transition: opacity 0.2s;
}

.desc.actionable {
    color: #ffffff;
    text-decoration: underline;
}

.login-btn {
    background: #ffffff;
    color: #2b9939;
    border-radius: 32rpx;
}

.order-entry {
    margin: 120rpx 24rpx 24rpx;
    background: #ffffff;
    border-radius: 24rpx;
    padding: 32rpx;
}

.order-entry .title {
    display: flex;
    justify-content: space-between;
    font-size: 30rpx;
    color: #1a1a1a;
}

.more {
    color: #999;
    font-size: 24rpx;
}

.order-status {
    margin-top: 32rpx;
    display: flex;
    justify-content: space-around;
}

.status-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #555;
}

.status-icon {
    width: 64rpx;
    height: 64rpx;
    margin-bottom: 12rpx;
}

.menu-list {
    margin: 0 24rpx;
    background: #ffffff;
    border-radius: 24rpx;
    padding: 24rpx 0;
}

.menu-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 28rpx 32rpx;
    border-bottom: 1rpx solid #f0f0f0;
    color: #333;
}

.menu-item:last-child {
    border-bottom: none;
}

.arrow {
    color: #999;
}
</style>
