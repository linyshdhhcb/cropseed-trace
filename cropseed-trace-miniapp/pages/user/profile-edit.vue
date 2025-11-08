<template>
    <view class="profile-page">
        <view class="card avatar-card">
            <view class="avatar-wrapper">
                <image class="avatar" :src="displayAvatar" mode="aspectFill"></image>
                <view class="avatar-actions">
                    <view class="avatar-title">头像</view>
                    <view class="avatar-tip">点击更换头像，展示更好的自己</view>
                    <button class="avatar-btn" size="mini" @tap="changeAvatar">更换头像</button>
                </view>
            </view>
        </view>

        <view class="card form-card" v-if="!loading">
            <view class="form-item">
                <text class="label">昵称</text>
                <input class="input" v-model="form.nickname" placeholder="请输入昵称" maxlength="20" />
            </view>
            <view class="form-item">
                <text class="label">手机号</text>
                <input class="input" type="number" v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
            </view>
            <view class="form-item">
                <text class="label">性别</text>
                <picker mode="selector" :range="genderOptions" range-key="label" :value="genderIndex"
                    @change="onGenderChange">
                    <view class="picker-value">{{ genderOptions[genderIndex].label }}</view>
                </picker>
            </view>
            <view class="form-item">
                <text class="label">国家</text>
                <input class="input" v-model="form.country" placeholder="请输入国家" />
            </view>
            <view class="form-item">
                <text class="label">省份</text>
                <input class="input" v-model="form.province" placeholder="请输入省份" />
            </view>
            <view class="form-item">
                <text class="label">城市</text>
                <input class="input" v-model="form.city" placeholder="请输入城市" />
            </view>
        </view>

        <view class="loading-card" v-else>
            <text>加载中...</text>
        </view>

        <view class="footer">
            <button class="save-btn" type="primary" :loading="saving" @tap="submit">保存信息</button>
        </view>
    </view>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user.js'
import { getUserInfo, updateUserInfo } from '@/api/auth.js'
import http from '@/common/http.js'

const userStore = useUserStore()
const defaultAvatar = '/static/default-avatar.png'

const form = reactive({
    nickname: '',
    phone: '',
    country: '',
    province: '',
    city: '',
    gender: 0
})

const genderOptions = [
    { label: '保密', value: 0 },
    { label: '男', value: 1 },
    { label: '女', value: 2 }
]

const genderIndex = ref(0)
const avatarUrl = ref('')
const loading = ref(false)
const saving = ref(false)

const displayAvatar = computed(() => {
    return avatarUrl.value || defaultAvatar
})

onLoad(async () => {
    userStore.loadFromStorage()
    if (!userStore.isLoggedIn) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        setTimeout(() => {
            uni.navigateTo({ url: '/pages/auth/login' })
        }, 600)
        return
    }
    await loadProfile()
})

async function loadProfile() {
    loading.value = true
    try {
        const data = await getUserInfo()
        if (data) {
            form.nickname = data.nickname || ''
            form.phone = data.phone || ''
            form.country = data.country || ''
            form.province = data.province || ''
            form.city = data.city || ''
            form.gender = data.gender ?? 0
            avatarUrl.value = data.avatarUrl || ''
            const idx = genderOptions.findIndex((item) => item.value === form.gender)
            genderIndex.value = idx >= 0 ? idx : 0
        }
    } catch (error) {
        console.error('获取用户资料失败', error)
    } finally {
        loading.value = false
    }
}

function onGenderChange(event) {
    genderIndex.value = Number(event?.detail?.value || 0)
    form.gender = genderOptions[genderIndex.value].value
}

async function changeAvatar() {
    try {
        const res = await new Promise((resolve, reject) => {
            uni.chooseImage({
                count: 1,
                sizeType: ['compressed'],
                sourceType: ['album', 'camera'],
                success: resolve,
                fail: reject
            })
        })
        const path = res.tempFilePaths?.[0] || res.tempFiles?.[0]?.path
        if (!path) {
            uni.showToast({ title: '未选择图片', icon: 'none' })
            return
        }
        uni.showLoading({ title: '上传中', mask: true })
        const url = await http.upload('/file/upload', path)
        if (url) {
            avatarUrl.value = url
            uni.showToast({ title: '上传成功', icon: 'success' })
        }
    } catch (error) {
        console.error('上传头像失败', error)
        uni.showToast({ title: error.message || '上传失败', icon: 'none' })
    } finally {
        uni.hideLoading()
    }
}

function validate() {
    if (!form.nickname.trim()) {
        uni.showToast({ title: '请输入昵称', icon: 'none' })
        return false
    }
    if (form.phone && !/^1\d{10}$/.test(form.phone)) {
        uni.showToast({ title: '手机号格式不正确', icon: 'none' })
        return false
    }
    return true
}

async function submit() {
    if (!validate()) return
    saving.value = true
    try {
        const payload = {
            nickname: form.nickname.trim(),
            phone: form.phone || '',
            gender: form.gender,
            country: form.country || '',
            province: form.province || '',
            city: form.city || '',
            avatarUrl: avatarUrl.value
        }
        const result = await updateUserInfo(payload)
        if (result) {
            avatarUrl.value = result.avatarUrl || ''
            form.nickname = result.nickname || form.nickname
            form.phone = result.phone || form.phone
            form.country = result.country || form.country
            form.province = result.province || form.province
            form.city = result.city || form.city
            form.gender = result.gender ?? form.gender
            const idx = genderOptions.findIndex((item) => item.value === form.gender)
            genderIndex.value = idx >= 0 ? idx : genderIndex.value
            userStore.setUserInfo(result)
        }
        uni.showToast({ title: '保存成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 600)
    } catch (error) {
        console.error('更新资料失败', error)
        uni.showToast({ title: error.message || '保存失败', icon: 'none' })
    } finally {
        saving.value = false
    }
}
</script>

<style scoped>
.profile-page {
    min-height: 100vh;
    padding: 40rpx 32rpx 32rpx;
    background: linear-gradient(180deg, #f4fff6 0%, #f7f9fb 45%, #ffffff 100%);
    box-sizing: border-box;
    padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

.card {
    background: #ffffff;
    border-radius: 28rpx;
    padding: 32rpx 32rpx 28rpx 32rpx;
    margin-bottom: 28rpx;
    box-shadow: 0 12rpx 36rpx rgba(0, 0, 0, 0.08);
    border: 1rpx solid rgba(83, 191, 104, 0.12);
}

.avatar-card {
    display: flex;
    align-items: center;
}

.avatar-wrapper {
    display: flex;
    align-items: center;
}

.avatar {
    width: 140rpx;
    height: 140rpx;
    border-radius: 50%;
    border: 6rpx solid rgba(83, 191, 104, 0.2);
}

.avatar-actions {
    margin-left: 28rpx;
    display: flex;
    flex-direction: column;
    gap: 12rpx;
}

.avatar-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #1a1a1a;
}

.avatar-tip {
    font-size: 24rpx;
    color: #888;
}

.avatar-btn {
    width: 180rpx;
    height: 64rpx;
    line-height: 64rpx;
    background: linear-gradient(90deg, #2b9939, #53bf68);
    color: #ffffff;
    border-radius: 32rpx;
    font-size: 26rpx;
    box-shadow: 0 10rpx 24rpx rgba(83, 191, 104, 0.25);
}

.form-card .form-item {
    display: flex;
    align-items: center;
    padding: 24rpx 12rpx;
    border-bottom: 1rpx solid #f0f3f8;
}

.form-card .form-item:last-child {
    border-bottom: none;
}

.label {
    width: 160rpx;
    font-size: 28rpx;
    color: #444;
    font-weight: 500;
}

.input {
    flex: 1;
    font-size: 28rpx;
    padding: 18rpx 24rpx;
    border-radius: 24rpx;
    background: #f5f7fa;
    border: 1rpx solid rgba(83, 191, 104, 0.08);
}

.picker-value {
    flex: 1;
    font-size: 28rpx;
    color: #333;
    padding: 18rpx 24rpx;
    border-radius: 24rpx;
    background: #f5f7fa;
    border: 1rpx solid rgba(83, 191, 104, 0.08);
}

.loading-card {
    margin-bottom: 28rpx;
    padding: 60rpx 0;
    text-align: center;
    color: #666;
    background: #ffffff;
    border-radius: 24rpx;
    box-shadow: 0 12rpx 36rpx rgba(0, 0, 0, 0.06);
}

.footer {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    padding: 24rpx 32rpx calc(32rpx + env(safe-area-inset-bottom));
    background: rgba(255, 255, 255, 0.95);
    box-shadow: 0 -8rpx 24rpx rgba(0, 0, 0, 0.05);
    box-sizing: border-box;
}

.save-btn {
    width: 100%;
    height: 96rpx;
    line-height: 96rpx;
    background: linear-gradient(90deg, #2b9939, #53bf68);
    border-radius: 48rpx;
    color: #ffffff;
    font-size: 30rpx;
    box-shadow: 0 12rpx 30rpx rgba(83, 191, 104, 0.3);
    border: none;
}
</style>
