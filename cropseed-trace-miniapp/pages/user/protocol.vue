<template>
    <view class="protocol-page">
        <view class="header">
            <view class="badge">IMPORTANT</view>
            <view class="title">{{ title }}</view>
            <view class="subtitle">请仔细阅读以下内容，充分了解并同意相关条款后继续使用本服务</view>
        </view>
        <view class="content-card">
            <scroll-view scroll-y class="content-scroll">
                <view class="content-body">
                    <rich-text :nodes="content" />
                </view>
            </scroll-view>
        </view>
    </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const title = ref('用户协议')
const content = ref('')

const defaultContent = {
    user: `欢迎您使用农作物种质资源数字化溯源系统小程序。\n\n一、使用须知\n1. 您应遵守相关法律法规，不得利用本平台从事违法活动。\n2. 平台提供商品浏览、下单、支付、订单查询等服务，具体以实际功能为准。\n3. 如发现账户被盗用，请及时联系我们处理。\n\n二、服务规范\n1. 请妥善保管账户和密码，勿向他人透露。\n2. 在使用过程中产生的订单、评论等信息应符合文明规范。\n3. 如您违反相关规定，平台有权采取冻结或终止服务等措施。\n\n三、通知与变更\n1. 我们可能根据运营需要对协议条款进行变更，并通过页面公告、消息通知等方式提示。\n2. 若您继续使用平台服务，即视为接受变更后的协议内容。`,
    privacy: `我们高度重视您的个人信息保护。\n\n一、信息收集与使用\n1. 为完成订单履约，我们可能会收集您的姓名、电话、地址等基本信息。\n2. 收集的信息仅用于订单处理、售后服务、客户支持等目的。\n\n二、信息共享与披露\n1. 未经您授权，我们不会向第三方披露您的个人信息，法律法规另有规定除外。\n2. 在物流派送、支付结算等场景，我们会向合作伙伴提供必要的信息，且要求其严格保密。\n\n三、信息的管理\n1. 您可在“我的-收货地址”页面更新或删除相关信息。\n2. 如需注销账号，可通过客服渠道提交申请，我们将在核验后予以处理。\n\n四、信息安全\n1. 我们采用合理的安全措施保护您的信息，防止数据遗失、滥用或未经授权的访问。\n2. 如发生信息安全事件，我们将按照法规要求及时通知您。`
}

onLoad((options) => {
    const type = options?.type || 'user'
    title.value = type === 'privacy' ? '隐私政策' : '用户协议'
    content.value = defaultContent[type] || defaultContent.user
})
</script>

<style scoped>
.protocol-page {
    min-height: 100vh;
    background: linear-gradient(180deg, #f5fff6 0%, #f7f9fb 40%, #ffffff 100%);
    padding: 40rpx 32rpx 48rpx;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
}

.header {
    background: #ffffff;
    border-radius: 28rpx;
    padding: 36rpx 32rpx;
    box-shadow: 0 16rpx 48rpx rgba(43, 153, 57, 0.12);
    text-align: center;
}

.badge {
    display: inline-block;
    padding: 8rpx 24rpx;
    border-radius: 999rpx;
    background: rgba(43, 153, 57, 0.12);
    color: #2b9939;
    font-size: 22rpx;
    letter-spacing: 2rpx;
}

.title {
    margin-top: 18rpx;
    font-size: 40rpx;
    font-weight: 700;
    color: #1a1a1a;
}

.subtitle {
    margin-top: 16rpx;
    font-size: 26rpx;
    color: #666;
    line-height: 40rpx;
}

.content-card {
    flex: 1;
    margin-top: 32rpx;
    background: #ffffff;
    border-radius: 24rpx;
    box-shadow: 0 12rpx 36rpx rgba(0, 0, 0, 0.06);
    overflow: hidden;
}

.content-scroll {
    height: 100%;
}

.content-body {
    padding: 36rpx 32rpx 48rpx;
}

.content-body rich-text {
    font-size: 28rpx;
    line-height: 48rpx;
    color: #333;
    white-space: pre-wrap;
}
</style>
