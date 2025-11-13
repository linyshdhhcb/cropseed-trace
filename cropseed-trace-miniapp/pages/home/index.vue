<template>
    <view class="home-page">
        <view class="home-header">
            <view class="location" @tap="chooseAddress">
                <image class="location-icon" src="/static/positioning.png" mode="aspectFit"></image>
                <text class="address">{{ userStore.userInfo?.city || '定位中' }}</text>
            </view>
            <view class="search-bar" @tap="goSearch">
                <image class="search-icon" src="/static/search.png" mode="aspectFit"></image>
                <text class="placeholder">搜索品种、品类、关键词</text>
            </view>
            <view v-if="!userStore.isLoggedIn" class="login-entry" @tap="goLogin">登录</view>
        </view>

        <scroll-view class="home-scroll" scroll-y enable-back-to-top>
            <view class="banner">
                <swiper class="banner-swiper" circular autoplay interval="5000" duration="500">
                    <swiper-item v-for="(image, index) in bannerImages" :key="index">
                        <image :src="image" mode="aspectFill"></image>
                    </swiper-item>
                </swiper>
            </view>

            <view class="section">
                <view class="section-title">热门分类</view>
                <view class="category-grid">
                    <view v-for="item in topCategories" :key="item.id" class="category-item" @tap="goCategory(item)">
                        <view class="category-name">{{ item.categoryName }}</view>
                        <text class="category-count">{{ item.productCount || 0 }} 个品种</text>
                    </view>
                </view>
            </view>

            <view class="section">
                <view class="section-title">
                    推荐好种
                    <view class="more" @tap="goRecommend">更多</view>
                </view>
                <view class="product-list">
                    <view v-for="item in recommendList" :key="item.id" class="product-item" @tap="goDetail(item.id)">
                        <image class="product-image" :src="item.imageUrl || '/static/no-image-available.png'"
                            mode="aspectFill"></image>
                        <view class="product-info">
                            <view class="product-name">{{ item.seedName }}</view>
                            <view class="product-price">￥{{ item.unitPrice }}</view>
                            <view class="product-desc">{{ item.characteristics || '高产稳产' }}</view>
                        </view>
                    </view>
                    <view v-if="!recommendList.length && !loading" class="empty">暂无推荐数据</view>
                </view>
            </view>
        </scroll-view>
    </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCategoryTree, getRecommendProducts, getProductList } from '@/api/product.js'
import { useUserStore } from '@/stores/user.js'

const userStore = useUserStore()
const loading = ref(false)
const categoryTree = ref([])
const recommendList = ref([])
const bannerImages = [
    '/static/home-banner.png',
    '/static/home-banner1.png',
    '/static/home-banner2.png'
]

const topCategories = computed(() => categoryTree.value.slice(0, 6))

onMounted(() => {
    userStore.loadFromStorage()
    loadCategories()
    loadRecommend()
})

async function loadCategories() {
    try {
        const data = await getCategoryTree()
        console.log('首页获取的分类数据:', data)
        categoryTree.value = data || []
        if (categoryTree.value.length) {
            console.log('首页第一个分类的子分类:', categoryTree.value[0].children)
        }
    } catch (error) {
        console.error('获取分类失败', error)
    }
}

async function loadRecommend() {
    if (loading.value) return
    loading.value = true
    try {
        // 先尝试获取推荐商品
        const recommendData = await getRecommendProducts()
        if (Array.isArray(recommendData) && recommendData.length > 0) {
            // 将推荐数据转换为商品列表格式
            recommendList.value = recommendData.map(item => ({
                id: item.targetId,
                seedName: item.targetName || '商品名称',
                imageUrl: item.targetImage || '/static/no-image-available.png',
                unitPrice: item.targetPrice || '0.00',
                characteristics: item.recommendationReason || '推荐商品'
            }))
        } else {
            // 如果没有推荐数据，则获取商品列表作为后备
            const fallback = await getProductList({ page: 1, size: 6 })
            recommendList.value = fallback?.list || []
        }
    } catch (error) {
        console.error('获取推荐失败', error)
        // 如果推荐接口失败，也尝试获取商品列表作为后备
        try {
            const fallback = await getProductList({ page: 1, size: 6 })
            recommendList.value = fallback?.list || []
        } catch (fallbackError) {
            console.error('获取商品列表失败', fallbackError)
        }
    } finally {
        loading.value = false
    }
}

function goSearch() {
    uni.navigateTo({ url: '/pages/product/list' })
}

function goCategory(item) {
    const id = item?.id
    if (!id) return
    uni.navigateTo({ url: `/pages/product/list?categoryId=${id}` })
}

function goRecommend() {
    uni.navigateTo({ url: '/pages/product/list?recommend=1' })
}

function goDetail(id) {
    uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
}

function goLogin() {
    uni.navigateTo({ url: '/pages/auth/login' })
}

function chooseAddress() {
    uni.showToast({ title: '定位服务开发中', icon: 'none' })
}
</script>

<style scoped>
.home-page {
    min-height: 100vh;
    background-color: #f7f9fb;
}

.home-header {
    display: flex;
    align-items: center;
    padding: 24rpx 32rpx;
    background-color: #ffffff;
    box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.04);
}

.location {
    display: flex;
    align-items: center;
    font-size: 26rpx;
    color: #2b9939;
    margin-right: 24rpx;
}

.location-icon {
    width: 32rpx;
    height: 32rpx;
    margin-right: 8rpx;
}

.address {
    margin-left: 0;
}

.search-bar {
    flex: 1;
    display: flex;
    align-items: center;
    height: 72rpx;
    background-color: #f2f4f7;
    border-radius: 36rpx;
    padding: 0 24rpx;
    color: #999;
}

.search-icon {
    width: 32rpx;
    height: 32rpx;
    margin-right: 12rpx;
}

.placeholder {
    margin-left: 0;
}

.login-entry {
    margin-left: 20rpx;
    color: #2b9939;
    font-size: 26rpx;
}

.home-scroll {
    height: calc(100vh - 120rpx);
}

.banner {
    padding: 32rpx;
}

.banner-swiper {
    height: 320rpx;
    border-radius: 24rpx;
    overflow: hidden;
}

.banner-swiper image {
    width: 100%;
    height: 100%;
    border-radius: 24rpx;
}

.section {
    margin-top: 32rpx;
    padding: 0 32rpx;
}

.section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #1a1a1a;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.more {
    font-size: 26rpx;
    color: #2b9939;
}

.category-grid {
    margin-top: 24rpx;
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24rpx;
}

.category-item {
    background: #ffffff;
    padding: 24rpx;
    border-radius: 20rpx;
    box-shadow: 0 8rpx 24rpx rgba(43, 153, 57, 0.08);
}

.category-name {
    font-size: 28rpx;
    font-weight: 500;
    color: #2b9939;
}

.category-count {
    margin-top: 8rpx;
    font-size: 22rpx;
    color: #999;
}

.product-list {
    margin-top: 24rpx;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 24rpx;
}

.product-item {
    background: #ffffff;
    border-radius: 20rpx;
    overflow: hidden;
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
}

.product-image {
    width: 100%;
    height: 240rpx;
    background: #f5f5f5;
}

.product-info {
    padding: 20rpx;
}

.product-name {
    font-size: 28rpx;
    color: #1a1a1a;
    font-weight: 500;
    height: 72rpx;
    overflow: hidden;
}

.product-price {
    margin-top: 12rpx;
    color: #e73a32;
    font-size: 30rpx;
    font-weight: 600;
}

.product-desc {
    margin-top: 8rpx;
    font-size: 24rpx;
    color: #999;
}

.empty {
    grid-column: span 2;
    text-align: center;
    color: #aaa;
    padding: 80rpx 0;
}
</style>
