<template>
    <view class="detail-page" v-if="product">
        <!-- 商品图片轮播 -->
        <swiper class="gallery" circular indicator-dots indicator-color="rgba(255,255,255,0.5)"
            indicator-active-color="#2b9939">
            <swiper-item v-for="(img, index) in productImages" :key="index">
                <image :src="img || '/static/no-image-available.png'" mode="aspectFill"></image>
            </swiper-item>
        </swiper>

        <!-- 商品基本信息 -->
        <view class="info-card">
            <view class="title">{{ product.seedName }}</view>
            <view class="sub-info">
                <text class="info-item">品种：{{ product.variety || '-' }}</text>
                <text class="info-item">产地：{{ product.originPlace || '-' }}</text>
            </view>
            <view class="price-row">
                <view class="price-wrapper">
                    <text class="price-symbol">￥</text>
                    <text class="price">{{ product.unitPrice }}</text>
                </view>
                <view class="stock-wrapper">
                    <text class="stock-label">库存：</text>
                    <text class="stock">{{ product.stock || '充足' }}</text>
                </view>
            </view>
        </view>

        <!-- 规格参数 -->
        <view class="section">
            <view class="section-header">
                <view class="section-title">规格参数</view>
            </view>
            <view class="section-content">
                <rich-text :nodes="product.specifications || '规格信息待完善'" />
            </view>
        </view>

        <!-- 特性描述 -->
        <view class="section">
            <view class="section-header">
                <view class="section-title">特性描述</view>
            </view>
            <view class="section-content">
                <rich-text :nodes="product.characteristics || '特色介绍待完善'" />
            </view>
        </view>

        <!-- 质检报告 -->
        <view class="section" v-if="product.qualityReport">
            <view class="section-header">
                <view class="section-title">质检报告</view>
            </view>
            <view class="section-content">
                <view class="report-item" @tap="previewReport">
                    <text class="report-icon">📄</text>
                    <text class="report-text">查看质检报告</text>
                    <text class="report-arrow">></text>
                </view>
            </view>
        </view>

        <!-- 底部安全距离占位 -->
        <view class="safe-area-bottom"></view>

        <!-- 底部操作栏 -->
        <view class="fab-bar">
            <view class="action-btn" @tap="goCart">
                <text class="action-icon">🛒</text>
                <text class="action-text">购物车</text>
            </view>
            <view class="quantity-selector" @tap="showQuantityModal">
                <text class="quantity-label">数量</text>
                <text class="quantity-value">{{ quantity }}</text>
                <text class="quantity-arrow">></text>
            </view>
            <button class="btn-cart" @tap="addCart">加入购物车</button>
            <button class="btn-buy" type="primary" @tap="buyNow">立即购买</button>
        </view>

        <!-- 数量选择弹窗 -->
        <view class="quantity-modal" v-if="showQuantity" @tap="hideQuantityModal">
            <view class="modal-content" @tap.stop>
                <view class="modal-header">
                    <text class="modal-title">选择数量</text>
                    <text class="modal-close" @tap="hideQuantityModal">×</text>
                </view>
                <view class="modal-body">
                    <view class="stepper-large">
                        <view class="stepper-btn" :class="{ disabled: quantity <= 1 }" @tap="decrease">-</view>
                        <input type="number" v-model.number="quantity" class="stepper-input" />
                        <view class="stepper-btn" @tap="increase">+</view>
                    </view>
                </view>
                <view class="modal-footer">
                    <button class="modal-confirm" @tap="hideQuantityModal">确定</button>
                </view>
            </view>
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
const showQuantity = ref(false)
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

function showQuantityModal() {
    showQuantity.value = true
}

function hideQuantityModal() {
    showQuantity.value = false
}
</script>

<style scoped>
.detail-page {
    padding-bottom: 180rpx;
    background: #f5f5f5;
    min-height: 100vh;
}

/* 图片轮播区域 */
.gallery {
    height: 750rpx;
    width: 100%;
    background: #fff;
}

.gallery image {
    width: 100%;
    height: 100%;
    display: block;
}

/* 商品信息卡片 */
.info-card {
    margin: 24rpx 24rpx 0;
    background: #ffffff;
    border-radius: 20rpx;
    padding: 32rpx 28rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.title {
    font-size: 36rpx;
    font-weight: 600;
    color: #1a1a1a;
    line-height: 1.5;
    margin-bottom: 20rpx;
}

.sub-info {
    display: flex;
    flex-wrap: wrap;
    gap: 24rpx;
    margin-bottom: 24rpx;
}

.info-item {
    font-size: 26rpx;
    color: #666;
    line-height: 1.5;
}

.price-row {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    padding-top: 20rpx;
    border-top: 1rpx solid #f0f0f0;
}

.price-wrapper {
    display: flex;
    align-items: baseline;
}

.price-symbol {
    font-size: 28rpx;
    color: #e73a32;
    font-weight: 600;
    margin-right: 4rpx;
}

.price {
    font-size: 44rpx;
    color: #e73a32;
    font-weight: 700;
    line-height: 1;
}

.stock-wrapper {
    display: flex;
    align-items: center;
    gap: 8rpx;
}

.stock-label {
    font-size: 24rpx;
    color: #999;
}

.stock {
    font-size: 26rpx;
    color: #2b9939;
    font-weight: 500;
}

/* 详情区块 */
.section {
    margin: 24rpx 24rpx 0;
    background: #ffffff;
    border-radius: 20rpx;
    padding: 0;
    overflow: hidden;
}

.section-header {
    padding: 32rpx 28rpx 24rpx;
    border-bottom: 1rpx solid #f0f0f0;
}

.section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #1a1a1a;
    position: relative;
    padding-left: 16rpx;
}

.section-title::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 6rpx;
    height: 28rpx;
    background: linear-gradient(180deg, #2b9939, #53bf68);
    border-radius: 3rpx;
}

.section-content {
    padding: 24rpx 28rpx 32rpx;
    font-size: 28rpx;
    color: #555;
    line-height: 1.8;
    word-break: break-all;
}

/* 质检报告 */
.report-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
}

.report-icon {
    font-size: 32rpx;
    margin-right: 16rpx;
}

.report-text {
    flex: 1;
    font-size: 28rpx;
    color: #2b9939;
}

.report-arrow {
    font-size: 28rpx;
    color: #ccc;
    font-weight: 300;
}

/* 底部安全距离 */
.safe-area-bottom {
    height: 140rpx;
}

/* 底部操作栏 */
.fab-bar {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    padding: 20rpx 24rpx;
    padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
    background: #ffffff;
    box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.08);
    z-index: 100;
    gap: 16rpx;
}

.action-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100rpx;
    padding: 8rpx 0;
}

.action-icon {
    font-size: 44rpx;
    line-height: 1;
    margin-bottom: 4rpx;
}

.action-text {
    font-size: 22rpx;
    color: #666;
}

.quantity-selector {
    display: flex;
    align-items: center;
    padding: 0 20rpx;
    height: 64rpx;
    background: #f5f5f5;
    border-radius: 32rpx;
    gap: 12rpx;
}

.quantity-label {
    font-size: 26rpx;
    color: #333;
}

.quantity-value {
    font-size: 28rpx;
    color: #1a1a1a;
    font-weight: 600;
    min-width: 40rpx;
    text-align: center;
}

.quantity-arrow {
    font-size: 24rpx;
    color: #999;
    font-weight: 300;
}

.btn-cart,
.btn-buy {
    flex: 1;
    height: 80rpx;
    line-height: 80rpx;
    border-radius: 40rpx;
    font-size: 28rpx;
    font-weight: 600;
    border: none;
    padding: 0;
}

.btn-cart {
    background: #fff3e0;
    color: #e73a32;
}

.btn-buy {
    background: linear-gradient(90deg, #2b9939, #53bf68);
    color: #ffffff;
}

/* 数量选择弹窗 */
.quantity-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 1000;
    display: flex;
    align-items: flex-end;
}

.modal-content {
    width: 100%;
    background: #ffffff;
    border-radius: 32rpx 32rpx 0 0;
    padding-bottom: env(safe-area-inset-bottom);
    animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
    from {
        transform: translateY(100%);
    }

    to {
        transform: translateY(0);
    }
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32rpx 32rpx 24rpx;
    border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #1a1a1a;
}

.modal-close {
    font-size: 48rpx;
    color: #999;
    line-height: 1;
    width: 48rpx;
    height: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
}

.modal-body {
    padding: 40rpx 32rpx;
}

.stepper-large {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0;
}

.stepper-btn {
    width: 80rpx;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f5f5;
    font-size: 36rpx;
    color: #333;
    font-weight: 500;
    border-radius: 8rpx;
}

.stepper-btn.disabled {
    color: #ccc;
    background: #f9f9f9;
}

.stepper-input {
    width: 120rpx;
    height: 80rpx;
    text-align: center;
    font-size: 32rpx;
    color: #1a1a1a;
    font-weight: 600;
    background: #f9f9f9;
    margin: 0 20rpx;
    border-radius: 8rpx;
}

.modal-footer {
    padding: 24rpx 32rpx 32rpx;
}

.modal-confirm {
    width: 100%;
    height: 88rpx;
    line-height: 88rpx;
    background: linear-gradient(90deg, #2b9939, #53bf68);
    color: #ffffff;
    border-radius: 44rpx;
    font-size: 30rpx;
    font-weight: 600;
    border: none;
}

.loading {
    padding: 200rpx 0;
    text-align: center;
    color: #888;
    font-size: 28rpx;
}
</style>
