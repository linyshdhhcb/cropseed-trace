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
                            <view class="recommendation-tags" v-if="item.recommendationTags && item.recommendationTags.length">
                                <view v-for="tag in item.recommendationTags" :key="tag.name" 
                                      class="tag" :class="tag.type">
                                    {{ tag.name }}
                                </view>
                            </view>
                            <view v-else class="product-desc">{{ item.characteristics || '高产稳产' }}</view>
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
import { getCategoryTree, getRecommendProducts, getProductList, reportBehavior } from '@/api/product.js'
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
    
    // 延迟上报页面浏览行为
    setTimeout(() => {
        reportPageView()
    }, 2000)
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
                characteristics: item.recommendationReason || '推荐商品',
                recommendationTags: parseRecommendationTags(item.recommendationReason)
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
    // 上报点击行为
    reportClickBehavior(id, '首页推荐')
    uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
}

// 上报用户行为
async function reportClickBehavior(seedId, source) {
    if (!userStore.isLoggedIn) return
    
    try {
        await reportBehavior({
            seedId: seedId,
            behaviorType: 1, // 1-浏览
            duration: 1,
            source: source
        })
    } catch (error) {
        console.warn('上报行为失败:', error)
    }
}

// 上报页面浏览行为
async function reportPageView() {
    if (!userStore.isLoggedIn) return
    
    try {
        // 上报首页浏览行为（可以用一个特殊的seedId表示页面）
        await reportBehavior({
            seedId: 0, // 0表示页面浏览
            behaviorType: 1,
            duration: 5,
            source: '首页浏览'
        })
    } catch (error) {
        console.warn('上报页面浏览失败:', error)
    }
}

function goLogin() {
    uni.navigateTo({ url: '/pages/auth/login' })
}

function chooseAddress() {
    uni.showToast({ title: '定位服务开发中', icon: 'none' })
}

// 解析推荐理由为标签
function parseRecommendationTags(reason) {
    if (!reason) return []
    
    // 定义算法标签映射
    const algorithmMap = {
        '协同过滤': { name: '协同过滤', type: 'collaborative' },
        '内容推荐': { name: '内容', type: 'content' },
        '个性化推荐': { name: '个性化', type: 'personalized' },
        '热门推荐': { name: '热门', type: 'popular' },
        '混合推荐': { name: '智能推荐', type: 'hybrid' }
    }
    
    const tags = []
    
    // 如果是混合推荐，只显示智能推荐标签，避免标签过多
    if (reason.includes('混合推荐:')) {
        tags.push(algorithmMap['混合推荐'])
        
        // 可以选择性地添加1-2个主要算法标签
        const algorithms = reason.replace('混合推荐:', '').split('+')
        let addedCount = 0
        algorithms.forEach(alg => {
            const trimmed = alg.trim()
            if (algorithmMap[trimmed] && addedCount < 2) {
                tags.push(algorithmMap[trimmed])
                addedCount++
            }
        })
    } else {
        // 单一算法推荐
        Object.keys(algorithmMap).forEach(key => {
            if (reason.includes(key)) {
                tags.push(algorithmMap[key])
            }
        })
    }
    
    // 如果没有匹配到任何算法，返回默认标签
    if (tags.length === 0) {
        tags.push({ name: '推荐', type: 'default' })
    }
    
    return tags
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

.recommendation-tags {
    margin-top: 8rpx;
    display: flex;
    flex-wrap: wrap;
    gap: 8rpx;
}

.tag {
    padding: 6rpx 16rpx;
    border-radius: 24rpx;
    font-size: 20rpx;
    color: #fff;
    background: #667eea;
    white-space: nowrap;
    max-width: 140rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
    transform: scale(1);
    transition: all 0.2s ease;
}

/* 不同算法类型的标签颜色 */
.tag.hybrid {
    background: #667eea;
    box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
}

.tag.collaborative {
    background: #f093fb;
    box-shadow: 0 4rpx 12rpx rgba(240, 147, 251, 0.3);
}

.tag.content {
    background: #4facfe;
    box-shadow: 0 4rpx 12rpx rgba(79, 172, 254, 0.3);
}

.tag.personalized {
    background: #43e97b;
    box-shadow: 0 4rpx 12rpx rgba(67, 233, 123, 0.3);
}

.tag.popular {
    background: #fa709a;
    box-shadow: 0 4rpx 12rpx rgba(250, 112, 154, 0.3);
}

.tag.default {
    background: #a8edea;
    color: #666;
    box-shadow: 0 4rpx 12rpx rgba(168, 237, 234, 0.3);
}

.empty {
    grid-column: span 2;
    text-align: center;
    color: #aaa;
    padding: 80rpx 0;
}
</style>
