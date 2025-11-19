<template>
  <div class="trace-record-detail">
    <div v-if="record" class="detail-content">
      <!-- 基础信息 -->
      <div class="info-section">
        <h3>基础信息</h3>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item">
              <label>溯源码:</label>
              <span class="trace-code">{{ record.traceCode }}</span>
              <el-button 
                type="text" 
                size="small" 
                @click="copyToClipboard(record.traceCode)"
                style="margin-left: 10px;"
              >
                复制
              </el-button>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>批次ID:</label>
              <span>{{ record.batchId }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>记录类型:</label>
              <el-tag :type="getRecordTypeColor(record.recordType)">
                {{ getRecordTypeText(record.recordType) }}
              </el-tag>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 15px;">
          <el-col :span="8">
            <div class="info-item">
              <label>记录阶段:</label>
              <span>{{ record.recordStage || '-' }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>相关实体:</label>
              <span>{{ record.entityName || '-' }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>记录时间:</label>
              <span>{{ formatDateTime(record.recordTime) }}</span>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 操作人员信息 -->
      <div class="info-section">
        <h3>操作人员</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="info-item">
              <label>操作员:</label>
              <span>{{ record.operatorName || '-' }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>联系电话:</label>
              <span>{{ record.operatorPhone || '-' }}</span>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 位置和环境信息 -->
      <div class="info-section">
        <h3>位置和环境</h3>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="info-item">
              <label>位置信息:</label>
              <span>{{ record.location || '-' }}</span>
            </div>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 15px;">
          <el-col :span="8">
            <div class="info-item">
              <label>温度:</label>
              <span>{{ record.temperature ? record.temperature + '℃' : '-' }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>湿度:</label>
              <span>{{ record.humidity ? record.humidity + '%' : '-' }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>质量等级:</label>
              <span>{{ record.qualityGrade || '-' }}</span>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 数量和检测信息 -->
      <div class="info-section">
        <h3>数量和检测</h3>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item">
              <label>数量:</label>
              <span>{{ record.quantity ? record.quantity + ' ' + (record.unit || '') : '-' }}</span>
            </div>
          </el-col>
          <el-col :span="16">
            <div class="info-item">
              <label>检测结果:</label>
              <span>{{ record.testResult || '-' }}</span>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 内容信息 -->
      <div class="info-section">
        <h3>内容信息</h3>
        <div class="info-item full-width">
          <label>内容摘要:</label>
          <p class="content-text">{{ record.contentSummary || '-' }}</p>
        </div>
        
        <div v-if="record.detailedContent" class="info-item full-width">
          <label>详细内容:</label>
          <div class="detailed-content">
            <el-input 
              :value="record.detailedContent" 
              type="textarea" 
              :rows="4" 
              readonly
            />
          </div>
        </div>

        <div v-if="record.remark" class="info-item full-width">
          <label>备注:</label>
          <p class="content-text">{{ record.remark }}</p>
        </div>
      </div>

      <!-- 区块链信息 -->
      <div class="info-section">
        <h3>区块链信息</h3>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item">
              <label>上链状态:</label>
              <el-tag :type="getBlockchainStatusType(record.blockchainStatus)">
                {{ getBlockchainStatusText(record.blockchainStatus) }}
              </el-tag>
            </div>
          </el-col>
          <el-col :span="16">
            <div class="info-item">
              <label>交易哈希:</label>
              <span v-if="record.blockchainTxHash" class="tx-hash">
                {{ record.blockchainTxHash }}
                <el-button 
                  type="text" 
                  size="small" 
                  @click="copyToClipboard(record.blockchainTxHash)"
                >
                  复制
                </el-button>
              </span>
              <span v-else>-</span>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 系统信息 -->
      <div class="info-section">
        <h3>系统信息</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="info-item">
              <label>创建时间:</label>
              <span>{{ formatDateTime(record.createTime) }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>更新时间:</label>
              <span>{{ formatDateTime(record.updateTime) }}</span>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button type="primary" @click="viewTraceChain">查看溯源链</el-button>
        <el-button type="success" @click="verifyIntegrity" :loading="verifying">验证完整性</el-button>
        <el-button type="info" @click="getProof" :loading="gettingProof">获取证明</el-button>
        <el-button v-if="record.blockchainStatus !== 2" type="warning" @click="uploadToBlockchain">
          上链
        </el-button>
      </div>
    </div>

    <div v-else class="no-data">
      <el-empty description="暂无记录详情" />
    </div>

    <!-- 区块链证明对话框 -->
    <el-dialog title="区块链证明" v-model="showProofDialog" width="600px">
      <div v-if="proofData" class="proof-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="溯源码">{{ proofData.traceCode }}</el-descriptions-item>
          <el-descriptions-item label="交易哈希">{{ proofData.txHash }}</el-descriptions-item>
          <el-descriptions-item label="区块高度">{{ proofData.blockHeight }}</el-descriptions-item>
          <el-descriptions-item label="验证状态">
            <el-tag :type="proofData.verified ? 'success' : 'danger'">
              {{ proofData.verified ? '已验证' : '验证失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="生成时间" :span="2">
            {{ formatDateTime(proofData.timestamp) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { verifyTraceIntegrity, getTraceProof } from '@/api/trace'
import { ElMessage } from 'element-plus'

export default {
  name: 'TraceRecordDetail',
  props: {
    record: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      verifying: false,
      gettingProof: false,
      showProofDialog: false,
      proofData: null
    }
  },
  methods: {
    async verifyIntegrity() {
      if (!this.record || !this.record.traceCode) {
        ElMessage.error('溯源码不存在')
        return
      }

      this.verifying = true
      try {
        const response = await verifyTraceIntegrity(this.record.traceCode)
        if (response.code === 200) {
          if (response.data) {
            ElMessage.success('数据验证通过，完整性良好')
          } else {
            ElMessage.warning('数据验证失败，可能存在篡改')
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

    async getProof() {
      if (!this.record || !this.record.traceCode) {
        ElMessage.error('溯源码不存在')
        return
      }

      this.gettingProof = true
      try {
        const response = await getTraceProof(this.record.traceCode)
        if (response.code === 200) {
          this.proofData = response.data
          this.showProofDialog = true
        } else {
          ElMessage.error(response.message || '获取证明失败')
        }
      } catch (error) {
        console.error('获取证明失败', error)
        ElMessage.error('获取证明失败')
      } finally {
        this.gettingProof = false
      }
    },

    viewTraceChain() {
      if (this.record && this.record.traceCode) {
        this.$router.push(`/trace/chain/${this.record.traceCode}`)
      }
    },

    uploadToBlockchain() {
      ElMessage.info('上链功能开发中...')
    },

    copyToClipboard(text) {
      try {
        navigator.clipboard.writeText(text)
        ElMessage.success('复制成功')
      } catch (error) {
        const textarea = document.createElement('textarea')
        textarea.value = text
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
        ElMessage.success('复制成功')
      }
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
.trace-record-detail {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.info-section {
  margin-bottom: 30px;
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}

.info-section h3 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 2px solid #409eff;
  padding-bottom: 8px;
}

.info-item {
  margin-bottom: 15px;
  display: flex;
  align-items: flex-start;
}

.info-item label {
  font-weight: 600;
  color: #606266;
  min-width: 120px;
  margin-right: 10px;
}

.info-item span {
  color: #303133;
}

.info-item.full-width {
  flex-direction: column;
}

.info-item.full-width label {
  margin-bottom: 8px;
}

.trace-code {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409eff;
}

.tx-hash {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  word-break: break-all;
}

.content-text {
  margin: 0;
  line-height: 1.6;
  white-space: pre-wrap;
}

.detailed-content {
  width: 100%;
}

.actions {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.actions .el-button {
  margin: 0 10px;
}

.no-data {
  text-align: center;
  padding: 40px;
}

.proof-content {
  padding: 20px;
}

@media (max-width: 768px) {
  .trace-record-detail {
    padding: 10px;
  }
  
  .info-section {
    padding: 15px;
  }
  
  .actions .el-button {
    margin: 5px;
  }
}
</style>
