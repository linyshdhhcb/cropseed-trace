<template>
    <view class="trace-page">
        <!-- 顶部标题区域 -->
        <view class="header-section">
            <view class="header-title">产品溯源查询</view>
            <view class="header-subtitle">扫描二维码或输入溯源码查询产品信息</view>
        </view>

        <!-- 查询操作区域 -->
        <view class="query-section">
            <!-- 扫码按钮 -->
            <view class="scan-card" @tap="scanCode">
                <image src="/static/trace/camera.png" class="scan-icon" mode="aspectFit" />
                <view class="scan-text">
                    <text class="scan-title">扫描二维码</text>
                    <text class="scan-desc">扫描产品包装上的溯源二维码</text>
                </view>
                <view class="scan-arrow">›</view>
            </view>

            <!-- 手动输入区域 -->
            <view class="input-card">
                <view class="input-header">
                    <text class="input-title">手动输入溯源码</text>
                </view>
                <view class="input-wrapper">
                    <input 
                        class="trace-input" 
                        v-model="traceCode" 
                        placeholder="请输入12位溯源码（如：BJ2024000001）"
                        maxlength="12"
                        @input="onInputChange"
                    />
                    <button 
                        class="query-btn" 
                        :disabled="!traceCode || querying"
                        :loading="querying"
                        @tap="queryTrace"
                    >
                        查询
                    </button>
                </view>
            </view>
        </view>

        <!-- 溯源结果展示区域 -->
        <view class="result-section" v-if="traceResult">
            <!-- 产品基本信息 -->
            <view class="product-card">
                <view class="card-header">
                    <text class="card-title">产品信息</text>
                    <view class="verify-badge" :class="{'verified': traceResult.blockchainInfo.verified}">
                        <text class="badge-icon">{{ traceResult.blockchainInfo.verified ? '✓' : '○' }}</text>
                        <text class="badge-text">{{ traceResult.blockchainInfo.verified ? '区块链已验证' : '未上链' }}</text>
                    </view>
                </view>
                <view class="product-info">
                    <view class="info-row">
                        <text class="info-label">溯源码：</text>
                        <text class="info-value">{{ traceResult.traceCode }}</text>
                    </view>
                    <view class="info-row" v-if="traceResult.productInfo">
                        <text class="info-label">批次号：</text>
                        <text class="info-value">{{ traceResult.productInfo.batchId }}</text>
                    </view>
                    <view class="info-row" v-if="traceResult.productInfo">
                        <text class="info-label">生产单位：</text>
                        <text class="info-value">{{ traceResult.productInfo.entityName || '-' }}</text>
                    </view>
                    <view class="info-row">
                        <text class="info-label">溯源记录：</text>
                        <text class="info-value highlight">{{ traceResult.recordCount }} 条</text>
                    </view>
                    <view class="info-row" v-if="traceResult.queryTime">
                        <text class="info-label">查询时间：</text>
                        <text class="info-value">{{ traceResult.queryTime }}</text>
                    </view>
                </view>
            </view>

            <!-- 溯源链时间线 -->
            <view class="timeline-card">
                <view class="card-header">
                    <text class="card-title">溯源链</text>
                    <text class="card-subtitle">完整追溯产品生产流通全过程</text>
                </view>
                <view class="timeline">
                    <view 
                        class="timeline-item" 
                        v-for="(item, index) in traceResult.traceChain" 
                        :key="item.id"
                    >
                        <view class="timeline-dot" :class="{'first': index === 0, 'last': index === traceResult.traceChain.length - 1}">
                            <view class="dot-inner" :class="{'on-chain': item.onChain}"></view>
                        </view>
                        <view class="timeline-content">
                            <view class="timeline-header">
                                <text class="stage-name">{{ item.stageName }}</text>
                                <text class="on-chain-badge" v-if="item.onChain">🔗链上</text>
                            </view>
                            <view class="timeline-body">
                                <text class="content-text">{{ item.content || '-' }}</text>
                                <!-- 详细信息 -->
                                <view class="detail-info" v-if="item.temperature || item.humidity || item.quantity || item.qualityGrade">
                                    <view class="detail-item" v-if="item.temperature">
                                        <image src="/static/trace/temperature.png" class="info-icon" mode="aspectFit" />
                                        <text>{{ item.temperature }}℃</text>
                                    </view>
                                    <view class="detail-item" v-if="item.humidity">
                                        <image src="/static/trace/humidity.png" class="info-icon" mode="aspectFit" />
                                        <text>{{ item.humidity }}%</text>
                                    </view>
                                    <view class="detail-item" v-if="item.quantity">
                                        <image src="/static/trace/unit.png" class="info-icon" mode="aspectFit" />
                                        <text>{{ item.quantity }}{{ item.unit || '' }}</text>
                                    </view>
                                    <view class="detail-item quality" v-if="item.qualityGrade">
                                        <text>质量等级：{{ item.qualityGrade }}</text>
                                    </view>
                                </view>
                                <!-- 操作人员和地点 -->
                                <view class="operator-info">
                                    <view class="operator-text" v-if="item.operatorName">
                                        <image src="/static/trace/user.png" class="info-icon" mode="aspectFit" />
                                        <text>{{ item.operatorName }}</text>
                                    </view>
                                    <view class="location-text" v-if="item.location">
                                        <image src="/static/trace/address.png" class="info-icon" mode="aspectFit" />
                                        <text>{{ item.location }}</text>
                                    </view>
                                </view>
                                <!-- 详细内容（解析detailedContent） -->
                                <view class="detailed-content" v-if="parseDetailedContent(item.detailedContent)">
                                    <view class="detail-section">
                                        <text class="section-title">详细信息</text>
                                        <view class="detail-grid">
                                            <view 
                                                class="detail-grid-item" 
                                                v-for="(detail, key) in parseDetailedContent(item.detailedContent)" 
                                                :key="key"
                                                :class="{'full-width': isImageUrl(key, detail)}"
                                            >
                                                <text class="detail-key">{{ formatDetailKey(key) }}：</text>
                                                <!-- 图片类型字段直接显示图片 -->
                                                <image 
                                                    v-if="isImageUrl(key, detail)"
                                                    :src="detail"
                                                    class="detail-image"
                                                    mode="aspectFill"
                                                    @tap="previewImage([detail], 0)"
                                                />
                                                <!-- 普通文本字段 -->
                                                <text 
                                                    v-else
                                                    class="detail-value"
                                                >{{ formatDetailValue(detail) }}</text>
                                            </view>
                                        </view>
                                    </view>
                                </view>
                                <!-- 图片展示 -->
                                <view class="images-wrapper" v-if="item.images && item.images.length > 0">
                                    <image 
                                        v-for="(img, imgIndex) in item.images" 
                                        :key="imgIndex"
                                        :src="img" 
                                        class="record-image"
                                        mode="aspectFill"
                                        @tap="previewImage(item.images, imgIndex)"
                                    />
                                </view>
                            </view>
                            <view class="timeline-footer">
                                <text class="time-text">{{ formatTime(item.recordTime) }}</text>
                            </view>
                        </view>
                    </view>
                </view>
            </view>

            <!-- 区块链验证信息 -->
            <view class="blockchain-card" v-if="traceResult.blockchainInfo && traceResult.blockchainInfo.verified">
                <view class="card-header">
                    <text class="card-title">区块链存证信息</text>
                </view>
                <view class="blockchain-info">
                    <view class="info-row" v-if="traceResult.blockchainInfo.txId">
                        <text class="info-label">交易哈希：</text>
                        <text class="info-value hash">{{ traceResult.blockchainInfo.txId }}</text>
                    </view>
                    <view class="info-row" v-if="traceResult.blockchainInfo.timestamp">
                        <text class="info-label">上链时间：</text>
                        <text class="info-value">{{ formatTimestamp(traceResult.blockchainInfo.timestamp) }}</text>
                    </view>
                    <view class="info-row" v-if="!traceResult.blockchainInfo.txId && traceResult.blockchainInfo.verified">
                        <text class="info-label">验证状态：</text>
                        <text class="info-value">已上链验证</text>
                    </view>
                </view>
            </view>
        </view>

        <!-- 空状态提示 -->
        <view class="empty-state" v-if="!traceResult && !querying">
            <image src="/static/trace.png" class="empty-icon" mode="aspectFit" />
            <text class="empty-text">扫描二维码或输入溯源码开始查询</text>
        </view>
    </view>
</template>

<script>
import { scanTraceCode, queryTraceCode } from "@/api/trace.js";

export default {
    data() {
        return {
            traceCode: "",
            querying: false,
            traceResult: null
        };
    },
    methods: {
        /**
         * 扫描二维码
         */
        scanCode() {
            uni.scanCode({
                success: (res) => {
                    console.log('扫码结果:', res);
                    // 从扫描结果中提取溯源码
                    let code = res.result;
                    
                    // 如果扫描的是完整URL，尝试从URL中提取溯源码
                    if (code.includes('traceCode=')) {
                        const match = code.match(/traceCode=([A-Z0-9]+)/);
                        if (match) {
                            code = match[1];
                        }
                    }
                    
                    this.traceCode = code;
                    this.queryTraceByCode(code, true);
                },
                fail: (err) => {
                    console.error('扫码失败:', err);
                    uni.showToast({
                        title: '扫码失败，请重试',
                        icon: 'none'
                    });
                }
            });
        },

        /**
         * 手动查询
         */
        queryTrace() {
            if (!this.traceCode) {
                uni.showToast({
                    title: '请输入溯源码',
                    icon: 'none'
                });
                return;
            }
            this.queryTraceByCode(this.traceCode, false);
        },

        /**
         * 查询溯源信息
         * @param {String} code - 溯源码
         * @param {Boolean} isScan - 是否扫码查询
         */
        async queryTraceByCode(code, isScan) {
            this.querying = true;
            this.traceResult = null;

            try {
                // 获取用户openid（如果已登录）
                const userInfo = uni.getStorageSync('userInfo');
                const openid = userInfo?.openid || '';

                // 调用不同的API
                const apiCall = isScan ? scanTraceCode : queryTraceCode;
                const data = await apiCall(code, openid);

                // http.js 成功时已经返回 data.data，直接使用
                this.traceResult = data;
                uni.showToast({
                    title: '查询成功',
                    icon: 'success'
                });
            } catch (error) {
                console.error('查询溯源信息失败:', error);
                // error 包含完整的错误信息
                uni.showToast({
                    title: error?.message || '查询失败，请重试',
                    icon: 'none'
                });
            } finally {
                this.querying = false;
            }
        },

        /**
         * 输入变化处理
         */
        onInputChange(e) {
            // 自动转大写
            this.traceCode = e.detail.value.toUpperCase();
        },

        /**
         * 格式化时间
         */
        formatTime(time) {
            if (!time) return '-';
            // 处理字符串日期
            const date = new Date(time);
            if (isNaN(date.getTime())) return '-';
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hour = String(date.getHours()).padStart(2, '0');
            const minute = String(date.getMinutes()).padStart(2, '0');
            return `${year}-${month}-${day} ${hour}:${minute}`;
        },

        /**
         * 格式化时间戳（处理字符串格式的时间戳）
         */
        formatTimestamp(timestamp) {
            if (!timestamp) return '-';
            // 将字符串时间戳转为数字
            const time = typeof timestamp === 'string' ? parseInt(timestamp) : timestamp;
            if (isNaN(time)) return '-';
            const date = new Date(time);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hour = String(date.getHours()).padStart(2, '0');
            const minute = String(date.getMinutes()).padStart(2, '0');
            const second = String(date.getSeconds()).padStart(2, '0');
            return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
        },

        /**
         * 预览图片
         */
        previewImage(images, index) {
            uni.previewImage({
                urls: images,
                current: index
            });
        },

        /**
         * 解析详细内容JSON
         */
        parseDetailedContent(detailedContent) {
            if (!detailedContent) return null;
            try {
                const data = typeof detailedContent === 'string' ? JSON.parse(detailedContent) : detailedContent;
                // 过滤掉不需要展示的字段
                const excludeKeys = ['action', 'traceCode', 'seedId', 'producerId'];
                const filtered = {};
                Object.keys(data).forEach(key => {
                    if (!excludeKeys.includes(key) && data[key] !== null && data[key] !== '') {
                        filtered[key] = data[key];
                    }
                });
                return Object.keys(filtered).length > 0 ? filtered : null;
            } catch (e) {
                console.error('解析detailedContent失败:', e);
                return null;
            }
        },

        /**
         * 格式化详细信息的键名
         */
        formatDetailKey(key) {
            const keyMap = {
                'batchNo': '批次号',
                'producerName': '生产商',
                'productionLocation': '产地',
                'productionDate': '生产日期',
                'expiryDate': '保质期至',
                'harvestDate': '收获日期',
                'processingDate': '加工日期',
                'productionEquipment': '生产设备',
                'processingMethod': '加工方式',
                'seedSource': '种子来源',
                'parentVariety': '亲本品种',
                'qualityGrade': '质量等级',
                'moistureContent': '含水率',
                'germinationRate': '发芽率',
                'purity': '纯度',
                'qualityStatus': '质量状态',
                'qualityReport': '质检报告',
                'unit': '单位',
                'packagingType': '包装类型',
                'packagingSpecification': '包装规格',
                'storageCondition': '储存条件',
                'certification': '认证信息',
                'traceabilityLevel': '溯源级别',
                'operatorName': '操作人员',
                'operatorPhone': '联系电话',
                'remarks': '备注'
            };
            return keyMap[key] || key;
        },

        /**
         * 格式化详细信息的值
         */
        formatDetailValue(value) {
            if (value === null || value === undefined || value === '') return '-';
            // 处理数字
            if (typeof value === 'number') {
                return value.toString();
            }
            return value;
        },

        /**
         * 判断是否是图片URL字段
         */
        isImageUrl(key, value) {
            // 判断字段名包含report、image、photo等关键词，且值是URL
            const imageKeys = ['qualityReport', 'report', 'image', 'photo', 'picture'];
            const isImageKey = imageKeys.some(k => key.toLowerCase().includes(k.toLowerCase()));
            const isUrl = typeof value === 'string' && value.startsWith('http');
            const isImageExt = typeof value === 'string' && value.match(/\.(jpg|jpeg|png|gif|webp|bmp)$/i);
            return isImageKey && isUrl && isImageExt;
        }
    }
};
</script>

<style lang="scss" scoped>
.trace-page {
    min-height: 100vh;
    background: #f5f5f5;
    padding-bottom: 20rpx;
}

/* 顶部标题区域 */
.header-section {
    background: linear-gradient(135deg, #2b9939 0%, #1afa29 100%);
    padding: 60rpx 40rpx 40rpx;
    color: white;
    text-align: center;
}

.header-title {
    font-size: 48rpx;
    font-weight: bold;
    margin-bottom: 20rpx;
}

.header-subtitle {
    font-size: 28rpx;
    opacity: 0.9;
}

/* 查询操作区域 */
.query-section {
    padding: 30rpx;
}

.scan-card {
    background: white;
    border-radius: 20rpx;
    padding: 40rpx;
    margin-bottom: 30rpx;
    display: flex;
    align-items: center;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.scan-icon {
    width: 80rpx;
    height: 80rpx;
    margin-right: 30rpx;
}

.scan-text {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.scan-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 10rpx;
}

.scan-desc {
    font-size: 26rpx;
    color: #999;
}

.scan-arrow {
    font-size: 60rpx;
    color: #2b9939;
}

.input-card {
    background: white;
    border-radius: 20rpx;
    padding: 40rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.input-header {
    margin-bottom: 30rpx;
}

.input-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
}

.input-wrapper {
    display: flex;
    gap: 20rpx;
}

.trace-input {
    flex: 1;
    height: 80rpx;
    border: 2rpx solid #e0e0e0;
    border-radius: 12rpx;
    padding: 0 20rpx;
    font-size: 28rpx;
}

.query-btn {
    width: 160rpx;
    height: 80rpx;
    background: #2b9939;
    color: white;
    border: none;
    border-radius: 12rpx;
    font-size: 28rpx;
    line-height: 80rpx;
    padding: 0;
}

.query-btn[disabled] {
    background: #ccc;
}

/* 结果展示区域 */
.result-section {
    padding: 0 30rpx 30rpx;
}

.product-card,
.timeline-card,
.blockchain-card {
    background: white;
    border-radius: 20rpx;
    padding: 40rpx;
    margin-bottom: 30rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;
    padding-bottom: 20rpx;
    border-bottom: 2rpx solid #f0f0f0;
}

.card-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
}

.card-subtitle {
    font-size: 24rpx;
    color: #999;
}

.verify-badge {
    display: flex;
    align-items: center;
    padding: 8rpx 20rpx;
    border-radius: 20rpx;
    background: #f5f5f5;
    font-size: 24rpx;
}

.verify-badge.verified {
    background: #e8f5e9;
    color: #2b9939;
}

.badge-icon {
    margin-right: 8rpx;
}

.product-info,
.blockchain-info {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
}

.info-row {
    display: flex;
    font-size: 28rpx;
}

.info-label {
    color: #666;
    min-width: 160rpx;
}

.info-value {
    flex: 1;
    color: #333;
    word-break: break-all;
}

.info-value.highlight {
    color: #2b9939;
    font-weight: bold;
}

.info-value.hash {
    font-size: 24rpx;
    font-family: monospace;
    color: #666;
}

/* 时间线样式 */
.timeline {
    position: relative;
}

.timeline-item {
    display: flex;
    margin-bottom: 40rpx;
    position: relative;

    &:last-child {
        margin-bottom: 0;
    }
}

.timeline-dot {
    position: relative;
    width: 40rpx;
    flex-shrink: 0;
    display: flex;
    align-items: flex-start;
    padding-top: 8rpx;

    &::after {
        content: '';
        position: absolute;
        left: 50%;
        top: 40rpx;
        bottom: -40rpx;
        width: 2rpx;
        background: #e0e0e0;
        transform: translateX(-50%);
    }

    &.last::after {
        display: none;
    }
}

.dot-inner {
    width: 24rpx;
    height: 24rpx;
    border-radius: 50%;
    background: #e0e0e0;
    border: 4rpx solid white;
    box-shadow: 0 0 0 2rpx #e0e0e0;
}

.dot-inner.on-chain {
    background: #2b9939;
    box-shadow: 0 0 0 2rpx #2b9939;
}

.timeline-content {
    flex: 1;
    margin-left: 20rpx;
    background: #f8f8f8;
    border-radius: 12rpx;
    padding: 24rpx;
}

.timeline-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
}

.stage-name {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
}

.on-chain-badge {
    font-size: 22rpx;
    color: #2b9939;
    background: #e8f5e9;
    padding: 4rpx 12rpx;
    border-radius: 8rpx;
}

.timeline-body {
    margin-bottom: 16rpx;
}

.content-text {
    font-size: 28rpx;
    color: #666;
    line-height: 1.6;
    display: block;
    margin-bottom: 16rpx;
}

.detail-info {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    margin-bottom: 16rpx;
}

.detail-item {
    display: flex;
    align-items: center;
    gap: 8rpx;
    font-size: 24rpx;
    color: #888;
    background: white;
    padding: 8rpx 16rpx;
    border-radius: 8rpx;
}

.detail-item.quality {
    background: #fff8e1;
    color: #f57c00;
    font-weight: bold;
}

.operator-info {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    font-size: 24rpx;
    color: #888;
}

.operator-text,
.location-text {
    display: flex;
    align-items: center;
    gap: 8rpx;
}

.info-icon {
    width: 28rpx;
    height: 28rpx;
    flex-shrink: 0;
}

.detailed-content {
    margin-top: 16rpx;
    background: #fafafa;
    border-radius: 12rpx;
    padding: 20rpx;
}

.detail-section {
    display: flex;
    flex-direction: column;
}

.section-title {
    font-size: 26rpx;
    font-weight: bold;
    color: #2b9939;
    margin-bottom: 16rpx;
}

.detail-grid {
    display: flex;
    flex-direction: column;
    gap: 12rpx;
}

.detail-grid-item {
    display: flex;
    font-size: 24rpx;
    line-height: 1.6;
}

.detail-grid-item.full-width {
    flex-direction: column;
    align-items: flex-start;
}

.detail-key {
    color: #666;
    min-width: 160rpx;
    flex-shrink: 0;
}

.detail-value {
    color: #333;
    flex: 1;
    word-break: break-all;
}

.detail-image {
    width: 100%;
    height: 300rpx;
    border-radius: 8rpx;
    margin-top: 8rpx;
    background: #f0f0f0;
}

.images-wrapper {
    display: flex;
    gap: 16rpx;
    margin-top: 16rpx;
    flex-wrap: wrap;
}

.record-image {
    width: 150rpx;
    height: 150rpx;
    border-radius: 8rpx;
}

.timeline-footer {
    display: flex;
    justify-content: flex-end;
}

.time-text {
    font-size: 24rpx;
    color: #999;
}

/* 空状态 */
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 120rpx 40rpx;
}

.empty-icon {
    width: 200rpx;
    height: 200rpx;
    margin-bottom: 40rpx;
    opacity: 0.5;
}

.empty-text {
    font-size: 28rpx;
    color: #999;
}
</style>
