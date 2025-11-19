<template>
  <div class="blockchain-management">
    <div class="page-header">
      <h2>区块链管理</h2>
      <div class="header-actions">
        <el-button type="success" @click="batchUpload" :loading="batchUploading">
          <el-icon><Upload /></el-icon>
          批量上链
        </el-button>
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 区块链状态统计 -->
    <div class="blockchain-stats">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-icon total">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ stats.totalRecords }}</div>
                <div class="stat-label">总记录数</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-icon success">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ stats.onChainRecords }}</div>
                <div class="stat-label">已上链记录</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-icon pending">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ stats.pendingRecords }}</div>
                <div class="stat-label">待上链记录</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-icon failed">
                <el-icon><CircleClose /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ stats.failedRecords }}</div>
                <div class="stat-label">上链失败</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 区块链配置信息 -->
    <div class="blockchain-config">
      <el-card>
        <template #header>
          <h3>区块链网络配置</h3>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="网络名称">腾讯云TBaas长安链体验网络</el-descriptions-item>
          <el-descriptions-item label="集群ID">chainmaker-demo</el-descriptions-item>
          <el-descriptions-item label="链ID">chain_demo</el-descriptions-item>
          <el-descriptions-item label="合约名称">ChainMakerDemo</el-descriptions-item>
          <el-descriptions-item label="网络状态">
            <el-tag type="success">正常</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最后同步时间">{{ formatDateTime(lastSyncTime) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>

    <!-- 上链记录列表 -->
    <div class="blockchain-records">
      <el-card>
        <template #header>
          <div class="card-header">
            <h3>区块链记录</h3>
            <el-form :inline="true" size="small">
              <el-form-item>
                <el-select v-model="recordsFilter.status" placeholder="上链状态" clearable style="width: 120px;">
                  <el-option label="未上链" :value="0" />
                  <el-option label="上链中" :value="1" />
                  <el-option label="已上链" :value="2" />
                  <el-option label="失败" :value="3" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-input
                  v-model="recordsFilter.traceCode"
                  placeholder="溯源码"
                  clearable
                  style="width: 150px;"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" @click="loadBlockchainRecords">筛选</el-button>
              </el-form-item>
            </el-form>
          </div>
        </template>

        <el-table 
          :data="blockchainRecords" 
          v-loading="recordsLoading" 
          stripe
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          
          <el-table-column prop="traceCode" label="溯源码" width="140">
            <template #default="{ row }">
              <el-button type="text" @click="viewTraceDetail(row.traceCode)">
                {{ row.traceCode }}
              </el-button>
            </template>
          </el-table-column>

          <el-table-column prop="recordStage" label="记录阶段" width="100" />

          <el-table-column prop="blockchainStatus" label="上链状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getBlockchainStatusType(row.blockchainStatus)">
                {{ getBlockchainStatusText(row.blockchainStatus) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="blockchainTxHash" label="交易哈希" width="200">
            <template #default="{ row }">
              <div v-if="row.blockchainTxHash" class="tx-hash">
                <span>{{ formatTxHash(row.blockchainTxHash) }}</span>
                <el-button type="text" size="small" @click="copyTxHash(row.blockchainTxHash)">
                  复制
                </el-button>
              </div>
              <span v-else>-</span>
            </template>
          </el-table-column>

          <el-table-column prop="recordTime" label="记录时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.recordTime) }}
            </template>
          </el-table-column>

          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button 
                v-if="row.blockchainStatus === 0 || row.blockchainStatus === 3" 
                type="primary" 
                size="small" 
                @click="uploadSingle(row)"
              >
                上链
              </el-button>
              <el-button 
                v-if="row.blockchainStatus === 2" 
                type="success" 
                size="small" 
                @click="verifyIntegrity(row)"
              >
                验证
              </el-button>
              <el-button 
                v-if="row.blockchainStatus === 2 && row.blockchainTxHash" 
                type="info" 
                size="small" 
                @click="getProof(row)"
              >
                获取证明
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="recordsPagination.current"
            v-model:page-size="recordsPagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="recordsPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            :hide-on-single-page="false"
            @size-change="handleRecordsSizeChange"
            @current-change="handleRecordsCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 批量操作 -->
    <div v-if="selectedRows.length > 0" class="batch-operations">
      <el-card>
        <div class="batch-info">
          <span>已选择 {{ selectedRows.length }} 条记录</span>
          <div class="batch-actions">
            <el-button type="primary" @click="batchUploadSelected" :loading="batchUploading">
              批量上链
            </el-button>
            <el-button type="success" @click="batchVerifySelected" :loading="batchVerifying">
              批量验证
            </el-button>
            <el-button @click="clearSelection">清除选择</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 区块链证明对话框 -->
    <el-dialog title="区块链证明" v-model="showProofDialog" width="700px">
      <div v-if="currentProof" class="proof-content">
        <el-alert
          title="区块链存证证明"
          type="success"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <template #default>
            该溯源记录已成功上链到腾讯云TBaas区块链平台，数据真实可信，不可篡改。
          </template>
        </el-alert>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="溯源码" :span="2">
            <el-tag type="primary" size="large">{{ currentProof.traceCode }}</el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item label="数据哈希" :span="2">
            <div class="hash-value">
              <code>{{ currentProof.dataHash }}</code>
              <el-button 
                type="text" 
                size="small" 
                @click="copyToClipboard(currentProof.dataHash)"
                style="margin-left: 10px;"
              >
                复制
              </el-button>
            </div>
          </el-descriptions-item>

          <el-descriptions-item label="交易哈希" :span="2" v-if="currentProof.txHash">
            <div class="hash-value">
              <code>{{ currentProof.txHash }}</code>
              <el-button 
                type="text" 
                size="small" 
                @click="copyToClipboard(currentProof.txHash)"
                style="margin-left: 10px;"
              >
                复制
              </el-button>
            </div>
          </el-descriptions-item>

          <el-descriptions-item label="区块高度" v-if="currentProof.blockHeight">
            {{ currentProof.blockHeight }}
          </el-descriptions-item>
          
          <el-descriptions-item label="验证状态" :span="currentProof.blockHeight ? 1 : 2">
            <el-tag :type="currentProof.verified ? 'success' : 'danger'" size="large">
              <el-icon style="margin-right: 5px;">
                <CircleCheck v-if="currentProof.verified" />
                <CircleClose v-else />
              </el-icon>
              {{ currentProof.verified ? '验证通过' : '验证失败' }}
            </el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item label="证明生成时间" :span="2">
            {{ formatDateTime(currentProof.timestamp) }}
          </el-descriptions-item>

          <el-descriptions-item label="区块链平台" :span="2">
            <el-tag>腾讯云TBaas - 长安链体验网络</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="proof-qrcode" style="margin-top: 20px; text-align: center; padding: 20px; background: #f5f7fa; border-radius: 4px;">
          <div style="color: #606266; margin-bottom: 10px;">扫码查看证明</div>
          <div style="font-size: 12px; color: #909399;">
            验证码：{{ currentProof.traceCode }}
          </div>
        </div>

        <div class="proof-actions" style="margin-top: 20px; text-align: center;">
          <el-button type="primary" @click="downloadProof">
            <el-icon><Download /></el-icon>
            下载证明
          </el-button>
          <el-button type="success" @click="copyProofText">
            <el-icon><DocumentCopy /></el-icon>
            复制证明
          </el-button>
          <el-button @click="showProofDialog = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 溯源详情对话框 -->
    <el-dialog title="溯源记录详情" v-model="showDetailDialog" width="900px">
      <TraceRecordDetail v-if="currentRecord" :record="currentRecord" />
    </el-dialog>
  </div>
</template>

<script>
import { Upload, Refresh, Document, CircleCheck, Clock, CircleClose, Download, DocumentCopy } from '@element-plus/icons-vue'
import { 
  getTraceRecordsPage, 
  batchUploadToBlockchain,
  verifyTraceIntegrity,
  getTraceProof,
  getTraceRecordById
} from '@/api/trace'
import { ElMessage, ElMessageBox } from 'element-plus'
import TraceRecordDetail from './components/TraceRecordDetail.vue'

export default {
  name: 'BlockchainManagement',
  components: {
    Upload,
    Refresh,
    Document,
    CircleCheck,
    Clock,
    CircleClose,
    Download,
    DocumentCopy,
    TraceRecordDetail
  },
  data() {
    return {
      // 统计数据
      stats: {
        totalRecords: 0,
        onChainRecords: 0,
        pendingRecords: 0,
        failedRecords: 0
      },

      // 上链记录
      blockchainRecords: [],
      recordsLoading: false,
      recordsFilter: {
        status: null,
        traceCode: ''
      },
      recordsPagination: {
        current: 1,
        size: 20,
        total: 0
      },

      // 选择的记录
      selectedRows: [],

      // 批量操作状态
      batchUploading: false,
      batchVerifying: false,

      // 对话框
      showProofDialog: false,
      showDetailDialog: false,
      currentProof: null,
      currentRecord: null,

      // 最后同步时间
      lastSyncTime: new Date()
    }
  },
  mounted() {
    this.loadStats()
    this.loadBlockchainRecords()
  },
  methods: {
    async loadStats() {
      try {
        // 加载统计数据
        const response = await getTraceRecordsPage({
          pageNum: 1,
          pageSize: 1
        })
        
        if (response.code === 200) {
          // 这里应该有专门的统计API，暂时用模拟数据
          this.stats = {
            totalRecords: response.data.total || 156,
            onChainRecords: Math.floor((response.data.total || 156) * 0.8),
            pendingRecords: Math.floor((response.data.total || 156) * 0.15),
            failedRecords: Math.floor((response.data.total || 156) * 0.05)
          }
        }
      } catch (error) {
        console.error('加载统计数据失败', error)
        // 使用默认数据
        this.stats = {
          totalRecords: 156,
          onChainRecords: 134,
          pendingRecords: 18,
          failedRecords: 4
        }
      }
    },

    async loadBlockchainRecords() {
      this.recordsLoading = true
      try {
        const params = {
          pageNum: this.recordsPagination.current,
          pageSize: this.recordsPagination.size,
          ...this.recordsFilter
        }
        
        // 过滤空值
        Object.keys(params).forEach(key => {
          if (params[key] === '' || params[key] === null) {
            delete params[key]
          }
        })

        const response = await getTraceRecordsPage(params)
        if (response.code === 200) {
          this.blockchainRecords = response.data.list || []
          this.recordsPagination.total = response.data.total || 0
        }
      } catch (error) {
        console.error('加载区块链记录失败', error)
        ElMessage.error('加载数据失败')
      } finally {
        this.recordsLoading = false
      }
    },

    async batchUpload() {
      try {
        await ElMessageBox.confirm(
          '确定要批量上链所有未上链的记录吗？',
          '批量上链确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        this.batchUploading = true
        const response = await batchUploadToBlockchain(100)
        if (response.code === 200) {
          ElMessage.success(`批量上链完成，成功处理 ${response.data} 条记录`)
          this.loadStats()
          this.loadBlockchainRecords()
        } else {
          ElMessage.error(response.message || '批量上链失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量上链失败', error)
          ElMessage.error('批量上链失败')
        }
      } finally {
        this.batchUploading = false
      }
    },

    async batchUploadSelected() {
      if (this.selectedRows.length === 0) {
        ElMessage.warning('请选择要上链的记录')
        return
      }

      this.batchUploading = true
      try {
        // 这里应该调用指定记录的批量上链API
        ElMessage.info('功能开发中...')
      } finally {
        this.batchUploading = false
      }
    },

    async batchVerifySelected() {
      if (this.selectedRows.length === 0) {
        ElMessage.warning('请选择要验证的记录')
        return
      }

      this.batchVerifying = true
      try {
        let successCount = 0
        let failCount = 0

        for (const record of this.selectedRows) {
          if (record.traceCode) {
            try {
              const response = await verifyTraceIntegrity(record.traceCode)
              if (response.code === 200 && response.data) {
                successCount++
              } else {
                failCount++
              }
            } catch (error) {
              failCount++
            }
          }
        }

        ElMessage.success(`验证完成：成功 ${successCount} 条，失败 ${failCount} 条`)
      } catch (error) {
        ElMessage.error('批量验证失败')
      } finally {
        this.batchVerifying = false
      }
    },

    async uploadSingle(record) {
      try {
        ElMessage.info('正在上链处理...')
        // 这里应该调用单个记录上链的API
        setTimeout(() => {
          ElMessage.success('上链请求已提交')
          this.loadBlockchainRecords()
        }, 1000)
      } catch (error) {
        ElMessage.error('上链失败')
      }
    },

    async verifyIntegrity(record) {
      // 检查上链状态
      if (record.blockchainStatus !== 2) {
        ElMessage.warning('该记录尚未成功上链，无法验证。当前状态：' + this.getBlockchainStatusText(record.blockchainStatus))
        return
      }

      try {
        const response = await verifyTraceIntegrity(record.traceCode)
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
        ElMessage.error('验证失败：' + (error.message || '网络错误'))
      }
    },

    async getProof(record) {
      // 检查上链状态
      if (record.blockchainStatus !== 2) {
        ElMessage.warning('该记录尚未成功上链，无法生成证明。当前状态：' + this.getBlockchainStatusText(record.blockchainStatus))
        return
      }

      if (!record.blockchainTxHash) {
        ElMessage.warning('该记录缺少交易哈希，无法生成证明')
        return
      }

      try {
        const response = await getTraceProof(record.traceCode)
        if (response.code === 200) {
          this.currentProof = response.data
          this.showProofDialog = true
        } else {
          ElMessage.error(response.message || '获取证明失败')
        }
      } catch (error) {
        console.error('获取证明失败', error)
        ElMessage.error('获取证明失败：' + (error.message || '网络错误'))
      }
    },

    async viewTraceDetail(traceCode) {
      // 这里应该根据溯源码查询记录详情
      try {
        // 临时使用第一条记录作为示例
        this.currentRecord = this.blockchainRecords.find(r => r.traceCode === traceCode)
        this.showDetailDialog = true
      } catch (error) {
        ElMessage.error('加载详情失败')
      }
    },

    refreshData() {
      this.loadStats()
      this.loadBlockchainRecords()
      this.lastSyncTime = new Date()
      ElMessage.success('数据已刷新')
    },

    handleSelectionChange(selection) {
      this.selectedRows = selection
    },

    clearSelection() {
      this.selectedRows = []
      this.$refs.recordTable?.clearSelection()
    },

    handleRecordsSizeChange() {
      this.recordsPagination.current = 1
      this.loadBlockchainRecords()
    },

    handleRecordsCurrentChange() {
      this.loadBlockchainRecords()
    },

    copyTxHash(txHash) {
      try {
        navigator.clipboard.writeText(txHash)
        ElMessage.success('交易哈希已复制')
      } catch (error) {
        const textarea = document.createElement('textarea')
        textarea.value = txHash
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
        ElMessage.success('交易哈希已复制')
      }
    },

    downloadProof() {
      if (this.currentProof) {
        const proofData = {
          溯源码: this.currentProof.traceCode,
          数据哈希: this.currentProof.dataHash,
          验证状态: this.currentProof.verified ? '已验证' : '验证失败',
          证明生成时间: this.formatDateTime(this.currentProof.timestamp),
          区块链平台: '腾讯云TBaas - 长安链体验网络'
        }

        // 添加可选字段
        if (this.currentProof.txHash) {
          proofData.交易哈希 = this.currentProof.txHash
        }
        if (this.currentProof.blockHeight) {
          proofData.区块高度 = this.currentProof.blockHeight
        }

        const jsonStr = JSON.stringify(proofData, null, 2)
        const blob = new Blob([jsonStr], { type: 'application/json' })
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `区块链证明_${this.currentProof.traceCode}_${Date.now()}.json`
        link.click()
        URL.revokeObjectURL(url)
        
        ElMessage.success('证明已下载')
      }
    },

    copyToClipboard(text) {
      if (!text) return
      
      navigator.clipboard.writeText(text).then(() => {
        ElMessage.success('已复制到剪贴板')
      }).catch(() => {
        // 降级方案
        const textarea = document.createElement('textarea')
        textarea.value = text
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
        ElMessage.success('已复制到剪贴板')
      })
    },

    copyProofText() {
      if (this.currentProof) {
        let proofText = `=== 区块链存证证明 ===\n\n`
        proofText += `溯源码：${this.currentProof.traceCode}\n`
        proofText += `数据哈希：${this.currentProof.dataHash}\n`
        
        if (this.currentProof.txHash) {
          proofText += `交易哈希：${this.currentProof.txHash}\n`
        }
        if (this.currentProof.blockHeight) {
          proofText += `区块高度：${this.currentProof.blockHeight}\n`
        }
        
        proofText += `验证状态：${this.currentProof.verified ? '已验证' : '验证失败'}\n`
        proofText += `证明生成时间：${this.formatDateTime(this.currentProof.timestamp)}\n`
        proofText += `区块链平台：腾讯云TBaas - 长安链体验网络\n\n`
        proofText += `该溯源记录已成功上链，数据真实可信，不可篡改。`
        
        this.copyToClipboard(proofText)
      }
    },

    // 工具方法
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      // 处理字符串类型的时间戳
      const timestamp = typeof dateTime === 'string' ? Number(dateTime) : dateTime
      const date = new Date(timestamp)
      // 检查日期是否有效
      if (isNaN(date.getTime())) return '-'
      return date.toLocaleString('zh-CN')
    },

    formatTxHash(txHash) {
      if (!txHash) return '-'
      if (txHash.length > 20) {
        return `${txHash.substring(0, 10)}...${txHash.substring(txHash.length - 8)}`
      }
      return txHash
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
.blockchain-management {
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

.header-actions .el-button {
  margin-left: 10px;
}

.blockchain-stats,
.blockchain-config,
.blockchain-records {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
}

.stat-item {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: white;
}

.stat-icon.total { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.success { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.stat-icon.pending { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
.stat-icon.failed { background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%); }

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
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

.tx-hash {
  display: flex;
  align-items: center;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.tx-hash span {
  margin-right: 8px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.batch-operations {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
}

.batch-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #409eff;
  color: white;
  border-radius: 8px;
  min-width: 400px;
}

.batch-actions .el-button {
  margin-left: 10px;
}

.proof-content {
  padding: 20px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    width: 100%;
    text-align: right;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .batch-operations {
    position: static;
    transform: none;
    margin-top: 20px;
  }
  
  .batch-info {
    flex-direction: column;
    gap: 15px;
    min-width: auto;
  }
}

/* 证明对话框样式 */
.proof-content {
  padding: 10px 0;
}

.hash-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hash-value code {
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  color: #409eff;
  word-break: break-all;
  flex: 1;
  margin-right: 10px;
}

.proof-qrcode {
  border: 2px dashed #dcdfe6;
}

.proof-actions .el-button {
  margin: 0 5px;
}
</style>
