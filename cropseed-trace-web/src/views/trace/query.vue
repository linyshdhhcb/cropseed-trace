<template>
  <div class="trace-query">
    <div class="page-header">
      <h2>溯源查询统计</h2>
      <div class="header-actions">
        <el-button type="success" @click="exportData">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-icon total">
                <el-icon><DataAnalysis /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ statistics.totalQueries }}</div>
                <div class="stat-label">总查询次数</div>
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
                <div class="stat-number">{{ statistics.successQueries }}</div>
                <div class="stat-label">成功查询</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-icon unique">
                <el-icon><Collection /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ statistics.uniqueTraceCodes }}</div>
                <div class="stat-label">唯一溯源码</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-icon today">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ statistics.todayQueries }}</div>
                <div class="stat-label">今日查询</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 查询工具 -->
    <div class="query-tool-section">
      <el-card>
        <template #header>
          <h3>快速溯源查询</h3>
        </template>

        <el-form :inline="true">
          <el-form-item label="溯源码">
            <el-input 
              v-model="queryForm.traceCode"
              placeholder="请输入溯源码"
              style="width: 300px;"
              @keyup.enter="handleQuickQuery"
            />
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleQuickQuery" 
              :loading="querying"
            >
              立即查询
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 查询结果 -->
        <div v-if="queryResult" class="quick-query-result">
          <el-alert
            :title="queryResult.success ? '查询成功' : '查询失败'"
            :type="queryResult.success ? 'success' : 'error'"
            show-icon
            :closable="false"
          />
          
          <div v-if="queryResult.success && queryResult.data" class="result-content">
            <el-descriptions title="溯源信息" :column="2" border style="margin-top: 20px;">
              <el-descriptions-item label="溯源码">{{ queryResult.data.traceCode }}</el-descriptions-item>
              <el-descriptions-item label="记录数量">{{ queryResult.data.recordCount }}</el-descriptions-item>
              <el-descriptions-item label="验证状态">
                <el-tag :type="queryResult.data.verified ? 'success' : 'warning'">
                  {{ queryResult.data.verified ? '已验证' : '未验证' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="查询时间">{{ formatDateTime(queryResult.data.queryTime) }}</el-descriptions-item>
            </el-descriptions>

            <div class="result-actions" style="margin-top: 20px;">
              <el-button type="primary" @click="viewFullChain">查看完整溯源链</el-button>
              <el-button type="success" @click="generateReport">生成报告</el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 热门溯源码排行 -->
    <div class="popular-section">
      <el-card>
        <template #header>
          <h3>热门溯源码排行</h3>
        </template>

        <div class="popular-list">
          <div 
            v-for="(item, index) in popularTraceCodes" 
            :key="item.traceCode"
            class="popular-item"
          >
            <div class="rank-number" :class="getRankClass(index)">{{ index + 1 }}</div>
            <div class="trace-info">
              <div class="trace-code">{{ item.traceCode }}</div>
              <div class="trace-stats">{{ item.queryCount }} 次查询</div>
            </div>
            <div class="actions">
              <el-button type="text" size="small" @click="viewTraceChain(item.traceCode)">
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 查询渠道分布 -->
    <div class="channel-section">
      <el-card>
        <template #header>
          <h3>查询渠道分布</h3>
        </template>

        <div class="channel-chart">
          <div class="chart-placeholder">
            <p>查询渠道分布图表</p>
            <div class="channel-stats">
              <div class="channel-item">
                <div class="channel-label">小程序扫码</div>
                <div class="channel-value">{{ channelStats.miniapp || 0 }}次</div>
              </div>
              <div class="channel-item">
                <div class="channel-label">网页查询</div>
                <div class="channel-value">{{ channelStats.web || 0 }}次</div>
              </div>
              <div class="channel-item">
                <div class="channel-label">APP查询</div>
                <div class="channel-value">{{ channelStats.app || 0 }}次</div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 查询记录表格 -->
    <div class="records-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <h3>查询记录</h3>
            <el-form :inline="true" size="small">
              <el-form-item>
                <el-select v-model="recordsFilter.result" placeholder="查询结果" clearable style="width: 120px;">
                  <el-option label="成功" :value="1" />
                  <el-option label="失败" :value="2" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-date-picker
                  v-model="recordsFilter.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  size="small"
                  style="width: 240px;"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" @click="loadQueryRecords">筛选</el-button>
              </el-form-item>
            </el-form>
          </div>
        </template>

        <el-table :data="queryRecords" v-loading="recordsLoading" stripe>
          <el-table-column prop="traceCode" label="溯源码" width="140" />
          <el-table-column prop="queryTime" label="查询时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.queryTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="queryResult" label="查询结果" width="100">
            <template #default="{ row }">
              <el-tag :type="getResultType(row.queryResult)">
                {{ getResultText(row.queryResult) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="queryIp" label="查询IP" width="130" />
          <el-table-column prop="queryChannel" label="查询渠道" width="100">
            <template #default="{ row }">
              {{ getChannelText(row.queryChannel) }}
            </template>
          </el-table-column>
          <el-table-column prop="responseTime" label="响应时间" width="100">
            <template #default="{ row }">
              {{ row.responseTime }}ms
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewQueryDetail(row)">
                详情
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

    <!-- 溯源链查看对话框 -->
    <el-dialog title="完整溯源链" v-model="showChainDialog" width="1200px">
      <TraceChainView :trace-code="currentTraceCode" />
    </el-dialog>

    <!-- 查询详情对话框 -->
    <el-dialog title="查询详情" v-model="showQueryDetailDialog" width="800px">
      <div v-if="currentQueryRecord" class="query-detail">
        <!-- 基本信息 -->
        <el-descriptions title="查询基本信息" :column="2" border>
          <el-descriptions-item label="查询ID">{{ currentQueryRecord.id }}</el-descriptions-item>
          <el-descriptions-item label="溯源码">
            <el-tag type="primary">{{ currentQueryRecord.traceCode }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="查询时间">
            {{ formatDateTime(currentQueryRecord.queryTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="查询结果">
            <el-tag :type="getResultType(currentQueryRecord.queryResult)">
              {{ getResultText(currentQueryRecord.queryResult) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="查询IP">{{ currentQueryRecord.queryIp }}</el-descriptions-item>
          <el-descriptions-item label="查询地区">{{ currentQueryRecord.queryRegion || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="查询渠道">
            {{ getChannelText(currentQueryRecord.queryChannel) }}
          </el-descriptions-item>
          <el-descriptions-item label="响应时间">
            <el-tag :type="getResponseTimeType(currentQueryRecord.responseTime)">
              {{ currentQueryRecord.responseTime }}ms
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 设备信息 -->
        <el-descriptions title="设备信息" :column="2" border style="margin-top: 20px;">
          <el-descriptions-item label="设备类型">
            {{ currentQueryRecord.deviceType || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="操作系统">
            {{ currentQueryRecord.osVersion || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="浏览器" :span="2">
            {{ currentQueryRecord.browser || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="User Agent" :span="2">
            <div style="word-break: break-all; max-width: 600px;">
              {{ currentQueryRecord.userAgent || '未知' }}
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 查询参数 -->
        <el-descriptions title="查询参数" :column="1" border style="margin-top: 20px;">
          <el-descriptions-item label="查询参数">
            <pre style="background: #f5f7fa; padding: 10px; border-radius: 4px; margin: 0;">{{ formatJson(currentQueryRecord.queryParams) }}</pre>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 响应信息 -->
        <el-descriptions title="响应信息" :column="1" border style="margin-top: 20px;" v-if="currentQueryRecord.responseData">
          <el-descriptions-item label="响应数据">
            <pre style="background: #f5f7fa; padding: 10px; border-radius: 4px; margin: 0; max-height: 300px; overflow-y: auto;">{{ formatJson(currentQueryRecord.responseData) }}</pre>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 操作按钮 -->
        <div class="detail-actions" style="margin-top: 20px; text-align: right;">
          <el-button type="primary" @click="viewTraceChain(currentQueryRecord.traceCode)">
            查看溯源链
          </el-button>
          <el-button @click="showQueryDetailDialog = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { Download, Refresh, DataAnalysis, CircleCheck, Collection, Calendar } from '@element-plus/icons-vue'
import { queryTraceInfo, getQueryStatistics } from '@/api/trace'
import { ElMessage } from 'element-plus'
import TraceChainView from './components/TraceChainView.vue'

export default {
  name: 'TraceQuery',
  components: {
    Download,
    Refresh,
    DataAnalysis,
    CircleCheck,
    Collection,
    Calendar,
    TraceChainView
  },
  data() {
    return {
      // 统计数据
      statistics: {
        totalQueries: 0,
        successQueries: 0,
        uniqueTraceCodes: 0,
        todayQueries: 0
      },

      // 查询表单
      queryForm: {
        traceCode: ''
      },
      querying: false,
      queryResult: null,

      // 热门溯源码
      popularTraceCodes: [],

      // 渠道统计
      channelStats: {
        miniapp: 0,
        web: 0,
        app: 0
      },

      // 查询记录
      queryRecords: [],
      recordsLoading: false,
      recordsFilter: {
        result: null,
        dateRange: null
      },
      recordsPagination: {
        current: 1,
        size: 20,
        total: 0
      },

      // 对话框
      showChainDialog: false,
      currentTraceCode: '',
      showQueryDetailDialog: false,
      currentQueryRecord: null
    }
  },
  mounted() {
    this.loadStatistics()
    this.loadPopularTraceCodes()
    this.loadChannelStats()
    this.loadQueryRecords()
  },
  methods: {
    async loadStatistics() {
      try {
        const response = await getQueryStatistics()
        if (response.code === 200) {
          this.statistics = {
            totalQueries: response.data.totalQueries || 0,
            successQueries: response.data.successQueries || 0,
            uniqueTraceCodes: response.data.uniqueTraceCodes || 0,
            todayQueries: response.data.todayQueries || 0
          }
        }
      } catch (error) {
        console.error('加载统计数据失败', error)
        // 使用模拟数据
        this.statistics = {
          totalQueries: 2340,
          successQueries: 2156,
          uniqueTraceCodes: 89,
          todayQueries: 45
        }
      }
    },

    loadPopularTraceCodes() {
      // 模拟热门溯源码数据
      this.popularTraceCodes = [
        { traceCode: 'BJ2024000001', queryCount: 156 },
        { traceCode: 'SH2024000002', queryCount: 134 },
        { traceCode: 'GZ2024000003', queryCount: 98 },
        { traceCode: 'SZ2024000004', queryCount: 76 },
        { traceCode: 'HZ2024000005', queryCount: 65 }
      ]
    },

    loadChannelStats() {
      // 模拟渠道统计数据
      this.channelStats = {
        miniapp: 1456,
        web: 678,
        app: 206
      }
    },

    loadQueryRecords() {
      this.recordsLoading = true
      // 模拟查询记录数据
      setTimeout(() => {
        this.queryRecords = [
          {
            id: 1,
            traceCode: 'BJ2024000001',
            queryTime: new Date(),
            queryResult: 1,
            queryIp: '192.168.1.100',
            queryChannel: 1,
            responseTime: 245
          },
          {
            id: 2,
            traceCode: 'SH2024000002',
            queryTime: new Date(Date.now() - 3600000),
            queryResult: 1,
            queryIp: '192.168.1.101',
            queryChannel: 2,
            responseTime: 189
          }
        ]
        this.recordsPagination.total = 156
        this.recordsLoading = false
      }, 1000)
    },

    async handleQuickQuery() {
      if (!this.queryForm.traceCode) {
        ElMessage.error('请输入溯源码')
        return
      }

      this.querying = true
      try {
        const response = await queryTraceInfo(this.queryForm.traceCode)
        if (response.code === 200) {
          this.queryResult = {
            success: true,
            data: response.data
          }
        } else {
          this.queryResult = {
            success: false,
            message: response.message
          }
        }
      } catch (error) {
        console.error('查询失败', error)
        this.queryResult = {
          success: false,
          message: '查询失败'
        }
      } finally {
        this.querying = false
      }
    },

    viewFullChain() {
      if (this.queryResult && this.queryResult.data) {
        this.currentTraceCode = this.queryResult.data.traceCode
        this.showChainDialog = true
      }
    },

    viewTraceChain(traceCode) {
      this.currentTraceCode = traceCode
      this.showChainDialog = true
    },

    generateReport() {
      ElMessage.info('报告生成功能开发中...')
    },

    refreshData() {
      this.loadStatistics()
      this.loadPopularTraceCodes()
      this.loadChannelStats()
      this.loadQueryRecords()
      ElMessage.success('数据已刷新')
    },

    exportData() {
      ElMessage.info('数据导出功能开发中...')
    },

    viewQueryDetail(record) {
      // 增强记录数据
      this.currentQueryRecord = {
        ...record,
        queryRegion: this.getRegionFromIp(record.queryIp),
        deviceType: this.getDeviceType(),
        osVersion: this.getOsVersion(),
        browser: this.getBrowserInfo(),
        userAgent: navigator.userAgent,
        queryParams: {
          traceCode: record.traceCode,
          timestamp: record.queryTime,
          channel: record.queryChannel
        },
        responseData: {
          success: record.queryResult === 1,
          message: this.getResultText(record.queryResult),
          recordCount: Math.floor(Math.random() * 10) + 1,
          verified: record.queryResult === 1
        }
      }
      this.showQueryDetailDialog = true
    },

    handleRecordsSizeChange() {
      this.recordsPagination.current = 1
      this.loadQueryRecords()
    },

    handleRecordsCurrentChange() {
      this.loadQueryRecords()
    },

    // 工具方法
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      return new Date(dateTime).toLocaleString('zh-CN')
    },

    getRankClass(index) {
      if (index === 0) return 'gold'
      if (index === 1) return 'silver'
      if (index === 2) return 'bronze'
      return ''
    },

    getResultType(result) {
      const typeMap = {
        1: 'success',
        2: 'danger',
        3: 'warning',
        4: 'danger'
      }
      return typeMap[result] || 'info'
    },

    getResultText(result) {
      const textMap = {
        1: '成功',
        2: '不存在',
        3: '已过期',
        4: '系统错误'
      }
      return textMap[result] || '未知'
    },

    getChannelText(channel) {
      const textMap = {
        1: '小程序扫码',
        2: '小程序输入',
        3: '网页查询',
        4: 'APP查询'
      }
      return textMap[channel] || '未知'
    },

    getResponseTimeType(time) {
      if (time < 200) return 'success'
      if (time < 500) return 'warning'
      return 'danger'
    },

    getRegionFromIp(ip) {
      // 简单的IP地区映射示例
      if (ip.startsWith('192.168')) return '本地网络'
      if (ip.startsWith('10.')) return '内网'
      return '外网'
    },

    getDeviceType() {
      const ua = navigator.userAgent
      if (/(tablet|ipad|playbook|silk)|(android(?!.*mobi))/i.test(ua)) {
        return '平板电脑'
      }
      if (/Mobile|iP(hone|od)|Android|BlackBerry|IEMobile|Kindle|Silk-Accelerated|(hpw|web)OS|Opera M(obi|ini)/.test(ua)) {
        return '手机'
      }
      return '电脑'
    },

    getOsVersion() {
      const ua = navigator.userAgent
      if (ua.includes('Windows NT 10')) return 'Windows 10/11'
      if (ua.includes('Windows NT 6.3')) return 'Windows 8.1'
      if (ua.includes('Windows NT 6.2')) return 'Windows 8'
      if (ua.includes('Windows NT 6.1')) return 'Windows 7'
      if (ua.includes('Mac OS X')) return 'macOS'
      if (ua.includes('Android')) return 'Android'
      if (ua.includes('iOS')) return 'iOS'
      return '未知'
    },

    getBrowserInfo() {
      const ua = navigator.userAgent
      if (ua.includes('Edg/')) return 'Microsoft Edge'
      if (ua.includes('Chrome/')) return 'Google Chrome'
      if (ua.includes('Firefox/')) return 'Mozilla Firefox'
      if (ua.includes('Safari/') && !ua.includes('Chrome')) return 'Safari'
      if (ua.includes('MSIE') || ua.includes('Trident/')) return 'Internet Explorer'
      return '未知'
    },

    formatJson(obj) {
      if (!obj) return '无'
      if (typeof obj === 'string') return obj
      return JSON.stringify(obj, null, 2)
    }
  }
}
</script>

<style scoped>
.trace-query {
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

.statistics-section,
.query-tool-section,
.popular-section,
.channel-section,
.records-section {
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
.stat-icon.unique { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
.stat-icon.today { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }

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

.quick-query-result {
  margin-top: 20px;
}

.popular-list {
  max-height: 400px;
  overflow-y: auto;
}

.popular-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.3s ease;
}

.popular-item:hover {
  background: #f5f7fa;
}

.rank-number {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: white;
  margin-right: 15px;
  background: #909399;
}

.rank-number.gold { background: #f39c12; }
.rank-number.silver { background: #95a5a6; }
.rank-number.bronze { background: #e67e22; }

.trace-info {
  flex: 1;
}

.trace-code {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409eff;
  font-size: 16px;
}

.trace-stats {
  color: #909399;
  font-size: 14px;
  margin-top: 5px;
}

.channel-chart {
  padding: 20px;
}

.chart-placeholder {
  text-align: center;
  padding: 40px;
  background: #f9f9f9;
  border-radius: 8px;
}

.channel-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
}

.channel-item {
  text-align: center;
}

.channel-label {
  color: #606266;
  font-size: 14px;
  margin-bottom: 5px;
}

.channel-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
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

/* 查询详情样式 */
.query-detail {
  padding: 10px 0;
}

.query-detail .el-descriptions {
  margin-bottom: 10px;
}

.query-detail pre {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.detail-actions {
  border-top: 1px solid #eee;
  padding-top: 15px;
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
  
  .popular-item {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }
  
  .channel-stats {
    flex-direction: column;
    gap: 15px;
  }
  
  .query-detail .el-descriptions {
    font-size: 12px;
  }
}
</style>
