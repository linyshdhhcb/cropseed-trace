<template>
  <div class="trace-chain-view">
    <div v-if="traceCode" class="chain-header">
      <h3>溯源链: {{ traceCode }}</h3>
      <div class="chain-summary">
        <el-tag type="info">共 {{ chainData.length }} 条记录</el-tag>
        <el-tag type="success" style="margin-left: 10px;">
          已上链 {{ blockchainCount }} 条
        </el-tag>
      </div>
    </div>

    <div v-loading="loading" class="chain-content">
      <div v-if="chainData.length > 0" class="timeline-container">
        <!-- 时间线视图 -->
        <el-timeline>
          <el-timeline-item
            v-for="(record, index) in sortedChainData"
            :key="record.id"
            :timestamp="formatDateTime(record.recordTime)"
            placement="top"
            :type="getTimelineType(record.recordType)"
            :icon="getTimelineIcon(record.recordType)"
            size="large"
          >
            <el-card class="record-card">
              <div class="record-header">
                <div class="record-title">
                  <h4>{{ record.recordStage }}</h4>
                  <el-tag :type="getRecordTypeColor(record.recordType)" size="small">
                    {{ getRecordTypeText(record.recordType) }}
                  </el-tag>
                </div>
                <div class="record-status">
                  <el-tag :type="getBlockchainStatusType(record.blockchainStatus)" size="small">
                    {{ getBlockchainStatusText(record.blockchainStatus) }}
                  </el-tag>
                </div>
              </div>

              <div class="record-info">
                <el-row :gutter="15">
                  <el-col :span="12">
                    <div class="info-item">
                      <span class="label">操作员:</span>
                      <span class="value">{{ record.operatorName }}</span>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div class="info-item">
                      <span class="label">实体:</span>
                      <span class="value">{{ record.entityName || '-' }}</span>
                    </div>
                  </el-col>
                  <el-col :span="24">
                    <div class="info-item">
                      <span class="label">位置:</span>
                      <span class="value">{{ record.location || '-' }}</span>
                    </div>
                  </el-col>
                  <el-col :span="24">
                    <div class="info-item content-summary">
                      <span class="label">内容摘要:</span>
                      <p class="value">{{ record.contentSummary }}</p>
                    </div>
                  </el-col>
                </el-row>

                <!-- 环境和数量信息 -->
                <div v-if="hasEnvironmentData(record)" class="environment-info">
                  <el-row :gutter="15">
                    <el-col v-if="record.temperature !== null" :span="8">
                      <div class="env-item">
                        <span class="icon">🌡️</span>
                        <span>{{ record.temperature }}℃</span>
                      </div>
                    </el-col>
                    <el-col v-if="record.humidity !== null" :span="8">
                      <div class="env-item">
                        <span class="icon">💧</span>
                        <span>{{ record.humidity }}%</span>
                      </div>
                    </el-col>
                    <el-col v-if="record.quantity !== null" :span="8">
                      <div class="env-item">
                        <span class="icon">📦</span>
                        <span>{{ record.quantity }} {{ record.unit }}</span>
                      </div>
                    </el-col>
                  </el-row>
                </div>

                <!-- 操作按钮 -->
                <div class="record-actions">
                  <el-button type="text" size="small" @click="viewDetail(record)">
                    查看详情
                  </el-button>
                  <el-button 
                    v-if="record.blockchainTxHash" 
                    type="text" 
                    size="small" 
                    @click="copyTxHash(record.blockchainTxHash)"
                  >
                    复制交易哈希
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>

      <div v-else-if="!loading" class="no-data">
        <el-empty description="暂无溯源记录" />
      </div>
    </div>

    <!-- 操作工具栏 -->
    <div v-if="chainData.length > 0" class="chain-toolbar">
      <el-button type="primary" @click="verifyChain" :loading="verifying">
        验证完整性
      </el-button>
      <el-button type="success" @click="exportChain">
        导出溯源链
      </el-button>
      <el-button type="info" @click="generateReport">
        生成报告
      </el-button>
    </div>

    <!-- 记录详情对话框 -->
    <el-dialog title="记录详情" v-model="showDetailDialog" width="900px">
      <TraceRecordDetail :record="currentRecord" />
    </el-dialog>
  </div>
</template>

<script>
// 暂时不使用图标，避免导入错误
// import { DataLine, Cloud, Package } from '@element-plus/icons-vue'
import { getTraceChain, verifyTraceIntegrity } from '@/api/trace'
import { ElMessage } from 'element-plus'
import TraceRecordDetail from './TraceRecordDetail.vue'

export default {
  name: 'TraceChainView',
  components: {
    TraceRecordDetail
  },
  props: {
    traceCode: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      loading: false,
      verifying: false,
      chainData: [],
      showDetailDialog: false,
      currentRecord: null
    }
  },
  computed: {
    sortedChainData() {
      return [...this.chainData].sort((a, b) => 
        new Date(a.recordTime) - new Date(b.recordTime)
      )
    },
    blockchainCount() {
      return this.chainData.filter(record => record.blockchainStatus === 2).length
    }
  },
  watch: {
    traceCode: {
      immediate: true,
      handler(newCode) {
        if (newCode) {
          this.loadTraceChain()
        }
      }
    }
  },
  methods: {
    async loadTraceChain() {
      if (!this.traceCode) return

      this.loading = true
      try {
        const response = await getTraceChain(this.traceCode)
        if (response.code === 200) {
          this.chainData = response.data || []
        } else {
          ElMessage.error(response.message || '加载溯源链失败')
          this.chainData = []
        }
      } catch (error) {
        console.error('加载溯源链失败', error)
        ElMessage.error('加载溯源链失败')
        this.chainData = []
      } finally {
        this.loading = false
      }
    },

    async verifyChain() {
      if (!this.traceCode) return

      this.verifying = true
      try {
        const response = await verifyTraceIntegrity(this.traceCode)
        if (response.code === 200) {
          if (response.data) {
            ElMessage.success('溯源链验证通过，数据完整性良好')
          } else {
            ElMessage.warning('溯源链验证失败，数据可能被篡改')
          }
        } else {
          ElMessage.error(response.message || '验证失败')
        }
      } catch (error) {
        console.error('验证失败', error)
        ElMessage.error('验证失败')
      } finally {
        this.verifying = false
      }
    },

    exportChain() {
      // 导出溯源链数据为CSV
      const headers = [
        'ID', '记录阶段', '记录类型', '操作员', '记录时间', 
        '位置', '温度', '湿度', '数量', '单位', '内容摘要', '上链状态'
      ]
      
      const csvData = this.sortedChainData.map(record => [
        record.id,
        record.recordStage,
        this.getRecordTypeText(record.recordType),
        record.operatorName,
        this.formatDateTime(record.recordTime),
        record.location || '',
        record.temperature || '',
        record.humidity || '',
        record.quantity || '',
        record.unit || '',
        record.contentSummary,
        this.getBlockchainStatusText(record.blockchainStatus)
      ])

      const csvContent = 'data:text/csv;charset=utf-8,\uFEFF' + 
        [headers, ...csvData].map(row => row.join(',')).join('\n')
      
      const encodedUri = encodeURI(csvContent)
      const link = document.createElement('a')
      link.setAttribute('href', encodedUri)
      link.setAttribute('download', `溯源链_${this.traceCode}_${new Date().toISOString().slice(0, 10)}.csv`)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      ElMessage.success('导出成功')
    },

    generateReport() {
      ElMessage.info('报告生成功能开发中...')
    },

    viewDetail(record) {
      this.currentRecord = record
      this.showDetailDialog = true
    },

    copyTxHash(txHash) {
      try {
        navigator.clipboard.writeText(txHash)
        ElMessage.success('交易哈希复制成功')
      } catch (error) {
        const textarea = document.createElement('textarea')
        textarea.value = txHash
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
        ElMessage.success('交易哈希复制成功')
      }
    },

    hasEnvironmentData(record) {
      return record.temperature !== null || 
             record.humidity !== null || 
             record.quantity !== null
    },

    getTimelineType(recordType) {
      const typeMap = {
        1: 'success', // 生产记录
        2: 'warning', // 质检记录
        3: 'info',    // 流通记录
        4: 'primary', // 销售记录
        5: 'danger'   // 异常记录
      }
      return typeMap[recordType] || 'info'
    },

    getTimelineIcon(recordType) {
      // 可以根据记录类型返回不同的图标
      return undefined
    },

    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      return new Date(dateTime).toLocaleString('zh-CN')
    },

    getRecordTypeText(type) {
      const typeMap = {
        1: '生产记录',
        2: '质检记录',
        3: '流通记录',
        4: '销售记录', 
        5: '异常记录'
      }
      return typeMap[type] || '未知'
    },

    getRecordTypeColor(type) {
      const colorMap = {
        1: 'success',
        2: 'warning',
        3: 'info',
        4: 'primary',
        5: 'danger'
      }
      return colorMap[type] || ''
    },

    getBlockchainStatusType(status) {
      const statusMap = {
        0: 'info',    // 未上链
        1: 'warning', // 上链中
        2: 'success', // 上链成功
        3: 'danger'   // 上链失败
      }
      return statusMap[status] || 'info'
    },

    getBlockchainStatusText(status) {
      const statusMap = {
        0: '未上链',
        1: '上链中', 
        2: '已上链',
        3: '上链失败'
      }
      return statusMap[status] || '未知'
    }
  }
}
</script>

<style scoped>
.trace-chain-view {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.chain-header {
  margin-bottom: 20px;
  text-align: center;
}

.chain-header h3 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 20px;
}

.chain-summary {
  margin-bottom: 20px;
}

.timeline-container {
  margin: 20px 0;
}

.record-card {
  margin-left: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e4e7ed;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.record-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.record-title h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.record-info {
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
}

.info-item .label {
  font-weight: 600;
  color: #606266;
  min-width: 80px;
  margin-right: 10px;
}

.info-item .value {
  color: #303133;
  flex: 1;
}

.info-item.content-summary .value {
  margin: 0;
  line-height: 1.5;
}

.environment-info {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 6px;
  margin: 15px 0;
}

.env-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
  font-size: 14px;
}

.env-item .icon {
  font-size: 16px;
  margin-right: 3px;
}

.record-actions {
  text-align: right;
  border-top: 1px solid #f0f0f0;
  padding-top: 10px;
}

.chain-toolbar {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.chain-toolbar .el-button {
  margin: 0 10px;
}

.no-data {
  text-align: center;
  padding: 40px;
}

@media (max-width: 768px) {
  .trace-chain-view {
    padding: 10px;
  }
  
  .record-card {
    margin-left: 10px;
  }
  
  .record-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .chain-toolbar .el-button {
    margin: 5px;
    display: block;
    width: 100%;
  }
}
</style>
