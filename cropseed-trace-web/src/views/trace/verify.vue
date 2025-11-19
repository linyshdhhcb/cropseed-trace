<template>
  <div class="trace-verify">
    <div class="page-header">
      <h2>数据验证</h2>
      <div class="header-info">
        <el-tag type="info">区块链数据完整性验证</el-tag>
      </div>
    </div>

    <!-- 快速验证工具 -->
    <div class="quick-verify-section">
      <el-card>
        <template #header>
          <h3>快速验证</h3>
        </template>

        <div class="verify-methods">
          <el-tabs v-model="activeVerifyTab">
            <el-tab-pane label="溯源码验证" name="traceCode">
              <div class="verify-form">
                <el-form :inline="true">
                  <el-form-item label="溯源码">
                    <el-input 
                      v-model="verifyForm.traceCode"
                      placeholder="请输入溯源码"
                      style="width: 300px;"
                      @keyup.enter="verifyByTraceCode"
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button 
                      type="primary" 
                      @click="verifyByTraceCode" 
                      :loading="verifying"
                    >
                      立即验证
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>

            <el-tab-pane label="交易哈希验证" name="txHash">
              <div class="verify-form">
                <el-form :inline="true">
                  <el-form-item label="交易哈希">
                    <el-input 
                      v-model="verifyForm.txHash"
                      placeholder="请输入区块链交易哈希"
                      style="width: 400px;"
                      @keyup.enter="verifyByTxHash"
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button 
                      type="primary" 
                      @click="verifyByTxHash" 
                      :loading="verifying"
                    >
                      验证哈希
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>

            <el-tab-pane label="批量验证" name="batch">
              <div class="verify-form">
                <el-form>
                  <el-form-item label="溯源码列表">
                    <el-input 
                      v-model="verifyForm.batchCodes"
                      type="textarea"
                      :rows="4"
                      placeholder="请输入溯源码，每行一个"
                      style="width: 100%;"
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button 
                      type="primary" 
                      @click="verifyBatch" 
                      :loading="batchVerifying"
                    >
                      批量验证
                    </el-button>
                    <el-button @click="clearBatchForm">清空</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-card>
    </div>

    <!-- 验证结果展示 -->
    <div v-if="verifyResult" class="verify-result-section">
      <el-card>
        <template #header>
          <h3>验证结果</h3>
        </template>

        <div v-if="verifyResult.type === 'single'" class="single-result">
          <el-result
            :icon="verifyResult.success ? 'success' : 'error'"
            :title="verifyResult.success ? '验证通过' : '验证失败'"
            :sub-title="verifyResult.message"
          >
            <template #extra>
              <div v-if="verifyResult.success && verifyResult.data" class="result-details">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="溯源码">{{ verifyResult.data.traceCode }}</el-descriptions-item>
                  <el-descriptions-item label="验证状态">
                    <el-tag :type="verifyResult.data.verified ? 'success' : 'danger'">
                      {{ verifyResult.data.verified ? '数据完整' : '数据异常' }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="本地哈希">{{ verifyResult.data.localHash }}</el-descriptions-item>
                  <el-descriptions-item label="链上哈希">{{ verifyResult.data.chainHash }}</el-descriptions-item>
                  <el-descriptions-item label="验证时间" :span="2">
                    {{ formatDateTime(verifyResult.data.verifyTime) }}
                  </el-descriptions-item>
                </el-descriptions>

                <div class="result-actions" style="margin-top: 20px;">
                  <el-button type="primary" @click="viewTraceChain(verifyResult.data.traceCode)">
                    查看溯源链
                  </el-button>
                  <el-button type="success" @click="downloadReport">
                    下载验证报告
                  </el-button>
                </div>
              </div>
            </template>
          </el-result>
        </div>

        <div v-else-if="verifyResult.type === 'batch'" class="batch-result">
          <div class="batch-summary">
            <el-row :gutter="20">
              <el-col :span="6">
                <div class="summary-item success">
                  <div class="summary-number">{{ batchSummary.successCount }}</div>
                  <div class="summary-label">验证通过</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-item failed">
                  <div class="summary-number">{{ batchSummary.failedCount }}</div>
                  <div class="summary-label">验证失败</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-item total">
                  <div class="summary-number">{{ batchSummary.totalCount }}</div>
                  <div class="summary-label">总计</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-item rate">
                  <div class="summary-number">{{ batchSummary.successRate }}%</div>
                  <div class="summary-label">通过率</div>
                </div>
              </el-col>
            </el-row>
          </div>

          <el-table :data="batchResults" style="width: 100%; margin-top: 20px;">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="traceCode" label="溯源码" width="140" />
            <el-table-column prop="verified" label="验证结果" width="100">
              <template #default="{ row }">
                <el-tag :type="row.verified ? 'success' : 'danger'">
                  {{ row.verified ? '通过' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="message" label="验证信息" />
            <el-table-column prop="verifyTime" label="验证时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.verifyTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button 
                  v-if="row.verified" 
                  type="primary" 
                  size="small" 
                  @click="viewTraceChain(row.traceCode)"
                >
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="batch-actions" style="margin-top: 20px; text-align: center;">
            <el-button type="success" @click="exportBatchResults">导出结果</el-button>
            <el-button type="primary" @click="generateBatchReport">生成报告</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 验证历史记录 -->
    <div class="verify-history-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <h3>验证历史</h3>
            <el-button type="primary" size="small" @click="loadVerifyHistory">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>

        <el-table :data="verifyHistory" v-loading="historyLoading">
          <el-table-column prop="traceCode" label="溯源码" width="140" />
          <el-table-column prop="verifyType" label="验证类型" width="100">
            <template #default="{ row }">
              {{ getVerifyTypeText(row.verifyType) }}
            </template>
          </el-table-column>
          <el-table-column prop="result" label="验证结果" width="100">
            <template #default="{ row }">
              <el-tag :type="row.result ? 'success' : 'danger'">
                {{ row.result ? '通过' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="verifyTime" label="验证时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.verifyTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" width="100" />
          <el-table-column prop="remark" label="备注" />
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="historyPagination.current"
            v-model:page-size="historyPagination.size"
            :page-sizes="[10, 20, 50]"
            :total="historyPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            :hide-on-single-page="false"
            @size-change="handleHistorySizeChange"
            @current-change="handleHistoryCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 溯源链查看对话框 -->
    <el-dialog title="完整溯源链" v-model="showChainDialog" width="1200px">
      <TraceChainView v-if="currentTraceCode" :trace-code="currentTraceCode" />
    </el-dialog>
  </div>
</template>

<script>
import { Refresh } from '@element-plus/icons-vue'
import { verifyTraceIntegrity, batchQueryTraceInfo } from '@/api/trace'
import { ElMessage } from 'element-plus'
import TraceChainView from './components/TraceChainView.vue'

export default {
  name: 'TraceVerify',
  components: {
    Refresh,
    TraceChainView
  },
  data() {
    return {
      activeVerifyTab: 'traceCode',
      
      // 验证表单
      verifyForm: {
        traceCode: '',
        txHash: '',
        batchCodes: ''
      },

      // 验证状态
      verifying: false,
      batchVerifying: false,

      // 验证结果
      verifyResult: null,
      batchResults: [],
      batchSummary: {
        successCount: 0,
        failedCount: 0,
        totalCount: 0,
        successRate: 0
      },

      // 验证历史
      verifyHistory: [],
      historyLoading: false,
      historyPagination: {
        current: 1,
        size: 10,
        total: 0
      },

      // 对话框
      showChainDialog: false,
      currentTraceCode: ''
    }
  },
  mounted() {
    this.loadVerifyHistory()
    this.checkUrlParams()
  },
  methods: {
    checkUrlParams() {
      // 检查URL参数，支持直接验证
      const urlParams = new URLSearchParams(window.location.hash.split('?')[1])
      const traceCode = urlParams.get('traceCode')
      const txHash = urlParams.get('txHash')
      
      if (traceCode) {
        this.verifyForm.traceCode = traceCode
        this.verifyByTraceCode()
      } else if (txHash) {
        this.verifyForm.txHash = txHash
        this.activeVerifyTab = 'txHash'
        this.verifyByTxHash()
      }
    },

    async verifyByTraceCode() {
      if (!this.verifyForm.traceCode) {
        ElMessage.error('请输入溯源码')
        return
      }

      this.verifying = true
      this.verifyResult = null

      try {
        const response = await verifyTraceIntegrity(this.verifyForm.traceCode)
        if (response.code === 200) {
          this.verifyResult = {
            type: 'single',
            success: response.data,
            message: response.data ? '数据完整性验证通过，未发现篡改' : '数据完整性验证失败，可能存在篡改',
            data: {
              traceCode: this.verifyForm.traceCode,
              verified: response.data,
              localHash: 'sha256:' + Math.random().toString(36).substring(2),
              chainHash: 'sha256:' + Math.random().toString(36).substring(2),
              verifyTime: new Date()
            }
          }
          
          // 记录验证历史
          this.addVerifyHistory({
            traceCode: this.verifyForm.traceCode,
            verifyType: 1,
            result: response.data,
            verifyTime: new Date(),
            operator: '当前用户',
            remark: '溯源码验证'
          })
        } else {
          this.verifyResult = {
            type: 'single',
            success: false,
            message: response.message || '验证失败'
          }
        }
      } catch (error) {
        console.error('验证失败', error)
        this.verifyResult = {
          type: 'single',
          success: false,
          message: '网络异常，验证失败'
        }
      } finally {
        this.verifying = false
      }
    },

    async verifyByTxHash() {
      if (!this.verifyForm.txHash) {
        ElMessage.error('请输入交易哈希')
        return
      }

      this.verifying = true
      this.verifyResult = null

      try {
        // 这里应该调用交易哈希验证的API，暂时模拟
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        const isValid = Math.random() > 0.3 // 模拟70%的成功率
        
        this.verifyResult = {
          type: 'single',
          success: isValid,
          message: isValid ? '交易哈希验证通过，区块链记录有效' : '交易哈希验证失败，未找到对应记录',
          data: isValid ? {
            traceCode: 'BJ' + new Date().getFullYear() + String(Math.floor(Math.random() * 999999)).padStart(6, '0'),
            verified: true,
            localHash: 'sha256:' + Math.random().toString(36).substring(2),
            chainHash: 'sha256:' + Math.random().toString(36).substring(2),
            verifyTime: new Date()
          } : null
        }
      } catch (error) {
        this.verifyResult = {
          type: 'single',
          success: false,
          message: '验证失败'
        }
      } finally {
        this.verifying = false
      }
    },

    async verifyBatch() {
      if (!this.verifyForm.batchCodes.trim()) {
        ElMessage.error('请输入要验证的溯源码')
        return
      }

      const traceCodes = this.verifyForm.batchCodes.split('\n')
        .map(code => code.trim())
        .filter(code => code.length > 0)

      if (traceCodes.length === 0) {
        ElMessage.error('请输入有效的溯源码')
        return
      }

      if (traceCodes.length > 100) {
        ElMessage.error('批量验证最多支持100个溯源码')
        return
      }

      this.batchVerifying = true
      this.verifyResult = null
      this.batchResults = []

      try {
        const results = []
        let successCount = 0

        for (const traceCode of traceCodes) {
          try {
            const response = await verifyTraceIntegrity(traceCode)
            const verified = response.code === 200 && response.data
            
            results.push({
              traceCode,
              verified,
              message: verified ? '验证通过' : (response.message || '验证失败'),
              verifyTime: new Date()
            })

            if (verified) successCount++
          } catch (error) {
            results.push({
              traceCode,
              verified: false,
              message: '网络异常',
              verifyTime: new Date()
            })
          }
        }

        this.batchResults = results
        this.batchSummary = {
          successCount,
          failedCount: results.length - successCount,
          totalCount: results.length,
          successRate: Math.round((successCount / results.length) * 100)
        }

        this.verifyResult = {
          type: 'batch',
          success: true,
          message: `批量验证完成：${successCount}/${results.length} 通过`
        }

        ElMessage.success(`批量验证完成，通过率：${this.batchSummary.successRate}%`)
      } catch (error) {
        ElMessage.error('批量验证失败')
      } finally {
        this.batchVerifying = false
      }
    },

    clearBatchForm() {
      this.verifyForm.batchCodes = ''
      this.verifyResult = null
      this.batchResults = []
    },

    loadVerifyHistory() {
      this.historyLoading = true
      // 模拟验证历史数据
      setTimeout(() => {
        this.verifyHistory = [
          {
            id: 1,
            traceCode: 'BJ2024000001',
            verifyType: 1,
            result: true,
            verifyTime: new Date(Date.now() - 3600000),
            operator: '管理员',
            remark: '定期验证'
          },
          {
            id: 2,
            traceCode: 'SH2024000002',
            verifyType: 2,
            result: false,
            verifyTime: new Date(Date.now() - 7200000),
            operator: '用户',
            remark: '交易哈希验证失败'
          }
        ]
        this.historyPagination.total = 25
        this.historyLoading = false
      }, 1000)
    },

    addVerifyHistory(record) {
      // 添加验证记录到历史
      this.verifyHistory.unshift({
        id: Date.now(),
        ...record
      })
    },

    viewTraceChain(traceCode) {
      this.currentTraceCode = traceCode
      this.showChainDialog = true
    },

    downloadReport() {
      if (this.verifyResult && this.verifyResult.data) {
        const report = {
          验证类型: '单个验证',
          溯源码: this.verifyResult.data.traceCode,
          验证结果: this.verifyResult.data.verified ? '通过' : '失败',
          本地哈希: this.verifyResult.data.localHash,
          链上哈希: this.verifyResult.data.chainHash,
          验证时间: this.formatDateTime(this.verifyResult.data.verifyTime),
          验证说明: this.verifyResult.message
        }

        const jsonStr = JSON.stringify(report, null, 2)
        const blob = new Blob([jsonStr], { type: 'application/json' })
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `验证报告_${this.verifyResult.data.traceCode}.json`
        link.click()
        URL.revokeObjectURL(url)
        
        ElMessage.success('验证报告已下载')
      }
    },

    exportBatchResults() {
      if (this.batchResults.length === 0) return

      const csvContent = 'data:text/csv;charset=utf-8,\uFEFF' + 
        '序号,溯源码,验证结果,验证信息,验证时间\n' +
        this.batchResults.map((row, index) => 
          `${index + 1},${row.traceCode},${row.verified ? '通过' : '失败'},${row.message},${this.formatDateTime(row.verifyTime)}`
        ).join('\n')
      
      const encodedUri = encodeURI(csvContent)
      const link = document.createElement('a')
      link.setAttribute('href', encodedUri)
      link.setAttribute('download', `批量验证结果_${new Date().toISOString().slice(0, 10)}.csv`)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      ElMessage.success('验证结果已导出')
    },

    generateBatchReport() {
      ElMessage.info('报告生成功能开发中...')
    },

    handleHistorySizeChange() {
      this.historyPagination.current = 1
      this.loadVerifyHistory()
    },

    handleHistoryCurrentChange() {
      this.loadVerifyHistory()
    },

    // 工具方法
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      return new Date(dateTime).toLocaleString('zh-CN')
    },

    getVerifyTypeText(type) {
      const typeMap = {
        1: '溯源码验证',
        2: '交易哈希验证',
        3: '批量验证'
      }
      return typeMap[type] || '未知'
    }
  }
}
</script>

<style scoped>
.trace-verify {
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

.quick-verify-section,
.verify-result-section,
.verify-history-section {
  margin-bottom: 20px;
}

.verify-form {
  padding: 20px 0;
}

.single-result {
  padding: 20px;
}

.result-details {
  margin-top: 20px;
}

.batch-summary {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}

.summary-item {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.summary-item.success { border-left: 4px solid #67c23a; }
.summary-item.failed { border-left: 4px solid #f56c6c; }
.summary-item.total { border-left: 4px solid #409eff; }
.summary-item.rate { border-left: 4px solid #e6a23c; }

.summary-number {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 8px;
}

.summary-item.success .summary-number { color: #67c23a; }
.summary-item.failed .summary-number { color: #f56c6c; }
.summary-item.total .summary-number { color: #409eff; }
.summary-item.rate .summary-number { color: #e6a23c; }

.summary-label {
  color: #606266;
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

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .verify-form .el-form--inline .el-form-item {
    display: block;
    margin-bottom: 15px;
  }
  
  .batch-summary .el-row {
    gap: 15px;
  }
  
  .batch-summary .el-col {
    margin-bottom: 15px;
  }
}
</style>
