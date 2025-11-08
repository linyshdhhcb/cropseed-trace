<template>
    <view class="detail-page" v-if="product">
        <swiper class="gallery" circular indicator-dots>
            <swiper-item v-for="(img, index) in productImages" :key="index">
                <image :src="img || '/static/no-image-available.png'" mode="aspectFill"></image>
            </swiper-item>
        </swiper>

        <view class="info-card">
            <view class="title">{{ product.seedName }}</view>
            <view class="sub-info">
                <text>品种：{{ product.variety || '-' }}</text>
                <text>产地：{{ product.originPlace || '-' }}</text>
            </view>
            <view class="price-row">
                <text class="price">￥{{ product.unitPrice }}</text>
                <text class="stock">库存：{{ product.stock || '充足' }}</text>
            </view>
        </view>

        <view class="section">
            <view class="section-title">规格参数</view>
            <view class="section-content">
                <rich-text :nodes="product.specifications || '规格信息待完善'" />
            </view>
        </view>

        <view class="section">
            <view class="section-title">特性描述</view>
            <view class="section-content">
                <rich-text :nodes="product.characteristics || '特色介绍待完善'" />
            </view>
        </view>

        <view class="section" v-if="product.qualityReport">
            <view class="section-title">质检报告</view>
            <view class="section-content">
                <view class="report" @tap="previewReport">查看质检报告</view>
            </view>
        </view>

        <view class="fab-bar">
            <view class="action" @tap="goCart">
                <text class="icon">🛒</text>
                <text>购物车</text>
            </view>
            <view class="quantity">
                <text>数量</text>
                <view class="stepper">
                    <view class="btn" @tap="decrease">-</view>
                    <input type="number" v-model.number="quantity" class="input" />
                    <view class="btn" @tap="increase">+</view>
                </view>
            </view>
            <button class="btn-cart" @tap="addCart">加入购物车</button>
            <button class="btn-buy" type="primary" @tap="buyNow">立即购买</button>
        </view>
    </view>
    <view v-else class="loading">加载中...</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getProductDetail } from '@/api/product.js'
import { useCartStore } from '@/stores/cart.js'
import { useOrderStore } from '@/stores/order.js'

const product = ref(null)
const productImages = ref([])
const quantity = ref(1)
let productId = null

const cartStore = useCartStore()
const orderStore = useOrderStore()

onLoad(async (options) => {
    productId = Number(options?.id)
    if (!productId) {
        uni.showToast({ title: '商品不存在', icon: 'none' })
        setTimeout(() => uni.navigateBack(), 1500)
        return
    }
    await loadDetail()
})

async function loadDetail() {
    try {
        const data = await getProductDetail(productId)
        product.value = data
        const images = data?.imageList || [data?.imageUrl].filter(Boolean)
        productImages.value = images.length > 0 ? images : ['/static/no-image-available.png']
    } catch (error) {
        console.error('获取详情失败', error)
        uni.showToast({ title: '获取详情失败', icon: 'none' })
    }
}

function decrease() {
    if (quantity.value > 1) {
        quantity.value -= 1
    }
}

function increase() {
    quantity.value += 1
}

async function addCart() {
    if (!product.value) return
    try {
        await cartStore.addItem({ seedId: productId, quantity: quantity.value })
        uni.showToast({ title: '已加入购物车', icon: 'success' })
    } catch (error) {
        console.error('加入购物车失败', error)
    }
}

function buyNow() {
    if (!product.value) return
    const tempItem = {
        seedId: product.value.id,
        seedName: product.value.seedName,
        unitPrice: product.value.unitPrice,
        quantity: quantity.value,
        imageUrl: product.value.imageUrl,
        selected: true
    }
    orderStore.setConfirmItems([tempItem], 'buyNow')
    uni.navigateTo({ url: '/pages/order/confirm?from=buyNow' })
}

function goCart() {
    uni.switchTab({ url: '/pages/cart/index' })
}

function previewReport() {
    if (product.value?.qualityReport) {
        uni.downloadFile({
            url: product.value.qualityReport,
            success: (res) => {
                const filePath = res.tempFilePath
                uni.openDocument({ filePath })
            }
        })
    }
}
</script>

<style scoped>
.detail-page {
    padding-bottom: 200rpx;
    background: #f7f9fb;
    min-height: 100vh;
}

.gallery {
    height: 520rpx;
    background: #fff;
}

.gallery image {
    width: 100%;
    height: 100%;
}

.info-card {
    margin: -40rpx 32rpx 0;
    background: #ffffff;
    border-radius: 24rpx;
    padding: 32rpx;
    box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.08);
}

.title {
    font-size: 36rpx;
    font-weight: 600;
    color: #1a1a1a;
}

.sub-info {
    margin-top: 12rpx;
    font-size: 24rpx;
    color: #888;
    display: flex;
    justify-content: space-between;
}

.price-row {
    margin-top: 24rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.price {
    font-size: 40rpx;
    color: #e73a32;
    font-weight: 700;
}

.stock {
    font-size: 26rpx;
    color: #2b9939;
}

.section {
    margin: 24rpx 32rpx 0;
    background: #ffffff;
    border-radius: 24rpx;
    padding: 32rpx;
}

.section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #1a1a1a;
}

.section-content {
    margin-top: 20rpx;
    font-size: 26rpx;
    color: #555;
    line-height: 42rpx;
}

.report {
    color: #2b9939;
    text-decoration: underline;
}

.fab-bar {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    padding: 16rpx 24rpx;
    background: #ffffff;
    box-shadow: 0 -6rpx 24rpx rgba(0, 0, 0, 0.08);
}

.action {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 120rpx;
    font-size: 24rpx;
    color: #666;
}

.icon {
    font-size: 40rpx;
}

.quantity {
    flex: 1;
    display: flex;
    align-items: center;
    font-size: 26rpx;
    color: #333;
}

.stepper {
    display: flex;
    align-items: center;
    margin-left: 16rpx;
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
    width: 80rpx;
    text-align: center;
}

.btn-cart,
.btn-buy {
    margin-left: 16rpx;
    border-radius: 40rpx;
    height: 88rpx;
    line-height: 88rpx;
    padding: 0 32rpx;
}

.btn-cart {
    background: #ffe8df;
    color: #e73a32;
}

.btn-buy {
    background: linear-gradient(90deg, #2b9939, #53bf68);
}

.loading {
    padding: 120rpx 0;
    text-align: center;
    color: #888;
}
</style>
