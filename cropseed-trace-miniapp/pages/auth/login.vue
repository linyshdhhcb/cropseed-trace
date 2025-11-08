<template>
    <view class="login-page">
        <image class="hero" src="/static/login-banner.png" mode="widthFix"></image>
        <view class="title">欢迎使用种质资源商城</view>
        <view class="tips">授权登录后即可浏览商品、管理订单与地址</view>

        <button class="login-btn" type="primary" :loading="loading" @tap="handleLogin">
            微信一键登录
        </button>

        <view class="protocol">
            登录即表示同意
            <text class="link" @tap="openProtocol">《用户协议》</text>
            与
            <text class="link" @tap="openPrivacy">《隐私政策》</text>
        </view>
    </view>
</template>

<script setup>
import { ref } from 'vue'
import { wxLogin } from '@/api/auth.js'
import { useUserStore } from '@/stores/user.js'

const loading = ref(false)
const userStore = useUserStore()

async function handleLogin() {
    if (loading.value) return
    loading.value = true
    try {
        await userStore.loadFromStorage()
        const loginRes = await new Promise((resolve, reject) => {
            uni.login({
                provider: 'weixin',
                success: resolve,
                fail: reject
            })
        })
        const code = loginRes?.code
        if (!code) {
            throw new Error('未获取到登录凭证')
        }
        const data = await wxLogin(code)
        await userStore.login({
            token: data.token,
            userInfo: data.userInfo
        })
        uni.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => {
            uni.switchTab({ url: '/pages/home/index' })
        }, 500)
    } catch (error) {
        console.error('登录失败', error)
        uni.showToast({ title: error.message || '登录失败', icon: 'none' })
    } finally {
        loading.value = false
    }
}

function openProtocol() {
    uni.navigateTo({ url: '/pages/user/protocol?type=user' })
}

function openPrivacy() {
    uni.navigateTo({ url: '/pages/user/protocol?type=privacy' })
}
</script>

<style scoped>
.login-page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 120rpx 40rpx 80rpx;
    background: linear-gradient(180deg, #f4fff6 0%, #ffffff 60%);
}

.hero {
    width: 520rpx;
    margin-bottom: 80rpx;
}

.title {
    font-size: 40rpx;
    font-weight: 600;
    color: #1a1a1a;
}

.tips {
    margin-top: 24rpx;
    font-size: 26rpx;
    color: #666;
    text-align: center;
}

.login-btn {
    margin-top: 80rpx;
    width: 100%;
    height: 92rpx;
    line-height: 92rpx;
    border-radius: 46rpx;
    background-color: #2b9939;
}

.protocol {
    margin-top: 40rpx;
    font-size: 24rpx;
    color: #888;
}

.link {
    color: #2b9939;
}
</style>
