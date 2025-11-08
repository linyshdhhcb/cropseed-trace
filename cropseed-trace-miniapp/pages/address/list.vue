<template>
    <view class="address-page">
        <scroll-view class="address-scroll" scroll-y>
            <view v-for="item in list" :key="item.id" class="address-item" @tap="handleTap(item)">
                <view class="top">
                    <view class="name">{{ item.consignee }}</view>
                    <view class="phone">{{ item.phone }}</view>
                    <view class="tag" v-if="item.isDefault === 1 || item.isDefault === true">默认</view>
                </view>
                <view class="detail">{{ item.province }}{{ item.city }}{{ item.district }}{{ item.detailAddress }}
                </view>
                <view class="actions" @tap.stop>
                    <view class="action" @tap="setDefault(item)">设为默认</view>
                    <view class="action" @tap="edit(item)">编辑</view>
                    <view class="action danger" @tap="remove(item)">删除</view>
                </view>
            </view>

            <view v-if="!list.length && !loading" class="empty">暂无地址，请添加</view>
        </scroll-view>

        <button class="add-btn" type="primary" @tap="add">新增地址</button>
    </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onLoad } from '@dcloudio/uni-app'
import {
    getAddressList,
    deleteAddress,
    setDefaultAddress
} from '@/api/address.js'

const list = ref([])
const loading = ref(false)
const mode = ref('')

onLoad((options) => {
    mode.value = options?.mode || ''
})

onShow(() => {
    loadList()
})

async function loadList() {
    loading.value = true
    try {
        const data = await getAddressList()
        list.value = data || []
    } catch (error) {
        console.error('获取地址失败', error)
    } finally {
        loading.value = false
    }
}

function add() {
    uni.navigateTo({ url: '/pages/address/edit' })
}

function edit(item) {
    // ID可能是字符串（后端序列化为字符串避免精度丢失）或数字
    const id = item.id
    if (!id) {
        uni.showToast({ title: '地址ID无效', icon: 'none' })
        return
    }
    uni.navigateTo({ url: `/pages/address/edit?id=${id}` })
}

async function remove(item) {
    const confirm = await new Promise((resolve) => {
        uni.showModal({ title: '提示', content: '确认删除该地址？', success: (res) => resolve(res.confirm) })
    })
    if (!confirm) return
    try {
        // ID可能是字符串（后端序列化为字符串避免精度丢失）或数字
        const id = item.id
        if (!id) {
            uni.showToast({ title: '地址ID无效', icon: 'none' })
            return
        }
        await deleteAddress(id)
        uni.showToast({ title: '已删除', icon: 'success' })
        loadList()
    } catch (error) {
        console.error('删除地址失败', error)
        uni.showToast({ title: error.message || '删除失败', icon: 'none' })
    }
}

async function setDefault(item) {
    try {
        // ID可能是字符串（后端序列化为字符串避免精度丢失）或数字
        const id = item.id
        if (!id) {
            uni.showToast({ title: '地址ID无效', icon: 'none' })
            return
        }
        await setDefaultAddress(id)
        uni.showToast({ title: '已设为默认', icon: 'success' })
        loadList()
    } catch (error) {
        console.error('设置默认地址失败', error)
        uni.showToast({ title: error.message || '设置失败', icon: 'none' })
    }
}

function handleTap(item) {
    if (mode.value === 'select') {
        uni.setStorageSync('tempSelectedAddress', item)
        uni.navigateBack()
    }
}
</script>

<style scoped>
.address-page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background: #f7f9fb;
}

.address-scroll {
    flex: 1;
    padding: 24rpx;
}

.address-item {
    background: #ffffff;
    border-radius: 24rpx;
    padding: 32rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.05);
}

.top {
    display: flex;
    align-items: center;
}

.name {
    font-size: 32rpx;
    font-weight: 600;
    color: #1a1a1a;
}

.phone {
    margin-left: 20rpx;
    color: #666;
}

.tag {
    margin-left: 20rpx;
    padding: 4rpx 12rpx;
    border-radius: 20rpx;
    background: #e0f5e5;
    color: #2b9939;
    font-size: 22rpx;
}

.detail {
    margin-top: 16rpx;
    font-size: 26rpx;
    color: #555;
    line-height: 40rpx;
}

.actions {
    margin-top: 24rpx;
    display: flex;
    justify-content: flex-end;
    color: #2b9939;
}

.action {
    margin-left: 24rpx;
    font-size: 26rpx;
}

.action.danger {
    color: #e73a32;
}

.empty {
    text-align: center;
    color: #999;
    padding: 120rpx 0;
}

.add-btn {
    margin: 24rpx;
    border-radius: 44rpx;
    background: linear-gradient(90deg, #2b9939, #53bf68);
}
</style>
