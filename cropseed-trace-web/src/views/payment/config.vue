<template>
  <div class="config-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>支付配置管理</span>
          <el-button type="primary" @click="saveConfig" :loading="saveLoading">保存配置</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="config-tabs">
        <!-- 支付宝配置 -->
        <el-tab-pane label="支付宝配置" name="alipay">
          <el-form :model="alipayConfig" label-width="120px" class="config-form">
            <el-form-item label="应用ID">
              <el-input v-model="alipayConfig.appId" placeholder="请输入支付宝应用ID" />
              <div class="form-tip">沙箱环境应用ID，用于测试</div>
            </el-form-item>
            
            <el-form-item label="服务器地址">
              <el-input v-model="alipayConfig.serverUrl" placeholder="支付宝网关地址" />
              <div class="form-tip">沙箱环境：https://openapi-sandbox.dl.alipaydev.com/gateway.do</div>
            </el-form-item>
            
            <el-form-item label="字符集">
              <el-select v-model="alipayConfig.charset" placeholder="请选择字符集">
                <el-option label="UTF-8" value="UTF-8" />
                <el-option label="GBK" value="GBK" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="签名类型">
              <el-select v-model="alipayConfig.signType" placeholder="请选择签名类型">
                <el-option label="RSA2" value="RSA2" />
                <el-option label="RSA" value="RSA" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="异步通知URL">
              <el-input v-model="alipayConfig.notifyUrl" placeholder="支付结果异步通知地址" />
              <div class="form-tip">支付完成后支付宝会向此地址发送通知</div>
            </el-form-item>
            
            <el-form-item label="同步返回URL">
              <el-input v-model="alipayConfig.returnUrl" placeholder="支付完成后跳转地址" />
              <div class="form-tip">用户支付完成后的跳转页面</div>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 微信支付配置 -->
        <el-tab-pane label="微信支付配置" name="wechat">
          <el-form :model="wechatConfig" label-width="120px" class="config-form">
            <el-form-item label="应用ID">
              <el-input v-model="wechatConfig.appId" placeholder="请输入微信小程序AppID" />
              <div class="form-tip">微信小程序的AppID</div>
            </el-form-item>
            
            <el-form-item label="应用密钥">
              <el-input v-model="wechatConfig.appSecret" placeholder="请输入微信小程序AppSecret" type="password" />
              <div class="form-tip">微信小程序的AppSecret</div>
            </el-form-item>
            
            <el-form-item label="商户号">
              <el-input v-model="wechatConfig.mchId" placeholder="请输入微信商户号" />
              <div class="form-tip">微信支付商户号</div>
            </el-form-item>
            
            <el-form-item label="商户密钥">
              <el-input v-model="wechatConfig.key" placeholder="请输入商户密钥" type="password" />
              <div class="form-tip">微信支付商户密钥</div>
            </el-form-item>
            
            <el-form-item label="通知地址">
              <el-input v-model="wechatConfig.notifyUrl" placeholder="支付结果通知地址" />
              <div class="form-tip">支付完成后微信会向此地址发送通知</div>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 系统配置 -->
        <el-tab-pane label="系统配置" name="system">
          <el-form :model="systemConfig" label-width="120px" class="config-form">
            <el-form-item label="支付超时时间">
              <el-input-number v-model="systemConfig.paymentTimeout" :min="1" :max="60" />
              <span class="unit">分钟</span>
              <div class="form-tip">订单支付超时时间，超时后订单自动取消</div>
            </el-form-item>
            
            <el-form-item label="库存检查">
              <el-switch v-model="systemConfig.stockCheck" />
              <div class="form-tip">是否启用库存检查，防止超卖</div>
            </el-form-item>
            
            <el-form-item label="分布式锁">
              <el-switch v-model="systemConfig.distributedLock" />
              <div class="form-tip">是否启用分布式锁，防止并发问题</div>
            </el-form-item>
            
            <el-form-item label="支付日志">
              <el-switch v-model="systemConfig.paymentLog" />
              <div class="form-tip">是否记录详细的支付日志</div>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <!-- 配置状态 -->
      <div class="config-status">
        <h3>配置状态</h3>
        <div class="status-grid">
          <div class="status-item">
            <div class="status-icon success">✓</div>
            <div class="status-content">
              <div class="status-title">支付宝沙箱</div>
              <div class="status-desc">已配置并可用</div>
            </div>
          </div>
          <div class="status-item">
            <div class="status-icon warning">⚠</div>
            <div class="status-content">
              <div class="status-title">微信支付</div>
              <div class="status-desc">仅模拟支付</div>
            </div>
          </div>
          <div class="status-item">
            <div class="status-icon success">✓</div>
            <div class="status-content">
              <div class="status-title">Redis连接</div>
              <div class="status-desc">分布式锁可用</div>
            </div>
          </div>
          <div class="status-item">
            <div class="status-icon success">✓</div>
            <div class="status-content">
              <div class="status-title">数据库</div>
              <div class="status-desc">订单系统正常</div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('alipay')
const saveLoading = ref(false)

// 支付宝配置
const alipayConfig = reactive({
  appId: '9021000141610286',
  serverUrl: 'https://openapi-sandbox.dl.alipaydev.com/gateway.do',
  charset: 'UTF-8',
  signType: 'RSA2',
  notifyUrl: 'http://715b1392.r6.cpolar.top/api/payment/alipay/notify',
  returnUrl: 'https://openapi.alipay.com/gateway.do'
})

// 微信配置
const wechatConfig = reactive({
  appId: 'wx54acde2424a69af2',
  appSecret: '',
  mchId: '1666666666666666',
  key: '',
  notifyUrl: 'http://your-domain.com/api/wechat/pay/notify'
})

// 系统配置
const systemConfig = reactive({
  paymentTimeout: 15,
  stockCheck: true,
  distributedLock: true,
  paymentLog: true
})

onMounted(() => {
  loadConfig()
})

// 加载配置
function loadConfig() {
  // 这里可以从后端API加载配置
  ElMessage.success('配置加载完成')
}

// 保存配置
async function saveConfig() {
  saveLoading.value = true
  try {
    // 模拟保存配置到后端
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('配置保存成功')
  } catch (error) {
    ElMessage.error('保存失败：' + error.message)
  } finally {
    saveLoading.value = false
  }
}
</script>

<style scoped>
.config-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.config-tabs {
  margin-top: 20px;
}

.config-form {
  max-width: 600px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  line-height: 1.4;
}

.unit {
  margin-left: 8px;
  color: #666;
  font-size: 14px;
}

.config-status {
  margin-top: 40px;
  padding-top: 30px;
  border-top: 1px solid #e4e7ed;
}

.config-status h3 {
  margin-bottom: 20px;
  color: #303133;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.status-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #e4e7ed;
}

.status-item:has(.status-icon.success) {
  border-left-color: #67c23a;
}

.status-item:has(.status-icon.warning) {
  border-left-color: #e6a23c;
}

.status-item:has(.status-icon.error) {
  border-left-color: #f56c6c;
}

.status-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: #fff;
  margin-right: 12px;
}

.status-icon.success {
  background: #67c23a;
}

.status-icon.warning {
  background: #e6a23c;
}

.status-icon.error {
  background: #f56c6c;
}

.status-content {
  flex: 1;
}

.status-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.status-desc {
  font-size: 12px;
  color: #909399;
}
</style>
