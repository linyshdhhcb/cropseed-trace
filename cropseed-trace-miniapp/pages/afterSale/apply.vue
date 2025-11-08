<template>
    <view class="after-apply-page">
        <view v-if="isDetail" class="detail-card">
            <view class="row"><text>售后单号</text><text>{{ detail.afterSalesNo }}</text></view>
            <view class="row"><text>订单号</text><text>{{ detail.orderNo }}</text></view>
            <view class="row"><text>类型</text><text>{{ typeText(detail.type) }}</text></view>
            <view class="row"><text>状态</text><text>{{ statusText(detail.status) }}</text></view>
            <view class="row"><text>申请时间</text><text>{{ detail.createTime }}</text></view>
            <view class="row"><text>原因</text><text>{{ detail.reason }}</text></view>
            <view class="images">
                <image v-for="(img, index) in detail.evidenceImages" :key="index" :src="img" mode="aspectFill"
                    @tap="previewImage(index, detail.evidenceImages)"></image>
            </view>
            <view class="row" v-if="detail.processRemark"><text>处理备注</text><text>{{ detail.processRemark }}</text>
            </view>
            <button v-if="detail.status === 0" class="cancel-btn" @tap="cancel(detail.id)">撤回申请</button>
        </view>

        <view v-else class="form">
            <view class="form-item">
                <text class="label">订单号</text>
                <input v-model="form.orderNo" placeholder="请输入订单号" />
            </view>
            <view class="form-item">
                <text class="label">售后类型</text>
                <picker :range="typeRange" :value="typeIndex" @change="onTypeChange">
                    <view class="picker-value">{{ typeRange[typeIndex] }}</view>
                </picker>
            </view>
            <view class="form-item">
                <text class="label">售后原因</text>
            </view>
            <textarea class="textarea" v-model="form.reason" maxlength="200" placeholder="请描述售后原因" />

            <view class="form-item">
                <text class="label">凭证图片</text>
            </view>
            <view class="image-box">
                <view v-for="(img, index) in form.evidenceImages" :key="index" class="preview">
                    <image :src="img" mode="aspectFill"></image>
                    <view class="close" @tap="removeImage(index)">×</view>
                </view>
                <view v-if="form.evidenceImages.length < 3" class="upload" @tap="chooseImage">+</view>
            </view>

            <button class="submit" type="primary" :loading="loading" @tap="submit">提交申请</button>
        </view>
    </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import http from '@/common/http.js'
import {
    applyAfterSale,
    getAfterSaleDetail,
    cancelAfterSale
} from '@/api/afterSale.js'

const form = reactive({
    orderId: null,
    orderNo: '',
    type: 1,
    reason: '',
    evidenceImages: []
})
const typeRange = ['退货', '换货']
const typeIndex = ref(0)
const loading = ref(false)
const isDetail = ref(false)
const detail = ref(null)

onLoad(async (options) => {
    if (options?.orderId) {
        form.orderId = Number(options.orderId)
    }
    if (options?.orderNo) {
        form.orderNo = options.orderNo
    }
    if (options?.id) {
        isDetail.value = true
        await loadDetail(options.id)
    }
})

async function loadDetail(id) {
    try {
        const data = await getAfterSaleDetail(Number(id))
        detail.value = {
            ...data,
            evidenceImages: data?.evidenceImages ? JSON.parse(data.evidenceImages) : []
        }
    } catch (error) {
        console.error('获取售后详情失败', error)
    }
}

function onTypeChange(e) {
    typeIndex.value = Number(e.detail.value)
    form.type = typeIndex.value + 1
}

async function chooseImage() {
    const res = await uni.chooseImage({ count: 3 - form.evidenceImages.length })
    const filePaths = res.tempFilePaths || res.tempFiles?.map((item) => item.path) || []
    for (const path of filePaths) {
        try {
            const result = await http.upload('/file/upload', path)
            form.evidenceImages.push(result.fileUrl)
        } catch (error) {
            console.error('上传失败', error)
        }
    }
}

function removeImage(index) {
    form.evidenceImages.splice(index, 1)
}

function previewImage(index, list) {
    uni.previewImage({ urls: list, current: index })
}

function validate() {
    if (!form.orderNo) {
        uni.showToast({ title: '请输入订单号', icon: 'none' })
        return false
    }
    if (!form.reason) {
        uni.showToast({ title: '请填写售后原因', icon: 'none' })
        return false
    }
    return true
}

async function submit() {
    if (!validate()) return
    loading.value = true
    try {
        const payload = {
            orderId: form.orderId,
            orderNo: form.orderNo,
            type: form.type,
            reason: form.reason,
            evidenceImages: form.evidenceImages
        }
        await applyAfterSale(payload)
        uni.showToast({ title: '提交成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 800)
    } catch (error) {
        uni.showToast({ title: error.message || '提交失败', icon: 'none' })
    } finally {
        loading.value = false
    }
}

async function cancel(id) {
    try {
        await cancelAfterSale(id)
        uni.showToast({ title: '已撤回', icon: 'success' })
        await loadDetail(id)
    } catch (error) {
        uni.showToast({ title: '撤回失败', icon: 'none' })
    }
}

function statusText(status) {
    const map = {
        0: '待处理',
        1: '审核通过',
        2: '审核拒绝',
        3: '已完成'
    }
    return map[status] || '未知状态'
}

function typeText(type) {
    return type === 2 ? '换货' : '退货'
}
</script>

<style scoped>
.after-apply-page {
    min-height: 100vh;
    background: #f7f9fb;
    padding: 24rpx;
}

.detail-card,
.form {
    background: #ffffff;
    border-radius: 24rpx;
    padding: 32rpx;
}

.row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 16rpx;
    color: #333;
}

.images {
    display: flex;
    margin-top: 16rpx;
}

.images image {
    width: 160rpx;
    height: 160rpx;
    margin-right: 16rpx;
    border-radius: 16rpx;
    background: #f5f5f5;
}

.cancel-btn {
    margin-top: 24rpx;
    border-radius: 44rpx;
}

.form-item {
    margin-bottom: 24rpx;
    display: flex;
    align-items: center;
    color: #333;
}

.label {
    width: 160rpx;
    font-size: 28rpx;
}

.picker-value,
input {
    flex: 1;
    font-size: 28rpx;
}

.textarea {
    width: 100%;
    min-height: 200rpx;
    background: #f5f7fa;
    border-radius: 16rpx;
    padding: 20rpx;
    font-size: 28rpx;
}

.image-box {
    display: flex;
    flex-wrap: wrap;
    gap: 24rpx;
}

.preview {
    position: relative;
}

.preview image,
.upload {
    width: 180rpx;
    height: 180rpx;
    border-radius: 16rpx;
    background: #f1f1f1;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 48rpx;
    color: #999;
}

.close {
    position: absolute;
    top: -12rpx;
    right: -12rpx;
    width: 36rpx;
    height: 36rpx;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
    text-align: center;
    line-height: 36rpx;
    font-size: 28rpx;
}

.submit {
    margin-top: 40rpx;
    border-radius: 44rpx;
    background: linear-gradient(90deg, #2b9939, #53bf68);
}
</style>
