<template>
  <div class="trace-record-detail">
    <div v-if="record" class="detail-content">
      <!-- 基础信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">基础信息</span>
          </div>
        </template>
        <el-descriptions :column="3" border size="default">
          <el-descriptions-item label="溯源码" label-class-name="label-bold">
            <span class="trace-code">{{ record.traceCode }}</span>
            <el-button 
              type="primary" 
              link
              size="small" 
              @click="copyToClipboard(record.traceCode)"
              style="margin-left: 10px;"
            >
              复制
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="批次ID" label-class-name="label-bold">
            {{ record.batchId }}
          </el-descriptions-item>
          <el-descriptions-item label="记录类型" label-class-name="label-bold">
            <el-tag :type="getRecordTypeColor(record.recordType)">
              {{ getRecordTypeText(record.recordType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="记录阶段" label-class-name="label-bold">
            {{ record.recordStage || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="相关实体" label-class-name="label-bold">
            {{ record.entityName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="记录时间" label-class-name="label-bold">
            {{ formatDateTime(record.recordTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 操作人员信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">操作人员</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="操作员" label-class-name="label-bold">
            {{ record.operatorName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话" label-class-name="label-bold">
            {{ record.operatorPhone || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 位置和环境信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">位置和环境</span>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="位置信息" label-class-name="label-bold" :span="3">
            {{ record.location || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="温度" label-class-name="label-bold">
            {{ record.temperature ? record.temperature + '℃' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="湿度" label-class-name="label-bold">
            {{ record.humidity ? record.humidity + '%' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="质量等级" label-class-name="label-bold">
            {{ record.qualityGrade || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 数量和检测信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">数量和检测</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="数量" label-class-name="label-bold">
            {{ record.quantity ? record.quantity + ' ' + (record.unit || '') : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="检测结果" label-class-name="label-bold">
            {{ record.testResult || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 内容信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">内容信息</span>
          </div>
        </template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="内容摘要" label-class-name="label-bold">
            <div class="content-text">{{ record.contentSummary || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item v-if="record.detailedContent" label="详细内容(JSON)" label-class-name="label-bold">
            <div class="json-viewer">
              <pre class="json-content">{{ formatJSON(record.detailedContent) }}</pre>
              <el-button 
                type="primary" 
                link
                size="small" 
                @click="copyToClipboard(record.detailedContent)"
                class="copy-json-btn"
              >
                复制JSON
              </el-button>
            </div>
          </el-descriptions-item>
          <el-descriptions-item v-if="record.remark" label="备注" label-class-name="label-bold">
            <div class="content-text">{{ record.remark }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 区块链信息 -->
      <el-card class="info-card blockchain-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">区块链信息</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="上链状态" label-class-name="label-bold">
            <el-tag :type="getBlockchainStatusType(record.blockchainStatus)" size="large">
              {{ getBlockchainStatusText(record.blockchainStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="交易哈希" label-class-name="label-bold">
            <span v-if="record.blockchainTxHash" class="tx-hash">
              {{ record.blockchainTxHash }}
              <el-button 
                type="primary" 
                link
                size="small" 
                @click="copyToClipboard(record.blockchainTxHash)"
              >
                复制
              </el-button>
            </span>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 系统信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">系统信息</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="创建时间" label-class-name="label-bold">
            {{ formatDateTime(record.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间" label-class-name="label-bold">
            {{ formatDateTime(record.updateTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 操作按钮 -->
      <div class="actions">
        <!-- <el-button type="primary" @click="viewTraceChain">查看溯源链</el-button> -->
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
    <el-dialog title="区块链证明" v-model="showProofDialog" width="700px">
      <div v-if="proofData" class="proof-content">
        <el-alert
          title="区块链存证证明"
          type="success"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <template #default>
            该溯源记录已成功上链到区块链网络，数据真实可信，不可篡改。
          </template>
        </el-alert>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="溯源码" :span="2">
            <el-tag type="primary" size="large">{{ proofData.traceCode }}</el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item label="数据哈希" :span="2">
            <div class="hash-value">
              <code>{{ proofData.dataHash }}</code>
              <el-button 
                type="primary"
                link
                size="small" 
                @click="copyToClipboard(proofData.dataHash)"
                style="margin-left: 10px;"
              >
                复制
              </el-button>
            </div>
          </el-descriptions-item>

          <el-descriptions-item label="交易哈希" :span="2" v-if="proofData.txHash">
            <div class="hash-value">
              <code>{{ proofData.txHash }}</code>
              <el-button 
                type="primary"
                link
                size="small" 
                @click="copyToClipboard(proofData.txHash)"
                style="margin-left: 10px;"
              >
                复制
              </el-button>
            </div>
          </el-descriptions-item>

          <el-descriptions-item label="区块高度" v-if="proofData.blockHeight">
            {{ proofData.blockHeight }}
          </el-descriptions-item>
          
          <el-descriptions-item label="验证状态" :span="proofData.blockHeight ? 1 : 2">
            <el-tag :type="proofData.verified ? 'success' : 'danger'" size="large">
              {{ proofData.verified ? '验证通过' : '验证失败' }}
            </el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item label="证明生成时间" :span="2">
            {{ formatDateTime(proofData.timestamp) }}
          </el-descriptions-item>

          <el-descriptions-item label="区块链平台" :span="2">
            腾讯云TBaas - 长安链体验网络
          </el-descriptions-item>
        </el-descriptions>

        <div class="proof-qrcode">
          <div class="qrcode-label">扫码查看证明</div>
          <div class="qrcode-code">验证码：{{ proofData.traceCode }}</div>
        </div>

        <div class="proof-actions">
          <el-button type="primary" @click="downloadProof">
            下载证明
          </el-button>
          <el-button type="success" @click="copyProofText">
            复制证明
          </el-button>
          <el-button @click="showProofDialog = false">关闭</el-button>
        </div>
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

      // 检查上链状态
      if (this.record.blockchainStatus !== 2) {
        ElMessage.warning('该记录尚未成功上链，无法生成证明。当前状态：' + this.getBlockchainStatusText(this.record.blockchainStatus))
        return
      }

      // 检查是否有交易哈希
      if (!this.record.blockchainTxHash) {
        ElMessage.warning('该记录缺少交易哈希，无法生成证明')
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
        ElMessage.error('获取证明失败：' + (error.message || '网络错误'))
      } finally {
        this.gettingProof = false
      }
    },

    // viewTraceChain() {
    //   if (this.record && this.record.traceCode) {
    //     this.$router.push(`/trace/chain/${this.record.traceCode}`)
    //   }
    // },

    uploadToBlockchain() {
      ElMessage.info('上链功能开发中...')
    },

    downloadProof() {
      if (this.proofData) {
        const proofData = {
          溯源码: this.proofData.traceCode,
          数据哈希: this.proofData.dataHash,
          验证状态: this.proofData.verified ? '已验证' : '验证失败',
          证明生成时间: this.formatDateTime(this.proofData.timestamp),
          区块链平台: '腾讯云TBaas - 长安链体验网络'
        }

        // 添加可选字段
        if (this.proofData.txHash) {
          proofData.交易哈希 = this.proofData.txHash
        }
        if (this.proofData.blockHeight) {
          proofData.区块高度 = this.proofData.blockHeight
        }

        const jsonStr = JSON.stringify(proofData, null, 2)
        const blob = new Blob([jsonStr], { type: 'application/json' })
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `区块链证明_${this.proofData.traceCode}_${Date.now()}.json`
        link.click()
        URL.revokeObjectURL(url)
        
        ElMessage.success('证明已下载')
      }
    },

    copyProofText() {
      if (this.proofData) {
        let proofText = `=== 区块链存证证明 ===\n\n`
        proofText += `溯源码：${this.proofData.traceCode}\n`
        proofText += `数据哈希：${this.proofData.dataHash}\n`
        
        if (this.proofData.txHash) {
          proofText += `交易哈希：${this.proofData.txHash}\n`
        }
        if (this.proofData.blockHeight) {
          proofText += `区块高度：${this.proofData.blockHeight}\n`
        }
        
        proofText += `验证状态：${this.proofData.verified ? '已验证' : '验证失败'}\n`
        proofText += `证明生成时间：${this.formatDateTime(this.proofData.timestamp)}\n`
        proofText += `区块链平台：腾讯云TBaas - 长安链体验网络\n\n`
        proofText += `该溯源记录已成功上链，数据真实可信，不可篡改。`
        
        this.copyToClipboard(proofText)
      }
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

    formatJSON(jsonStr) {
      if (!jsonStr) return '-'
      try {
        // 尝试解析JSON字符串
        const jsonObj = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr
        // 格式化输出，缩进2个空格
        return JSON.stringify(jsonObj, null, 2)
      } catch (error) {
        // 如果不是有效的JSON，直接返回原字符串
        return jsonStr
      }
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
  max-height: 75vh;
  overflow-y: auto;
  padding: 0 5px;
}

.detail-content {
  padding: 10px 0;
}

.info-card {
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
}

.info-card:last-of-type {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.blockchain-card {
  border: 2px solid #e6f7ff;
  background: linear-gradient(135deg, #f0f9ff 0%, #ffffff 100%);
}

:deep(.label-bold) {
  font-weight: 600;
  color: #606266;
  background-color: #fafafa;
}

.trace-code {
  font-family: 'Courier New', Consolas, monospace;
  font-weight: bold;
  font-size: 14px;
  color: #409eff;
  background: #ecf5ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.tx-hash {
  font-family: 'Courier New', Consolas, monospace;
  font-size: 12px;
  word-break: break-all;
  color: #67c23a;
  background: #f0f9ff;
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-block;
  max-width: 500px;
}

.content-text {
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  color: #606266;
}

.json-viewer {
  position: relative;
  background: #282c34;
  border-radius: 6px;
  padding: 16px;
  overflow: hidden;
}

.json-content {
  margin: 0;
  padding: 0;
  font-family: 'Courier New', Consolas, Monaco, monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #abb2bf;
  background: transparent;
  overflow-x: auto;
  max-height: 400px;
  overflow-y: auto;
}

.json-content::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.json-content::-webkit-scrollbar-track {
  background: #21252b;
  border-radius: 4px;
}

.json-content::-webkit-scrollbar-thumb {
  background: #4b5263;
  border-radius: 4px;
}

.json-content::-webkit-scrollbar-thumb:hover {
  background: #5c6370;
}

.copy-json-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  color: #61afef;
}

.actions {
  text-align: center;
  margin-top: 30px;
  padding: 20px 0;
  border-top: 2px solid #f0f0f0;
}

.actions .el-button {
  margin: 0 8px;
  min-width: 120px;
}

.no-data {
  text-align: center;
  padding: 60px 20px;
}

.proof-content {
  padding: 0;
}

.hash-value {
  display: flex;
  align-items: center;
  gap: 10px;
}

.hash-value code {
  font-family: 'Courier New', Consolas, Monaco, monospace;
  font-size: 12px;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
  word-break: break-all;
  flex: 1;
}

.proof-qrcode {
  margin-top: 20px;
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 6px;
}

.qrcode-label {
  color: #606266;
  margin-bottom: 10px;
  font-size: 14px;
}

.qrcode-code {
  font-size: 12px;
  color: #909399;
}

.proof-actions {
  margin-top: 20px;
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.proof-actions .el-button {
  margin: 0 8px;
}

@media (max-width: 768px) {
  .trace-record-detail {
    padding: 0 2px;
  }
  
  .info-card {
    margin-bottom: 15px;
  }
  
  .actions .el-button {
    margin: 5px;
    min-width: 100px;
  }

  .tx-hash {
    max-width: 100%;
  }
  
  .json-viewer {
    padding: 12px;
  }
  
  .json-content {
    font-size: 12px;
    max-height: 300px;
  }
}
</style>
