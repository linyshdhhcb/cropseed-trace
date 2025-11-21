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
                <!-- 基本信息区域 -->
                <el-descriptions :column="2" size="small" border class="record-descriptions">
                  <el-descriptions-item label="批次ID" v-if="record.batchId">
                    {{ record.batchId }}
                  </el-descriptions-item>
                  <el-descriptions-item label="操作员">
                    {{ record.operatorName || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="联系电话" v-if="record.operatorPhone">
                    {{ record.operatorPhone }}
                  </el-descriptions-item>
                  <el-descriptions-item label="相关实体">
                    {{ record.entityName || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="位置信息" :span="2">
                    {{ record.location || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="温度" v-if="record.temperature !== null">
                    <span class="env-value">🌡️ {{ record.temperature }}℃</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="湿度" v-if="record.humidity !== null">
                    <span class="env-value">💧 {{ record.humidity }}%</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="数量" v-if="record.quantity !== null">
                    <span class="env-value">📦 {{ record.quantity }} {{ record.unit || '' }}</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="质量等级" v-if="record.qualityGrade">
                    <el-tag size="small">{{ record.qualityGrade }}</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="检测结果" :span="2" v-if="record.testResult">
                    {{ record.testResult }}
                  </el-descriptions-item>
                  <el-descriptions-item label="内容摘要" :span="2">
                    <div class="content-summary-text">{{ record.contentSummary || '-' }}</div>
                  </el-descriptions-item>
                </el-descriptions>

                <!-- 区块链信息 -->
                <div v-if="record.blockchainTxHash" class="blockchain-info">
                  <el-descriptions :column="1" size="small" border>
                    <el-descriptions-item label="交易哈希">
                      <div class="tx-hash">
                        <code>{{ formatTxHash(record.blockchainTxHash) }}</code>
                        <el-button 
                          type="primary" 
                          link
                          size="small" 
                          @click="copyTxHash(record.blockchainTxHash)"
                        >
                          复制
                        </el-button>
                      </div>
                    </el-descriptions-item>
                  </el-descriptions>
                </div>

                <!-- 操作按钮 -->
                <div class="record-actions">
                  <el-button type="primary" size="small" @click="viewDetail(record)">
                    查看完整详情
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

    formatTxHash(txHash) {
      if (!txHash) return '-'
      if (txHash.length > 20) {
        return `${txHash.substring(0, 10)}...${txHash.substring(txHash.length - 8)}`
      }
      return txHash
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
  padding: 0;
}

.record-descriptions {
  margin-bottom: 10px;
}

.record-descriptions :deep(.el-descriptions__label) {
  font-weight: 600;
  background-color: #fafafa;
}

.content-summary-text {
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-word;
}

.env-value {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #409eff;
  font-weight: 500;
}

.blockchain-info {
  margin-top: 10px;
  background: linear-gradient(135deg, #f0f9ff 0%, #ffffff 100%);
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #e6f7ff;
}

.tx-hash {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tx-hash code {
  font-family: 'Courier New', Consolas, monospace;
  font-size: 12px;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
  color: #67c23a;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
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
