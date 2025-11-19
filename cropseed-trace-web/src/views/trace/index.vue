<template>
  <div class="trace-container">
    <div class="trace-header">
      <h2>溯源管理</h2>
      <p class="trace-description">基于区块链技术的农作物种质资源溯源管理系统</p>
    </div>

    <!-- 功能卡片区域 -->
    <div class="trace-cards">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="trace-card" @click="$router.push('/trace/records')">
            <div class="card-icon records">
              <el-icon><Document /></el-icon>
            </div>
            <div class="card-content">
              <h3>溯源记录管理</h3>
              <p>管理完整的溯源记录链条</p>
            </div>
          </div>
        </el-col>

        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="trace-card" @click="$router.push('/trace/codes')">
            <div class="card-icon codes">
              <el-icon><Postcard /></el-icon>
            </div>
            <div class="card-content">
              <h3>溯源码管理</h3>
              <p>生成和管理溯源码</p>
            </div>
          </div>
        </el-col>

        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="trace-card" @click="$router.push('/trace/entities')">
            <div class="card-icon entities">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <div class="card-content">
              <h3>溯源实体管理</h3>
              <p>管理生产基地、加工厂等</p>
            </div>
          </div>
        </el-col>

        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="trace-card" @click="$router.push('/trace/query')">
            <div class="card-icon query">
              <el-icon><Search /></el-icon>
            </div>
            <div class="card-content">
              <h3>溯源查询统计</h3>
              <p>查看溯源查询统计数据</p>
            </div>
          </div>
        </el-col>

        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="trace-card" @click="$router.push('/trace/blockchain')">
            <div class="card-icon blockchain">
              <el-icon><Link /></el-icon>
            </div>
            <div class="card-content">
              <h3>区块链管理</h3>
              <p>查看上链状态和证明</p>
            </div>
          </div>
        </el-col>

        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="trace-card" @click="$router.push('/trace/verify')">
            <div class="card-icon verify">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="card-content">
              <h3>数据验证</h3>
              <p>验证溯源数据完整性</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 最近记录 -->
    <div class="recent-records">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>最近溯源记录</span>
            <el-button type="primary" @click="$router.push('/trace/records')">查看全部</el-button>
          </div>
        </template>

        <el-table :data="recentRecords" style="width: 100%" v-loading="loading">
          <el-table-column prop="traceCode" label="溯源码" width="140" />
          <el-table-column prop="recordStage" label="记录阶段" width="100" />
          <el-table-column prop="operatorName" label="操作员" width="100" />
          <el-table-column prop="recordTime" label="记录时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.recordTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="contentSummary" label="内容摘要" />
          <el-table-column prop="blockchainStatus" label="上链状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getBlockchainStatusType(row.blockchainStatus)">
                {{ getBlockchainStatusText(row.blockchainStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewDetail(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script>
import { Document, Postcard, OfficeBuilding, Search, Link, CircleCheck } from '@element-plus/icons-vue'
import { getTraceRecordsPage, getQueryStatistics } from '@/api/trace'
import { ElMessage } from 'element-plus'

export default {
  name: 'TraceIndex',
  components: {
    Document,
    Postcard,
    OfficeBuilding,
    Search,
    Link,
    CircleCheck
  },
  data() {
    return {
      loading: false,
      statistics: {
        totalRecords: 0,
        totalCodes: 0,
        blockchainRecords: 0,
        queryCount: 0
      },
      recentRecords: []
    }
  },
  mounted() {
    this.loadStatistics()
    this.loadRecentRecords()
  },
  methods: {
    async loadStatistics() {
      try {
        // 加载统计数据
        const response = await getQueryStatistics()
        if (response.code === 200) {
          this.statistics = {
            totalRecords: response.data.totalRecords || 0,
            totalCodes: response.data.totalCodes || 0,
            blockchainRecords: response.data.blockchainRecords || 0,
            queryCount: response.data.queryCount || 0
          }
        }
      } catch (error) {
        console.warn('加载统计数据失败', error)
        // 使用模拟数据
        this.statistics = {
          totalRecords: 156,
          totalCodes: 89,
          blockchainRecords: 134,
          queryCount: 2340
        }
      }
    },

    async loadRecentRecords() {
      this.loading = true
      try {
        const response = await getTraceRecordsPage({
          pageNum: 1,
          pageSize: 10
        })
        if (response.code === 200) {
          this.recentRecords = response.data.list || []
        }
      } catch (error) {
        console.error('加载最近记录失败', error)
        ElMessage.error('加载最近记录失败')
      } finally {
        this.loading = false
      }
    },

    viewDetail(record) {
      this.$router.push(`/trace/records/${record.id}`)
    },

    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      return new Date(dateTime).toLocaleString('zh-CN')
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
.trace-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 100px);
}

.trace-header {
  text-align: center;
  margin-bottom: 30px;
}

.trace-header h2 {
  color: #303133;
  margin-bottom: 8px;
  font-size: 28px;
  font-weight: 600;
}

.trace-description {
  color: #606266;
  font-size: 16px;
  margin: 0;
}

.trace-cards {
  margin-bottom: 30px;
}

.trace-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  height: 120px;
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.trace-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.card-icon.records { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.card-icon.codes { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.card-icon.entities { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.card-icon.query { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.card-icon.blockchain { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
.card-icon.verify { background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); }

.card-content h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
  font-weight: 600;
}

.card-content p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.trace-statistics {
  margin-bottom: 30px;
}

.stat-card {
  height: 100px;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 10px;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.recent-records {
  background: white;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

@media (max-width: 768px) {
  .trace-container {
    padding: 10px;
  }
  
  .trace-card {
    height: auto;
    flex-direction: column;
    text-align: center;
    padding: 20px;
  }
  
  .card-icon {
    margin-right: 0;
    margin-bottom: 12px;
  }
  
  .trace-header h2 {
    font-size: 24px;
  }
}
</style>
