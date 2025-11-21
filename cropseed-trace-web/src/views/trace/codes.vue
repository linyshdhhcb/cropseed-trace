<template>
  <div class="trace-codes">
    <div class="page-header">
      <h2>溯源码管理</h2>
      <el-button type="primary" @click="showGeneratorDialog = true">
        <el-icon><Plus /></el-icon>
        生成溯源码
      </el-button>
    </div>

    <!-- 功能卡片 -->
    <div class="function-cards">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="function-card" @click="showGeneratorDialog = true">
            <div class="card-content">
              <div class="card-icon generate">
                <el-icon><Postcard /></el-icon>
              </div>
              <div class="card-info">
                <h3>生成溯源码</h3>
                <p>为新批次生成唯一溯源码</p>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card class="function-card">
            <div class="card-content">
              <div class="card-icon validate">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="card-info">
                <h3>验证溯源码</h3>
                <p>检查溯源码格式和有效性</p>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card class="function-card">
            <div class="card-content">
              <div class="card-icon parse">
                <el-icon><View /></el-icon>
              </div>
              <div class="card-info">
                <h3>解析溯源码</h3>
                <p>查看溯源码详细信息</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 溯源码验证工具 -->
    <div class="validation-section">
      <el-card>
        <template #header>
          <h3>溯源码工具</h3>
        </template>

        <el-tabs v-model="activeTab">
          <el-tab-pane label="验证溯源码" name="validate">
            <div class="tool-content">
              <el-form :inline="true">
                <el-form-item label="溯源码">
                  <el-input 
                    v-model="validateForm.traceCode"
                    placeholder="请输入溯源码"
                    style="width: 300px;"
                    @input="handleTraceCodeInput"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button 
                    type="primary" 
                    @click="validateCode" 
                    :loading="validating"
                  >
                    验证
                  </el-button>
                </el-form-item>
              </el-form>

              <div v-if="validateResult !== null" class="validation-result">
                <el-alert
                  :title="validateResult ? '溯源码格式正确' : '溯源码格式错误'"
                  :type="validateResult ? 'success' : 'error'"
                  show-icon
                  :closable="false"
                />
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="解析溯源码" name="parse">
            <div class="tool-content">
              <el-form :inline="true">
                <el-form-item label="溯源码">
                  <el-input 
                    v-model="parseForm.traceCode"
                    placeholder="请输入溯源码"
                    style="width: 300px;"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button 
                    type="primary" 
                    @click="parseCode" 
                    :loading="parsing"
                  >
                    解析
                  </el-button>
                </el-form-item>
              </el-form>

              <div v-if="parseResult" class="parse-result">
                <el-descriptions title="解析结果" :column="2" border>
                  <el-descriptions-item label="溯源码">{{ parseResult.traceCode }}</el-descriptions-item>
                  <el-descriptions-item label="地区代码">{{ parseResult.regionCode }}</el-descriptions-item>
                  <el-descriptions-item label="地区名称">{{ parseResult.regionName }}</el-descriptions-item>
                  <el-descriptions-item label="年份">{{ parseResult.year }}</el-descriptions-item>
                  <el-descriptions-item label="序号">{{ parseResult.number }}</el-descriptions-item>
                  <el-descriptions-item label="格式有效">
                    <el-tag :type="parseResult.valid ? 'success' : 'danger'">
                      {{ parseResult.valid ? '是' : '否' }}
                    </el-tag>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="检查使用状态" name="check">
            <div class="tool-content">
              <el-form :inline="true">
                <el-form-item label="溯源码">
                  <el-input 
                    v-model="checkForm.traceCode"
                    placeholder="请输入溯源码"
                    style="width: 300px;"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button 
                    type="primary" 
                    @click="checkUsage" 
                    :loading="checking"
                  >
                    检查
                  </el-button>
                </el-form-item>
              </el-form>

              <div v-if="checkResult" class="check-result">
                <el-descriptions title="使用状态" :column="2" border>
                  <el-descriptions-item label="溯源码">{{ checkResult.traceCode }}</el-descriptions-item>
                  <el-descriptions-item label="格式有效">
                    <el-tag :type="checkResult.valid ? 'success' : 'danger'">
                      {{ checkResult.valid ? '是' : '否' }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="使用状态">
                    <el-tag :type="checkResult.used ? 'danger' : 'success'">
                      {{ checkResult.status }}
                    </el-tag>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="生成二维码" name="qrcode">
            <div class="tool-content">
              <el-form :inline="true">
                <el-form-item label="溯源码">
                  <el-input 
                    v-model="qrcodeForm.traceCode"
                    placeholder="请输入溯源码"
                    style="width: 300px;"
                  />
                </el-form-item>
                <el-form-item label="批次号">
                  <el-input 
                    v-model="qrcodeForm.batchNo"
                    placeholder="可选"
                    style="width: 200px;"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button 
                    type="primary" 
                    @click="generateQRCode" 
                    :disabled="!qrcodeForm.traceCode"
                  >
                    <el-icon><Picture /></el-icon>
                    生成二维码
                  </el-button>
                </el-form-item>
              </el-form>

              <div v-if="qrcodeForm.traceCode" class="qrcode-preview">
                <el-alert
                  title="二维码预览"
                  type="success"
                  :closable="false"
                  show-icon
                >
                  <template #default>
                    <p>点击"生成二维码"按钮查看完整的二维码预览和下载选项</p>
                  </template>
                </el-alert>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <!-- 地区配置管理 -->
    <div class="region-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <h3>支持地区配置</h3>
            <el-button type="primary" size="small" @click="loadRegions">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>

        <div class="regions-grid">
          <el-row :gutter="20">
            <el-col 
              v-for="(name, code) in regions" 
              :key="code" 
              :xs="24" :sm="12" :md="8" :lg="6"
            >
              <div class="region-item">
                <div class="region-code">{{ code }}</div>
                <div class="region-name">{{ name }}</div>
                <el-button 
                  type="text" 
                  size="small" 
                  @click="generateForRegion(code)"
                >
                  生成溯源码
                </el-button>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>

    <!-- 溯源码生成器对话框 -->
    <el-dialog 
      title="溯源码生成器" 
      v-model="showGeneratorDialog" 
      width="500px"
      :close-on-click-modal="false"
    >
      <TraceCodeGenerator @generated="handleCodeGenerated" />
    </el-dialog>

    <!-- 二维码生成对话框 -->
    <TraceQRCode 
      v-model="showQRCodeDialog"
      :trace-code="qrcodeForm.traceCode"
      :batch-no="qrcodeForm.batchNo"
      :product-name="qrcodeForm.productName"
    />
  </div>
</template>

<script>
import { Plus, Postcard, CircleCheck, View, Refresh, Picture } from '@element-plus/icons-vue'
import { 
  validateTraceCode, 
  parseTraceCode, 
  checkTraceCodeUsage,
  getSupportedRegions,
  generateTraceCode 
} from '@/api/trace'
import { ElMessage } from 'element-plus'
import TraceCodeGenerator from './components/TraceCodeGenerator.vue'
import TraceQRCode from './components/TraceQRCode.vue'

export default {
  name: 'TraceCodes',
  components: {
    Plus,
    Postcard,
    CircleCheck,
    View,
    Refresh,
    Picture,
    TraceCodeGenerator,
    TraceQRCode
  },
  data() {
    return {
      activeTab: 'validate',
      showGeneratorDialog: false,
      
      // 验证表单
      validateForm: {
        traceCode: ''
      },
      validating: false,
      validateResult: null,

      // 解析表单
      parseForm: {
        traceCode: ''
      },
      parsing: false,
      parseResult: null,

      // 检查表单
      checkForm: {
        traceCode: ''
      },
      checking: false,
      checkResult: null,

      // 二维码表单
      qrcodeForm: {
        traceCode: '',
        batchNo: '',
        productName: ''
      },
      showQRCodeDialog: false,

      // 地区数据
      regions: {}
    }
  },
  mounted() {
    this.loadRegions()
  },
  methods: {
    async loadRegions() {
      try {
        const response = await getSupportedRegions()
        if (response.code === 200) {
          this.regions = response.data || {}
        }
      } catch (error) {
        console.error('加载地区列表失败', error)
        // 使用默认地区列表
        this.regions = {
          'BJ': '北京市',
          'SH': '上海市',
          'GZ': '广州市',
          'SZ': '深圳市',
          'HZ': '杭州市',
          'CD': '成都市',
          'WH': '武汉市',
          'XA': '西安市'
        }
      }
    },

    async validateCode() {
      if (!this.validateForm.traceCode) {
        ElMessage.error('请输入溯源码')
        return
      }

      this.validating = true
      try {
        const response = await validateTraceCode(this.validateForm.traceCode)
        if (response.code === 200) {
          this.validateResult = response.data
        } else {
          ElMessage.error(response.message || '验证失败')
        }
      } catch (error) {
        console.error('验证失败', error)
        ElMessage.error('验证失败')
      } finally {
        this.validating = false
      }
    },

    async parseCode() {
      if (!this.parseForm.traceCode) {
        ElMessage.error('请输入溯源码')
        return
      }

      this.parsing = true
      try {
        const response = await parseTraceCode(this.parseForm.traceCode)
        if (response.code === 200) {
          this.parseResult = response.data
        } else {
          ElMessage.error(response.message || '解析失败')
        }
      } catch (error) {
        console.error('解析失败', error)
        ElMessage.error('解析失败')
      } finally {
        this.parsing = false
      }
    },

    async checkUsage() {
      if (!this.checkForm.traceCode) {
        ElMessage.error('请输入溯源码')
        return
      }

      this.checking = true
      try {
        const response = await checkTraceCodeUsage(this.checkForm.traceCode)
        if (response.code === 200) {
          this.checkResult = response.data
        } else {
          ElMessage.error(response.message || '检查失败')
        }
      } catch (error) {
        console.error('检查失败', error)
        ElMessage.error('检查失败')
      } finally {
        this.checking = false
      }
    },

    async generateForRegion(regionCode) {
      try {
        const response = await generateTraceCode(regionCode)
        if (response.code === 200) {
          ElMessage.success(`生成成功: ${response.data}`)
          // 可以将生成的溯源码复制到剪贴板
          this.copyToClipboard(response.data)
        } else {
          ElMessage.error(response.message || '生成失败')
        }
      } catch (error) {
        console.error('生成失败', error)
        ElMessage.error('生成失败')
      }
    },

    handleTraceCodeInput() {
      // 清除之前的验证结果
      this.validateResult = null
    },

    handleCodeGenerated(traceCode) {
      this.showGeneratorDialog = false
      ElMessage.success(`生成溯源码: ${traceCode}`)
      this.copyToClipboard(traceCode)
      // 自动填充到二维码表单
      this.qrcodeForm.traceCode = traceCode
      this.activeTab = 'qrcode'
    },

    // 生成二维码
    generateQRCode() {
      if (!this.qrcodeForm.traceCode) {
        ElMessage.error('请输入溯源码')
        return
      }
      this.showQRCodeDialog = true
    },

    copyToClipboard(text) {
      try {
        navigator.clipboard.writeText(text)
        ElMessage.success('已复制到剪贴板')
      } catch (error) {
        const textarea = document.createElement('textarea')
        textarea.value = text
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
        ElMessage.success('已复制到剪贴板')
      }
    }
  }
}
</script>

<style scoped>
.trace-codes {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.function-cards {
  margin-bottom: 30px;
}

.function-card {
  cursor: pointer;
  transition: all 0.3s ease;
  height: 120px;
}

.function-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.card-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.card-icon.generate {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-icon.validate {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.card-icon.parse {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.card-info h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #303133;
}

.card-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.validation-section,
.region-section {
  margin-bottom: 30px;
}

.tool-content {
  padding: 20px 0;
}

.validation-result,
.parse-result,
.check-result {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #303133;
}

.regions-grid {
  padding: 20px 0;
}

.region-item {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.region-item:hover {
  background: #e8f4fd;
  transform: translateY(-2px);
}

.region-code {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.region-name {
  color: #606266;
  margin-bottom: 10px;
  font-size: 14px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .function-card {
    margin-bottom: 15px;
  }
  
  .card-content {
    flex-direction: column;
    text-align: center;
  }
  
  .card-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>
